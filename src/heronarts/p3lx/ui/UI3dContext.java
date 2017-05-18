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

import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
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
public class UI3dContext extends UIObject implements UITabFocus {

    /**
     * Mode of interaction from keyboard mouse events
     */
    public enum InteractionMode {
        /**
         * Camera has a fixed center point, eye rotates around this point and zooms on it
         */
        ZOOM,

        /**
         * Camera has a fixed radius, eye moves around like a FPS video-game
         */
        MOVE
    };

    private InteractionMode interactionMode = InteractionMode.ZOOM;

    private final PVector center = new PVector(0, 0, 0);

    private final PVector eye = new PVector(0, 0, 0);

    private final PVector centerDamped = new PVector(0, 0, 0);

    private final PVector eyeDamped = new PVector(0, 0, 0);

    /**
     * Angle of the eye position about the vertical Z-axis
     */
    public final MutableParameter theta = new MutableParameter("Theta", 0);

    /**
     * Angle of the eye position off the X-Y plane
     */
    public final MutableParameter phi = new MutableParameter("Phi", 0);

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
    public final BoundedParameter perspective = new BoundedParameter("Perspective", 60, 30, 150);

    /**
     * Depth of perspective field, exponential factor of radius by exp(10, Depth)
     */
    public final BoundedParameter depth = new BoundedParameter("Depth", 1, 0, 4);

    private final DampedParameter thetaDamped =
        new DampedParameter(this.theta, this.rotationVelocity, this.rotationAcceleration);

    private final DampedParameter phiDamped =
        new DampedParameter(this.phi, this.rotationVelocity, this.rotationAcceleration);

    private final DampedParameter radiusDamped =
        new DampedParameter(this.radius, this.cameraVelocity, this.cameraAcceleration);

    private final MutableParameter pxParameter = new MutableParameter();
    private final MutableParameter pyParameter = new MutableParameter();
    private final MutableParameter pzParameter = new MutableParameter();

    private final DampedParameter pxDamped = new DampedParameter(
        this.pxParameter, this.cameraVelocity, this.cameraAcceleration
    );

    private final DampedParameter pyDamped = new DampedParameter(
        this.pyParameter, this.cameraVelocity, this.cameraAcceleration
    );

    private final DampedParameter pzDamped = new DampedParameter(
        this.pzParameter, this.cameraVelocity, this.cameraAcceleration
    );

    // Radius bounds
    private float minRadius = 1, maxRadius = Float.MAX_VALUE;

    private static final float MAX_PHI = PConstants.HALF_PI * .9f;

    private boolean showCenter = false;

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
        addLoopTask(this.radiusDamped);
        addLoopTask(this.pxDamped);
        addLoopTask(this.pyDamped);
        addLoopTask(this.pzDamped);

        this.thetaDamped.start();
        this.radiusDamped.start();
        this.phiDamped.start();
        this.pxDamped.start();
        this.pyDamped.start();
        this.pzDamped.start();

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
                double value = phi.getValue();
                if (value < -MAX_PHI || value > MAX_PHI) {
                    phi.setValue(LXUtils.constrain(value, -MAX_PHI, MAX_PHI));
                }
            }
        });
    }

    public PGraphics getGraphics() {
        return this.pg;
    }

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
            this.pg = this.ui.applet.createGraphics((int) width, (int) height, PConstants.P3D);
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
                position = this.center;
                break;
            case MOVE:
                position = this.eye;
                break;
            }
            this.pxParameter.setValue(position.x);
            this.pyParameter.setValue(position.y);
            this.pzParameter.setValue(position.z);
            this.pxDamped.setValue(position.x);
            this.pyDamped.setValue(position.y);
            this.pzDamped.setValue(position.z);
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
    public UI3dContext setTheta(float theta) {
        this.theta.setValue(theta);
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
        return this;
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
        if (this.interactionMode != InteractionMode.ZOOM) {
            throw new IllegalStateException("setCenter() only allowed in ZOOM mode");
        }
        this.pxParameter.setValue(this.center.x = x);
        this.pyParameter.setValue(this.center.y = y);
        this.pzParameter.setValue(this.center.z = z);
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
        this.pxParameter.setValue(this.eye.x = x);
        this.pyParameter.setValue(this.eye.y = y);
        this.pzParameter.setValue(this.eye.z = z);
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
        float rv = this.radiusDamped.getValuef();
        double tv = this.thetaDamped.getValue();
        double pv = this.phiDamped.getValue();

        float sintheta = (float) Math.sin(tv);
        float costheta = (float) Math.cos(tv);
        float sinphi = (float) Math.sin(pv);
        float cosphi = (float) Math.cos(pv);

        float px = this.pxDamped.getValuef();
        float py = this.pyDamped.getValuef();
        float pz = this.pzDamped.getValuef();

        switch (this.interactionMode) {
        case ZOOM:
            this.centerDamped.set(px, py, pz);
            this.eyeDamped.set(
                px + rv * cosphi * sintheta,
                py + rv * sinphi,
                pz - rv * cosphi * costheta
            );
            this.eye.set(this.eyeDamped);
            break;
        case MOVE:
            this.eyeDamped.set(px, py, pz);
            this.centerDamped.set(
                px + rv * cosphi * sintheta,
                py + rv * sinphi,
                pz + rv * cosphi * costheta
            );
            this.center.set(this.centerDamped);
            break;
        }
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
            0, -1, 0
        );

        // Set perspective projection
        float radiusValue = this.radiusDamped.getValuef();
        float depthFactor = (float) Math.pow(10, this.depth.getValue());
        pg.perspective(
            this.perspective.getValuef() / 180.f * PConstants.PI,
            pg.width / (float) pg.height,
            radiusValue / depthFactor,
            radiusValue * depthFactor
        );

        // Enable depth test
        pg.hint(PConstants.ENABLE_DEPTH_TEST);

        // Draw all the components in the scene
        this.beforeDraw(ui, pg);
        if (this.showCenter) {
            drawCenterDot(pg);
        }
        for (UIObject child : this.mutableChildren) {
            child.draw(ui, pg);
        }
        this.afterDraw(ui, pg);

        // Reset the depth test, camera and perspective
        pg.hint(PConstants.DISABLE_DEPTH_TEST);
        pg.camera();
        pg.perspective();

        if (hasFocus()) {
            drawFocusBorder(ui, pg);
        }

        if (this.pg != null) {
            this.pg.endDraw();
            dstPg.image(this.pg, this.x, this.y);
        }
    }

    private void drawCenterDot(PGraphics pg) {
        pg.stroke(LXColor.RED);
        pg.strokeWeight(10);
        pg.beginShape(PConstants.POINTS);
        pg.vertex(this.pxDamped.getValuef(), this.pyDamped.getValuef(), this.pzDamped.getValuef());
        pg.endShape();
        pg.strokeWeight(1);
    }

    private void drawFocusBorder(UI ui, PGraphics pg) {
        pg.strokeWeight(1);
        pg.stroke(ui.theme.getFocusColor());
        int focusInset = 2;
        int focusDash = 10;
        // Top left
        pg.line(focusInset, focusInset, focusInset + focusDash, focusInset);
        pg.line(focusInset, focusInset, focusInset, focusInset + focusDash);
        // Top right
        pg.line(ui.applet.width - focusInset, focusInset, ui.applet.width - focusInset - focusDash, focusInset);
        pg.line(ui.applet.width - focusInset, focusInset, ui.applet.width - focusInset, focusInset + focusDash);
        // Bottom left
        pg.line(focusInset, ui.applet.height - focusInset, focusInset + focusDash, ui.applet.height - focusInset);
        pg.line(focusInset, ui.applet.height - focusInset, focusInset, ui.applet.height - focusInset - focusDash);
        // Bottom right
        pg.line(ui.applet.width - focusInset, ui.applet.height - focusInset, ui.applet.width - focusInset - focusDash, ui.applet.height - focusInset);
        pg.line(ui.applet.width - focusInset, ui.applet.height - focusInset, ui.applet.width - focusInset, ui.applet.height - focusInset - focusDash);
    }

    /**
     * Subclasses may override, useful to turn on lighting, etc.
     *
     * @param ui UI
     * @param pg PGraphics
     */
    protected void beforeDraw(UI ui, PGraphics pg) {
    }

    /**
     * Subclasses may override, useful to turn off lighting, etc.
     *
     * @param ui UI
     * @param pg PGraphics
     */
    protected void afterDraw(UI ui, PGraphics pg) {
    }

    @Override
    protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (mouseEvent.getCount() > 1) {
            focus();
        }
    }

    @Override
    protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        switch (this.interactionMode) {
        case ZOOM:
            if (mouseEvent.isShiftDown()) {
                this.radius.incrementValue(dy);
            } else if (mouseEvent.isMetaDown()) {
                float dcx = dx * (float) Math.cos(this.thetaDamped.getValuef());
                float dcz = dx * (float) Math.sin(this.thetaDamped.getValuef());
                setCenter(this.center.x - dcx, this.center.y + dy, this.center.z - dcz);
            } else {
                this.theta.incrementValue(-dx * .003);
                this.phi.incrementValue(dy * .003);
            }
            break;
        case MOVE:
            if (mouseEvent.isMetaDown() || mouseEvent.isShiftDown()) {
                float costh = (float) Math.cos(this.thetaDamped.getValuef());
                float sinth = (float) Math.sin(this.thetaDamped.getValuef());;
                float dex = dx*costh;
                float dez = -dx*sinth;
                float dey = -dy;
                if (mouseEvent.isShiftDown()) {
                    dex -= dy*sinth;
                    dez -= dy*costh;
                    dey = 0;
                }
                setEye(this.eye.x + dex, this.eye.y + dey, this.eye.z + dez);
            } else {
                this.theta.incrementValue(dx * .003);
                this.phi.incrementValue(-dy * .003);
            }
            break;
        }
    }

    @Override
    protected void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
        switch (this.interactionMode) {
        case ZOOM:
            this.radius.incrementValue(delta);
            break;
        case MOVE:
            float dcx = delta * (float) Math.sin(this.thetaDamped.getValuef());
            float dcz = delta * (float) Math.cos(this.thetaDamped.getValuef());
            setEye(this.eye.x - dcx, this.eye.y, this.eye.z - dcz);
            break;
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        float amount = .02f;
        if (keyEvent.isShiftDown()) {
            amount *= 10.f;
        }
        if (this.interactionMode == InteractionMode.MOVE) {
            amount *= -1;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
            consumeKeyEvent();
            this.theta.incrementValue(amount);
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
            consumeKeyEvent();
            this.theta.incrementValue(-amount);
        } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
            consumeKeyEvent();
            this.phi.incrementValue(-amount);
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
            consumeKeyEvent();
            this.phi.incrementValue(amount);
        }
    }

}
