package com.symmetrylabs.slstudio.network;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import heronarts.lx.LX;

import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.AbstractListListener;
import com.symmetrylabs.util.listenable.ListenableList;

public class NetworkMonitor {
    public final ListenableList<NetworkDevice> deviceList;

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

        deviceList.addListener(new AbstractListListener<NetworkDevice>() {
            public void itemAdded(int index, final NetworkDevice newDevice) {
                if (newDevice.versionId.isEmpty()) {
                    warnOldVersion();
                }
                new VersionCommand(newDevice.ipAddress, new VersionCommandCallback() {
                    public void onResponse(java.net.DatagramPacket response, final int version) {
                        dispatcher.dispatchEngine(() -> newDevice.versionNumber.set(version));
                    }

                    public void onFinish() {
                        dispatcher.dispatchEngine(() -> {
                            for (NetworkDevice device : deviceList) {
                                if (device.versionNumber.get() != -1 &&
                                      device.versionNumber.get() != newDevice.versionNumber.get()) {
                                    warnOldVersion();
                                }
                            }
                        });
                    }
                });
            }
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
