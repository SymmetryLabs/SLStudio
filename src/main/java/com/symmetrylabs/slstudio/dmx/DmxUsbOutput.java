package com.symmetrylabs.slstudio.dmx;

import java.util.List;
import java.util.ArrayList;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.component.GammaExpander;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.color.Ops16;

public class DmxUsbOutput extends LXOutput {
    public enum RGBWMode {
        USE_HSB,
        NO_WHITE,
        ADD_WHITE,
    }

    private DmxUsbWriter dmxWriter;

    public static final int WHITE = -1;
    public static final int RED = 0;
    public static final int GREEN = 120;
    public static final int BLUE = 240;

    private final BooleanParameter bitModeParam = new BooleanParameter("16-bit", false);

    private int[] colorChannels = new int[0];
    private RGBWMode mode = RGBWMode.USE_HSB;
    private GammaExpander gammaExpander = null;

    public DmxUsbOutput(LX lx) {
        super(lx);

        addParameter(bitModeParam);

        dmxWriter = new DmxUsbWriter();
    }

    public DmxUsbOutput(LX lx, String portName) {
        super(lx);

        dmxWriter = new DmxUsbWriter(portName);
    }

    public void setColorChannels(int[] colorChannels) {
        this.colorChannels = colorChannels;
    }

    public DmxUsbOutput setGammaExpander(GammaExpander g) {
        gammaExpander = g;
        return this;
    }

    public DmxUsbOutput setRGBWMode(RGBWMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    protected void onSend(PolyBuffer src) {

        boolean bitMode = bitModeParam.isOn();

        long[] colors = (long[])src.getArray(PolyBuffer.Space.RGB16);

        for (int i = 0; i < colors.length; ++i) {
            long color = colors[i];

            if (gammaExpander != null) {
                color = gammaExpander.getExpandedColor16(color);
            }

            int red = Ops16.red(color);
            int green = Ops16.green(color);
            int blue = Ops16.blue(color);
            int white = 0;

            double saturation, brightness;

            switch (mode) {
            case USE_HSB:
                saturation = Ops16.saturation(color);
                brightness = Ops16.brightness(color);

                red *= saturation;
                green *= saturation;
                blue *= saturation;
                white = (int)((1 - saturation) * brightness * Ops16.MAX);
                break;

            case NO_WHITE:
                break;

            case ADD_WHITE:
                saturation = Ops16.saturation(color);
                brightness = Ops16.brightness(color);
                white = (int)(Ops16.MAX * Math.pow((1.0 - saturation) * brightness, 2));
                break;
            }

            for (int j = 0; j < colorChannels.length; ++j) {
                int colorChannel = colorChannels[j];
                int value = 0;

                switch (colorChannel) {
                case RED:
                    value = red;
                    break;
                case GREEN:
                    value = green;
                    break;
                case BLUE:
                    value = blue;
                    break;
                case WHITE:
                    value = white;
                    break;
                }

                if (false) {
                    int channel = 2 * (i * colorChannels.length + j);
                    dmxWriter.setChannelData(channel, (value & 0xff00) >>> 8);
                    dmxWriter.setChannelData(channel + 1, value & 0x00ff);
                }
                else {
                    int channel = i * colorChannels.length + j;
                    dmxWriter.setChannelData(channel, (value & 0xff00) >>> 8);
                }
            }
        }

        dmxWriter.send();
    }
}
