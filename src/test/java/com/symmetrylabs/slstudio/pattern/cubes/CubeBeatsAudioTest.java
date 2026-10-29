package com.symmetrylabs.slstudio.pattern.cubes;

import heronarts.lx.audio.SyntheticMeterDriver;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Evaluates CubeBeatsAudio on synthetic music rendered through the real GraphicMeter.
 * Ground truth = the synthesized event times. These signals are simplified stand-ins
 * for real music; passing here is necessary, not sufficient.
 */
public class CubeBeatsAudioTest {
    private static final int SR = SyntheticMeterDriver.SAMPLE_RATE;
    private static final int N = SyntheticMeterDriver.BUFFER_SIZE;
    private static final double FRAME_MS = 1000.0 / 60;
    /* CubeBeats defaults: Gain normalized 0.5 -> 0 dB; Range normalized 0.4 -> 6 + 0.4 * 90 = 42 dB */
    private static final double GAIN_DB = 0, RANGE_DB = 42;
    private static final float SENS = 0.5f;
    private static final double MATCH_EARLY_S = 0.010, MATCH_LATE_S = 0.090;

    // ---------------------------------------------------------------- synthesis

    private static float white(long n) {
        long x = n * 0x9E3779B97F4A7C15L;
        x ^= x >>> 33; x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33; x *= 0xc4ceb9fe1a85ec53L;
        x ^= x >>> 33;
        return ((x >>> 40) / (float) (1L << 24)) * 2f - 1f;
    }

    private static class Track {
        final double durationS;
        final List<double[]> kicks = new ArrayList<>();  // {t, amp}
        final List<double[]> snares = new ArrayList<>(); // {t, amp}
        final List<double[]> hats = new ArrayList<>();   // {t, amp}
        final List<double[]> bass = new ArrayList<>();   // {t, freq, amp} sustained until next note
        double padAmp = 0, pinkAmp = 0, floorAmp = 0.0005;
        boolean clip = false;
        DoubleUnaryOperator master = t -> 1;

        Track(double durationS) { this.durationS = durationS; }

        void sort() {
            for (List<double[]> l : Arrays.asList(kicks, snares, hats, bass)) l.sort((a, b) -> Double.compare(a[0], b[0]));
        }

        List<Double> kickTimes() {
            List<Double> r = new ArrayList<>();
            for (double[] e : kicks) r.add(e[0]);
            return r;
        }

        List<Double> times(List<double[]> l) {
            List<Double> r = new ArrayList<>();
            for (double[] e : l) r.add(e[0]);
            return r;
        }

        private static int firstAfter(List<double[]> l, double t) {
            int lo = 0, hi = l.size();
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (l.get(mid)[0] <= t) lo = mid + 1; else hi = mid;
            }
            return lo;
        }

        void render(long n0, float[] out) {
            double tStart = n0 / (double) SR, tEnd = (n0 + out.length) / (double) SR;
            int k0 = firstAfter(kicks, tStart - 0.5), k1 = firstAfter(kicks, tEnd);
            int s0 = firstAfter(snares, tStart - 0.3), s1 = firstAfter(snares, tEnd);
            int h0 = firstAfter(hats, tStart - 0.08), h1 = firstAfter(hats, tEnd);
            int b0 = Math.max(0, firstAfter(bass, tStart) - 2), b1 = firstAfter(bass, tEnd);
            for (int i = 0; i < out.length; i++) {
                long n = n0 + i;
                double t = n / (double) SR;
                double v = 0;
                for (int k = k0; k < k1; k++) {
                    double tau = t - kicks.get(k)[0];
                    if (tau < 0 || tau >= 0.5) continue;
                    v += kicks.get(k)[1] * Math.exp(-tau / 0.18)
                        * Math.sin(2 * Math.PI * (50 * tau + 90 * 0.03 * (1 - Math.exp(-tau / 0.03))));
                }
                for (int k = s0; k < s1; k++) {
                    double tau = t - snares.get(k)[0];
                    if (tau < 0 || tau >= 0.3) continue;
                    v += snares.get(k)[1] * (Math.exp(-tau / 0.09) * 0.5 * (white(n) - white(n - 1))
                        + 0.5 * Math.exp(-tau / 0.06) * Math.sin(2 * Math.PI * 180 * tau));
                }
                for (int k = h0; k < h1; k++) {
                    double tau = t - hats.get(k)[0];
                    if (tau < 0 || tau >= 0.08) continue;
                    v += hats.get(k)[1] * Math.exp(-tau / 0.025)
                        * 0.35 * (white(n + 7777) - 2 * white(n + 7776) + white(n + 7775));
                }
                for (int k = b0; k < b1; k++) {
                    double[] note = bass.get(k);
                    double tau = t - note[0];
                    if (tau < 0) continue;
                    double env = 1 - Math.exp(-tau / 0.015);
                    if (k + 1 < bass.size() && t >= bass.get(k + 1)[0]) {
                        double after = t - bass.get(k + 1)[0];
                        if (after > 0.06) continue;
                        env *= Math.exp(-after / 0.01);
                    }
                    v += note[2] * env * Math.sin(2 * Math.PI * note[1] * tau);
                }
                if (padAmp > 0) {
                    v += padAmp * (Math.sin(2 * Math.PI * 220 * t) + Math.sin(2 * Math.PI * 277.18 * t)
                        + Math.sin(2 * Math.PI * 329.63 * t)) / 3 * (0.7 + 0.3 * Math.sin(2 * Math.PI * 0.1 * t));
                }
                if (pinkAmp > 0) {
                    double lp = 0;
                    for (int j = 0; j < 8; j++) lp += white(n - j + 99991);
                    v += pinkAmp * (lp / 8 * 2 + 0.3 * white(n + 4242));
                }
                v *= master.applyAsDouble(t);
                if (clip) v = Math.tanh(v);
                out[i] = (float) (v + floorAmp * white(n + 12345));
            }
        }
    }

    private static List<Double> grid(double start, double end, double bpm, double offsetBeats, double everyBeats) {
        List<Double> r = new ArrayList<>();
        double beat = 60.0 / bpm;
        for (int k = 0; ; k++) {
            double t = start + (offsetBeats + k * everyBeats) * beat;
            if (t >= end) break;
            if (t >= 0) r.add(t);
        }
        return r;
    }

    private static void add(List<double[]> l, List<Double> ts, double amp) {
        for (double t : ts) l.add(new double[] {t, amp});
    }

    private static void addBass(Track tr, List<Double> ts, double amp) {
        double[] freqs = {45, 55, 49, 41};
        for (int i = 0; i < ts.size(); i++) tr.bass.add(new double[] {ts.get(i), freqs[i % freqs.length], amp});
    }

    // ---------------------------------------------------------------- running

    private static class Result {
        final List<Double> kickT = new ArrayList<>(), snareT = new ArrayList<>(), hatT = new ArrayList<>();
        double[] time, bass, mid, high, presence, intensity, bpm, conf;
        int invalid = 0;
        String firstInvalid = null;
        long updateNs = 0;
        int frames;

        double mean(double[] s, double t0, double t1) {
            double sum = 0; int c = 0;
            for (int i = 0; i < frames; i++) if (time[i] >= t0 && time[i] <= t1) { sum += s[i]; c++; }
            return c == 0 ? Double.NaN : sum / c;
        }

        double fracAbove(double[] s, double thr, double t0, double t1) {
            int above = 0, c = 0;
            for (int i = 0; i < frames; i++) if (time[i] >= t0 && time[i] <= t1) { c++; if (s[i] > thr) above++; }
            return c == 0 ? Double.NaN : above / (double) c;
        }

        double fracBpmWithin(double target, double tol, double t0, double t1) {
            int ok = 0, c = 0;
            for (int i = 0; i < frames; i++) if (time[i] >= t0 && time[i] <= t1) { c++; if (Math.abs(bpm[i] - target) <= tol) ok++; }
            return c == 0 ? Double.NaN : ok / (double) c;
        }

        double maxOf(double[] s, double t0, double t1) {
            double m = 0;
            for (int i = 0; i < frames; i++) if (time[i] >= t0 && time[i] <= t1) m = Math.max(m, s[i]);
            return m;
        }
    }

    private static Result run(Track tr) {
        tr.sort();
        SyntheticMeterDriver drv = new SyntheticMeterDriver(GAIN_DB, RANGE_DB);
        CubeBeatsAudio.configureMeter(drv.meter);
        CubeBeatsAudio a = new CubeBeatsAudio();
        a.sensitivity = SENS;
        int frames = (int) (tr.durationS * 1000 / FRAME_MS);
        Result r = new Result();
        r.frames = frames;
        r.time = new double[frames]; r.bass = new double[frames]; r.mid = new double[frames];
        r.high = new double[frames]; r.presence = new double[frames]; r.intensity = new double[frames];
        r.bpm = new double[frames]; r.conf = new double[frames];
        float[] win = new float[N], bands = new float[CubeBeatsAudio.NUM_BANDS];
        for (int f = 0; f < frames; f++) {
            double t = (f + 1) * FRAME_MS / 1000;
            long nEnd = Math.round(t * SR);
            tr.render(nEnd - N, win);
            drv.push(win, FRAME_MS, bands);
            long t0 = System.nanoTime();
            a.update(bands, FRAME_MS);
            r.updateNs += System.nanoTime() - t0;
            if (a.kick) r.kickT.add(t);
            if (a.snare) r.snareT.add(t);
            if (a.hat) r.hatT.add(t);
            r.time[f] = t; r.bass[f] = a.bass; r.mid[f] = a.mid; r.high[f] = a.high;
            r.presence[f] = a.presence; r.intensity[f] = a.intensity;
            r.bpm[f] = 60000.0 / a.periodMs; r.conf[f] = a.tempoConfidence;
            validate(r, a, t);
        }
        return r;
    }

    private static void validate(Result r, CubeBeatsAudio a, double t) {
        float[] unit = {a.presence, a.bass, a.mid, a.high, a.level, a.intensity, a.kickEnv, a.snareEnv, a.hatEnv,
            a.kickStrength, a.snareStrength, a.hatStrength, a.tempoConfidence};
        boolean bad = false;
        for (float v : unit) if (!(v >= 0 && v <= 1)) bad = true;
        for (float v : a.bandNorm) if (!(v >= 0 && v <= 1)) bad = true;
        if (!(a.beatPhase >= 0 && a.beatPhase < 1)) bad = true;
        if (!(a.periodMs >= 300 && a.periodMs <= 1000)) bad = true;
        if (bad) {
            r.invalid++;
            if (r.firstInvalid == null) {
                r.firstInvalid = String.format("t=%.2f presence=%f bass=%f mid=%f high=%f level=%f intensity=%f phase=%f period=%f conf=%f",
                    t, a.presence, a.bass, a.mid, a.high, a.level, a.intensity, a.beatPhase, a.periodMs, a.tempoConfidence);
            }
        }
    }

    /** @return {recall, precision, gtCount, detCount, meanLatencyMs} over [t0, t1] */
    private static double[] match(List<Double> gt, List<Double> det, double t0, double t1) {
        boolean[] gtHit = new boolean[gt.size()], detHit = new boolean[det.size()];
        double latSum = 0; int latN = 0;
        int j = 0;
        for (int i = 0; i < gt.size(); i++) {
            double g = gt.get(i);
            while (j < det.size() && det.get(j) < g - MATCH_EARLY_S) j++;
            for (int k = j; k < det.size() && det.get(k) <= g + MATCH_LATE_S; k++) {
                if (!detHit[k]) { detHit[k] = true; gtHit[i] = true; latSum += (det.get(k) - g) * 1000; latN++; break; }
            }
        }
        int gtC = 0, gtM = 0, dC = 0, dM = 0;
        for (int i = 0; i < gt.size(); i++) if (gt.get(i) >= t0 && gt.get(i) <= t1) { gtC++; if (gtHit[i]) gtM++; }
        for (int k = 0; k < det.size(); k++) if (det.get(k) >= t0 && det.get(k) <= t1) { dC++; if (detHit[k]) dM++; }
        return new double[] {gtC == 0 ? 1 : gtM / (double) gtC, dC == 0 ? 1 : dM / (double) dC, gtC, dC,
            latN == 0 ? Double.NaN : latSum / latN};
    }

    private static int countIn(List<Double> l, double t0, double t1) {
        int c = 0;
        for (double v : l) if (v >= t0 && v <= t1) c++;
        return c;
    }

    private static void log(String scenario, String fmt, Object... args) {
        System.out.println("[CubeBeatsAudio] " + scenario + ": " + String.format(fmt, args));
    }

    private static void assertValid(String scenario, Result r) {
        Assert.assertEquals(scenario + " invalid outputs, first: " + r.firstInvalid, 0, r.invalid);
    }

    private static void assertDetection(String scenario, String what, double[] m, double minRecall, double minPrecision) {
        log(scenario, "%s recall=%.3f precision=%.3f gt=%d det=%d latency=%.1fms", what, m[0], m[1], (int) m[2], (int) m[3], m[4]);
        Assert.assertTrue(scenario + " " + what + " recall " + m[0], m[0] >= minRecall);
        Assert.assertTrue(scenario + " " + what + " precision " + m[1], m[1] >= minPrecision);
    }

    // ---------------------------------------------------------------- scenarios

    @Test
    public void silence() {
        Track tr = new Track(20);
        Result r = run(tr);
        assertValid("silence", r);
        log("silence", "kicks=%d snares=%d hats=%d maxPresence(3-20s)=%.3f maxBass=%.3f maxMid=%.3f maxHigh=%.3f maxIntensity=%.3f",
            r.kickT.size(), r.snareT.size(), r.hatT.size(), r.maxOf(r.presence, 3, 20), r.maxOf(r.bass, 3, 20),
            r.maxOf(r.mid, 3, 20), r.maxOf(r.high, 3, 20), r.maxOf(r.intensity, 3, 20));
        Assert.assertEquals(0, r.kickT.size());
        Assert.assertTrue(r.snareT.size() + r.hatT.size() <= 2);
        Assert.assertTrue(r.maxOf(r.presence, 3, 20) < 0.1);
        Assert.assertTrue(r.maxOf(r.bass, 3, 20) < 0.1);
        Assert.assertTrue(r.maxOf(r.mid, 3, 20) < 0.1);
        Assert.assertTrue(r.maxOf(r.high, 3, 20) < 0.1);
        Assert.assertTrue(r.maxOf(r.intensity, 3, 20) < 0.1);
    }

    @Test
    public void lowLevel() {
        Track tr = new Track(30);
        add(tr.kicks, grid(0.5, 30, 120, 0, 1), 0.5);
        add(tr.hats, grid(0.5, 30, 120, 0.5, 1), 0.12);
        tr.master = t -> 0.1; // -20 dB
        Result r = run(tr);
        assertValid("lowLevel", r);
        assertDetection("lowLevel", "kick", match(tr.kickTimes(), r.kickT, 4, 30), 0.85, 0.85);
        double pres = r.mean(r.presence, 4, 30), bass = r.mean(r.bass, 4, 30);
        log("lowLevel", "meanPresence=%.3f meanBass=%.3f", pres, bass);
        Assert.assertTrue(pres >= 0.6);
        Assert.assertTrue(bass >= 0.15 && bass <= 0.9);

        Track quiet = new Track(30);
        add(quiet.kicks, grid(0.5, 30, 120, 0, 1), 0.5);
        add(quiet.hats, grid(0.5, 30, 120, 0.5, 1), 0.12);
        quiet.master = t -> 0.02; // -34 dB: informational recall, but no false-trigger storm
        Result q = run(quiet);
        assertValid("veryQuiet", q);
        double[] m = match(quiet.kickTimes(), q.kickT, 4, 30);
        log("veryQuiet", "kick recall=%.3f precision=%.3f det=%d meanPresence=%.3f", m[0], m[1], (int) m[3], q.mean(q.presence, 4, 30));
        Assert.assertTrue("veryQuiet kick precision " + m[1], m[1] >= 0.8);
    }

    @Test
    public void sparsePercussion() {
        double bpm = 90, end = 32, beat = 60 / bpm;
        Track tr = new Track(end);
        List<Double> kicks = new ArrayList<>();
        kicks.addAll(grid(0.5, end, bpm, 0, 4));
        kicks.addAll(grid(0.5, end, bpm, 2.5, 4));
        Collections.sort(kicks);
        add(tr.kicks, kicks, 0.5);
        List<Double> snares = new ArrayList<>(grid(0.5, end, bpm, 1, 2));
        add(tr.snares, snares, 0.3);
        add(tr.hats, grid(0.5, end, bpm, 0.5, 4), 0.1);
        add(tr.hats, grid(0.5, end, bpm, 1.5, 4), 0.1);
        Result r = run(tr);
        assertValid("sparse", r);
        assertDetection("sparse", "kick", match(tr.kickTimes(), r.kickT, 3, end), 0.9, 0.85);
        assertDetection("sparse", "snare", match(snares, r.snareT, 3, end), 0.6, 0.6);
        tr.sort();
        double[] hm = match(tr.times(tr.hats), r.hatT, 3, end);
        log("sparse", "hat recall=%.3f precision=%.3f det=%d gt=%d; tempoConf(mean 10-32s)=%.3f bpm(mean)=%.1f (beat=%.0fms, irregular kicks)",
            hm[0], hm[1], (int) hm[3], (int) hm[2], r.mean(r.conf, 10, end), r.mean(r.bpm, 10, end), beat * 1000);
        Assert.assertTrue("sparse hat recall " + hm[0], hm[0] >= 0.5);
        double hatRate = countIn(r.hatT, 3, end) / (end - 3), gtRate = hm[2] / (end - 3);
        Assert.assertTrue("sparse hat rate " + hatRate, hatRate <= 2 * gtRate + 1);
    }

    @Test
    public void bassHeavy() {
        double bpm = 128, end = 32;
        Track tr = new Track(end);
        add(tr.kicks, grid(0.5, end, bpm, 0, 1), 0.6);
        addBass(tr, grid(0.5, end, bpm, 0.5, 1), 0.45);
        add(tr.hats, grid(0.5, end, bpm, 0.5, 1), 0.08);
        Result r = run(tr);
        assertValid("bassHeavy", r);
        assertDetection("bassHeavy", "kick", match(tr.kickTimes(), r.kickT, 4, end), 0.85, 0.85);
        double pinned = r.fracAbove(r.bass, 0.98, 4, end), meanBass = r.mean(r.bass, 4, end);
        double bpmOk = r.fracBpmWithin(bpm, 3, 12, end), conf = r.mean(r.conf, 12, end);
        log("bassHeavy", "bassPinnedFrac=%.3f meanBass=%.3f bpmWithin3(12-32s)=%.3f meanConf=%.3f meanBpm=%.1f",
            pinned, meanBass, bpmOk, conf, r.mean(r.bpm, 12, end));
        Assert.assertTrue(pinned < 0.2);
        Assert.assertTrue(meanBass >= 0.2 && meanBass <= 0.9);
        Assert.assertTrue(bpmOk >= 0.9);
        Assert.assertTrue(conf >= 0.6);
    }

    @Test
    public void denseLoud() {
        double bpm = 140, end = 32;
        Track tr = new Track(end);
        add(tr.kicks, grid(0.5, end, bpm, 0, 1), 0.9);
        addBass(tr, grid(0.5, end, bpm, 0.5, 1), 0.4);
        add(tr.snares, grid(0.5, end, bpm, 1, 2), 0.6);
        add(tr.hats, grid(0.5, end, bpm, 0, 0.25), 0.2);
        tr.pinkAmp = 0.2;
        tr.padAmp = 0.15;
        tr.clip = true;
        Result r = run(tr);
        assertValid("dense", r);
        assertDetection("dense", "kick", match(tr.kickTimes(), r.kickT, 4, end), 0.75, 0.8);
        double hiPinned = r.fracAbove(r.high, 0.98, 4, end), midPinned = r.fracAbove(r.mid, 0.98, 4, end);
        log("dense", "highPinnedFrac=%.3f midPinnedFrac=%.3f meanBass=%.3f meanMid=%.3f meanHigh=%.3f snares=%d hats/s=%.2f bpm(mean 12-32)=%.1f conf=%.3f",
            hiPinned, midPinned, r.mean(r.bass, 4, end), r.mean(r.mid, 4, end), r.mean(r.high, 4, end),
            r.snareT.size(), countIn(r.hatT, 4, end) / (end - 4), r.mean(r.bpm, 12, end), r.mean(r.conf, 12, end));
        Assert.assertTrue(hiPinned < 0.25);
        Assert.assertTrue(midPinned < 0.25);
    }

    @Test
    public void tempoChange() {
        double end = 44;
        Track tr = new Track(end);
        add(tr.kicks, grid(0.5, 20, 100, 0, 1), 0.6);
        add(tr.hats, grid(0.5, 20, 100, 0.5, 1), 0.1);
        add(tr.kicks, grid(20, end, 140, 0, 1), 0.6);
        add(tr.hats, grid(20, end, 140, 0.5, 1), 0.1);
        Result r = run(tr);
        assertValid("tempoChange", r);
        assertDetection("tempoChange", "kick", match(tr.kickTimes(), r.kickT, 3, end), 0.85, 0.85);
        double ok100 = r.fracBpmWithin(100, 3, 12, 20), ok140 = r.fracBpmWithin(140, 3, 30, end);
        double relock = Double.NaN;
        for (int i = 0; i < r.frames; i++) {
            if (r.time[i] < 20) continue;
            boolean stable = true;
            for (int k = i; k < r.frames && r.time[k] < r.time[i] + 3; k++) if (Math.abs(r.bpm[k] - 140) > 3) { stable = false; break; }
            if (stable) { relock = r.time[i] - 20; break; }
        }
        log("tempoChange", "bpmWithin3@100(12-20s)=%.3f bpmWithin3@140(30-44s)=%.3f relockAfter=%.2fs conf(12-20)=%.3f conf(30-44)=%.3f",
            ok100, ok140, relock, r.mean(r.conf, 12, 20), r.mean(r.conf, 30, end));
        Assert.assertTrue(ok100 >= 0.9);
        Assert.assertTrue(ok140 >= 0.9);
    }

    @Test
    public void levelChange() {
        double bpm = 120, end = 45;
        Track tr = new Track(end);
        add(tr.kicks, grid(0.5, end, bpm, 0, 1), 0.6);
        addBass(tr, grid(0.5, end, bpm, 0.5, 1), 0.3);
        add(tr.hats, grid(0.5, end, bpm, 0.5, 1), 0.1);
        tr.master = t -> (t >= 15 && t < 30) ? 1.0 : 0.1;
        tr.clip = true;
        Result r = run(tr);
        assertValid("levelChange", r);
        assertDetection("levelChange", "kick[-20dB 3-15s]", match(tr.kickTimes(), r.kickT, 3, 15), 0.8, 0.8);
        assertDetection("levelChange", "kick[0dB 18-30s]", match(tr.kickTimes(), r.kickT, 18, 30), 0.8, 0.8);
        assertDetection("levelChange", "kick[-20dB 33-45s]", match(tr.kickTimes(), r.kickT, 33, 45), 0.8, 0.8);
        double b1 = r.mean(r.bass, 3, 15), b2 = r.mean(r.bass, 18, 30), b3 = r.mean(r.bass, 33, 45);
        double i1 = r.mean(r.intensity, 10, 15), i2 = r.mean(r.intensity, 25, 30), iEnd = r.intensity[r.frames - 1];
        log("levelChange", "meanBass seg1=%.3f seg2=%.3f seg3=%.3f; intensity [10-15]=%.3f [25-30]=%.3f end=%.3f",
            b1, b2, b3, i1, i2, iEnd);
        Assert.assertTrue(b1 >= 0.15 && b1 <= 0.9);
        Assert.assertTrue(b2 >= 0.15 && b2 <= 0.9);
        Assert.assertTrue(b3 > 0.08);
        Assert.assertTrue(i2 > i1 + 0.15);
        Assert.assertTrue(iEnd < i2);
    }

    @Test
    public void sustainedTenMinutes() {
        double end = 600;
        Track tr = new Track(end);
        List<double[]> breakdowns = new ArrayList<>();
        List<Double> kickGt = new ArrayList<>();
        double t = 0.5;
        int beatIdx = 0;
        double bdStart = -1;
        while (t < end) {
            double bpm = 118 + 14 * (t / end);
            double beat = 60 / bpm;
            int bar = beatIdx / 4, inBar = beatIdx % 4;
            boolean breakdown = (bar % 40) >= 32;
            if (breakdown && inBar == 0 && (bar % 40) == 32) bdStart = t;
            if (!breakdown && bdStart >= 0) { breakdowns.add(new double[] {bdStart, t}); bdStart = -1; }
            if (!breakdown) {
                tr.kicks.add(new double[] {t, 0.8});
                kickGt.add(t);
                tr.bass.add(new double[] {t + beat / 2, new double[] {45, 55, 49, 41}[beatIdx % 4], 0.35});
                if (inBar == 1 || inBar == 3) tr.snares.add(new double[] {t, 0.5});
                tr.hats.add(new double[] {t, 0.15});
                tr.hats.add(new double[] {t + beat / 2, 0.15});
            } else {
                tr.hats.add(new double[] {t + beat / 2, 0.15});
            }
            t += beat;
            beatIdx++;
        }
        final List<double[]> bds = breakdowns;
        tr.master = x -> {
            for (double[] b : bds) if (x >= b[0] && x < b[1]) return 0.4;
            return 1.0;
        };
        tr.padAmp = 0.12;
        tr.pinkAmp = 0.1;
        Result r = run(tr);
        assertValid("tenMinutes", r);
        double[] m = match(kickGt, r.kickT, 4, end);
        assertDetection("tenMinutes", "kick", m, 0.75, 0.8);
        int worstBreakdown = 0;
        for (double[] b : breakdowns) worstBreakdown = Math.max(worstBreakdown, countIn(r.kickT, b[0] + 0.6, b[1] - 0.05));
        double usPerUpdate = r.updateNs / 1000.0 / r.frames;
        log("tenMinutes", "breakdowns=%d worstKicksInBreakdown=%d usPerUpdate=%.2f meanIntensity=%.3f",
            breakdowns.size(), worstBreakdown, usPerUpdate, r.mean(r.intensity, 4, end));
        Assert.assertTrue(worstBreakdown <= 2);
        Assert.assertTrue("update cost " + usPerUpdate + "us", usPerUpdate < 200);
    }
}
