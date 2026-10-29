package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.pattern.cubes.CubeBeatsAudio;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.util.MathUtils.random;
import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.*;

/**
 * EDM-oriented audio-reactive pattern: kicks drive bass slams/shockwaves,
 * claps drive accent-colored flashes, hats add sparkle.
 * Modes: Pump, Split, Rings, Chase.
 */
public class BeatDrop extends SLPattern<SLModel> {
    public static final String GROUP_NAME = "Audio";
    private static final int MAX_RINGS = 16;
    private static final float INV_HALF_DIAG = 1f / 0.866f;

    private final GraphicMeter eq = new GraphicMeter(lx.engine.audio.getInput());
    private final CubeBeatsAudio audio = new CubeBeatsAudio();
    private final float[] bands = new float[CubeBeatsAudio.NUM_BANDS];

    public final DiscreteParameter modeParam =
        new DiscreteParameter("Mode", new String[]{"Pump", "Split", "Rings", "Chase"});
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
    private float lastKickStrength, lastClapStrength;
    private float floorB;

    // rings mode state
    private final List<Ring> rings = new ArrayList<Ring>();

    // chase mode state
    private float chasePos;
    private int chaseDir = 1;

    private static class Ring {
        float cx, cy, cz, r, strength, hue;
        boolean inward;
    }

    public BeatDrop(LX lx) {
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
        addParameter(hueParam);
        addParameter(spreadParam);
        addParameter(accentParam);
        addParameter(satParam);
        addParameter(trailsParam);

        CubeBeatsAudio.configureMeter(eq);
        eq.range.setNormalized(0.4);
        eq.start();
        addModulator(eq).start();

        precompute();
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
        }
        if (audio.snare) lastClapStrength = audio.snareStrength;

        float breath = 0.5f + 0.5f * (float) Math.sin(nowMs * TWO_PI / 5000);
        floorB = floorParam.getValuef() * 100 * (0.6f + 0.4f * breath);
    }

    private void onHatAudio() {
        int count = (int) (hatsParam.getValuef() * audio.hatStrength * n * 0.03f);
        for (int j = 0; j < count; j++) {
            sparkle[(int) random(n)] = 1;
        }
    }

    private void onKickAudio(float kickAmt, float baseHue) {
        if (modeParam.getValuei() == 2 && rings.size() < MAX_RINGS) {
            Ring ring = new Ring();
            if (audio.kickCount % 2 == 0 || n == 0) {
                ring.cx = 0.5f; ring.cy = 0.5f; ring.cz = 0.5f;
            } else {
                int i = (int) random(n);
                ring.cx = pXn[i]; ring.cy = pYn[i]; ring.cz = pZn[i];
            }
            ring.r = 0;
            ring.strength = kickAmt * audio.kickStrength;
            ring.hue = baseHue;
            ring.inward = false;
            rings.add(ring);
        }
        if (audio.kickCount % 4 == 0) chaseDir = -chaseDir;
    }

    private void onClapAudio(float clapAmt, float accentHue) {
        if (modeParam.getValuei() == 2 && rings.size() < MAX_RINGS) {
            Ring ring = new Ring();
            ring.cx = 0.5f; ring.cy = 0.5f; ring.cz = 0.5f;
            ring.r = 1.4f;
            ring.strength = clapAmt * audio.snareStrength;
            ring.hue = accentHue;
            ring.inward = true;
            rings.add(ring);
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
        float bass = audio.bass, mid = audio.mid, presence = audio.presence;

        if (audio.hat) onHatAudio();
        if (audio.kick) onKickAudio(kickParam.getValuef(), baseHue);
        if (audio.snare) onClapAudio(clapParam.getValuef(), accentHue);

        int mode = ((modeParam.getValuei() % 4) + 4) % 4;
        switch (mode) {
            case 0: renderPump(baseHue, accentHue, sat, spread, size, bass); break;
            case 1: renderSplit(baseHue, accentHue, sat, spread, mid, bass); break;
            case 2: renderRings(deltaMs, baseHue, sat, spread, size, speed, bass); break;
            case 3: renderChase(deltaMs, baseHue, accentHue, sat, spread, size, speed, bass, presence); break;
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

    private void renderPump(float baseHue, float accentHue, float sat, float spread,
                            float size, float bass) {
        float kickAmt = kickParam.getValuef();
        float r = 1 - audio.kickEnv;
        float w = 0.05f + 0.15f * size;
        float inv2w2 = 1f / (2 * w * w);
        float wave = kickAmt * lastKickStrength * 110 * clamp01(audio.kickEnv * 3);
        for (int i = 0; i < n; i++) {
            float d = pDist[i];
            float b = floorB + bass * 70 * (1 - 0.5f * d);
            float h = baseHue + d * spread;
            float s = sat;
            float dr = d - r;
            b += wave * (float) Math.exp(-dr * dr * inv2w2);
            b += kickEnv * 35;
            b = lerp(b, 100, clapFlash);
            h = hueLerp(h, accentHue, clapFlash);
            s = sat * (1 - 0.7f * clapFlash);
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }

    private void renderSplit(float baseHue, float accentHue, float sat, float spread,
                             float mid, float bass) {
        float splitY = 0.5f;
        for (int i = 0; i < n; i++) {
            float yn = pYn[i];
            float b, h, s;
            if (yn < splitY) {
                float fill = constrain(bass * 0.7f + kickEnv * 0.5f, 0, 1);
                float t = yn / splitY;
                float lit = constrain((fill - t) / 0.05f + 1, 0, 1);
                b = floorB + lit * (60 + 40 * kickEnv);
                h = baseHue + yn * spread;
                s = sat;
            } else {
                float t = (1 - yn) / (1 - splitY);
                float lit = t < (0.25f + 0.75f * clapEnv) ? 1 : 0.15f;
                b = floorB + clapEnv * 100 * lit;
                b = lerp(b, 100, clapFlash);
                h = accentHue;
                s = sat * (1 - 0.6f * clapFlash);
            }
            if (Math.abs(yn - splitY) < 0.03f) {
                b = Math.max(b, mid * 80);
                s = sat * 0.5f;
                h = baseHue + spread * 0.5f;
            }
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }

    private void renderRings(double deltaMs, float baseHue, float sat, float spread,
                             float size, float speed, float bass) {
        float dt = (float) deltaMs;
        float grow = dt * (0.0006f + speed * 0.003f);
        float strDecay = (float) Math.exp(-dt / 700);
        for (int i = rings.size() - 1; i >= 0; i--) {
            Ring ring = rings.get(i);
            ring.r += ring.inward ? -grow * 1.5f : grow;
            ring.strength *= strDecay;
            if ((ring.inward && ring.r < 0) || (!ring.inward && ring.r > 1.5f)
                || ring.strength < 0.03f) {
                rings.remove(i);
            }
        }
        float w = 0.04f + 0.12f * size;
        float inv2w2 = 1f / (2 * w * w);
        for (int i = 0; i < n; i++) {
            float d = pDist[i];
            float b = floorB + bass * 20 * (1 - d);
            float h = baseHue + d * spread;
            float s = sat;
            float best = 0; float bestHue = h; boolean bestInward = false;
            for (Ring ring : rings) {
                float dx = pXn[i] - ring.cx, dy = pYn[i] - ring.cy, dz = pZn[i] - ring.cz;
                float dd = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) * INV_HALF_DIAG;
                float dr = dd - ring.r;
                float g = ring.strength * (float) Math.exp(-dr * dr * inv2w2);
                b += g * 100;
                if (g > best) { best = g; bestHue = ring.hue; bestInward = ring.inward; }
            }
            if (best > 0) {
                h = bestHue;
                s = bestInward ? sat * 0.6f : sat;
            }
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }

    private void renderChase(double deltaMs, float baseHue, float accentHue, float sat,
                             float spread, float size, float speed, float bass, float presence) {
        if (audio.tempoConfidence > 0.3f && presence > 0.25f) {
            chasePos = chaseDir > 0 ? audio.beatPhase : 1 - audio.beatPhase;
        } else {
            chasePos += (float) deltaMs * (0.0005f + speed * 0.002f) * chaseDir;
            chasePos -= (float) Math.floor(chasePos);
            if (chasePos < 0) chasePos += 1f;
        }
        int seg = 2 + Math.round(size * 10);
        float bw = 0.04f + 0.1f * size;
        float inv2bw2 = 1f / (2 * bw * bw);
        for (int i = 0; i < n; i++) {
            float xn = pXn[i];
            int k = Math.min(seg - 1, (int) (xn * seg));
            boolean on = ((k + audio.kickCount) % 2 == 0);
            float b = floorB + (on ? bass * 30 : 0);
            float h = baseHue + k * spread / seg;
            float s = sat;
            float dx = Math.abs(xn - chasePos);
            dx = Math.min(dx, 1 - dx);
            float bar = (float) Math.exp(-dx * dx * inv2bw2);
            b += bar * (60 + 40 * kickEnv);
            s = lerp(s, sat * 0.5f, bar * kickEnv);
            b = lerp(b, 100 - Math.min(b, 100), clapFlash);
            h = hueLerp(h, accentHue, clapFlash);
            bufH[i] = h; bufS[i] = s; bufB[i] = b;
        }
    }
}
