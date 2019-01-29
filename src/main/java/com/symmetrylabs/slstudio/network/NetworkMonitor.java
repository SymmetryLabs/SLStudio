package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

public class NetworkMonitor {
    private static final int SCAN_PERIOD_MS = 500;

    public final ListenableSet<NetworkDevice> opcDeviceList;
    public final ListenableSet<InetAddress> artNetDeviceList;

    private final OpcNetworkScanner opcNetworkScanner;
    private final ArtNetNetworkScanner artNetNetworkScanner;
    private final Selector recvSelector;
    private final Timer timer = new Timer("NetworkScanner");

    private boolean started = false;

    private static Map<LX, WeakReference<NetworkMonitor>> instanceByLX = new WeakHashMap<>();

    public static synchronized NetworkMonitor getInstance(LX lx) {
        WeakReference<NetworkMonitor> weakRef = instanceByLX.get(lx);
        NetworkMonitor ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new NetworkMonitor(lx)));
        }
        return ref;
    }

    private NetworkMonitor(LX lx) {
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);
        try {
            recvSelector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            /* if we can't set up a selector, we aren't going to be able to discover anything,
               and we aren't going to be able to run the show, so let's crash SLStudio. */
            throw new RuntimeException(e);
        }

        opcNetworkScanner = new OpcNetworkScanner(dispatcher, recvSelector);
        opcDeviceList = opcNetworkScanner.deviceList;

        artNetNetworkScanner = new ArtNetNetworkScanner(dispatcher, recvSelector);
        artNetDeviceList = artNetNetworkScanner.deviceList;

        opcDeviceList.addListener(new SetListener<NetworkDevice>() {
            public void onItemAdded(final NetworkDevice newDevice) {
                if (newDevice.versionId.isEmpty()) {
                    warnOldVersion();
                }
                try (OpcSocket socket = new OpcSocket(newDevice.ipAddress)) {
                    // @Deprecated: Remove this after all controllers are updated to Aura.
                    socket.send(new OpcMessage(0x88, 3));
                    socket.listen(1000, (src, reply) -> {
                        if (reply.bytes.length == 1) {
                            int version = Byte.toUnsignedInt(reply.bytes[0]);
                            dispatcher.dispatchEngine(() -> {
                                newDevice.version.set(version);
                                for (NetworkDevice device : opcDeviceList) {
                                    if (device.version.get() != -1 &&
                                        device.version.get() != version) {
                                        warnOldVersion();
                                    }
                                }
                            });
                        }
                });
                }
            }
            public void onItemRemoved(final NetworkDevice device) { }
        });
    }

    private void warnOldVersion() {
        SLStudio.setWarning("NetworkMonitor", "One or more cubes have outdated firmware!");
    }

    public synchronized NetworkMonitor start() {
        if (!started) {
            Thread t = new Thread(this::loop);
            t.setDaemon(true);
            t.start();
            started = true;
        }
        return this;
    }

    public synchronized void stop() {
        started = false;
    }

    private void loop() {
        while (started) {
            opcNetworkScanner.scan();
            artNetNetworkScanner.scan();

            long start = System.nanoTime();
            long remaining;

            do {
                /* wait for at most SCAN_PERIOD_MS for a response on any of our channels */
                try {
                    recvSelector.select(SCAN_PERIOD_MS);
                } catch (IOException e) {
                    System.err.println("unable to select on network discovery selector:");
                    e.printStackTrace();
                }

                /* go through each ready channel and notify the associated network scanner that
                   it's got mail. */
                Iterator<SelectionKey> iter = recvSelector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey sel = iter.next();

                    /* UdpBroadcastNetworkScanner is the only thing that gets to stick stuff to our
                       Selector, and it promises the attachment is always a poitner to a
                       UdpBroadcastNetworkScanner. */
                    UdpBroadcastNetworkScanner sc = (UdpBroadcastNetworkScanner) sel.attachment();
                    sc.onSelected(sel);

                    /* It's our responsibility to remove things from the selected set once we've
                       acted on them. */
                    iter.remove();
                }
                /* it's possible that our selector could wait way less time than SCAN_PERIOD_MS.
                   we keep looping on our select until our scan period has elapsed, then we go to
                   the start of the outer loop and send discovery packets again. */
                remaining = 1_000_000L * SCAN_PERIOD_MS - (System.nanoTime() - start);
            } while (remaining > 0);
        }
    }
}
