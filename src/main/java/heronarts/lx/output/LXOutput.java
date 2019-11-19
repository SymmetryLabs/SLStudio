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

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.color.Spaces;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.RGB8;

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

    /**
     * If this output has more than this many children and the engine is
     * multi-threaded, we send to our children in parallel.
     */
    private static final int PARALLEL_CHILD_COUNT = 20;

    private final LX lx;
    private final List<LXOutput> children = new ArrayList<LXOutput>();

    /**
     * Buffer with colors for this output, gamma-corrected
     */
    protected final PolyBuffer buffer;

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
        RAW,
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
        new BoundedParameter("FPS", 34,0, 300)
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

    protected LXOutput(LX lx) {
        this(lx, "Output");
    }

    protected LXOutput(LX lx, String label) {
        super(lx, label);
        this.lx = lx;
        this.buffer = new PolyBuffer(lx);
        addParameter("enabled", this.enabled);
        addParameter("mode", this.mode);
        addParameter("fps", this.framesPerSecond);
        addParameter("gamma", this.gammaCorrection);
        addParameter("brightness", this.brightness);
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
     * Maintained for compatibility.  Use send(PolyBuffer) instead.
     * @param colors Array of color values
     */
    @Deprecated
    public LXOutput send(int[] colors) {
        return send(PolyBuffer.wrapArray(lx, colors));
    }

    /**
     * Sends data to this output, after applying throttle and color correction.
     * @param src Color buffer to send
     */
    public LXOutput send(PolyBuffer src) {
        long now = System.currentTimeMillis();
        double fps = framesPerSecond.getValue();
        if (enabled.isOn() && (fps == 0 || now > lastFrameMillis + 1000/fps)) {
            PolyBuffer out = processOutput(src, src.getBestFreshSpace());
            onSend(out);
            if (lx.engine.isNetworkMultithreaded.isOn() && children.size() > PARALLEL_CHILD_COUNT) {
                children.parallelStream().forEach(child -> child.send(out));
            } else {
                for (LXOutput child : children) {
                    child.send(out);
                }
            }
            lastFrameMillis = now;
        }
        return this;
    }

    /**
     * Convert perceptual color data to linear color data for output, and apply target brightness.
     *
     * Note: output brightness is applied in linear space, not perceptual space.
     */
    protected PolyBuffer processOutput(PolyBuffer src, PolyBuffer.Space space) {
        double lum = Spaces.cie_lightness_to_luminance(brightness.getValue());

        switch (mode.getEnum()) {
            case OFF:
                lum = 0;
                // fall through
            case WHITE:
                if (space == RGB16) {
                    Arrays.fill((long[]) buffer.getArray(space), Ops16.gray(lum));
                    buffer.markModified(RGB16);
                } else {
                    Arrays.fill((int[]) buffer.getArray(RGB8), Ops8.gray(lum));
                    buffer.markModified(RGB8);
                }
                return buffer;

            case NORMAL:
                int gamma = gammaCorrection.getValuei();
                if (gamma > 0 || lum < 1) {
                    if (space == RGB16) {
                        long[] srcLongs = (long[]) src.getArray(RGB16);
                        long[] outLongs = (long[]) buffer.getArray(RGB16);
                        for (int i = 0; i < srcLongs.length; ++i) {
                            double factor = lum * FastMath.pow(Ops16.level(srcLongs[i]), gamma);
                            outLongs[i] = Ops16.multiply(srcLongs[i], factor);
                        }
                        buffer.markModified(RGB16);
                    } else {
                        int[] srcInts = (int[]) src.getArray(RGB8);
                        int[] outInts = (int[]) buffer.getArray(RGB8);
                        for (int i = 0; i < srcInts.length; ++i) {
                            double factor = lum * FastMath.pow(Ops8.level(srcInts[i]), gamma);
                            outInts[i] = Ops8.multiply(srcInts[i], factor);
                        }
                        buffer.markModified(RGB8);
                    }
                    return buffer;
                }
        }

        return src;
    }

    /**
     * Old-style subclasses override this method to send 8-bit color data.
     * New-style subclasses should override onSend(PolyBuffer) instead.
     * @param colors 8-bit color values
     */
    @Deprecated
    protected /* abstract */ void onSend(int[] colors) { }

    /**
     * Sends out color data.  Subclasses should override this method.
     * @param src The color data to send.
     */
    protected void onSend(PolyBuffer src) {
        // For compatibility, this invokes the method that previous subclasses
        // were supposed to implement.  Implementations of onSend(int[]) know
        // only how to send 8-bit color data, so that's what we pass to them.
        onSend((int[]) src.getArray(RGB8));

        // New subclasses should override and replace this method with one that
        // obtains a color array in the desired space using src.getArray(space),
        // and sends out data from that array.
    }
}
