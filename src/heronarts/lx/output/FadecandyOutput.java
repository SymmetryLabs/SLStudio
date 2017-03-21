/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
            this.firmwareConfig |= 0x08;
        } else {
            this.firmwareConfig &= ~0x08;
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
