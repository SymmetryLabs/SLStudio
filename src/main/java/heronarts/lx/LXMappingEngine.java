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

package heronarts.lx;

import java.util.Set;
import java.util.HashSet;

import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class LXMappingEngine {

    public enum Mode {
        OFF,
        MIDI,
        MODULATION_SOURCE,
        MODULATION_TARGET,
        TRIGGER_SOURCE,
        TRIGGER_TARGET
    };

    private LXParameter controlTarget = null;
    private Set<LXParameterListener> controlTargetListeners = new HashSet<LXParameterListener>();

    public final EnumParameter<Mode> mode = new EnumParameter<Mode>("Mode", Mode.OFF);

    LXMappingEngine() {
        mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                controlTarget = null;
            }
        });
    }

    public LXMappingEngine setMode(Mode mode) {
        this.mode.setValue(mode);
        return this;
    }

    public Mode getMode() {
        return this.mode.getEnum();
    }

    public final LXMappingEngine addControlTargetListener(LXParameterListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Cannot add null control target listener");
        }
        if (controlTargetListeners.contains(listener)) {
            throw new IllegalStateException("Cannot add duplicate control target listener " + listener);
        }
        controlTargetListeners.add(listener);
        return this;
    }

    public final LXMappingEngine removeControlTargetListener(LXParameterListener listener) {
        controlTargetListeners.remove(listener);
        return this;
    }

    public LXMappingEngine setControlTarget(LXParameter controlTarget) {
        this.controlTarget = controlTarget;
        for (LXParameterListener l : controlTargetListeners) {
            l.onParameterChanged(controlTarget);
        }
        return this;
    }

    public LXParameter getControlTarget() {
        return this.controlTarget;
    }

}
