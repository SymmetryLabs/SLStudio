package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import com.symmetrylabs.slstudio.component.HiddenComponent;

@HiddenComponent
public class Vortex extends SLPattern<SLModel> {
    private final DiscreteParameter countParam = new DiscreteParameter("count", 50, 0, 200);
    private final CompoundParameter xCenterParam = new CompoundParameter("xc", model.cx, model.xMin, model.xMax);
    private final CompoundParameter zCenterParam = new CompoundParameter("zc", model.cz, model.zMin, model.zMax);
    private final CompoundParameter xRadiusParam = new CompoundParameter("xrad", model.xRange / 10, 0, model.xRange);
    private final CompoundParameter zRadiusParam = new CompoundParameter("zrad", model.zRange / 10, 0, model.zRange);
    private final CompoundParameter radiusVarianceParam = new CompoundParameter("radvar", 0.05, 0, 1);
    private final CompoundParameter sizeParam = new CompoundParameter("size", 12, 0, 120);
    private final CompoundParameter angVelParam = new CompoundParameter("omega", 90, 0, 360);
    private final CompoundParameter linVelParam = new CompoundParameter("v", model.yRange / 3, 0, model.yRange);
    private final CompoundParameter smearParam = new CompoundParameter("smear", 0.4, 0, 1);
    private final BooleanParameter reset = new BooleanParameter("reset", false).setMode(BooleanParameter.Mode.MOMENTARY);

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private boolean firstRun = true;

    private class Particle {
        double xrad;
        double zrad;
        double theta;
        double h;
        double size;
        LXVector loc;

        Particle(double xrad, double zrad, double theta, double h, double size) {
            this.xrad = xrad;
            this.zrad = zrad;
            this.theta = theta;
            this.h = h;
            this.size = size;
            updateLoc();
        }

        private void updateLoc() {
            loc = new LXVector((float) (xrad * Math.sin(theta)), 0, (float) (zrad * Math.cos(theta)))
                .add(new LXVector(xCenterParam.getValuef(), model.yMin + (float) h, zCenterParam.getValuef()));
        }

        boolean dead() {
            return h > model.yRange + size;
        }

        /**
         * @param ms    Milliseconds elapsed
         * @param omega Angular velocity, in degrees per second
         * @param v     Linear velocity, in inches per second
         */
        void step(double ms, double omega, double v) {
            h += ms * v / 1000f;
            theta += Math.PI * ms * omega / 1000f / 180f;
            updateLoc();
        }
    }

    public Vortex(LX lx) {
        super(lx);
        addParameter(countParam);
        addParameter(sizeParam);
        addParameter(xRadiusParam);
        addParameter(zRadiusParam);
        addParameter(xCenterParam);
        addParameter(zCenterParam);
        addParameter(angVelParam);
        addParameter(linVelParam);
        addParameter(radiusVarianceParam);
        addParameter(smearParam);
        addParameter(reset);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == reset && reset.getValueb()) {
            particles.clear();
            firstRun = true;
        }
    }

    private Particle randomParticle() {
        return new Particle(
            (1 + random.nextGaussian() * radiusVarianceParam.getValue()) * xRadiusParam.getValue(),
            (1 + random.nextGaussian() * radiusVarianceParam.getValue()) * zRadiusParam.getValue(),
            360f * random.nextFloat(),
            -sizeParam.getValue(),
            sizeParam.getValue());
    }

    @Override
    public void run(double elapsedMs) {
        double omega = angVelParam.getValuef();
        double v = linVelParam.getValuef();

        /* step existing particles forward and delete the dead ones */
        particles.parallelStream().forEach(p -> p.step(elapsedMs, omega, v));
        particles.removeIf(Particle::dead);

        /* replenish our particle supply */
        int count = countParam.getValuei();
        if (firstRun) {
            for (int i = 0; i < count; i++) {
                Particle p = randomParticle();
                p.h = random.nextFloat() * model.yRange;
                particles.add(p);
            }
            firstRun = false;
        } else {
            /* We introduce some randomness with the recreation of particles,
             * to make it feel less like there's a fixed number */
            while (particles.size() < count && random.nextFloat() < smearParam.getValue()) {
                particles.add(randomParticle());
            }
        }

        int on = LXColor.WHITE;
        int off = 0;
        Arrays.fill(colors, off);

        for (LXVector vec : getVectors()) {
            for (Particle p : particles) {
                float dx = vec.x - p.loc.x;
                float dy = vec.y - p.loc.y;
                float dz = vec.z - p.loc.z;
                if (dx * dx + dy * dy + dz * dz <= p.size * p.size) {
                    colors[vec.index] = on;
                    break;
                }
            }
        }
    }

    @Override
    public String getCaption() {
        return String.format("%d/%d particles", particles.size(), countParam.getValuei());
    }

    @Override
    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        markers.add(new CubeMarker(
            new PVector(xCenterParam.getValuef(), model.cy, zCenterParam.getValuef()),
            new PVector(xRadiusParam.getValuef(), model.yRange / 2, zRadiusParam.getValuef()),
            LXColor.GREEN
        ));
        return markers;
    }
}
