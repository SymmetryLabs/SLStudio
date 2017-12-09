package com.symmetrylabs.slstudio.ping;

import com.symmetrylabs.slstudio.model.LXPointNormal;
import com.symmetrylabs.slstudio.util.BlobFollower;
import com.symmetrylabs.slstudio.util.BlobTracker;
import com.symmetrylabs.slstudio.util.Marker;
import com.symmetrylabs.slstudio.util.Octahedron;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


public class LightSource extends SLPatternWithMarkers {
    CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);
    CompoundParameter y = new CompoundParameter("y", model.yMax, 0, 240);
    CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
    CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    CompoundParameter sat = new CompoundParameter("sat", 0, 0, 1);
    CompoundParameter gain = new CompoundParameter("gain", 1, 0, 3);
    CompoundParameter falloff = new CompoundParameter("falloff", 0.25, 0, 1);
    CompoundParameter ambient = new CompoundParameter("ambient", 0, 0, 1);
    BooleanParameter useBlobs = new BooleanParameter("useBlobs");

    List<Light> lights = new ArrayList<Light>();
    int numActiveLights = 0;
    BlobFollower bf;

    public LightSource(LX lx) {
        super(lx);
        addParameter(x);
        addParameter(y);
        addParameter(z);
        addParameter(hue);
        addParameter(sat);
        addParameter(gain);
        addParameter(falloff);
        addParameter(ambient);
        addParameter(useBlobs);
        bf = new BlobFollower(BlobTracker.getInstance(lx));
    }

    public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        PVector pos = new PVector(x.getValuef(), y.getValuef(), z.getValuef());
        float value = gain.getValuef() * 100f;
        markers.add(new Octahedron(pos, 20, LX.hsb(hue.getValuef(), sat.getValuef() * 100f, value > 100 ? 100 : value)));
        markers.addAll(bf.getMarkers());
        return markers;
    }

    public void run(double deltaMs) {
        resetLights();
        if (useBlobs.isOn()) {
            for (BlobFollower.Follower f : bf.getFollowers()) {
                addLight(new PVector(f.pos.x, y.getValuef(), f.pos.z), f.value);
            }
        } else {
            addLight(new PVector(x.getValuef(), y.getValuef(), z.getValuef()), 1);
        }
        renderLights();
        bf.advance((float) deltaMs * 0.001f);
    }

    void resetLights() {
        numActiveLights = 0;
    }

    void addLight(PVector pos, float value) {
        int li = numActiveLights;
        if (li >= lights.size()) {
            lights.add(new Light(pos, value));
        } else {
            Light light = lights.get(li);
            light.pos = pos;
            light.value = value;
        }
        numActiveLights++;
    }

    void renderLights() {
        final float h = hue.getValuef();
        final float s = sat.getValuef() * 100f;
        final float g = gain.getValuef();
        final float a = ambient.getValuef();
        final List<Light> activeLights = lights.subList(0, numActiveLights);

        activeLights.parallelStream().forEach(new Consumer<Light>() {
            public void accept(Light light) {
                for (LXPoint p : model.points) {
                    LXPointNormal pn = (LXPointNormal) p;
                    PVector pv = new PVector(p.x, p.y, p.z);
                    PVector toLight = PVector.sub(light.pos, pv);
                    float dist = toLight.mag();

                    float extent = dist / (falloff.getValuef() * model.xRange);
                    if (extent < 1) extent = 1; // avoid division by zero or excessive brightness
                    float brightness = 1.0f / (extent * extent);

                    float cosAngle = PVector.dot(toLight, pn.normal) / dist;
                    if (cosAngle < 0) cosAngle = 0;

                    light.levels[p.index] = cosAngle * brightness * light.value * g;
                }
            }
        });

        Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
            public void accept(LXPoint p) {
                float sum = a;
                for (Light light : activeLights) {
                    sum += light.levels[p.index];
                }
                colors[p.index] = LX.hsb(h, s, (sum > 1 ? 1 : sum) * 100f);
            }
        });
    }

    class Light {
        public PVector pos;
        public float value;
        public float[] levels;

        Light(PVector pos, float value) {
            this.pos = pos;
            this.value = value;
            this.levels = new float[colors.length];
        }
    }
}
