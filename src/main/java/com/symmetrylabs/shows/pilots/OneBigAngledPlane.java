package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

public class OneBigAngledPlane extends SLPattern<SLModel> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private final CompoundParameter thickParam = new CompoundParameter("thick", 12, 0, model.rMax);
    private final CompoundParameter normalXParam = new CompoundParameter("x", 0.9, -1, 1);
    private final CompoundParameter normalYParam = new CompoundParameter("y", 0.3, -1, 1);
    private final CompoundParameter normalZParam = new CompoundParameter("z", 0.315, -1, 1);
    private final CompoundParameter cXParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private final CompoundParameter cYParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private final CompoundParameter cZParam = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);

    public OneBigAngledPlane(LX lx) {
        super(lx);

        addParameter(thickParam);
        addParameter(normalXParam);
        addParameter(normalYParam);
        addParameter(normalZParam);
        addParameter(cXParam);
        addParameter(cYParam);
        addParameter(cZParam);
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = 0;
        }

        LXVector normal = new LXVector(normalXParam.getValuef(), normalYParam.getValuef(), normalZParam.getValuef());
        normal.normalize();

        float halfThick = thickParam.getValuef() / 2f;
        LXVector negCenter = new LXVector(-cXParam.getValuef(), -cYParam.getValuef(), -cZParam.getValuef());

        for (LXVector v : getVectors()) {
            float proj = normal.dot(v.copy().add(negCenter));
            if (Math.abs(proj) < halfThick) {
                colors[v.index] = LXColor.WHITE;
            }
        }
    }
}
