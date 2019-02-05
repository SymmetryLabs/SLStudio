package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.LX;
import com.badlogic.gdx.graphics.Camera;


public class CameraControlWindow implements Window {
    private CameraInputController cic;
    private LX lx;

    public CameraControlWindow(LX lx, CameraInputController cic) {
        this.lx = lx;
        this.cic = cic;
    }

    @Override
    public void draw() {
        UI.begin("Camera controls");
        if (UI.button("front")) {
            go(0, 0, -1, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("back")) {
            go(0, 0, 1, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("bottom")) {
            go(0, -1, 0, 0, 0, -1);
        }
        UI.sameLine();
        if (UI.button("top")) {
            go(0, 1, 0, 0, 0, 1);
        }
        UI.sameLine();
        if (UI.button("left")) {
            go(1, 0, 0, 0, 1, 0);
        }
        UI.sameLine();
        if (UI.button("right")) {
            go(-1, 0, 0, 0, 1, 0);
        }
        UI.end();
    }

    private void go(float x, float y, float z, float ux, float uy, float uz) {
        Camera cam = cic.camera;
        float rad = cam.position.dst(cic.target);
        cic.target.set(lx.model.cx, lx.model.cy, lx.model.cz);
        cam.direction.set(-x, -y, -z).nor();
        cam.position.set(x, y, z).nor().scl(rad).add(cic.target);
        cam.up.set(ux, uy, uz).nor();
        cam.update();
    }
}
