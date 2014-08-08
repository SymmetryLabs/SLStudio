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

/**
 * A layer is a components that has a run method and operates on some other
 * buffer component. The layer does not actually own the color buffer. An
 * effect is an example of a layer, or patterns may compose themselves from
 * multiple layers.
 */
public abstract class LXLayer extends LXLayeredComponent {

    protected LXLayer(LX lx) {
        super(lx);
    }

    protected LXLayer(LX lx, LXBufferedComponent buffer) {
        super(lx, buffer);
    }

    @Override
    protected final void onLoop(double deltaMs) {
        run(deltaMs);
    }

    /**
     * Run this layer.
     *
     * @param deltaMs Milliseconds elapsed since last frame
     */
    public abstract void run(double deltaMs);

}
