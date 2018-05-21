package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContext;
import processing.core.PFont;
import processing.core.PGraphics;

import static com.symmetrylabs.util.Utils.millis;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;


public class UIFramerate extends UI2dContext {
    private final P3LX lx;
    private final PFont font;

    public UIFramerate(UI ui, final P3LX lx, float x, float y) {
        super(ui, x, y, 900, 30);
        this.lx = lx;
        this.font = SLStudio.applet.loadFont("Inconsolata-Bold-14.vlw");
        setVisible(true);
    }

    private long lastDebugPrint = millis();

    protected void onDraw(UI ui, PGraphics pg) {
        pg.textFont(font);
        pg.clear();
        pg.textAlign(LEFT, TOP);
        pg.fill(0xa0ffffff);
        if (lx.engine.isThreaded()) {
            String using16bit = "";
            for (int i = 0; i < 8 && i < lx.engine.getChannels().size(); i++) {
                if (lx.engine.getChannel(i).colorSpace.getEnum() == PolyBuffer.Space.RGB16) {
                    using16bit += (i + 1);
                }
            }
            if (lx.engine.colorSpace.getEnum() == PolyBuffer.Space.RGB16) {
                using16bit += "E";
            }
            pg.text(String.format(
                "Engine: %5.1f fps    UI: %5.1f fps    Frame: %4.1fms, avg %4.1fms, max %4.1fms    16-bit: %-8s  Conversions:%2d    (? for help)",
                lx.engine.frameRate(),
                lx.applet.frameRate,
                lx.engine.timer.runCurrentNanos / 1e6,
                lx.engine.timer.runAvgNanos / 1e6,
                lx.engine.timer.runWorstNanos / 1e6,
                "[" + using16bit + "]",
                lx.engine.conversionsPerFrame
            ), 0, 0);

            if (lx.engine.timer.runLastNanos >= 100000000
                && lx.engine.timer.runLastNanos == lx.engine.timer.runWorstNanos
                && lastDebugPrint + 500 < millis()) {
                lastDebugPrint = millis();
                printTimingStats();
            }
        } else {
            pg.text(String.format("FPS: %02.1f", lx.applet.frameRate), 0, 0);
        }
        redraw();
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
