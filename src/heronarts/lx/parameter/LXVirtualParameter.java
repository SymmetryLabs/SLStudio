/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.parameter;

/**
 * A virtual parameter is one that wraps or forwards to another real parameter.
 * Typically this is done in situations in which the parameter to forward to
 * varies based on some other contextual action or UI, for instance a virtual
 * knob that maps to whatever pattern is currently active.
 * 
 * This type of parameter is not listenable, since the underlying parameter is
 * dynamic.
 */
public abstract class LXVirtualParameter implements LXParameter {

    /**
     * The parameter to operate on.
     * 
     * @return The underlying real parameter to operate on.
     */
    protected abstract LXParameter getRealParameter();

    public final LXParameter reset() {
        LXParameter p = getRealParameter();
        if (p != null) {
            p.reset();
        }
        return this;
    }

    public final LXParameter setValue(double value) {
        LXParameter p = getRealParameter();
        if (p != null) {
            p.setValue(value);
        }
        return this;
    }

    public double getValue() {
        LXParameter p = getRealParameter();
        if (p != null) {
            return p.getValue();
        }
        return 0;
    }

    public float getValuef() {
        return (float) getValue();
    }

    public String getLabel() {
        LXParameter p = getRealParameter();
        if (p != null) {
            return p.getLabel();
        }
        return null;
    }

}
