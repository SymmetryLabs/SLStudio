package com.symmetrylabs.slstudio.dmx;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.color.Ops16;

import com.symmetrylabs.slstudio.component.GammaExpander;

public class DmxUsbOutput extends LXOutput {
    public enum RGBWMode {
        USE_HSB,
        NO_WHITE,
        ADD_WHITE,
    }

    private DmxUsbWriter dmxWriter;

    public static final int EMPTY = -2;
    public static final int WHITE = -1;
    public static final int RED = 0;
    public static final int AMBER = 60;
    public static final int GREEN = 120;
    public static final int CYAN = 180;
    public static final int BLUE = 240;
    public static final int VIOLET = 300;

    private final BooleanParameter bitModeParam = new BooleanParameter("16-bit", false);

    private int[] colorChannels = new int[0];
    private RGBWMode mode = RGBWMode.USE_HSB;
    private GammaExpander gammaExpander = null;
    private boolean hasNonRGB = false;
    private boolean hasWhite = false;

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

        for (int c : colorChannels) {
            if (c != RED && c != GREEN && c != BLUE && c != WHITE) {
                hasNonRGB = true;
            }

            if (c == WHITE) {
                hasWhite = true;
            }
        }
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
            int amber = 0;
            int green = Ops16.green(color);
            int cyan = 0;
            int blue = Ops16.blue(color);
            int violet = 0;
            int white = 0;

            double hue = 0, saturation = 0, brightness = 0;

            if (hasWhite || hasNonRGB) {
                hue = Ops16.hue(color);
                saturation = Ops16.saturation(color);
                brightness = Ops16.brightness(color);
            }

            if (hasNonRGB) {
                if (saturation == 0) {
                    red = amber = green = cyan = blue = violet = (int)(brightness * Ops16.MAX + 0.5);
                } else {
                    double h = (hue - Math.floor(hue)) * 12.;
                    double f = h - Math.floor(h);
                    int levelHighColor = (int)(brightness * Ops16.MAX + 0.5);
                    int levelLowColor = (int)(brightness * (1. - saturation) * Ops16.MAX + 0.5);
                    int fallingColor = (int)(brightness * (1. - saturation * f) * Ops16.MAX + 0.5);
                    int risingColor = (int)(brightness * (1. - (saturation * (1. - f))) * Ops16.MAX + 0.5);
                    switch ((int)h) {
                    case 0:
                        red = levelHighColor;
                        amber = risingColor;
                        green = levelLowColor;
                        cyan = levelLowColor;
                        blue = levelLowColor;
                        violet = levelLowColor;
                        break;
                    case 1:
                        red = fallingColor;
                        amber = levelHighColor;
                        green = levelLowColor;
                        cyan = levelLowColor;
                        blue = levelLowColor;
                        violet = levelLowColor;
                        break;
                    case 2:
                        red = levelLowColor;
                        amber = levelHighColor;
                        green = risingColor;
                        cyan = levelLowColor;
                        blue = levelLowColor;
                        violet = levelLowColor;
                        break;
                    case 3:
                        red = levelLowColor;
                        amber = fallingColor;
                        green = levelHighColor;
                        cyan = levelLowColor;
                        blue = levelLowColor;
                        violet = levelLowColor;
                        break;
                    case 4:
                        red = levelLowColor;
                        amber = levelLowColor;
                        green = levelHighColor;
                        cyan = risingColor;
                        blue = levelLowColor;
                        violet = levelLowColor;
                        break;
                    case 5:
                        red = levelLowColor;
                        amber = levelLowColor;
                        green = fallingColor;
                        cyan = levelHighColor;
                        blue = levelLowColor;
                        violet = levelLowColor;
                        break;
                    case 6:
                        red = levelLowColor;
                        amber = levelLowColor;
                        green = levelLowColor;
                        cyan = levelHighColor;
                        blue = risingColor;
                        violet = levelLowColor;
                        break;
                    case 7:
                        red = levelLowColor;
                        amber = levelLowColor;
                        green = levelLowColor;
                        cyan = fallingColor;
                        blue = levelHighColor;
                        violet = levelLowColor;
                        break;
                    case 8:
                        red = levelLowColor;
                        amber = levelLowColor;
                        green = levelLowColor;
                        cyan = levelLowColor;
                        blue = levelHighColor;
                        violet = risingColor;
                        break;
                    case 9:
                        red = levelLowColor;
                        amber = levelLowColor;
                        green = levelLowColor;
                        cyan = levelLowColor;
                        blue = fallingColor;
                        violet = levelHighColor;
                        break;
                    case 10:
                        red = risingColor;
                        amber = levelLowColor;
                        green = levelLowColor;
                        cyan = levelLowColor;
                        blue = levelLowColor;
                        violet = levelHighColor;
                        break;
                    case 11:
                        red = levelHighColor;
                        amber = levelLowColor;
                        green = levelLowColor;
                        cyan = levelLowColor;
                        blue = levelLowColor;
                        violet = fallingColor;
                        break;
                    }
                }
            }

            switch (mode) {
            case USE_HSB:
                red *= saturation;
                amber *= saturation;
                green *= saturation;
                cyan *= saturation;
                blue *= saturation;
                violet *= saturation;
                white = (int)((1 - saturation) * brightness * Ops16.MAX);
                break;

            case ADD_WHITE:
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
                case AMBER:
                    value = amber;
                    break;
                case GREEN:
                    value = green;
                    break;
                case CYAN:
                    value = cyan;
                    break;
                case BLUE:
                    value = blue;
                    break;
                case VIOLET:
                    value = violet;
                    break;
                case WHITE:
                    value = white;
                    break;
                case EMPTY:
                    value = 0;
                    break;
                }

                if (bitMode) {
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
