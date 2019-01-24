package com.symmetrylabs.slstudio.output;

import com.fazecast.jSerialComm.SerialPort;
import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.output.LXOutput;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import com.symmetrylabs.color.Spaces;

/**
 * LXOutput module for controlling Maniacal Labs AllPixelMini USB-serial-to-SPI devices
 *
 * The AllPixel Mini is a cheap device that can control up to 750 RGB LEDs with a global
 * brightness prescaler for HDR support.
 *
 * @see <a href="https://www.tindie.com/products/ManiacalLabs/allpixelmini-universal-led-controller/">Tindie page for the AllPixelMini controller</a>
 */
public class AllPixelOutput extends LXOutput {
    public enum LedType {
        /* These constants are set up to match the numerical constants in the AllPixel's firmware */
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
    private String currentWarning = null;

    public AllPixelOutput(LX lx) {
        this(lx, LedType.APA102, new int[] {0, 1, 2});
    }

    /**
     * @param lx the lx instance to bind to
     * @param ledType the type of LED that the AllPixelMini is controlling
     * @param channelOrder the order in which to send color channels. {0, 1, 2} means send RGB, {2, 0, 1} means send BRG, etc
     */
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
        discoverPort();
    }

    private void discoverPort() {
        /* TODO: figure out how to get serial ports by USB device
           vendor/product ID pair */
        for (SerialPort port : SerialPort.getCommPorts()) {
            if ("AVR USB Serial".equals(port.getPortDescription())) {
                this.port = port;
                break;
            }
        }
        if (this.port == null) {
            warn("no AllPixel device found");
        } else {
            sendConfigureCommand();
        }
    }

    private boolean openPort() {
        if (port == null) {
            return false;
        }
        port.setBaudRate(921600);
        port.setComPortTimeouts(
            SerialPort.TIMEOUT_WRITE_BLOCKING | SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
            READ_TIMEOUT_MS, WRITE_TIMEOUT_MS);
        return port.openPort();
    }

    protected void sendConfigureCommand() {
        if (port == null || !port.isOpen()) {
            port.setBaudRate(921600);
            port.setComPortTimeouts(
                SerialPort.TIMEOUT_WRITE_BLOCKING | SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                READ_TIMEOUT_MS, WRITE_TIMEOUT_MS);
            boolean open = port.openPort();
            if (!open && !isRebooting) {
                warn("could not open serial port");
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
        if (port == null) {
            discoverPort();
            if (port == null) {
                return;
            } else {
                sendConfigureCommand();
            }
        }
        if (isRebooting) {
            sendConfigureCommand();
            return;
        }
        double br = brightness.getValue();
        /* if we put master output in raw mode we can use non-prescaled colors and then
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
        if (port == null) {
            return;
        }
        int written = port.writeBytes(msg, msg.length);
        if (written != msg.length) {
            warn("failed to write %d bytes, only wrote %d", msg.length, written);
        }
        byte[] buf = new byte[1];
        int read = port.readBytes(buf, 1);
        if (read != 1) {
            if (!isRebooting) {
                port = null;
                warn("output is unresponsive");
            }
            return;
        }
        isRebooting = false;
        int ret = Byte.toUnsignedInt(buf[0]);
        int msgCode = Byte.toUnsignedInt(msg[0]);
        switch (ret) {
        case 255:
            warn(null);
            return;
        case 42:
            warn("device is rebooting, sends may fail");
            isRebooting = true;
            return;
        case 1:
            warn("message size error returned from device on message code %d", msgCode);
            return;
        case 2:
            warn("unsupported command error returned from device on message code %d", msgCode);
            return;
        case 3:
            warn("too-many-pixels error returned from device");
            return;
        case 0:
            warn("generic error returned from device on message code %d", msgCode);
            return;
        default:
            warn("unknown error %d returned from device on message code %d", ret, msgCode);
            return;
        }
    }

    /**
     * Configures master output for pseudo-hidpi support with the AllPixel controller.
     *
     * The AllPixel only supports 8-bit color, but it does support an additional
     * 8-bit global brightness value. We set that brightness value to match the
     * master fader of the engine, so that we can have nice colors at the lower
     * end of the global brightness scale.
     *
     * For that to work, though, we don't want the master channel to prescale
     * the color values before it hands them off to us, otherwise we end up
     * scaling by the brightness twice: once in LX and once on the controller.
     * Show code can reconfigure the master LX output to not prescale colors by
     * the master brightness by calling this function. The AllPixel class
     * doesn't do this by default because it changes behavior for all LXOutputs,
     * which would be surprising behavior for a constructor.
     */
    public static void configureMasterOutput(LX lx) {
        lx.engine.output.mode.setValue(Mode.RAW);
    }

    /* used so that we don't spam the logs too badly when we're disconnected */
    private void warn(String w, Object... args) {
        if (w == null) {
            SLStudio.setWarning("AllPixelOutput", null);
        }
        String v = String.format(w, args);
        if (currentWarning == null || !v.equals(currentWarning)) {
            SLStudio.setWarning("AllPixelOutput", v);
            currentWarning = v;
        }
    }
}
