package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.LX;
import com.badlogic.gdx.graphics.Camera;


public class CameraControlWindow implements Window {
    private CameraInputController cic;
    private OrthoPerspCamera camera;
    private LX lx;

    private static final Vector3 ISO_VEC = new Vector3(0, 0, -1).rotateRad((float) Math.asin(1 / Math.sqrt(3)), 1, 0, 0);
    private static final Vector3 ISO_L = new Vector3(ISO_VEC).rotate(-45, 0, 1, 0);
    private static final Vector3 ISO_R = new Vector3(ISO_VEC).rotate(45, 0, 1, 0);

    public CameraControlWindow(LX lx, CameraInputController cic, OrthoPerspCamera camera) {
        this.lx = lx;
        this.cic = cic;
        this.camera = camera;
    }

    @Override
    public void draw() {
        UI.setNextWindowPosition(UI.width / 2.f, 30, 0.5f, 0);
        UI.pushColor(UI.COLOR_WINDOW_BORDER, 0x01000000);
        UI.begin("Camera controls",
                 UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_MOVE | UI.WINDOW_NO_TITLE_BAR |
                 UI.WINDOW_NO_DOCKING | UI.WINDOW_ALWAYS_AUTO_RESIZE);
        UI.popColor();

        if (UI.button("FRONT")) {
            go(0, 0, -1, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("BACK")) {
            go(0, 0, 1, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("TOP")) {
            go(0, 1, 0, 0, 0, 1);
        }
        UI.sameLine();
        if (UI.button("BOTTOM")) {
            go(0, -1, 0, 0, 0, -1);
        }
        UI.sameLine();
        if (UI.button("LEFT")) {
            go(1, 0, 0, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("RIGHT")) {
            go(-1, 0, 0, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("ISO-L")) {
            go(ISO_L.x, ISO_L.y, ISO_L.z, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("ISO-R")) {
            go(ISO_R.x, ISO_R.y, ISO_R.z, 0, 1, 0);
        }
        UI.sameLine();
        camera.ortho = UI.checkbox("ortho", camera.ortho);
        UI.end();
    }

    private void go(float x, float y, float z, float ux, float uy, float uz) {
        float rad = camera.position.dst(cic.target);
        cic.target.set(lx.model.cx, lx.model.cy, lx.model.cz);
        camera.direction.set(-x, -y, -z).nor();
        camera.position.set(x, y, z).nor().scl(rad).add(cic.target);
        camera.up.set(ux, uy, uz).nor();
        camera.update();
    }
}
