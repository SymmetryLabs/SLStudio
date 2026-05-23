package heronarts.p3lx.ui.studio.device;

import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.warp.LXWarp;
import heronarts.p3lx.ui.UI;
import processing.event.KeyEvent;

class UIWarpDevice extends UIDevice {

    private final static int WIDTH = 124;

    private final LXBus bus;
    /** When non-null, this tile is scoped to a single pattern's per-pattern chain. */
    private final LXPattern pattern;
    final LXWarp warp;

    UIWarpDevice(UI ui, LXBus bus, final LXWarp warp) {
        this(ui, bus, null, warp);
    }

    UIWarpDevice(UI ui, LXBus bus, LXPattern pattern, final LXWarp warp) {
        super(ui, warp, WIDTH);
        this.bus = bus;
        this.pattern = pattern;
        this.warp = warp;
        setTitle(warp.label);
        setEnabledButton(warp.enabled);
        buildDefaultControlUI(warp);
    }

    private void doRemove() {
        if (pattern != null && bus instanceof LXChannel) {
            ((LXChannel) bus).removePatternWarp(pattern, warp);
        } else {
            bus.removeWarp(warp);
        }
    }

    private void doMove(int newIndex) {
        if (pattern != null && bus instanceof LXChannel) {
            ((LXChannel) bus).movePatternWarp(pattern, warp, newIndex);
        } else {
            bus.moveWarp(warp, newIndex);
        }
    }

    private int chainSize() {
        if (pattern != null && bus instanceof LXChannel) {
            return ((LXChannel) bus).getPatternWarps(pattern).size();
        }
        return bus.getWarps().size();
    }

    @Override
    protected boolean isEligibleControlParameter(LXComponent component, LXParameter parameter) {
        return (parameter != warp.enabled) && super.isEligibleControlParameter(component, parameter);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            consumeKeyEvent();
            warp.enabled.toggle();
        } else if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            consumeKeyEvent();
            doRemove();
        } else {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }

        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_D) {
                consumeKeyEvent();
                doRemove();
            } else if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                consumeKeyEvent();
                if (warp.getIndex() > 0) {
                    doMove(warp.getIndex() - 1);
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                consumeKeyEvent();
                if (warp.getIndex() < chainSize() - 1) {
                    doMove(warp.getIndex() + 1);
                }
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyReleased(keyEvent, keyChar, keyCode);
    }
}
