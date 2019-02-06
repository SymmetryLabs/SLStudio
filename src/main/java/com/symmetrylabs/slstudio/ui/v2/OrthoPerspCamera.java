package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;


/**
 * OrthoPerspCamera is a camera that can flip between orthographic and
 * perspective projection that flips screen-X to make it look like we're in a
 * left-handed coordinate system.
 */
public class OrthoPerspCamera extends Camera {
    private float perspectiveFov;
    private float orthoZoom;
    boolean ortho = false;
    private final Vector3 tmp = new Vector3();

    public OrthoPerspCamera(float width, float height, float perspectiveFov) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        this.perspectiveFov = perspectiveFov;
        this.orthoZoom = 0.6f;
    }

    @Override
    public void update() {
        update(true);
    }

    @Override
    public void update(boolean updateFrustum) {
        if (ortho) {
            projection.setToOrtho(
                orthoZoom * -viewportWidth / 2,
                orthoZoom * (viewportWidth / 2),
                orthoZoom * -(viewportHeight / 2),
                orthoZoom * viewportHeight / 2,
                near, far);
            view.setToLookAt(position, tmp.set(position).add(direction), up);
            combined.set(projection);
            Matrix4.mul(combined.val, view.val);
        } else {
            float aspect = viewportWidth / viewportHeight;
            projection.setToProjection(Math.abs(near), Math.abs(far), perspectiveFov, aspect);
            view.setToLookAt(position, tmp.set(position).add(direction), up);
            combined.set(projection);
            Matrix4.mul(combined.val, view.val);
        }
        if (updateFrustum) {
            invProjectionView.set(combined);
            Matrix4.inv(invProjectionView.val);
            frustum.update(invProjectionView);
        }
    }

    public static class InputController extends CameraInputController {
        private final float orthoZoomScale = 0.0008f;
        private final float orthoZoomMin = 0.1f;

        public InputController(OrthoPerspCamera camera) {
            super(camera);
        }

        @Override
        public boolean zoom(float amount) {
            if (super.zoom(amount)) {
                OrthoPerspCamera cam = (OrthoPerspCamera) camera;
                cam.orthoZoom = Float.max(orthoZoomMin, cam.orthoZoom - orthoZoomScale * amount);
                return true;
            }
            return false;
        }
    }
}
