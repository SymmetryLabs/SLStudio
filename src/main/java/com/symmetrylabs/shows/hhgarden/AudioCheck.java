package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.audio.LXAudioBuffer;
import heronarts.lx.audio.LXAudioInput;
import processing.core.PVector;

public class AudioCheck extends SLPattern<SLModel> {
    private LXAudioBuffer audioBuffer;
    private float[] audioSamples = new float[0];

    public AudioCheck(LX lx) {
        super(lx);
    }

    @Override public void onActive() {
        super.onActive();
        LXAudioInput input = getChannel().audioInput;
        input.open();
        input.start();
        audioBuffer = input.mix;
        audioSamples = new float[audioBuffer.bufferSize()];
    }

    @Override public void onInactive() {
        LXAudioInput input = getChannel().audioInput;
        if (input.isOpen()) {
            input.stop();
        }
        input.close();
        super.onInactive();
    }

    private CubeMarker makeBar(float x, float z, float minY, float maxY, float thickness, int color) {
        return new CubeMarker(
            new PVector(x - thickness/2, (minY + maxY)/2, z),
            new PVector(thickness/2, (maxY - minY)/2, 0),
            color
        );
    }
    public List<Marker> getMarkers() {
        float xScale = 1;
        float yScale = 200;

        List<Marker> markers = new ArrayList<>();
        if (audioBuffer != null) {
            audioBuffer.getSamples(audioSamples);
            markers.add(new CubeMarker(
                new PVector(xScale*audioSamples.length/2, 0, 0),
                new PVector(xScale*audioSamples.length/2, yScale, 0),
                0xff00c0c0
            ));
            markers.add(new CubeMarker(
                new PVector(xScale*audioSamples.length/2, 0, 0),
                new PVector(xScale*audioSamples.length/2, 0, 0),
                0xff404040
            ));
            for (int i = 0; i < audioSamples.length; i++) {
                markers.add(makeBar(xScale*i, 0, 0, yScale*audioSamples[i], 0, 0xffc0c000));
            }
        }
        return markers;
    }
}
