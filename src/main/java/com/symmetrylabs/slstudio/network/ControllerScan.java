package com.symmetrylabs.slstudio.network;

import java.util.Arrays;
import java.net.InetAddress;

import com.symmetrylabs.util.listenable.ListenableList;
import com.symmetrylabs.util.dispatch.Dispatcher;

public class ControllerScan {

    public final ListenableList<NetworkDevice> networkDevices = new ListenableList<NetworkDevice>();

    private final Dispatcher dispatcher;

    public ControllerScan(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void scan() {
        new Runnable() {
            final ListenableList<NetworkDevice> tmpNetworkDevices = new ListenableList<NetworkDevice>();
            int instances = 0;

            public void run() {
                for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
                    instances++;
                    new MacAddrCommand(addr, new MacAddrCommandCallback() {
                        public void onResponse(java.net.DatagramPacket response, byte[] macAddr) {
                            final NetworkDevice networkDevice = new NetworkDevice(response.getAddress(), macAddr);
                            dispatcher.dispatchEngine(() -> tmpNetworkDevices.add(networkDevice));
                        }

                        public void onFinish() {
                            dispatcher.dispatchEngine(new Runnable() {
                                public void run() {
                                    if (--instances == 0) {
                                        for (int i = 0; i < networkDevices.size(); i++) {
                                            NetworkDevice device = networkDevices.get(i);
                                            boolean found = false;
                                            boolean removed = false;
                                            for (NetworkDevice tmpDevice : tmpNetworkDevices) {
                                                if (Arrays.equals(device.macAddress, tmpDevice.macAddress)
                                                    && device.ipAddress.equals(tmpDevice.ipAddress)) {
                                                    found = true;
                                                    device.connectionRetries = 0;
                                                    break;
                                                } else if (Arrays.equals(device.macAddress, tmpDevice.macAddress)
                                                    || device.ipAddress.equals(tmpDevice.ipAddress)) {
                                                    removed = true;
                                                    networkDevices.remove(i--);
                                                    break;
                                                }
                                            }
                                            if (!found && !removed) {
                                                if (++device.connectionRetries == 5) {
                                                    networkDevices.remove(i--);
                                                }
                                            }
                                        }
                                        for (NetworkDevice tmpDevice : tmpNetworkDevices) {
                                            boolean found = false;
                                            for (NetworkDevice device : networkDevices) {
                                                if (Arrays.equals(device.macAddress, tmpDevice.macAddress)) {
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (!found) {
                                                networkDevices.add(tmpDevice);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }.run();
    }
}
