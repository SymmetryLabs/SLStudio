package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class PilotsPlanes extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final CompoundParameter speedParam = new CompoundParameter("speed", 12, -500, 500);
    private final CompoundParameter distParam = new CompoundParameter("dist", 120, 10, 600);
    private final CompoundParameter thickParam = new CompoundParameter("thick", 12, 0, 60);
    private final CompoundParameter normalXParam = new CompoundParameter("x", 0.9, 0, 1);
    private final CompoundParameter normalYParam = new CompoundParameter("y", 0.3, 0, 1);
    private final CompoundParameter normalZParam = new CompoundParameter("z", 0.315, 0, 1);
    private final BooleanParameter redParam = new BooleanParameter("red", true);

    private final BooleanParameter flipSpeedParam = new BooleanParameter("flips", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter flipColorParam = new BooleanParameter("flipc", false).setMode(BooleanParameter.Mode.MOMENTARY);

    float off;

    public PilotsPlanes(LX lx) {
        super(lx);
        addParameter(speedParam);
        addParameter(distParam);
        addParameter(thickParam);
        addParameter(redParam);
        addParameter(normalXParam);
        addParameter(normalYParam);
        addParameter(normalZParam);

        addParameter(flipSpeedParam);
        addParameter(flipColorParam);

        off = 0;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == flipSpeedParam && flipSpeedParam.getValueb()) {
            speedParam.setValue(-speedParam.getValue());
        } else if (p == flipColorParam && flipColorParam.getValueb()) {
            redParam.setValue(!redParam.getValueb());
        }
    }

    @Override
    public void run(double elapsedMs) {
        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        LXVector normal = new LXVector(normalXParam.getValuef(), normalYParam.getValuef(), normalZParam.getValuef());
        normal.normalize();

        off += speedParam.getValuef() / 1000f * (float) elapsedMs;
        float dist = distParam.getValuef();
        float halfThick = thickParam.getValuef() / 2f;
        int color = redParam.getValueb() ? PilotsShow.RED : PilotsShow.YELLOW;

        for (LXVector v : getVectors()) {
            float proj = normal.dot(v) + off;
            while (proj > dist) {
                proj -= dist;
            }
            while (proj < 0) {
                proj += dist;
            }

            if (proj < halfThick || proj > dist - halfThick) {
                colors[v.index] = color;
            }
        }
    }
}
