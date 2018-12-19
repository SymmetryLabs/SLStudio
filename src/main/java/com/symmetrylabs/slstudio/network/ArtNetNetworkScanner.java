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

import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.dispatch.Dispatcher;

public class ArtNetNetworkScanner {
    private static final int MAX_MILLIS_WITHOUT_REPLY = 10000;
    private static final int ARTNET_DISCOVERY_TIMEOUT = 3000;

    public final ListenableSet<InetAddress> deviceList = new ListenableSet<>();
    protected Map<InetAddress, Long> lastReplyMillis = new HashMap<>();
    protected final Dispatcher dispatcher;
    protected final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ArtNetNetworkScanner(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void scan() {
        expireDevices();
        for (InetAddress broadcast : NetworkUtils.getBroadcastAddresses()) {
            ArtNetPollDatagram poll = new ArtNetPollDatagram(broadcast);
            try (DatagramSocket socket = new DatagramSocket(ArtNetDatagramUtil.ARTNET_PORT, broadcast)) {
                socket.send(poll.getPacket());
                executor.submit(new ListenTask(socket, ARTNET_DISCOVERY_TIMEOUT));
            } catch (IOException e) {
                System.err.println("unable to send ArtNet discovery packet: " + e.getMessage());
            }
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
        final DatagramSocket socket;
        final int timeoutMillis;

        ListenTask(DatagramSocket socket, int timeoutMillis) {
            this.socket = socket;
            this.timeoutMillis = timeoutMillis;
        }

        public void run() {
            try {
                try {
                    socket.setSoTimeout(timeoutMillis);
                    final DatagramPacket reply = new DatagramPacket(new byte[65535], 65535);
                    while (true) {
                        socket.receive(reply);  // throws SocketTimeoutException upon a timeout
                        updateDevice(reply.getAddress());
                    }
                } finally {
                    socket.close();
                }
            } catch (SocketTimeoutException e) {
                // ignore; no need to print the stack trace
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
