/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.ui.component;

import heronarts.lx.LXComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class UIComponentLabel extends UILabel implements LXParameterListener {

    private LXComponent component;
    private String prefix = "";

    public UIComponentLabel(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public UIComponentLabel setPrefix(String prefix) {
        if (this.prefix != prefix) {
            this.prefix = prefix;
            updateLabel();
        }
        return this;
    }

    public UIComponentLabel setComponent(LXComponent component) {
        if (this.component != component) {
            if (this.component != null) {
                LXComponent parent = this.component;
                while (parent != null) {
                    parent.label.removeListener(this);
                    parent = parent.getParent();
                }
            }
            this.component = component;
            while (component != null) {
                component.label.addListener(this);
                component = component.getParent();
            }
            updateLabel();
        }
        return this;
    }

    public void onParameterChanged(LXParameter p) {
        updateLabel();
    }

    private void updateLabel() {
        if (this.component == null) {
            setLabel("");
        } else {
            setLabel((this.prefix != null ? this.prefix : "") + this.component.getCanonicalPath());
        }
    }
}

