package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import processing.core.PFont;
import processing.core.PGraphics;

import java.text.DecimalFormat;

import static com.symmetrylabs.slstudio.util.Utils.millis;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;


public class UIFramerate extends UI2dContext {
    private final P3LX lx;
    private final PFont monospacedFont;
    private final DecimalFormat fpsFormat = new DecimalFormat("0.0");
    private final DecimalFormat elapsedFormat = new DecimalFormat("0.00");

    public UIFramerate(UI ui, final P3LX lx, float x) {
        super(ui, x, 0, 700, 30);
        this.lx = lx;
        this.monospacedFont = SLStudio.applet.createFont("Monospaced", 15);
        setVisible(true);
    }

    private long lastDebugPrint = millis();

    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(monospacedFont);
        pg.clear();
        pg.textSize(15);
        pg.textAlign(LEFT, TOP);
        if (lx.engine.isThreaded()) {
            // pg.text(String.format("Engine: %2$.1f UI: %2$.1f",
            //   lx.engine.frameRate(), lx.applet.frameRate), 0, 0);
            pg.text(String.format(
                "Engine FPS: %4s UI: %4s  //  Frame: %5sms (avg) %5sms (worst)",
                fpsFormat.format(lx.engine.frameRate()),
                fpsFormat.format(lx.applet.frameRate),
                elapsedFormat.format(lx.engine.timer.runAvgNanos / 1000000.0),
                elapsedFormat.format(lx.engine.timer.runWorstNanos / 1000000.0)
            ), 0, 0);
            if (lx.engine.timer.runLastNanos >= 100000000
                && lx.engine.timer.runLastNanos == lx.engine.timer.runWorstNanos
                && lastDebugPrint + 500 < millis()) {
                lastDebugPrint = millis();
                StringBuilder sb = new StringBuilder();
                sb.append("LXEngine  " + ((int) (lx.engine.timer.runLastNanos / 1000000)) + "ms\n");
                if (((int) (lx.engine.timer.oscNanos / 1000000)) != 0) {
                    sb.append("  osc  " + ((int) (lx.engine.timer.oscNanos / 1000000)) + "ms\n");
                }
                if (((int) (lx.engine.timer.inputNanos / 1000000)) != 0) {
                    sb.append("  inputs  " + ((int) (lx.engine.timer.inputNanos / 1000000)) + "ms\n");
                }
                if (((int) (lx.engine.timer.channelNanos / 1000000)) != 0) {
                    sb.append("  channels  " + ((int) (lx.engine.timer.channelNanos / 1000000)) + "ms\n");
                    for (LXChannel channel : lx.engine.channels) {
                        if (((int) (channel.timer.loopNanos / 1000000)) != 0) {
                            sb.append("    " + channel.getLabel() + "  " + ((int) (channel.timer.loopNanos / 1000000)) + "ms\n");
                            LXPattern pattern = channel.getActivePattern();
                            if (((int) (pattern.timer.runNanos / 1000000)) != 0) {
                                sb.append("      " + pattern.getLabel() + "  " + ((int) (pattern.timer.runNanos / 1000000)) + "ms\n");
                            }
                            for (LXEffect effect : channel.getEffects()) {
                                if (((int) (effect.timer.runNanos / 1000000)) != 0) {
                                    sb.append("      " + effect.getLabel() + "  " + ((int) (effect.timer.runNanos / 1000000)) + "ms\n");
                                }
                            }
                        }
                    }
                }
                if (((int) (lx.engine.timer.fxNanos / 1000000)) != 0) {
                    sb.append("  effects  " + ((int) (lx.engine.timer.fxNanos / 1000000)) + "ms\n");
                    for (LXEffect effect : lx.engine.masterChannel.getEffects()) {
                        if (((int) (effect.timer.runNanos / 1000000)) != 0) {
                            sb.append("    " + effect.getLabel() + "  " + ((int) (effect.timer.runNanos / 1000000)) + "ms\n");
                        }
                    }
                }
                System.out.println(sb);
            }
        } else {
            pg.text(String.format("FPS: %02.1f", lx.applet.frameRate), 0, 0);
        }


        redraw();
    }
}
