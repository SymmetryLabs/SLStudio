package com.symmetrylabs.slstudio.ui;

import heronarts.lx.LX;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.UI2dContainer;

import com.symmetrylabs.slstudio.aivj.AiVj;
import com.symmetrylabs.slstudio.SLStudio;

public class UIAiVj extends UICollapsibleSection {

    private final float WIDTH;

    public UIAiVj(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 122);
        this.WIDTH = w;

        setTitle("AI VJ");
        setPadding(10);

        final UIRecorder recorder = new UIRecorder();
        recorder.addToContainer(this);

        final UIPlayer player = new UIPlayer();
        player.addToContainer(this);

        final UILabel generateSpotifyDataLabel = new UILabel(23, 77, 110, 14).setLabel("generate Spotify data");
        generateSpotifyDataLabel.setBackground(false);
        generateSpotifyDataLabel.addToContainer(this);

        final UIButton generateSpotifyDataButton = new UIButton(2, 81, 15, 15)
            .setParameter(SLStudio.applet.aivj.recorder.generateSpotifyData);
        generateSpotifyDataButton.setBorderRounding(5);
        generateSpotifyDataButton.addToContainer(this);

        SLStudio.applet.aivj.recorder.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) SLStudio.applet.aivj.player.isRunning.setValue(false);
                redraw();
            }
        });
        SLStudio.applet.aivj.player.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
             if (((BooleanParameter)parameter).isOn()) SLStudio.applet.aivj.recorder.isRunning.setValue(false);
             redraw();
            }
        });

        SLStudio.applet.aivj.recorder.generateSpotifyData.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                redraw();
            }
        });
    }

    private class UIRecorder extends UI2dContainer {
     private UIRecorder() {
            super(0, 0, WIDTH/2-5, 73);
            setPadding(5);
            setBackgroundColor(0x22000000);

            UILabel label = new UILabel(5, 0, 60, 14).setLabel("Recorder");
            label.setBackground(false);
            label.addToContainer(this);

            UIButton recordButton = new UIButton(5, 20, WIDTH/2-15, 23)
                .setActiveColor(0xbbbb0000)
                .setInactiveColor(0x11ff0000)
                .setActiveLabel("Recording...")
                .setInactiveLabel("Record")
                .setParameter(SLStudio.applet.aivj.recorder.isRunning);
            recordButton.setBorderRounding(15);
            recordButton.addToContainer(this);

            UILabel runTimeLabel = new UILabel(5, 48, 100, 14).setLabel("Runtime (mins)");
            runTimeLabel.setBackground(false);
            runTimeLabel.addToContainer(this);

            UIIntegerBox runtimeInput = new UIIntegerBox(97, 50, 50, 17) {
                // so we can avoid the decimel places from DiscreteParameter
                protected void onValueChange(int value) {
                    SLStudio.applet.aivj.recorder.runtime.setValue(value);
                }
            }.setRange(1, 60).setValue(10);
            runtimeInput.addToContainer(this);
        }
    }

    private class UIPlayer extends UI2dContainer {
        private UIPlayer() {
            super(WIDTH/2, 0, WIDTH/2-5, 73);
            setPadding(5);
            setBackgroundColor(0x22000000);

            UILabel label = new UILabel(5, 0, 60, 14).setLabel("Player");
            label.setBackground(false);
            label.addToContainer(this);

            UIButton playButton = new UIButton(5, 20, WIDTH/2-15, 23)
                .setActiveColor(0xbb00bb00)
                .setInactiveColor(0x1100ff00)
                .setActiveLabel("Playing...")
                .setInactiveLabel("Play")
                .setParameter(SLStudio.applet.aivj.player.isRunning);
            playButton.setBorderRounding(15);
            playButton.addToContainer(this);

            UILabel runTimeLabel = new UILabel(5, 48, 100, 14).setLabel("Runtime (mins)");
            runTimeLabel.setBackground(false);
            runTimeLabel.addToContainer(this);

            UIIntegerBox runtimeInput = new UIIntegerBox(97, 50, 50, 17) {
                // so we can avoid the decimel places from DiscreteParameter
                protected void onValueChange(int value) {
                    SLStudio.applet.aivj.player.runtime.setValue(value);
                }
            }.setRange(1, 60).setValue(10);
            runtimeInput.addToContainer(this);
        }
    }
}