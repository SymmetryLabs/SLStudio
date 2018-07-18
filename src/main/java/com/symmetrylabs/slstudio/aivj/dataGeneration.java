package com.symmetrylabs.slstudio.aivj;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.audio.LXAudioComponent;
import heronarts.lx.audio.LXAudioInput;

public class dataGeneration extends LXPattern {

    private LXAudioInput audioInput = lx.engine.audio.getInput();

    public dataGeneration(LX lx) {
        super(lx);

//        addParameter(audioInput);

    }
    public void run(double deltaMs, PolyBuffer.Space space) {

    }
}
