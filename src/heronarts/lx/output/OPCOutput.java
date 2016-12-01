/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.output;

import heronarts.lx.LX;

public class OPCOutput extends LXSocketOutput {

    static final int HEADER_LEN = 4;

    static final int BYTES_PER_PIXEL = 3;

    static final int INDEX_CHANNEL = 0;
    static final int INDEX_COMMAND = 1;
    static final int INDEX_DATA_LEN_MSB = 2;
    static final int INDEX_DATA_LEN_LSB = 3;
    static final int INDEX_DATA = 4;

    static final int OFFSET_R = 0;
    static final int OFFSET_G = 1;
    static final int OFFSET_B = 2;

    static final int COMMAND_SET_PIXEL_COLORS = 0;

    private final byte[] packetData;

    private final int[] pointIndices;

    private static int[] allPoints(LX lx) {
        int[] points = new int[lx.total];
        for (int i = 0; i < points.length; ++i) {
            points[i] = i;
        }
        return points;
    }

    public OPCOutput(LX lx, String host, int port) {
        this(lx, host, port, allPoints(lx));
    }

    public OPCOutput(LX lx, String host, int port, int[] pointIndices) {
        super(lx, host, port);
        this.pointIndices = pointIndices;

        int dataLength = BYTES_PER_PIXEL*pointIndices.length;
        this.packetData = new byte[HEADER_LEN + dataLength];
        this.packetData[INDEX_CHANNEL] = 0;
        this.packetData[INDEX_COMMAND] = COMMAND_SET_PIXEL_COLORS;
        this.packetData[INDEX_DATA_LEN_MSB] = (byte)(dataLength >>> 8);
        this.packetData[INDEX_DATA_LEN_LSB] = (byte)(dataLength & 0xFF);
    }

    @Override
    protected byte[] getPacketData(int[] colors) {
        for (int i = 0; i < this.pointIndices.length; ++i) {
            int dataOffset = INDEX_DATA + i * BYTES_PER_PIXEL;
            int c = colors[this.pointIndices[i]];
            this.packetData[dataOffset + OFFSET_R] = (byte) (0xFF & (c >> 16));
            this.packetData[dataOffset + OFFSET_G] = (byte) (0xFF & (c >> 8));
            this.packetData[dataOffset + OFFSET_B] = (byte) (0xFF & c);
        }
        return this.packetData;
    }

    public OPCOutput setChannel(int channel) {
        this.packetData[INDEX_CHANNEL] = (byte) channel;
        return this;
    }

}
