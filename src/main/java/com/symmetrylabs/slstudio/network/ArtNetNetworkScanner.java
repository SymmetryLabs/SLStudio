package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.slstudio.output.ArtNetDatagramUtil;
import com.symmetrylabs.slstudio.output.ArtNetPollDatagram;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.nio.ByteBuffer;
import java.net.SocketAddress;

import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.dispatch.Dispatcher;

public class ArtNetNetworkScanner {
    protected static final long MAX_NANOS_WITHOUT_REPLY = 15_000_000_000L;
    protected static final int ARTNET_DISCOVERY_TIMEOUT = 10000;
    protected static final short ARTNET_POLLREPLY_OPCODE = 0x2100;

    public final ListenableSet<InetAddress> deviceList = new ListenableSet<>();
    protected Map<InetAddress, Long> lastReplyNanos = new HashMap<>();
    protected final Dispatcher dispatcher;
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Map<InetAddress, DatagramChannel> recvChans;
    protected Selector recvSelector;
    protected Set<InetAddress> errorBroadcasts = new HashSet<>();

    public ArtNetNetworkScanner(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        recvChans = new HashMap<>();
        try {
            recvSelector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            /* if we can't set up a selector, we aren't going to be able to discover anything,
                 and we aren't going to be able to run the show, so let's crash SLStudio. */
            throw new RuntimeException(e);
        }
    }

    public void scan() {
        expireInterfaces();
        expireDevices();
        for (InetAddress broadcast : NetworkUtils.getBroadcastAddresses()) {
            if (!recvChans.containsKey(broadcast)) {
                try {
                    System.out.println("bind to " + broadcast);
                    DatagramChannel recvChan = SelectorProvider.provider().openDatagramChannel();
                    recvChan.configureBlocking(false);
                    recvChan.bind(new InetSocketAddress(broadcast, ArtNetDatagramUtil.ARTNET_PORT));
                    recvChan.register(recvSelector, SelectionKey.OP_READ, broadcast);
                    recvChans.put(broadcast, recvChan);
                } catch (IOException e) {
                    if (!errorBroadcasts.contains(broadcast)) {
                        System.err.println("couldn't set up discovery listener:");
                        e.printStackTrace();
                        errorBroadcasts.add(broadcast);
                        continue;
                    }
                }
            }
            try {
                final DatagramSocket sendSock = new DatagramSocket();
                sendSock.setBroadcast(true);
                sendSock.send(new ArtNetPollDatagram(broadcast).getPacket());
            } catch (IOException e) {
                if (!errorBroadcasts.contains(broadcast)) {
                    System.err.println(String.format("couldn't send artnet poll to %s:", broadcast));
                    e.printStackTrace();
                    errorBroadcasts.add(broadcast);
                    continue;
                }
            }
        }
        try {
            recvSelector.selectNow();
        } catch (IOException e) {
            System.err.println("unable to select on sockets:");
            e.printStackTrace();
        }

        Iterator<SelectionKey> iter = recvSelector.selectedKeys().iterator();
        while (iter.hasNext()) {
            SelectionKey sel = iter.next();
            DatagramChannel chan = (DatagramChannel) sel.channel();

            /* We just need the header to know if this is a PollReply, the rest of
                 the packet will be silently discarded. */
            ByteBuffer recvBuf = ByteBuffer.allocate(ArtNetDatagramUtil.HEADER_LENGTH);
            try {
                SocketAddress recvAddr = chan.receive(recvBuf);
                if (recvAddr != null &&
                        ArtNetDatagramUtil.isArtNetPacket(recvBuf.array()) &&
                        ArtNetDatagramUtil.getOpCode(recvBuf.array()) == ARTNET_POLLREPLY_OPCODE) {
                    updateDevice(((InetSocketAddress) recvAddr).getAddress());
                }
            } catch (IOException e) {
                System.err.println("unable to receive on datagram channel:");
                e.printStackTrace();
            }

            /* It's our responsibility to remove things from the selected set once we've
                 acted on them. */
            iter.remove();
        }
    }

    public void expireDevices() {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                long now = System.nanoTime();
                List<InetAddress> addrs = new ArrayList<>(deviceList);
                for (InetAddress addr : addrs) {
                    long last = lastReplyNanos.get(addr);
                    if (now - last > MAX_NANOS_WITHOUT_REPLY) {
                        if (deviceList.contains(addr)) {
                            System.out.println(
                                String.format(
                                    "lost ArtNet device at %s, last reply was %.3f seconds ago",
                                    addr.getHostAddress(), 1e-9 * (now - last)));
                        }
                        deviceList.remove(addr);
                        lastReplyNanos.remove(addr);
                    }
                }
            }
        });
    }

    private void expireInterfaces() {
        Set<InetAddress> toRemove = new HashSet<InetAddress>(recvChans.keySet());
        toRemove.removeAll(NetworkUtils.getBroadcastAddresses());
        for (InetAddress a : toRemove) {
            System.out.println("unbind from interface " + a);
            DatagramChannel chan = recvChans.get(a);
            /* closing the channel removes it from the selector set */
            try {
                chan.close();
            } catch (IOException e) {}
            recvChans.remove(a);
        }
    }

    public void updateDevice(final InetAddress addr) {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                if (!deviceList.contains(addr)) {
                    System.out.println("found new ArtNet device at " + addr.getHostAddress());
                }
                lastReplyNanos.put(addr, System.nanoTime());
                deviceList.add(addr);
            }
        });
    }
}
