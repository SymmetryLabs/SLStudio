package com.symmetrylabs.slstudio.ui;

import java.util.List;
import java.util.ArrayList;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PConstants;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.component.UILabel;

import com.symmetrylabs.slstudio.aivj.AiVj;
import com.symmetrylabs.slstudio.SLStudio;

public class UIAiVjStatus extends UI2dContext {

    public final static int WIDTH = 500;

    private enum Mode {
        RECORDING, PLAYING
    }

    private final Timer timer;

    private final UILabel status;

    private final PFont font;

    private Mode mode;

    public UIAiVjStatus(UI ui, final P3LX lx, float x, float y) {
        super(ui, x, y, WIDTH, 80);
        setVisible(false);

        this.timer = new Timer();
        lx.engine.addLoopTask(timer);

        this.font = SLStudio.applet.createFont("Menlo-Regular-13.vlw", 30);
        this.status = new UILabel(0, 0, WIDTH, 50).setLabel("");
        status.setBackground(false).setFont(font);
        status.setTextAlignment(PConstants.CENTER);
        status.addToContainer(this);

        SLStudio.applet.aivj.recorder.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) {
                    show(Mode.RECORDING, SLStudio.applet.aivj.recorder.runtime.getValuei());
                } else {
                    reset();
                }
            }
        });

        SLStudio.applet.aivj.player.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) {
                    show(Mode.PLAYING, SLStudio.applet.aivj.player.runtime.getValuei());
                } else {
                    reset();
                }
            }
        });
    }

    private void show(Mode mode, int numMinutes) {
        reset();
        timer.start(numMinutes);
        this.mode = mode;
        setVisible(true);
        redraw();
    }

    private void reset() {
        timer.stop();
        status.setLabel("");
        setVisible(false);
        redraw();
    }

    private void tick(String remainingTime) {
        redraw();
        status.setLabel("AI VJ: " + ((mode == Mode.RECORDING) ? "Recording" : "Playing") + " (" + remainingTime + ")");
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.clear();
        redraw();
    }

    private class Timer implements LXLoopTask {

        private boolean on = false;
        private int elapsedTime = 0;
        private int remainingSeconds = 0;

        public void loop(double deltaMs) {
            if (on) {
                elapsedTime += deltaMs;

                if (elapsedTime > 1000) {
                    int minutes = (int)Math.floor(remainingSeconds / 60);
                    int seconds = (int)remainingSeconds % 60;

                    if (seconds >= 10) {
                        tick(minutes + ":" + ((seconds == 0) ? "00" : seconds));
                    }
                    else {
                        tick(minutes + ":" + ((seconds == 0) ? "00" : "0" + seconds));
                    }
                    if (minutes == 0 && seconds == 0){
                        System.out.println("hit 0!");
                        tick(minutes + ":" + ((seconds == 0) ? "00" : "0" + seconds));

                        reset();
                        SLStudio.applet.aivj.player.isRunning.setValue(false);
                        SLStudio.applet.aivj.recorder.isRunning.setValue(false);
                    }

                    if (remainingSeconds-- == 0) on = false;
                    elapsedTime = 0;
                }
            } else {
                elapsedTime = 0;
            }
        }

        private void start(int numMinutes) {
            on = true;
            remainingSeconds = numMinutes * 60;
        }

        private void stop() {
            on = false;
            elapsedTime = 0;
            remainingSeconds = 0;
        }
    }
}
