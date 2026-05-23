/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.studio.device;

import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UI;
import processing.event.KeyEvent;

class UIEffectDevice extends UIDevice {

    private final static int WIDTH = 124;

    private final LXBus bus;
    /** When non-null, this tile is scoped to a single pattern's per-pattern chain. */
    private final LXPattern pattern;
    final LXEffect effect;

    UIEffectDevice(UI ui, LXBus bus, final LXEffect effect) {
        this(ui, bus, null, effect);
    }

    UIEffectDevice(UI ui, LXBus bus, LXPattern pattern, final LXEffect effect) {
        super(ui, effect, WIDTH);
        this.bus = bus;
        this.pattern = pattern;
        this.effect = effect;
        setTitle(effect.label);
        setEnabledButton(effect.enabled);
        buildDefaultControlUI(effect);
    }

    private void doRemove() {
        if (pattern != null && bus instanceof LXChannel) {
            ((LXChannel) bus).removePatternEffect(pattern, effect);
        } else {
            bus.removeEffect(effect);
        }
    }

    private void doMove(int newIndex) {
        if (pattern != null && bus instanceof LXChannel) {
            ((LXChannel) bus).movePatternEffect(pattern, effect, newIndex);
        } else {
            bus.moveEffect(effect, newIndex);
        }
    }

    private int chainSize() {
        if (pattern != null && bus instanceof LXChannel) {
            return ((LXChannel) bus).getPatternEffects(pattern).size();
        }
        return bus.getEffects().size();
    }

    @Override
    protected boolean isEligibleControlParameter(LXComponent component, LXParameter parameter) {
        return (parameter != effect.enabled) && super.isEligibleControlParameter(component, parameter);
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            consumeKeyEvent();
            effect.enabled.toggle();
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
                if (effect.getIndex() > 0) {
                    doMove(effect.getIndex() - 1);
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                consumeKeyEvent();
                if (effect.getIndex() < chainSize() - 1) {
                    doMove(effect.getIndex() + 1);
                }
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyReleased(keyEvent, keyChar, keyCode);
    }
}