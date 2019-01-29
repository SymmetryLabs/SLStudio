package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.slstudio.output.ArtNetDatagramUtil;
import com.symmetrylabs.slstudio.output.ArtNetPollDatagram;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.dispatch.Dispatcher;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ArtNetNetworkScanner extends UdpBroadcastNetworkScanner {
    protected static final long MAX_MILLIS_WITHOUT_REPLY = 15_000L;
    protected static final short ARTNET_POLLREPLY_OPCODE = 0x2100;

    public final ListenableSet<InetAddress> deviceList = new ListenableSet<>();
    protected Map<InetAddress, Long> lastReplyNanos = new HashMap<>();

    public ArtNetNetworkScanner(Dispatcher dispatcher) {
        /* we only allocate a large enough response buffer to pull in the header of the packet, since
           all we're interested in is whether the response packet is an ArtNet POLLREPLY header. */
        super(dispatcher, "ArtNet", ArtNetDatagramUtil.ARTNET_PORT, ArtNetDatagramUtil.HEADER_LENGTH, MAX_MILLIS_WITHOUT_REPLY);
    }

    @Override
    protected DatagramPacket[] getDiscoverPackets(InetAddress addr) {
        return new DatagramPacket[] {
            new ArtNetPollDatagram(addr).getPacket(),
        };
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

    protected void updateDevice(final InetAddress addr) {
    }

    @Override
    protected void expireDevices() {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                long now = System.nanoTime();
                List<InetAddress> addrs = new ArrayList<>(deviceList);
                for (InetAddress addr : addrs) {
                    long last = lastReplyNanos.get(addr);
                    if (now - last > maxMillisWithoutReply * 1e6) {
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
