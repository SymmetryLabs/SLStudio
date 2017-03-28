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

package heronarts.p3lx.ui.component;

import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIControlTarget;

public class UIButtonGroup extends UI2dContainer implements UIControlTarget {

    private final static int DEFAULT_BUTTON_MARGIN = 4;

    private final DiscreteParameter parameter;

    public final UIButton[] buttons;

    private boolean inParameterUpdate = false;

    public UIButtonGroup(DiscreteParameter parameter, float x, float y, float w, float h) {
        this(parameter, x, y, w, h, false);
    }

    public UIButtonGroup(final DiscreteParameter parameter, float x, float y, float w, float h, final boolean hideFirst) {
        super(x, y, w, h);
        setLayout(UI2dContainer.Layout.HORIZONTAL);
        setChildMargin(DEFAULT_BUTTON_MARGIN);

        this.parameter = parameter;
        int range = parameter.getRange();
        this.buttons = new UIButton[range];

        int numButtons = range - (hideFirst ? 1 : 0);
        int buttonWidth = (int) (w - (numButtons-1) * DEFAULT_BUTTON_MARGIN) / numButtons;

        for (int i = hideFirst ? 1 : 0; i < range; ++i) {
            final int iv = i;
            this.buttons[i] = new UIButton(0, 0, buttonWidth, h) {
                @Override
                public void onToggle(boolean enabled) {
                    if (!inParameterUpdate) {
                        if (enabled) {
                            parameter.setValue(iv);
                        } else if (hideFirst) {
                            parameter.setValue(0);
                        }
                    }
                }
            };
            this.buttons[i]
            .setLabel(parameter.getOptions()[i])
            .setActive(i == parameter.getValuei())
            .addToContainer(this);
        }

        parameter.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                int active = parameter.getValuei();
                inParameterUpdate = true;
                for (int i = 0; i < buttons.length; ++i) {
                    if (!hideFirst || i > 0) {
                        buttons[i].setActive(i == active);
                    }
                }
                inParameterUpdate = false;
            }
        });
    }

    @Override
    public LXParameter getControlTarget() {
        return this.parameter;
    }
}
