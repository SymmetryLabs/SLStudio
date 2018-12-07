package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.hhgarden.FlowerModel.Direction;
import com.symmetrylabs.shows.hhgarden.FlowerModel.FlowerPoint;
import com.symmetrylabs.shows.hhgarden.FlowerModel.Group;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class FlowerGroupMask extends LXEffect {
    private BooleanParameter stamen = new BooleanParameter("stamen", false);
    private BooleanParameter petal1 = new BooleanParameter("petal1", false);
    private BooleanParameter petal2 = new BooleanParameter("petal2", false);
    private BooleanParameter stem = new BooleanParameter("stem", false);
    private BooleanParameter up = new BooleanParameter("up", true);
    private BooleanParameter a = new BooleanParameter("a", true);
    private BooleanParameter b = new BooleanParameter("b", true);
    private BooleanParameter c = new BooleanParameter("c", true);
    private FlowerModel fm;

    public FlowerGroupMask(LX lx) {
        super(lx);
        addParameter(stamen);
        addParameter(petal1);
        addParameter(petal2);
        addParameter(stem);
        addParameter(up);
        addParameter(a);
        addParameter(b);
        addParameter(c);
    }

    @Override
    public void run(double deltaMs, double amount) {
        boolean gs = stamen.getValueb();
        boolean gp1 = petal1.getValueb();
        boolean gp2 = petal2.getValueb();
        boolean gm = stem.getValueb();
        boolean du = up.getValueb();
        boolean da = a.getValueb();
        boolean db = b.getValueb();
        boolean dc = c.getValueb();

        for (LXPoint point : model.points) {
            if (!(point instanceof FlowerPoint)) {
                continue;
            }
            FlowerPoint fp = (FlowerPoint) point;

            boolean ok = true;
            switch (fp.group) {
            case STAMEN: ok = gs; break;
            case PETAL1: ok = gp1; break;
            case PETAL2: ok = gp2; break;
            case STEM: ok = gm; break;
            }
            switch (fp.direction) {
            case UP: ok = ok && du; break;
            case A: ok = ok && da; break;
            case B: ok = ok && db; break;
            case C: ok = ok && dc; break;
            }

            if (!ok) {
                colors[fp.index] = 0;
            }
        }
    }
}
