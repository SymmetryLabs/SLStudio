package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.constrain;

/**
 * Single layered audio-reactive EDM pattern for the cubes show.
 * Low band: expanding shockwave rings + whole-cube bass punches.
 * Mid band: rotating face-group flashes + orbiting face accents.
 * High band: sparse edge comets + deterministic sparkle.
 */
public class CubeRave extends SLPattern<CubesModel> {
    public static final String GROUP_NAME = "Audio";

    private static final int MAX_WAVES = 8;
    private static final float MIN_HZ = 30f;
    private static final float MAX_HZ = 16000f;

    // Own FFT meter; follows the engine input/output mode without enabling hardware.
    private class RaveMeter extends GraphicMeter {
        RaveMeter() {
            super("CubeRave", lx.engine.audio.input.mix);
        }

        @Override
        public void loop(double deltaMs) {
            setBuffer(lx.engine.audio.mode.getEnum() == LXAudioEngine.Mode.OUTPUT
                ? lx.engine.audio.output.mix : lx.engine.audio.input.mix);
            super.loop(deltaMs);
        }
    }

    private final GraphicMeter eq = new RaveMeter();

    // audio conditioning
    public final CompoundParameter gainDb =
        (CompoundParameter) new CompoundParameter("Gain", 12, -24, 36)
            .setDescription("Gain applied to FFT bands before normalization")
            .setUnits(LXParameter.Units.DECIBELS);
    public final CompoundParameter rangeDb =
        (CompoundParameter) new CompoundParameter("Range", 48, 24, 84)
            .setDescription("dB window mapped to 0-1 band levels")
            .setUnits(LXParameter.Units.DECIBELS);
    public final CompoundParameter gate =
        new CompoundParameter("Gate", 0.1, 0, 0.8)
            .setDescription("Normalized floor: levels below this are suppressed and the rest rescaled");
    public final CompoundParameter lowSplit =
        new CompoundParameter("LowSplit", 180, 80, 400)
            .setDescription("Split between low and mid bands (Hz)");
    public final CompoundParameter midSplit =
        new CompoundParameter("MidSplit", 2500, 800, 6000)
            .setDescription("Split between mid and high bands (Hz)");
    public final CompoundParameter lowGain =
        new CompoundParameter("LowGain", 1, 0, 3).setDescription("Low band linear gain");
    public final CompoundParameter midGain =
        new CompoundParameter("MidGain", 1, 0, 3).setDescription("Mid band linear gain");
    public final CompoundParameter highGain =
        new CompoundParameter("HighGain", 1, 0, 3).setDescription("High band linear gain");
    public final CompoundParameter lowAttack =
        (CompoundParameter) new CompoundParameter("LowAtk", 0, 0, 250)
            .setDescription("Low band attack").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter lowDecay =
        (CompoundParameter) new CompoundParameter("LowDec", 260, 20, 1600)
            .setDescription("Low band decay").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter midAttack =
        (CompoundParameter) new CompoundParameter("MidAtk", 5, 0, 250)
            .setDescription("Mid band attack").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter midDecay =
        (CompoundParameter) new CompoundParameter("MidDec", 200, 20, 1600)
            .setDescription("Mid band decay").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter highAttack =
        (CompoundParameter) new CompoundParameter("HighAtk", 0, 0, 250)
            .setDescription("High band attack").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter highDecay =
        (CompoundParameter) new CompoundParameter("HighDec", 140, 20, 1600)
            .setDescription("High band decay").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter sensitivity =
        new CompoundParameter("Sens", 0.5, 0, 1).setDescription("Onset sensitivity");
    public final CompoundParameter minGap =
        (CompoundParameter) new CompoundParameter("MinGap", 120, 70, 350)
            .setDescription("Minimum time between onsets").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter levelLow =
        new CompoundParameter("LvlLow", 1, 0, 1).setDescription("Low band render mix");
    public final CompoundParameter levelMid =
        new CompoundParameter("LvlMid", 1, 0, 1).setDescription("Mid band render mix");
    public final CompoundParameter levelHigh =
        new CompoundParameter("LvlHigh", 1, 0, 1).setDescription("High band render mix");

    // visuals
    public final CompoundParameter waveSpeed =
        new CompoundParameter("WaveSpeed", 1.5, 0.2, 5)
            .setDescription("Shockwave expansion speed (model radii/sec)");
    public final CompoundParameter waveWidth =
        new CompoundParameter("WaveWidth", 0.13, 0.04, 0.5).setDescription("Shockwave ring width");
    public final CompoundParameter motion =
        new CompoundParameter("Motion", 1, 0.1, 4).setDescription("Orbit and comet speed");
    public final CompoundParameter density =
        new CompoundParameter("Density", 0.45, 0.1, 1).setDescription("Accent and sparkle density");
    public final CompoundParameter punch =
        new CompoundParameter("Punch", 0.6, 0, 1).setDescription("Whole-cube bass punch amount");
    public final CompoundParameter trails =
        (CompoundParameter) new CompoundParameter("Trails", 100, 0, 700)
            .setDescription("Afterimage decay").setUnits(LXParameter.Units.MILLISECONDS);
    public final CompoundParameter brightness =
        new CompoundParameter("Bright", 0.85, 0, 1).setDescription("Master brightness");
    public final CompoundParameter backdrop =
        new CompoundParameter("Backdrop", 0.08, 0, 0.4).setDescription("Dim audio-driven background");
    public final CompoundParameter hue =
        new CompoundParameter("Hue", 0, -180, 180).setDescription("Palette hue offset");
    public final CompoundParameter spread =
        new CompoundParameter("Spread", 120, 0, 240).setDescription("Band hue separation");
    public final CompoundParameter saturation =
        new CompoundParameter("Sat", 85, 0, 100).setDescription("Saturation");
    public final CompoundParameter white =
        new CompoundParameter("White", 0.5, 0, 1).setDescription("Whitens transient peaks");

    /** Per-band envelope + adaptive onset detector. */
    static class BandState {
        float level, baseline, prevTarget, sinceOnset = 1e9f;
        boolean armed = true;
        int onsets;

        boolean update(double deltaMs, float target, float attackMs, float decayMs,
                       float sens, float gapMs) {
            target = constrain(target, 0, 1);
            float a;
            if (target > level) {
                a = attackMs <= 0 ? 1f : 1f - (float) Math.exp(-deltaMs / Math.max(1e-3f, attackMs));
            } else {
                a = 1f - (float) Math.exp(-deltaMs / Math.max(1f, decayMs));
            }
            level = constrain(level + (target - level) * a, 0, 1);
            float btau = target > baseline ? 350f : 2600f;
            baseline += (target - baseline) * (1f - (float) Math.exp(-deltaMs / btau));
            sinceOnset += (float) deltaMs;

            float thr = baseline * (1f + (1.1f - sens)) + 0.015f + 0.06f * (1f - sens);
            if (!armed && target < thr * 0.7f) armed = true;
            boolean onset = armed && target >= thr && target > prevTarget + 1e-4f
                && sinceOnset >= gapMs;
            if (onset) {
                armed = false;
                sinceOnset = 0;
                onsets++;
            }
            prevTarget = target;
            return onset;
        }
    }

    /** Triggerable envelope: linear attack then exponential decay to ~5% over decayMs. */
    static class Pulse {
        float v;
        private float t = -1; // ms since trigger, -1 = idle

        void trigger() { t = 0; v = 0; }

        boolean isActive() { return t >= 0; }

        float update(double deltaMs, float attackMs, float decayMs) {
            if (t < 0) return 0;
            t += (float) deltaMs;
            if (attackMs > 0 && t < attackMs) {
                v = t / attackMs;
            } else {
                float de = attackMs > 0 ? t - attackMs : t;
                v = (float) Math.exp(-3f * de / Math.max(1f, decayMs));
                if (v < 0.004f) { v = 0; t = -1; }
            }
            return v;
        }
    }

    private static class Wave {
        float cx, cy, cz, r;       // r in normalized model radii
        final Pulse env = new Pulse();
        boolean active;
    }

    // geometry cache, indexed by contiguous point ordinal
    private CubesModel cachedModel;
    private int n;
    private int numCubes;
    private int[] ptIndex, pCube, pFace, pStripOrd;
    private float[] px, py, pz, pLyN, pStripT;
    private float[] cubeX, cubeY, cubeZ, cubeLevelN;
    private float modelExtent = 1;
    private Map<Integer, Integer> ordOf = new HashMap<>();

    // dynamic render state
    private final Wave[] waves = new Wave[MAX_WAVES];
    private float[] faceFlash;      // numCubes*4
    private int[] stripSel;         // per-cube selected strip ordinal 0..11
    private float[] cometPos;
    private final BandState lowBand = new BandState();
    private final BandState midBand = new BandState();
    private final BandState highBand = new BandState();
    private final Pulse bassPulse = new Pulse();
    private final Pulse midPulse = new Pulse();
    private final Pulse highPulse = new Pulse();
    private float[] accR, accG, accB, pPeak;
    private float[] trR, trG, trB;
    private float orbitPhase;
    private double nowMs;

    public CubeRave(LX lx) {
        super(lx);
        for (int i = 0; i < MAX_WAVES; i++) waves[i] = new Wave();
        addParameter(gainDb); addParameter(rangeDb); addParameter(gate);
        addParameter(lowSplit); addParameter(midSplit);
        addParameter(lowGain); addParameter(midGain); addParameter(highGain);
        addParameter(lowAttack); addParameter(lowDecay);
        addParameter(midAttack); addParameter(midDecay);
        addParameter(highAttack); addParameter(highDecay);
        addParameter(sensitivity); addParameter(minGap);
        addParameter(levelLow); addParameter(levelMid); addParameter(levelHigh);
        addParameter(waveSpeed); addParameter(waveWidth); addParameter(motion);
        addParameter(density); addParameter(punch); addParameter(trails);
        addParameter(brightness); addParameter(backdrop); addParameter(hue);
        addParameter(spread); addParameter(saturation); addParameter(white);
        addModulator(eq).start();
    }

    private void ensureGeometry() {
        if (model == cachedModel) return;
        cachedModel = model;
        n = model.points.length;
        List<CubesModel.Cube> cubes = model.getCubes();
        numCubes = cubes.size();
        ptIndex = new int[n]; pCube = new int[n]; pFace = new int[n]; pStripOrd = new int[n];
        px = new float[n]; py = new float[n]; pz = new float[n];
        pLyN = new float[n]; pStripT = new float[n];
        accR = new float[n]; accG = new float[n]; accB = new float[n]; pPeak = new float[n];
        trR = new float[n]; trG = new float[n]; trB = new float[n];
        cubeX = new float[numCubes]; cubeY = new float[numCubes]; cubeZ = new float[numCubes];
        cubeLevelN = new float[numCubes];
        faceFlash = new float[numCubes * 4];
        stripSel = new int[numCubes];
        cometPos = new float[numCubes];
        for (int i = 0; i < MAX_WAVES; i++) waves[i].active = false;
        if (n == 0) return;

        modelExtent = 0.5f * (float) Math.sqrt(model.xRange * model.xRange
            + model.yRange * model.yRange + model.zRange * model.zRange);
        if (modelExtent <= 0) modelExtent = 1;

        ordOf = new HashMap<>(n * 2);
        int o = 0;
        for (LXPoint p : model.points) {
            ordOf.put(p.index, o);
            ptIndex[o] = p.index; px[o] = p.x; py[o] = p.y; pz[o] = p.z;
            o++;
        }

        for (int ci = 0; ci < numCubes; ci++) {
            CubesModel.Cube cube = cubes.get(ci);
            float cx = 0, cy = 0, cz = 0;
            for (LXPoint p : cube.points) { cx += p.x; cy += p.y; cz += p.z; }
            int np = Math.max(1, cube.points.length);
            cubeX[ci] = cx / np; cubeY[ci] = cy / np; cubeZ[ci] = cz / np;
            cubeLevelN[ci] = model.yRange > 0
                ? constrain((cubeY[ci] - model.yMin) / model.yRange, 0, 1) : 0.5f;
            stripSel[ci] = (int) (hash01(ci * 31 + 7, 0) * 12);
            cometPos[ci] = hash01(ci * 97 + 13, 0);
        }

        for (int ci = 0; ci < numCubes; ci++) {
            CubesModel.Cube cube = cubes.get(ci);
            float yMin = Float.MAX_VALUE, yMax = -Float.MAX_VALUE;
            for (LXPoint p : cube.points) { yMin = Math.min(yMin, p.y); yMax = Math.max(yMax, p.y); }
            float yRange = Math.max(1e-3f, yMax - yMin);
            List<CubesModel.Face> faces = cube.getFaces();
            for (int fi = 0; fi < faces.size(); fi++) {
                List<CubesModel.CubesStrip> strips = faces.get(fi).getStrips();
                for (int si = 0; si < strips.size(); si++) {
                    LXPoint[] pts = strips.get(si).points;
                    for (int k = 0; k < pts.length; k++) {
                        int i = ordinalOf(pts[k].index);
                        if (i < 0) continue;
                        pCube[i] = ci; pFace[i] = fi; pStripOrd[i] = fi * 3 + si;
                        pStripT[i] = pts.length <= 1 ? 0 : (float) k / (pts.length - 1);
                        pLyN[i] = (pts[k].y - yMin) / yRange;
                    }
                }
            }
        }
    }

    private int ordinalOf(int index) {
        Integer i = ordOf.get(index);
        return i == null ? -1 : i;
    }

    private static float hash01(int a, int b) {
        int h = a * 0x9E3779B1 ^ b * 0x85EBCA6B;
        h ^= h >>> 13; h *= 0xC2B2AE35; h ^= h >>> 16;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }

    /** Normalize a raw FFT amplitude into 0-1 via own gain/range (pre-gate). */
    private float bandLevel(float amp, CompoundParameter bandGain) {
        float ref = Math.max(1f, eq.fft.getSize() * 0.25f);
        double db = 20 * Math.log10(Math.max(amp * bandGain.getValuef() / ref, 1e-8))
            + gainDb.getValuef();
        return constrain((float) (db + rangeDb.getValuef()) / rangeDb.getValuef(), 0, 1);
    }

    private float applyGate(float v) {
        float g = gate.getValuef();
        return v <= g ? 0 : Math.min(1f, (v - g) / Math.max(1e-3f, 1f - g));
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space space) {
        ensureGeometry();
        float lo = bandLevel(eq.fft.getAverage(MIN_HZ, lowSplit.getValuef()), lowGain);
        float mid = bandLevel(eq.fft.getAverage(lowSplit.getValuef(), midSplit.getValuef()), midGain);
        float hi = bandLevel(eq.fft.getAverage(midSplit.getValuef(), MAX_HZ), highGain);
        renderFrame(deltaMs, lo, mid, hi);
        markModified(SRGB8);
    }

    /** Testable seam: raw normalized pre-gate band levels -> envelopes + render. */
    void renderFrame(double deltaMs, float lowRaw, float midRaw, float highRaw) {
        int[] colors = (int[]) getArray(SRGB8);
        ensureGeometry();
        nowMs += deltaMs;
        if (n == 0) return;

        float sens = sensitivity.getValuef();
        float gap = minGap.getValuef();
        boolean lowOn = lowBand.update(deltaMs, applyGate(lowRaw),
            lowAttack.getValuef(), lowDecay.getValuef(), sens, gap);
        boolean midOn = midBand.update(deltaMs, applyGate(midRaw),
            midAttack.getValuef(), midDecay.getValuef(), sens, gap);
        boolean highOn = highBand.update(deltaMs, applyGate(highRaw),
            highAttack.getValuef(), highDecay.getValuef(), sens, gap);

        if (lowOn) {
            bassPulse.trigger();
            if (numCubes > 0) {
                Wave w = waves[lowBand.onsets % MAX_WAVES];
                w.active = true;
                int ci = lowBand.onsets % numCubes;
                w.cx = cubeX[ci]; w.cy = cubeY[ci]; w.cz = cubeZ[ci]; w.r = 0;
                w.env.trigger();
            }
        }
        if (midOn) {
            midPulse.trigger();
            for (int ci = 0; ci < numCubes; ci++) {
                faceFlash[ci * 4 + ((ci + midBand.onsets) & 3)] = 1;
            }
        }
        if (highOn) {
            highPulse.trigger();
            for (int ci = 0; ci < numCubes; ci++) {
                stripSel[ci] = (int) (hash01(ci * 31 + 7, highBand.onsets * 17) * 12);
            }
        }

        float ws = waveSpeed.getValuef();
        float loAtk = lowAttack.getValuef(), midAtk = midAttack.getValuef();
        for (Wave w : waves) {
            if (!w.active) continue;
            w.r += (float) (deltaMs * 0.001f * ws);
            w.env.update(deltaMs, loAtk, lowDecay.getValuef() * 1.5f + 200);
            if (!w.env.isActive() || w.r > 2.5f) w.active = false;
        }
        bassPulse.update(deltaMs, loAtk, Math.max(120f, lowDecay.getValuef()));
        midPulse.update(deltaMs, midAtk, Math.max(120f, midDecay.getValuef()));
        highPulse.update(deltaMs, highAttack.getValuef(), Math.max(80f, highDecay.getValuef()));
        float ffDecay = (float) Math.exp(-deltaMs / 240);
        for (int i = 0; i < faceFlash.length; i++) faceFlash[i] *= ffDecay;
        float mo = motion.getValuef();
        orbitPhase += (float) (deltaMs * 0.0009f * mo);
        for (int ci = 0; ci < numCubes; ci++) {
            cometPos[ci] += (float) (deltaMs * 0.0014f * mo * (0.6f + hash01(ci, 3) * 0.8f));
            if (cometPos[ci] > 1.3f) cometPos[ci] -= 1.3f;
        }

        render(colors, deltaMs);
    }

    private static void hsbToRgb(float h, float s, float v, float[] out) {
        float c = v * s;
        float hp = (((h % 360) + 360) % 360) / 60f;
        float x = c * (1f - Math.abs(hp % 2f - 1f));
        float r = 0, g = 0, b = 0;
        if (hp < 1) { r = c; g = x; } else if (hp < 2) { r = x; g = c; }
        else if (hp < 3) { g = c; b = x; } else if (hp < 4) { g = x; b = c; }
        else if (hp < 5) { r = x; b = c; } else { r = c; b = x; }
        float m = v - c;
        out[0] = r + m; out[1] = g + m; out[2] = b + m;
    }

    private final float[] lu = new float[3], mu = new float[3],
        hu = new float[3], bu = new float[3];

    private void render(int[] colors, double deltaMs) {
        float wAmt = white.getValuef();
        float sat = saturation.getValuef() / 100f;
        float sp = spread.getValuef();
        float hue0 = palette.getHuef() + hue.getValuef();
        float mixL = levelLow.getValuef(), mixM = levelMid.getValuef(), mixH = levelHigh.getValuef();
        float pk = punch.getValuef(), den = density.getValuef();
        float width = waveWidth.getValuef();
        float lowE = lowBand.level, midE = midBand.level, highE = highBand.level;
        float accentB = midE * den * 0.7f * mixM;
        float sparkleB = highE * den * 0.8f * mixH;
        int bucket = (int) (nowMs / 90);
        hsbToRgb(hue0, sat, 1, lu);
        hsbToRgb(hue0 + sp * 0.5f, sat, 1, mu);
        hsbToRgb(hue0 + sp, Math.min(1f, sat * 0.8f), 1, hu);
        hsbToRgb(hue0, sat * 0.5f, 1, bu);
        float bg = backdrop.getValuef()
            * Math.min(1f, lowE * mixL + midE * mixM + highE * mixH);
        float bp = bassPulse.v * pk * mixL;
        float mp = midPulse.v * mixM, hp = highPulse.v * mixH;
        int parity = lowBand.onsets & 1;

        Arrays.fill(accR, 0); Arrays.fill(accG, 0); Arrays.fill(accB, 0); Arrays.fill(pPeak, 0);

        for (int o = 0; o < n; o++) {
            int ci = pCube[o];
            float peak = 0;
            if (bg > 0) {
                accR[o] += bg * bu[0]; accG[o] += bg * bu[1]; accB[o] += bg * bu[2];
            }
            // low: shockwave rings
            if (mixL > 0) {
                for (Wave w : waves) {
                    if (!w.active) continue;
                    float dx = px[o] - w.cx, dy = py[o] - w.cy, dz = pz[o] - w.cz;
                    float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / modelExtent - w.r;
                    float ring = (float) Math.exp(-(d * d) / (width * width));
                    float v = ring * w.env.v * mixL;
                    if (v > 0.003f) {
                        accR[o] += v * lu[0]; accG[o] += v * lu[1]; accB[o] += v * lu[2];
                        if (v > peak) peak = v;
                    }
                }
                // bass punch, biased to lower cubes, alternating cube groups
                if (bp > 0 && ci >= 0) {
                    float gb = ((ci & 1) == parity) ? 1f : 0.35f;
                    float v = bp * (1f - 0.65f * cubeLevelN[ci]) * (0.6f + 0.4f * pLyN[o]) * gb;
                    if (v > 0.003f) {
                        accR[o] += v * lu[0]; accG[o] += v * lu[1]; accB[o] += v * lu[2];
                        if (v > peak) peak = v;
                    }
                }
            }
            // mid: face flashes + orbiting accent
            if (mixM > 0 && ci >= 0) {
                float v = faceFlash[ci * 4 + pFace[o]] * mp
                    + (pFace[o] == ((int) (orbitPhase + ci * 0.33f) & 3) ? accentB : 0);
                if (v > 0.003f) {
                    accR[o] += v * mu[0]; accG[o] += v * mu[1]; accB[o] += v * mu[2];
                    if (v > peak) peak = v;
                }
            }
            // high: comets along selected strip + deterministic sparkle
            if (mixH > 0 && ci >= 0 && pStripOrd[o] == stripSel[ci]) {
                float rel = pStripT[o] - cometPos[ci];
                float comet = rel <= 0 ? (float) Math.exp(rel / 0.18f)
                    : (float) Math.exp(-rel / 0.04f);
                float v = (comet * Math.min(1f, 0.3f * highE + hp)
                    + (hash01(o, bucket) < sparkleB ? 0.5f * hp : 0)) * mixH;
                if (v > 0.003f) {
                    accR[o] += v * hu[0]; accG[o] += v * hu[1]; accB[o] += v * hu[2];
                    if (v > peak) peak = v;
                }
            }
            pPeak[o] = peak;
        }

        // whitening of transient peaks, still pre-master
        if (wAmt > 0) {
            for (int o = 0; o < n; o++) {
                float w = wAmt * Math.max(0f, pPeak[o] - 0.5f) / 0.5f;
                if (w > 0) {
                    w = Math.min(1f, w);
                    accR[o] += (1f - accR[o]) * w;
                    accG[o] += (1f - accG[o]) * w;
                    accB[o] += (1f - accB[o]) * w;
                }
            }
        }

        // trails: exponential decay of stored RGB channel buffer, max with new frame
        float tr = trails.getValuef();
        float k = tr <= 0 ? 0 : (float) Math.exp(-deltaMs * 4.6 / Math.max(1f, tr));
        float bri = brightness.getValuef();
        for (int o = 0; o < n; o++) {
            float r = Math.max(accR[o], trR[o] * k);
            float g = Math.max(accG[o], trG[o] * k);
            float b = Math.max(accB[o], trB[o] * k);
            if (r < 0.004f) r = 0; if (g < 0.004f) g = 0; if (b < 0.004f) b = 0;
            trR[o] = r; trG[o] = g; trB[o] = b;
            int idx = ptIndex[o];
            if (idx >= 0 && idx < colors.length) {
                colors[idx] = LXColor.rgb(
                    (int) (Math.min(1f, r * bri) * 255f + 0.5f),
                    (int) (Math.min(1f, g * bri) * 255f + 0.5f),
                    (int) (Math.min(1f, b * bri) * 255f + 0.5f));
            }
        }
    }
}
