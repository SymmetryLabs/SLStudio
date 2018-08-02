package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIObject;
import processing.core.PFont;
import processing.core.PGraphics;

import static com.symmetrylabs.util.Utils.millis;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;


public class UIFramerate extends UITextOverlay {
    private final P3LX lx;
    private long lastDebugPrint = millis();

    public UIFramerate(UI ui, final P3LX lx, UI3dContext parent, int anchorX, int anchorY, int alignX, int alignY) {
        super(ui, parent, anchorX, anchorY, alignX, alignY);
        this.lx = lx;
    }

    protected void onDraw(UI ui, PGraphics pg) {
        super.onDraw(ui, pg);
        redraw();
    }

    public String getText() {
        if (lx.engine.isThreaded()) {
            if (lx.engine.timer.runLastNanos >= 100000000
                && lx.engine.timer.runLastNanos == lx.engine.timer.runWorstNanos
                && lastDebugPrint + 500 < millis()) {
                lastDebugPrint = millis();
                printTimingStats();
            }

            return String.format(
                "Engine: %5.1ffps    UI: %5.1ffps    Net: %5.1ffps    Frame:%5.1fms, avg%5.1fms, max%5.1fms    16-bit conv:%3d %s",
                Math.min(lx.engine.frameRate(), 999),  // frameRate() sometimes returns Infinity
                Math.min(lx.applet.frameRate, 999),
                Math.min(lx.engine.network.frameRate(), 999),
                lx.engine.timer.runCurrentNanos / 1e6,
                lx.engine.timer.runAvgNanos / 1e6,
                lx.engine.timer.runWorstNanos / 1e6,
                lx.engine.conversionsPerFrame,
                "[" + getUsing16BitFlags() + "]"
            );
        } else {
            return String.format("FPS: %02.1f", lx.applet.frameRate);
        }
    }

    protected String getUsing16BitFlags() {
        String flags = "";
        for (int i = 0; i < 8 && i < lx.engine.getChannels().size(); i++) {
            if (lx.engine.getChannel(i).colorSpace.getEnum() == PolyBuffer.Space.RGB16) {
                flags += (i + 1);
            }
        }
        if (lx.engine.colorSpace.getEnum() == PolyBuffer.Space.RGB16) {
            flags += "E";
        }
        return flags;
    }

    protected void printTimingStats() {
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
}
