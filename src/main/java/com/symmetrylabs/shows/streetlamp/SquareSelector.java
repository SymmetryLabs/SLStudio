package com.symmetrylabs.shows.streetlamp;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SquareSelector extends SLPattern<StreetlampModel> {
    public static final String GROUP_NAME = StreetlampShow.SHOW_NAME;

    private final List<BooleanParameter> params;

    public SquareSelector(LX lx) {
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
    public void run(double elapsedMs, PolyBuffer.Space preferredSpace) {
        long[] colors = (long[]) getArray(PolyBuffer.Space.RGB16);
        long on = Ops16.rgba(0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF);
        long off = Ops16.rgba(0, 0, 0, 0xFFFF);
        for (int i = 0; i < model.points.length; i++) {
            colors[i] = params.get(i).getValueb() ? on : off;
        }
        markModified(PolyBuffer.Space.RGB16);
    }
}
