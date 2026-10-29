package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.pattern.cubes.CubeBeatsAudio;
import com.symmetrylabs.util.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Random;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.*;

/**
 * EDM-oriented audio-reactive pattern, sibling of BeatDrop with different
 * generators: domain-warped noise, kick-launched particles, a beat-stepped
 * 3D cellular automaton, and spectrum-driven wave interference.
 * Modes: Warp, Burst, Cells, Harmonics.
 */
public class DropField extends SLPattern<SLModel> {
    public static final String GROUP_NAME = "Audio";
    private static final float INV_HALF_DIAG = 1f / 0.866f;
    private static final int MAX_P = 96;
    private static final int MAX_WAVES = 10;

    private final GraphicMeter eq = new GraphicMeter(lx.engine.audio.getInput());
    private final CubeBeatsAudio audio = new CubeBeatsAudio();
    private final float[] bands = new float[CubeBeatsAudio.NUM_BANDS];
    private final Random rng = new Random(4242);

    public final DiscreteParameter modeParam =
        new DiscreteParameter("Mode", new String[]{"Warp", "Burst", "Cells", "Harmonics"});
    public final CompoundParameter gainParam = new CompoundParameter("Gain", 0.5);
    public final CompoundParameter sensParam = new CompoundParameter("Sens", 0.5);
    public final CompoundParameter attackParam = new CompoundParameter("Attack", 34, 5, 150);
    public final CompoundParameter releaseParam = new CompoundParameter("Release", 236, 60, 1200);
    public final CompoundParameter kickParam = new CompoundParameter("Kick", 0.8);
    public final CompoundParameter clapParam = new CompoundParameter("Clap", 0.8);
    public final CompoundParameter hatsParam = new CompoundParameter("Hats", 0.3);
    public final CompoundParameter floorParam = new CompoundParameter("Floor", 0.08);
    public final CompoundParameter speedParam = new CompoundParameter("Speed", 0.5);
    public final CompoundParameter sizeParam = new CompoundParameter("Size", 0.5);
    public final CompoundParameter densityParam = new CompoundParameter("Density", 0.5);
    public final CompoundParameter hueParam = new CompoundParameter("Hue", 0, -180, 180);
    public final CompoundParameter spreadParam = new CompoundParameter("Spread", 60, 0, 360);
    public final CompoundParameter accentParam = new CompoundParameter("Accent", 180, 0, 360);
    public final CompoundParameter satParam = new CompoundParameter("Sat", 90, 0, 100);
    public final CompoundParameter trailsParam = new CompoundParameter("Trails", 0.3);

    // per-point geometry, indexed by point.index
    private int n;
    private float[] pXn, pYn, pZn, pDist;
    private float[] bufH, bufS, bufB, trailH, trailS, trailB, sparkle;

    // frame-derived audio state
    private double nowMs;
    private float hueWalk;
    private float kickEnv, clapEnv, clapFlash;
    private float lastKickStrength;
    private float floorB;

    // shared clocks
    private double noiseT;
    private float warpAmt;

    // Burst particles (preallocated pool, alive prefix)
    private final P[] parts = new P[MAX_P];
    private int alive;

    private static class P {
        float x, y, z, vx, vy, vz, life, maxLife, hue, size;
        boolean clap;
    }

    // Cells automaton
    private int gridG = -1;
    private byte[] cells, next;
    private float[] cellAge;
    private int[] pCell;
    private int lastPhaseBucket = -1;
    private double freeStepMs;
    private int clapPlaneAxis = -1, clapPlaneK;

    // Harmonics wave table
    private final float[] dirX = new float[MAX_WAVES];
    private final float[] dirY = new float[MAX_WAVES];
    private final float[] dirZ = new float[MAX_WAVES];
    private final float[] phase = new float[MAX_WAVES];

    public DropField(LX lx) {
        super(lx);
        addParameter(modeParam);
        addParameter(gainParam);
        addParameter(sensParam);
        addParameter(attackParam);
        addParameter(releaseParam);
        addParameter(kickParam);
        addParameter(clapParam);
        addParameter(hatsParam);
        addParameter(floorParam);
        addParameter(speedParam);
        addParameter(sizeParam);
        addParameter(densityParam);
        addParameter(hueParam);
        addParameter(spreadParam);
        addParameter(accentParam);
        addParameter(satParam);
        addParameter(trailsParam);

        CubeBeatsAudio.configureMeter(eq);
        eq.range.setNormalized(0.4);
        eq.start();
        addModulator(eq).start();

        for (int i = 0; i < MAX_P; i++) parts[i] = new P();
        goldenSpiral();

        precompute();
    }

    private void goldenSpiral() {
        // unit directions on a golden-spiral sphere covering
        float ga = (float) (Math.PI * (3 - Math.sqrt(5)));
        for (int k = 0; k < MAX_WAVES; k++) {
            float y = 1f - 2f * (k + 0.5f) / MAX_WAVES;
            float r = (float) Math.sqrt(Math.max(0, 1 - y * y));
            float th = ga * k;
            dirX[k] = r * (float) Math.cos(th);
            dirY[k] = y;
            dirZ[k] = r * (float) Math.sin(th);
        }
    }

    private void precompute() {
        n = model.points.length;
        if (n == 0) return;
        pXn = new float[n]; pYn = new float[n]; pZn = new float[n]; pDist = new float[n];
        bufH = new float[n]; bufS = new float[n]; bufB = new float[n];
        trailH = new float[n]; trailS = new float[n]; trailB = new float[n];
        sparkle = new float[n];
        for (LXPoint p : model.points) {
            int i = p.index;
            pXn[i] = p.xn; pYn[i] = p.yn; pZn[i] = p.zn;
            float dx = p.xn - 0.5f, dy = p.yn - 0.5f, dz = p.zn - 0.5f;
            pDist[i] = constrain((float) Math.sqrt(dx * dx + dy * dy + dz * dz) * INV_HALF_DIAG, 0, 1);
        }
    }

    private void rebuildCells(int g) {
        gridG = g;
        cells = new byte[g * g * g];
        next = new byte[g * g * g];
        cellAge = new float[g * g * g];
        pCell = new int[n];
        for (int i = 0; i < n; i++) {
            int ix = Math.min(g - 1, (int) (pXn[i] * g));
            int iy = Math.min(g - 1, (int) (pYn[i] * g));
            int iz = Math.min(g - 1, (int) (pZn[i] * g));
            pCell[i] = ix + g * (iy + g * iz);
        }
    }

    private static float follow(float cur, float target, double dt, float tauUp, float tauDown) {
        float tau = target > cur ? tauUp : tauDown;
        float alpha = 1f - (float) Math.exp(-dt / Math.max(1f, tau));
        return cur + (target - cur) * alpha;
    }

    private static float hueLerp(float a, float b, float t) {
        float dh = ((b - a) % 360 + 540) % 360 - 180;
        return a + dh * t;
    }

    private static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    private static float hash01(int i) {
        return ((i * 1103515245 + 12345) & 0x7fffffff) / (float) 0x7fffffff;
    }

    private void updateAudio(double deltaMs) {
        eq.gain.setNormalized(gainParam.getValuef());
        for (int i = 0; i < CubeBeatsAudio.NUM_BANDS; i++) {
            bands[i] = i < eq.numBands ? eq.getBandf(i) : 0;
        }
        audio.sensitivity = sensParam.getValuef();
        audio.attackMs = attackParam.getValuef();
        audio.releaseMs = releaseParam.getValuef();
        audio.update(bands, deltaMs);

        float kickAmt = kickParam.getValuef();
        kickEnv = audio.kickEnv * kickAmt;
        float clapAmt = clapParam.getValuef();
        clapEnv = audio.snareEnv * clapAmt;
        clapFlash = clapEnv * clapEnv;
        float spread = spreadParam.getValuef();

        if (audio.kick) {
            lastKickStrength = audio.kickStrength;
            hueWalk = (hueWalk + audio.kickStrength * spread * 0.15f) % 360;
            noiseT += 0.15 * audio.kickStrength * kickAmt;
        }

        noiseT += deltaMs * (0.00005 + speedParam.getValuef() * 0.0004) * (1 + 2 * audio.bass);
        warpAmt = follow(warpAmt, audio.mid, deltaMs, 60, 400);

        float breath = 0.5f + 0.5f * (float) Math.sin(nowMs * TWO_PI / 5000);
        floorB = floorParam.getValuef() * 100 * (0.6f + 0.4f * breath);
    }

    private void onHatAudio() {
        int count = (int) (hatsParam.getValuef() * audio.hatStrength * n * 0.03f);
        for (int j = 0; j < count; j++) {
            sparkle[rng.nextInt(n)] = 1;
        }
    }

    private void spawnBurst(float baseHue, float accentHue, float spread, float size) {
        if (audio.kick) {
            float density = densityParam.getValuef();
            int count = (int) (6 + 24 * density * audio.kickStrength);
            float ox = 0.5f, oy = 0.5f, oz = 0.5f;
            if (audio.kickCount % 2 == 1 && n > 0) {
                int i = rng.nextInt(n);
                ox = pXn[i]; oy = pYn[i]; oz = pZn[i];
            }
            for (int j = 0; j < count && alive < MAX_P; j++) {
                P p = parts[alive++];
                p.x = ox; p.y = oy; p.z = oz;
                float gx = (float) rng.nextGaussian(), gy = (float) rng.nextGaussian(), gz = (float) rng.nextGaussian();
                float len = (float) Math.sqrt(gx * gx + gy * gy + gz * gz);
                if (len < 1e-6f) { gx = 0; gy = 1; gz = 0; len = 1; }
                float spd = (0.6f + 0.9f * rng.nextFloat()) * (0.4f + 0.8f * kickParam.getValuef() * audio.kickStrength);
                p.vx = gx / len * spd; p.vy = gy / len * spd; p.vz = gz / len * spd;
                p.life = 0; p.maxLife = 600 + 900 * rng.nextFloat();
                p.hue = baseHue + rng.nextFloat() * spread;
                p.size = 0.06f + 0.1f * size;
                p.clap = false;
            }
        }
        if (audio.snare) {
            float density = densityParam.getValuef();
            int count = (int) (4 + 16 * density * audio.snareStrength);
            for (int j = 0; j < count && alive < MAX_P; j++) {
                P p = parts[alive++];
                p.x = rng.nextFloat(); p.y = 1f; p.z = rng.nextFloat();
                float spd = -(0.5f + rng.nextFloat() * 0.8f) * clapParam.getValuef();
                p.vx = 0; p.vy = spd; p.vz = 0;
                p.life = 0; p.maxLife = 500 + 500 * rng.nextFloat();
                p.hue = accentHue;
                p.size = 0.05f + 0.08f * size;
                p.clap = true;
            }
        }
    }

    private void seedCells(float density) {
        int g = gridG;
        int count = (int) (density * g * g * 0.6f * (0.5f + audio.kickStrength));
        int cx = rng.nextInt(g), cy = rng.nextInt(g), cz = rng.nextInt(g);
        float rad = 0.2f * g;
        for (int j = 0; j < count; j++) {
            int ix = Math.min(g - 1, Math.max(0, (int) (cx + rng.nextGaussian() * rad)));
            int iy = Math.min(g - 1, Math.max(0, (int) (cy + rng.nextGaussian() * rad)));
            int iz = Math.min(g - 1, Math.max(0, (int) (cz + rng.nextGaussian() * rad)));
            cells[ix + g * (iy + g * iz)] = 1;
        }
    }

    private void clapPlane() {
        int g = gridG;
        int axis = rng.nextInt(3);
        int k = rng.nextInt(g);
        clapPlaneAxis = axis; clapPlaneK = k;
        for (int a = 0; a < g; a++) {
            for (int b = 0; b < g; b++) {
                int idx;
                if (axis == 0) idx = k + g * (a + g * b);
                else if (axis == 1) idx = a + g * (k + g * b);
                else idx = a + g * (b + g * k);
                cells[idx] = 1;
            }
        }
    }

    private void stepCells(double deltaMs) {
        int g = gridG;
        float dt = (float) deltaMs;
        for (int i = 0; i < cellAge.length; i++) cellAge[i] += dt;
        System.arraycopy(cells, 0, next, 0, cells.length);
        for (int z = 0; z < g; z++) {
            for (int y = 0; y < g; y++) {
                for (int x = 0; x < g; x++) {
                    int idx = x + g * (y + g * z);
                    byte st = cells[idx];
                    if (st == 1) {
                        next[idx] = 2;
                    } else if (st == 2) {
                        next[idx] = 0;
                    } else {
                        int nb = 0;
                        for (int dz = -1; dz <= 1; dz++) {
                            int zz = z + dz;
                            if (zz < 0 || zz >= g) continue;
                            for (int dy = -1; dy <= 1; dy++) {
                                int yy = y + dy;
                                if (yy < 0 || yy >= g) continue;
                                for (int dx = -1; dx <= 1; dx++) {
                                    int xx = x + dx;
                                    if (xx < 0 || xx >= g || (dx == 0 && dy == 0 && dz == 0)) continue;
                                    if (cells[xx + g * (yy + g * zz)] == 1) nb++;
                                }
                            }
                        }
                        if (nb == 2 || nb == 3) next[idx] = 1;
                    }
                }
            }
        }
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] != next[i]) cellAge[i] = 0;
            cells[i] = next[i];
        }
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        nowMs += deltaMs;
        if (n == 0) {
            markModified(SRGB8);
            return;
        }
        if (!(deltaMs > 0) || Double.isNaN(deltaMs)) deltaMs = 0.001;

        updateAudio(deltaMs);

        float baseHue = palette.getHuef() + hueParam.getValuef() + hueWalk;
        float accentHue = baseHue + accentParam.getValuef();
        float sat = satParam.getValuef();
        float spread = spreadParam.getValuef();
        float size = sizeParam.getValuef();
        float speed = speedParam.getValuef();
        float bass = audio.bass, presence = audio.presence;

        if (audio.hat) onHatAudio();

        int mode = ((modeParam.getValuei() % 4) + 4) % 4;
        switch (mode) {
            case 0:
                renderWarp(baseHue, accentHue, sat, spread, size, bass);
                break;
            case 1:
                spawnBurst(baseHue, accentHue, spread, size);
                renderBurst(deltaMs, baseHue, sat, spread, size, bass);
                break;
            case 2: {
                int g = Math.min(16, 6 + Math.round(size * 10));
                if (g != gridG) rebuildCells(g);
                boolean step = false;
                if (audio.kick) {
                    seedCells(densityParam.getValuef());
                    step = true;
                }
                if (audio.tempoConfidence > 0.3f && presence > 0.25f) {
                    int bucket = (int) (audio.beatPhase * 2);
                    if (lastPhaseBucket >= 0 && bucket != lastPhaseBucket) step = true;
                    lastPhaseBucket = bucket;
                    freeStepMs = 0;
                } else {
                    lastPhaseBucket = -1;
                    freeStepMs += deltaMs;
                    if (freeStepMs >= 900 - 700 * speed) {
                        freeStepMs = 0;
                        step = true;
                    }
                }
                if (audio.snare) clapPlane();
                if (step) stepCells(deltaMs);
                renderCells(baseHue, accentHue, sat, spread, bass);
                break;
            }
            case 3:
                renderHarmonics(deltaMs, baseHue, accentHue, sat, spread, size, speed, bass);
                break;
        }

        // hat sparkle pass (all modes)
        float sparkDecay = (float) Math.exp(-deltaMs / 80);
        for (int i = 0; i < n; i++) {
            sparkle[i] *= sparkDecay;
            if (sparkle[i] > 0.05f) {
                bufB[i] = Math.max(bufB[i], sparkle[i] * 100);
                bufS[i] = Math.min(bufS[i], sat * (1 - sparkle[i]));
            }
        }

        // trails combine + output
        float tv = trailsParam.getValuef();
        float decay = tv <= 0 ? 0 : (float) Math.exp(-deltaMs / (25 + tv * tv * 1600));
        for (int i = 0; i < n; i++) {
            float tb = trailB[i] * decay;
            if (tb > bufB[i]) {
                trailB[i] = tb;
                bufB[i] = tb;
                bufH[i] = trailH[i];
                bufS[i] = trailS[i];
            } else {
                trailB[i] = bufB[i];
                trailH[i] = bufH[i];
                trailS[i] = bufS[i];
            }
            colors[i] = LXColor.hsb(bufH[i], bufS[i], Math.min(100, bufB[i]));
        }
        markModified(SRGB8);
    }

    private void renderWarp(float baseHue, float accentHue, float sat, float spread,
                            float size, float bass) {
        float sc = 1.5f + 4 * (1 - size);
        float wAmp = warpAmt * 2.5f;
        float thr = 0.72f - 0.45f * bass - 0.3f * kickEnv;
        float edge = 0.08f + 0.1f * size;
        double t = noiseT;
        double t2 = noiseT * 0.7;
        for (int i = 0; i < n; i++) {
            float x = pXn[i] * sc, y = pYn[i] * sc, z = pZn[i] * sc;
            float qx = (float) SimplexNoise.noise(x, y, z, t);
            float qy = (float) SimplexNoise.noise(x + 5.2, y + 1.3, z, t);
            float qz = (float) SimplexNoise.noise(x + 9.7, y + 8.1, z + 2.6, t);
            double v = 0.5 + 0.5 * SimplexNoise.noise(x + wAmp * qx, y + wAmp * qy, z + wAmp * qz, t2);
            float lit = clamp01(((float) v - thr) / edge);
            float b = floorB + lit * (55 + 45 * bass) + kickEnv * 25 * (1 - pDist[i]);
            float h = baseHue + (float) v * spread * 2 + qx * 20;
            float s = sat * (1 - 0.5f * lit * kickEnv);
            b = lerp(b, 100 * (1 - lit), clapFlash);
            h = hueLerp(h, accentHue, clapFlash);
            s *= (1 - 0.6f * clapFlash);
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }

    private void renderBurst(double deltaMs, float baseHue, float sat, float spread,
                             float size, float bass) {
        float dtS = (float) deltaMs / 1000f;
        float clapDrag = (float) Math.exp(-deltaMs / 400);
        for (int j = alive - 1; j >= 0; j--) {
            P p = parts[j];
            p.life += (float) deltaMs;
            if (p.life >= p.maxLife) {
                alive--;
                parts[j] = parts[alive];
                parts[alive] = p;
                continue;
            }
            if (p.clap) {
                p.vx *= clapDrag; p.vy *= clapDrag; p.vz *= clapDrag;
            } else {
                p.vy -= 0.9f * dtS;
            }
            p.x += p.vx * dtS; p.y += p.vy * dtS; p.z += p.vz * dtS;
            if (p.y < 0) {
                p.y = 0;
                p.vy = -p.vy * 0.45f;
                p.vx *= 0.8f; p.vz *= 0.8f;
            }
        }
        for (int i = 0; i < n; i++) {
            float px = pXn[i], py = pYn[i], pz = pZn[i];
            float b = floorB + bass * 15 * (1 - py);
            float h = baseHue + py * spread;
            float s = sat;
            float best = 0, bestHue = h;
            for (int j = 0; j < alive; j++) {
                P p = parts[j];
                float dx = px - p.x, dy = py - p.y, dz = pz - p.z;
                float d2 = dx * dx + dy * dy + dz * dz;
                float r = p.size * (1 + 0.5f * kickEnv);
                if (d2 < 9 * r * r) {
                    float g = (float) Math.exp(-d2 / (2 * r * r)) * (1 - p.life / p.maxLife);
                    b += g * 110;
                    if (g > best) { best = g; bestHue = p.hue; }
                    if (p.clap) s = Math.min(s, sat * (1 - g));
                }
            }
            if (best > 0) h = bestHue;
            b = lerp(b, 100, clapFlash * 0.6f);
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }

    private void renderCells(float baseHue, float accentHue, float sat, float spread, float bass) {
        for (int i = 0; i < n; i++) {
            int cell = pCell[i];
            byte c = cells[cell];
            float a = clamp01(cellAge[cell] / 120f);
            float b, h, s;
            if (c == 1) {
                b = floorB + (60 + 40 * kickEnv) * a;
                h = baseHue + hash01(cell) * spread;
                s = sat;
            } else if (c == 2) {
                b = floorB + 35 * a;
                h = baseHue + spread * 0.5f;
                s = sat * 0.8f;
            } else {
                b = floorB + bass * 10;
                h = baseHue;
                s = sat;
            }
            b = lerp(b, 100, clapFlash * 0.7f);
            h = hueLerp(h, accentHue, clapFlash);
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }

    private void renderHarmonics(double deltaMs, float baseHue, float accentHue, float sat,
                                 float spread, float size, float speed, float bass) {
        float density = densityParam.getValuef();
        int K = Math.min(MAX_WAVES, 3 + Math.round(density * 7));
        float dt = (float) deltaMs;
        float ampSum = 0;
        for (int k = 0; k < K; k++) {
            int bandIdx = 1 + (int) (k * 13f / K);
            float amp = audio.bandNorm[bandIdx];
            ampSum += amp;
            phase[k] += dt * (0.0008f + speed * 0.004f) * (0.3f + amp) * (k % 2 == 0 ? 1 : -1);
            if (audio.kick) phase[k] += 0.5f * (float) Math.PI * audio.kickStrength;
            phase[k] %= TWO_PI;
        }
        float norm = Math.max(0.5f, ampSum);
        float contrast = 1 + 2 * kickEnv;
        float fBase = (2 + 6 * (1 - size));
        for (int i = 0; i < n; i++) {
            float px = pXn[i] - 0.5f, py = pYn[i] - 0.5f, pz = pZn[i] - 0.5f;
            float sum = 0;
            for (int k = 0; k < K; k++) {
                int bandIdx = 1 + (int) (k * 13f / K);
                float amp = audio.bandNorm[bandIdx];
                if (amp <= 0.001f) continue;
                float fk = fBase * (1 + 0.5f * k / K);
                float d = px * dirX[k] + py * dirY[k] + pz * dirZ[k];
                sum += amp * (float) Math.sin(fk * d + phase[k]);
            }
            float v = sum / norm;
            float lit = clamp01((v + 1) / 2);
            lit = (float) Math.pow(lit, contrast);
            float b = floorB + lit * (50 + 50 * bass);
            float h = baseHue + v * spread;
            float s = sat;
            b = lerp(b, 100, clapFlash * (0.5f + 0.5f * lit));
            h = hueLerp(h, accentHue, clapFlash);
            s *= (1 - 0.5f * clapFlash);
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }
}
