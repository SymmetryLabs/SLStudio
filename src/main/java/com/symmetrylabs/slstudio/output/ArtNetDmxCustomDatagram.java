package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.model.LXPoint;

import java.net.UnknownHostException;

public class ArtNetDmxCustomDatagram extends ArtNetDmxDatagram {
    protected static int calcBufferSize(int[] indices, int universeNumber) {
        boolean isCustom = isCustomUniverse(universeNumber);
        int multiplier = isCustom ? 8 : 3;
        int additionalBytes = isCustom ? 10 : 0; // Include extra space for the custom universe handling
        return (multiplier * indices.length) + additionalBytes + 10; // Add a small extra buffer of 10 bytes
    }

    public ArtNetDmxCustomDatagram(LX lx, String ipAddress, int[] indices, int universeNumber) {
        this(lx, ipAddress, indices, calcBufferSize(indices, universeNumber), universeNumber);
    }

    protected ArtNetDmxCustomDatagram(LX lx, String ipAddress, int[] indices, int dataLength, int universeNumber) {
        super(lx, ipAddress, indices, dataLength, universeNumber);
    }

    // Helper method to determine if a given universe number is custom
    private static boolean isCustomUniverse(int universeNumber) {
        return (
            //(universeNumber >= 9 && universeNumber <= 18) ||   //1
            (universeNumber >= 159 && universeNumber <= 168) ||   //16
            (universeNumber >= 199 && universeNumber <= 208) || //20
            (universeNumber >= 209 && universeNumber <= 218) || //21
            (universeNumber >= 249 && universeNumber <= 258)    //25
        );
    }

    @Override
    protected LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset, int unmappedColor) {
        boolean isCustom = isCustomUniverse(this.universeNumber);
        int channelIndex = offset;
        for (int index : pointIndices) {
            // Buffer overflow check
            if (channelIndex + (isCustom ? 8 : 3) >= buffer.length) {
                throw new ArrayIndexOutOfBoundsException("Buffer overflow at channelIndex: " + channelIndex + ", buffer length: " + buffer.length);
            }

            int colorValue = (index >= 0) ? colors[index] : unmappedColor;
            int gammaExpanded = gammaExpander.getExpandedColor(colorValue);

            if (isCustom) {
                buffer[channelIndex++] = (byte) 255; // master dimmer
            }

            buffer[channelIndex++] = (byte) Ops8.red(gammaExpanded);
            buffer[channelIndex++] = (byte) Ops8.green(gammaExpanded);
            buffer[channelIndex++] = (byte) Ops8.blue(gammaExpanded);

            if (isCustom) {
                buffer[channelIndex++] = (byte) 0; // skip
                buffer[channelIndex++] = (byte) Ops8.red(gammaExpanded);
                buffer[channelIndex++] = (byte) 0; // skip
                buffer[channelIndex++] = (byte) 0; // skip
                buffer[channelIndex++] = (byte) 0; // skip
                buffer[channelIndex++] = (byte) 0; // skip
            }
        }

        return this;
    }
}
