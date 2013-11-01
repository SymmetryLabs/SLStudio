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
 * A layer is a components that has a run method and is explicitly passed
 * a color buffer to mutate. The layer does not actually own the color buffer.
 * An effect is an example of a layer, or patterns may compose themselves
 * from multiple layers. 
 */
public abstract class LXLayer extends LXComponent {
    
    /**
     * Run this layer.
     * 
     * @param deltaMs Milliseconds elapsed since last frame
     * @param colors Pixel buffer
     */
    public abstract void run(double deltaMs, int[] colors);
    
}
