/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
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

import heronarts.lx.model.LXModel;

public abstract class LXModelComponent extends LXRunnableComponent {

    protected LXModel model;

    protected LXModelComponent(LX lx) {
        super(lx);
        this.model = lx.model;
    }

    public LXModel getModel() {
        return this.model;
    }

    public LXModelComponent setModel(LXModel model) {
        if (model == null) {
            throw new IllegalArgumentException("May not set null model");
        }
        if (this.model != model) {
            this.model = model;
            onModelChanged(model);
        }
        return this;
    }

    /**
     * Subclasses should override to handle changes to which model
     * they are addressing.
     *
     * @param model New model
     */
    protected void onModelChanged(LXModel model) {}

}
