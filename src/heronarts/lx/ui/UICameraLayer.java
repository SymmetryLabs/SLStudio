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

public abstract class UICameraLayer implements UILayer {
    
    private final UI ui;
    
    private float cx = 0 , cy = 0, cz = 0;
    
    private float theta = 0, phi = 0, r = 120;
    
    private float ex = 0, ey = 0, ez = 0;
    
    private float px = 0, py = 0; 
    
    protected UICameraLayer(UI ui) {
        this.ui = ui;
        computeEye();
    }
    
    public UICameraLayer setRadius(float r) {
        this.r = r;
        computeEye();
        return this;
    }
    
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
        this.ex = this.cx + this.r * cosphi * sintheta;
        this.ez = this.cz - this.r * cosphi * costheta;
        this.ey = this.cy + this.r * sinphi;
    }
    
    public final void draw() {
        this.ui.applet.camera(this.ex, this.ey, this.ez, this.cx, this.cy, this.cz, 0, -1, 0);
        onDraw(this.ui);
        this.ui.applet.camera();
    }
    
    protected /* abstract */void onDraw(UI ui) {}
    
    public boolean mousePressed(float mx, float my) {
        this.px = mx;
        this.py = my;
        return true;
    }
    
    public boolean mouseReleased(float mx, float my) {
        return true;
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
        setRadius(Math.max(0, this.r + delta));
        return true;
    }

}
