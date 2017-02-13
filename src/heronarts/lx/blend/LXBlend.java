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

package heronarts.lx.blend;

import heronarts.lx.LX;
import heronarts.lx.LXBuffer;

public abstract class LXBlend {

    public final LX lx;

    protected LXBlend(LX lx) {
        this.lx = lx;
    }

    public final static int NORMAL = 0;
    public final static int ADD = 1;
    public final static int MULTIPLY = 2;

    public final static String[] OPTIONS = {
        "Normal",
        "Add",
        "Multiply"
    };

    protected static final int ALPHA_SHIFT = 24;
    protected static final int R_SHIFT = 16;
    protected static final int G_SHIFT = 8;
    protected static final int R_MASK = 0x00ff0000;
    protected static final int G_MASK = 0x0000ff00;
    protected static final int B_MASK = 0x000000ff;
    protected static final int RB_MASK = R_MASK | B_MASK;

    protected static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    protected static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public void blend(int[] dst, int[] src, double alpha, LXBuffer buffer) {
        blend(dst, src, alpha, buffer.getArray());
    }

    /**
     * Blends the src buffer onto the destination buffer at the specified alpha amount.
     *
     * @param dst Destination buffer (lower layer)
     * @param src Source buffer (top layer)
     * @param alpha Alpha blend, from 0-1
     * @param output Output buffer
     */
    public abstract void blend(int[] dst, int[] src, double alpha, int[] output);
}
