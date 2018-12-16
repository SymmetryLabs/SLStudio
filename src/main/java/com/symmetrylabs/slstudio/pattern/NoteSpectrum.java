package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MusicUtils;
import com.symmetrylabs.util.Octahedron;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.LXAudioBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static heronarts.lx.PolyBuffer.Space.RGB16;

public class NoteSpectrum extends SLPattern<SLModel> {
    private final int STREAM_LENGTH = 100;
    private CompoundParameter gainParam = new CompoundParameter("gain", 4, 0, 10);
    private CompoundParameter floorParam = new CompoundParameter("floor", 12, 1, 64);
    private CompoundParameter brtParam = new CompoundParameter("brt", 1, 0, 2);
    private CompoundParameter fadeParam = new CompoundParameter("fade", 4, 1, 7);
    private CompoundParameter xSclParam = new CompoundParameter("xScl", 1, 0, 3);
    private CompoundParameter xOffParam = new CompoundParameter("xOff", 0, -model.xRange, model.xRange);

    private LXAudioBuffer audioBuffer;
    private float[] audioSamples;
    private ddf.minim.analysis.FFT fft = null;
    private long[] stream = new long[STREAM_LENGTH];

    private final int NOTE_MIN = MusicUtils.PITCH_C3;
    private final int NOTE_MAX = MusicUtils.PITCH_C7;
    private final int NUM_BANDS = NOTE_MAX + 1 - NOTE_MIN;
    private int[][] ranges;

    private float[] bands = new float[NUM_BANDS];
    private float[] values = new float[NUM_BANDS];
    private float[] lastValues = new float[NUM_BANDS];
    private float[] floors = new float[NUM_BANDS];
    private float[] ceilings = new float[NUM_BANDS];

    public NoteSpectrum(LX lx) {
        super(lx);

        addParameter(gainParam);
        addParameter(floorParam);
        addParameter(brtParam);
        addParameter(fadeParam);
        addParameter(xSclParam);
        addParameter(xOffParam);

        audioBuffer = lx.engine.audio.getInput().mix;
        audioSamples = new float[audioBuffer.bufferSize()];

        ranges = new int[NUM_BANDS][];
        for (int i = 0; i < NUM_BANDS; i++) {
            int p = NOTE_MIN + i;
            ranges[i] = new int[] {
                (int) Math.round(MusicUtils.pitchToHertz(p - 0.5)),
                (int) Math.round(MusicUtils.pitchToHertz(p + 0.5))
            };
        }

        fft = new ddf.minim.analysis.FFT(audioBuffer.bufferSize(), audioBuffer.sampleRate());
        fft.window(ddf.minim.analysis.FFT.HAMMING);
    }

    private CubeMarker makeBar(float x, float z, float minY, float maxY, float thickness, int color) {
        return new CubeMarker(
            new PVector(x - thickness/2, (minY + maxY)/2, z),
            new PVector(thickness/2, (maxY - minY)/2, 0),
            color
        );
    }
    public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        for (int i = 0; i < NUM_BANDS; i++) {
            markers.add(makeBar(i*10 - 100, -100, 0, bands[i], 8, 0xffff0080));
            markers.add(makeBar(i*10 - 100, -100, floors[i], ceilings[i], 8, 0xff0000ff));
            markers.add(makeBar(i*10 - 100, -150, 0, values[i], 8, 0xffffffff));
        }
        for (int i = 0; i < audioBuffer.bufferSize(); i++) {
            markers.add(makeBar(i, -100, 0, audioSamples[i]*100, 0, 0xff804000));
        }
        for (int i = 0; i < STREAM_LENGTH; i++) {
            markers.add(new Octahedron(new PVector(i*4 + 2, 0, -150), 2, Spaces.rgb16ToRgb8(stream[i])));
        }
        return markers;
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        readBands();
        normalizeBands();
        flowStream();

        long[] colors = (long[]) getArray(RGB16);
        float xScale = xSclParam.getValuef();
        float xOffset = xOffParam.getValuef();
        for (LXVector v : getVectors()) {
            float x = Math.abs(v.x - xOffset - model.cx) / (model.xRange / 2);
            float pos = (x / xScale) * STREAM_LENGTH;
            int index = (int) pos;
            double frac = pos - index;
            long left = index >= STREAM_LENGTH ? Ops16.BLACK : stream[index];
            long right = index + 1 >= STREAM_LENGTH ? Ops16.BLACK : stream[index + 1];
            colors[v.index] = Ops16.blend(left, right, frac);
        }
        markModified(RGB16);
    }

    private void readBands() {
        audioBuffer.getSamples(audioSamples);
        fft.forward(audioSamples);

        float gain = gainParam.getValuef();
        for (int b = 0; b < NUM_BANDS; b++) {
            int[] range = ranges[b];
            bands[b] = fft.calcAvg(range[0], range[1]) * gain;
        }
        bands[0] = Math.abs(bands[1] - bands[2]);
    }

    private void normalizeBands() {
        float FLOOR_FALL_RATE = 1/32f;
        float FLOOR_RISE_RATE = 1/128f;
        float FLOOR_NOISE_MARGIN = 1/16f;

        float MIN_CEILING = floorParam.getValuef();
        float CEILING_FALL_RATE = 1/256f;
        float CEILING_RISE_RATE = 1/256f; //1/32f;
        float CEILING_HEADROOM = 1/256f;

        for (int b = 0; b < NUM_BANDS; b++) {
            lastValues[b] = values[b];
            floors[b] = lerp(floors[b], bands[b], bands[b] < floors[b] ? FLOOR_FALL_RATE : FLOOR_RISE_RATE);
            ceilings[b] = lerp(ceilings[b], bands[b], bands[b] < ceilings[b] ? CEILING_FALL_RATE : CEILING_RISE_RATE);
            float floor = floors[b] * (1 + FLOOR_NOISE_MARGIN);
            float ceiling = ceilings[b] * (1 + CEILING_HEADROOM);
            float minCeiling = floor + MIN_CEILING;
            if (ceilings[b] < minCeiling) ceiling = ceilings[b] = minCeiling;
            values[b] = bands[b] > floor ? bands[b] - floor : 0;
            values[b] = values[b] * 64 / ceiling;
        }
    }

    private void flowStream() {
        float FADE_RATE = 1f/fadeParam.getValuef(); // 0.1f; //1/256f;
        float MIN_VALUE = 3;
        float[] impulseWeights = new float[] {
            1, 0.99f, 0.96f, 0.91f, 0.84f, 0.75f, 0.64f, 0.51f, 0.36f, 0.19f
        };
        float brightness = brtParam.getValuef();

        stream[0] = Ops16.BLACK;
        for (int i = STREAM_LENGTH - 1; i > 0; i--) {
            stream[i] = Ops16.blend(stream[i - 1], Ops16.blend(stream[i], Ops16.BLACK, 0.1), FADE_RATE);
        }
        float v12 = Math.max(values[1], values[2]);
        float v34 = Math.max(values[3], values[4]);
        float v56 = Math.max(values[5], values[6]);
        int r = v12 > MIN_VALUE ? (int) (65535 * ((v12 - MIN_VALUE) / 128f) * brightness + 0.5) : 0;
        int g = v34 > MIN_VALUE ? (int) (65535 * ((v34 - MIN_VALUE) / 128f) * brightness + 0.5) : 0;
        int b = v56 > MIN_VALUE ? (int) (65535 * ((v56 - MIN_VALUE) / 128f) * brightness + 0.5) : 0;
        long impulse = Ops16.rgba(r, g, b, 65535);
        int value = (r + g + b) / 3;
        for (int i = 0; i < 12; i++) {
            long s = stream[i];
            long currentValue = (Ops16.red(s) + Ops16.green(s) + Ops16.blue(s)) / 3;
            if (value > currentValue) {
                stream[i] = Ops16.blend(stream[i], impulse, impulseWeights[i/2]);
            }
        }
    }

    private float lerp(float start, float stop, float fraction) {
        return start + (stop - start) * fraction;
    }
}
