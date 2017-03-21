/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
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
