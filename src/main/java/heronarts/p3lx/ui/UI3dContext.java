/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXSerializable;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * This is a layer that contains a 3d scene with a camera. Mouse movements
 * control the camera, and the scene can contain components.
 */
public class UI3dContext extends UIObject implements LXSerializable, UITabFocus {

    /**
     * Mode of interaction from keyboard mouse events
     */
    public enum InteractionMode {
        /**
         * Camera has a fixed center point, eye rotates around this point and zooms on it,
         * Y is up
         */
        ZOOM,

        /**
         * Camera has a fixed center point, eye rotates around this point and zooms on it,
         * Z is up
         */
        ZOOM_Z_UP,

        /**
         * Camera has a fixed radius, eye moves around like a FPS video-game
         */
        MOVE
    };

    private InteractionMode interactionMode = InteractionMode.ZOOM;

    private final PVector center = new PVector(0, 0, 0);

    private final PVector eye = new PVector(0, 0, 0);

    private final PVector up = new PVector(0, -1, 0);

    private final PVector centerDamped = new PVector(0, 0, 0);

    private final PVector eyeDamped = new PVector(0, 0, 0);

    // Quaternion-based free trackball orientation (no gimbal lock).
    // Home orientations: maps camera-local axes (+X right, +Y up, -Z forward) to world.
    private static final Quat HOME_Y_UP = new Quat(0, 1, 0, 0);
    private static final Quat HOME_Z_UP = new Quat(0, 0, 0.70710678f, -0.70710678f);

    private final Quat orientationTarget = new Quat(0, 1, 0, 0);
    private final Quat orientationCurrent = new Quat(0, 1, 0, 0);

    // Reusable scratch vectors to avoid per-frame allocation
    private final PVector tmpForward = new PVector();
    private final PVector tmpUp = new PVector();
    private final PVector tmpRight = new PVector();

    // Rotation sensitivity (radians per pixel) and orientation damping follow factor
    private static final float ORBIT_SENS = 0.006f;
    private static final float ORIENT_DAMP = 0.5f;

    public final BooleanParameter ortho =
        new BooleanParameter("Ortho")
        .setDescription("Orthographic projection mode");

    public final BooleanParameter phiLock =
        new BooleanParameter("PhiLock", true)
        .setDescription("Locks phi to reasonable bounds");

    /**
     * Angle of the eye position about the vertical Z-axis
     */
    public final MutableParameter theta = new MutableParameter("Theta", 0);

    /**
     * Angle of the eye position off the X-Y plane
     */
    public final MutableParameter phi = new MutableParameter("Phi", 0);

    /**
     * Roll angle of the camera (rotation around the view direction / forward axis)
     */
    public final MutableParameter roll = new MutableParameter("Roll", 0);

    /**
     * Radius of the eye positon from center of the scene
     */
    public final MutableParameter radius = new MutableParameter("Radius", 120);

    /**
     * Max velocity used to damp changes to radius (zoom)
     */
    public final MutableParameter cameraVelocity = new MutableParameter("CVel", Float.MAX_VALUE);

    /**
     * Acceleration used to change camera radius (zoom)
     */
    public final MutableParameter cameraAcceleration = new MutableParameter("CAcl", 0);

    /**
     * Max velocity used to damp changes to rotation (theta/phi)
     */
    public final MutableParameter rotationVelocity = new MutableParameter("RVel", 4*Math.PI);

    /**
     * Acceleration used to change rotation (theta/phi)
     */
    public final MutableParameter rotationAcceleration = new MutableParameter("RAcl", 0);

    /**
     * Perspective of view
     */
    public final BoundedParameter perspective = (BoundedParameter)
        new BoundedParameter("Perspective", 60, 15, 150)
        .setExponent(2)
        .setDescription("Camera perspective factor");

    /**
     * Depth of perspective field, exponential factor of radius by exp(10, Depth)
     */
    public final BoundedParameter depth =
        new BoundedParameter("Depth", 1, 0, 4)
        .setDescription("Camera's depth of perspective field");

    private final DampedParameter thetaDamped =
        new DampedParameter(this.theta, this.rotationVelocity, this.rotationAcceleration);

    private final DampedParameter phiDamped =
        new DampedParameter(this.phi, this.rotationVelocity, this.rotationAcceleration);

    private final DampedParameter rollDamped =
        new DampedParameter(this.roll, this.rotationVelocity, this.rotationAcceleration);

    private final DampedParameter radiusDamped =
        new DampedParameter(this.radius, this.cameraVelocity, this.cameraAcceleration);

    private final MutableParameter positionX = new MutableParameter();
    private final MutableParameter positionY = new MutableParameter();
    private final MutableParameter positionZ = new MutableParameter();

    private final DampedParameter xDamped = new DampedParameter(
        this.positionX, this.cameraVelocity, this.cameraAcceleration
    );

    private final DampedParameter yDamped = new DampedParameter(
        this.positionY, this.cameraVelocity, this.cameraAcceleration
    );

    private final DampedParameter zDamped = new DampedParameter(
        this.positionZ, this.cameraVelocity, this.cameraAcceleration
    );

    // Radius bounds
    private float minRadius = 1, maxRadius = Float.MAX_VALUE;

    private static final float MAX_PHI = PConstants.HALF_PI;
    private static final float PHI_UP_CORRECTION = 0.999f * PConstants.HALF_PI;

    private boolean showCenter = false;

    // Track mouse position to compute clean per-frame deltas (avoids first-frame jump)
    private float lastMx = 0, lastMy = 0;
    private boolean hasDragStart = false;
    private boolean lastMetaDown = false;
    private boolean lastShiftDown = false;
    private boolean lastAltDown = false;

    private final int x;
    private final int y;
    private PGraphics pg;

    public UI3dContext(UI ui) {
        this(ui, null, 0, 0);
    }

    public UI3dContext(UI ui, int x, int y, int w, int h) {
        this(ui, ui.applet.createGraphics(w, h, PConstants.P3D), x, y);
    }

    protected UI3dContext(UI ui, PGraphics pg, int x, int y) {
        setUI(ui);
        this.pg = pg;
        this.x = x;
        this.y = y;

        addLoopTask(this.thetaDamped);
        addLoopTask(this.phiDamped);
        addLoopTask(this.rollDamped);
        addLoopTask(this.radiusDamped);
        addLoopTask(this.xDamped);
        addLoopTask(this.yDamped);
        addLoopTask(this.zDamped);

        this.thetaDamped.start();
        this.radiusDamped.start();
        this.phiDamped.start();
        this.rollDamped.start();
        this.xDamped.start();
        this.yDamped.start();
        this.zDamped.start();

        computePosition();
        this.radius.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                double value = radius.getValue();
                if (value < minRadius || value > maxRadius) {
                    radius.setValue(LXUtils.constrain(value, minRadius, maxRadius));
                }
            }
        });
        this.phi.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (phiLock.isOn()) {
                    double value = phi.getValue();
                    if (value < -MAX_PHI || value > MAX_PHI) {
                        phi.setValue(LXUtils.constrain(value, -MAX_PHI, MAX_PHI));
                    }
                }
            }
        });
    }

    public PGraphics getGraphics() {
        return this.pg;
    }

    @Override
    public float getX() { return x; }

    @Override
    public float getY() { return y; }

    @Override
    public float getWidth() {
        return (this.pg == null) ? this.ui.applet.width : this.pg.width;
    }

    @Override
    public float getHeight() {
        return (this.pg == null) ? this.ui.applet.height : this.pg.height;
    }

    public UI3dContext setSize(float width, float height) {
        if (this.pg == null) {
            throw new UnsupportedOperationException("Cannot resize UI3dContext created with no size.");
        } else {
            this.pg.dispose();
            // Processing createGraphics requires a non-null context ClassLoader on some
            // platforms (notably macOS + JOGL). The NEWT/JOGL resize thread may have none.
            Thread currentThread = Thread.currentThread();
            ClassLoader originalClassLoader = currentThread.getContextClassLoader();
            if (originalClassLoader == null) {
                currentThread.setContextClassLoader(this.ui.applet.getClass().getClassLoader());
            }
            try {
                this.pg = this.ui.applet.createGraphics((int) width, (int) height, PConstants.P3D);
            } finally {
                currentThread.setContextClassLoader(originalClassLoader);
            }
            onResize();
        }
        return this;
    }

    protected void onResize() {}


    /**
     * Adds a component to the layer
     *
     * @param component Component
     * @return this
     */
    public final UI3dContext addComponent(UI3dComponent component) {
        this.mutableChildren.add(component);
        return this;
    }

    /**
     * Removes a component from the layer
     *
     * @param component Component
     * @return this
     */
    public final UI3dContext removeComponent(UI3dComponent component) {
        this.mutableChildren.remove(component);
        return this;
    }

    /**
     * Set radius of the camera
     *
     * @param radius Camera radius
     * @return this
     */
    public UI3dContext setRadius(float radius) {
        this.radius.setValue(radius);
        return this;
    }

    /**
     * Set interaction mode for mouse/key events.
     *
     * @param interactionMode Interaction mode
     * @return this
     */
    public UI3dContext setInteractionMode(InteractionMode interactionMode) {
        if (this.interactionMode != interactionMode) {
            this.interactionMode = interactionMode;
            PVector position = this.center;
            switch (interactionMode) {
            case ZOOM:
            case MOVE:
                this.orientationTarget.set(HOME_Y_UP);
                this.orientationCurrent.set(HOME_Y_UP);
                break;
            case ZOOM_Z_UP:
                this.orientationTarget.set(HOME_Z_UP);
                this.orientationCurrent.set(HOME_Z_UP);
                break;
            }
            switch (interactionMode) {
            case ZOOM:
            case ZOOM_Z_UP:
                position = this.center;
                break;
            case MOVE:
                position = this.eye;
                break;
            }
            this.positionX.setValue(position.x);
            this.positionY.setValue(position.y);
            this.positionZ.setValue(position.z);
            this.xDamped.setValue(position.x);
            this.yDamped.setValue(position.y);
            this.zDamped.setValue(position.z);
        }
        return this;
    }

    /**
     * Sets perspective angle of the camera in degrees
     *
     * @param perspective Angle in degrees
     * @return this
     */
    public UI3dContext setPerspective(float perspective) {
        this.perspective.setValue(perspective);
        return this;
    }

    /**
     * Sets the camera's maximum zoom speed
     *
     * @param cameraVelocity Max units/per second radius may change by
     * @return this
     */
    public UI3dContext setCameraVelocity(float cameraVelocity) {
        this.cameraVelocity.setValue(cameraVelocity);
        return this;
    }

    /**
     * Set's the camera's zoom acceleration, 0 is infinite
     *
     * @param cameraAcceleration Acceleration for camera
     * @return this
     */
    public UI3dContext setCameraAcceleration(float cameraAcceleration) {
        this.cameraAcceleration.setValue(cameraAcceleration);
        return this;
    }

    /**
     * Sets the camera's maximum rotation speed
     *
     * @param rotationVelocity Max radians/per second viewing angle may change by
     * @return this
     */
    public UI3dContext setRotationVelocity(float rotationVelocity) {
        this.rotationVelocity.setValue(rotationVelocity);
        return this;
    }

    /**
     * Set's the camera's rotational acceleration, 0 is infinite
     *
     * @param rotationAcceleration Acceleration of camera rotation
     * @return this
     */
    public UI3dContext setRotationAcceleration(float rotationAcceleration) {
        this.rotationAcceleration.setValue(rotationAcceleration);
        return this;
    }

    /**
     * Set the theta angle of viewing
     *
     * @param theta Angle about the y axis
     * @return this
     */
    public UI3dContext setTheta(double theta) {
        this.theta.setValue(theta);
        applyThetaPhiToOrientation(false);
        return this;
    }

    /**
     * Set the phi angle of viewing
     *
     * @param phi Angle about the y axis
     * @return this
     */
    public UI3dContext setPhi(float phi) {
        this.phi.setValue(phi);
        applyThetaPhiToOrientation(false);
        return this;
    }

    /**
     * Rebuilds the orientation quaternion from the legacy theta/phi parameters,
     * preserving compatibility with view-preset buttons and saved projects.
     *
     * @param snap if true, snaps the displayed orientation immediately; otherwise animates
     */
    private void applyThetaPhiToOrientation(boolean snap) {
        double t = this.theta.getValue();
        double p = this.phi.getValue();
        float cphi = (float) Math.cos(p);
        float sphi = (float) Math.sin(p);
        float sth = (float) Math.sin(t);
        float cth = (float) Math.cos(t);

        if (this.interactionMode == InteractionMode.ZOOM_Z_UP) {
            // Z-up: eye_dir = (-cphi*sth, -cphi*cth, sphi); forward = -eye_dir
            this.tmpForward.set(cphi * sth, cphi * cth, -sphi);
            this.tmpUp.set(0, 0, -1);
        } else {
            // Y-up: eye_dir = (cphi*sth, sphi, -cphi*cth); forward = -eye_dir
            this.tmpForward.set(-cphi * sth, -sphi, cphi * cth);
            this.tmpUp.set(0, -1, 0);
        }
        Quat q = Quat.fromForwardUp(this.tmpForward, this.tmpUp);
        this.orientationTarget.set(q);
        if (snap) {
            this.orientationCurrent.set(q);
        }
    }

    /**
     * Sets bounds on the radius
     *
     * @param minRadius Minimum camera radius
     * @param maxRadius Maximum camera radius
     * @return this
     */
    public UI3dContext setRadiusBounds(float minRadius, float maxRadius) {
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        setRadius(LXUtils.constrainf(this.radius.getValuef(), minRadius, maxRadius));
        return this;
    }

    /**
     * Set minimum radius
     *
     * @param minRadius Minimum camera radius
     * @return this
     */
    public UI3dContext setMinRadius(float minRadius) {
        return setRadiusBounds(minRadius, this.maxRadius);
    }

    /**
     * Set maximum radius
     *
     * @param maxRadius Maximum camera radius
     * @return this
     */
    public UI3dContext setMaxRadius(float maxRadius) {
        return setRadiusBounds(this.minRadius, maxRadius);
    }

    /**
     * Determines whether to render a point at the center
     *
     * @param showCenter Draw a dot at center of context
     * @return this
     */
    public final UI3dContext showCenterPoint(boolean showCenter) {
        this.showCenter = showCenter;
        return this;
    }

    /**
     * Toggles visibility of a center point
     *
     * @return this
     */
    public final UI3dContext toggleCenterPoint() {
        return showCenterPoint(!this.showCenter);
    }

    /**
     * Sets the center of the scene, only respected in ZOOM mode
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param z Z-coordinate
     * @return this
     */
    public UI3dContext setCenter(float x, float y, float z) {
        if (this.interactionMode != InteractionMode.ZOOM && this.interactionMode != InteractionMode.ZOOM_Z_UP) {
            throw new IllegalStateException("setCenter() only allowed in ZOOM modes");
        }
        this.positionX.setValue(this.center.x = x);
        this.positionY.setValue(this.center.y = y);
        this.positionZ.setValue(this.center.z = z);
        return this;
    }

    /**
     * Sets the eye position, only respected in MOVE mode
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param z Z-coordinate
     * @return this
     */
    public UI3dContext setEye(float x, float y, float z) {
        if (this.interactionMode != InteractionMode.MOVE) {
            throw new IllegalStateException("setCenter() only allowed in MOVE mode");
        }
        this.positionX.setValue(this.eye.x = x);
        this.positionY.setValue(this.eye.y = y);
        this.positionZ.setValue(this.eye.z = z);
        return this;
    }

    /**
     * Gets the center position of the scene
     *
     * @return center of scene
     */
    public PVector getCenter() {
        return this.center;
    }

    /**
     * Gets the latest computed eye position
     *
     * @return eye position
     */
    public PVector getEye() {
        return this.eye;
    }

    private void computePosition() {
        // Smoothly follow the target orientation (free trackball, no gimbal lock)
        this.orientationCurrent.slerp(this.orientationTarget, ORIENT_DAMP);

        float rv = this.radiusDamped.getValuef();
        float px = this.xDamped.getValuef();
        float py = this.yDamped.getValuef();
        float pz = this.zDamped.getValuef();

        // Derive world-space camera basis from the current orientation
        this.orientationCurrent.rotateVector(0, 0, -1, this.tmpForward); // view direction
        this.orientationCurrent.rotateVector(0, 1, 0, this.tmpUp);       // up direction

        if (this.interactionMode == InteractionMode.MOVE) {
            // Eye is the fixed pivot; look outward along forward
            this.eyeDamped.set(px, py, pz);
            this.centerDamped.set(
                px + this.tmpForward.x * rv,
                py + this.tmpForward.y * rv,
                pz + this.tmpForward.z * rv
            );
        } else {
            // ZOOM / ZOOM_Z_UP: center is the fixed pivot; eye orbits around it
            this.centerDamped.set(px, py, pz);
            this.eyeDamped.set(
                px - this.tmpForward.x * rv,
                py - this.tmpForward.y * rv,
                pz - this.tmpForward.z * rv
            );
        }
        this.up.set(this.tmpUp.x, this.tmpUp.y, this.tmpUp.z);
        this.eye.set(this.eyeDamped);
        this.center.set(this.centerDamped);
    }

    /**
     * View-relative trackball orbit. Rotates the camera around its own local up
     * (yaw) and right (pitch) axes, so behavior is consistent from any angle with
     * no gimbal lock.
     *
     * @param yaw   rotation amount around local up axis (radians)
     * @param pitch rotation amount around local right axis (radians)
     */
    private void orbit(float yaw, float pitch) {
        Quat qYaw = Quat.fromAxisAngle(0, 1, 0, yaw);
        Quat qPitch = Quat.fromAxisAngle(1, 0, 0, pitch);
        Quat result = this.orientationTarget.mult(qYaw).mult(qPitch);
        result.normalize();
        this.orientationTarget.set(result);
    }

    /**
     * Rolls the camera around its forward viewing axis (banking).
     *
     * @param angle roll amount (radians)
     */
    private void rollView(float angle) {
        Quat qRoll = Quat.fromAxisAngle(0, 0, -1, angle);
        Quat result = this.orientationTarget.mult(qRoll);
        result.normalize();
        this.orientationTarget.set(result);
    }

    @Override
    public final void draw(UI ui, PGraphics dstPg) {
        if (!isVisible()) {
            return;
        }

        PGraphics pg = dstPg;
        if (this.pg != null) {
            pg = this.pg;
            pg.beginDraw();
            pg.clear();
        }

        // Set the camera
        computePosition();
        pg.camera(
            this.eyeDamped.x, this.eyeDamped.y, this.eyeDamped.z,
            this.centerDamped.x, this.centerDamped.y, this.centerDamped.z,
            this.up.x, this.up.y, this.up.z
        );

        // Set perspective projection
        float radiusValue = this.radiusDamped.getValuef();
        float aspect = pg.width / (float) pg.height;
        if (this.ortho.isOn()) {
            float halfRadius = radiusValue * .5f;
            pg.ortho(aspect * -halfRadius, aspect * halfRadius, -halfRadius, halfRadius);
        } else {
            float depthFactor = (float) Math.pow(10, this.depth.getValue());
            float nearClip = Math.max(0.5f, radiusValue / depthFactor);
            float farClip = radiusValue * depthFactor * 100f;
            pg.perspective(
                this.perspective.getValuef() / 180.f * PConstants.PI,
                aspect,
                nearClip,
                farClip
            );
        }

        if (ui.coordinateSystem == UI.CoordinateSystem.RIGHT_HANDED) {
            pg.scale(1, 1, -1);
        }

        // Enable depth test
        pg.hint(PConstants.ENABLE_DEPTH_TEST);

        // Draw all the components in the scene
        beginDraw(ui, pg);
        if (this.showCenter) {
            drawCenterDot(pg);
        }
        for (UIObject child : this.mutableChildren) {
            child.draw(ui, pg);
        }
        endDraw(ui, pg);

        // Reset the depth test, camera and perspective
        pg.hint(PConstants.DISABLE_DEPTH_TEST);
        pg.camera();
        pg.perspective();

        if (hasFocus()) {
            drawFocusBorder(ui, pg);
        }

        if (this.pg != null) {
            this.pg.endDraw();
            dstPg.image(this.pg, 0, 0);
        }
    }

    private void drawCenterDot(PGraphics pg) {
        pg.stroke(LXColor.RED);
        pg.strokeWeight(10);
        pg.beginShape(PConstants.POINTS);
        pg.vertex(this.xDamped.getValuef(), this.yDamped.getValuef(), this.zDamped.getValuef());
        pg.endShape();
        pg.strokeWeight(1);
    }

    private void drawFocusBorder(UI ui, PGraphics pg) {
        pg.strokeWeight(1);
        pg.stroke(ui.theme.getFocusColor());
        float focusInset = .5f;
        int focusDash = 10;
        // Top left
        pg.line(focusInset, focusInset, focusInset + focusDash, focusInset);
        pg.line(focusInset, focusInset, focusInset, focusInset + focusDash);
        // Top right
        pg.line(pg.width - focusInset, focusInset, pg.width - focusInset - focusDash, focusInset);
        pg.line(pg.width - focusInset, focusInset, pg.width - focusInset, focusInset + focusDash);
        // Bottom left
        pg.line(focusInset, pg.height - focusInset, focusInset + focusDash, pg.height - focusInset);
        pg.line(focusInset, pg.height - focusInset, focusInset, pg.height - focusInset - focusDash);
        // Bottom right
        pg.line(pg.width - focusInset, pg.height - focusInset, pg.width - focusInset - focusDash, pg.height - focusInset);
        pg.line(pg.width - focusInset, pg.height - focusInset, pg.width - focusInset, pg.height - focusInset - focusDash);
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mouseEvent.getCount() > 1) {
            focus();
        }
        lastMx = mx;
        lastMy = my;
        hasDragStart = false;
        lastMetaDown = mouseEvent.isMetaDown() || mouseEvent.isControlDown();
        lastShiftDown = mouseEvent.isShiftDown();
        lastAltDown = mouseEvent.isAltDown();
    }

    @Override
    public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        hasDragStart = false;
    }

    @Override
    public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        boolean metaDown = mouseEvent.isMetaDown() || mouseEvent.isControlDown();
        boolean shiftDown = mouseEvent.isShiftDown();
        boolean altDown = mouseEvent.isAltDown();
        // Compute our own delta to avoid accumulated first-frame jumps.
        // Also reset if modifier state changed, or if the raw Processing delta is suspiciously
        // large (> 40px), which indicates a missed press event (e.g. app focus click on macOS).
        if (!hasDragStart || metaDown != lastMetaDown || shiftDown != lastShiftDown || altDown != lastAltDown) {
            hasDragStart = true;
            lastMetaDown = metaDown;
            lastShiftDown = shiftDown;
            lastAltDown = altDown;
            lastMx = mx;
            lastMy = my;
            return;  // skip this event entirely — no accumulated delta to apply
        }
        lastMetaDown = metaDown;
        lastShiftDown = shiftDown;
        lastAltDown = altDown;
        float safeDx = mx - lastMx;
        float safeDy = my - lastMy;
        lastMx = mx;
        lastMy = my;

        // Unified view-relative controls for all interaction modes:
        //   Shift + drag  -> zoom
        //   Alt + drag    -> roll (around forward axis)
        //   Cmd/Ctrl drag -> pan (perpendicular to view)
        //   drag          -> orbit (free trackball)
        if (shiftDown) {
            this.radius.incrementValue(safeDy);
        } else if (altDown) {
            rollView(safeDx * ORBIT_SENS);
        } else if (metaDown) {
            pan(safeDx, safeDy);
        } else {
            orbit(-safeDx * ORBIT_SENS, safeDy * ORBIT_SENS);
        }
    }

    /**
     * View-relative pan: slides the pivot (center in ZOOM modes, eye in MOVE)
     * along the camera's screen-right and screen-up axes.
     */
    private void pan(float safeDx, float safeDy) {
        this.orientationCurrent.rotateVector(1, 0, 0, this.tmpRight); // screen right in world
        this.orientationCurrent.rotateVector(0, 1, 0, this.tmpUp);    // screen up in world

        float panScale = this.radiusDamped.getValuef() / 500f;
        float cdx = LXUtils.constrainf(safeDx, -30, 30) * panScale;
        float cdy = LXUtils.constrainf(safeDy, -30, 30) * panScale;

        // Drag right -> content follows cursor -> pivot moves left along screen-right.
        // Drag down  -> content follows cursor -> pivot moves down  along screen-up.
        float dxw = -this.tmpRight.x * cdx - this.tmpUp.x * cdy;
        float dyw = -this.tmpRight.y * cdx - this.tmpUp.y * cdy;
        float dzw = -this.tmpRight.z * cdx - this.tmpUp.z * cdy;

        if (this.interactionMode == InteractionMode.MOVE) {
            setEye(this.eye.x + dxw, this.eye.y + dyw, this.eye.z + dzw);
        } else {
            setCenter(this.center.x + dxw, this.center.y + dyw, this.center.z + dzw);
        }
    }

    @Override
    public void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
        switch (this.interactionMode) {
        case ZOOM:
        case ZOOM_Z_UP:
            this.radius.incrementValue(delta * this.radius.getValue() / 1000.);
            break;
        case MOVE:
            this.orientationCurrent.rotateVector(0, 0, -1, this.tmpForward);
            setEye(
                this.eye.x + this.tmpForward.x * delta,
                this.eye.y + this.tmpForward.y * delta,
                this.eye.z + this.tmpForward.z * delta
            );
            break;
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        float amount = .02f;
        if (keyEvent.isShiftDown()) {
            amount *= 10.f;
        }
        if (this.interactionMode == InteractionMode.MOVE) {
            amount *= -1;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            consumeKeyEvent();
            orbit(amount, 0);
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
            consumeKeyEvent();
            orbit(-amount, 0);
        } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
            consumeKeyEvent();
            orbit(0, -amount);
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
            consumeKeyEvent();
            orbit(0, amount);
        }
    }

    private static final String KEY_RADIUS = "radius";
    private static final String KEY_THETA = "theta";
    private static final String KEY_PHI = "phi";
    private static final String KEY_ROLL = "roll";
    private static final String KEY_POSITION_X = "positionX";
    private static final String KEY_POSITION_Y = "positionY";
    private static final String KEY_POSITION_Z = "positionZ";
    private static final String KEY_ORIENT_W = "orientW";
    private static final String KEY_ORIENT_X = "orientX";
    private static final String KEY_ORIENT_Y = "orientY";
    private static final String KEY_ORIENT_Z = "orientZ";

    @Override
    public void save(LX lx, JsonObject object) {
        object.addProperty(KEY_RADIUS, this.radius.getValue());
        // Legacy theta/phi/roll retained for backward compatibility with older builds
        object.addProperty(KEY_THETA, this.theta.getValue());
        object.addProperty(KEY_PHI, this.phi.getValue());
        object.addProperty(KEY_ROLL, this.roll.getValue());
        object.addProperty(KEY_POSITION_X, this.positionX.getValue());
        object.addProperty(KEY_POSITION_Y, this.positionY.getValue());
        object.addProperty(KEY_POSITION_Z, this.positionZ.getValue());
        // Quaternion orientation is the source of truth for the trackball camera
        object.addProperty(KEY_ORIENT_W, this.orientationTarget.w);
        object.addProperty(KEY_ORIENT_X, this.orientationTarget.x);
        object.addProperty(KEY_ORIENT_Y, this.orientationTarget.y);
        object.addProperty(KEY_ORIENT_Z, this.orientationTarget.z);
    }

    @Override
    public void load(LX lx, JsonObject object) {
        if (object.has(KEY_RADIUS)) {
            this.radius.setValue(object.get(KEY_RADIUS).getAsDouble());
        }
        if (object.has(KEY_THETA)) {
            this.theta.setValue(object.get(KEY_THETA).getAsDouble());
        }
        if (object.has(KEY_PHI)) {
            this.phi.setValue(object.get(KEY_PHI).getAsDouble());
        }
        if (object.has(KEY_ROLL)) {
            this.roll.setValue(object.get(KEY_ROLL).getAsDouble());
        }
        if (object.has(KEY_POSITION_X)) {
            this.positionX.setValue(object.get(KEY_POSITION_X).getAsDouble());
        }
        if (object.has(KEY_POSITION_Y)) {
            this.positionY.setValue(object.get(KEY_POSITION_Y).getAsDouble());
        }
        if (object.has(KEY_POSITION_Z)) {
            this.positionZ.setValue(object.get(KEY_POSITION_Z).getAsDouble());
        }
        // Restore orientation: prefer the quaternion; fall back to legacy theta/phi
        if (object.has(KEY_ORIENT_W) && object.has(KEY_ORIENT_X)
            && object.has(KEY_ORIENT_Y) && object.has(KEY_ORIENT_Z)) {
            float qw = object.get(KEY_ORIENT_W).getAsFloat();
            float qx = object.get(KEY_ORIENT_X).getAsFloat();
            float qy = object.get(KEY_ORIENT_Y).getAsFloat();
            float qz = object.get(KEY_ORIENT_Z).getAsFloat();
            this.orientationTarget.set(qw, qx, qy, qz);
            this.orientationTarget.normalize();
            this.orientationCurrent.set(this.orientationTarget);
        } else {
            applyThetaPhiToOrientation(true);
        }
        // Sync this.center so cmd+drag panning uses the correct base position
        this.center.x = this.positionX.getValuef();
        this.center.y = this.positionY.getValuef();
        this.center.z = this.positionZ.getValuef();
    }

    /**
     * Minimal unit-quaternion helper for the free trackball camera.
     * Rotation convention: rotateVector applies q * v * q^-1.
     */
    private static class Quat {
        float w, x, y, z;

        Quat(float w, float x, float y, float z) {
            this.w = w; this.x = x; this.y = y; this.z = z;
        }

        void set(Quat q) {
            this.w = q.w; this.x = q.x; this.y = q.y; this.z = q.z;
        }

        void set(float w, float x, float y, float z) {
            this.w = w; this.x = x; this.y = y; this.z = z;
        }

        static Quat fromAxisAngle(float ax, float ay, float az, float angle) {
            float half = angle * 0.5f;
            float s = (float) Math.sin(half);
            return new Quat((float) Math.cos(half), ax * s, ay * s, az * s);
        }

        /** Returns this * q as a new quaternion (apply q first, then this). */
        Quat mult(Quat q) {
            return new Quat(
                w * q.w - x * q.x - y * q.y - z * q.z,
                w * q.x + x * q.w + y * q.z - z * q.y,
                w * q.y - x * q.z + y * q.w + z * q.x,
                w * q.z + x * q.y - y * q.x + z * q.w
            );
        }

        void normalize() {
            float n = (float) Math.sqrt(w * w + x * x + y * y + z * z);
            if (n > 1e-9f) {
                w /= n; x /= n; y /= n; z /= n;
            }
        }

        /** Rotates vector (vx,vy,vz) by this quaternion, storing the result in out. */
        void rotateVector(float vx, float vy, float vz, PVector out) {
            float tx = 2f * (y * vz - z * vy);
            float ty = 2f * (z * vx - x * vz);
            float tz = 2f * (x * vy - y * vx);
            out.x = vx + w * tx + (y * tz - z * ty);
            out.y = vy + w * ty + (z * tx - x * tz);
            out.z = vz + w * tz + (x * ty - y * tx);
        }

        /** Spherically interpolate this toward target by t, storing into this. */
        void slerp(Quat target, float t) {
            float dot = w * target.w + x * target.x + y * target.y + z * target.z;
            float tw = target.w, tx = target.x, ty = target.y, tz = target.z;
            if (dot < 0f) {
                tw = -tw; tx = -tx; ty = -ty; tz = -tz; dot = -dot;
            }
            if (dot > 0.9995f) {
                w += t * (tw - w);
                x += t * (tx - x);
                y += t * (ty - y);
                z += t * (tz - z);
                normalize();
                return;
            }
            float theta0 = (float) Math.acos(dot);
            float theta = theta0 * t;
            float sinTheta = (float) Math.sin(theta);
            float sinTheta0 = (float) Math.sin(theta0);
            float s0 = (float) Math.cos(theta) - dot * sinTheta / sinTheta0;
            float s1 = sinTheta / sinTheta0;
            w = s0 * w + s1 * tw;
            x = s0 * x + s1 * tx;
            y = s0 * y + s1 * ty;
            z = s0 * z + s1 * tz;
        }

        /**
         * Builds an orientation quaternion from a desired world forward (eye->center)
         * and up vector. Local axes map: +X=right, +Y=up, -Z=forward.
         */
        static Quat fromForwardUp(PVector forward, PVector up) {
            float fx = forward.x, fy = forward.y, fz = forward.z;
            float fl = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
            if (fl < 1e-9f) return new Quat(1, 0, 0, 0);
            fx /= fl; fy /= fl; fz /= fl;
            // back = -forward (camera local +Z)
            float bx = -fx, by = -fy, bz = -fz;
            // right = up x back
            float rx = up.y * bz - up.z * by;
            float ry = up.z * bx - up.x * bz;
            float rz = up.x * by - up.y * bx;
            float rl = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
            if (rl < 1e-9f) { rx = 1; ry = 0; rz = 0; } else { rx /= rl; ry /= rl; rz /= rl; }
            // trueUp = back x right
            float ux = by * rz - bz * ry;
            float uy = bz * rx - bx * rz;
            float uz = bx * ry - by * rx;
            // Columns of rotation matrix = images of local X,Y,Z = right, trueUp, back
            float m00 = rx, m01 = ux, m02 = bx;
            float m10 = ry, m11 = uy, m12 = by;
            float m20 = rz, m21 = uz, m22 = bz;
            float tr = m00 + m11 + m22;
            float w, x, y, z, s;
            if (tr > 0) {
                s = (float) Math.sqrt(tr + 1f) * 2f;
                w = 0.25f * s;
                x = (m21 - m12) / s;
                y = (m02 - m20) / s;
                z = (m10 - m01) / s;
            } else if (m00 > m11 && m00 > m22) {
                s = (float) Math.sqrt(1f + m00 - m11 - m22) * 2f;
                w = (m21 - m12) / s;
                x = 0.25f * s;
                y = (m01 + m10) / s;
                z = (m02 + m20) / s;
            } else if (m11 > m22) {
                s = (float) Math.sqrt(1f + m11 - m00 - m22) * 2f;
                w = (m02 - m20) / s;
                x = (m01 + m10) / s;
                y = 0.25f * s;
                z = (m12 + m21) / s;
            } else {
                s = (float) Math.sqrt(1f + m22 - m00 - m11) * 2f;
                w = (m10 - m01) / s;
                x = (m02 + m20) / s;
                y = (m12 + m21) / s;
                z = 0.25f * s;
            }
            Quat q = new Quat(w, x, y, z);
            q.normalize();
            return q;
        }
    }

}
