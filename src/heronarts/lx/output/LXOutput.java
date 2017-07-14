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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.output;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the output stage from the LX engine to real devices.
 * Outputs may have their own brightness, be enabled/disabled, be throttled,
 * etc.
 */
public abstract class LXOutput extends LXComponent {

    static int[] fixtureToIndices(LXFixture fixture) {
        List<LXPoint> points = fixture.getPoints();
        int[] indices = new int[points.size()];
        int i = 0;
        for (LXPoint p : points) {
            indices[i++] = p.index;
        }
        return indices;
    }

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
    public final BooleanParameter enabled =
        new BooleanParameter("Enabled", true)
        .setDescription("Whether the output is active");

    public enum Mode {
        NORMAL,
        WHITE,
        OFF
    };

    /**
     * Sending mode, 0 = normal, 1 = all white, 2 = all off
     */
    public final EnumParameter<Mode> mode =
        new EnumParameter<Mode>("Mode", Mode.NORMAL)
        .setDescription("Operation mode of this output");

    /**
     * Framerate throttle
     */
    public final BoundedParameter framesPerSecond =
        new BoundedParameter("FPS", 0, 300)
        .setDescription("Maximum frames per second this output will send");

    /**
     * Gamma correction level
     */
    public final DiscreteParameter gammaCorrection =
        new DiscreteParameter("Gamma", 4)
        .setDescription("Gamma correction on the output, 0 is none");

    /**
     * Brightness of the output
     */
    public final BoundedParameter brightness =
        new BoundedParameter("Brightness", 1)
        .setDescription("Level of the output");

    /**
     * Time last frame was sent at.
     */
    private long lastFrameMillis = 0;

    private final int[] allWhite;

    private final int[] allOff;

    protected LXOutput(LX lx) {
        this(lx, "Output");
    }

    protected LXOutput(LX lx, String label) {
        super(lx, label);
        this.outputColors = new int[lx.total];
        this.allWhite = new int[lx.total];
        this.allOff = new int[lx.total];
        for (int i = 0; i < lx.total; ++i) {
            this.allWhite[i] = LXColor.WHITE;
            this.allOff[i] = LXColor.BLACK;
        }
        addParameter("enabled", enabled);
        addParameter("mode", mode);
        addParameter("fps", framesPerSecond);
        addParameter("gamma", gammaCorrection);
        addParameter("brightness", brightness);
    }

    /**
     * Adds a child to this output, sent after color-correction
     *
     * @param child Child output
     * @return this
     */
    public LXOutput addChild(LXOutput child) {
        // TODO(mcslee): need to setParent() on the LXComponent...
        this.children.add(child);
        return this;
    }

    /**
     * Removes a child
     *
     * @param child Child output
     * @return this
     */
    public LXOutput removeChild(LXOutput child) {
        this.children.remove(child);
        return this;
    }

    /**
     * Sends data to this output, after applying throttle and color correction
     *
     * @param colors Array of color values
     * @return this
     */
    public final LXOutput send(int[] colors) {
        if (!this.enabled.isOn()) {
            return this;
        }
        long now = System.currentTimeMillis();
        double fps = this.framesPerSecond.getValue();
        if ((fps == 0) || ((now - this.lastFrameMillis) > (1000. / fps))) {
            int[] colorsToSend;
            switch (this.mode.getEnum()) {
            case WHITE:
                int white = LXColor.hsb(0, 0, 100 * this.brightness.getValuef());
                for (int i = 0; i < this.allWhite.length; ++i) {
                    this.allWhite[i] = white;
                }
                colorsToSend = this.allWhite;
                break;

            case OFF:
                colorsToSend = this.allOff;
                break;

            default:
            case NORMAL:
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
                        float scaleBrightness = this.hsb[2];
                        for (int x = 0; x < gamma; ++x) {
                            scaleBrightness *= this.hsb[2];
                        }
                        scaleBrightness *= brt;
                        this.outputColors[i] = Color.HSBtoRGB(hsb[0], hsb[1], scaleBrightness);
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
     * @param colors Color values
     */
    protected abstract void onSend(int[] colors);
}