package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.LX;
import com.badlogic.gdx.graphics.Camera;


public class CameraControlWindow implements Window {
    private final OrthoPerspCamera.InputController cameraCtrl;
    private final OrthoPerspCamera camera;
    private final GnomonRenderable gnomon;
    private final LX lx;

    private static final Vector3 ISO_VEC = new Vector3(0, 0, -1).rotateRad((float) Math.asin(1 / Math.sqrt(3)), 1, 0, 0);
    private static final Vector3 ISO_L = new Vector3(ISO_VEC).rotate(-45, 0, 1, 0);
    private static final Vector3 ISO_R = new Vector3(ISO_VEC).rotate(45, 0, 1, 0);

    public CameraControlWindow(LX lx, OrthoPerspCamera.InputController cameraCtrl, OrthoPerspCamera camera, GnomonRenderable gnomon) {
        this.lx = lx;
        this.cameraCtrl = cameraCtrl;
        this.camera = camera;
        this.gnomon = gnomon;
        TextureManager.load("icons/back.png");
        TextureManager.load("icons/down.png");
        TextureManager.load("icons/front.png");
        TextureManager.load("icons/iso-left.png");
        TextureManager.load("icons/iso-right.png");
        TextureManager.load("icons/left.png");
        TextureManager.load("icons/right.png");
        TextureManager.load("icons/up.png");
    }

    @Override
    public void draw() {
        UI.setNextWindowPosition(UI.width / 2.f, 30, 0.5f, 0);
        UI.pushColor(UI.COLOR_WINDOW_BORDER, 0x01000000);
        UI.begin("Camera controls",
                 UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_MOVE | UI.WINDOW_NO_TITLE_BAR |
                 UI.WINDOW_NO_DOCKING | UI.WINDOW_ALWAYS_AUTO_RESIZE);
        UI.popColor();

        final float size = 16;

        if (TextureManager.button("icons/front.png", size, size)) {
            go(0, 0, -1, 0, 1, 0);
        }
        UI.sameLine();
        if (TextureManager.button("icons/back.png", size, size)) {
            go(0, 0, 1, 0, 1, 0);
        }
        UI.sameLine();
        if (TextureManager.button("icons/down.png", size, size)) {
            go(0, 1, 0, 0, 0, 1);
        }
        UI.sameLine();
        if (TextureManager.button("icons/up.png", size, size)) {
            go(0, -1, 0, 0, 0, -1);
        }
        UI.sameLine();
        if (TextureManager.button("icons/left.png", size, size)) {
            go(1, 0, 0, 0, 1, 0);
        }
        UI.sameLine();
        if (TextureManager.button("icons/right.png", size, size)) {
            go(-1, 0, 0, 0, 1, 0);
        }
        UI.sameLine();
        if (TextureManager.button("icons/iso-left.png", size, size)) {
            go(ISO_L.x, ISO_L.y, ISO_L.z, 0, 1, 0);
        }
        UI.sameLine();
        if (TextureManager.button("icons/iso-right.png", size, size)) {
            go(ISO_R.x, ISO_R.y, ISO_R.z, 0, 1, 0);
        }
        UI.sameLine();
        camera.ortho = UI.checkbox("ORTHO", camera.ortho);
        UI.sameLine();
        gnomon.visible = UI.checkbox("GNOMON", gnomon.visible);
        UI.end();
    }

    private void go(float x, float y, float z, float ux, float uy, float uz) {
        float rad = camera.position.dst(cameraCtrl.target);
        cameraCtrl.setTargetLH(lx.model.cx, lx.model.cy, lx.model.cz);
        camera.direction.set(-x, -y, -z).nor();
        camera.position.set(x, y, z).nor().scl(rad).add(cameraCtrl.target);
        camera.up.set(ux, uy, uz).nor();
        camera.update();
    }
}
