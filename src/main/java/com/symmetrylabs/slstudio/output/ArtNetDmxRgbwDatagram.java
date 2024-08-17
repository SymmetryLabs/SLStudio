package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.model.LXPoint;

public class ArtNetDmxRgbwDatagram extends ArtNetDmxDatagram {
    private static final int BYTES_PER_PIXEL = 4;

    public ArtNetDmxRgbwDatagram(LX lx, String ipAddress, int[] indices, int universeNumber) {
        this(lx, ipAddress, indices, BYTES_PER_PIXEL * indices.length, universeNumber);
    }

    protected ArtNetDmxRgbwDatagram(LX lx, String ipAddress, int[] indices, int dataLength, int universeNumber) {
        super(lx, ipAddress, indices, dataLength, universeNumber);
    }

    @Override
    protected LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset, int unmappedColor) {
        int channelIndex = offset;

        assert BYTES_PER_PIXEL == 4;

        for (int index : pointIndices) {
            if (channelIndex + BYTES_PER_PIXEL > buffer.length) {
                throw new ArrayIndexOutOfBoundsException("Buffer overflow at channelIndex: " + channelIndex + ", buffer length: " + buffer.length);
            }

            int colorValue = (index >= 0) ? colors[index] : unmappedColor;

            int gammaExpanded = gammaExpander.getExpandedColor(colorValue);
            int r = Ops8.red(gammaExpanded);
            int g = Ops8.green(gammaExpanded);
            int b = Ops8.blue(gammaExpanded);
            int w = r < g ? r : g;
            if (b < w) {
                w = b;
            }
            //System.out.print("r="+r+" g="+g+" b="+b+" w=" + w);
            r -= w;
            g -= w;
            b -= w;
            //System.out.println(" r_="+r+" g_="+g+" b_="+b);

            buffer[channelIndex++] = (byte)r;
            buffer[channelIndex++] = (byte)g;
            buffer[channelIndex++] = (byte)b;
            buffer[channelIndex++] = (byte)w;
        }

        return this;
    }
}
