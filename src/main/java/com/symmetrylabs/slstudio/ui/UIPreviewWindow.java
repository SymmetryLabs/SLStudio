package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.component.UIGLPointCloud;

public class UIPreviewWindow extends UI3dContext {
        public final UIGLPointCloud pointCloud;
        private int pointSize = 3;

        public UIPreviewWindow(SLStudioLX.UI ui, P3LX lx, int x, int y, int w, int h) {
                super(ui, x, y, w, h);

                addComponent(this.pointCloud = (UIGLPointCloud) new UIGLPointCloud(lx).setPointSize(pointSize));
                setCenter(lx.model.cx, lx.model.cy, lx.model.cz);
                setRadius(lx.model.rMax * 1.5f);
                setDescription("Preview Window: Displays the main output, or the channels/groups with CUE enabled");
        }

        public int getPointSize() {
            return pointSize;
        }

        public void setPointSize(int size) {
            pointCloud.setPointSize(size);
            pointSize = size;
        }

        @Override
        protected void onResize() {
                this.pointCloud.loadShader();
        }
}
