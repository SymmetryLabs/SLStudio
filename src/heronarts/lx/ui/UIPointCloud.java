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

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import processing.core.PConstants;

/**
 * Draws a cloud of points in the layer
 */
public class UIPointCloud extends UICameraComponent {

    private final LX lx;

    private final LXModel model;

    /**
     * Weight of points
     */
    private float pointWeight = 1;

    /**
     * Point cloud for everything in the LX instance
     * 
     * @param lx
     */
    public UIPointCloud(LX lx) {
        this(lx, lx.model);
    }

    /**
     * Point cloud for points in the specified model
     * 
     * @param lx
     * @param model
     */
    public UIPointCloud(LX lx, LXModel model) {
        this.lx = lx;
        this.model = model;
    }

    /**
     * Sets the weight of points
     * 
     * @param pointWeight Point weight
     * @return this
     */
    public UIPointCloud setPointWeight(float pointWeight) {
        this.pointWeight = pointWeight;
        return this;
    }

    protected void onDraw(UI ui) {
        int[] colors = this.lx.getColors();
        ui.applet.strokeWeight(this.pointWeight);
        // TODO(mcslee): make v1/v2 less hacky here
        ui.applet.beginShape(LX.isProcessing2X ? PConstants.POINTS
                : PConstants.POINT);
        for (LXPoint p : this.model.points) {
            ui.applet.stroke(colors[p.index]);
            ui.applet.vertex(p.x, p.y, p.z);
        }
        ui.applet.endShape();
    }
}
