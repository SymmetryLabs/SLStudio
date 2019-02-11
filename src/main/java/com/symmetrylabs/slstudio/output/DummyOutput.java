package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.output.LXOutput;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.color.Ops16;

public class DummyOutput extends LXOutput {
    protected boolean is16BitColorEnabled = false;

    public DummyOutput(LX lx) {
        this(lx, false);
    }

    public DummyOutput(LX lx, boolean is16BitColorEnabled) {
        super(lx);

        this.is16BitColorEnabled = is16BitColorEnabled;
    }

    public void set16BitColorEnabled(boolean enable) {
        is16BitColorEnabled = enable;
    }

    @Override
    protected void onSend(PolyBuffer src) {
        if (is16BitColorEnabled) {
            long[] colors = (long[])src.getArray(PolyBuffer.Space.RGB16);

            for (int i = 0; i < colors.length; ++i) {
                long color = colors[i];

                int red = Ops16.red(color);
                int green = Ops16.green(color);
                int blue = Ops16.blue(color);

                System.out.println(i + ": (" + red + ", " + green + ", " + blue + ")");
            }
        }
        else {
            int[] colors = (int[])src.getArray(PolyBuffer.Space.RGB8);

            for (int i = 0; i < colors.length; ++i) {
                int color = colors[i];

                int red = Ops8.red(color);
                int green = Ops8.green(color);
                int blue = Ops8.blue(color);

                System.out.println(i + ": (" + red + ", " + green + ", " + blue + ")");
            }
        }
    }
}
