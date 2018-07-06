package heronarts.p3lx.ui.studio.device;

import heronarts.lx.LXBus;
import heronarts.lx.LXComponent;
import heronarts.lx.warp.LXWarp;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UI;
import processing.event.KeyEvent;

class UIWarpDevice extends UIDevice {

    private final static int WIDTH = 124;

    private final LXBus bus;
    final LXWarp warp;

    UIWarpDevice(UI ui, LXBus bus, final LXWarp warp) {
        super(ui, warp, WIDTH);
        this.bus = bus;
        this.warp = warp;
        setTitle(warp.label);
        setEnabledButton(warp.enabled);
        buildDefaultControlUI(warp);
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
            bus.removeWarp(warp);
        } else {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }

        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_D) {
                consumeKeyEvent();
                bus.removeWarp(warp);
            } else if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                consumeKeyEvent();
                if (warp.getIndex() > 0) {
                    bus.moveWarp(warp, warp.getIndex() - 1);
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                consumeKeyEvent();
                if (warp.getIndex() < bus.getWarps().size() - 1) {
                    bus.moveWarp(warp, warp.getIndex() + 1);
                }
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyReleased(keyEvent, keyChar, keyCode);
    }
}