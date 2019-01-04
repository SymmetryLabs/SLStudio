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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.dispatch.Dispatcher;

public class ArtNetNetworkScanner {
    protected static final int MAX_MILLIS_WITHOUT_REPLY = 10000;
    protected static final int ARTNET_DISCOVERY_TIMEOUT = 3000;
    protected static final short ARTNET_POLLREPLY_OPCODE = 0x2100;

    public final ListenableSet<InetAddress> deviceList = new ListenableSet<>();
    protected Map<InetAddress, Long> lastReplyMillis = new HashMap<>();
    protected final Dispatcher dispatcher;
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Map<InetAddress, DatagramSocket> recvSocks;

    public ArtNetNetworkScanner(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        recvSocks = new HashMap<>();
    }

    public void scan() {
        expireDevices();
        for (InetAddress broadcast : NetworkUtils.getBroadcastAddresses()) {
            if (!recvSocks.containsKey(broadcast)) {
                try {
                    System.out.println("bind to " + broadcast);
                    DatagramSocket recvSock = new DatagramSocket(null);
                    recvSock.bind(new InetSocketAddress(broadcast, ArtNetDatagramUtil.ARTNET_PORT));
                    recvSock.setSoTimeout(ARTNET_DISCOVERY_TIMEOUT);
                    recvSock.setReuseAddress(true);
                    recvSocks.put(broadcast, recvSock);
                } catch (SocketException e) {
                    System.err.println("couldn't set up discovery listener:");
                    e.printStackTrace();
                }
            }
            executor.submit(new ListenTask(broadcast));
        }
    }

    public void expireDevices() {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                long now = System.currentTimeMillis();
                List<InetAddress> addrs = new ArrayList<>(deviceList);
                for (InetAddress addr : addrs) {
                    if (now > lastReplyMillis.get(addr) + MAX_MILLIS_WITHOUT_REPLY) {
                        if (!deviceList.contains(addr)) {
                            System.out.println("lost ArtNet device at " + addr.getHostAddress());
                        }
                        deviceList.remove(addr);
                        lastReplyMillis.remove(addr);
                    }
                }
            }
        });
    }

    public void updateDevice(final InetAddress addr) {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                if (!deviceList.contains(addr)) {
                    System.out.println("found new ArtNet device at " + addr.getHostAddress());
                }
                lastReplyMillis.put(addr, System.currentTimeMillis());
                deviceList.add(addr);
            }
        });
    }

    protected class ListenTask implements Runnable {
        final InetAddress broadcast;

        ListenTask(InetAddress broadcast) {
            this.broadcast = broadcast;
        }

        public void run() {
            ArtNetPollDatagram poll = new ArtNetPollDatagram(broadcast);
            DatagramSocket recvSock = recvSocks.get(broadcast);
            try {
                final DatagramSocket sendSock = new DatagramSocket();
                sendSock.setBroadcast(true);
                sendSock.send(poll.getPacket());

                final DatagramPacket reply = new DatagramPacket(new byte[65535], 65535);
                while (true) {
                    recvSock.receive(reply);  // throws SocketTimeoutException upon a timeout

                    byte[] respData = reply.getData();
                    if (ArtNetDatagramUtil.isArtNetPacket(respData) && ArtNetDatagramUtil.getOpCode(respData) == ARTNET_POLLREPLY_OPCODE) {
                        updateDevice(reply.getAddress());
                    }
                }
            } catch (SocketTimeoutException e) {
                // ignore; no need to print the stack trace
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
