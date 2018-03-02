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
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXCompoundModulation;

import static com.symmetrylabs.slstudio.util.MathUtils.random;
import static com.symmetrylabs.util.DistanceConstants.*;

public class ExplosionsTriggerable extends LXPattern {
        // Used to store info about each explosion.
        // See com.symmetrylabs.slstudio.pattern.explosions.L8onUtil.pde for the definition.
        private List<L8onExplosion> explosions = new ArrayList<L8onExplosion>();
        private final SinLFO saturationModulator = new SinLFO(80.0, 100.0, 200000);
        public BooleanParameter triggerExplosion = new BooleanParameter("trigger", false);
        private BoundedParameter brightnessParameter = new BoundedParameter("BRGT", 50, 10, 80);

        private static final double GAIN_DEFAULT = 6;
        private static final double MODULATION_RANGE = 1;

        private BandGate audioModulatorFull;
        private CompoundParameter rateParameter = new CompoundParameter("RATE", 8000.0, 15000.0, 750.0);

        private BoundedParameter blurParameter = new BoundedParameter("BLUR", 0.69);
        private L8onBlurLayer blurLayer = new L8onBlurLayer(lx, this, blurParameter);

        private Random pointRandom = new Random();

        private L8onAudioBeatGate beatGate = new L8onAudioBeatGate("XBEAT", lx);
        private L8onAudioClapGate clapGate = new L8onAudioClapGate("XCLAP", lx);

        public ExplosionsTriggerable(LX lx) {
                super(lx);

                addParameter(brightnessParameter);

                //createAudioModulator();
                //modulateRateParam();

                addParameter(rateParameter);
                addParameter(blurParameter);

                addLayer(blurLayer);

                addModulator(saturationModulator).start();
                addModulator(beatGate).start();
                addModulator(clapGate).start();

                addParameter(triggerExplosion);
                triggerExplosion.setMode(BooleanParameter.Mode.MOMENTARY);
                triggerExplosion.addListener(new LXParameterListener() {
                        public void onParameterChanged(LXParameter param) {
                                if (((BooleanParameter)param).isOn()) {
                                        createExplosion();
                                }
                        }
                });
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
                this.explosions.parallelStream().forEach(explosion -> {

                        float base_hue = palette.getHuef();
                        float wave_hue_diff = (float) (360.0 / this.explosions.size());

                        explosion.hue_value = (float)(base_hue % 360.0);
                        base_hue += wave_hue_diff;

                        if (!explosion.hasExploded()) {
                                explosion.explode();
                        } else if (explosion.isFinished()) {
                                // int index = explosions.indexOf(explosion);
                                // removeModulator(this.explosions.get(index).radius_modulator);
                                // explosions.remove(explosion);
                        }
                });

                model.getPoints().parallelStream().forEach(p -> {

                        int c;
                        float hue_value = 0.0f;
                        float sat_value = saturationModulator.getValuef();
                        float brightness_value = brightnessParameter.getValuef();

                        int num_explosions_in = 0;

                        for(L8onExplosion explosion : this.explosions) {
                                if(explosion.onExplosion(p.x, p.y, p.z)) {
            if (!explosion.isFinished()) {
                                            num_explosions_in++;
                                            hue_value = L8onUtil.natural_hue_blend(explosion.hue_value, hue_value, num_explosions_in);
            }
                                }
                        }

                        if(num_explosions_in > 0) {
                                c = LX.hsb(hue_value, sat_value, brightness_value);
                        } else {
                                c = colors[p.index];
                                c = LX.hsb(LXColor.h(c), LXColor.s(c), 0.0f);
                        }

                        colors[p.index] = c;
                });
        }

        private void createExplosion() {
                float stroke_width = this.new_stroke_width();
                QuadraticEnvelope new_radius_env = new QuadraticEnvelope(0.0, model.xRange, rateParameter);
                new_radius_env.setEase(QuadraticEnvelope.Ease.OUT);
                LXPoint new_center_point = model.points[pointRandom.nextInt(model.points.length)];
                addModulator(new_radius_env);
                BandGate explosionGate = (this.explosions.size() % 2 == 1) ? this.beatGate : this.clapGate;
                L8onExplosion explosion = new L8onExplosion(new_radius_env, explosionGate.gate, stroke_width, new_center_point.x, new_center_point.y, new_center_point.z);
                this.explosions.add(explosion);
                assignNewCenter(explosion);
        }

        private void assignNewCenter(L8onExplosion explosion) {
                float stroke_width = this.new_stroke_width();
                LXPoint new_center_point = model.points[pointRandom.nextInt(model.points.length)];
                float chill_time = (15.0f + random(15)) * 1000;
                QuadraticEnvelope new_radius_env = new QuadraticEnvelope(0.0, model.xRange, rateParameter);
                new_radius_env.setEase(QuadraticEnvelope.Ease.OUT);

                explosion.setCenter(new_center_point.x, new_center_point.y, new_center_point.z);
                addModulator(new_radius_env);
                explosion.setRadiusModulator(new_radius_env, stroke_width);
                explosion.setChillTime(chill_time);
        }

        public float new_stroke_width() {
                return 3 * INCHES + random(6 * INCHES);
        }
}
