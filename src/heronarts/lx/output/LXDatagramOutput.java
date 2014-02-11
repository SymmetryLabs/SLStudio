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

import heronarts.lx.LX;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * An output stage that functions by sending datagram packets.
 */
public class LXDatagramOutput extends LXOutput {
    
    /**
     * Output socket
     */
    private final DatagramSocket socket;
    
    private final List<LXDatagram> datagrams = new ArrayList<LXDatagram>();
    
    public LXDatagramOutput(LX lx) throws SocketException {
        this(lx, new DatagramSocket());
    }
    
    public LXDatagramOutput(LX lx, DatagramSocket socket) {
        super(lx);
        this.socket = socket;
    }
        
    public LXDatagramOutput addDatagram(LXDatagram datagram) {
        this.datagrams.add(datagram);
        return this;
    }
    
    public LXDatagramOutput addDatagrams(LXDatagram[] datagrams) {
        for (LXDatagram datagram : datagrams) {
            addDatagram(datagram);
        }
        return this;
    }

    protected final void onSend(int[] colors) {
        for (LXDatagram datagram : this.datagrams) {
            datagram.onSend(colors);
            try {
                this.socket.send(datagram.packet);
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }
    }
}
