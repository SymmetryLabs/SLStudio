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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;

public class Kinet {

    /**
     * In practice this is as hard as some Color Kinetics PSUs can be pushed
     * before they start dropping frames.
     */
    private final int DEFAULT_FRAMERATE = 24;
    
    final private KinetNode[] outputNodes;
    final private ArrayList<KinetPort> outputPorts;

    private long sendThrottleMillis;
    private long lastFrameMillis;
    
    private DatagramSocket socket;

    public Kinet(KinetNode[] outputNodes) throws SocketException {
        this.outputNodes = outputNodes;
        this.outputPorts = new ArrayList<KinetPort>();
        HashSet<KinetPort> uniquePorts = new HashSet<KinetPort>();
        for (KinetNode node : outputNodes) {
            if ((node != null) && !uniquePorts.contains(node.outputPort)) {
                uniquePorts.add(node.outputPort);
                this.outputPorts.add(node.outputPort);
            }
        }
        this.socket = new DatagramSocket();
        this.setFramerate(DEFAULT_FRAMERATE);
        this.lastFrameMillis = 0;
    }
    
    final public int size() {
        return this.outputNodes.length;
    }
    
    final public Kinet setFramerate(double frameRate) {
        this.sendThrottleMillis = (long) (1000. / frameRate);
        return this;
    }

    final public void sendThrottledColors(int[] colors) {
        long now = System.currentTimeMillis();
        if (now - this.lastFrameMillis > this.sendThrottleMillis) {
            this.lastFrameMillis = now;
            this.sendColors(colors);
        }
    }
    
    final public void sendColors(int[] colors) {
        for (int i = 0; i < colors.length; ++i) {
            KinetNode node = this.outputNodes[i];
            if (node != null) {
                int c = colors[i];
                byte r = (byte) ((c >> 16) & 0xFF);
                byte g = (byte) ((c >> 8) & 0xFF);
                byte b = (byte) ((c) & 0xFF);
                node.outputPort.setNode(node.nodeIndex, r, g, b);
            }
        }
        this.sendPackets();
    }

    final private void sendPackets() {
        try {
            for (KinetPort port : this.outputPorts) {
                port.send(this.socket);
            }
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }
}
