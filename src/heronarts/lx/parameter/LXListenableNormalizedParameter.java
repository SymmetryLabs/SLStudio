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

package heronarts.lx.parameter;

/**
 * A parameter that can be listened to and has normalized values. This is needed
 * for things like UI components such as a slider or a knob, which need to be
 * able to listen for changes to the parameter value and to update it in a
 * normalized range of values.
 */
public abstract class LXListenableNormalizedParameter extends
        LXListenableParameter implements LXNormalizedParameter {

    protected LXListenableNormalizedParameter(String label, double value) {
        super(label, value);
    }

}
