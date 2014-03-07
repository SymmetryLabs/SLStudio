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

package heronarts.lx.output;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

/**
 * This class represents the output stage from the LX engine to real devices.
 * Outputs may have their own brightness, be enabled/disabled, be throttled,
 * etc. 
 */
public abstract class LXOutput {
    
    private final LX lx; 
    
    private final List<LXOutput> children = new ArrayList<LXOutput>();
    
    /**
     * Buffer with colors for this output, gamma-corrected 
     */
    private final int[] outputColors;
    
    /**
     * Local array for color-conversions
     */
    private final float[] hsb = new float[3];
    
    /**
     * Whether the output is enabled.
     */
    public final BooleanParameter enabled = new BooleanParameter("ON", true);
    
    /**
     * Framerate throttle
     */
    public final BasicParameter framesPerSecond = new BasicParameter("FPS", 0, 300);
    
    /**
     * Gamma correction level
     */
    public final DiscreteParameter gammaCorrection = new DiscreteParameter("GAMMA", 4);
    
    /**
     * Brightness of the output
     */
    public final BasicParameter brightness = new BasicParameter("BRT", 1);
    
    /**
     * Time last frame was sent at.
     */
    private long lastFrameMillis = 0;
    
    protected LXOutput(LX lx) {
        this.lx = lx;
        this.outputColors = new int[lx.total];
    }
    
    /**
     * Adds a child to this output, sent after color-correction
     * 
     * @param child
     * @return
     */
    public LXOutput addChild(LXOutput child) {
        this.children.add(child);
        return this;
    }
    
    /**
     * Removes a child
     * 
     * @param child
     * @return
     */
    public LXOutput removeChild(LXOutput child) {
        this.children.remove(child);
        return this;
    }
    
    /**
     * Sends data to this output, after applying throttle and color correction
     * 
     * @param colors
     */
    public final LXOutput send(int[] colors) {
        if (!this.enabled.isOn()) {
            return this;
        }
        long now = System.currentTimeMillis();
        double fps = this.framesPerSecond.getValue();
        if ((fps == 0) ||
            ((now - this.lastFrameMillis) > (1000. / fps))) {
            int gamma = this.gammaCorrection.getValuei();
            double brt = this.brightness.getValuef();
            int[] colorsToSend = colors; 
            if (gamma > 0 || brt < 1) {
                int r, g, b, rgb;
                for (int i = 0; i < colors.length; ++i) {
                    rgb = colors[i];
                    r = (rgb >> 16) & 0xff;
                    g = (rgb >> 8) & 0xff;
                    b = rgb & 0xff;        
                    Color.RGBtoHSB(r, g, b, this.hsb);
                    float scaleBrightness = hsb[2];
                    for (int x = 0; x < gamma; ++x) {
                        scaleBrightness *= hsb[2];
                    }
                    scaleBrightness *= brt;
                    this.outputColors[i] = Color.HSBtoRGB(hsb[0], hsb[1], scaleBrightness);
                }
                colorsToSend = this.outputColors;
            }
            this.onSend(colorsToSend);
            for (LXOutput child : this.children) {
                child.send(colorsToSend);
            }
            this.lastFrameMillis = now;
        }
        return this;
    }
    
    /**
     * Subclasses implement this to send the data.
     * 
     * @param colors
     */
    protected abstract void onSend(int[] colors);
}