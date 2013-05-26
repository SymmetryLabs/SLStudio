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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents a color kinetics output port, which is an IP address of a power
 * supply and the port number on that PSU to send data on along with a buffer
 * of data on that port.
 */
public class KinetPort {
    final static private int DEFAULT_DATA_LEN = 512;
    final static private int HEADER_LEN = 24;
    final static private int KINET_PORT = 6038; 
    
    final private byte portNumber;
    final private InetAddress ipAddress;
    final private byte[] dataBuffer;

    public KinetPort(String ip, int port) {
        this(ip, port, DEFAULT_DATA_LEN);
    }
    
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

    public void setNode(int nodeIndex, byte r, byte g, byte b) {
        this.dataBuffer[HEADER_LEN + 3 * nodeIndex] = r;
        this.dataBuffer[HEADER_LEN + 3 * nodeIndex + 1] = g;
        this.dataBuffer[HEADER_LEN + 3 * nodeIndex + 2] = b;
    }
    
    public void send(DatagramSocket socket) throws IOException {
        socket.send(new DatagramPacket(this.dataBuffer, this.dataBuffer.length, this.ipAddress, KINET_PORT));
    }
}