package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.LX;
import com.badlogic.gdx.graphics.Camera;


public class ViewController {
    private final SLCamera.InputController cameraCtrl;
    private final SLCamera camera;
    private final GnomonRenderable gnomon;
    private final MarkerRenderable markers;
    private final LX lx;

    private static final Vector3 ISO_VEC = new Vector3(0, 0, -1).rotateRad((float) Math.asin(1 / Math.sqrt(3)), 1, 0, 0);
    private static final Vector3 ISO_L = new Vector3(ISO_VEC).rotate(-45, 0, 1, 0);
    private static final Vector3 ISO_R = new Vector3(ISO_VEC).rotate(45, 0, 1, 0);

    public enum ViewDirection {
        FRONT("Front", 0, 0, -1, 0, 1, 0),
        BACK("Back", 0, 0, 1, 0, 1, 0),
        DOWN("Down", 0, 1, 0, 0, 0, 1),
        UP("Up", 0, -1, 0, 0, 0, -1),
        LEFT("Left", 1, 0, 0, 0, 1, 0),
        RIGHT("Right", -1, 0, 0, 0, 1, 0),
        ISOLEFT("Isometric left", ISO_L.x, ISO_L.y, ISO_L.z, 0, 1, 0),
        ISORIGHT("Isometric right", ISO_R.x, ISO_R.y, ISO_R.z, 0, 1, 0);

        public final String name;
        public final float x, y, z;
        public final float ux, uy, uz;

        ViewDirection(String name, float x, float y, float z, float ux, float uy, float uz) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.ux = ux;
            this.uy = uy;
            this.uz = uz;
        }
    }

    public ViewController(LX lx, SLCamera.InputController cameraCtrl, SLCamera camera, GnomonRenderable gnomon, MarkerRenderable markers) {
        this.lx = lx;
        this.cameraCtrl = cameraCtrl;
        this.camera = camera;
        this.gnomon = gnomon;
        this.markers = markers;
        TextureManager.load("icons/back.png");
        TextureManager.load("icons/down.png");
        TextureManager.load("icons/front.png");
        TextureManager.load("icons/iso-left.png");
        TextureManager.load("icons/iso-right.png");
        TextureManager.load("icons/left.png");
        TextureManager.load("icons/right.png");
        TextureManager.load("icons/up.png");
    }

    public boolean isOrtho() {
        return camera.ortho;
    }

    public void setOrtho(boolean ortho) {
        camera.ortho = ortho;
    }

    public boolean isGnomonVisible() {
        return gnomon.visible;
    }

    public void setGnomonVisible(boolean visible) {
        gnomon.visible = visible;
    }

    public boolean isMarkersVisible() {
        return markers.visible;
    }

    public void setMarkersVisible(boolean visible) {
        markers.visible = visible;
    }

    public void setViewDirection(ViewDirection vd) {
        float rad = camera.position.dst(cameraCtrl.target);
        cameraCtrl.setTargetLH(lx.model.cx, lx.model.cy, lx.model.cz);
        camera.direction.set(-vd.x, -vd.y, -vd.z).nor();
        camera.position.set(vd.x, vd.y, vd.z).nor().scl(rad).add(cameraCtrl.target);
        camera.up.set(vd.ux, vd.uy, vd.uz).nor();
        camera.update();
    }
}
