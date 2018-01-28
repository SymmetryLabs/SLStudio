package com.symmetrylabs.slstudio.performance;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.midi.remote.LXMidiRemote;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import static com.symmetrylabs.slstudio.util.Utils.map;


public class FoxListener extends LXComponent {
    LXMidiRemote remote;

    private final LX lx;

    public FoxListener(LX lx) {
        super(lx);

        this.lx = lx;

        lx.engine.midi.whenReady(new Runnable() {
            public void run() {
                bind();
            }
        });
    }

    void bind() {
        LXMidiInput chosenInput = lx.engine.midi.matchInput("Faderfox");
        LXMidiOutput chosenOutput = lx.engine.midi.matchOutput("Faderfox");

        if (chosenInput == null || chosenOutput == null) {
            return;
        }

        chosenInput.open();
        chosenOutput.open();

        remote = new LXMidiRemote(chosenInput, chosenOutput);

        final CompoundParameter slowDown = new CompoundParameter("slowDown", 0);
        slowDown.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                float v = slowDown.getValuef();
                lx.engine.speed.setValue(map((float) v, 0f, 1f, 1.0f, 0f));
            }
        });

        remote.bindController(lx.engine.crossfader, 112);
        // remote.bindController(lx.engine.masterChannel.getEffect("Blur").getParameter("amount"), 32);
        // remote.bindController(lx.engine.output.brightness, 33);
        // remote.bindController(slowDown, 34);
        remote.bindController(slowDown, 32);
    }
}
