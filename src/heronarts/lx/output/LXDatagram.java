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

import java.net.DatagramPacket;
import java.net.InetAddress;

public abstract class LXDatagram {

    protected final byte[] buffer;
    
    final DatagramPacket packet;
    
    protected LXDatagram(int bufferSize) {
        this.buffer = new byte[bufferSize];
        this.packet = new DatagramPacket(this.buffer, bufferSize);
    }

    public LXDatagram setAddress(InetAddress address) {
        this.packet.setAddress(address);
        return this;
    }
    
    public LXDatagram setPort(int port) {
        this.packet.setPort(port);
        return this;
    }
    
    /**
     * Invoked by engine to send this packet when new color data is available.
     * The LXDatagram should update its buffer accordingly.
     * 
     * @param colors Color buffer
     */
    public abstract void onSend(int[] colors); 
}
