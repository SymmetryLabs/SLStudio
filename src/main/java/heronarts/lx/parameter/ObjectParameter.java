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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.parameter;

public class ObjectParameter<T> extends DiscreteParameter {

    private T[] objects = null;

    public ObjectParameter(String label, T[] objects) {
        super(label, 0, objects.length);
        setObjects(objects);
    }

    public ObjectParameter(String label, T[] objects, T value) {
        this(label, objects);
        setValue(value);
    }

    @Override
    public ObjectParameter<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /**
     * Set a list of objects for the parameter
     *
     * @param options Array of arbitrary object values
     * @return this
     */
    public ObjectParameter<T> setObjects(T[] objects) {
        this.objects = objects;
        String[] options = new String[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            options[i] = objects[i].toString();
        }
        setOptions(options);
        return this;
    }

    @Override
    public DiscreteParameter setRange(int minValue, int maxValue) {
        if (this.objects!= null && (this.objects.length != maxValue - minValue)) {
            throw new UnsupportedOperationException("May not call setRange on an ObjectParameter with Object list of different length");
        }
        return super.setRange(minValue, maxValue);
    }

    public LXParameter setValue(Object object) {
        if (this.objects == null) {
            throw new UnsupportedOperationException("Cannot setValue with an object unless setObjects() was called");
        }
        for (int i = 0; i < this.objects.length; ++i) {
            if (this.objects[i] == object) {
                return setValue(i);
            }
        }
        throw new IllegalArgumentException("Not a valid object for this parameter: " + object.toString());
    }

    public T getObject() {
        return this.objects[getValuei()];
    }

}
