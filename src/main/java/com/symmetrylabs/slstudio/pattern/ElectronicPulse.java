package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.pattern.cubes.CubeBeatsAudio;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Arrays;
import java.util.Random;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

/** Electronic music: kick shockwaves, sustained bass ribbons, and crisp hat sparks.
 * Frequency/onset separation is approximate; this is not instrument isolation.
 */
public class ElectronicPulse extends SLPattern<SLModel> {
    public final CompoundParameter gain = new CompoundParameter("Gain", 0.5);
    public final CompoundParameter sensitivity = new CompoundParameter("Sens", 0.5);
    public final CompoundParameter kick = new CompoundParameter("Kick", 0.85);
    public final CompoundParameter bass = new CompoundParameter("Bass", 0.6);
    public final CompoundParameter hats = new CompoundParameter("Hats", 0.7);
    public final CompoundParameter kickDecay = new CompoundParameter("Kick Decay", 260, 80, 700);
    public final CompoundParameter bassRelease = new CompoundParameter("Bass Tail", 220, 60, 800);
    public final CompoundParameter hatDecay = new CompoundParameter("Hat Decay", 65, 20, 180);
    public final CompoundParameter density = new CompoundParameter("Hat Density", 0.08, 0.01, 0.3);
    public final CompoundParameter speed = new CompoundParameter("Speed", 1, 0.25, 3);
    public final CompoundParameter hue = new CompoundParameter("Hue", 0, -180, 180);
    public final CompoundParameter spread = new CompoundParameter("Spread", 140, 0, 300);
    private final GraphicMeter eq;
    private final CubeBeatsAudio audio = new CubeBeatsAudio();
    private final float[] bands = new float[CubeBeatsAudio.NUM_BANDS];
    private final Random random = new Random();
    private SLModel cachedModel;
    private float[] radius = new float[0], height = new float[0], sparks = new float[0];
    private final float[] waveAge = new float[8], waveStrength = new float[8];
    private int nextWave;
    private float punch, motion;

    public ElectronicPulse(LX lx) {
        super(lx);
        addParameter(gain); addParameter(sensitivity); addParameter(kick);
        addParameter(bass); addParameter(hats); addParameter(kickDecay);
        addParameter(bassRelease); addParameter(hatDecay); addParameter(density);
        addParameter(speed); addParameter(hue); addParameter(spread);
        eq = new GraphicMeter("ElectronicPulse", lx.engine.audio.input.mix,
            CubeBeatsAudio.NUM_BANDS);
        CubeBeatsAudio.configureMeter(eq);
        eq.range.setNormalized(0.4);
        addModulator(eq).start();
    }

    private static float clamp(float value) {
        return Float.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0;
    }

    private void cacheGeometry() {
        if (cachedModel == model && radius.length == model.points.length) return;
        cachedModel = model;
        int n = model.points.length;
        radius = new float[n]; height = new float[n]; sparks = new float[n];
        Arrays.fill(waveStrength, 0);
        punch = 0;
        float extent = Math.max(model.xRange, Math.max(model.yRange, model.zRange));
        extent = Math.max(extent, 0.001f);
        float maxRadius = 0;
        for (int i = 0; i < n; i++) {
            LXPoint p = model.points[i];
            float x = (p.x - model.cx) / extent;
            float y = (p.y - model.cy) / extent;
            float z = (p.z - model.cz) / extent;
            radius[i] = (float) Math.sqrt(x*x + y*y + z*z);
            maxRadius = Math.max(maxRadius, radius[i]);
            height[i] = model.yRange > 0 ? (p.y - model.yMin) / model.yRange : 0.5f;
        }
        for (int i = 0; i < n; i++) radius[i] /= Math.max(maxRadius, 0.001f);
    }

    @Override
    public void run(double deltaMs, PolyBuffer.Space space) {
        // Follow the studio source selector; do not change the shared meter settings.
        eq.setBuffer(lx.engine.audio.mode.getEnum() == LXAudioEngine.Mode.OUTPUT
            ? lx.engine.audio.output.mix : lx.engine.audio.input.mix);
        eq.gain.setNormalized(gain.getValue());
        for (int i = 0; i < bands.length; i++) {
            bands[i] = lx.engine.audio.enabled.isOn() ? eq.getBandf(i) : 0;
        }
        renderFrame(deltaMs, bands);
    }

    // Package-private audio seam supports deterministic checks without audio hardware.
    void renderFrame(double deltaMs, float[] inputBands) {
        double dt = Double.isFinite(deltaMs) && deltaMs > 0 ? deltaMs : 0.001;
        audio.sensitivity = sensitivity.getValuef();
        audio.attackMs = 12;
        audio.releaseMs = bassRelease.getValuef();
        audio.update(inputBands, dt);
        renderLayers(dt, audio.bass, audio.high,
            audio.kick ? audio.kickStrength : 0, audio.hat ? audio.hatStrength : 0);
    }

    void renderLayers(double deltaMs, float bassLevel, float highLevel,
                      float kickHit, float hatHit) {
        cacheGeometry();
        double dt = Double.isFinite(deltaMs) && deltaMs > 0 ? deltaMs : 0.001;
        bassLevel = clamp(bassLevel); highLevel = clamp(highLevel);
        kickHit = clamp(kickHit); hatHit = clamp(hatHit);
        punch *= (float) Math.exp(-dt / kickDecay.getValue());
        float sparkFade = (float) Math.exp(-dt / hatDecay.getValue());
        for (int i = 0; i < sparks.length; i++) sparks[i] *= sparkFade;
        for (int w = 0; w < waveAge.length; w++) waveAge[w] += dt;
        if (kickHit > 0) {
            punch = Math.max(punch, kickHit);
            waveAge[nextWave] = 0; waveStrength[nextWave] = kickHit;
            nextWave = (nextWave + 1) % waveAge.length;
        }
        if (hatHit > 0 && sparks.length > 0) {
            int count = Math.max(1, (int) (sparks.length * density.getValue() * hatHit));
            for (int j = 0; j < count; j++) sparks[random.nextInt(sparks.length)] = hatHit;
        }
        motion = (float) ((motion + dt * speed.getValue() / 2200) % 1);
        float baseHue = palette.getHuef() + hue.getValuef();
        float kickAmount = kick.getValuef(), bassAmount = bass.getValuef(), hatAmount = hats.getValuef();
        float duration = 600 / speed.getValuef();
        float duck = 1 - 0.65f * punch * kickAmount;
        int[] colors = (int[]) getArray(SRGB8);
        Arrays.fill(colors, LXColor.BLACK);
        for (int i = 0; i < model.points.length; i++) {
            float ribbon = 0.5f + 0.5f * (float) Math.sin(2 * Math.PI * (height[i] * 1.5f + radius[i] - motion));
            float b = bassLevel * bassAmount * duck * (0.18f + 0.65f * ribbon);
            float k = punch * 0.3f;
            for (int w = 0; w < waveAge.length; w++) {
                float progress = waveAge[w] / duration;
                if (progress < 1.25f) {
                    float ring = Math.max(0, 1 - Math.abs(radius[i] - progress) / 0.18f);
                    k += waveStrength[w] * ring * (float) Math.exp(-waveAge[w] / kickDecay.getValue());
                }
            }
            float h = sparks[i] * hatAmount * (0.8f + 0.2f * highLevel);
            int color = LXColor.hsb(baseHue + spread.getValuef() * ribbon, 95, 100 * clamp(b));
            color = LXColor.add(color, LXColor.hsb(baseHue + 25, 80, 100 * clamp(k * kickAmount)));
            color = LXColor.add(color, LXColor.hsb(baseHue + 180, 18, 100 * clamp(h)));
            int index = model.points[i].index;
            if (index >= 0 && index < colors.length) colors[index] = color;
        }
        markModified(SRGB8);
    }
}
