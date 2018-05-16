package com.symmetrylabs.slstudio.network;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.dispatch.Dispatcher;

import static com.symmetrylabs.slstudio.network.OpcMessage.SYMMETRY_LABS;
import static com.symmetrylabs.slstudio.network.OpcMessage.SYMMETRY_LABS_IDENTIFY;
import static com.symmetrylabs.slstudio.network.OpcMessage.SYMMETRY_LABS_IDENTIFY_REPLY;

public class NetworkScanner {
    public final ListenableSet<NetworkDevice> deviceList = new ListenableSet<NetworkDevice>();

    protected Map<String, NetworkDevice> deviceMap = new HashMap<>();
    protected Map<String, Long> lastReplyMillis = new HashMap<>();
    protected final Dispatcher dispatcher;
    protected static long MAX_MILLIS_WITHOUT_REPLY = 2500;

    public NetworkScanner(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void scan() {
        expireDevices();
        for (InetAddress broadcast : NetworkUtils.getBroadcastAddresses()) {
            try (OpcSocket socket = new OpcSocket(broadcast)) {
                socket.send(new OpcMessage(0x88, 4));
                socket.send(new OpcMessage(0, SYMMETRY_LABS, SYMMETRY_LABS_IDENTIFY));
                socket.listenMultiple(1000, (src, reply) -> {
                    if (reply.bytes.length == 6) {
                        updateDevice(NetworkDevice.fromMacAddress(src, reply.bytes));
                    } else if (reply.isSysex(SYMMETRY_LABS, SYMMETRY_LABS_IDENTIFY_REPLY)) {
                        updateDevice(NetworkDevice.fromIdentifier(src, reply.getSysexContent()));
                    }
                });
            }
        }
    }

    public void expireDevices() {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                List<NetworkDevice> expiredDevices = new ArrayList<>();
                long now = System.currentTimeMillis();
                for (String addr : deviceMap.keySet()) {
                    if (now > lastReplyMillis.get(addr) + MAX_MILLIS_WITHOUT_REPLY) {
                        expiredDevices.add(deviceMap.get(addr));
                        deviceMap.remove(addr);
                        lastReplyMillis.remove(addr);
                    }
                }
                deviceList.removeAll(expiredDevices);
            }
        });
    }

    public void updateDevice(final NetworkDevice newDevice) {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                String addr = newDevice.ipAddress.toString();
                lastReplyMillis.put(addr, System.currentTimeMillis());

                NetworkDevice existing = deviceMap.get(addr);
                if (!newDevice.equals(existing)) {
                    // Some controllers reply to both commands, once with a product ID
                    // and once without.  Keep the reply with the product ID.
                    if (existing != null && newDevice.deviceId.equals(existing.deviceId) &&
                        !existing.productId.isEmpty()) {
                        return;
                    }

                    if (existing != null) {
                        deviceList.remove(existing);
                    }
                    deviceMap.put(addr, newDevice);
                    deviceList.add(newDevice);
                }
            }
        });
    }
}
