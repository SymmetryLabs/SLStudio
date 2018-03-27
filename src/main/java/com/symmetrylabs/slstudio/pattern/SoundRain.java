package com.symmetrylabs.slstudio.pattern;

import java.lang.Math;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.audio.LXAudioBuffer;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.LinearEnvelope;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;

public class SoundRain extends SLPattern<CubesModel> {
    private LXAudioBuffer audioBuffer;
    private float[] audioSamples;
    private ddf.minim.analysis.FFT fft = null;
    private LinearEnvelope[] bandVals = null;
    private float[] lightVals = null;
    private int avgSize;
    private float gain = 25;

    public final SawLFO pos = new SawLFO(0, 9, 8000);
    public final SinLFO col1 = new SinLFO(model.xMin, model.xMax, 5000);
    public final CompoundParameter gainParameter = new CompoundParameter("GAIN", 0.1, 0, .3);

    public SoundRain(LX lx) {
        super(lx);
        audioBuffer = lx.engine.audio.getInput().mix;
        audioSamples = new float[audioBuffer.bufferSize()];
        addModulator(pos).trigger();
        addModulator(col1).trigger();
        addParameter(gainParameter);
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
            float lv = Math.min(value * gain, 100);

            if (lv > lightVals[i]) {
                lightVals[i] = Math.min(Math.min(lightVals[i] + 15, lv), 100);
            } else {
                lightVals[i] = Math.max(Math.max(lv, lightVals[i] - 5), 0);
            }
        }

        for (CubesModel.Cube c : model.getCubes()) {
            for (int j = 0; j < c.getStrips().size(); j++) {
                Strip s = c.getStrips().get(j);

                if (j % 4 != 0 && j % 4 != 2) {
                    for (LXPoint p : s.points) {
                        int seq = ((int)(p.y * avgSize / model.yMax + pos.getValuef() + Math.sin(p.x + p.z) * 2)) % avgSize;
                        seq = Math.min(Math.abs(seq - (avgSize / 2)), avgSize - 1);
                        colors[p.index] = lx.hsb(200, Math.max(0, 100 - Math.abs(p.x - col1.getValuef()) / 2), lightVals[seq]);
                    }
                }
            }
        }
    }
}
