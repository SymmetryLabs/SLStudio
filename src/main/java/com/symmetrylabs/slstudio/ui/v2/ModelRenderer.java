package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;

public class ModelRenderer extends PointColorRenderer {
    protected final PolyBuffer lxColorBuffer;
    protected final ViewController viewController;

    public ModelRenderer(LX lx, LXModel model, ViewController vc) {
        super(lx, model);
        lxColorBuffer = new PolyBuffer(lx);
        viewController = vc;
    }

    @Override
    protected void fillGLBuffer() {
        lx.engine.copyUIBuffer(lxColorBuffer, UI_COLOR_SPACE);
        int[] colors = (int[]) lxColorBuffer.getArray(UI_COLOR_SPACE);

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            glColorBuffer[4 * i    ] = (float) ((0x00FF0000 & c) >> 16) / 255.f;
            glColorBuffer[4 * i + 1] = (float) ((0x0000FF00 & c) >> 8) / 255.f;
            glColorBuffer[4 * i + 2] = (float)  (0x000000FF & c) / 255.f;
            glColorBuffer[4 * i + 3] = 1.f;
        }
    }

    @Override
    public boolean isEnabled() {
        return !viewController.isRemoteDataDisplayed();
    }
}
