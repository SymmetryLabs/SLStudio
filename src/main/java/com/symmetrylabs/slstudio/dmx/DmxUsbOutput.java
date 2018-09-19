package com.symmetrylabs.slstudio.dmx;

import java.util.List;
import java.util.ArrayList;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.component.GammaExpander;
import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;
import heronarts.lx.color.LXColor;

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

    private int[] colorChannels = new int[0];
        private RGBWMode mode = RGBWMode.ADD_WHITE;
        private GammaExpander gammaExpander = null;

    public DmxUsbOutput(LX lx) {
        super(lx);

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
    protected void onSend(int[] colors) {
        for (int i = 0; i < colors.length; ++i) {
            int color = colors[i];

                        if (gammaExpander != null) {
                                color = gammaExpander.getExpandedColor(color);
                        }

                        int red = Ops8.red(color);
                        int green = Ops8.green(color);
                        int blue = Ops8.blue(color);
                        int white = 0;

                        switch (mode) {
                        case USE_HSB: {
                                float saturation = LXColor.s(color) / 100f;
                                float brightness = LXColor.b(color) / 100f;
                                red *= saturation;
                                green *= saturation;
                                blue *= saturation;
                                white = (int)((1 - saturation) * brightness * 255);
                                break;
                        }

                        case NO_WHITE:
                                break;

                        case ADD_WHITE: {
                                float saturation = LXColor.s(color) / 100f;
                                float brightness = LXColor.b(color) / 100f;
                                white = (int)(255 * Math.pow((1.0 - saturation) * brightness, 2));
                                break;
                        }
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

                int channel = i * colorChannels.length + j;
                //System.out.println(channel + " " + value);
                //System.out.println("Saturation: " + saturation + " Brightness: " + brightness);
                //System.out.println("Red: " + red + " Green: " + green + " Blue: " + blue);
                //System.out.println("Color: " + color);
                dmxWriter.setChannelData(channel, value);
            }
        }

        dmxWriter.send();
    }
}
