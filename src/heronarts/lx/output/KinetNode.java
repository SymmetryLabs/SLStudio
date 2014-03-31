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

/**
 * Class to represent a single KiNET node. Each node belongs on a port, and has
 * an index into that port's output buffer.
 */
@Deprecated
public class KinetNode {

    /**
     * The port that this node outputs on.
     */
    public final KinetPort outputPort;

    /**
     * The index of this node on that port.
     */
    public final int nodeIndex;

    /**
     * Constructs a new node
     * 
     * @param outputPort Output port
     * @param nodeIndex Index of node on port
     */
    @Deprecated
    public KinetNode(KinetPort outputPort, int nodeIndex) {
        if (outputPort == null) {
            throw new NullPointerException();
        }
        this.outputPort = outputPort;
        this.nodeIndex = nodeIndex;
    }
}
