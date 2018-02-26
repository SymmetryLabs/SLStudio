package com.symmetrylabs.slstudio.pattern.explosions;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.BandGate;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;

import static heronarts.lx.LX.TWO_PI;

public class SunriseSunset extends LXPattern {
        BoundedParameter dayTime = new BoundedParameter("DAY", 24000, 10000, 240000);
        LXProjection projection = new LXProjection(model);
        SawLFO sunPosition = new SawLFO(0, TWO_PI, dayTime);

        CompoundParameter sunRadius = new CompoundParameter("RAD", 1, 1, 24);
        CompoundParameter colorSpread = new CompoundParameter("CLR", 0.65, 0.65, 1);

        private static final double GAIN_DEFAULT = 6;
        private static final double MODULATION_RANGE = 1;

        private BandGate audioModulatorLow;
        private BandGate audioModulatorMid;
        private BoundedParameter blurParameter = new BoundedParameter("BLUR", 0.69);
        private L8onBlurLayer blurLayer = new L8onBlurLayer(lx, this, blurParameter);

        private BoundedParameter yMinParam = new BoundedParameter("YMIN", 465, 400, model.yMax);

        public SunriseSunset(LX lx) {
                super(lx);
                addModulator(sunPosition).start();

                addParameter(blurParameter);
                addLayer(blurLayer);

                addParameter(dayTime);
                addParameter(yMinParam);
                addParameter(sunRadius);
                addParameter(colorSpread);

                this.createAudioModulators();
        }


        private void createAudioModulators() {
                this.createLowAudioModulator();
                this.createMidAudioModulator();
        }

        private void createLowAudioModulator() {
                this.audioModulatorLow = new BandGate("Low", this.lx);
                addModulator(this.audioModulatorLow);
                this.audioModulatorLow.threshold.setValue(1);
                this.audioModulatorLow.floor.setValue(0);
                this.audioModulatorLow.gain.setValue(GAIN_DEFAULT);

                this.audioModulatorLow.maxFreq.setValue(216);
                this.audioModulatorLow.minFreq.setValue(0);

                this.audioModulatorLow.start();

                LXCompoundModulation compoundModulationLow = new LXCompoundModulation(audioModulatorLow.average, sunRadius);
                compoundModulationLow.range.setValue(MODULATION_RANGE);
        }

        private void createMidAudioModulator() {
                this.audioModulatorMid = new BandGate("Mid", this.lx);
                addModulator(this.audioModulatorMid);
                this.audioModulatorMid.threshold.setValue(1);
                this.audioModulatorMid.floor.setValue(0);
                this.audioModulatorMid.gain.setValue(GAIN_DEFAULT);

                this.audioModulatorMid.maxFreq.setValue(2200);
                this.audioModulatorMid.minFreq.setValue(216);

                this.audioModulatorMid.start();

                LXCompoundModulation compoundModulationMid = new LXCompoundModulation(audioModulatorMid.average, colorSpread);
                compoundModulationMid.range.setValue(MODULATION_RANGE);
        }

        public void run(double deltaMs) {
                projection.reset();
                projection.rotateZ(sunPosition.getValuef());

                int i = 0;
                for(LXVector v: projection) {
                        if (model.yMax - v.y < sunRadius.getValuef()) {
                                setColor(i, LX.hsb(0, 0, 100));
                        } else if(v.y > yMinParam.getValuef()) {
                                float yn = (v.y - yMinParam.getValuef()) / model.yRange;
                                float hue = (350 + ((360 * colorSpread.getValuef() * yn))) % 360;
                                setColor(i, LX.hsb(hue, 100, 100 * yn));
                        } else {
                                setColor(i, 0);
                        }
                        i++;
                }
        }
}
