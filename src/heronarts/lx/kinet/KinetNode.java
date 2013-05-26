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

package heronarts.lx.kinet;

public class KinetNode {
    final public KinetPort outputPort;
    final public int nodeIndex;
    
    public KinetNode(KinetPort outputPort, int nodeIndex) {
        if (outputPort == null) {
            throw new NullPointerException();
        }
        this.outputPort = outputPort;
        this.nodeIndex = nodeIndex;
    }
}
