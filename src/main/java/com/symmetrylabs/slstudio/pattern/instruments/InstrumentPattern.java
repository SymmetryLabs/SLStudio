package com.symmetrylabs.slstudio.pattern.instruments;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.MidiPolyphonicExpressionPattern;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MusicUtils;
import com.symmetrylabs.util.Octahedron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ddf.minim.analysis.FFT;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.Tempo;
import heronarts.lx.audio.LXAudioBuffer;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
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
    private final double radius = Math.hypot(Math.hypot(model.xRange/2, model.yRange/2), model.zRange/2);
    private final CompoundParameter spreadParam = new CompoundParameter("Spread", radius/2, 0, radius);

    private final CompoundParameter sizeParam = new CompoundParameter("Size", radius * 0.1, 0, radius);
    private final CompoundParameter rateParam = new CompoundParameter("Rate", 1, 0, 10);
    private final CompoundParameter orientParam = new CompoundParameter("Orient", 0, -1, 1);

    private final EnumParameter<TriggerSource> sourceParam = new EnumParameter<>("Source", TriggerSource.BEAT);
    private final CompoundParameter gainParam = new CompoundParameter("Gain", 1, 0, 10);
    private final DiscreteParameter pitchLoParam = new DiscreteParameter("PitchLo", MusicUtils.PITCH_C1, 0, 127);
    private final DiscreteParameter pitchHiParam = new DiscreteParameter("PitchHi", MusicUtils.PITCH_C5, 0, 127);
    private final CompoundParameter falloffParam = new CompoundParameter("Falloff", 2, 0, 12);

    private final ParameterSet paramSet = new ParameterSet();
    private Instrument instrument;
    private Note[] lastNotes;

    private Integer beat;
    private boolean measure;

    private Note[] midiNotes;

    private LXAudioBuffer audio;
    private float[] samples;
    private FFT fft;
    private float[] bands;
    private float[] hertzLo;
    private float[] hertzHi;

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
        addParameter(rateParam);
        addParameter(orientParam);

        addParameter(sourceParam);
        addParameter(gainParam);
        addParameter(pitchLoParam);
        addParameter(pitchHiParam);
        addParameter(falloffParam);

        pitchLoParam.setFormatter(new MusicUtils.MidiPitchFormatter());
        pitchHiParam.setFormatter(new MusicUtils.MidiPitchFormatter());
        instrParam.addListener(param -> {
            instrument = null;  // will be reconstructed in run()
        });

        lastNotes = setupNotes();
        initBeat();
        initMidi();
        initAudio();
    }

    protected void initBeat() {
        beat = null;
        measure = false;
        lx.tempo.addListener(this);
    }

    protected void initMidi() {
        midiNotes = setupNotes();
    }

    protected Note[] setupNotes() {
        Note[] notes = new Note[MusicUtils.MAX_PITCH + 1];
        for (int p = 0; p < notes.length; p++) {
            notes[p] = new Note(false, false, 0);
        }
        return notes;
    }

    protected void initAudio() {
        audio = lx.engine.audio.getInput().mix;
        samples = new float[audio.bufferSize()];
        fft = new FFT(audio.bufferSize(), audio.sampleRate());
        fft.window(FFT.HAMMING);

        bands = new float[MusicUtils.MAX_PITCH + 1];
        hertzLo = new float[MusicUtils.MAX_PITCH + 1];
        hertzHi = new float[MusicUtils.MAX_PITCH + 1];
        for (int p = 0; p <= MusicUtils.MAX_PITCH; p++) {
            hertzLo[p] = (float) MusicUtils.pitchToHertz(p - 0.4);
            hertzHi[p] = (float) MusicUtils.pitchToHertz(p + 0.4);
        }
    }

    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        Note[] notes = setupNotes();
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
        lastNotes = notes;

        long[] colors = (long[]) getPolyBuffer().getArray(RGB16);
        Arrays.fill(colors, 0);
        getPolyBuffer().markModified(RGB16);

        if (instrument == null) {
            instrument = InstrumentRegistry.getInstrument(instrParam.getOption());
        }
        instrument.run(model, paramSet, notes, deltaMs / 1000.0, getPolyBuffer());
    }

    protected void putBeatNotes(Note[] notes) {
        if (measure) {
            notes[MusicUtils.PITCH_C3].attack = true;
            notes[MusicUtils.PITCH_C3].sustain = true;
            notes[MusicUtils.PITCH_C3].intensity = 1;
        } else if (beat != null) {
            notes[MusicUtils.PITCH_C4].attack = true;
            notes[MusicUtils.PITCH_C4].sustain = true;
            notes[MusicUtils.PITCH_C4].intensity = 1;
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
            notes[p].copyFrom(midiNotes[p]);
            midiNotes[p].attack = false;
        }
    }

    @Override public void noteOn(int pitch, double velocity) {
        midiNotes[pitch].attack = true;
        midiNotes[pitch].sustain = true;
        midiNotes[pitch].intensity = velocity;
    }

    @Override public void notePressure(int pitch, double pressure) {
        midiNotes[pitch].intensity = pressure;
    }

    @Override public void noteOff(int pitch) {
        midiNotes[pitch].sustain = false;
        midiNotes[pitch].intensity = 0;
    }

    protected void putAudioNotes(Note[] notes) {
        audio.getSamples(samples);
        fft.forward(samples);
        for (int pitch = 0; pitch <= MusicUtils.MAX_PITCH; pitch++) {
            bands[pitch] = fft.calcAvg(hertzLo[pitch], hertzHi[pitch]);
        }
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
        for (int i = 0; i < bands.length; i++) {
            markers.add(makeBar(i*10 - 100, -100, 0, bands[i], 8, 0xffff0080));
            //markers.add(makeBar(i*10 - 100, -100, floors[i], ceilings[i], 8, 0xff0000ff));
            //markers.add(makeBar(i*10 - 100, -150, 0, values[i], 8, 0xffffffff));
        }
        for (int i = 0; i < samples.length; i++) {
            markers.add(makeBar(i, -100, 0, samples[i]*100, 0, 0xff804000));
        }
        return markers;
    }

    @Override public String getCaption() {
        String result = "Notes:";
        for (int p = 0; p < lastNotes.length; p++) {
            if (lastNotes[p].sustain) {
                result += " " + MusicUtils.formatPitch(p);
                if (lastNotes[p].attack) {
                    result += "*";
                }
            }
        }
        return result;
    }

    class ParameterSet implements Instrument.ParameterSet {
        public long generateColor(double variation) {
            double hue = hueParam.getValue() + hueVarParam.getValue() * variation;
            double sat = satParam.getValue();
            double brt = brtParam.getValue();
            return Ops16.hsb(hue, sat, brt);
        }

        public LXVector generatePosition(LXVector variation) {
            float x = xParam.getValuef();
            float y = yParam.getValuef();
            float z = model.cz;
            float spread = spreadParam.getValuef();
            return new LXVector(
                x + variation.x * spread,
                y + variation.y * spread,
                z + variation.z * spread
            );
        }

        public double getSize() { return sizeParam.getValue(); }
        public double getRate() { return rateParam.getValue(); }
        public double getOrient() { return orientParam.getValue(); }

        public int getPitchLo() { return pitchLoParam.getValuei(); }
        public int getPitchHi() { return pitchHiParam.getValuei(); }
    }
}
