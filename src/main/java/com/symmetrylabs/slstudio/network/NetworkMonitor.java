package com.symmetrylabs.slstudio.network;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;

import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;

public class NetworkMonitor {
    public final ListenableSet<NetworkDevice> deviceList;

    private final NetworkScanner networkScanner;
    private final Timer timer = new Timer();

    private boolean started = false;
    private boolean oldVersionWarningGiven = false;

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
        networkScanner = new NetworkScanner(dispatcher);
        deviceList = networkScanner.deviceList;

        deviceList.addListener(new SetListener<NetworkDevice>() {
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
                                for (NetworkDevice device : deviceList) {
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
        if (!oldVersionWarningGiven) {
            System.out.println("WARNING: One or more cubes have outdated firmware!");
            oldVersionWarningGiven = true;
        }
    }

    public synchronized NetworkMonitor start() {
        if (!started) {
            timer.schedule(new TimerTask() {
                public void run() {
                    networkScanner.scan();
                }
            }, 0, 500);
            started = true;
        }
        return this;
    }
}
