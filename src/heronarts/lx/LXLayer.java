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

package heronarts.lx;

/**
 * A layer is a components that has a run method and operates on some other
 * buffer component. The layer does not actually own the color buffer. An
 * effect is an example of a layer, or patterns may compose themselves from
 * multiple layers.
 */
public abstract class LXLayer extends LXLayeredComponent {

    protected LXLayer(LX lx) {
        super(lx);
    }

    protected LXLayer(LX lx, LXBufferedComponent buffer) {
        super(lx, buffer);
    }

    @Override
    public String getLabel() {
        return "Layer";
    }

    @Override
    protected final void onLoop(double deltaMs) {
        run(deltaMs);
    }

    /**
     * Run this layer.
     *
     * @param deltaMs Milliseconds elapsed since last frame
     */
    public abstract void run(double deltaMs);

}
