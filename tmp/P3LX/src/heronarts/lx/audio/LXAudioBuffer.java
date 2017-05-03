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

package heronarts.lx.audio;

public class LXAudioBuffer {

    final float[] samples;
    float rms;

    LXAudioBuffer(int bufferSize) {
        this.samples = new float[bufferSize];
    }

    public int bufferSize() {
        return this.samples.length;
    }

    public float getRms() {
        return this.rms;
    }

    public synchronized void getSamples(float[] dest) {
        if (this.samples.length != dest.length) {
            throw new IllegalArgumentException("LXAudioBuffer getSamples destination array must have same length");
        }
        System.arraycopy(this.samples, 0, dest, 0, dest.length);
    }

}
