/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameterModulation;

public class UICompoundParameterControl extends UIParameterControl {

    private final List<LXListenableParameter> modulationParameters = new ArrayList<LXListenableParameter>();

    private final LXParameterListener redrawListener = new LXParameterListener() {
        @Override
        public void onParameterChanged(LXParameter p) {
            redraw();
        }
    };

    protected UICompoundParameterControl(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public UIParameterControl setParameter(LXListenableNormalizedParameter parameter) {
        for (LXListenableParameter p : this.modulationParameters) {
            p.removeListener(this.redrawListener);
        }
        this.modulationParameters.clear();
        return super.setParameter(parameter);
    }

    protected void registerModulation(LXParameterModulation modulation) {
        if (!this.modulationParameters.contains(modulation.range)) {
            this.modulationParameters.add(modulation.range);
            this.modulationParameters.add(modulation.polarity);
            modulation.range.addListener(this.redrawListener);
            modulation.polarity.addListener(this.redrawListener);
            if (!this.modulationParameters.contains(modulation.color)) {
                this.modulationParameters.add(modulation.color);
                modulation.color.addListener(this.redrawListener);
            }
        }
    }

}
