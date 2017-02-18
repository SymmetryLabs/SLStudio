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
 * A component which owns a buffer with its own view of the model. The typical
 * example of this is LXPattern.
 */
public class LXBufferedComponent extends LXLayeredComponent {

    protected LXBufferedComponent(LX lx) {
        super(lx, new ModelBuffer(lx));
    }

    public final int[] getColors() {
        return getBuffer().getArray();
    }

    @Override
    public void loop(double deltaMs) {
        super.loop(deltaMs);
    }

}
