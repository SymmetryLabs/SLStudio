package com.symmetrylabs.performance;

import com.symmetrylabs.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.LXMidiOutput;
import heronarts.lx.midi.MidiAftertouch;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.midi.MidiProgramChange;
import heronarts.lx.midi.remote.LXMidiRemote;
import heronarts.lx.midi.surface.LXMidiSurface;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.println;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class APC40Listener extends LXComponent {
    LXMidiRemote remotes[] = new LXMidiRemote[2];

    public BooleanParameter hasRemote = new BooleanParameter("hasRemote", false);

    public APC40Listener(LX lx) {
        super(lx);

        lx.engine.midi.whenReady(new Runnable() {
            public void run() {
                bind();

            }
        });
    }

    class Listener implements LXMidiListener {
        BoundedParameter col;
        int side;

        public Listener(BoundedParameter col, int side) {
            this.col = col;
            this.side = side;
        }

        public void aftertouchReceived(MidiAftertouch aftertouch) {
            // println("AFTER", aftertouch.getChannel());
        }

        public void controlChangeReceived(MidiControlChange cc) {
            if (cc.getCC() == 47) {
                int v = cc.getValue();
                int diff = v > 50 ? -5 : 5;
                float raw = col.getValuef() + diff;
                float mod = raw < 0 ? 360 - raw : raw % 360;
                col.setValue(mod);
            }
        }

        public void noteOffReceived(MidiNote note) {
            // println("NOTE OFF", note.getPitch(), note.getVelocity());
        }

        public void noteOnReceived(MidiNoteOn note) {
            if (note.getPitch() == 94) {
                println("UP");
                int i = SLStudio.applet.performanceManager.getWindowIndex(side);
                SLStudio.applet.performanceManager.windows[i].channel.goPrev();
            }
            if (note.getPitch() == 95) {
                println("DOWN");
                int i = SLStudio.applet.performanceManager.getWindowIndex(side);
                SLStudio.applet.performanceManager.windows[i].channel.goNext();
            }
        }

        public void pitchBendReceived(MidiPitchBend pitchBend) {
            // println("PB", pitchBend.getChannel());
        }

        public void programChangeReceived(MidiProgramChange pc) {
            // println("PC", pc.getProgram());
        }
    }

    void bind() {
        for (final LXMidiSurface s : SLStudio.applet.lx.engine.midi.surfaces) {
            s.enabled.setValue(false);
            s.enabled.addListener(new LXParameterListener() {
                public void onParameterChanged(LXParameter parameter) {
                    if (s.enabled.isOn()) {
                        s.enabled.setValue(false);
                    }
                }
            });
        }

        List<LXMidiInput> inputs = SLStudio.applet.lx.engine.midi.getInputs();
        List<LXMidiOutput> outputs = SLStudio.applet.lx.engine.midi.getOutputs();
        ArrayList<LXMidiInput> apcInputs = new ArrayList();
        ArrayList<LXMidiOutput> apcOutputs = new ArrayList();


        for (LXMidiInput i : inputs) {
            if (i.getName().contains("APC40")) {
                apcInputs.add(i);
            }
        }

        for (LXMidiOutput o : outputs) {
            if (o.getName().contains("APC40")) {
                apcOutputs.add(o);
            }
        }

        int n = PApplet.min(apcInputs.size(), apcOutputs.size(), 2);

        for (int i = 0; i < n; i++) {
            LXMidiInput input = apcInputs.get(i);
            LXMidiOutput output = apcOutputs.get(i);

            input.open();
            output.open();

            remotes[i] = new LXMidiRemote(input, output);
            remotes[i].logEvents(true);

            if (i == 0) {
                input.addListener(new Listener(SLStudio.applet.performanceManager.lColor, 0));
                remotes[i].sendNoteOn(0, 32, 127);
            } else {
                input.addListener(new Listener(SLStudio.applet.performanceManager.rColor, 1));
                remotes[i].sendNoteOn(0, 33, 127);
            }
        }

        if (n > 0) {
            hasRemote.setValue(true);
        }
    }
}
