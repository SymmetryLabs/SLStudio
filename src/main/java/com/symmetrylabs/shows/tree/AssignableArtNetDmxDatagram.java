package com.symmetrylabs.shows.tree;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.component.GammaExpander;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.model.LXPoint;

import java.net.UnknownHostException;

public class AssignableArtNetDmxDatagram extends LXDatagram {

    private final static int DEFAULT_UNIVERSE = 0;
    private final static int ARTNET_HEADER_LENGTH = 18;
    private final static int ARTNET_PORT = 6454;
    private final static int SEQUENCE_INDEX = 12;

    public String ipAddress;
    private int[] pointIndices;
    public int universe = 0;
    private boolean sequenceEnabled = false;
    private byte sequence = 1;

    private GammaExpander GammaExpander;

    public AssignableArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int universeNumber) {
        this(lx, ipAddress, indices, 3 * indices.length, universeNumber);
    }

    public AssignableArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int dataLength, int universeNumber) {
        super(ARTNET_HEADER_LENGTH + dataLength + (dataLength % 2));
        this.ipAddress = ipAddress;
        this.pointIndices = indices;
        this.universe = universeNumber;

        GammaExpander = GammaExpander.getInstance(lx);

        try {
            setAddress(ipAddress);
            setPort(ARTNET_PORT);
        } catch (UnknownHostException e) {
            //System.out.println("MappingPixlite with ip address (" + ipAddress + ") is not on the network.");
        }

        this.buffer[0] = 'A';
        this.buffer[1] = 'r';
        this.buffer[2] = 't';
        this.buffer[3] = '-';
        this.buffer[4] = 'N';
        this.buffer[5] = 'e';
        this.buffer[6] = 't';
        this.buffer[7] = 0;
        this.buffer[8] = 0x00; // ArtDMX opcode
        this.buffer[9] = 0x50; // ArtDMX opcode
        this.buffer[10] = 0; // Protcol version
        this.buffer[11] = 14; // Protcol version
        this.buffer[12] = 0; // Sequence
        this.buffer[13] = 0; // Physical
        this.buffer[14] = (byte) (universeNumber & 0xff); // Universe LSB
        this.buffer[15] = (byte) ((universeNumber >>> 8) & 0xff); // Universe MSB
        this.buffer[16] = (byte) ((dataLength >>> 8) & 0xff);
        this.buffer[17] = (byte) (dataLength & 0xff);

        for (int i = ARTNET_HEADER_LENGTH; i < this.buffer.length; ++i) {
            this.buffer[i] = 0;
        }
    }

    public AssignableArtNetDmxDatagram setSequenceEnabled(boolean sequenceEnabled) {
        this.sequenceEnabled = sequenceEnabled;
        return this;
    }

    public void setIndices(int[] indices) {
        this.pointIndices = indices;
    }

    @Override
    public void onSend(int[] colors) {
        copyPointsGamma(colors, this.pointIndices, ARTNET_HEADER_LENGTH);

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

    LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset) {
        int i = offset;
        int[] byteOffset = BYTE_ORDERING[this.byteOrder.ordinal()];
        for (int index : pointIndices) {
            int colorValue = (index >= 0) ? colors[index] : 0;

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
