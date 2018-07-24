package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXVector;
import static com.symmetrylabs.util.MathUtils.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.symmetrylabs.util.MathUtils.random;
import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.abs;

public class WarpSpeed extends SLPattern<CubesModel> {

    public final CompoundParameter numBars = new CompoundParameter("Density", -40, -500, -0.001);
    public final CompoundParameter size = new CompoundParameter("Length", 0.35, 0.1, 1.0);
    public final CompoundParameter sizeVar = new CompoundParameter("LenVar", 0.5);
    public final CompoundParameter speedP = new CompoundParameter("Speed", -3500, -10000, -300);
    public final CompoundParameter speedVar = new CompoundParameter("SpdVar", 0.5);
    public final CompoundParameter hueVariance = new CompoundParameter("HueVar", 0.2);
    public final CompoundParameter falloff = new CompoundParameter("falloff", 0.1);
    public final DiscreteParameter mode = new DiscreteParameter("mode", new String[] {"down", "up"});

    private float leftoverMs = 0;
    private float msPerRaindrop = 40;
    private List<Bar> bars;

    class Bar {
        LXVector p;
        LXVector v;
        float length;
        float hue;
        float speed;

        Bar() {
            this.length = (float)((model.yRange*0.4f)*(size.getValuef() + random(-0.5f, 0.5f)));
            float velMagnitude = 120;

            this.p = randomVector();
            this.v = new LXVector(0, 3 * model.yMax, 0);
            switch (mode.getOption()) {
                case "down": v.mult(-1);
            }

            this.hue = (random(-90, 90) * hueVariance.getValuef()) + palette.getHuef();
            this.speed = abs(speedP.getValuef() + (speedVar.getValuef() * random(-1000, 1000)));
        }

        private LXVector randomVector() {
            CubesModel.CubesStrip verticalStrip = null;

            List<CubesModel.CubesStrip> strips = ((CubesModel)model).getStrips();
            while (verticalStrip == null) {
                CubesModel.CubesStrip randomStrip = strips.get((int) random(0, strips.size()));

                if (!randomStrip.isHorizontal) {
                    verticalStrip = randomStrip;
                }
            }

            float yPos = 0;
            switch (mode.getOption()) {
                case "down": yPos = model.yMax+length;
                case "up":   yPos = model.yMin-length;
            }

            //System.out.println(yPos);

            return new LXVector(
                verticalStrip.getPoints().get(0).x,
                yPos,
                verticalStrip.getPoints().get(0).z
            );
        }

        // returns TRUE when this should die
        boolean age(double ms) {
            p.add(new LXVector(v).mult((float) (ms / this.speed)));
            switch (mode.getOption()) {
                case "down": return this.p.y < (model.yMin - this.length);
                case "up":   return this.p.y > (model.yMax + this.length);
            }
            return true;
        }
    }

    public WarpSpeed(LX lx) {
        super(lx);
        addParameter(numBars);
        addParameter(size);
        addParameter(sizeVar);
        addParameter(speedP);
        addParameter(speedVar);
        addParameter(hueVariance);
        addParameter(falloff);
        addParameter(mode);
        bars = new LinkedList<Bar>();

        mode.addListener(parameter -> {
            for (Bar bar : bars) {
                switch (mode.getOption()) {
                    case "down": if (bar.v.y > 0) bar.v.mult(-1);
                    case "up":   if (bar.v.y < 0) bar.v.mult(-1);
                }
            }
        });
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        leftoverMs += deltaMs;
        float msPerBar = Math.abs(numBars.getValuef());
        while (leftoverMs > msPerBar) {
            leftoverMs -= msPerBar;
            bars.add(new Bar());
        }

        getVectors().parallelStream().forEach(p -> {
            int c = 0;
            for (Bar bar : bars) {
                if (p.x >= (bar.p.x - 2) && p.x <= (bar.p.x + 2) &&
                      p.z >= (bar.p.z - 2) && p.z <= (bar.p.z + 2) &&
                      p.y >= (bar.p.y - bar.length) && p.y <= (bar.p.y + bar.length)) {

                    float d = abs(bar.p.y - p.y) / bar.length;
                    if (d < 1) {
                        c = Ops8.add(c, LXColor.hsb(bar.hue, palette.getSaturationf(), (float)Math.pow(1 - d, 4*falloff.getValuef()) * 100));
                    }
                }
            }
            colors[p.index] = c;
        });

        Iterator<Bar> i = bars.iterator();
        while (i.hasNext()) {
            Bar bar = i.next();
            boolean dead = bar.age(deltaMs);
            if (dead) {
                i.remove();
            }
        }
        markModified(SRGB8);
    }
}


