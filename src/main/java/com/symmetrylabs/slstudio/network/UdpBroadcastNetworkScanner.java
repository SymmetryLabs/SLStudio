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

public abstract class UdpBroadcastNetworkScanner {
    protected final Dispatcher dispatcher;
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Map<InetAddress, DatagramChannel> recvChans;
    protected Selector recvSelector;
    protected Set<InetAddress> errorBroadcasts = new HashSet<>();
    protected final int broadcastPort;
    protected final int responseBufferSize;
    protected final long maxMillisWithoutReply;
    protected final String protoName;

    public UdpBroadcastNetworkScanner(
        Dispatcher dispatcher, String protoName, int broadcastPort, int responseBufferSize, long maxMillisWithoutReply) {
        this.dispatcher = dispatcher;
        this.broadcastPort = broadcastPort;
        this.responseBufferSize = responseBufferSize;
        this.maxMillisWithoutReply = maxMillisWithoutReply;
        this.protoName = protoName;

        recvChans = new HashMap<>();
        try {
            recvSelector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            /* if we can't set up a selector, we aren't going to be able to discover anything,
                 and we aren't going to be able to run the show, so let's crash SLStudio. */
            throw new RuntimeException(e);
        }
    }

    protected abstract DatagramPacket[] getDiscoverPackets(InetAddress addr);
    protected abstract void onReply(SocketAddress addr, ByteBuffer response);
    protected abstract void expireDevices();

    public void scan() {
        expireInterfaces();
        expireDevices();
        for (InetAddress broadcast : NetworkUtils.getBroadcastAddresses()) {
            /* Create the channel that we'll receive return broadcasts on, if we haven't made
               one already. */
            if (!recvChans.containsKey(broadcast)) {
                try {
                    DatagramChannel recvChan = SelectorProvider.provider().openDatagramChannel();
                    recvChan.configureBlocking(false);
                    recvChan.bind(new InetSocketAddress(broadcast, broadcastPort));
                    recvChan.register(recvSelector, SelectionKey.OP_READ, broadcast);
                    recvChans.put(broadcast, recvChan);
                } catch (IOException e) {
                    if (!errorBroadcasts.contains(broadcast)) {
                        System.err.println("couldn't set up " + protoName + " discovery listener:");
                        e.printStackTrace();
                        errorBroadcasts.add(broadcast);
                        continue;
                    }
                }
            }

            /* Then send the discovery packet */
            try {
                final DatagramSocket sendSock = new DatagramSocket();
                sendSock.setBroadcast(true);
                DatagramPacket[] packets = getDiscoverPackets(broadcast);
                for (int i = 0; i < packets.length; i++) {
                    sendSock.send(packets[i]);
                }
            } catch (IOException e) {
                if (!errorBroadcasts.contains(broadcast)) {
                    System.err.println(String.format("couldn't send %s poll to %s:", protoName, broadcast));
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

            ByteBuffer recvBuf = ByteBuffer.allocate(responseBufferSize);
            try {
                SocketAddress recvAddr = chan.receive(recvBuf);
                onReply(recvAddr, recvBuf);
            } catch (IOException e) {
                System.err.println("unable to receive on datagram channel:");
                e.printStackTrace();
            }

            /* It's our responsibility to remove things from the selected set once we've
                 acted on them. */
            iter.remove();
        }
    }

    private void expireInterfaces() {
        Set<InetAddress> toRemove = new HashSet<InetAddress>(recvChans.keySet());
        toRemove.removeAll(NetworkUtils.getBroadcastAddresses());
        for (InetAddress a : toRemove) {
            DatagramChannel chan = recvChans.get(a);
            /* closing the channel removes it from the selector set */
            try {
                chan.close();
            } catch (IOException e) {}
            recvChans.remove(a);
        }
    }
}
