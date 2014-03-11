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

import heronarts.lx.parameter.BooleanParameter;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class LXDatagram {

    protected final byte[] buffer;
    
    final DatagramPacket packet;
    
    /**
     * Whether this datagram is active
     */
    public final BooleanParameter enabled = new BooleanParameter("ON", true);
    
    protected LXDatagram(int bufferSize) {
        this.buffer = new byte[bufferSize];
        for (int i = 0; i < bufferSize; ++i) {
            this.buffer[i] = 0;
        }
        this.packet = new DatagramPacket(this.buffer, bufferSize);
    }

    public LXDatagram setAddress(String ipAddress) throws UnknownHostException {
        this.packet.setAddress(InetAddress.getByName(ipAddress));
        return this;
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
     * Helper for subclasses to copy a list of points into the data buffer in
     * RGB byte order at a specified offset.
     * 
     * @param colors List of color values
     * @param point Indices List of point indices
     * @param offset Offset in buffer to write
     * @return this
     */
    protected LXDatagram copyPoints(int[] colors, int[] pointIndices, int offset) {
        int i = offset;
        for (int index : pointIndices) {
            int color = (index >= 0) ? colors[index] : 0;
            this.buffer[i++] = (byte) ((color >> 16) & 0xff);
            this.buffer[i++] = (byte) ((color >> 8) & 0xff);
            this.buffer[i++] = (byte) (color & 0xff);
        }
        return this;
    }
    
    /**
     * Invoked by engine to send this packet when new color data is available.
     * The LXDatagram should update the packet object accordingly to contain
     * the appropriate buffer.
     * 
     * @param colors Color buffer
     */
    public abstract void onSend(int[] colors); 
}
