//package com.symmetrylabs.slstudio.aivj;
//
//import heronarts.lx.audio.DecibelMeter;
//import heronarts.lx.audio.FourierTransform;
//import heronarts.lx.audio.LXAudioBuffer;
//import heronarts.lx.modulator.LXModulator;
//import heronarts.lx.parameter.LXNormalizedParameter;
//
//public class MelSpectrogram extends LXModulator implements LXNormalizedParameter //extends DecibelMeter {
//
//    protected LXAudioBuffer buffer;
//
//    public final FourierTransform fft;
//    private final float[] fftBuffer;
//    public MelSpectrogram (String label, LXAudioBuffer buffer, int numBands) {
////        super(label, buffer);
//
//
//        addParameter("slope", this.slope);
//        this.fftBuffer = new float[buffer.bufferSize()];
//        this.fft = new FourierTransform(buffer.bufferSize(), buffer.sampleRate());
//        this.fft.setNumBands(this.numBands = numBands);
//    }
//
//
//}
