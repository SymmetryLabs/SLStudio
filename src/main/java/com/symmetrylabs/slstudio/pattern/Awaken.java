package com.symmetrylabs.slstudio.pattern;

import com.google.gson.JsonObject;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.palettes.ColorPalette;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Awaken extends SLPattern<SLModel> {
    private static final String KEY_PALETTE_NAME = "paletteName";

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();
    private final DiscreteParameter palette =
        new DiscreteParameter("palette", paletteLibrary.getNames());
    private final CompoundParameter speedParam =
        new CompoundParameter("speed", 12, -1000, 1000);
    private final CompoundParameter widthParam =
        new CompoundParameter("width", model.rRange / 2, 1, 2 * model.rRange);
    private final BooleanParameter wipeOutParam = new BooleanParameter("wipeOut", true);
    private final BooleanParameter refillParam = new BooleanParameter("refill", true);
    private final BooleanParameter triggerParam =
        new BooleanParameter("trigger", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final BooleanParameter resetParam =
        new BooleanParameter("reset", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private final Random rand = new Random();

    private static class Center {
        LXVector loc;
        float killDist;
        float dist;
        boolean dying;
    }
    private List<Center> centers = new ArrayList<>();

    public Awaken(LX lx) {
        super(lx);
        addParameter(palette);
        addParameter(speedParam);
        addParameter(widthParam);
        addParameter(wipeOutParam);
        addParameter(refillParam);
        addParameter(triggerParam);
        addParameter(resetParam);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == triggerParam && triggerParam.getValueb()) {
            pickNewCenter();
        } else if (p == resetParam && resetParam.getValueb()) {
            centers.clear();
        }
    }

    private void pickNewCenter() {
        Center c = new Center();
        List<LXVector> vectors = getVectorList();
        c.loc = vectors.get(rand.nextInt(vectors.size()));
        c.dist = 0;
        c.killDist = 0;
        for (LXVector v : vectors) {
            c.killDist = Float.max(c.killDist, v.dist(c.loc));
        }
        centers.add(c);
    }

    @Override
    public String getCaption() {
        return String.format("%d centers", centers.size());
    }

    @Override
    public void run(double elapsedMs) {
        if (refillParam.getValueb() && centers.size() == 0) {
            pickNewCenter();
        }

        float width = widthParam.getValuef();
        for (Center c : centers) {
            c.dist += speedParam.getValuef() / 1000f * (float) elapsedMs;
            if (wipeOutParam.getValueb() && c.dist - width > c.killDist) {
                c.dying = true;
                c.dist = 0;
            }
        }
        ColorPalette gradient = paletteLibrary.get(palette.getOption());

        centers.removeIf(c -> c.dying && c.dist > c.killDist);
        if (centers.isEmpty()) {
            Arrays.fill(colors, gradient.getColor(0));
            return;
        }

        for (LXVector vx : getVectors()) {
            float d = 0;
            for (Center c : centers) {
                if (!c.dying) {
                    float dist = vx.dist(c.loc);
                    float cd = c.dist - dist;
                    d = Float.max(cd, d);
                } else {
                    if (vx.dist(c.loc) > c.dist) {
                        d = width;
                    }
                }
            }
            d /= width;
            int color = gradient.getColor(d);
            colors[vx.index] = color;
        }
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.addProperty(KEY_PALETTE_NAME, palette.getOption());
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);
        if (obj.has(KEY_PALETTE_NAME)) {
            String pname = obj.get(KEY_PALETTE_NAME).getAsString();
            String[] palettes = paletteLibrary.getNames();
            for (int i = 0; i < palettes.length; i++) {
                if (palettes[i].equals(pname)) {
                    palette.setValue(i);
                    return;
                }
            }
            System.err.println("couldn't find palette '" + pname + "'");
        }
    }
}
