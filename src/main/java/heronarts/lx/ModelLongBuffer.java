package heronarts.lx;

import heronarts.lx.model.LXModel;

public class ModelLongBuffer implements Buffer {
    private long[] array;

    public ModelLongBuffer(LX lx) {
        initArray(lx.model);

        lx.addListener(new LX.Listener() {
            @Override
            public void modelChanged(LX lx, LXModel model) {
                initArray(model);
            }
        });
    }

    private void initArray(LXModel model) {
        this.array = new long[model.size];  // initialized to 0 by Java
    }

    public long[] getArray() {
        return this.array;
    }
}
