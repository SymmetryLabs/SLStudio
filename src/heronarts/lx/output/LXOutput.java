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

import heronarts.lx.LX;
import heronarts.lx.parameter.BasicParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the output stage from the LX engine to real devices.
 * Outputs may have their own brightness, be enabled/disabled, be throttled,
 * etc.
 */
public abstract class LXOutput {

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
     * Sending mode, 0 = normal, 1 = all white, 2 = all off
     */
    public final DiscreteParameter mode = new DiscreteParameter("MODE", 3);

    /**
     * Normal output mode, colors are sent according to animation
     */
    public final static int MODE_NORMAL = 0;

    /**
     * White output mode, all pixels are sent full white
     */
    public final static int MODE_WHITE = 1;

    /**
     * Off output mode, all pixels are sent off
     */
    public final static int MODE_OFF = 2;

    /**
     * Framerate throttle
     */
    public final BasicParameter framesPerSecond = new BasicParameter("FPS", 0,
            300);

    /**
     * Gamma correction level
     */
    public final DiscreteParameter gammaCorrection = new DiscreteParameter(
            "GAMMA", 4);

    /**
     * Brightness of the output
     */
    public final BasicParameter brightness = new BasicParameter("BRT", 1);

    /**
     * Time last frame was sent at.
     */
    private long lastFrameMillis = 0;

    private final int[] allWhite;

    private final int[] allOff;

    protected LXOutput(LX lx) {
        this.outputColors = new int[lx.total];
        this.allWhite = new int[lx.total];
        this.allOff = new int[lx.total];
        for (int i = 0; i < lx.total; ++i) {
            this.allWhite[i] = 0xffffffff;
            this.allOff[i] = 0xff000000;
        }
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
        if ((fps == 0) || ((now - this.lastFrameMillis) > (1000. / fps))) {
            int[] colorsToSend;
            switch (this.mode.getValuei()) {
            case MODE_WHITE:
                int white = LX.hsb(0, 0, 100 * this.brightness.getValuef());
                for (int i = 0; i < this.allWhite.length; ++i) {
                    this.allWhite[i] = white;
                }
                colorsToSend = this.allWhite;
                break;

            case MODE_OFF:
                colorsToSend = this.allOff;
                break;

            default:
            case MODE_NORMAL:
                colorsToSend = colors;
                int gamma = this.gammaCorrection.getValuei();
                double brt = this.brightness.getValuef();
                if (gamma > 0 || brt < 1) {
                    int r, g, b, rgb;
                    for (int i = 0; i < colorsToSend.length; ++i) {
                        rgb = colorsToSend[i];
                        r = (rgb >> 16) & 0xff;
                        g = (rgb >> 8) & 0xff;
                        b = rgb & 0xff;
                        Color.RGBtoHSB(r, g, b, this.hsb);
                        float scaleBrightness = hsb[2];
                        for (int x = 0; x < gamma; ++x) {
                            scaleBrightness *= hsb[2];
                        }
                        scaleBrightness *= brt;
                        this.outputColors[i] = Color.HSBtoRGB(hsb[0], hsb[1],
                                scaleBrightness);
                    }
                    colorsToSend = this.outputColors;
                }
                break;
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