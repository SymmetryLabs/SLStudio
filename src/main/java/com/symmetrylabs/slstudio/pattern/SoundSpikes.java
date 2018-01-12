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

import com.symmetrylabs.slstudio.model.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;

public class SoundSpikes extends SLPattern {
    private LXAudioBuffer audioBuffer;
    private float[] audioSamples;
    private ddf.minim.analysis.FFT fft = null; 
    private LinearEnvelope[] bandVals = null;
    private float[] lightVals = null;
    private int avgSize;
    private float gain = 25;

    public final CompoundParameter gainParameter = new CompoundParameter("GAIN", 0.5);
    public final CompoundParameter hueVariance = new CompoundParameter("HUEVAR");
    public final SawLFO pos = new SawLFO(0, model.xMax, 8000);

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
                    for (LXPoint p : s.points) {
                        float dis = (Math.abs(p.x - model.xMax / 2) + pos.getValuef()) % model.xMax / 2;
                        int seq = (int)((dis * avgSize * 2) / model.xMax);
                        if (seq > avgSize) seq = avgSize - seq;
                        seq = LXUtils.constrain(seq, 0, avgSize - 1);
                        float br = Math.max(0, lightVals[seq] - p.y);
                        colors[p.index] = lx.hsb(((dis * avgSize) * hueVariance.getValuef()) / model.xMax + palette.getHuef(), 90, br);
                    }
                }
            }
        }
    }  
}