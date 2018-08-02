package com.symmetrylabs.slstudio.pattern;

import java.lang.Math;

import ddf.minim.analysis.FFT;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.audio.LXAudioBuffer;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.LXUtils;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.transform.LXVector;

public class SoundSpikes extends SLPattern<CubesModel> {
    private LXAudioBuffer audioBuffer;
    private float[] audioSamples;
    private ddf.minim.analysis.FFT fft = null;
    private LinearEnvelope[] bandVals = null;
    private float[] lightVals = null;
    private int avgSize;
    private float gain = 25;

    final CompoundParameter gainParameter = new CompoundParameter("Gain", 0.5);
    final CompoundParameter hueVariance = new CompoundParameter("HueVar");
    final SawLFO pos = new SawLFO(0, model.xMax, 8000);

    public SoundSpikes(LX lx) {
        super(lx);
        audioBuffer = lx.engine.audio.getInput().mix;
        audioSamples = new float[audioBuffer.bufferSize()];
        addParameter(gainParameter);
        addParameter(hueVariance);
        addModulator(pos).trigger();
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == gainParameter) {
            gain = 50 * parameter.getValuef();
        }
    }

    public void onActive() {
        if (this.fft == null) {
            this.fft = new ddf.minim.analysis.FFT(audioBuffer.bufferSize(), audioBuffer.sampleRate());
            this.fft.window(ddf.minim.analysis.FFT.HAMMING);
            this.fft.logAverages(40, 1);
            this.avgSize = this.fft.avgSize();
            this.bandVals = new LinearEnvelope[this.avgSize];

            for (int i = 0; i < this.bandVals.length; ++i) {
                this.addModulator(this.bandVals[i] = (new LinearEnvelope(0, 0, 700 + i * 4))).trigger();
            }

            lightVals = new float[avgSize];
        }
    }

    public void run(double deltaMs) {
        audioBuffer.getSamples(audioSamples);
        this.fft.forward(audioSamples);

        for (int i = 0; i < avgSize; ++i) {
            float value = this.fft.getAvg(i);
            this.bandVals[i].setRangeFromHereTo(value, 40).trigger();
            float lv = Math.min(value * gain, model.yMax + 10);

            if (lv > lightVals[i]) {
                lightVals[i] = Math.min(Math.min(lightVals[i] + 60, lv), model.yMax - 10);
            } else {
                lightVals[i] = Math.max(Math.max(lv, lightVals[i] - 10), 0);
            }
        }

        int i = 0;
        for (CubesModel.Cube c : ((CubesModel)model).getCubes()) {
            for (int j = 0; j < c.getStrips().size(); j++) {
                Strip s = c.getStrips().get(j);

                if (j % 4 != 0 && j % 4 != 2) {
                    for (LXVector v : getVectors(s.points)) {
                        float dis = (Math.abs(v.x - model.xMax / 2) + pos.getValuef()) % model.xRange / 2;
                        int seq = (int)((dis * avgSize * 2) / model.xRange);
                        if (seq > avgSize) seq = avgSize - seq;
                        seq = LXUtils.constrain(seq, 0, avgSize - 1);
                        float br = Math.max(0, lightVals[seq] - v.y);
                        colors[v.index] = lx.hsb(
                            ((dis * avgSize) * hueVariance.getValuef()) / model.xRange + palette.getHuef(),
                            palette.getSaturationf(),
                            br);
                    }
                }
            }
        }
    }
}
