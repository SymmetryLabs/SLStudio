/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class LXMappingEngine {

    public enum Mode {
        OFF,
        MIDI
    };

    private LXParameter controlTarget = null;

    public final DiscreteParameter mode = new DiscreteParameter("Mode", Mode.values());

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
        return (Mode) this.mode.getObject();
    }

    public LXMappingEngine setControlTarget(LXParameter controlTarget) {
        this.controlTarget = controlTarget;
        return this;
    }

    public LXParameter getControlTarget() {
        return this.controlTarget;
    }


}
