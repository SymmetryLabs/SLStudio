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

package heronarts.p3lx.ui.studio.modulation;

import heronarts.lx.LX;
import heronarts.lx.modulator.VariableLFO;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIModulationSource;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UIToggleSet;
import heronarts.p3lx.ui.studio.lfo.UIWave;

public class UIVariableLFO extends UIModulator {

    private static final int WAVE_HEIGHT = 40;
    private static final int HEIGHT = 110;

    private final UIWave wave;

    public UIVariableLFO(UI ui, LX lx, VariableLFO lfo, float x, float y, float w) {
        super(ui, lx, lfo, true, x, y, w, HEIGHT);

        this.wave = new UIWave(ui, lfo, 0, 0, getContentWidth(), WAVE_HEIGHT);
        this.wave.addToContainer(this);

        new UIToggleSet(0, 44, getContentWidth(), 16)
        .setParameter(lfo.waveshape)
        .addToContainer(this);

        UI2dContainer knobs = new UI2dContainer(0, 68, getContentWidth(), UIKnob.HEIGHT);
        knobs.setLayout(UI2dContainer.Layout.HORIZONTAL).setChildMargin(2).addToContainer(this);

        new UIKnob(lfo.skew).addToContainer(knobs);
        new UIKnob(lfo.shape).addToContainer(knobs);
        new UIKnob(lfo.exp).addToContainer(knobs);
        new UIKnob(lfo.phase).addToContainer(knobs);
        new UIKnob(lfo.period).addToContainer(knobs);
        new UIKnob(lfo.tempoPeriod).addToContainer(knobs);

    }

    @Override
    protected UIModulationSource getModulationSourceUI() {
        return this.wave;
    }
}
