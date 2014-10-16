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

/**
 * Distributed Display Protocol is a simple protocol developed by 3waylabs. It's
 * a simple framing of raw color buffers, without DMX size limitations.
 * 
 * The specification is available at http://www.3waylabs.com/ddp/
 */
public class DDPDatagram extends LXDatagram {

    private static final int HEADER_LENGTH = 10;
    private static final int DEFAULT_PORT = 4048;

    private final int[] pointIndices;

    public DDPDatagram(int[] pointIndices) {
        super(HEADER_LENGTH + pointIndices.length * 3);
        setPort(DEFAULT_PORT);
        int dataLen = pointIndices.length * 3;
        this.pointIndices = pointIndices;

        // Flags: V V x T S R Q P
        this.buffer[0] = 0x41;

        // Reserved
        this.buffer[1] = 0x00;

        // Data type
        this.buffer[2] = 0x00;

        // Destination ID, default
        this.buffer[3] = 0x01;

        // Data offset
        this.buffer[4] = 0x00;
        this.buffer[5] = 0x00;
        this.buffer[6] = 0x00;
        this.buffer[7] = 0x00;

        // Data length
        this.buffer[8] = (byte) (0xff & (dataLen >> 8));
        this.buffer[9] = (byte) (0xff & dataLen);
    }

    public void onSend(int[] colors) {
        copyPoints(colors, this.pointIndices, HEADER_LENGTH);
    }
}
