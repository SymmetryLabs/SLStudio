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

package heronarts.p3lx.ui.studio.mixer;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UISlider;
import processing.core.PConstants;
import processing.core.PGraphics;

public class UIMasterStrip extends UIMixerStrip {

    public UIMasterStrip(UI ui, final LX lx, float x) {
        super(ui, lx, lx.engine.masterChannel, x);

        // Strip name
        new UILabel(PADDING, PADDING-1, this.width-2*PADDING, 16)
        .setLabel("Master")
        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
        .setTextOffset(0, 1)
        .setFontColor(ui.theme.getControlTextColor())
        .addToContainer(this);

        new UIButton(6*PADDING, 40, 28, 28)
        .setLabel("Live")
        .setParameter(lx.engine.output.enabled)
        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
        .setTextOffset(0, 2)
        .addToContainer(this);

        float syp = 22;
        new UISlider(UISlider.Direction.VERTICAL, this.width-PADDING-FADER_WIDTH, syp, FADER_WIDTH, 80)
        .setShowLabel(false)
        .setParameter(lx.engine.output.brightness)
        .addToContainer(this);

        float yp = 108;
        new UIButton(4, yp, 16, 16).setLabel("A").setParameter(lx.engine.cueA).setActiveColor(ui.theme.getAttentionColor()).addToContainer(this);
        new UILabel(26, yp + 4, 28, 13).setLabel("CUE").setTextAlignment(PConstants.LEFT, PConstants.TOP).setFont(ui.theme.getControlFont()).addToContainer(this);
        new UIButton(52, yp, 16, 16).setLabel("B").setParameter(lx.engine.cueB).setActiveColor(ui.theme.getAttentionColor()).addToContainer(this);

        new UIDropMenu(4, height - 40, width-8, 16, lx.engine.crossfaderBlendMode)
        .setDirection(UIDropMenu.Direction.UP)
        .addToContainer(this);

        new UISlider(PADDING, height - 20 - PADDING, width-2*PADDING, 20)
        .setParameter(lx.engine.crossfader)
        .setShowLabel(false)
        .addToContainer(this);
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
        super.onDraw(ui, pg);
        pg.line(1, this.height - 64, this.width-2, this.height - 64);
    }

}
