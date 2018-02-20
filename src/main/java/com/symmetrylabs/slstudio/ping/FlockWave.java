package com.symmetrylabs.slstudio.ping;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Collection;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.math3.util.FastMath;
import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.kernel.SLKernel;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ColorPalette;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import com.symmetrylabs.util.BlobFollower;
import com.symmetrylabs.util.BlobTracker;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.Octahedron;

public class FlockWave extends SLPatternWithMarkers {

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();

    CompoundParameter timeScale = new CompoundParameter("timeScale", 1, 0, 1);  // time scaling factor
    BooleanParameter oscFollowers = new BooleanParameter("atBlobs");
    BooleanParameter oscBlobs = new BooleanParameter("nearBlobs");
    BooleanParameter everywhere = new BooleanParameter("everywhere");
    CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);  // focus coordinates (in)
    CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
    CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
    CompoundParameter zScale = new CompoundParameter("zScale", 0, -6, 12);  // z scaling factor (dB)
    DiscreteParameter maxBirds = new DiscreteParameter("maxBirds", 8, 0, 100);

    CompoundParameter spnRad = new CompoundParameter("spnRad", 100, 0, 400);  // radius (in) within which to spawn birds
    CompoundParameter spnRate = new CompoundParameter("spnRate", 0.2, 0, 2);  // maximum spawn rate (birds/s)
    CompoundParameter spnVary = new CompoundParameter("spnVary", 0, 0, 1);
        // vary spawn rate according to focus speed (0 = don't vary, 1 = determine entirely by speed)
    CompoundParameter scatter = new CompoundParameter("scatter", 100, 0, 1000);  // initial velocity randomness (in/s)
    CompoundParameter spdMult = new CompoundParameter("spdMult", 1, 0, 2);  // (ratio) bird target speed / focus speed
    CompoundParameter maxSpd = new CompoundParameter("maxSpd", 10, 0, 100);  // max bird speed (in/s)
    CompoundParameter turnSec = new CompoundParameter("turnSec", 1, 0, 2);  // time (s) to complete 90% of a turn
    CompoundParameter fadeInSec = new CompoundParameter("fadeInSec", 0.5, 0, 2);  // time (s) to fade up to 100% intensity
    CompoundParameter fadeOutSec = new CompoundParameter("fadeOutSec", 1, 0, 2);// time (s) to fade down to 10% intensity

    CompoundParameter size = new CompoundParameter("size", 100, 0, 2000);  // render radius of each bird (in)
    CompoundParameter detail = new CompoundParameter("detail", 4, 0, 10);  // ripple spatial frequency (number of waves)
    CompoundParameter ripple = new CompoundParameter("ripple", 0, -10, 10);  // ripple movement (waves/s)
    DiscreteParameter palette = new DiscreteParameter("palette", paletteLibrary.getNames());
        // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward start or stop
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

    PVector prevFocus = null;
    Set<Bird> birds = new CopyOnWriteArraySet<>();
    float numToSpawn = 0f;

    private BlobTracker blobTracker;
    private BlobFollower blobFollower;
    private ZigzagPalette pal = new ZigzagPalette();
    private long lastRun;

    public FlockWave(LX lx) {
        super(lx);

        blobTracker = BlobTracker.getInstance(lx);
        blobFollower = new BlobFollower(blobTracker);

        addParameter(oscFollowers);
        addParameter(oscBlobs);
        addParameter(everywhere);

        addParameter(timeScale);
        addParameter(size);
        addParameter(detail);
        addParameter(ripple);

        addParameter(x);
        addParameter(y);
        addParameter(z);
        addParameter(zScale);

        addParameter(palette);
        addParameter(palShift);
        addParameter(palBias);
        addParameter(palCutoff);

        addParameter(palStart);
        addParameter(palStop);
        addParameter(spnRad);
        addParameter(maxBirds);

        addParameter(spnRate);
        addParameter(spnVary);
        addParameter(scatter);

        addParameter(spdMult);
        addParameter(maxSpd);
        addParameter(turnSec);
        addParameter(fadeInSec);
        addParameter(fadeOutSec);
    }

    public void run(double deltaMs) {
        advanceSimulation((float) deltaMs * 0.001f * timeScale.getValuef());
        blobFollower.advance((float) deltaMs * 0.001f);
        render();
        lastRun = new Date().getTime();
    }

    void advanceSimulation(float deltaSec) {
        if (oscBlobs.isOn()) {
            updateBlobTrackerParameters();

            List<BlobTracker.Blob> blobs = blobTracker.getBlobs();
            for (BlobTracker.Blob b : blobs) {
                spawnBirds(deltaSec, b.pos, b.vel, b.size);
            }

            advanceBirdsWithBlobs(deltaSec, blobs);
        } else {
            PVector focus = new PVector(x.getValuef(), y.getValuef(), z.getValuef());

            if (prevFocus != null) {
                PVector vel = PVector.sub(focus, prevFocus);
                if (deltaSec > 0) {
                    vel.div(deltaSec);
                }

                spawnBirds(deltaSec, focus, vel, 1);
                advanceBirds(deltaSec, vel);
            }

            prevFocus = focus;
        }

        removeExpiredBirds();
    }

    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        if (lastRun + 1000 < new Date().getTime()) return markers; // hack to hide markers if inactive
        if (oscFollowers.isOn()) {
            markers.addAll(blobFollower.getMarkers());
        } else {
            for (Bird bird : birds) {
                markers.add(new Octahedron(bird.pos, 1 + bird.value * 12, 0x00ffff));
            }
            if (oscBlobs.isOn()) {
                for (BlobTracker.Blob b : blobTracker.getBlobs()) {
                    markers.add(new CubeMarker(b.pos, spnRad.getValuef(), 0x00ff00));
                }
            } else if (everywhere.isOn()) {
                markers.add(new CubeMarker(
                    new PVector(model.cx, model.cy, model.cz),
                    new PVector(model.xRange / 2, model.yRange / 2, model.zRange / 2),
                    0x00ff00
                ));
            } else {
                markers.add(new CubeMarker(
                    new PVector(x.getValuef(), y.getValuef(), z.getValuef()),
                    spnRad.getValuef(),
                    0x00ff00
                ));
            }
        }
        return markers;
    }

    void updateBlobTrackerParameters() {
        blobTracker.setBlobY(y.getValuef());
    }

    void spawnBirds(float deltaSec, PVector focus, PVector vel, float weight) {
        float speed = vel.mag();
        float vary = spnVary.getValuef();
        float spawnFactor = vary * (speed / 240.0f) + (1.0f - vary);
        numToSpawn += deltaSec * spnRate.getValuef() * weight * spawnFactor;

        while (numToSpawn >= 1.0) {
            spawnBird(focus);
            numToSpawn -= 1.0;
        }

        if (vary > 0.01) {
            if (FastMath.random() < numToSpawn) {
                spawnBird(focus);
            }
            numToSpawn = 0;
        }
    }

    void spawnBird(PVector focus) {
        if ((birds.size() + 1) <= maxBirds.getValue()) {
            PVector pos = getRandomUnitVector();
            if (everywhere.isOn()) {
                pos.x = model.xMin + (float) Math.random() * (model.xMax - model.xMin);
                pos.y = model.yMin + (float) Math.random() * (model.yMax - model.yMin);
                pos.z = model.zMin + (float) Math.random() * (model.zMax - model.zMin);
            } else {
                pos.mult(spnRad.getValuef());
                pos.add(focus);
            }
            birds.add(new Bird(pos, LXColor.hsb(FastMath.random() * 360, FastMath.random() * 100, 100)));
        }
    }

    void advanceBirds(float deltaSec, PVector vel) {
        PVector targetVel = PVector.mult(vel, spdMult.getValuef());
        for (Bird b : birds) {
            b.run(deltaSec, targetVel);
        }
    }

    void advanceBirdsWithBlobs(float deltaSec, List<BlobTracker.Blob> blobs) {
        for (Bird b : birds) {
            PVector velSum = new PVector(0, 0, 0);
            float totalWeight = 0;
            for (BlobTracker.Blob blob : blobs) {
                float distance = PVector.sub(b.pos, blob.pos).mag();
                float weight = 1.0f / (distance * distance);
                PVector.add(velSum, PVector.mult(blob.vel, weight), velSum);
                totalWeight += weight;
            }

            if (totalWeight > 0) {
                velSum.div(totalWeight);
            }

            PVector targetVel = PVector.mult(velSum, spdMult.getValuef());
            b.run(deltaSec, targetVel);
        }
    }

    void removeExpiredBirds() {
        List<Bird> expired = new ArrayList<Bird>();
        for (Bird b : birds) {
            if (b.expired) {
                expired.add(b);
            }
        }
        birds.removeAll(expired);
    }

    void render() {  // choose a rendering style
        if (oscFollowers.isOn()) {
            List<Bird> followBirds = new ArrayList<Bird>();
            for (BlobFollower.Follower f : blobFollower.getFollowers()) {
                Bird b = new Bird(f.pos, 0);
                b.vel = f.vel;
                b.value = f.value;
                b.elapsedSec = f.ageSec;
                followBirds.add(b);
            }
            renderPlasma(followBirds);
        } else {
            renderPlasma(birds);
        }
    }

    void renderTrails() {
        float radius = size.getValuef();
        float sqRadius = radius * radius;

        for (LXPoint p : model.points) {
            int rgb = 0;
            for (Bird b : birds) {
                if (Math.abs(b.pos.x - p.x) < radius) {
                    if ((b.pos.x - p.x) * (b.pos.x - p.x) + (b.pos.y - p.y) * (b.pos.y - p.y) < sqRadius) {
                        rgb = LXColor.add(rgb, LXColor.lerp(0, b.rgb, b.value));
                    }
                }
            }
            colors[p.index] = LXColor.add(LXColor.lerp(colors[p.index], 0, 0.1), rgb);
        }
    }

    void renderVoronoi() {
        float radius = size.getValuef();

        radius = 10000;
        for (LXPoint p : model.points) {
            Bird closestBird = null;
            float minSqDist = 1e6f;
            for (Bird b : birds) {
                if (Math.abs(b.pos.x - p.x) < radius) {
                    float sqDist = (b.pos.x - p.x) * (b.pos.x - p.x) + (b.pos.y - p.y) * (b.pos.y - p.y);
                    if (sqDist < minSqDist) {
                        minSqDist = sqDist;
                        closestBird = b;
                    }
                }
            }
            colors[p.index] = minSqDist > radius ? 0 : LXColor.lerp(0, closestBird.rgb, closestBird.value);
        }
    }

    ColorPalette getPalette() {
        pal.setPalette(paletteLibrary.get(palette.getOption()));
        pal.setBottom(palStart.getValue());
        pal.setTop(palStop.getValue());
        pal.setBias(palBias.getValue());
        pal.setShift(palShift.getValue());
        pal.setCutoff(palCutoff.getValue());
        return pal;
    }

    private static class FlockWaveRenderPlasmaKernel extends SLKernel {

        int numPoints;
        float[] pointsXYZ;
        float[] result;

        int numBirds = -1;
        int numBirdsMax = -1;
        float[] birdPosX;
        float[] birdPosY;
        float[] birdPosZ;
        float[] birdValue;
        float[] birdElapsedSec;

        float waveNumber;
        float extent;
        float rippleSpeed;
        float zSqFactor;
        float shift;

        FlockWaveRenderPlasmaKernel() {
            setExplicit(true);
        }

        private float calcPlasmaLayer(int birdIndex) {
            int i = getGlobalId();

            float birdX = birdPosX[birdIndex];
            float birdY = birdPosY[birdIndex];
            float birdZ = birdPosZ[birdIndex];

            float pointX = pointsXYZ[3 * i];
            float pointY = pointsXYZ[3 * i + 1];
            float pointZ = pointsXYZ[3 * i + 2];

            float x_diff = birdX - pointX;
            float y_diff = birdY - pointY;
            float z_diff = birdZ - pointZ;

            float sqDist = (x_diff * x_diff + y_diff * y_diff + z_diff * z_diff) / (extent * extent);

            if (sqDist > 1) { return 0; }

            float dz = z_diff / extent;
            float phase = sqrt(sqDist + dz * dz * zSqFactor);
            float a = 1 - sqDist;
            return a * a * birdValue[birdIndex]
                    * sin(waveNumber * 2 * PI * phase - birdElapsedSec[birdIndex] * rippleSpeed)
                    * cos(waveNumber * 5 / 4 * phase);
        }

        @Override public void run() {
            int i = getGlobalId();
            if (i >= numPoints) { return; }

            float sum = shift;
            for (int j = 0; j < numBirds; j++) {
                sum += calcPlasmaLayer(j);
            }
            result[i] = sum;
        }
    }

    private final FlockWaveRenderPlasmaKernel kernel = new FlockWaveRenderPlasmaKernel();

    void renderPlasma(final Collection<Bird> birds) {
        if (birds.size() > 0) {
            if (kernel.result == null || kernel.result.length != colors.length) {
                kernel.numPoints = colors.length;
                kernel.put(kernel.pointsXYZ = ((SLModel)model).pointsXYZ);
                kernel.result = new float[colors.length];
            }

            int maxBirdsValue = maxBirds.getValuei();
            kernel.numBirds = birds.size();
            if (kernel.numBirds > maxBirdsValue) {
                maxBirdsValue = kernel.numBirds;
            }

            if (kernel.numBirdsMax != maxBirdsValue) {
                kernel.numBirdsMax = maxBirdsValue;
                kernel.put(kernel.birdPosX = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdPosY = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdPosZ = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdValue = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdElapsedSec = new float[kernel.numBirdsMax]);
            }

            int i = 0;
            for (Bird bird : birds) {
                if (i >= kernel.numBirdsMax)
                    break;

                kernel.birdPosX[i] = bird.pos.x;
                kernel.birdPosY[i] = bird.pos.y;
                kernel.birdPosZ[i] = bird.pos.z;
                kernel.birdValue[i] = bird.value;
                kernel.birdElapsedSec[i] = bird.elapsedSec;
                i++;
            }

            kernel.waveNumber = detail.getValuef();
            kernel.extent = size.getValuef();
            kernel.rippleSpeed = ripple.getValuef();
            float zFactor = (float) FastMath.pow(10, zScale.getValuef() / 10);
            kernel.zSqFactor = zFactor * zFactor - 1;
            kernel.shift = palShift.getValuef();

            kernel.executeForSize(model.points.length);
            kernel.get(kernel.result);

            final ColorPalette pal = getPalette();
            final float[] result = kernel.result;

            model.getPoints().parallelStream().forEach(p -> {
                colors[p.index] = pal.getColor(result[p.index]);
            });
        } else {
            int color = getPalette().getColor(palShift.getValue());
            Arrays.fill(colors, color);
        }
    }

    PVector getRandomUnitVector() {
        PVector pos = new PVector();
        while (true) {
            pos.set((float) FastMath.random() * 2 - 1, (float) FastMath.random() * 2 - 1, (float) FastMath.random() * 2 - 1);
            if (pos.mag() < 1) {
                return pos;
            }
        }
    }

    void setPalette(String name) {
        String[] options = palette.getOptions();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(name)) {
                palette.setValue(i);
            }
        }
    }

    public class Bird implements Comparable<Bird> {
        public PVector pos;
        public PVector vel;
        public int rgb;
        public float value;
        public float elapsedSec;
        public boolean expired;
        public double[] renderedValues;

        Bird(PVector pos, int rgb) {
            this.pos = pos;
            this.vel = PVector.mult(getRandomUnitVector(), scatter.getValuef());
            this.rgb = rgb;
            this.value = 0;
            this.elapsedSec = 0;
            this.expired = false;
            this.renderedValues = new double[colors.length];
        }

        void run(float deltaSec, PVector targetVel) {
            advance(deltaSec);
            turn(deltaSec, targetVel);

            elapsedSec += deltaSec;
            if (elapsedSec < fadeInSec.getValuef()) {
                value = elapsedSec / fadeInSec.getValuef();
            } else {
                value = (float) FastMath.pow(0.1, (elapsedSec - fadeInSec.getValuef()) / fadeOutSec.getValuef());
                if (value < 0.004) expired = true;
            }
        }

        void advance(float deltaSec) {
            pos.add(PVector.mult(vel, (float) deltaSec));
        }

        void turn(float deltaSec, PVector targetVel) {
            float speed = vel.mag();
            float targetSpeed = targetVel.mag();

            float frac = (float) FastMath.pow(0.1, deltaSec / turnSec.getValuef());
            vel = PVector.add(PVector.mult(vel, frac), PVector.mult(targetVel, 1 - frac));
            speed = speed * frac + targetSpeed * (1 - frac);
            if (targetSpeed > maxSpd.getValuef()) targetSpeed = maxSpd.getValuef();

            float mag = vel.mag();
            if (mag > 0 && mag < speed) vel.div(mag / speed);
        }

        public int compareTo(Bird other) {
            return Float.compare(pos.x, other.pos.x);
        }
    }
}
