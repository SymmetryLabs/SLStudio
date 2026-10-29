package com.symmetrylabs.slstudio.pattern.cubes;

import heronarts.lx.audio.GraphicMeter;

/**
 * Music analysis for CubeBeats: adaptive-gain band envelopes, kick/snare/hat
 * onset detection, and kick-IOI tempo tracking. Pure Java; the only LX touch
 * point is configureMeter().
 */
public final class CubeBeatsAudio {
    public static final int NUM_BANDS = 16;
    public static final double METER_ATTACK_MS = 1, METER_RELEASE_MS = 25, METER_SLOPE_DB = 4.5;

    /** Configure a GraphicMeter for fast transient response (values in ms/dB). */
    public static void configureMeter(GraphicMeter eq) {
        eq.attack.setValue(METER_ATTACK_MS);
        eq.release.setValue(METER_RELEASE_MS);
        eq.slope.setValue(METER_SLOPE_DB);
    }

    // inputs (set by caller before update)
    public float sensitivity = 0.5f;
    public float attackMs = 34f, releaseMs = 236f;

    // continuous outputs, 0..1
    public float presence;
    public float bass, mid, high, level;
    public float intensity;
    public final float[] bandNorm = new float[NUM_BANDS];

    // discrete onsets
    public boolean kick, snare, hat;
    public float kickStrength, snareStrength, hatStrength;
    public int kickCount, snareCount, hatCount;
    public double msSinceKick = 1e9, msSinceSnare = 1e9, msSinceHat = 1e9;
    public float kickEnv, snareEnv, hatEnv;

    // tempo
    public float periodMs = 500f;
    public float tempoConfidence;
    public float beatPhase;
    public double timeMs;

    // internal state
    private final Agc bassAgc = new Agc(0.04f, 0.01f, 0.15f);
    private final Agc midAgc = new Agc(0.04f, 0.01f, 0.15f);
    private final Agc highAgc = new Agc(0.04f, 0.01f, 0.15f);
    private final Agc levelAgc = new Agc(0.04f, 0.01f, 0.15f);
    private final Agc[] bandAgc = new Agc[NUM_BANDS];
    private final float[] bandEnv = new float[NUM_BANDS];
    private final Agc longAgc = new Agc(0.008f, 0.004f, 0.2f);

    private float bassSt, midSt, highSt, levelSt;

    private static final float PEAK_FRAC = 0.40f;
    private static final float KICK_BAND3_W = 0.0f;

    private final Onset kickDet = new Onset(5, 60, 250, 230, 0.03f, 220);
    private final Onset snareDet = new Onset(3, 50, 200, 160, 0.03f, 160);
    private final Onset hatDet = new Onset(3, 30, 120, 90, 0.025f, 90);

    private final float[] ioiRing = new float[8];
    private int ioiCount;
    private double lastKickTime = Double.NaN;

    private static float follow(float cur, float target, double dt, float tauUp, float tauDown) {
        float tau = target > cur ? tauUp : tauDown;
        float alpha = 1f - (float) Math.exp(-dt / Math.max(1f, tau));
        return cur + (target - cur) * alpha;
    }

    private static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static float smoothstep(float e0, float e1, float x) {
        float t = clamp((x - e0) / (e1 - e0), 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static float mean(float[] b, int lo, int hi) {
        float s = 0;
        for (int i = lo; i <= hi; i++) s += b[i];
        return s / (hi - lo + 1);
    }

    private static class Agc {
        final float peakDecay, floorRise, minSpan;
        float peak, floor;
        Agc(float peakDecay, float floorRise, float minSpan) {
            this.peakDecay = peakDecay; this.floorRise = floorRise; this.minSpan = minSpan;
        }
        float norm(float x, double dt) {
            if (x > peak) peak = x; else peak -= (float) (dt / 1000.0) * peakDecay;
            if (x < floor) floor += (x - floor) * (1f - (float) Math.exp(-dt / 200.0));
            else floor += (float) (dt / 1000.0) * floorRise;
            if (floor > peak) floor = peak;
            return clamp((x - floor) / Math.max(peak - floor, minSpan), 0, 1);
        }
    }

    private static class Onset {
        final float fastUp, fastDown, slowTau, cooldownMs, minOdf, envTau;
        float fast, slow, odfMean, odfDev, msSince = (float) 1e9, env, peakOdf;
        float[] fastB, slowB;
        boolean armed = true;
        int count;
        Onset(float fastUp, float fastDown, float slowTau, float cooldownMs, float minOdf, float envTau) {
            this.fastUp = fastUp; this.fastDown = fastDown; this.slowTau = slowTau;
            this.cooldownMs = cooldownMs; this.minOdf = minOdf; this.envTau = envTau;
        }
        void initBands(int nb) {
            fastB = new float[nb]; slowB = new float[nb];
        }
        private float finish(float odf, double dt, float sensitivity, float presence) {
            float k = 3.0f + (1.0f - 3.0f) * sensitivity;
            float thr = Math.max(odfMean + k * odfDev + minOdf, PEAK_FRAC * peakOdf);
            if (odf < thr) {
                odfMean = follow(odfMean, odf, dt, 1500, 1500);
                odfDev = follow(odfDev, Math.abs(odf - odfMean), dt, 1500, 1500);
            }
            peakOdf *= (float) Math.exp(-dt / 2000.0);
            if (!armed && odf < 0.5f * thr) armed = true;
            if (armed && odf > thr && msSince >= cooldownMs && presence > 0.25f) {
                armed = false;
                msSince = 0;
                count++;
                env = 1;
                peakOdf = Math.max(peakOdf, odf);
                return clamp(0.35f + 0.65f * (odf - thr) / Math.max(thr, 0.02f), 0, 1);
            }
            return -1;
        }
        /** Returns strength 0..1 on fire, else -1. */
        float update(float raw, double dt, float sensitivity, float presence) {
            msSince += dt;
            env *= (float) Math.exp(-dt / envTau);
            fast = follow(fast, raw, dt, fastUp, fastDown);
            slow = follow(slow, raw, dt, slowTau, slowTau);
            return finish(Math.max(0, fast - slow), dt, sensitivity, presence);
        }
        /** Kick variant: per-band spectral flux over band indices {1,2,3} of b. */
        float updateBands(float[] b, double dt, float sensitivity, float presence) {
            msSince += dt;
            env *= (float) Math.exp(-dt / envTau);
            float odf = 0;
            for (int j = 0; j < fastB.length; j++) {
                int bi = j + 1;
                float w = j < 2 ? 1f : KICK_BAND3_W;
                fastB[j] = follow(fastB[j], b[bi], dt, fastUp, fastDown);
                slowB[j] = follow(slowB[j], b[bi], dt, slowTau, slowTau);
                odf += w * Math.max(0, fastB[j] - slowB[j]);
            }
            return finish(odf, dt, sensitivity, presence);
        }
    }

    private final float[] bandScratch = new float[NUM_BANDS];
    private float presenceHold;

    {
        for (int i = 0; i < NUM_BANDS; i++) bandAgc[i] = new Agc(0.04f, 0.01f, 0.15f);
        kickDet.initBands(3);
    }

    public void update(float[] bands, double deltaMs) {
        if (!(deltaMs > 0) || Double.isNaN(deltaMs)) deltaMs = 0.001;
        double dt = deltaMs;
        timeMs += dt;
        kick = snare = hat = false;

        float[] b = bandScratch;
        for (int i = 0; i < NUM_BANDS; i++) {
            float v = i < bands.length ? bands[i] : 0;
            b[i] = Float.isNaN(v) ? 0 : v;
        }

        float bassRaw = mean(b, 1, 3);
        float midRaw = mean(b, 4, 8);
        float snareRaw = mean(b, 9, 11);
        float highRaw = mean(b, 12, 14);
        float levelRaw = mean(b, 1, 14);

        // presence gate with peak hold
        float pRaw = Math.max(bassRaw, Math.max(midRaw, highRaw));
        if (pRaw > presenceHold) presenceHold = pRaw;
        else presenceHold -= (float) (dt / 1000.0) * 0.05f;
        presence = follow(presence, smoothstep(0.02f, 0.08f, presenceHold), dt, 300, 1500);

        // continuous envelopes on AGC-normalized groups
        bassSt = follow(bassSt, bassAgc.norm(bassRaw, dt), dt, attackMs, releaseMs);
        midSt = follow(midSt, midAgc.norm(midRaw, dt), dt, attackMs * 2, releaseMs * 1.5f);
        highSt = follow(highSt, highAgc.norm(highRaw, dt), dt, attackMs * 0.5f, releaseMs * 0.5f);
        levelSt = follow(levelSt, levelAgc.norm(levelRaw, dt), dt, attackMs, releaseMs);
        bass = bassSt * presence;
        mid = midSt * presence;
        high = highSt * presence;
        level = levelSt * presence;

        for (int i = 1; i <= 14; i++) {
            float nrm = bandAgc[i].norm(b[i], dt);
            float tauDown = 350 + (120 - 350) * ((i - 1) / 13f);
            bandEnv[i] = follow(bandEnv[i], nrm, dt, 15, tauDown);
            bandNorm[i] = bandEnv[i] * presence;
        }
        bandNorm[0] = bandNorm[15] = 0;

        // long-term section energy
        intensity = follow(intensity, longAgc.norm(levelRaw, dt) * presence, dt, 3000, 6000);

        // onsets
        float ks = kickDet.updateBands(b, dt, sensitivity, presence);
        if (ks >= 0) {
            kick = true; kickStrength = ks; kickEnv = 1;
            kickCount = kickDet.count; msSinceKick = 0;
            onKickTempo();
        }
        float ss = snareDet.update(snareRaw, dt, sensitivity, presence);
        if (ss >= 0) {
            snare = true; snareStrength = ss; snareEnv = 1;
            snareCount = snareDet.count; msSinceSnare = 0;
        }
        float hs = hatDet.update(highRaw, dt, sensitivity, presence);
        if (snare) hs = -1; // snare suppresses a simultaneous hat fire
        if (hs >= 0) {
            hat = true; hatStrength = hs; hatEnv = 1;
            hatCount = hatDet.count; msSinceHat = 0;
        }
        msSinceKick = kickDet.msSince;
        msSinceSnare = snareDet.msSince;
        msSinceHat = hatDet.msSince;
        kickEnv = kickDet.env;
        snareEnv = snareDet.env;
        hatEnv = hatDet.env;

        // tempo
        if (msSinceKick > 4.0 * periodMs) {
            tempoConfidence *= (float) Math.exp(-dt / 2000.0);
        }
        beatPhase += (float) (dt / periodMs);
        beatPhase -= Math.floor(beatPhase);
        if (kick) {
            float err = beatPhase >= 0.5f ? beatPhase - 1 : beatPhase;
            if (tempoConfidence > 0.5f) {
                beatPhase -= 0.5f * err;
                beatPhase -= Math.floor(beatPhase);
            } else {
                beatPhase = 0;
            }
        }
    }

    private void onKickTempo() {
        if (!Double.isNaN(lastKickTime)) {
            double ioi = timeMs - lastKickTime;
            if (ioi >= 250 && ioi <= 2000) {
                float f = (float) ioi;
                while (f > 1000) f /= 2;
                while (f < 300) f *= 2;
                ioiRing[ioiCount % ioiRing.length] = f;
                ioiCount++;
                int n = Math.min(ioiCount, ioiRing.length);
                if (n >= 4) {
                    float[] sorted = new float[n];
                    System.arraycopy(ioiRing, 0, sorted, 0, n);
                    java.util.Arrays.sort(sorted);
                    float m = n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2f;
                    int inliers = 0;
                    for (int i = 0; i < n; i++) {
                        if (Math.abs(ioiRing[i] - m) < 0.08f * m) inliers++;
                    }
                    float conf = (inliers / (float) n) * Math.min(1, n / 6f);
                    if (conf > 0.4f) periodMs += (m - periodMs) * 0.3f;
                    tempoConfidence = conf;
                }
            }
        }
        lastKickTime = timeMs;
    }
}
