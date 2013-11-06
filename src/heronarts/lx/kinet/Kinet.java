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

import heronarts.lx.LX;
import heronarts.lx.LXUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class represents an output network of KiNET nodes. Typically this is
 * constructed via creating one or more ports, i.e.:
 * <pre>
 * KinetPort port = new KinetPort("10.1.1.1", 1);
 * KinetNode[] nodes = new KinetNode[lx.total];
 * for (int i = 0; i < lx.total; ++i) {
 *     nodes[i] = new KinetNode(port, i); 
 * }
 * Kinet kinet = new Kinet(nodes);
 * </pre>
 * 
 * The above would map pixels in order to a single output port in order. Typically
 * nodes are not wired in the same fixed order, so logic is used to map the
 * node indices from their logical representation (in the LX model) to their
 * physical wiring.
 */
public class Kinet {

    /**
     * In practice this is as hard as some Color Kinetics PSUs can be pushed
     * before they start dropping frames.
     */
    private static final int DEFAULT_FRAMERATE = 24;
    
    private final LX lx; 
    
    private final KinetNode[] outputNodes;
    private final ArrayList<KinetPort> outputPorts;

    private final int[] kinetColors;    
    private double brightness;
    private int gamma;
    
    private long sendThrottleMillis;
    private long lastFrameMillis;
    
    private DatagramSocket socket;

    /**
     * Constructs a new output network.
     * 
     * @param lx LX instance
     * @param outputNodes Array of nodes to output to, must be lx.total in
     *                       length, unused nodes may be null
     * @throws SocketException If the UDP socket can't be constructed
     */
    public Kinet(LX lx, KinetNode[] outputNodes) throws SocketException {
        this.lx = lx;
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
        this.brightness = 1.;
        this.kinetColors = new int[lx.total];        
        this.setFramerate(DEFAULT_FRAMERATE);
        this.lastFrameMillis = 0;
    }
    
    /**
     * The size of the network.
     * 
     * @return The number of nodes in the network
     */
    public final int size() {
        return this.outputNodes.length;
    }
    
    /**
     * Sets the maximum framerate of the network.
     * 
     * @param frameRate Maximum framerate
     * @return this
     */
    public final Kinet setFramerate(double frameRate) {
        this.sendThrottleMillis = (long) (1000. / frameRate);
        return this;
    }

    /**
     * Sets brightness factor of the KiNET output.
     *  
     * @param brightness Value from 0-100 scale
     * @return this
     */
    public Kinet setBrightness(double brightness) {
        this.brightness = LXUtils.constrain(brightness/100., 0, 1);
        return this;
    }
    
    /**
     * Sets level of gamma correction. The correction is polynomial. Brightness
     * values are computed from 0-1, the gamma value given is the exponent to
     * which the value is taken.
     * 
     * @param gamma 2 is quadratic, 3 cubic, etc.
     * @return this
     */
    public Kinet setGammaCorrection(int gamma) {
        if (gamma < 1) {
            this.gamma = 1;
        } else {
            this.gamma = gamma;
        }
        return this;
    }
    
    /**
     * Sends an array of pixels out to this network, respecting the specified
     * framerate. If the last frame was sent too recently, these colors will be
     * ignored.
     * 
     * @param colors Array of color values
     * @return this
     */
    public final Kinet sendThrottledColors(int[] colors) {
        long now = System.currentTimeMillis();
        if (now - this.lastFrameMillis > this.sendThrottleMillis) {
            this.lastFrameMillis = now;
            this.sendColors(colors);
        }
        return this;
    }
    
    /**
     * Sends pixels to the network, ignoring framerate.
     * 
     * @param colors Array of color values
     * @return this
     */
    public final Kinet sendColors(int[] colors) {
        int[] sendColors = colors;
        if (this.brightness < 1. || this.gamma > 1) {
            float factor = (float) (this.brightness * this.brightness);
            for (int i = 0; i < colors.length; ++i) {
                float b, bNew;
                bNew = b = this.lx.applet.brightness(colors[i]) / 100.f;
                for (int g = 1; g < this.gamma; ++g) {
                    bNew = bNew * b;
                }
                this.kinetColors[i] = this.lx.applet.color(
                        this.lx.applet.hue(colors[i]),
                        this.lx.applet.saturation(colors[i]),
                        bNew * 100.f * factor
                        );
            }
            sendColors = this.kinetColors;
        }
        
        for (int i = 0; i < sendColors.length; ++i) {
            KinetNode node = this.outputNodes[i];
            if (node != null) {
                int c = sendColors[i];
                byte r = (byte) ((c >> 16) & 0xFF);
                byte g = (byte) ((c >> 8) & 0xFF);
                byte b = (byte) ((c) & 0xFF);
                node.outputPort.setNode(node.nodeIndex, r, g, b);
            }
        }
        this.sendPackets();
        return this;
    }

    private final void sendPackets() {
        try {
            for (KinetPort port : this.outputPorts) {
                port.send(this.socket);
            }
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }
}
