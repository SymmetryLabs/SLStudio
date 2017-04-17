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
import heronarts.lx.LXMappingEngine;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UITriggerSource;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIParameterControl;

public class UITriggerModulationButton extends UIButton implements UITriggerSource {

    private final LX lx;
    private final BooleanParameter trigger;

    public UITriggerModulationButton(final LX lx, BooleanParameter trigger, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.lx = lx;
        this.trigger = trigger;
        setLabel("\u2022");
        lx.engine.mapping.mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (lx.engine.mapping.mode.getEnum() != LXMappingEngine.Mode.TRIGGER_TARGET) {
                    setActive(false);
                }
            }
        });
    }

    @Override
    public void onToggle(boolean on) {
        if (on) {
            getUI().mapTriggerSource(this);
        } else {
            if (this.lx.engine.mapping.mode.getEnum() == LXMappingEngine.Mode.TRIGGER_TARGET) {
                this.lx.engine.mapping.mode.setValue(LXMappingEngine.Mode.OFF);
            }
        }
    }

    @Override
    public BooleanParameter getTriggerSource() {
        return this.trigger;
    }

    @Override
    public String getDescription() {
        return UIParameterControl.getDescription(this.trigger);
    }
}
