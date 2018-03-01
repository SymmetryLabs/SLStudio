package com.symmetrylabs.slstudio.performance;

import com.symmetrylabs.slstudio.SLStudio;
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
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.slstudio.performance.PerformanceManager.DeckSide.LEFT;
import static com.symmetrylabs.slstudio.performance.PerformanceManager.DeckSide.RIGHT;
import static processing.core.PApplet.println;

import com.symmetrylabs.slstudio.workspaces.Workspaces;


public class APC40Listener extends LXComponent {
    LXMidiRemote remotes[] = new LXMidiRemote[2];

    private final Workspaces workspaces;

    public BooleanParameter hasRemote = new BooleanParameter("hasRemote", false);

    public APC40Listener(LX lx, Workspaces workspaces) {
        super(lx);
        this.workspaces = workspaces;

        lx.engine.midi.whenReady(this::bind);
    }

    class Listener implements LXMidiListener {
        BoundedParameter col;
        PerformanceManager.DeckSide side;

        public Listener(BoundedParameter col, PerformanceManager.DeckSide side) {
            this.col = col;
            this.side = side;
        }

        public void aftertouchReceived(MidiAftertouch aftertouch) {
            // println("AFTER", aftertouch.getChannel());
        }

        public void controlChangeReceived(MidiControlChange cc) {
            if (cc.getCC() == 14) {
                System.out.println("control: " + cc.getCC() + ", value: " + cc.getNormalized());
                System.out.println("old value: " + SLStudio.applet.lx.engine.output.brightness.getValuef());
                SLStudio.applet.lx.engine.output.brightness.setValue(cc.getNormalized());
            }

            // if (cc.getCC() == 47) {
            //     int v = cc.getValue();
            //     int diff = v > 50 ? -5 : 5;
            //     float raw = col.getValuef() + diff;
            //     float mod = raw < 0 ? 360 - raw : raw % 360;
            //     col.setValue(mod);
            // }
        }

        public void noteOffReceived(MidiNote note) {
            // println("NOTE OFF", note.getPitch(), note.getVelocity());
            if (note.getPitch() == 91) {
                SLStudio.applet.lx.engine.output.enabled.setValue(false);
            }
        }

        public void noteOnReceived(MidiNoteOn note) {
            System.out.println(note.getPitch());

            if (note.getPitch() == 91) {
                SLStudio.applet.lx.engine.output.enabled.setValue(true);
            }

            switch (note.getPitch()) {
                case 32: // 1
                    workspaces.goIndex(0);
                    break;

                case 33: // 2
                    workspaces.goIndex(1);
                    break;

                case 34: // 3
                    workspaces.goIndex(2);
                    break;

                case 35: // 4
                    workspaces.goIndex(3);
                    break;

                case 36: // 5
                    workspaces.goIndex(4);
                    break;

                case 37: // 6
                    workspaces.goIndex(5);
                    break;

                case 38: // 7
                    workspaces.goIndex(6);
                    break;

                case 39: // 8
                    workspaces.goIndex(7);
                    break;

                case 24: // 9
                    workspaces.goIndex(8);
                    break;

                case 25: // 10
                    workspaces.goIndex(9);
                    break;

                case 26: // 11
                    workspaces.goIndex(10);
                    break;

                case 27: // 12
                    workspaces.goIndex(11);
                    break;

                case 28: // 13
                    workspaces.goIndex(12);
                    break;

                case 29: // 14
                    workspaces.goIndex(13);
                    break;

                case 30: // 15
                    workspaces.goIndex(14);
                    break;

                case 31: // 16
                    workspaces.goIndex(15);
                    break;

                case 16: // 17
                    workspaces.goIndex(16);
                    break;

                case 17: // 18
                    workspaces.goIndex(17);
                    break;

                case 18: // 19
                    workspaces.goIndex(18);
                    break;

                case 19: // 20
                    workspaces.goIndex(19);
                    break;
        }

            // if (note.getPitch() == 94) {
            //     println("UP");
            //     int i = SLStudio.applet.performanceManager.focusedDeskIndexForSide(side);
            //     SLStudio.applet.performanceManager.deckWindows[i].channel.goPrev();
            // }
            // if (note.getPitch() == 95) {
            //     println("DOWN");
            //     int i = SLStudio.applet.performanceManager.focusedDeskIndexForSide(side);
            //     SLStudio.applet.performanceManager.deckWindows[i].channel.goNext();
            // }
        }

        public void pitchBendReceived(MidiPitchBend pitchBend) {
            // println("PB", pitchBend.getChannel());
        }

        public void programChangeReceived(MidiProgramChange pc) {
            // println("PC", pc.getProgram());
            System.out.println("program");
        }
    }

    void bind() {
        for (final LXMidiSurface s : SLStudio.applet.lx.engine.midi.surfaces) {
            s.enabled.setValue(false);
            s.enabled.addListener(parameter -> {
                if (s.enabled.isOn()) {
                    s.enabled.setValue(false);
                }
            });
        }

        List<LXMidiInput> inputs = SLStudio.applet.lx.engine.midi.getInputs();
        List<LXMidiOutput> outputs = SLStudio.applet.lx.engine.midi.getOutputs();
        ArrayList<LXMidiInput> apcInputs = new ArrayList<>();
        ArrayList<LXMidiOutput> apcOutputs = new ArrayList<>();


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
                input.addListener(new Listener(SLStudio.applet.performanceManager.lColor, LEFT));
                remotes[i].sendNoteOn(0, 32, 127);
            } else {
                input.addListener(new Listener(SLStudio.applet.performanceManager.rColor, RIGHT));
                remotes[i].sendNoteOn(0, 33, 127);
            }
        }

        if (n > 0) {
            hasRemote.setValue(true);
        }
    }
}
