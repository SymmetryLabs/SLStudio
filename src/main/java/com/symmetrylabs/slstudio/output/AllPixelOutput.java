package com.symmetrylabs.slstudio.output;

import com.fazecast.jSerialComm.SerialPort;
import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.output.LXOutput;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import com.symmetrylabs.color.Spaces;

public class AllPixelOutput extends LXOutput {
    public enum LedType {
        LPD8806(1),
        WS2801(2),
        WS2811(3),
        WS2811_400(4),
        TM1809_TM1804(5),
        TM1803(6),
        UCS1903(7),
        SM16716(8),
        APA102(9),
        LPD1886(10),
        P9813(11);

        public final int index;
        LedType(int index) {
            this.index = index;
        }
    }

    private static final int READ_TIMEOUT_MS = 100;
    private static final int WRITE_TIMEOUT_MS = 500;

    protected final LX lx;
    protected SerialPort port;
    private final byte[] dataMessage;
    private final LedType ledType;
    private final int[] channelOrder;
    private boolean isRebooting = false;

    public AllPixelOutput(LX lx) {
        this(lx, LedType.APA102, new int[] {0, 1, 2});
    }

    public AllPixelOutput(LX lx, LedType ledType, int[] channelOrder) {
        super(lx);
        this.lx = lx;
        this.port = null;
        this.dataMessage = new byte[3 * lx.model.size + 3];
        this.ledType = ledType;
        this.channelOrder = channelOrder;

        /* we send brightness via setBrightness, so we don't want to pre-mix brightness values
             and decrease our dynamic range */
        mode.setValue(Mode.RAW);

        if (lx.model.size * 3 > 0xFFFF) {
            throw new RuntimeException("model data size (3 * model size) must fit in 16 bits, got " + (3 * lx.model.size));
        }

        /* TODO: figure out how to get serial ports by USB device
             vendor/product ID pair */
        for (SerialPort port : SerialPort.getCommPorts()) {
            if ("AVR USB Serial".equals(port.getPortDescription())) {
                this.port = port;
                break;
            }
        }
        if (this.port == null) {
            SLStudio.setWarning("AllPixelOutput", "no AllPixel device found");
            enabled.setValue(false);
        } else {
            sendConfigureCommand();
        }
    }

    private boolean openPort() {
        port.setBaudRate(921600);
        port.setComPortTimeouts(
            SerialPort.TIMEOUT_WRITE_BLOCKING | SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
            READ_TIMEOUT_MS, WRITE_TIMEOUT_MS);
        return port.openPort();
    }

    protected void sendConfigureCommand() {
        if (!port.isOpen()) {
            port.setBaudRate(921600);
            port.setComPortTimeouts(
                SerialPort.TIMEOUT_WRITE_BLOCKING | SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                READ_TIMEOUT_MS, WRITE_TIMEOUT_MS);
            boolean open = port.openPort();
            if (!open && !isRebooting) {
                SLStudio.setWarning("AllPixelOutput", "could not open serial port");
                enabled.setValue(false);
            }
            if (!open) {
                return;
            }
        }
        byte[] configCmd = new byte[7];
        int dataSize = 3 * lx.model.size;
        configCmd[0] = 1; // SETUP_DATA command
        configCmd[1] = 4; // low byte of message length
        configCmd[2] = 0; // high byte of message length
        configCmd[3] = (byte) ledType.index;
        configCmd[4] = (byte) (dataSize & 0xFF);
        configCmd[5] = (byte) ((dataSize >> 8) & 0xFF);
        configCmd[6] = 16; // SPI speed in MHz
        sendMessage(configCmd);
    }

    protected void sendBrightness(int brightness) {
        brightness = brightness < 0 ? 0 : brightness > 255 ? 255 : brightness;
        byte[] brightCmd = new byte[4];
        brightCmd[0] = 3; // BRIGHTNESS command
        brightCmd[1] = 1; // low byte of message length
        brightCmd[2] = 0; // high byte of message length
        brightCmd[3] = (byte) brightness;
        sendMessage(brightCmd);
    }

    @Override
    public void onSend(PolyBuffer buf) {
        if (isRebooting) {
            sendConfigureCommand();
            return;
        }
        double br = brightness.getValue();
        /* if we put master output in raw mode (desirable!) we can use non-prescaled colors and then
             scale the global brightness by the master output level */
        if (lx.engine.output.mode.getEnum() == Mode.RAW) {
            br *= lx.engine.output.brightness.getValue();
        }
        double lum = Spaces.cie_lightness_to_luminance(br);
        sendBrightness((int) (lum * 255));
        int dataSize = 3 * lx.model.size;
        if (dataMessage[0] == 0) {
            /* this means the message hasn't been set up yet; write in the header now */
            dataMessage[0] = 2; // PIXEL_DATA command
            dataMessage[1] = (byte) (dataSize & 0xFF);
            dataMessage[2] = (byte) ((dataSize >> 8) & 0xFF);
        }
        int[] cdata = (int[]) buf.getArray(PolyBuffer.Space.RGB8);
        byte[] rgb = new byte[3];
        for (int i = 0; i < cdata.length; i++) {
            int c = cdata[i];
            rgb[0] = (byte) ((c >> 16) & 0xFF);
            rgb[1] = (byte) ((c >>  8) & 0xFF);
            rgb[2] = (byte) ((c      ) & 0xFF);
            dataMessage[3 + 3 * i + 0] = rgb[channelOrder[0]];
            dataMessage[3 + 3 * i + 1] = rgb[channelOrder[1]];
            dataMessage[3 + 3 * i + 2] = rgb[channelOrder[2]];
        }
        sendMessage(dataMessage);
    }

    protected void sendMessage(byte[] msg) {
        int written = port.writeBytes(msg, msg.length);
        if (written != msg.length) {
            SLStudio.setWarning(
                "AllPixelOutput",
                String.format("failed to write %d bytes, only wrote %d", msg.length, written));
        }
        byte[] buf = new byte[1];
        int read = port.readBytes(buf, 1);
        if (read != 1) {
            if (!isRebooting) {
                SLStudio.setWarning("AllPixelOutput", "output is unresponsive");
            }
            return;
        }
        isRebooting = false;
        int ret = Byte.toUnsignedInt(buf[0]);
        int msgCode = Byte.toUnsignedInt(msg[0]);
        switch (ret) {
        case 255:
            SLStudio.setWarning("AllPixelOutput", null); // all is good
            return;
        case 42:
            SLStudio.setWarning("AllPixelOutput", "device is rebooting, sends may fail");
            isRebooting = true;
            return;
        case 1:
            SLStudio.setWarning(
                "AllPixelOutput",
                String.format("message size error returned from device on message code %d", msgCode));
            return;
        case 2:
            SLStudio.setWarning(
                "AllPixelOutput",
                String.format("unsupported command error returned from device on message code %d", msgCode));
            return;
        case 3:
            SLStudio.setWarning("AllPixelOutput", "too-many-pixels error returned from device");
            return;
        case 0:
            SLStudio.setWarning(
                "AllPixelOutput",
                String.format("generic error returned from device on message code %d", msgCode));
            return;
        default:
            SLStudio.setWarning(
                "AllPixelOutput",
                String.format("unknown error %d returned from device on message code %d", ret, msgCode));
            return;
        }
    }

    public static void configureMasterOutput(LX lx) {
        lx.engine.output.mode.setValue(Mode.RAW);
    }
}
