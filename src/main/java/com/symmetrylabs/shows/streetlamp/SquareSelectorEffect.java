package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.color.Ops16;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SquareSelectorEffect extends LXEffect {
    public static final String GROUP_NAME = StreetlampShow.SHOW_NAME;

    private final List<BooleanParameter> params;

    public SquareSelectorEffect(LX lx) {
        super(lx);

        params = new ArrayList<>();
        for (LXPoint p : model.points) {
            params.add(new BooleanParameter(String.format("S%02d", p.index), false));
        }
        for (BooleanParameter p : params) {
            addParameter(p);
        }
    }

    @Override
    public void run(double elapsedMs, double amount, PolyBuffer.Space preferredSpace) {
        long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);
        for (int i = 0; i < model.points.length; i++) {
            if (!params.get(i).getValueb()) {
                long c = colors[i];
                int r = (int) ((c >> 32) & 0xFFFF);
                int g = (int) ((c >> 16) & 0xFFFF);
                int b = (int) (c & 0xFFFF);
                colors[i] = Ops16.rgba(r, g, b, 0);
            }
        }
        markModified(PolyBuffer.Space.RGB16);
    }
}
