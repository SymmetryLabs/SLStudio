package com.symmetrylabs.slstudio.pattern.explosions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.BandGate;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.QuadraticEnvelope;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.random;
import static com.symmetrylabs.util.DistanceConstants.*;

public class Explosions extends LXPattern {
    // Used to store info about each explosion.
    // See com.symmetrylabs.slstudio.pattern.explosions.L8onUtil.pde for the definition.
    private List<L8onExplosion> explosions = new ArrayList<L8onExplosion>();
    private final SinLFO saturationModulator = new SinLFO(80.0, 100.0, 200000);
    private CompoundParameter numExplosionsParameter = new CompoundParameter("Number", 4.0, 1.0, 20.0);
    private BoundedParameter brightnessParameter = new BoundedParameter("Bright", 50, 10, 80);

    private static final double GAIN_DEFAULT = 6;
    private static final double MODULATION_RANGE = 1;

    private BandGate audioModulatorFull;
    private CompoundParameter rateParameter = new CompoundParameter("Rate", 8000.0, 8000.0, 750.0);

    private BoundedParameter blurParameter = new BoundedParameter("Blur", 0.69);
    private L8onBlurLayer blurLayer = new L8onBlurLayer(lx, this, blurParameter);

    private Random pointRandom = new Random();

    private L8onAudioBeatGate beatGate = new L8onAudioBeatGate("xBeat", lx);
    private L8onAudioClapGate clapGate = new L8onAudioClapGate("xClap", lx);

    public Explosions(LX lx) {
        super(lx);

        addParameter(numExplosionsParameter);
        addParameter(brightnessParameter);

        createAudioModulator();
        modulateRateParam();

        addParameter(rateParameter);
        addParameter(blurParameter);

        addLayer(blurLayer);

        addModulator(saturationModulator).start();
        addModulator(beatGate).start();
        addModulator(clapGate).start();

        initExplosions();
    }

    private void createAudioModulator() {
        this.audioModulatorFull = new BandGate("Full", this.lx);
        addModulator(this.audioModulatorFull);
        this.audioModulatorFull.threshold.setValue(1);
        this.audioModulatorFull.floor.setValue(0);
        this.audioModulatorFull.gain.setValue(GAIN_DEFAULT);

        this.audioModulatorFull.maxFreq.setValue(this.audioModulatorFull.maxFreq.range.max);
        this.audioModulatorFull.minFreq.setValue(0);

        this.audioModulatorFull.start();
    }

    private void modulateRateParam() {
        LXCompoundModulation compoundModulation = new LXCompoundModulation(audioModulatorFull.average, rateParameter);
        compoundModulation.range.setValue(MODULATION_RANGE);
    }

    public void run(double deltaMs) {
        initExplosions();

        float base_hue = lx.palette.getHuef();
        float wave_hue_diff = (float) (360.0 / this.explosions.size());

        for(L8onExplosion explosion : this.explosions) {
            if (explosion.isChillin((float)deltaMs)) {
                continue;
            }

            explosion.hue_value = (float)(base_hue % 360.0);
            base_hue += wave_hue_diff;

            if (!explosion.hasExploded()) {
                explosion.explode();
            } else if (explosion.isFinished()) {
                assignNewCenter(explosion);
            }
        }

        int c;
        float hue_value = 0.0f;
        float sat_value = saturationModulator.getValuef();
        float brightness_value = brightnessParameter.getValuef();

        for (LXVector v : getVectors()) {
            int num_explosions_in = 0;

            for(L8onExplosion explosion : this.explosions) {
                if(explosion.isChillin(0)) {
                    continue;
                }

                if(explosion.onExplosion(v.x, v.y, v.z)) {
                    num_explosions_in++;
                    hue_value = L8onUtil.natural_hue_blend(explosion.hue_value, hue_value, num_explosions_in);
                }
            }

            if(num_explosions_in > 0) {
                c = LX.hsb(hue_value, sat_value, brightness_value);
            } else {
                c = colors[v.index];
                c = LX.hsb(LXColor.h(c), LXColor.s(c), 0.0f);
            }

            colors[v.index] = c;
        }
    }

    private void initExplosions() {
        int num_explosions = (int) numExplosionsParameter.getValue();

        if (this.explosions.size() == num_explosions) {
            return;
        }

        if (this.explosions.size() < num_explosions) {
            for(int i = 0; i < (num_explosions - this.explosions.size()); i++) {
                float stroke_width = this.new_stroke_width();
                QuadraticEnvelope new_radius_env = new QuadraticEnvelope(0.0, model.xRange, rateParameter);
                new_radius_env.setEase(QuadraticEnvelope.Ease.OUT);
                List<LXVector> vectors = getVectorList();
                LXVector center = vectors.get(pointRandom.nextInt(vectors.size()));
                addModulator(new_radius_env);
                BandGate explosionGate = (this.explosions.size() % 2 == 1) ? this.beatGate : this.clapGate;
                this.explosions.add(
                        new L8onExplosion(new_radius_env, explosionGate.gate, stroke_width, center.x, center.y, center.z)
                );
            }
        } else {
            for(int i = (this.explosions.size() - 1); i >= num_explosions; i--) {
                removeModulator(this.explosions.get(i).radius_modulator);
                this.explosions.remove(i);
            }
        }
    }

    private void assignNewCenter(L8onExplosion explosion) {
        float stroke_width = this.new_stroke_width();
        List<LXVector> vectors = getVectorList();
        LXVector center = vectors.get(pointRandom.nextInt(vectors.size()));
        float chill_time = (15.0f + random(15)) * 1000;
        QuadraticEnvelope new_radius_env = new QuadraticEnvelope(0.0, model.xRange, rateParameter);
        new_radius_env.setEase(QuadraticEnvelope.Ease.OUT);

        explosion.setCenter(center.x, center.y, center.z);
        addModulator(new_radius_env);
        explosion.setRadiusModulator(new_radius_env, stroke_width);
        explosion.setChillTime(chill_time);
    }

    public float new_stroke_width() {
        return 3 * INCHES + random(6 * INCHES);
    }
}
