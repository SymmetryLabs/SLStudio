package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import java.util.Random;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import static com.symmetrylabs.util.MathUtils.*;


public class TexturePoisson extends SLPattern<TreeModel> {
    public static final String GROUP_NAME = "tree";

    public String getAuthor() {
        // why does this method even exist
        return "Haldean Danger Brown";
    }

    public final CompoundParameter rate = (CompoundParameter)
        new CompoundParameter("rate", 400, 0, 5000)
            .setDescription("Number of leaf advances per frame")
            .setExponent(2);

    public final CompoundParameter rampExp = (CompoundParameter)
        new CompoundParameter("ramp", 0.8, 0.01, 10)
        .setDescription("Exponent on the ramp applied to the texture")
        .setExponent(2);

    public final BooleanParameter symm = new BooleanParameter("symm", true);
    public final BooleanParameter rewind = new BooleanParameter("rewind", false);

    private final int[] leafSteps;
    private final int maxStep;
    private final Random r = new Random(0);
    private final int[] ramp;

    public TexturePoisson(LX lx) {
        super(lx);
        maxStep = TreeModel.Leaf.NUM_LEDS;
        leafSteps = new int[model.leaves.size()];
        for (int i = 0; i < model.leaves.size(); i++) {
            leafSteps[i] = r.nextInt(maxStep);
        }
        ramp = new int[maxStep];

        addParameter(rate);
        addParameter(rampExp);
        addParameter(symm);
        addParameter(rewind);
    }

    public void run(double deltaMs) {
        boolean rew = rewind.getValueb();
        for (int i = 0; i < rate.getValuef(); i++) {
            int x = r.nextInt(leafSteps.length);
            leafSteps[x] = (leafSteps[x] + (rew ? -1 : 1)) % maxStep;
            if (leafSteps[x] < 0) {
                leafSteps[x] = maxStep - 1;
            }
        }

        for (int i = 0; i < maxStep; i++) {
            int modDist = symm.getValueb() ? Integer.min(i, maxStep - i) : i;
            double gr = 1.0 - Math.pow((float) modDist / (maxStep / (symm.getValueb() ? 2.0f : 1.0f)), rampExp.getValue());
            ramp[i] = LXColor.gray(100.f * gr);
        }

        int[] leafColors = new int[TreeModel.Leaf.NUM_LEDS];
        for (int i = 0; i < leafSteps.length; i++) {
            TreeModel.Leaf leaf = model.leaves.get(i);
            int step = leafSteps[i];
            for (int j = 0; j < leafColors.length; j++) {
                int k = j - step;
                if (k < 0) k += maxStep;
                colors[leaf.points[j].index] = ramp[k];
            }
        }
    }
}
