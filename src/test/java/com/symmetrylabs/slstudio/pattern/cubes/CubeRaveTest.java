package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.ApplicationState;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.DecibelMeter;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.data.LXVersion;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class CubeRaveTest {

    @BeforeClass
    public static void setup() {
        ApplicationState.setProvider(new ApplicationState.DummyProvider());
    }

    // ---------------------------------------------------------- pure helpers

    @Test
    public void bandStateAttackDecayIndependent() {
        CubeRave.BandState fast = new CubeRave.BandState();
        CubeRave.BandState slow = new CubeRave.BandState();
        // zero attack: level reaches target in one frame
        fast.update(16, 0.8f, 0, 300, 0.5f, 120);
        Assert.assertEquals(0.8f, fast.level, 1e-4f);
        // nonzero attack rises more slowly
        for (int i = 0; i < 6; i++) slow.update(16, 0.8f, 100, 300, 0.5f, 120);
        Assert.assertTrue("slow attack should lag", slow.level < 0.6f);
        // decay independent of attack: silence decays to ~0 in finite time
        CubeRave.BandState b = new CubeRave.BandState();
        b.update(16, 1f, 0, 100, 0.5f, 120);
        for (int i = 0; i < 60; i++) b.update(16, 0, 0, 100, 0.5f, 120);
        Assert.assertTrue("level should decay, got " + b.level, b.level < 0.05f);
    }

    @Test
    public void bandStateSustainedTargetTriggersOnce() {
        CubeRave.BandState b = new CubeRave.BandState();
        for (int i = 0; i < 30; i++) b.update(16, 0, 0, 200, 0.5f, 120);
        int onsets = 0;
        for (int i = 0; i < 600; i++) {
            if (b.update(16, 0.7f, 5, 200, 0.5f, 120)) onsets++;
        }
        Assert.assertEquals("sustained energy should trigger once", 1, onsets);
    }

    @Test
    public void bandStateSeparatedRisesRetrigger() {
        CubeRave.BandState b = new CubeRave.BandState();
        int onsets = 0;
        for (int cycle = 0; cycle < 4; cycle++) {
            for (int i = 0; i < 2; i++) if (b.update(16, 0.7f, 0, 100, 0.5f, 120)) onsets++;
            for (int i = 0; i < 15; i++) if (b.update(16, 0, 0, 100, 0.5f, 120)) onsets++;
        }
        Assert.assertTrue("expected >=3 onsets, got " + onsets, onsets >= 3);
    }

    @Test
    public void bandStateMinGapCooldown() {
        CubeRave.BandState b = new CubeRave.BandState();
        Assert.assertTrue(b.update(16, 0.8f, 0, 100, 0.5f, 200));
        // drop to re-arm quickly, then rise again 48ms later (< 200ms gap): no onset
        b.update(16, 0, 0, 100, 0.5f, 200);
        Assert.assertFalse("rise within minGap must not trigger",
            b.update(16, 0.85f, 0, 100, 0.5f, 200));
        // after gap elapses, a fresh rise triggers
        for (int i = 0; i < 20; i++) b.update(16, 0, 0, 100, 0.5f, 200);
        Assert.assertTrue(b.update(16, 0.9f, 0, 100, 0.5f, 200));
    }

    @Test
    public void bandStateZeroDeltaFinite() {
        CubeRave.BandState b = new CubeRave.BandState();
        for (int i = 0; i < 10; i++) {
            b.update(0, 0.7f, 0, 100, 0.5f, 120);
            Assert.assertTrue(Float.isFinite(b.level));
            Assert.assertTrue(Float.isFinite(b.baseline));
        }
        CubeRave.Pulse p = new CubeRave.Pulse();
        p.trigger();
        Assert.assertTrue(Float.isFinite(p.update(0, 0, 100)));
        Assert.assertTrue(Float.isFinite(p.update(0, 50, 100)));
    }

    @Test
    public void pulseLongAttackSurvivesUntilPeak() {
        CubeRave.Pulse p = new CubeRave.Pulse();
        p.trigger();
        Assert.assertTrue(p.isActive());
        // zero delta and long attack: envelope near zero but must not be retired
        Assert.assertEquals(0f, p.update(0, 250, 100), 1e-6f);
        Assert.assertTrue(p.isActive());
        Assert.assertTrue(p.update(16, 250, 100) < 0.2f);
        Assert.assertTrue(p.isActive());
        // during the attack window the pulse climbs toward its peak
        Assert.assertTrue(p.update(200, 250, 100) > 0.8f);
        Assert.assertTrue(p.isActive());
        // after enough decay time it retires
        p.update(400, 250, 100);
        Assert.assertFalse(p.isActive());
    }

    // ---------------------------------------------------------- integration

    private static CubesModel twoCubeModel() {
        CubesModel.Cube[] cubes = {
            new CubesModel.Cube("c1", 0, 0, 0, 0, 0, 0, new LXTransform(),
                CubesModel.Cube.Type.SMALL),
            new CubesModel.Cube("c2", 30, 20, 10, 0, 90, 0, new LXTransform(),
                CubesModel.Cube.Type.SMALL),
        };
        return new CubesModel("rave-test", new ArrayList<>(), cubes, null, null);
    }

    private static CubeRave newPattern(CubesModel m) {
        LX lx = new LX(LXVersion.SLSTUDIO_ORIG, m);
        return new CubeRave(lx);
    }

    private static int[] colorsOf(CubeRave p) {
        return (int[]) p.getPolyBuffer().getArray(PolyBuffer.Space.SRGB8);
    }

    private static int nonBlack(int[] colors) {
        int c = 0;
        for (int col : colors) if ((col & 0xFFFFFF) != 0) c++;
        return c;
    }

    @Test
    public void emptyModelSafe() {
        LXPoint.resetIdCounter();
        CubesModel m = new CubesModel("empty");
        CubeRave p = newPattern(m);
        p.renderFrame(16, 1, 1, 1);
        p.renderFrame(0, 1, 1, 1);
    }

    @Test
    public void silenceBlackAfterTails() {
        LXPoint.resetIdCounter();
        CubesModel m = twoCubeModel();
        CubeRave p = newPattern(m);
        int[] colors = colorsOf(p);
        for (int i = 0; i < 5; i++) p.renderFrame(16, 0.9f, 0.9f, 0.9f);
        Assert.assertTrue(nonBlack(colors) > 0);
        for (int i = 0; i < 300; i++) p.renderFrame(16, 0, 0, 0);
        Assert.assertEquals("silence must decay to black", 0, nonBlack(colors));
    }

    @Test
    public void eachBandLightsDistinctOutput() {
        int[][] snapshot = new int[3][];
        CubesModel m = null;
        for (int band = 0; band < 3; band++) {
            LXPoint.resetIdCounter();
            m = twoCubeModel();
            CubeRave p = newPattern(m);
            int[] colors = colorsOf(p);
            for (int i = 0; i < 12; i++) {
                p.renderFrame(16, band == 0 ? 0.95f : 0, band == 1 ? 0.95f : 0,
                    band == 2 ? 0.95f : 0);
            }
            Assert.assertTrue("band " + band + " produced no light", nonBlack(colors) > 0);
            snapshot[band] = colors.clone();
        }
        for (int a = 0; a < 3; a++) {
            for (int b = a + 1; b < 3; b++) {
                int diff = 0;
                for (int i = 0; i < snapshot[a].length; i++)
                    if (snapshot[a][i] != snapshot[b][i]) diff++;
                Assert.assertTrue("bands " + a + " and " + b + " identical", diff > 0);
            }
        }
    }

    @Test
    public void geometryCoversModelPoints() {
        LXPoint.resetIdCounter();
        CubesModel m = twoCubeModel();
        CubeRave p = newPattern(m);
        p.density.setValue(1);
        int[] colors = colorsOf(p);
        boolean[] everLit = new boolean[colors.length];
        // repeated low onsets: expanding waves should sweep every point
        for (int f = 0; f < 240; f++) {
            boolean rising = (f % 20) < 2;
            p.renderFrame(16, rising ? 0.9f : 0, rising ? 0.9f : 0, rising ? 0.9f : 0);
            for (LXPoint pt : m.points)
                if (pt.index < colors.length && (colors[pt.index] & 0xFFFFFF) != 0)
                    everLit[pt.index] = true;
        }
        int lit = 0;
        for (LXPoint pt : m.points) if (pt.index < colors.length && everLit[pt.index]) lit++;
        Assert.assertTrue("only " + lit + "/" + m.points.length + " points ever lit",
            lit >= m.points.length * 0.9f);
    }

    @Test
    public void nonzeroPointIndices() {
        LXPoint.resetIdCounter();
        new LXPoint(0, 0, 0); // bump the global counter so model indices start at 1
        CubesModel m = twoCubeModel();
        int maxIndex = 0;
        for (LXPoint pt : m.points) maxIndex = Math.max(maxIndex, pt.index);
        Assert.assertTrue("fixture should have nonzero indices",
            maxIndex >= m.points.length);
        CubeRave p = newPattern(m); // buffer sized n; last point index == n is skipped
        int[] colors = colorsOf(p);
        for (int i = 0; i < 30; i++) p.renderFrame(16, 0.95f, 0.9f, 0.9f);
        Assert.assertTrue(nonBlack(colors) > 0);
        Assert.assertEquals(0, colors[0] & 0xFFFFFF); // no model point owns index 0
    }

    @Test
    public void extremeControlsFinite() {
        LXPoint.resetIdCounter();
        CubesModel m = twoCubeModel();
        CubeRave p = newPattern(m);
        p.gate.setValue(0.8);
        p.waveSpeed.setValue(5);
        p.waveWidth.setValue(0.5);
        p.motion.setValue(4);
        p.trails.setValue(700);
        p.white.setValue(1);
        p.brightness.setValue(1);
        p.spread.setValue(240);
        p.sensitivity.setValue(0);
        p.minGap.setValue(70);
        int[] colors = colorsOf(p);
        for (int i = 0; i < 120; i++) {
            p.renderFrame(16 + (i % 3) * 7, (i % 7) < 3 ? 1f : 0, 1, (i % 5) < 2 ? 1f : 0);
        }
        p.renderFrame(0, 1, 1, 1); // zero delta safe
    }

    @Test
    public void brightnessZeroAlwaysBlack() {
        LXPoint.resetIdCounter();
        CubesModel m = twoCubeModel();
        CubeRave p = newPattern(m);
        p.white.setValue(1);
        p.backdrop.setValue(0.4);
        p.trails.setValue(700);
        int[] colors = colorsOf(p);
        for (int i = 0; i < 10; i++) p.renderFrame(16, 0.95f, 0.95f, 0.95f);
        Assert.assertTrue(nonBlack(colors) > 0);
        p.brightness.setValue(0);
        p.renderFrame(16, 0.95f, 0.95f, 0.95f);
        Assert.assertEquals("Bright=0 must be black despite bg/white/trails",
            0, nonBlack(colors));
    }

    @Test
    public void mixesOffIsBlack() {
        LXPoint.resetIdCounter();
        CubesModel m = twoCubeModel();
        CubeRave p = newPattern(m);
        p.levelLow.setValue(0);
        p.levelMid.setValue(0);
        p.levelHigh.setValue(0);
        p.backdrop.setValue(0.4);
        p.trails.setValue(0);
        int[] colors = colorsOf(p);
        for (int i = 0; i < 30; i++) p.renderFrame(16, 0.95f, 0.95f, 0.95f);
        Assert.assertEquals("all band mixes at 0 must be black", 0, nonBlack(colors));
    }

    @Test
    public void slowAttackDelaysTransient() {
        LXPoint.resetIdCounter();
        CubesModel m = twoCubeModel();
        CubeRave p = newPattern(m);
        p.lowAttack.setValue(250);
        p.backdrop.setValue(0);
        int[] colors = colorsOf(p);
        for (int i = 0; i < 30; i++) p.renderFrame(16, 0, 0, 0); // silence warmup
        for (int i = 0; i < 2; i++) p.renderFrame(16, 0.9f, 0, 0);
        long early = sumBrightness(colors);
        for (int i = 0; i < 16; i++) p.renderFrame(16, 0.9f, 0, 0);
        long late = sumBrightness(colors);
        Assert.assertTrue("long attack should grow the transient (" + early + " -> "
            + late + ")", late > early);
        Assert.assertTrue(late > 0);
    }

    private static long sumBrightness(int[] colors) {
        long s = 0;
        for (int c : colors) s += (c & 0xFF) + ((c >>> 8) & 0xFF) + ((c >>> 16) & 0xFF);
        return s;
    }

    // ---------------------------------------------------------- FFT path

    private static GraphicMeter meterOf(CubeRave p) throws Exception {
        Field f = CubeRave.class.getDeclaredField("eq");
        f.setAccessible(true);
        return (GraphicMeter) f.get(p);
    }

    private static float[] sine(int size, int sampleRate, double hz, double amp) {
        float[] s = new float[size];
        for (int i = 0; i < size; i++)
            s[i] = (float) (amp * Math.sin(2 * Math.PI * hz * i / sampleRate));
        return s;
    }

    @Test
    public void fftDrivenBandResponses() throws Exception {
        double[] freqs = {70, 1000, 8000};
        for (double hz : freqs) {
            LXPoint.resetIdCounter();
            CubesModel m = twoCubeModel();
            CubeRave p = newPattern(m);
            GraphicMeter eq = meterOf(p);
            int[] colors = colorsOf(p);
            float[] s = sine(eq.fft.getSize(), eq.fft.getSampleRate(), hz, 0.7);
            for (int i = 0; i < 12; i++) {
                eq.fft.compute(s);
                p.run(16, PolyBuffer.Space.SRGB8);
            }
            Assert.assertTrue(hz + " Hz sine produced no light", nonBlack(colors) > 0);
            // digital silence decays to black
            float[] zero = new float[eq.fft.getSize()];
            for (int i = 0; i < 300; i++) {
                eq.fft.compute(zero);
                p.run(16, PolyBuffer.Space.SRGB8);
            }
            Assert.assertEquals(hz + " Hz: silence should decay to black",
                0, nonBlack(colors));
        }
    }

    @Test
    public void meterFollowsAudioMode() throws Exception {
        LXPoint.resetIdCounter();
        CubesModel m = twoCubeModel();
        LX lx = new LX(LXVersion.SLSTUDIO_ORIG, m);
        CubeRave p = new CubeRave(lx);
        GraphicMeter eq = meterOf(p);
        Field bf = DecibelMeter.class.getDeclaredField("buffer");
        bf.setAccessible(true);

        lx.engine.audio.mode.setValue(LXAudioEngine.Mode.OUTPUT);
        eq.loop(16);
        Assert.assertSame(lx.engine.audio.output.mix, bf.get(eq));
        lx.engine.audio.mode.setValue(LXAudioEngine.Mode.INPUT);
        eq.loop(16);
        Assert.assertSame(lx.engine.audio.input.mix, bf.get(eq));
    }

    @Test
    public void modelReplacementRebuilds() {
        LXPoint.resetIdCounter();
        CubesModel m1 = twoCubeModel();
        LX lx = new LX(LXVersion.SLSTUDIO_ORIG, m1);
        CubeRave p = new CubeRave(lx);
        p.renderFrame(16, 0.9f, 0.9f, 0.9f);
        LXPoint.resetIdCounter();
        CubesModel m2 = twoCubeModel();
        p.setModel(m2);
        int[] colors = colorsOf(p);
        for (int i = 0; i < 10; i++) p.renderFrame(16, 0.9f, 0.9f, 0.9f);
        Assert.assertTrue(nonBlack(colors) > 0);
    }
}
