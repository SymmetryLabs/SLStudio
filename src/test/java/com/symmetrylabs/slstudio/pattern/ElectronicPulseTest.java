package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.data.LXVersion;
import heronarts.lx.model.LXPoint;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ElectronicPulseTest {

    @BeforeClass
    public static void setup() {
        ApplicationState.setProvider(new ApplicationState.DummyProvider());
    }

    private final List<ElectronicPulse> patterns = new ArrayList<>();
    private final List<LX> engines = new ArrayList<>();

    @After
    public void tearDown() {
        for (ElectronicPulse p : patterns) {
            try { p.dispose(); } catch (Throwable ignored) { }
        }
        for (LX lx : engines) {
            try { lx.dispose(); } catch (Throwable ignored) { }
        }
        patterns.clear();
        engines.clear();
    }

    // ------------------------------------------------------------- fixture

    private static SLModel gridModel() {
        List<LXPoint> pts = new ArrayList<>();
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                for (int z = 0; z < 4; z++)
                    pts.add(new LXPoint(x * 10f, y * 10f, z * 10f));
        return new SLModel("ep-grid", pts);
    }

    private static SLModel lineModel() {
        List<LXPoint> pts = new ArrayList<>();
        for (int i = 0; i < 64; i++) pts.add(new LXPoint(i * 5f, 0, 0));
        return new SLModel("ep-line", pts);
    }

    private static SLModel smallModel() {
        List<LXPoint> pts = new ArrayList<>();
        for (int i = 0; i < 8; i++) pts.add(new LXPoint(i * 7f, i * 3f, i));
        return new SLModel("ep-small", pts);
    }

    private ElectronicPulse newPattern(SLModel m) {
        LX lx = new LX(LXVersion.SLSTUDIO_ORIG, m);
        engines.add(lx);
        ElectronicPulse p = new ElectronicPulse(lx);
        patterns.add(p);
        return p;
    }

    private static int[] colorsOf(ElectronicPulse p) {
        return (int[]) p.getPolyBuffer().getArray(PolyBuffer.Space.SRGB8);
    }

    private static int nonBlack(int[] colors) {
        int c = 0;
        for (int col : colors) if ((col & 0xFFFFFF) != 0) c++;
        return c;
    }

    private static float[] zeros() {
        return new float[16];
    }

    // ------------------------------------------------------------- tests

    @Test
    public void silenceBlackAfterTails() {
        LXPoint.resetIdCounter();
        ElectronicPulse p = newPattern(gridModel());
        int[] colors = colorsOf(p);
        p.renderFrame(16, zeros());
        Assert.assertEquals(0, nonBlack(colors));
        p.renderLayers(16, 0.8f, 0.8f, 1f, 1f);
        Assert.assertTrue("layered hit should light", nonBlack(colors) > 0);
        for (int i = 0; i < 400; i++) p.renderFrame(16, zeros());
        Assert.assertEquals("silence must decay to black", 0, nonBlack(colors));
    }

    @Test
    public void eachLayerLightsDistinctOutput() {
        int[][] snapshot = new int[3][];
        for (int layer = 0; layer < 3; layer++) {
            LXPoint.resetIdCounter();
            ElectronicPulse p = newPattern(gridModel());
            int[] colors = colorsOf(p);
            for (int i = 0; i < 4; i++) {
                p.renderLayers(16,
                    layer == 0 ? 0.8f : 0,
                    layer == 2 ? 0.8f : 0,
                    layer == 1 ? 1f : 0,
                    layer == 2 ? 1f : 0);
            }
            Assert.assertTrue("layer " + layer + " produced no light",
                nonBlack(colors) > 0);
            snapshot[layer] = colors.clone();
        }
        for (int a = 0; a < 3; a++) {
            for (int b = a + 1; b < 3; b++) {
                int diff = 0;
                for (int i = 0; i < snapshot[a].length; i++)
                    if (snapshot[a][i] != snapshot[b][i]) diff++;
                Assert.assertTrue("layers " + a + " and " + b + " identical", diff > 0);
            }
        }
        LXPoint.resetIdCounter();
        ElectronicPulse off = newPattern(gridModel());
        off.kick.setValue(0);
        off.bass.setValue(0);
        off.hats.setValue(0);
        int[] colors = colorsOf(off);
        for (int i = 0; i < 4; i++) off.renderLayers(16, 1f, 1f, 1f, 1f);
        Assert.assertEquals("all layer controls at 0 must be black", 0, nonBlack(colors));
    }

    @Test
    public void emptyModelSafe() {
        LXPoint.resetIdCounter();
        ElectronicPulse p = newPattern(new SLModel("ep-empty", new ArrayList<LXPoint>()));
        p.renderLayers(16, 0.9f, 0.9f, 1f, 1f);
        p.renderLayers(0, 0.9f, 0.9f, 1f, 1f);
        p.renderFrame(16, zeros());
    }

    @Test
    public void flatLineModelRenders() {
        LXPoint.resetIdCounter();
        ElectronicPulse p = newPattern(lineModel());
        int[] colors = colorsOf(p);
        p.renderLayers(16, 0.9f, 0, 1f, 0);
        Assert.assertTrue("kick on flat line produced no light", nonBlack(colors) > 0);
        for (int i = 0; i < 60; i++) p.renderLayers(16, 0, 0, 0, 0);
        p.renderLayers(16, 0.9f, 0, 0, 0);
        Assert.assertTrue("bass on flat line produced no light", nonBlack(colors) > 0);
    }

    @Test
    public void modelReplacementRebuilds() {
        LXPoint.resetIdCounter();
        SLModel m1 = gridModel();
        LX lx = new LX(LXVersion.SLSTUDIO_ORIG, m1);
        engines.add(lx);
        ElectronicPulse p = new ElectronicPulse(lx);
        patterns.add(p);
        p.renderLayers(16, 0.9f, 0, 1f, 0);
        LXPoint.resetIdCounter();
        p.setModel(smallModel());
        int[] colors = colorsOf(p);
        for (int i = 0; i < 10; i++) p.renderLayers(16, 0.9f, 0, 1f, 0);
        Assert.assertTrue(nonBlack(colors) > 0);
    }

    // ----------------------------------------------------- audio routing

    private static boolean driveTrain(ElectronicPulse p, int bandLo, int bandHi,
                                      int pulseMs, int periodMs) {
        int[] colors = colorsOf(p);
        boolean lit = false;
        for (int t = 0; t < 3000; t += 10) {
            float[] bands = zeros();
            if (t % periodMs < pulseMs) {
                for (int b = bandLo; b <= bandHi; b++) bands[b] = 0.9f;
            }
            p.renderFrame(10, bands);
            if (nonBlack(colors) > 0) lit = true;
        }
        return lit;
    }

    @Test
    public void lowPulsesFireKickNotHats() {
        // Kick layer only: low pulse train must light it.
        LXPoint.resetIdCounter();
        ElectronicPulse kickOnly = newPattern(gridModel());
        kickOnly.bass.setValue(0);
        kickOnly.hats.setValue(0);
        Assert.assertTrue("low train should fire kick layer",
            driveTrain(kickOnly, 1, 3, 80, 500));

        // Hats layer only: same train must not light it.
        LXPoint.resetIdCounter();
        ElectronicPulse hatsOnly = newPattern(gridModel());
        hatsOnly.kick.setValue(0);
        hatsOnly.bass.setValue(0);
        Assert.assertFalse("low train must not fire hat layer",
            driveTrain(hatsOnly, 1, 3, 80, 500));
    }

    @Test
    public void highPulsesFireHatsNotKick() {
        // Hats layer only: high pulse train must light it.
        LXPoint.resetIdCounter();
        ElectronicPulse hatsOnly = newPattern(gridModel());
        hatsOnly.kick.setValue(0);
        hatsOnly.bass.setValue(0);
        Assert.assertTrue("high train should fire hat layer",
            driveTrain(hatsOnly, 12, 14, 40, 125));

        // Kick layer only: same train must not light it.
        LXPoint.resetIdCounter();
        ElectronicPulse kickOnly = newPattern(gridModel());
        kickOnly.bass.setValue(0);
        kickOnly.hats.setValue(0);
        Assert.assertFalse("high train must not fire kick layer",
            driveTrain(kickOnly, 12, 14, 40, 125));
    }
}
