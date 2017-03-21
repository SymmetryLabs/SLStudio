/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

import heronarts.lx.LXComponent;

public class LXParameterModulation extends LXComponent {

    public final LXNormalizedParameter source;

    public final CompoundParameter target;

    public final BoundedParameter range = new BoundedParameter("Range", 0, -1, 1);

    public LXParameterModulation(LXNormalizedParameter source, CompoundParameter target) {
        this.source = source;
        this.target = target;
        addParameter(this.range);
        target.addModulation(this);
    }

    @Override
    public void dispose() {
        this.target.removeModulation(this);
        super.dispose();
    }

    @Override
    public String getLabel() {
        return this.source.getLabel() + "=>";
    }
}
