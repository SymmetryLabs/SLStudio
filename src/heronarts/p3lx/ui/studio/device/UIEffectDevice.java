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
import heronarts.lx.LXEffect;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIKnob;
import processing.event.KeyEvent;

class UIEffectDevice extends UIDevice {

    private final static int WIDTH = 124;

    private final LXBus bus;
    final LXEffect effect;

    UIEffectDevice(UI ui, LXBus bus, final LXEffect effect) {
        super(ui, WIDTH);
        this.bus = bus;
        this.effect = effect;
        setTitle(effect.label);
        setEnabledButton(effect.enabled);

        float xp = 0, yp = 0;
        int i = 0;
        for (LXParameter parameter : effect.getParameters()) {
            if (parameter instanceof BoundedParameter || parameter instanceof DiscreteParameter) {
                if ((i > 0) && (i % 3 == 0)) {
                    xp += UIKnob.WIDTH + 4;
                    yp = 0;
                }
                new UIKnob(xp, yp)
                .setParameter((LXListenableNormalizedParameter) parameter)
                .addToContainer(this);
                yp += 48;
                ++i;
            }
        }
        setExpandedWidth(xp + UIKnob.WIDTH + 8);

        effect.label.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                setTitle(effect.getLabel());
            }
        });
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ENTER) {
            consumeKeyEvent();
            effect.enabled.toggle();
        } else {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }

        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
            if (keyCode == java.awt.event.KeyEvent.VK_D || keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                consumeKeyEvent();
                bus.removeEffect(effect);
            } else if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
                consumeKeyEvent();
                if (effect.getIndex() > 0) {
                    bus.moveEffect(effect, effect.getIndex() - 1);
                }
            } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
                consumeKeyEvent();
                if (effect.getIndex() < bus.getEffects().size() - 1) {
                    bus.moveEffect(effect, effect.getIndex() + 1);
                }
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        super.onKeyReleased(keyEvent, keyChar, keyCode);
    }
}