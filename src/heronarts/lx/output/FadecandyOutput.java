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

import java.io.IOException;

import heronarts.lx.LX;

public class FadecandyOutput extends OPCOutput {

    private byte firmwareConfig = 0x00;

    private String colorCorrection = null;

    public FadecandyOutput(LX lx, String host, int port) {
        super(lx, host, port);
    }

    public FadecandyOutput(LX lx, String host, int port, int[] pointIndices) {
        super(lx, host, port, pointIndices);
    }

    @Override
    protected void didConnect() {
        sendColorCorrectionPacket();
        sendFirmwareConfigPacket();
    }

    public FadecandyOutput setDithering(boolean enabled) {
        if (enabled) {
            this.firmwareConfig &= ~0x01;
        } else {
            this.firmwareConfig |= 0x01;
        }
        sendFirmwareConfigPacket();
        return this;
    }

    public FadecandyOutput setInterpolation(boolean enabled) {
        if (enabled) {
            this.firmwareConfig &= ~0x02;
        } else {
            this.firmwareConfig |= 0x02;
        }
        sendFirmwareConfigPacket();
        return this;
    }

    public FadecandyOutput setStatusLedAuto() {
        this.firmwareConfig &= 0x0C;
        sendFirmwareConfigPacket();
        return this;
    }

    public FadecandyOutput setStatusLed(boolean on) {
        this.firmwareConfig |= 0x04; // Manual LED control
        if (on) {
            firmwareConfig |= 0x08;
        } else {
            firmwareConfig &= ~0x08;
        }
        sendFirmwareConfigPacket();
        return this;
    }

    private final byte[] firmwarePacket = new byte[9];

    private void sendFirmwareConfigPacket() {
        if (!isConnected()) {
            // We'll do this when we reconnect
            return;
        }

        this.firmwarePacket[0] = 0;          // Channel (reserved)
        this.firmwarePacket[1] = (byte)0xFF; // Command (System Exclusive)
        this.firmwarePacket[2] = 0;          // Length high byte
        this.firmwarePacket[3] = 5;          // Length low byte
        this.firmwarePacket[4] = 0x00;       // System ID high byte
        this.firmwarePacket[5] = 0x01;       // System ID low byte
        this.firmwarePacket[6] = 0x00;       // Command ID high byte
        this.firmwarePacket[7] = 0x02;       // Command ID low byte
        this.firmwarePacket[8] = this.firmwareConfig;

        try {
            this.output.write(this.firmwarePacket);
        } catch (IOException iox) {
            dispose(iox);
        }
    }

    public FadecandyOutput setColorCorrection(float gamma, float red, float green, float blue) {
        this.colorCorrection = "{ \"gamma\": " + gamma + ", \"whitepoint\": [" + red + "," + green + "," + blue + "]}";
        sendColorCorrectionPacket();
        return this;
    }

    public FadecandyOutput setColorCorrection(String s) {
        this.colorCorrection = s;
        sendColorCorrectionPacket();
        return this;
    }

    private void sendColorCorrectionPacket() {
        if ((this.colorCorrection == null) || !isConnected()) {
            return;
        }

        byte[] content = this.colorCorrection.getBytes();
        int packetLen = content.length + 4;
        byte[] header = new byte[8];
        header[0] = 0;          // Channel (reserved)
        header[1] = (byte)0xFF; // Command (System Exclusive)
        header[2] = (byte)(packetLen >> 8);
        header[3] = (byte)(packetLen & 0xFF);
        header[4] = 0x00;       // System ID high byte
        header[5] = 0x01;       // System ID low byte
        header[6] = 0x00;       // Command ID high byte
        header[7] = 0x01;       // Command ID low byte

        try {
            this.output.write(header);
            this.output.write(content);
        } catch (IOException iox) {
            dispose(iox);
        }
    }

}
