package com.symmetrylabs.slstudio.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.dispatch.Dispatcher;

import static com.symmetrylabs.slstudio.network.OpcMessage.SYMMETRY_LABS;
import static com.symmetrylabs.slstudio.network.OpcMessage.SYMMETRY_LABS_IDENTIFY;
import static com.symmetrylabs.slstudio.network.OpcMessage.SYMMETRY_LABS_IDENTIFY_REPLY;

public class OpcNetworkScanner extends UdpBroadcastNetworkScanner {
    protected static long MAX_MILLIS_WITHOUT_REPLY = 10000;

    public final ListenableSet<NetworkDevice> deviceList = new ListenableSet<NetworkDevice>();
    protected Map<String, NetworkDevice> deviceMap = new HashMap<>();
    protected Map<String, Long> lastReplyMillis = new HashMap<>();

    public OpcNetworkScanner(Dispatcher dispatcher) {
        super(dispatcher, "OPC", OpcSocket.DEFAULT_PORT, 65536, MAX_MILLIS_WITHOUT_REPLY);
    }

    private DatagramPacket messageToPacket(OpcMessage msg, SocketAddress addr) {
        return new DatagramPacket(msg.bytes, msg.bytes.length, addr);
    }

    @Override
    protected DatagramPacket[] getDiscoverPackets(InetAddress addr) {
        return new DatagramPacket[] {
            /* this is a legacy "poll" message that is needed for cubes with very old firmware */
            messageToPacket(new OpcMessage(0x88, 4), new InetSocketAddress(addr, OpcSocket.DEFAULT_PORT)),
            messageToPacket(new OpcMessage(0, SYMMETRY_LABS, SYMMETRY_LABS_IDENTIFY), new InetSocketAddress(addr, OpcSocket.DEFAULT_PORT)),
        };
    }

    @Override
    protected void onReply(SocketAddress addr, ByteBuffer response) {
        OpcMessage reply = new OpcMessage(response.array(), response.position());
        InetSocketAddress insa = (InetSocketAddress) addr;
        InetAddress ina = insa.getAddress();
        if (reply.rawLength == 6) {
            updateDevice(NetworkDevice.fromMacAddress(ina, reply.bytes));
        } else if (reply.isSysex(SYMMETRY_LABS, SYMMETRY_LABS_IDENTIFY_REPLY)) {
            updateDevice(NetworkDevice.fromIdentifier(ina, reply.getSysexContent()));
        }
    }

    @Override
    protected void expireDevices() {
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                long now = System.currentTimeMillis();
                List<String> addrs = new ArrayList<>(deviceMap.keySet());  // avoid ConcurrentModificationException
                for (String addr : addrs) {
                    if (now > lastReplyMillis.get(addr) + MAX_MILLIS_WITHOUT_REPLY) {
                        deviceList.remove(deviceMap.get(addr));
                        deviceMap.remove(addr);
                        lastReplyMillis.remove(addr);
                    }
                }
            }
        });
    }

    public void updateDevice(final NetworkDevice newDevice) {
        System.out.println("updating device " + newDevice);
        dispatcher.dispatchEngine(new Runnable() {
            public void run() {
                String addr = newDevice.ipAddress.toString();
                lastReplyMillis.put(addr, System.currentTimeMillis());

                NetworkDevice existing = deviceMap.get(addr);
                if (!newDevice.equals(existing)) {
                    // Some controllers reply to both commands,     once with a product ID
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
