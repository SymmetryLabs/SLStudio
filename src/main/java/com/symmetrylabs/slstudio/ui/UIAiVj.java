package com.symmetrylabs.slstudio.ui;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.LX;
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

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.util.dispatch.Dispatcher;

public class UIAiVj extends UICollapsibleSection {

    private final float WIDTH;

    public UIAiVj(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 255);
        this.WIDTH = w;

        setTitle("AI VJ");
        setPadding(10);

        final Recorder recorder = new Recorder();
        recorder.addToContainer(this);

        final Player player = new Player();
        player.addToContainer(this);

        recorder.isRunning.addListener(new LXParameterListener() {
            public void onParameterChaned(BooleanParameter p) {
                if (p.isOn()) player.isRunning.setValue(false);
            }
        });
        player.isRunning.addListener(new LXParameterListener() {
            public void onParameterChaned(BooleanParameter p) {
                if (p.isOn()) recorder.isRunning.setValue(false);
            }
        });

        // UIIntegerBox intBox = new UIIntegerBox(0, 0, 50, 17).setRange(1, 120);
        // intBox.addToContainer(this);
    }

    private class Recorder extends UI2dContainer {

        public final BooleanParameter isRunning = new BooleanParameter("isRunning", false);

        private Recorder() {
            super(0, 0, WIDTH/2-5, 230);
            setPadding(5);
            setBackgroundColor(0x11000000);

            UILabel label = new UILabel(5, 0, 60, 14).setLabel("Recorder");
            label.addToContainer(this);

            UIButton recordButton = new UIButton(0, 30, 50, 50);
            recordButton.setBackgroundColor(0x11ff0000);
            recordButton.addToContainer(this);
        }
    }

    private class Player extends UI2dContainer {

        public final BooleanParameter isRunning = new BooleanParameter("isRunning", false);

        private Player() {
            super(WIDTH/2, 0, WIDTH/2-10, 230);
            setPadding(5);
            setBackgroundColor(0x11000000);

            UILabel label = new UILabel(5, 0, 60, 14).setLabel("Player");
            label.addToContainer(this);
        }
    }
}