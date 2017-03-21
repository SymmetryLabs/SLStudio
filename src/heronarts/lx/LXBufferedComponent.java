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

package heronarts.lx;

/**
 * A component which owns a buffer with its own view of the model. The typical
 * example of this is LXPattern.
 */
public abstract class LXBufferedComponent extends LXLayeredComponent {

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

    @Override
    protected LXLayeredComponent setBuffer(LXBuffer buffer) {
        throw new UnsupportedOperationException("Cannot setBuffer on LXBufferedComponent, owns its own buffer");
    }

}
