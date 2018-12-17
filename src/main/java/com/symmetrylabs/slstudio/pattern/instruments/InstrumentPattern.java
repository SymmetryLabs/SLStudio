package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.MidiPolyphonicExpressionPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MusicUtils;
import com.symmetrylabs.util.OctreeModelIndex;
import com.symmetrylabs.util.SphereMarker;
import com.symmetrylabs.util.SphereWithArrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ddf.minim.analysis.FFT;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.Tempo;
import heronarts.lx.audio.LXAudioBuffer;
import heronarts.lx.audio.LXAudioInput;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.transform.LXVector;
import processing.core.PVector;

import static com.symmetrylabs.slstudio.pattern.instruments.Instrument.*;
import static heronarts.lx.PolyBuffer.Space.RGB16;

public class InstrumentPattern extends MidiPolyphonicExpressionPattern<SLModel>
    implements Tempo.Listener {
    public enum TriggerSource {
        BEAT,
        MIDI,
        AUDIO
    };

    private final CompoundParameter hueParam = new CompoundParameter("Hue", 0, -1, 1);
    private final CompoundParameter hueVarParam = new CompoundParameter("HueVar", 0, 0, 1);
    private final CompoundParameter satParam = new CompoundParameter("Sat", 0, 0, 1);
    private final CompoundParameter brtParam = new CompoundParameter("Brt", 1, 0, 1);

    private final DiscreteParameter instrParam = new DiscreteParameter(
        "Instr", InstrumentRegistry.getNames().toArray(new String[0]));
    private final CompoundParameter xParam = new CompoundParameter("X", model.cx, model.xMin, model.xMax);
    private final CompoundParameter yParam = new CompoundParameter("Y", model.cy, model.yMin, model.yMax);
    private final float radius = (float) Math.hypot(Math.hypot(model.xRange/2, model.yRange/2), model.zRange/2);
    private final CompoundParameter spreadParam = new CompoundParameter("Spread", -2, -10, 0);

    private final CompoundParameter sizeParam = new CompoundParameter("Size", -6, -10, 0);
    private final CompoundParameter sizeVarParam = new CompoundParameter("SizeVar", 0.5, 0, 1);
    private final CompoundParameter rateParam = new CompoundParameter("Rate", 0, -5, 5);
    private final CompoundParameter orientParam = new CompoundParameter("Orient", 0, -1, 1);

    private final EnumParameter<TriggerSource> sourceParam = new EnumParameter<>("Source", TriggerSource.BEAT);
    private final CompoundParameter floorParam = new CompoundParameter("Floor", 0.1f, 0, 1);
    private final CompoundParameter ceilingParam = new CompoundParameter("Ceiling", 0.3f, 0, 1);
    private final CompoundParameter attackThParam = new CompoundParameter("AttackTh", 0.4f, 0, 1);
    private final CompoundParameter releasThParam = new CompoundParameter("ReleasTh", 0.3f, 0, 1);

    private final CompoundParameter gainParam = new CompoundParameter("Gain", 0, -30, 30);  // decibels
    private final DiscreteParameter pitchLoParam = new DiscreteParameter("PitchLo", MusicUtils.PITCH_C1, 0, 127);
    private final DiscreteParameter pitchHiParam = new DiscreteParameter("PitchHi", MusicUtils.PITCH_C5, 0, 127);
    private final CompoundParameter falloffParam = new CompoundParameter("Falloff", 2, 0, 12);

    private final ParameterSet paramSet = new ParameterSet();
    private Instrument instrument;
    private Note[] notes;

    // Beat note trigger
    private Integer beat;
    private boolean measure;

    // MIDI note trigger

    // Audio note trigger
    private LXAudioBuffer audio;
    private float[] samples;
    private FFT fft;
    private float[] hertzLo;
    private float[] hertzHi;
    private float globalCeiling;
    private float[] bands;
    private float[] levels;
    private float[] floors;
    private float[] ceilings;
    private float[] lastValues;
    private float[] values;
    private float[] peaks;

    // Geometry
    private OctreeModelIndex index;

    public InstrumentPattern(LX lx) {
        super(lx);
        pitchLoParam.setFormatter(MusicUtils.MIDI_PITCH_FORMATTER);
        pitchHiParam.setFormatter(MusicUtils.MIDI_PITCH_FORMATTER);

        addParameter(hueParam);
        addParameter(hueVarParam);
        addParameter(satParam);
        addParameter(brtParam);

        addParameter(instrParam);
        addParameter(xParam);
        addParameter(yParam);
        addParameter(spreadParam);
        addParameter(sizeParam);
        addParameter(sizeVarParam);
        addParameter(rateParam);
        addParameter(orientParam);

        addParameter(sourceParam);
        addParameter(floorParam);
        addParameter(ceilingParam);
        addParameter(attackThParam);
        addParameter(releasThParam);

        addParameter(gainParam);
        addParameter(pitchLoParam);
        addParameter(pitchHiParam);
        addParameter(falloffParam);

        pitchLoParam.setFormatter(new MusicUtils.MidiPitchFormatter());
        pitchHiParam.setFormatter(new MusicUtils.MidiPitchFormatter());
        instrParam.addListener(param -> {
            instrument = null;  // will be reconstructed in run()
        });

        notes = setupNotes();
        sourceParam.addListener(param -> {
            notes = setupNotes();
        });

        initBeat();
        initMidi();
        initAudio();
    }

    @Override public void onVectorsChanged() {
        super.onVectorsChanged();
        index = new OctreeModelIndex(model, getVectors());
    }

    @Override public void onActive() {
        super.onActive();
        enableDisableAudio();
    }

    @Override public void onInactive() {
        enableDisableAudio();
        super.onInactive();
    }

    protected void initBeat() {
        beat = null;
        measure = false;
        lx.tempo.addListener(this);
    }

    protected void initMidi() {
    }

    protected Note[] setupNotes() {
        Note[] notes = new Note[MusicUtils.NUM_PITCHES];
        for (int p = 0; p < notes.length; p++) {
            notes[p] = new Note(false, false, 0);
        }
        return notes;
    }

    protected void initAudio() {
        hertzLo = new float[MusicUtils.NUM_PITCHES];
        hertzHi = new float[MusicUtils.NUM_PITCHES];
        for (int p = 0; p <= MusicUtils.MAX_PITCH; p++) {
            hertzLo[p] = (float) MusicUtils.pitchToHertz(p - 0.5);
            hertzHi[p] = (float) MusicUtils.pitchToHertz(p + 0.5);
        }
        bands = new float[MusicUtils.NUM_PITCHES];
        levels = new float[MusicUtils.NUM_PITCHES];
        floors = new float[MusicUtils.NUM_PITCHES];
        ceilings = new float[MusicUtils.NUM_PITCHES];
        values = new float[MusicUtils.NUM_PITCHES];
        peaks = new float[MusicUtils.NUM_PITCHES];
    }

    protected void enableDisableAudio() {
        if (getChannel() != null) {
            LXAudioInput input = getChannel().audioInput;
            if (isActive && sourceParam.getEnum() == TriggerSource.AUDIO) {
                input.open();
                input.start();
                audio = input.mix;
                samples = new float[audio.bufferSize()];
                fft = new FFT(audio.bufferSize(), audio.sampleRate());
                fft.window(FFT.HAMMING);
            } else {
                if (input.isOpen()) {
                    input.stop();
                }
                input.close();
                audio = null;
            }
        }
    }

    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        switch (sourceParam.getEnum()) {
            case BEAT:
                putBeatNotes(notes);
                break;
            case MIDI:
                putMidiNotes(notes);
                break;
            case AUDIO:
                putAudioNotes(notes);
                break;
        }

        long[] colors = (long[]) getPolyBuffer().getArray(RGB16);
        Arrays.fill(colors, 0);
        getPolyBuffer().markModified(RGB16);

        if (instrument == null) {
            instrument = InstrumentRegistry.getInstrument(instrParam.getOption());
        }
        instrument.run(model, paramSet, notes, deltaMs / 1000.0, getPolyBuffer());
    }

    protected void putBeatNotes(Note[] notes) {
        final int C3 = MusicUtils.PITCH_C3;
        final int C4 = MusicUtils.PITCH_C4;

        notes[C3].attack = false;
        notes[C3].sustain = false;
        notes[C3].intensity = 0;
        notes[C4].attack = false;
        notes[C4].sustain = false;
        notes[C4].intensity = 0;
        if (measure) {
            notes[MusicUtils.PITCH_C3].attack = true;
            notes[MusicUtils.PITCH_C3].sustain = true;
            notes[MusicUtils.PITCH_C3].intensity = 1;
        } else if (beat != null) {
            notes[MusicUtils.PITCH_C4].attack = true;
            notes[MusicUtils.PITCH_C4].sustain = true;
            notes[MusicUtils.PITCH_C4].intensity = 0.5;
        }
        measure = false;
        beat = null;
    }

    @Override public void onBeat(Tempo tempo, int beat) {
        this.beat = beat;
    }

    @Override public void onMeasure(Tempo tempo) {
        measure = true;
    }

    protected void putMidiNotes(Note[] notes) {
        for (int p = 0; p < notes.length; p++) {
            notes[p].attack = false;
        }
    }

    @Override public void noteOn(int pitch, double velocity) {
        notes[pitch].attack = true;
        notes[pitch].sustain = true;
        notes[pitch].intensity = velocity;
    }

    @Override public void notePressure(int pitch, double pressure) {
        notes[pitch].intensity = pressure;
    }

    @Override public void noteOff(int pitch) {
        notes[pitch].sustain = false;
        notes[pitch].intensity = 0;
    }

    protected void putAudioNotes(Note[] notes) {
        readAudioBands();
        normalizeAudioBands();
        detectAudioNotes(notes);
    }

    protected void readAudioBands() {
        if (audio != null) {
            audio.getSamples(samples);
            fft.forward(samples);
            int pitchLo = pitchLoParam.getValuei();
            int pitchHi = pitchHiParam.getValuei();
            float bandMax = 0;
            for (int pitch = 0; pitch <= MusicUtils.MAX_PITCH; pitch++) {
                bands[pitch] = fft.calcAvg(hertzLo[pitch], hertzHi[pitch]);
                if (pitch >= pitchLo && pitch <= pitchHi) {
                    bandMax = Math.max(bandMax, bands[pitch]);
                }
            }
            bandMax = Math.max(bandMax, 1);
            globalCeiling = Math.max(globalCeiling, bandMax / 4);
            globalCeiling = moveToward(globalCeiling, bandMax, 0.2f, 0.01f);
        }
    }

    private void normalizeAudioBands() {
        float FLOOR_FALL_RATE = 0.03f;
        float FLOOR_RISE_RATE = 0.008f;
        float FLOOR_NOISE_MARGIN = floorParam.getValuef();

        float MIN_CEILING = ceilingParam.getValuef();
        float CEILING_FALL_RATE = 0.004f;
        float CEILING_RISE_RATE = 0.05f;
        float CEILING_HEADROOM = 0.004f;

        float gain = (float) Math.pow(10, gainParam.getValuef()/20) / globalCeiling;

        lastValues = values;
        values = new float[values.length];
        for (int b = 0; b < bands.length; b++) {
            levels[b] = bands[b] * gain;
            floors[b] = moveToward(floors[b], levels[b], FLOOR_RISE_RATE, FLOOR_FALL_RATE);
            ceilings[b] = moveToward(ceilings[b], levels[b] * (1 + CEILING_HEADROOM), CEILING_RISE_RATE, CEILING_FALL_RATE);
            floors[b] = Math.max(floors[b], FLOOR_NOISE_MARGIN);
            ceilings[b] = Math.max(ceilings[b], floors[b] + MIN_CEILING);
            values[b] = Math.max(0, (levels[b] - floors[b]) / (ceilings[b] - floors[b]));
        }
    }

    private void detectAudioNotes(Note[] notes) {
        int pitchLo = pitchLoParam.getValuei();
        int pitchHi = pitchHiParam.getValuei();
        float attackTh = attackThParam.getValuef();
        float releasTh = releasThParam.getValuef();

        for (int p = 0; p < notes.length; p++) {
            notes[p].attack = false;
        }
        for (int p = pitchLo; p <= pitchHi; p++) {
            int prev = Math.max(p - 1, 0);
            int next = Math.min(p + 1, values.length - 1);
            if (values[p] > lastValues[p] + attackTh &&
                  values[p] > values[prev] && values[p] > values[next]) {
                notes[p].attack = true;
                notes[p].sustain = true;
                peaks[p] = values[0];
            }
            notes[p].intensity = values[p];
            if (notes[p].sustain) {
                peaks[p] = Math.max(peaks[p], (float) notes[p].intensity);
                if (notes[p].intensity < peaks[p] * releasTh) {
                    notes[p].sustain = false;
                    peaks[p] = 0;
                }
            }
        }
    }

    private float lerp(float start, float stop, float fraction) {
        return start + (stop - start) * fraction;
    }

    private float moveToward(float start, float goal, float upFrac, float downFrac) {
        return lerp(start, goal, goal > start ? upFrac : downFrac);
    }

    private CubeMarker makeBar(float x, float z, float minY, float maxY, float thickness, int color) {
        return new CubeMarker(
            new PVector(x - thickness/2, (minY + maxY)/2, z),
            new PVector(thickness/2, (maxY - minY)/2, 0),
            color
        );
    }

    @Override public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();

        float x = xParam.getValuef();
        float y = yParam.getValuef();
        float z = model.cz;

        LXVector vel = paramSet.getVelocity();
        float spread = (float) Math.pow(2, spreadParam.getValuef()) * radius;
        markers.add(new SphereWithArrow(
            new PVector(x, y, z), spread, 0xff404040,
            new PVector(vel.x, vel.y, vel.z), 0xffa0a0a0));

        if (sourceParam.getEnum() == TriggerSource.AUDIO) {
            float rawScale = 4;
            float yScale = 100;
            int pitchLo = pitchLoParam.getValuei();
            int pitchHi = pitchHiParam.getValuei();

            // z = 0: raw band energy values
            markers.add(new CubeMarker(
                new PVector(bands.length*10/2, rawScale*globalCeiling/2, 0),
                new PVector(bands.length*10/2, rawScale*globalCeiling/2, 0),
                0xff002000
            ));
            for (int i = 0; i < bands.length; i++) {
                boolean inRange = (i >= pitchLo && i <= pitchHi);
                markers.add(makeBar(i*10, 0, 0, bands[i]*rawScale, 8, inRange ? 0xff004000 : 0xff001000));

                // z = 100: scaled according to global energy levels
                markers.add(makeBar(i*10, 100, 0, levels[i]*yScale, 8, inRange ? 0xff008080 : 0xff002020));
                markers.add(makeBar(
                    i*10, 100, floors[i]*yScale, ceilings[i]*yScale, 8, inRange ? 0xff002040 : 0xff000810));

                // z = 200: normalized according to per-band floor and ceiling
                markers.add(makeBar(i*10, 200, 0, values[i]*yScale, 8, inRange ? 0xffc0c000 : 0xff303000));
            }
        }
        return markers;
    }

    @Override public String getCaption() {
        String result = "Notes:";
        for (int p = 0; p < notes.length; p++) {
            if (notes[p].sustain) {
                result += " " + MusicUtils.formatPitch(p);
                if (notes[p].attack) {
                    result += "*";
                }
            }
        }
        return result;
    }

    class ParameterSet implements Instrument.ParameterSet {
        public long getColor(double variation) {
            double hue = hueParam.getValue() + hueVarParam.getValue() * variation;
            double sat = satParam.getValue();
            double brt = brtParam.getValue();
            return Ops16.hsb(hue, sat, brt);
        }

        public LXVector getPosition(LXVector variation) {
            float x = xParam.getValuef();
            float y = yParam.getValuef();
            float z = model.cz;
            float spread = (float) Math.pow(2, spreadParam.getValuef()) * radius;
            return new LXVector(
                x + variation.x * spread,
                y + variation.y * spread,
                z + variation.z * spread
            );
        }

        public LXPoint getPoint(LXVector variation) {
            LXVector pos = getPosition(variation);
            LXPoint nearest = index.nearestPoint(pos);
            if (nearest == null) {
                // Stupid hack because index.nearestPoint doesn't actually work.
                for (float dist : new float[] {30, 90, 270}) {
                    List<LXPoint> points = index.pointsWithin(pos, dist);
                    if (points.size() > 0) {
                        return getNearestPoint(points, pos);
                    }
                }
                return getNearestPoint(model.getPoints(), pos);
            }
            return nearest;
        }

        protected LXPoint getNearestPoint(List<LXPoint> points, LXVector pos) {
            LXPoint nearest = null;
            float minSqDist = 0;
            for (LXPoint point : points) {
                float dx = point.x - pos.x;
                float dy = point.y - pos.y;
                float dz = point.z - pos.z;
                float sqDist = dx * dx + dy * dy + dz * dz;
                if (nearest == null || sqDist < minSqDist) {
                    nearest = point;
                    minSqDist = sqDist;
                }
            }
            return nearest;
        }

        public double getSize(double variation) {
            return Math.pow(2, sizeParam.getValue()) * radius * (1 + variation * sizeVarParam.getValue());
        }

        public double getRate() {
            return Math.pow(2, rateParam.getValue());
        }

        public LXVector getVelocity() {
            double radians = orientParam.getValue() * 2 * Math.PI;
            double speed = getRate() * radius;
            return new LXVector((float) (speed * Math.cos(radians)), (float) (speed * Math.sin(radians)), 0);
        }

        public int getPitchLo() { return pitchLoParam.getValuei(); }
        public int getPitchHi() { return pitchHiParam.getValuei(); }
    }
}
