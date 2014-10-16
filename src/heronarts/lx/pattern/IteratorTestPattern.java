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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.pattern;

import heronarts.lx.LX;
import heronarts.lx.modulator.SawLFO;

/**
 * Braindead simple test pattern that iterates through all the nodes turning
 * them on one by one in fixed order.
 */
public class IteratorTestPattern extends LXPattern {

    final private SawLFO index;

    public IteratorTestPattern(LX lx) {
        super(lx);
        this.addModulator(this.index = new SawLFO(0, lx.total, lx.total * 100))
                .trigger();
    }

    public void run(double deltaMs) {
        int active = (int) Math.floor(this.index.getValue());
        for (int i = 0; i < colors.length; ++i) {
            this.colors[i] = (i == active) ? 0xFFFFFFFF : 0xFF000000;
        }
    }
}
