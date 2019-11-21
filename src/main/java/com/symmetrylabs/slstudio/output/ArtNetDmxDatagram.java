package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.component.GammaExpander;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;

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

    private int unmappedPointColor = 0x000000;
    private boolean flashUnmapped = false;
    private boolean flashInOn = true;
    private long lastFlashNanos = System.nanoTime();

    private GammaExpander GammaExpander;
    private BooleanParameter testDataLine = new BooleanParameter("Test Dataline", true);

    public ArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int universeNumber) {
        this(lx, ipAddress, indices, 3 * indices.length, universeNumber);
    }

    public ArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int dataLength, int universeNumber) {
        super(ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH + dataLength + (dataLength % 2));

        this.pointIndices = indices;

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
        if (testDataLine.isOn()){
            copyTestPoints(this.pointIndices.length, ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH);
        }
        else{
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
    }

    private void copyTestPoints(int length, int j) {
        long millis = System.currentTimeMillis();
        int PERIOD = 3000; // 2 seconds

        long limitLightUp = (millis%PERIOD) / 20;
        for (int i = 0; i < length; i++) {
            buffer[i + 0] = (byte) 0xff;
            buffer[i + 1] = (byte) 0x00;
            buffer[i + 2] = (byte) 0xff;
            if (i > limitLightUp){
                buffer[i + 0] = (byte) 0x0;
                buffer[i + 1] = (byte) 0xff;
                buffer[i + 2] = (byte) 0x0;
            }

            i += 3;
        }
    }

    LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset) {
        int i = offset;
        int[] byteOffset = BYTE_ORDERING[this.byteOrder.ordinal()];
        int unmappedC = flashUnmapped && !flashInOn ? 0 : unmappedPointColor;
        if (System.nanoTime() - lastFlashNanos > FLASH_NANOS) {
            lastFlashNanos = System.nanoTime();
            flashInOn = !flashInOn;
        }
        for (int index : pointIndices) {
            int colorValue = (index >= 0) ? colors[index] : unmappedC;

            int gammaExpanded = GammaExpander.getExpandedColor(colorValue);
            buffer[i + byteOffset[0]] = (byte) Ops8.red(gammaExpanded);
            buffer[i + byteOffset[1]] = (byte) Ops8.green(gammaExpanded);
            buffer[i + byteOffset[2]] = (byte) Ops8.blue(gammaExpanded);

            i += 3;
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
