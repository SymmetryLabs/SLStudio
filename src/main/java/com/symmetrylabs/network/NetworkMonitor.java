package com.symmetrylabs.network;

import com.symmetrylabs.SLStudio;
import com.symmetrylabs.util.listenable.AbstractListListener;
import com.symmetrylabs.util.listenable.ListenableList;
import heronarts.lx.LX;


public class NetworkMonitor {

    private final ControllerScan controllerScan = new ControllerScan();

    public final ListenableList<NetworkDevice> networkDevices = controllerScan.networkDevices;

    private final java.util.TimerTask scanTask = new ScanTask();
    private final java.util.Timer timer = new java.util.Timer();

    private boolean oldVersionWarningGiven = false;

    public NetworkMonitor(LX lx) {
        networkDevices.addListener(new AbstractListListener<NetworkDevice>() {
            public void itemAdded(int index, final NetworkDevice result) {
                new VersionCommand(result.ipAddress, new VersionCommandCallback() {
                    public void onResponse(java.net.DatagramPacket response, final int version) {
                        SLStudio.applet.dispatcher.dispatchEngine(new Runnable() {
                            public void run() {
                                result.version.set(version);
                            }
                        });
                    }

                    public void onFinish() {
                        SLStudio.applet.dispatcher.dispatchEngine(new Runnable() {
                            public void run() {
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
