package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.component.GammaExpander;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.model.LXPoint;

import java.net.UnknownHostException;

public class ArtNetDmxDatagram extends LXDatagram {
    private static final int ARTNET_DMX_HEADER_LENGTH = 6;
    private static final short ARTNET_DMX_OPCODE = 0x5000;
    private static final int SEQUENCE_INDEX = 12;

    private final static int DEFAULT_UNIVERSE = 0;
    private final static long FLASH_NANOS = 100_000_000;

    private int[] pointIndices;
    private boolean sequenceEnabled = false;
    private byte sequence = 1;

    private int universeNumber;

    private int unmappedPointColor = 0x000000;
    private boolean flashUnmapped = false;
    private boolean flashInOn = true;
    private long lastFlashNanos = System.nanoTime();

    private GammaExpander GammaExpander;

    private boolean isCustomUniverse() {
        return ((this.universeNumber >= 79 && this.universeNumber <= 88) || //8
                (this.universeNumber >= 199 && this.universeNumber <= 208) ||    //20
                (this.universeNumber >= 209 && this.universeNumber <= 218) || //21
                (this.universeNumber >= 249 && this.universeNumber <= 258)   //25

        );
    }

    // Method to calculate buffer size
    private static int calcBufferSize(int[] indices, int universeNumber) {
        boolean isCustomUniverse = isCustomUniverse(universeNumber);
        int multiplier = isCustomUniverse ? 8 : 3;
        return multiplier * indices.length;
    }


   // Static helper method to determine if a given universe number is custom
    private static boolean isCustomUniverse(int universeNumber) {
        return ((universeNumber >= 79 && universeNumber <= 88) ||   //8
                (universeNumber >= 199 && universeNumber <= 208) || //20
                (universeNumber >= 209 && universeNumber <= 218) || //21
                (universeNumber >= 249 && universeNumber <= 258)    //25

        );
    }

    // Constructors
    public ArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int universeNumber) {
        this(lx, ipAddress, indices, calcBufferSize(indices, universeNumber), universeNumber);
    }

    public ArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int dataLength, int universeNumber) {
        super(ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH + dataLength + (dataLength % 2));

        this.pointIndices = indices;
        this.universeNumber = universeNumber;

        GammaExpander = GammaExpander.getInstance(lx);

        try {
            setAddress(ipAddress);
            setPort(ArtNetDatagramUtil.ARTNET_PORT);
        } catch (UnknownHostException e) {
            //System.out.println("MappingPixlite with ip address (" + ipAddress + ") is not on the network.");
        }

        ArtNetDatagramUtil.fillHeader(buffer, ARTNET_DMX_OPCODE);
        this.buffer[12] = 0; // Sequence
        this.buffer[13] = 0; // Physical
        this.buffer[14] = (byte) (universeNumber & 0xff); // Universe LSB
        this.buffer[15] = (byte) ((universeNumber >>> 8) & 0xff); // Universe MSB
        this.buffer[16] = (byte) ((dataLength >>> 8) & 0xff);
        this.buffer[17] = (byte) (dataLength & 0xff);
    }

    public ArtNetDmxDatagram setUnmappedPointColor(int c, boolean flash) {
        unmappedPointColor = c;
        flashUnmapped = flash;
        return this;
    }

    public ArtNetDmxDatagram setSequenceEnabled(boolean sequenceEnabled) {
        this.sequenceEnabled = sequenceEnabled;
        return this;
    }

    public void setUniverse(int universe) {
        this.buffer[14] = (byte) (universe & 0xff); // Universe LSB
        this.buffer[15] = (byte) ((universe >>> 8) & 0xff); // Universe MSB
    }

    public void updatePoints(LXPoint[] points) {
        // FINISH - need to refactor and have datagram buffer adapt size
        int[] indices = new int[points.length];
        int i = 0;
        for (LXPoint p : points) {
            indices[i++] = p.index;
        }
        this.pointIndices = indices;
    }

    public void setIndices(int[] indices) {
        this.pointIndices = indices;
    }

    @Override
    public void onSend(int[] colors) {
        copyPointsGamma(
            colors, this.pointIndices, ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH);

        if (this.sequenceEnabled) {
            if (++this.sequence == 0) {
                ++this.sequence;
            }
            this.buffer[SEQUENCE_INDEX] = this.sequence;
        }

        // We need to slow down the speed at which we send the packets so that we don't overload our switches. 3us seems to
        // be about right - Yona
        busySleep(3000);
    }

public LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset) {
    boolean isCustomUniverse = (
                (this.universeNumber >= 79 && this.universeNumber <= 88) || //8
                (this.universeNumber >= 199 && this.universeNumber <= 208) ||  //20
                (this.universeNumber >= 209 && this.universeNumber <= 218) ||  //21
                (this.universeNumber >= 249 && this.universeNumber <= 258));  //21
    int unmappedC = flashUnmapped && !flashInOn ? 0 : unmappedPointColor;
 
    if (System.nanoTime() - lastFlashNanos > FLASH_NANOS) {
        lastFlashNanos = System.nanoTime();
        flashInOn = !flashInOn;
    }
 
    int channelIndex = offset;
    for (int index : pointIndices) {
        int colorValue = (index >= 0) ? colors[index] : unmappedC;
        int gammaExpanded = GammaExpander.getExpandedColor(colorValue);
        

        if (isCustomUniverse) {
            buffer[channelIndex++] = (byte) 255; // master dimmer
            buffer[channelIndex++] = (byte) 0; // skip
            buffer[channelIndex++] = (byte) 0; // skip
            buffer[channelIndex++] = (byte) 0; // skip

        }

        buffer[channelIndex++] = (byte) Ops8.red(gammaExpanded);
        buffer[channelIndex++] = (byte) Ops8.green(gammaExpanded);
        buffer[channelIndex++] = (byte) Ops8.blue(gammaExpanded);


        if (isCustomUniverse) {
            buffer[channelIndex++] = (byte) 0; // skip
        }
 

        
    }
 
    return this;
}

    public void busySleep(long nanos) {
        long elapsed;
        final long startTime = System.nanoTime();
        do {
            elapsed = System.nanoTime() - startTime;
        } while (elapsed < nanos);
    }
}
