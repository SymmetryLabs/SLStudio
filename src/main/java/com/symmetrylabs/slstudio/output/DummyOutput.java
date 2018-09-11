package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;

public class DummyOutput extends LXOutput {
    public DummyOutput(LX lx) {
        super(lx);
    }

    @Override
    protected void onSend(int[] colors) {
        for (int i = 0; i < colors.length; ++i) {
            int color = colors[i];

            int red = (color >> 16) & 0xff;
            int green = (color >> 8) & 0xff;
            int blue = color & 0xff;

            System.out.println(i + ": (" + red + ", " + green + ", " + blue + ")");
        }
    }
}
