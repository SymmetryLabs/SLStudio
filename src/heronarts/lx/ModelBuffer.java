/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx;

import heronarts.lx.model.LXModel;

public class ModelBuffer implements LXBuffer {
    private int[] array;

    ModelBuffer(LX lx) {
        initArray(lx.model);

        lx.addListener(new LX.Listener() {
            @Override
            public void modelChanged(LX lx, LXModel model) {
                initArray(model);
            }
        });
    }

    private void initArray(LXModel model) {
        this.array = new int[model.size];
        for (int i = 0; i < this.array.length; ++i) {
            this.array[i] = 0;
        }
    }

    public int[] getArray() {
        return this.array;
    }

}
