package com.symmetrylabs.slstudio.network;

import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.util.dispatch.Dispatcher;
import com.symmetrylabs.slstudio.util.listenable.AbstractListListener;
import com.symmetrylabs.slstudio.util.listenable.ListenableList;

public class NetworkMonitor {

    private final ControllerScan controllerScan;

    public final ListenableList<NetworkDevice> networkDevices;

    private final java.util.TimerTask scanTask = new ScanTask();
    private final java.util.Timer timer = new java.util.Timer();

    private boolean oldVersionWarningGiven = false;

    private static Map<LX, WeakReference<NetworkMonitor>> instanceByLX = new WeakHashMap<>();

    private boolean started = false;

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
        controllerScan = new ControllerScan(dispatcher);
        networkDevices = controllerScan.networkDevices;

        networkDevices.addListener(new AbstractListListener<NetworkDevice>() {
            public void itemAdded(int index, final NetworkDevice result) {
                new VersionCommand(result.ipAddress, new VersionCommandCallback() {
                    public void onResponse(java.net.DatagramPacket response, final int version) {
                        dispatcher.dispatchEngine(() -> result.version.set(version));
                    }

                    public void onFinish() {
                        dispatcher.dispatchEngine(() -> {
                            if (!oldVersionWarningGiven) {
                                for (NetworkDevice device : networkDevices) {
                                    if (device.version.get() != -1
                                            && (device.version.get() < result.version.get()
                                            || device.version.get() > result.version.get())) {

                                        System.out.println("WARNING: One or more cubes have outdated firmware!");
                                        oldVersionWarningGiven = true;

                                        return;
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public synchronized NetworkMonitor start() {
        if (started)
            return this;

        timer.schedule(scanTask, 0, 500);
        started = true;
        return this;
    }

    class ScanTask extends java.util.TimerTask {
        public void run() {
            controllerScan.scan();
        }
    }

}
