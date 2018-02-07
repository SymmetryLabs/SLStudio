package com.symmetrylabs.slstudio.network;

import java.util.Map;
import java.util.HashMap;

import heronarts.lx.LX;

import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.AbstractListListener;
import com.symmetrylabs.util.listenable.ListenableList;

public class NetworkMonitor {

    private final ControllerScan controllerScan = new ControllerScan();

    public final ListenableList<NetworkDevice> networkDevices = controllerScan.networkDevices;

    private final java.util.TimerTask scanTask = new ScanTask();
    private final java.util.Timer timer = new java.util.Timer();

    private boolean oldVersionWarningGiven = false;

    private static Map<LX, NetworkMonitor> instanceByLX = new HashMap<>();

    public static synchronized NetworkMonitor getInstance(LX lx) {
        if (!instanceByLX.containsKey(lx)) {
            instanceByLX.put(lx, new NetworkMonitor(lx));
        }
        return instanceByLX.get(lx);
    }

    private NetworkMonitor(LX lx) {
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);

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

    public void start() {
        timer.schedule(scanTask, 0, 500);
    }

    class ScanTask extends java.util.TimerTask {
        public void run() {
            controllerScan.scan();
        }
    }

}
