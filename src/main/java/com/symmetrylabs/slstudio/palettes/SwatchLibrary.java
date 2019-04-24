package com.symmetrylabs.slstudio.palettes;

import com.google.gson.JsonObject;
import heronarts.lx.*;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import org.jetbrains.annotations.NotNull;
import static com.symmetrylabs.util.MathUtils.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import heronarts.lx.parameter.LXParameterListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SwatchLibrary extends LXComponent implements Iterable<SwatchLibrary.Swatch>, LXParameterListener {
    private static final String KEY_SWATCH_INDEX = "index";
    private static final String KEY_SWATCH_HUE = "hue";
    private static final String KEY_SWATCH_SAT = "sat";
    private static final String KEY_SWATCH_BRIGHT = "brightness";
    private static final String KEY_SWATCHES = "swatches";

    public interface SwatchListener {
        void onSwatchesUpdated();
    }

    public static class Swatch {
        public final ColorParameter color;
        public int index;

        public Swatch(int index, float h, float s, float b, LXParameterListener listener) {
            this.index = index;
            color = new ColorParameter("Swatch" + index);
            color.hue.setValue(h);
            color.saturation.setValue(s);
            color.brightness.setValue(b);
            color.addListener(listener);
        }

        JsonObject toJson() {
            JsonObject res = new JsonObject();
            res.addProperty(KEY_SWATCH_INDEX, index);
            res.addProperty(KEY_SWATCH_HUE, color.hue.getValue());
            res.addProperty(KEY_SWATCH_SAT, color.saturation.getValue());
            res.addProperty(KEY_SWATCH_BRIGHT, color.brightness.getValue());
            return res;
        }

        static Swatch fromJson(JsonObject obj, LXParameterListener listener) {
            return new Swatch(
                obj.get(KEY_SWATCH_INDEX).getAsInt(),
                obj.get(KEY_SWATCH_HUE).getAsFloat(),
                obj.get(KEY_SWATCH_SAT).getAsFloat(),
                obj.get(KEY_SWATCH_BRIGHT).getAsFloat(),
                listener);
        }
    }

    protected static class SwatchTransition {
        public final LXNormalizedParameter param;
        // if true, param is periodic with period = 1, and we should go through the periodic point instead of the "normal" way
        public final boolean wrap;
        public final double start;
        public final double end;
        public final double duration;
        public float age;

        SwatchTransition(LXNormalizedParameter param, double start, double end, double duration, boolean isPeriodic) {
            this.param = param;
            this.start = start;
            this.end = end;
            this.duration = duration;
            this.age = 0;
            if (isPeriodic) {
                double a = Math.abs(end - start); // distance to go along the normal direction
                double b = (1 - Double.max(start, end)) + Double.min(start, end); // distance to go if we exploit periodicity
                this.wrap = b < a;
            } else {
                this.wrap = false;
            }
        }

        boolean loop(double deltaMs) {
            age += deltaMs;
            double t = age / duration;
            if (t > 1) {
                t = 1;
            }
            double v;
            if (wrap) {
                double dist = end - start;
                if (dist < 0) dist += 1;
                else dist -= 1;

                double trav = t * dist;
                v = wrap(start + trav);
            } else {
                v = start + (end - start) * t;
            }
            param.setNormalized(v);
            return t >= 1.0 - 1e-5;
        }
    }

    public final List<Swatch> swatches = new ArrayList<>();
    public final List<SwatchTransition> transitions = new ArrayList<>();
    protected final List<SwatchListener> listeners = new ArrayList<>();
    public final CompoundParameter transitionTime =
        (CompoundParameter) new CompoundParameter("transitionTime", 0, 0, 15_000).setExponent(3);
    protected final LX lx;

    public SwatchLibrary(LX lx) {
        super(lx);
        this.lx = lx;
        addParameter(transitionTime);
    }

    public void addSwatch() {
        swatches.add(new Swatch(swatches.size(), 0, 0, 0, this));
        bang();
    }

    public void removeSwatch(Swatch swatch) {
        swatches.remove(swatch);
        for (int i = 0; i < swatches.size(); i++) {
            swatches.get(i).index = i;
        }
        bang();
    }

    public void addListener(SwatchListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SwatchListener listener) {
        listeners.remove(listener);
    }

    public void loop(double elapsedMs) {
        for (Iterator<SwatchTransition> iter = transitions.iterator(); iter.hasNext();) {
            SwatchTransition st = iter.next();
            if (st.loop(elapsedMs)) {
                iter.remove();
            }
        }
    }

    @Override
    public void onParameterChanged(LXParameter param) {
        bang();
    }

    public void bang() {
        for (SwatchListener sl : listeners) {
            sl.onSwatchesUpdated();
        }
    }

    @NotNull
    @Override
    public Iterator<Swatch> iterator() {
        return swatches.iterator();
    }

    public void apply(int swatchIndex) {
        apply(swatches.get(swatchIndex));
    }

    public void apply(Swatch swatch) {
        apply(swatch, lx.engine.getFocusedLook());
    }

    public void apply(Swatch swatch, LXLook look) {
        transitions.clear();
        applyImpl(swatch, look);
    }

    public void apply(Swatch swatch, LXComponent c) {
        transitions.clear();
        applyImpl(swatch, c);
    }

    protected void applyImpl(Swatch swatch, LXLook look) {
        applyImpl(swatch, lx.palette.color.hue, lx.palette.color.saturation, lx.palette.color.brightness);

        for (LXChannel chan : look.channels) {
            if (!chan.acceptSwatches.getValueb()) {
                continue;
            }
            for (LXPattern pat : chan.getPatterns()) {
                applyImpl(swatch, pat);
            }
            for (LXEffect eff : chan.getEffects()) {
                applyImpl(swatch, eff);
            }
        }
    }

    protected void applyImpl(Swatch swatch, LXComponent c) {
        for (LXParameter param : c.getParameters()) {
            if (param instanceof ColorParameter) {
                ColorParameter cp = (ColorParameter) param;
                applyImpl(swatch, cp.hue, cp.saturation, cp.brightness);
            } else if (param instanceof LXNormalizedParameter) {
                LXNormalizedParameter np = (LXNormalizedParameter) param;
                if (np.getLabel().toLowerCase().equals("hue")) {
                    applyImpl(swatch, np, null, null);
                } else if (np.getLabel().toLowerCase().equals("saturation")) {
                    applyImpl(swatch, null, np, null);
                } else if (np.getLabel().toLowerCase().equals("sat")) {
                    applyImpl(swatch, null, np, null);
                } else if (np.getLabel().toLowerCase().equals("color")) {
                    applyImpl(swatch, np, null, null);
                }
            }
        }
    }

    protected void applyImpl(Swatch swatch, LXNormalizedParameter h, LXNormalizedParameter s, LXNormalizedParameter b) {
        double tt = transitionTime.getValue();
        if (tt == 0) {
            if (h != null) h.setNormalized(swatch.color.hue.getNormalized());
            if (s != null) s.setNormalized(swatch.color.saturation.getNormalized());
            if (b != null) b.setNormalized(swatch.color.brightness.getNormalized());
        } else {
            if (h != null) transitions.add(new SwatchTransition(h, h.getNormalized(), swatch.color.hue.getNormalized(), tt, true));
            if (s != null) transitions.add(new SwatchTransition(s, s.getNormalized(), swatch.color.saturation.getNormalized(), tt, false));
            if (b != null) transitions.add(new SwatchTransition(b, b.getNormalized(), swatch.color.brightness.getNormalized(), tt, false));
        }
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        swatches.clear();
        transitions.clear();
        super.load(lx, obj);

        if (obj.has(KEY_SWATCHES)) {
            JsonArray swatchArray = obj.getAsJsonArray(KEY_SWATCHES);
            for (JsonElement swatchElem : swatchArray) {
                swatches.add(Swatch.fromJson((JsonObject) swatchElem, this));
            }
        }
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        JsonArray arr = new JsonArray();
        for (Swatch s : swatches) {
            arr.add(s.toJson());
        }
        obj.add(KEY_SWATCHES, arr);
    }

    public void resetToDefault() {
        swatches.clear();
        transitions.clear();

        swatches.add(new Swatch(0, 0, 100, 100, this));
        swatches.add(new Swatch(1, 30, 100, 100, this));
        swatches.add(new Swatch(2, 60, 100, 100, this));
        swatches.add(new Swatch(3, 90, 100, 100, this));
        swatches.add(new Swatch(4, 180, 100, 100, this));
        swatches.add(new Swatch(5, 225, 100, 100, this));
        swatches.add(new Swatch(6, 270, 100, 100, this));
    }

    public static SwatchLibrary getDefault(LX lx) {
        SwatchLibrary sl = new SwatchLibrary(lx);
        sl.resetToDefault();
        return sl;
    }
}
