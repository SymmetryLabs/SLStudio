package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.kalpa.TreeModel;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeSirens extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter base =
        new CompoundParameter("Base", 20, 0, 60)
            .setDescription("Base brightness level");

    public final CompoundParameter speed1 = new CompoundParameter("Spd1", 9000, 19000, 5000).setDescription("Speed of siren 1");
    public final CompoundParameter speed2 = new CompoundParameter("Spd2", 9000, 19000, 5000).setDescription("Speed of siren 2");
    public final CompoundParameter speed3 = new CompoundParameter("Spd3", 9000, 19000, 5000).setDescription("Speed of siren 3");
    public final CompoundParameter speed4 = new CompoundParameter("Spd4", 9000, 19000, 5000).setDescription("Speed of siren 4");

    public final CompoundParameter size1 = new CompoundParameter("Sz1", PI / 8, PI / 32, HALF_PI).setDescription("Size of siren 1");
    public final CompoundParameter size2 = new CompoundParameter("Sz2", PI / 8, PI / 32, HALF_PI).setDescription("Size of siren 2");
    public final CompoundParameter size3 = new CompoundParameter("Sz3", PI / 8, PI / 32, HALF_PI).setDescription("Size of siren 3");
    public final CompoundParameter size4 = new CompoundParameter("Sz4", PI / 8, PI / 32, HALF_PI).setDescription("Size of siren 4");

    public final BooleanParameter reverse = new BooleanParameter("Reverse", false);

    public final LXModulator azim1 = startModulator(new SawLFO(0, TWO_PI, this.speed1).randomBasis());
    public final LXModulator azim2 = startModulator(new SawLFO(TWO_PI, 0, this.speed2).randomBasis());
    public final LXModulator azim3 = startModulator(new SawLFO(0, TWO_PI, this.speed3).randomBasis());
    public final LXModulator azim4 = startModulator(new SawLFO(TWO_PI, 0, this.speed2).randomBasis());

    public TreeSirens(LX lx) {
        super(lx);
        addParameter("speed1", this.speed1);
        addParameter("speed2", this.speed2);
        addParameter("speed3", this.speed3);
        addParameter("speed4", this.speed4);
        addParameter("size1", this.size1);
        addParameter("size2", this.size2);
        addParameter("size3", this.size3);
        addParameter("size4", this.size4);
    }

    public void run(double deltaMs) {
        float azim1 = this.azim1.getValuef();
        float azim2 = this.azim2.getValuef();
        float azim3 = this.azim3.getValuef();
        float azim4 = this.azim3.getValuef();
        float falloff1 = 100 / this.size1.getValuef();
        float falloff2 = 100 / this.size2.getValuef();
        float falloff3 = 100 / this.size3.getValuef();
        float falloff4 = 100 / this.size4.getValuef();
        for (TreeModel.Leaf leaf : model.getLeaves()) {
            float azim = leaf.point.azimuth;
            float dist = max(max(max(
                100 - falloff1 * LXUtils.wrapdistf(azim, azim1, TWO_PI),
                100 - falloff2 * LXUtils.wrapdistf(azim, azim2, TWO_PI)),
                100 - falloff3 * LXUtils.wrapdistf(azim, azim3, TWO_PI)),
                100 - falloff4 * LXUtils.wrapdistf(azim, azim4, TWO_PI)
            );
            setColor(leaf, LXColor.gray(max(0, dist)));
        }
    }
}
