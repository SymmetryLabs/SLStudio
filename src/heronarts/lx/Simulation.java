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

package heronarts.lx;

import processing.core.PConstants;
import processing.core.PGraphics;
import java.lang.Math;

class Simulation implements PConstants {

    final private LX lx;
    
    private int x;
    private int y;
    private int w;
    private int h;
    private int pixelSize;
    private double xSpacing;
    private double ySpacing;
    
    Simulation(LX lx) {
        this.lx = lx;
        this.setBounds(0, 0, lx.applet.width, lx.applet.height);
    }

    void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
        this.xSpacing = width / (double) lx.width;
        this.ySpacing = height / (double) lx.height;
        this.pixelSize = (int) Math.max(2, Math.min(this.xSpacing, this.ySpacing) / 3.);
    }

    protected void draw(int[] colors) {
        PGraphics g = lx.getGraphics();
        g.noStroke();
        g.fill(0);
        g.rect(this.x, this.y, this.w, this.h);
        g.ellipseMode(CENTER);
        for (int i = 0; i < this.lx.width; ++i) {
            for (int j = 0; j < this.lx.height; ++j) {
                g.fill(colors[i + j*this.lx.width]);
                g.ellipse((int) (this.x + (i+0.5)*this.xSpacing), (int) (this.y + (j+0.5)*this.ySpacing), this.pixelSize, this.pixelSize);
            }
        }
    }
}
