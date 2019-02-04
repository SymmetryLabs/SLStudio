package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.slstudio.output.ArtNetDatagramUtil;
import com.symmetrylabs.slstudio.output.ArtNetPollDatagram;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ArtNetNetworkScanner extends UdpBroadcastNetworkScanner {
    protected static final long MAX_MILLIS_WITHOUT_REPLY = 15_000L;
    protected static final short ARTNET_POLLREPLY_OPCODE = 0x2100;

    public final ListenableSet<InetAddress> deviceList = new ListenableSet<>();
    protected final Map<InetAddress, Long> lastReplyNanos = new HashMap<>();
    protected final Dispatcher dispatcher;

    public ArtNetNetworkScanner(Dispatcher dispatcher, Selector selector) {
        /* we only allocate a large enough response buffer to pull in the header of the packet, since
           all we're interested in is whether the response packet is an ArtNet POLLREPLY header. */
        super(selector, "ArtNet", ArtNetDatagramUtil.HEADER_LENGTH,
              new ByteBuffer[] { ByteBuffer.wrap(new ArtNetPollDatagram(null).getBytes()) });
        this.dispatcher = dispatcher;
    }

    @Override
    protected void prepareChannel(DatagramChannel chan, InetAddress iface) throws IOException {
        /* MacOS and Linux let us bind directly to broadcast addresses, and just
           deliver us only broadcast packets received on that interface. Windows
           is a little more stubborn and only lets you bind to the local IP
           address, so we have to do some digging to figure out which IP address
           corresponds to the interface with the given broadcast address.
           Theoretically we could only run this on Windows, but in the interest
           of making all platforms as similar as possible and because MacOS and
           Linux also receive broadcasts on sockets bound to a local address, we
           do this for everyone. */
        InetAddress addr = null;
        outer: for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InterfaceAddress iaddr : ni.getInterfaceAddresses()) {
                if (iaddr.getBroadcast() != null && iaddr.getBroadcast().equals(iface)) {
                    addr = iaddr.getAddress();
                    break outer;
                }
            }
        }
        if (addr == null) {
            throw new IOException("no usable bind address found for broadcast address " + iface);
        }
        chan.bind(new InetSocketAddress(addr, ArtNetDatagramUtil.ARTNET_PORT));
    }

    @Override
    protected void sendPacket(InetAddress broadcast, DatagramChannel chan, ByteBuffer data) throws IOException {
        /* because the channel is bound and listening to broadcasts, we can't send broadcasts from it;
           unlike with OPC, where devices respond to the port that sent the broadcast, ArtNet devices
           broadcast their poll reply back to the whole network. We make an ephemeral socket here just
           to send the data, and then throw the socket away. */
        final DatagramPacket packet = new DatagramPacket(
            data.array(), data.array().length, broadcast, ArtNetDatagramUtil.ARTNET_PORT);
        final DatagramSocket sendSock = new DatagramSocket();
        sendSock.setBroadcast(true);
        sendSock.send(packet);
    }

    @Override
    protected void onReply(SocketAddress recvAddr, ByteBuffer response) {
        if (recvAddr != null && ArtNetDatagramUtil.isArtNetPacket(response.array()) &&
            ArtNetDatagramUtil.getOpCode(response.array()) == ARTNET_POLLREPLY_OPCODE) {
            final InetAddress addr = ((InetSocketAddress) recvAddr).getAddress();
            dispatcher.dispatchEngine(new Runnable() {
                public void run() {
                    if (!deviceList.contains(addr)) {
                        System.out.println(String.format("found new ArtNet device at %s", addr.getHostAddress()));
                    }
                    lastReplyNanos.put(addr, System.nanoTime());
                    deviceList.add(addr);
                }
            });
        }
    }

    @Override
    protected void expireDevices() {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                long now = System.nanoTime();
                List<InetAddress> addrs = new ArrayList<>(deviceList);
                for (InetAddress addr : addrs) {
                    long last = lastReplyNanos.get(addr);
                    if (now - last > MAX_MILLIS_WITHOUT_REPLY * 1e6) {
                        if (deviceList.contains(addr)) {
                            System.out.println(
                                String.format(
                                    "lost %s device at %s, last reply was %.3f seconds ago",
                                    protoName, addr.getHostAddress(), 1e-9 * (now - last)));
                        }
                        deviceList.remove(addr);
                        lastReplyNanos.remove(addr);
                    }
                }
            }
        });
    }
}
