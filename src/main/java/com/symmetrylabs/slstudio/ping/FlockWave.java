package com.symmetrylabs.slstudio.ping;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.math3.util.FastMath;
import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.kernel.SLKernel;
import com.symmetrylabs.slstudio.palettes.ColorPalette;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import com.symmetrylabs.slstudio.util.BlobFollower;
import com.symmetrylabs.slstudio.util.BlobTracker;
import com.symmetrylabs.slstudio.util.CubeMarker;
import com.symmetrylabs.slstudio.util.Marker;
import com.symmetrylabs.slstudio.util.Octahedron;

public class FlockWave extends SLPatternWithMarkers {
    CompoundParameter timeScale = new CompoundParameter("timeScale", 1, 0, 1);  // time scaling factor
    DiscreteParameter birdMode = new DiscreteParameter("birdMode", new String[] {
        "everywhere", "nearXyz", "nearBlobs", "atBlobs", "blobPull"
    });
    BooleanParameter perSun = new BooleanParameter("perSun");
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
    CompoundParameter spdMult = new CompoundParameter("spdMult", 1, 0, 8);  // (ratio) bird target speed / focus speed
    CompoundParameter maxSpd = new CompoundParameter("maxSpd", 10, 0, 100);  // max bird speed (in/s)
    CompoundParameter turnSec = new CompoundParameter("turnSec", 1, 0, 2);  // time (s) to complete 90% of a turn
    CompoundParameter fadeInSec = new CompoundParameter("fadeInSec", 0.5, 0, 2);  // time (s) to fade up to 100% intensity
    CompoundParameter fadeOutSec = new CompoundParameter("fadeOutSec", 1, 0, 2);// time (s) to fade down to 10% intensity
    CompoundParameter pushRad = new CompoundParameter("pushRad", 400, 0, 1000);  // radius (in) of the "push" field around blobs in blobPull mode
    DiscreteParameter maxPushers = new DiscreteParameter("maxPushers", 4, 0, 10);

    CompoundParameter size = new CompoundParameter("size", 100, 0, 2000);  // render radius of each bird (in)
    CompoundParameter detail = new CompoundParameter("detail", 4, 0, 10);  // ripple spatial frequency (number of waves)
    CompoundParameter ripple = new CompoundParameter("ripple", 0, -10, 10);  // ripple movement (waves/s)
    DiscreteParameter palette = new DiscreteParameter("palette", ((SLStudioLX) lx).paletteLibrary.getNames());
        // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward start or stop
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

    PVector ZERO = new PVector(0, 0, 0);
    PVector prevFocus = null;
    List<Bird> birds = new CopyOnWriteArrayList<>();
    float numToSpawn = 0f;

    private BlobTracker blobTracker;
    private BlobFollower blobFollower;
    private ZigzagPalette pal = new ZigzagPalette();
    private long lastRun;

    public FlockWave(LX lx) {
        super(lx);

        blobTracker = BlobTracker.getInstance(lx);
        blobFollower = new BlobFollower(blobTracker);

        addParameter(birdMode);
        addParameter(perSun);

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
        addParameter(pushRad);
        addParameter(maxPushers);
    }

    public void run(double deltaMs) {
        advanceSimulation((float) deltaMs * 0.001f * timeScale.getValuef());
        blobFollower.advance((float) deltaMs * 0.001f);
        render();
        lastRun = new Date().getTime();
    }

    void advanceSimulation(float deltaSec) {
        if (birdMode.getOption().equals("nearBlobs")) {  // spawn birds near blobs
            updateBlobTrackerParameters();

            List<BlobTracker.Blob> blobs = blobTracker.getBlobs();
            for (BlobTracker.Blob b : blobs) {
                spawnBirds(deltaSec, b.pos, b.vel, b.size);
            }

            advanceBirdsWithBlobs(deltaSec, blobs);
        } else if (birdMode.getOption().equals("blobPull")) {
            spawnBirds(deltaSec, ZERO, ZERO, 1);
            advanceBirdsWithPull(deltaSec, blobFollower.getFollowers());
        } else {  // spawn birds near xyz, or everywhere (spawnBirds will decide)
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

        if (birdMode.getOption().equals("atBlobs") ||
              birdMode.getOption().equals("blobPull")) {
            markers.addAll(blobFollower.getMarkers());
        }
        if (!birdMode.getOption().equals("atBlobs")) {
            for (Bird bird : birds) {
                markers.add(new Octahedron(bird.pos, 1 + bird.value * 12, 0x00ffff));
            }
        }
        if (birdMode.getOption().equals("nearBlobs")) {
            for (BlobTracker.Blob b : blobTracker.getBlobs()) {
                markers.add(new CubeMarker(b.pos, spnRad.getValuef(), 0x00ff00));
            }
        }
        if (birdMode.getOption().equals("everywhere") ||
              birdMode.getOption().equals("blobPull")) {
            markers.add(new CubeMarker(
                new PVector(model.cx, model.cy, model.cz),
                new PVector(model.xRange / 2, model.yRange / 2, model.zRange / 2),
                0x00ff00
            ));
        }
        if (birdMode.getOption().equals("nearXyz")) {
            markers.add(new CubeMarker(
                new PVector(x.getValuef(), y.getValuef(), z.getValuef()),
                spnRad.getValuef(),
                0x00ff00
            ));
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
            PVector pos;
            if (birdMode.getOption().equals("everywhere") ||
                     birdMode.getOption().equals("blobPull")) {  // spawn throughout the model volume
                pos = chooseSpawnPosition();
            } else {  // spawn near the given focus coordinates
                pos = getRandomUnitVector();
                pos.mult(spnRad.getValuef());
                pos.add(focus);
            }
            int rgb = LXColor.hsb(FastMath.random() * 360, FastMath.random() * 100, 100);
            birds.add(new Bird(pos, rgb, !birdMode.getOption().equals("blobPull")));
        }
    }

    /**
     * Chooses the position for a new bird, anywhere within the model's bounding box,
     * preferring regions where there aren't already a lot of birds.
     */
    PVector chooseSpawnPosition() {
        PVector pos = new PVector();
        PVector best = new PVector();
        float bestMinDist = 0;
        for (int sample = 0; sample < 20; sample++) {
            pos.x = model.xMin + (float) Math.random() * (model.xMax - model.xMin);
            pos.y = model.yMin + (float) Math.random() * (model.yMax - model.yMin);
            pos.z = model.zMin + (float) Math.random() * (model.zMax - model.zMin);
            float minDist = Float.POSITIVE_INFINITY;
            for (Bird b : birds) {
                float dist = PVector.sub(pos, b.pos).mag();
                if (dist < minDist) {
                    minDist = dist;
                }
            }
            if (minDist > bestMinDist) {
                best.set(pos);
                bestMinDist = minDist;
            }
        }
        return best;
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

    void advanceBirdsWithPull(float deltaSec, List<BlobFollower.Follower> followers) {
        List<BlobFollower.Follower> sortedFollowers = new ArrayList<>();
        sortedFollowers.addAll(followers);
        sortedFollowers.sort(new Comparator<BlobFollower.Follower>() {
            public int compare(BlobFollower.Follower a, BlobFollower.Follower b) {
                return Float.compare(-a.vel.mag(), -b.vel.mag());
            }
        });

        int numPushers = Math.min(sortedFollowers.size(), maxPushers.getValuei());
        float pushRadius = pushRad.getValuef();
        for (Bird b : birds) {
            PVector targetVel = new PVector(0, 0, 0);
            for (int i = 0; i < numPushers; i++) {
                BlobFollower.Follower f = sortedFollowers.get(i);
                float dist = PVector.sub(f.pos, b.pos).mag() / pushRadius;
                if (dist < 1.0f) {
                    float factor = (dist < 0.5f) ? 1.0f : 1.0f - (dist - 0.5f)*2;
                    targetVel.add(PVector.mult(f.vel, factor));
                }
            }
            targetVel.mult(spdMult.getValuef());
            targetVel.z *= 1.0f/zScale.getValuef();
            b.run(deltaSec, targetVel);
            if (b.pos.x < model.xMin || b.pos.x > model.xMax ||
                  b.pos.y < model.yMin || b.pos.y > model.yMax ||
                  b.pos.z < model.zMin || b.pos.z > model.zMax) {
                b.startFadeOut();
            }
        }
        for (int i = maxBirds.getValuei(); i < birds.size(); i++) {
            birds.get(i).startFadeOut();
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

    void render() {
        if (birdMode.getOption().equals("atBlobs")) {  // render birds at the blob follower locations
            List<Bird> followBirds = new ArrayList<Bird>();
            for (BlobFollower.Follower f : blobFollower.getFollowers()) {
                Bird b = new Bird(f.pos, 0);
                b.vel = f.vel;
                b.value = f.value;
                b.elapsedSec = f.ageSec;
                followBirds.add(b);
            }
            renderPlasma(followBirds);
        } else {  // render birds at the actual bird locations in the "birds" array
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
        pal.setPalette(((SLStudioLX) lx).paletteLibrary.get(palette.getOption()));
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
                kernel.put(kernel.pointsXYZ = model.pointsXYZ);
                kernel.result = new float[colors.length];
            }

            int maxBirdsValue = maxBirds.getValuei();
            if (kernel.numBirdsMax != maxBirdsValue) {
                kernel.numBirdsMax = maxBirdsValue;
                kernel.put(kernel.birdPosX = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdPosY = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdPosZ = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdValue = new float[kernel.numBirdsMax]);
                kernel.put(kernel.birdElapsedSec = new float[kernel.numBirdsMax]);
            }

            kernel.numBirds = birds.size();
            if (kernel.numBirds > kernel.numBirdsMax) {
                kernel.numBirds = kernel.numBirdsMax;
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

    public class Bird implements Comparable<Bird> {
        public PVector pos;
        public PVector vel;
        public int rgb;
        public float value;
        public float elapsedSec;
        public float fadeOutStartSec;
        public boolean expired;
        public double[] renderedValues;

        Bird(PVector pos, int rgb, boolean fadeOut) {
            this.pos = pos;
            this.vel = PVector.mult(getRandomUnitVector(), scatter.getValuef());
            this.rgb = rgb;
            this.value = 0;
            this.elapsedSec = 0;
            this.fadeOutStartSec = fadeOut ? fadeInSec.getValuef() : Float.POSITIVE_INFINITY;
            this.expired = false;
            this.renderedValues = new double[colors.length];
        }

        Bird(PVector pos, int rgb) {
            this(pos, rgb, true);
        }

        void run(float deltaSec, PVector targetVel) {
            advance(deltaSec);
            turn(deltaSec, targetVel);

            elapsedSec += deltaSec;
            if (elapsedSec < fadeInSec.getValuef()) {
                value = elapsedSec / fadeInSec.getValuef();
            } else if (elapsedSec < fadeOutStartSec) {
                value = 1.0f;
            } else {
                // in blobPull mode, only start expiring after the bird has left the model volume
                value = (float) FastMath.pow(0.1, (elapsedSec - fadeOutStartSec) / fadeOutSec.getValuef());
                if (value < 0.004) expired = true;
            }
        }

        void startFadeOut() {
            if (fadeOutStartSec > elapsedSec) {
                fadeOutStartSec = elapsedSec;
            }
        }

        void advance(float deltaSec) {
            pos.add(PVector.mult(vel, (float) deltaSec));
        }

        void turn(float deltaSec, PVector targetVel) {
            float frac = (float) FastMath.pow(0.1, deltaSec / turnSec.getValuef());
            vel = PVector.add(PVector.mult(vel, frac), PVector.mult(targetVel, 1 - frac));
            if (!birdMode.getOption().equals("blobPull")) {
                float speed = vel.mag();
                float targetSpeed = targetVel.mag();

                speed = speed * frac + targetSpeed * (1 - frac);
                if (targetSpeed > maxSpd.getValuef()) targetSpeed = maxSpd.getValuef();

                float mag = vel.mag();
                if (mag > 0 && mag < speed) vel.div(mag / speed);
            }
        }

        public int compareTo(Bird other) {
            return Float.compare(pos.x, other.pos.x);
        }
    }
}
