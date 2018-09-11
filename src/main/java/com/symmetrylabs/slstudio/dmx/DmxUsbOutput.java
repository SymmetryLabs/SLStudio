package com.symmetrylabs.slstudio.dmx;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;
import heronarts.lx.color.LXColor;

public class DmxUsbOutput extends LXOutput {
    private DmxUsbWriter dmxWriter;

    public static final int WHITE = -1;
    public static final int RED = 0;
    public static final int GREEN = 120;
    public static final int BLUE = 240;

    private int[] colorChannels = new int[0];

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

    @Override
    protected void onSend(int[] colors) {
        for (int i = 0; i < colors.length; ++i) {
            int color = colors[i];

            float saturation = LXColor.s(color) / 100f;
            float brightness = LXColor.b(color) / 100f;

            //byte red = (byte)(LXColor.red(color) * saturation);
            //byte green = (byte)(LXColor.green(color) * saturation);
            //byte blue = (byte)(LXColor.blue(color) * saturation);
            //byte white = (byte)((1 - saturation) * brightness * 255);

            int red = (int)(((color >> 16) & 0xff) * saturation);
            int green = (int)(((color >> 8) & 0xff) * saturation);
            int blue = (int)((color & 0xff) * saturation);
            int white = (int)((1 - saturation) * brightness * 255);

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
