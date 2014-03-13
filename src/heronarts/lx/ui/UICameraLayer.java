/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.ui;

import heronarts.lx.LXUtils;

import processing.core.PConstants;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a layer that contains a 3d scene with a camera. Mouse movements control
 * the camera, and the scene can contain components.
 */
public class UICameraLayer implements UILayer {
    
    private final UI ui;
    
    private final List<UICameraComponent> components = new ArrayList<UICameraComponent>(); 
    
    // Center of the scene
    private float cx = 0, cy = 0, cz = 0;
    
    // Polar eye position
    private float theta = 0, phi = 0, radius = 120;
    
    // Computed eye position
    private float ex = 0, ey = 0, ez = 0;
    
    // Mouse tracking
    private float px = 0, py = 0;
    
    // Radius bounds
    private float minRadius = 0, maxRadius = Float.MAX_VALUE;
        
    public UICameraLayer(UI ui) {
        this.ui = ui;
        computeEye();
    }
    
    /**
     * Adds a component to the layer
     * 
     * @param component
     * @return this
     */
    public final UICameraLayer addComponent(UICameraComponent component) {
        this.components.add(component);
        return this;
    }
    
    /**
     * Removes a component from the layer
     * 
     * @param component
     * @return this
     */
    public final UICameraLayer removeComponent(UICameraComponent component) {
        this.components.remove(component);
        return this;
    }
    
    /**
     * Set radius of the camera
     * 
     * @param r radius
     * @return this
     */
    public UICameraLayer setRadius(float radius) {
        this.radius = LXUtils.constrainf(radius, this.minRadius, this.maxRadius);
        computeEye();
        return this;
    }
    
    /**
     * Set the theta angle of viewing
     * 
     * @param theta Angle about the y axis
     * @return this
     */
    public UICameraLayer setTheta(float theta) {
        this.theta = theta;
        computeEye();
        return this;
    }
    
    /**
     * Sets bounds on the radius
     * @param minRadius
     * @param maxRadius
     * @return this
     */
    public UICameraLayer setRadiusBounds(float minRadius, float maxRadius) {
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        setRadius(LXUtils.constrainf(this.radius, minRadius, maxRadius));
        return this;
    }
    
    /**
     * Set minimum radius
     * 
     * @param minRadius
     * @return this
     */
    public UICameraLayer setMinRadius(float minRadius) {
        return setRadiusBounds(minRadius, this.maxRadius);
    }
    
    /**
     * Set maximum radius
     * 
     * @param maxRadius
     * @return this
     */
    public UICameraLayer setMaxRadius(float maxRadius) {
        return setRadiusBounds(this.minRadius, maxRadius);
    }
    
    /**
     * Sets the center of the scene
     * @param x
     * @param y
     * @param z
     * @return this
     */
    public UICameraLayer setCenter(float x, float y, float z) {
        this.cx = x;
        this.cy = y;
        this.cz = z;
        computeEye();
        return this;
    }
    
    private void computeEye() {
        float sintheta = (float) Math.sin(this.theta);
        float costheta = (float) Math.cos(this.theta);
        float sinphi = (float) Math.sin(this.phi);
        float cosphi = (float) Math.cos(this.phi);
        this.ex = this.cx + this.radius * cosphi * sintheta;
        this.ez = this.cz - this.radius * cosphi * costheta;
        this.ey = this.cy + this.radius * sinphi;
    }
    
    public final void draw() {
        // Set the camera view
        this.ui.applet.camera(this.ex, this.ey, this.ez, this.cx, this.cy, this.cz, 0, -1, 0);
        
        // Draw all the components in the scene
        this.beforeDraw();        
        for (UICameraComponent component : this.components) {
            if (component.isVisible()) {
                component.draw(this.ui);
            }
        }
        this.afterDraw();
    
        // Reset the camera
        this.ui.applet.camera();
    }
    
    /**
     * Subclasses may override, useful to turn on lighting, etc.
     */
    protected void beforeDraw() {}
    
    /**
     * Subclasses may override, useful to turn off lighting, etc.
     */
    protected void afterDraw() {}
        
    public boolean mousePressed(float mx, float my) {
        this.px = mx;
        this.py = my;
        return true;
    }
    
    public boolean mouseReleased(float mx, float my) {
        return true;
    }
    
    public boolean mouseClicked(float mx, float my) {
        return false;
    }
    
    public boolean mouseDragged(float mx, float my) {
        float dx = mx - this.px;
        float dy = my - this.py;
        this.px = mx;
        this.py = my;
        
        this.theta -= dx*.003;
        this.phi += dy*.003;
        
        this.phi = LXUtils.constrainf(this.phi, -PConstants.QUARTER_PI, PConstants.QUARTER_PI);
        
        computeEye();

        return true;
    }
    
    public boolean mouseWheel(float mx, float my, float delta) {
        setRadius(this.radius + delta);
        return true;
    }

}
