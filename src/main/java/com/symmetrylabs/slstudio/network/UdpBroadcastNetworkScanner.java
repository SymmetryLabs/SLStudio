package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.util.NetworkUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Base class for implementing UDP-based network discovery protocols.
 * <p>
 * This class manages a set of {@link DatagramChannel}s configured to listen on
 * each network interface with a broadcast address. Users of a
 * UdpBroadcastNetworkScanner should call {@code scan()} with some regularity;
 * on each invocation of scan, UdpBroadcastNetworkScanner will groom its list of
 * DatagramChannels (removing ones for interfaces that have gone away, making
 * new ones for new interfaces), it will send discovery packets on each
 * available interface, and it will expire devices that haven't been seen for a
 * while (or, more precisely, subclasses of it will).
 * <p>
 * While UdpBroadcastNetworkScanner gets everything ready for listening, it
 * never actually goes through the motions of checking to see if any of its
 * channels are ready to receive. Instead, each of the channels is registered on
 * a {@link Selector} given to it in its constructor. {@link NetworkMonitor}
 * manages this selector, and selects on it with the appropriate frequency. When
 * one of our channels is selected, NetworkMonitor calls back in to the
 * UdpBroadcastNetworkScanner and asks it to read from the selected chanenl.
 */
public abstract class UdpBroadcastNetworkScanner {
    protected Map<InetAddress, DatagramChannel> chans;
    protected Set<InetAddress> errorBroadcasts = new HashSet<>();
    protected final String protoName;
    protected final ByteBuffer[] discoveryPackets;
    private final Selector recvSelector;
    private final ByteBuffer recvBuf;

    /**
     * Create a new UdpBroadcastNetworkScanner.
     * @param recvSelector the selector with which all of our channels will be registered
     * @param protoName the name of the protocol we're using to scan, used for logging messages only
     * @param responseBufferSize the size of the buffer used to receive response packets. Response packets longer than this size will be truncated when passed to {@link #onReply(SocketAddress, ByteBuffer) onReply}
     * @param discoveryPackets a list of byte buffers containing the contents of the discovery packets that should be sent to each interface on each scan.
     */
    public UdpBroadcastNetworkScanner(Selector recvSelector, String protoName, int responseBufferSize, ByteBuffer[] discoveryPackets) {
        this.protoName = protoName;
        this.discoveryPackets = discoveryPackets;
        this.recvSelector = recvSelector;
        recvBuf = ByteBuffer.allocate(responseBufferSize);

        chans = new HashMap<>();
    }

    /**
     * Initializes a channel after the channel is created.
     *
     * Some protocols (OPC) don't need to do anything with the channel after
     * creating it, since devices respond directly to the port that sent the
     * discovery broadcast. Other protocols (ArtNet) make things more difficult
     * by responding to broadcasts by broadcasting back; those protocols need to
     * bind the channel to a different address than just the one they're sending
     * from.
     *
     * If the implementation of sendPacket just sends the packet over the channel,
     * no implementation of prepareChannel is required.
     *
     * @param chan the channel to configure
     * @param iface the interface the channel should be listening on
     */
    protected void prepareChannel(DatagramChannel chan, InetAddress iface) throws IOException {}

    /**
     * Sends a packet to the specified broadcast address.
     *
     * This is provided because some protocols (*ahem* artnet *ahem*) make
     * clients shout across the network at each other over broadcasts instead of
     * via call-response like a nice protocol like OPC. This means that the
     * channel we set up for listening is inappropriate for sending, because
     * it's bound to the broadcast address. For those protocols, a new
     * DatagramSocket has to be created to actually send the packet; this method
     * is provided as a hook to make that possible.
     *
     * @param addr the address to send the packet to
     * @param chan the channel that UdpBroadcastNetworkScanner is managing to listen to responses on the associated interface
     * @param data the data to send to the address
     */
    protected abstract void sendPacket(InetAddress addr, DatagramChannel chan, ByteBuffer data) throws IOException;

    /**
     * Callback called on receipt of a packet on the channel
     *
     * Implementing classes should check whether this is a valid discovery
     * response, and if it is, should update their device list.
     *
     * @param addr the address we received this packet from
     * @param response the data in the packet, truncated to be responseBufferSize long. This buffer is reused; clients should not maintain a reference to it outside of this method.
     */
    protected abstract void onReply(SocketAddress addr, ByteBuffer response);

    /**
     * Callback for implementing classes to expire devices out of their device list.
     *
     * This is called after each scan runs.
     */
    protected abstract void expireDevices();

    /**
     * Run a single iteration of the scan loop.
     *
     * This grooms the set of channels we're listening on based on available
     * interfaces, expires devices we haven't seen for a while, and sends a
     * discovery packet. This method is 100% non-blocking.
     */
    public synchronized void scan() {
        expireInterfaces();
        expireDevices();
        for (InetAddress broadcast : NetworkUtils.getBroadcastAddresses()) {
            /* Create the channel that we'll receive return broadcasts on, if we haven't made
               one already. */

            // try and nerf all other network comms...
            // I feel like static IP targets will still send.  Which is bad.
            // NOTE this is important because otherwise our application floods sum shit.
            if(broadcast.toString().equals("/10.255.255.255")){
//            if(true){
                if (!chans.containsKey(broadcast)) {
                    try {
                        DatagramChannel recvChan = SelectorProvider.provider().openDatagramChannel();
                        recvChan.setOption(StandardSocketOptions.SO_BROADCAST, true);
                        recvChan.configureBlocking(false);
                        prepareChannel(recvChan, broadcast);
                    /* attach ourselves as a selector attachment so that NetworkMonitor can
                       give us a callback when we're selected. */
                        recvChan.register(recvSelector, SelectionKey.OP_READ, this);
                        chans.put(broadcast, recvChan);
                        System.out.println("bound to new interface " + broadcast + " for " + protoName);
                    } catch (IOException e) {
                        if (!errorBroadcasts.contains(broadcast)) {
                            System.err.println("couldn't set up " + protoName + " discovery listener on " + broadcast + ":");
                            e.printStackTrace();
                            errorBroadcasts.add(broadcast);
                            continue;
                        }
                    }
                }

                /* Then send the discovery packet */
                try {
                    DatagramChannel chan = chans.get(broadcast);
                    for (int i = 0; i < discoveryPackets.length; i++) {
                        discoveryPackets[i].rewind();
                        sendPacket(broadcast, chan, discoveryPackets[i]);
                    }
                } catch (IOException e) {
                    if (!errorBroadcasts.contains(broadcast)) {
                        System.err.println(String.format("couldn't send %s poll to %s:", protoName, broadcast));
                        e.printStackTrace();
                        errorBroadcasts.add(broadcast);
                    }
                }
            }
        }
    }

    /**
     * Called by NetworkMonitor when one of our channels is ready to receive data.
     */
    void onSelected(SelectionKey sel) {
        DatagramChannel chan = (DatagramChannel) sel.channel();
        while (true) {
            try {
                recvBuf.rewind();
                /* We're in non-blocking mode, so chan.receive always returns
                   immediately. If it returns null, there's no more packets
                   waiting to be received. */
                SocketAddress recvAddr = chan.receive(recvBuf);
                if (recvAddr == null) {
                    break;
                }
                onReply(recvAddr, recvBuf);
            } catch (IOException e) {
                System.err.println("unable to receive on datagram channel:");
                e.printStackTrace();
                break;
            }
        }
    }

    private void expireInterfaces() {
        Set<InetAddress> toRemove = new HashSet<InetAddress>(chans.keySet());
        toRemove.removeAll(NetworkUtils.getBroadcastAddresses());
        for (InetAddress a : toRemove) {
            System.err.println("lost interface " + a);
            DatagramChannel chan = chans.get(a);
            /* closing the channel removes it from the selector set */
            try {
                chan.close();
            } catch (IOException e) {}
            chans.remove(a);
        }
    }

    /** Close all channels associated with this network scanner */
    public void close() {
        for (DatagramChannel chan : chans.values()) {
            try {
                chan.close();
            } catch (IOException e) {
                System.err.println("unable to close " + protoName + " channel:");
                e.printStackTrace();
            }
        }
    }
}
