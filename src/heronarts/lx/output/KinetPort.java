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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents a KiNET output port, which is an IP address of a power
 * supply and the port number on that PSU to send data on along with a buffer
 * of data.
 */
@Deprecated public class KinetPort {
    private final static int DEFAULT_DATA_LEN = 512;
    private final static int HEADER_LEN = 24;
    private final static int KINET_PORT = 6038; 
    
    private final byte portNumber;
    private final InetAddress ipAddress;
    private final byte[] dataBuffer;

    /**
     * Constructs a new port
     * 
     * @param ip IP address of the supply
     * @param port Port number
     */
    public KinetPort(String ip, int port) {
        this(ip, port, DEFAULT_DATA_LEN);
    }
    
    /**
     * Constructs a new port with a custom data length
     * 
     * @param ip IP address of the supply
     * @param port Port number
     * @param dataLen Length of the pixel data buffer
     */
    public KinetPort(String ip, int port, int dataLen) {
        this.portNumber = (byte) port;
        this.dataBuffer = new byte[HEADER_LEN + dataLen];
        this.dataBuffer[0] = (byte) 0x04;
        this.dataBuffer[1] = (byte) 0x01;
        this.dataBuffer[2] = (byte) 0xdc;
        this.dataBuffer[3] = (byte) 0x4a;
        this.dataBuffer[4] = (byte) 0x01;
        this.dataBuffer[5] = (byte) 0x00;
        this.dataBuffer[6] = (byte) 0x08;
        this.dataBuffer[7] = (byte) 0x01;
        this.dataBuffer[8] = (byte) 0x00;
        this.dataBuffer[9] = (byte) 0x00;
        this.dataBuffer[10] = (byte) 0x00;
        this.dataBuffer[11] = (byte) 0x00;
        this.dataBuffer[12] = (byte) 0xff;
        this.dataBuffer[13] = (byte) 0xff;
        this.dataBuffer[14] = (byte) 0xff;
        this.dataBuffer[15] = (byte) 0xff;
        this.dataBuffer[16] = portNumber;
        this.dataBuffer[17] = (byte) 0x00; // Sometimes a checksum? 0x00 works
                                            // fine.
        this.dataBuffer[18] = (byte) 0x00;
        this.dataBuffer[19] = (byte) 0x00;
        this.dataBuffer[20] = (byte) 0x00;
        this.dataBuffer[21] = (byte) 0x02; // Total # of ports on controller
                                            // (irrelevant?)
        this.dataBuffer[22] = (byte) 0x00;
        this.dataBuffer[23] = (byte) 0x00;
        for (int i = 0; i < dataLen; ++i) {
            this.dataBuffer[HEADER_LEN + i] = (byte) 0x00;
        }
        try {
            this.ipAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException uhx) {
            throw new RuntimeException("KinetPort could not determine IP address for " + ip, uhx);
        }
    }

    /**
     * Sets the color values for a particular node on this string 
     * 
     * @param nodeIndex Index of node on this port
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return this
     */
    public KinetPort setNode(int nodeIndex, byte r, byte g, byte b) {
        this.dataBuffer[HEADER_LEN + 3 * nodeIndex] = r;
        this.dataBuffer[HEADER_LEN + 3 * nodeIndex + 1] = g;
        this.dataBuffer[HEADER_LEN + 3 * nodeIndex + 2] = b;
        return this;
    }
    
    /**
     * Sends this port's data on the network
     * 
     * @param socket Socket to send the data on
     * @throws IOException If a networking error occurs
     * @return this
     */
    public KinetPort send(DatagramSocket socket) throws IOException {
        socket.send(new DatagramPacket(this.dataBuffer, this.dataBuffer.length, this.ipAddress, KINET_PORT));
        return this;
    }
}