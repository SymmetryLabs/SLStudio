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

import heronarts.lx.HeronLX;
import heronarts.lx.LXUtils;

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
    private static final int DEFAULT_FRAMERATE = 24;
    
    private final HeronLX lx; 
    
    final private KinetNode[] outputNodes;
    final private ArrayList<KinetPort> outputPorts;

    private final int[] kinetColors;    
    private double brightness;
    private int gamma;
    
    private long sendThrottleMillis;
    private long lastFrameMillis;
    
    private DatagramSocket socket;

    public Kinet(HeronLX lx, KinetNode[] outputNodes) throws SocketException {
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
    
    final public int size() {
        return this.outputNodes.length;
    }
    
    final public Kinet setFramerate(double frameRate) {
        this.sendThrottleMillis = (long) (1000. / frameRate);
        return this;
    }

    /**
     * Sets brightness factor of the KiNET output
     *  
     * @param brightness Value from 0-100 scale
     */
    public void setBrightness(double brightness) {
        this.brightness = LXUtils.constrain(brightness/100., 0, 1);
    }
    
    /**
     * Sets level of gamma correction. 
     * 
     * @param gamma 2 is quadratic, 3 cubic, etc.
     */
    public void setGammaCorrection(int gamma) {
        if (gamma < 1) {
            this.gamma = 1;
        } else {
            this.gamma = gamma;
        }
    }
    
    final public void sendThrottledColors(int[] colors) {
        long now = System.currentTimeMillis();
        if (now - this.lastFrameMillis > this.sendThrottleMillis) {
            this.lastFrameMillis = now;
            this.sendColors(colors);
        }
    }
    
    final public void sendColors(int[] colors) {
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
