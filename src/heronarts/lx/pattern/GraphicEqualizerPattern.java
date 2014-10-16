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
import heronarts.lx.LXUtils;
import heronarts.lx.audio.GraphicEQ;
import heronarts.lx.color.LXColor;
import heronarts.lx.transition.WipeTransition;

public class GraphicEqualizerPattern extends LXPattern {

    private final GraphicEQ eq;

    public GraphicEqualizerPattern(LX lx) {
        super(lx);
        addModulator(this.eq = new GraphicEQ(lx.audioInput())).start();
        this.transition = new WipeTransition(lx, WipeTransition.Direction.UP);
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < this.lx.width; ++i) {
            int avgIndex = (int) (i / (double) this.lx.width * (eq.numBands - 1));
            double value = eq.getBand(avgIndex);
            for (int j = 0; j < this.lx.height; ++j) {
                double jscaled = (this.lx.height - 1 - j)
                        / (double) (this.lx.height - 1);
                double b = LXUtils.constrain(400. * (value - jscaled), 0, 100);
                this.setColor(i, j, LXColor.hsb(this.lx.getBaseHue(), 100., b));
            }
        }
    }

}