package com.symmetrylabs.slstudio.sync;

import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.UdpBroadcastNetworkScanner;
import com.symmetrylabs.util.NetworkUtils;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEngine;
import heronarts.lx.LXPattern;
import heronarts.lx.LXComponent;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Network synchronization manager for coordinating patterns between multiple SLStudio instances.
 * Handles peer discovery, master/slave election, and pattern synchronization with fader automation.
 */
public class NetworkSyncManager extends LXComponent implements LXParameterListener {
    
    public static final int DISCOVERY_PORT = 8899;
    public static final int SYNC_OSC_PORT = 8002;
    public static final int TARGET_CHANNEL = 14;  // Channel to sync (0-based, so 14 = 15th channel)
    public static final int TARGET_PATTERN_INDEX = 0;  // First pattern
    public static final int HEARTBEAT_INTERVAL_MS = 2000;  // 2 seconds
    public static final int SYNC_HEARTBEAT_INTERVAL_MS = 2000;  // Re-send current pattern every 2 seconds
    public static final int CONNECTION_TIMEOUT_MS = 10000;  // 10 seconds (5x heartbeat for robustness)
    
    // Wi-Fi interface to use for sync. On macOS the device name varies (en0, en1, etc.)
    // so we detect it by display name first, then fall back to common device names.
    private static final String[] WIFI_INTERFACE_FALLBACKS = { "en0", "en1" };
    
    private final LX lx;
    private final NetworkMonitor networkMonitor;
    private final LXOscEngine oscEngine;
    private DatagramSocket discoverySocket;
    private final LXOscEngine.Receiver oscReceiver;
    private final LXOscEngine.Transmitter oscTransmitter;
    
    // Sync state
    public final BooleanParameter syncEnabled = new BooleanParameter("Sync", false)
        .setDescription("Enable network pattern synchronization");

    public final DiscreteParameter targetChannelParam = new DiscreteParameter("SyncCh", TARGET_CHANNEL + 1, 1, 65)
        .setDescription("Channel number (1-based) to synchronize across network");
    
    private boolean isMaster = false;
    private final Set<String> connectedPeers = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastSeenTime = new ConcurrentHashMap<>();
    private long lastHeartbeatTime = 0;
    private long lastSyncHeartbeatTime = 0;
    private boolean syncEnablePending = false;
    
    // Instance identification
    private final String instanceId;
    private final Random random = new Random();
    
    // Wi-Fi interface addresses
    private InetAddress wifiLocalAddress;
    private InetAddress wifiBroadcastAddress;
    
    // Fader automation
    private boolean isConnected = false;
    private LXChannel targetChannel = null;
    private final Map<LXChannel, Double> preSyncFaderValues = new HashMap<>();
    
    // Auto-cycle state
    private boolean preSyncAutoCycleEnabled = false;
    private boolean autoCycleStateSaved = false;
    private boolean isInTransition = false;
    private int pendingPatternIndex = -1;
    private final LXChannel.Listener channelListener = new LXChannel.AbstractListener() {
        @Override
        public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
            if (syncEnabled.isOn() && channel == targetChannel) {
                isInTransition = true;
                if (isMaster && isConnected) {
                    sendSyncTrigger();
                }
            }
        }
        
        @Override
        public void patternDidChange(LXChannel channel, LXPattern pattern) {
            if (syncEnabled.isOn() && channel == targetChannel) {
                isInTransition = false;
                if (!isMaster && isConnected) {
                    applyPendingPatternChange();
                }
            }
        }
    };
    
    private final LXOscListener oscListener = (OscMessage message) -> {
        String address = message.getAddressPattern().getValue();
        if (address.startsWith("/slstudio/sync/")) {
            handleSyncMessage(message);
        }
    };
    
    public NetworkSyncManager(LX lx) {
        super(lx, "NetworkSync");
        this.lx = lx;
        this.instanceId = generateInstanceId();
        
        // Initialize network components
        this.networkMonitor = NetworkMonitor.getInstance(lx);
        this.oscEngine = lx.engine.osc;
        
        try {
            initializeWifiInterface();
            
            this.discoverySocket = new DatagramSocket(DISCOVERY_PORT);
            this.discoverySocket.setBroadcast(true);
            this.oscReceiver = oscEngine.receiver(SYNC_OSC_PORT);
            this.oscReceiver.addListener(oscListener);
            this.oscTransmitter = oscEngine.transmitter(wifiBroadcastAddress, SYNC_OSC_PORT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize network for sync", e);
        }
        
        // Setup parameter listeners (order matters: target channel must load before sync enable)
        addParameter("targetChannelParam", this.targetChannelParam);
        addParameter("syncEnabled", this.syncEnabled);
        // Note: LXComponent.addParameter() automatically registers this as a listener
        
        System.out.println("NetworkSyncManager initialized with instance ID: " + instanceId);
    }
    
    private String generateInstanceId() {
        return "slstudio-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
    }
    
    private void initializeWifiInterface() {
        try {
            NetworkInterface wifiInterface = findWifiInterface();
            if (wifiInterface == null) {
                throw new RuntimeException("Wi-Fi interface not found");
            }
            
            for (InterfaceAddress addr : wifiInterface.getInterfaceAddresses()) {
                InetAddress address = addr.getAddress();
                if (address instanceof Inet4Address) {
                    wifiLocalAddress = address;
                    wifiBroadcastAddress = addr.getBroadcast();
                    break;
                }
            }
            
            if (wifiLocalAddress == null || wifiBroadcastAddress == null) {
                throw new RuntimeException("No IPv4 address/broadcast found on Wi-Fi interface");
            }
            
            System.out.println("🛜 SYNC WIFI: Using " + wifiInterface.getName() + " (" + wifiInterface.getDisplayName() + 
                ") at " + wifiLocalAddress.getHostAddress() + " broadcast " + wifiBroadcastAddress.getHostAddress());
        } catch (SocketException e) {
            throw new RuntimeException("Failed to initialize Wi-Fi interface", e);
        }
    }
    
    private NetworkInterface findWifiInterface() throws SocketException {
        // First try to find by display name (e.g., "Wi-Fi", "AirPort")
        for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            String displayName = iface.getDisplayName();
            if (displayName != null && (displayName.toLowerCase().contains("wi-fi") || 
                                        displayName.toLowerCase().contains("airport") ||
                                        displayName.toLowerCase().contains("wifi"))) {
                return iface;
            }
        }
        
        // Fallback to common device names
        for (String name : WIFI_INTERFACE_FALLBACKS) {
            NetworkInterface iface = NetworkInterface.getByName(name);
            if (iface != null && !iface.isLoopback()) {
                return iface;
            }
        }
        
        return null;
    }
    
    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == syncEnabled) {
            if (syncEnabled.isOn()) {
                enableSync();
            } else {
                disableSync();
            }
        } else if (p == targetChannelParam) {
            if (syncEnabled.isOn()) {
                System.out.println("🔄 CHANNEL CHANGE: Sync channel changed to " + targetChannelParam.getValuei() + ", re-syncing...");
                disableSync();
                enableSync();
            }
        }
    }
    
    private void enableSync() {
        // Get target channel first; if it's not available yet (e.g., during project load),
        // mark as pending and retry in update() once channels are fully restored.
        targetChannel = lx.engine.getChannel(targetChannelParam.getValuei() - 1);
        if (targetChannel == null) {
            syncEnablePending = true;
            System.err.println("⚠️  WARNING: Channel " + targetChannelParam.getValuei() + " not found for sync, retrying...");
            return;
        }
        syncEnablePending = false;
        
        System.out.println("🟢 SYNC ENABLED: Network synchronization activated");
        System.out.println("� INSTANCE ID: " + instanceId);
        isMaster = true;  // First instance assumes master role
        lastHeartbeatTime = System.currentTimeMillis();
        
        System.out.println("� TARGET: Channel " + targetChannelParam.getValuei() + ", Pattern " + TARGET_PATTERN_INDEX);
        System.out.println("👑 INITIAL ROLE: MASTER (assuming until we detect other instances)");
        
        attachChannelListener();
        saveAutoCycleState();
        applyAutoCycleForRole();
        
        // Start discovery
        startDiscovery();
    }
    
    private void attachChannelListener() {
        if (targetChannel != null) {
            targetChannel.addListener(channelListener);
        }
    }
    
    private void detachChannelListener() {
        if (targetChannel != null) {
            targetChannel.removeListener(channelListener);
        }
    }
    
    private void saveAutoCycleState() {
        if (targetChannel == null || autoCycleStateSaved) return;
        preSyncAutoCycleEnabled = targetChannel.autoCycleEnabled.isOn();
        autoCycleStateSaved = true;
    }
    
    private void applyAutoCycleForRole() {
        if (targetChannel == null) return;
        boolean desiredAutoCycle = isMaster;
        if (targetChannel.autoCycleEnabled.isOn() != desiredAutoCycle) {
            System.out.println("🔄 AUTO-CYCLE: Setting channel " + targetChannelParam.getValuei() + 
                " to " + desiredAutoCycle + " (" + (isMaster ? "MASTER" : "SLAVE") + ")");
            targetChannel.autoCycleEnabled.setValue(desiredAutoCycle);
        }
    }
    
    private void restoreAutoCycleState() {
        if (targetChannel == null || !autoCycleStateSaved) return;
        if (targetChannel.autoCycleEnabled.isOn() != preSyncAutoCycleEnabled) {
            System.out.println("🔄 RESTORE: Auto-cycle on channel " + targetChannelParam.getValuei() + 
                " restored to " + preSyncAutoCycleEnabled);
            targetChannel.autoCycleEnabled.setValue(preSyncAutoCycleEnabled);
        }
        autoCycleStateSaved = false;
    }
    
    private void disableSync() {
        syncEnablePending = false;
        System.out.println("🔴 SYNC DISABLED: Network synchronization deactivated");
        System.out.println("🔌 CLEANUP: Clearing all connections and stopping discovery");
        isMaster = false;
        connectedPeers.clear();
        lastSeenTime.clear();
        
        if (isConnected) {
            // Restore auto-cycle and other channels first, then fade target down
            restoreAutoCycleState();
            restoreOtherChannelFaderValues();
            if (targetChannel != null) {
                System.out.println("🎚️  FADER: Fading channel " + targetChannelParam.getValuei() + " down to 0.0 (sync disabled)");
                setChannelFader(0.0f, 1.0f);
            }
        }
        isConnected = false;
        detachChannelListener();
        isInTransition = false;
        pendingPatternIndex = -1;
    }
    
    private void startDiscovery() {
        // Send initial heartbeat immediately
        sendHeartbeat();
    }
    
    private void sendHeartbeat() {
        if (!syncEnabled.isOn() || discoverySocket == null) return;
        
        try {
            String discoveryMsg = String.format(
                "{\"instanceId\":\"%s\",\"isMaster\":%b,\"timestamp\":%d}",
                instanceId, isMaster, System.currentTimeMillis()
            );
            
            byte[] data = discoveryMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, 
                wifiBroadcastAddress, DISCOVERY_PORT);
            discoverySocket.send(packet);
            lastHeartbeatTime = System.currentTimeMillis();
            
            System.out.println("📡 DISCOVERY: Sent heartbeat as " + (isMaster ? "MASTER" : "SLAVE") + 
                " (ID: " + instanceId + ")");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to send discovery packet: " + e.getMessage());
        }
    }
    
    private void onDiscoveryReceived(String senderId, boolean senderIsMaster) {
        if (!syncEnabled.isOn()) return;
        
        System.out.println("🔍 DISCOVERY: Received packet from " + senderId + 
                          " (isMaster: " + senderIsMaster + ", we are: " + isMaster + ")");
        
        long now = System.currentTimeMillis();
        
        // Determine if this is a new/reconnected peer BEFORE updating lastSeenTime
        boolean wasConnected = isConnected;
        boolean wasNewPeer = !lastSeenTime.containsKey(senderId);
        
        lastSeenTime.put(senderId, now);
        
        // Master/slave election
        if (isMaster && senderIsMaster) {
            // Both think they're master - lower ID wins
            if (senderId.compareTo(instanceId) < 0) {
                // Other instance becomes master
                isMaster = false;
                System.out.println("🔄 ROLE CHANGE: Demoted to slave, master is: " + senderId);
                if (isConnected) {
                    applyAutoCycleForRole();
                }
            } else {
                System.out.println("👑 ROLE CONFLICT: We remain master (our ID: " + instanceId + 
                                  " vs their ID: " + senderId + ")");
            }
        }
        
        connectedPeers.add(senderId);
        
        if (wasNewPeer) {
            System.out.println("🤝 NEW PEER: Added " + senderId + " to connected peers");
        }
        
        if (!wasConnected && connectedPeers.size() > 0) {
            onNetworkConnected();
        }
        
        // If we're master and this is a new/reconnected slave, send initial sync
        if (isMaster && !senderIsMaster && wasNewPeer) {
            System.out.println("📤 SYNC TRIGGER: Sending initial sync to new slave " + senderId);
            sendInitialSync();
        }
    }
    
    private void onNetworkConnected() {
        System.out.println("🌐 NETWORK CONNECTED: Connected to " + connectedPeers.size() + " peers");
        System.out.println("📋 PEER LIST: " + connectedPeers);
        System.out.println("👑 OUR ROLE: " + (isMaster ? "MASTER" : "SLAVE"));
        isConnected = true;
        
        if (targetChannel != null) {
            System.out.println("🎨 PATTERN SYNC: Focusing channel " + targetChannelParam.getValuei());
            // Focus the target channel (master keeps its current pattern; slave will follow via initial sync)
            lx.engine.getFocusedLook().setFocusedChannel(targetChannel);
            
            // Save non-target channel fader values and fade them down
            saveOtherChannelFaderValues();
            fadeOtherChannelsDown();
            
            // Fade up target channel
            System.out.println("🎚️  FADER: Fading channel " + targetChannelParam.getValuei() + " up to 1.0");
            setChannelFader(1.0f, 1.0f);
            
            // Apply auto-cycle based on role (master ON, slave OFF)
            applyAutoCycleForRole();
            
            // If master, trigger initial sync
            if (isMaster) {
                System.out.println("📡 MASTER ACTION: Sending initial sync to all slaves");
                sendInitialSync();
            }
        } else {
            System.out.println("⚠️  WARNING: Target channel " + targetChannelParam.getValuei() + " not found!");
        }
    }
    
    private void onNetworkDisconnected() {
        System.out.println("🔌 NETWORK DISCONNECTED: Lost connection to all peers");
        System.out.println("👑 ROLE CHANGE: " + (!isMaster ? "Was slave, now promoted to MASTER" : "Was master, no longer connected"));
        connectedPeers.clear();
        
        if (isConnected) {
            // Apply auto-cycle for role and restore other channels first, then fade target down
            applyAutoCycleForRole();
            restoreOtherChannelFaderValues();
            if (targetChannel != null) {
                System.out.println("🎚️  FADER: Fading channel " + targetChannelParam.getValuei() + " down to 0.0");
                setChannelFader(0.0f, 1.0f);
            }
        }
        
        // If we were slave, try to become master
        if (!isMaster) {
            isMaster = true;
            System.out.println("🔄 PROMOTION: Promoted to master (no other peers detected)");
        }
        isConnected = false;
    }
    
    private void sendInitialSync() {
        if (!isMaster || targetChannel == null) return;
        
        try {
            int patternIndex = targetChannel.getActivePatternIndex();
            LXPattern pattern = targetChannel.getActivePattern();
            String syncMsg = String.format(
                "{\"channel\":%d,\"patternIndex\":%d,\"patternName\":\"%s\",\"faderValue\":1.0,\"fadeTime\":1.0,\"timestamp\":%d}",
                targetChannelParam.getValuei() - 1, patternIndex, pattern.getLabel(), System.currentTimeMillis()
            );
            
            OscMessage message = new OscMessage("/slstudio/sync/initial");
            // Add JSON as string argument
            message.add(syncMsg);
            oscTransmitter.send(message);
            
            System.out.println("📡 OSC SYNC: Sent initial sync - " + syncMsg);
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to send initial sync: " + e.getMessage());
        }
    }
    
    private void sendSyncTrigger() {
        if (!isMaster || targetChannel == null) return;
        
        try {
            int patternIndex = targetChannel.getNextPatternIndex();
            LXPattern pattern = targetChannel.getNextPattern();
            sendSyncMessage(patternIndex, pattern, false);
            
        } catch (Exception e) {
            System.err.println("Failed to send sync trigger: " + e.getMessage());
        }
    }
    
    private void sendSyncHeartbeat() {
        if (!isMaster || targetChannel == null) return;
        
        try {
            int patternIndex = targetChannel.getNextPatternIndex();
            LXPattern pattern = targetChannel.getNextPattern();
            sendSyncMessage(patternIndex, pattern, true);
            
        } catch (Exception e) {
            System.err.println("Failed to send sync heartbeat: " + e.getMessage());
        }
    }
    
    private void sendSyncMessage(int patternIndex, LXPattern pattern, boolean isHeartbeat) {
        String syncMsg = String.format(
            "{\"channel\":%d,\"patternIndex\":%d,\"patternName\":\"%s\",\"timestamp\":%d}",
            targetChannelParam.getValuei() - 1, patternIndex, pattern.getLabel(), System.currentTimeMillis()
        );
        
        OscMessage message = new OscMessage("/slstudio/sync/trigger");
        message.add(syncMsg);
        // Send multiple copies to guard against UDP packet loss
        try {
            for (int i = 0; i < 3; i++) {
                oscTransmitter.send(message);
            }
        } catch (IOException e) {
            System.err.println("Failed to send sync message: " + e.getMessage());
            return;
        }
        
        String label = isHeartbeat ? "HB" : "TX";
        System.out.println("📤 SYNC " + label + ": Trigger pattern index " + patternIndex + " (" + pattern.getLabel() + ")");
        lastSyncHeartbeatTime = System.currentTimeMillis();
    }
    
    private void handleSyncMessage(OscMessage message) {
        if (!syncEnabled.isOn() || isMaster) return;
        
        try {
            String jsonData = message.getString(0);
            int patternIndex = Integer.parseInt(extractJsonValue(jsonData, "patternIndex"));
            
            if (targetChannel != null) {
                int currentIndex = targetChannel.getActivePatternIndex();
                int nextIndex = targetChannel.getNextPatternIndex();
                System.out.println("📥 SYNC RX: Request pattern index " + patternIndex + ", current " + currentIndex + ", next " + nextIndex + ", inTransition " + isInTransition);
                if (patternIndex < 0 || patternIndex >= targetChannel.getPatterns().size()) {
                    System.err.println("⚠️  WARNING: Received invalid pattern index " + patternIndex);
                } else if (isInTransition) {
                    if (patternIndex != nextIndex && patternIndex != currentIndex) {
                        System.out.println("📥 SYNC RX: Queuing pattern index " + patternIndex + " until current transition finishes");
                        pendingPatternIndex = patternIndex;
                    }
                } else if (patternIndex != currentIndex) {
                    System.out.println("📥 SYNC RX: Switching to pattern index " + patternIndex);
                    targetChannel.goIndex(patternIndex);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to handle sync message: " + e.getMessage());
        }
    }
    
    private void applyPendingPatternChange() {
        if (pendingPatternIndex < 0 || targetChannel == null) return;
        int currentIndex = targetChannel.getActivePatternIndex();
        if (pendingPatternIndex != currentIndex) {
            System.out.println("🔄 PENDING: Applying queued pattern index " + pendingPatternIndex);
            targetChannel.goIndex(pendingPatternIndex);
        }
        pendingPatternIndex = -1;
    }
    
    private void setChannelFader(float value, float time) {
        if (targetChannel != null) {
            targetChannel.fader.setValue(value);
        }
    }
    
    private void saveOtherChannelFaderValues() {
        preSyncFaderValues.clear();
        for (LXChannel channel : lx.engine.getChannels()) {
            if (channel != targetChannel) {
                preSyncFaderValues.put(channel, channel.fader.getValue());
            }
        }
    }
    
    private void fadeOtherChannelsDown() {
        System.out.println("🎚️  FADER: Fading all non-sync channels down to 0.0");
        for (LXChannel channel : lx.engine.getChannels()) {
            if (channel != targetChannel) {
                channel.fader.setValue(0.0f);
            }
        }
    }
    
    private void restoreOtherChannelFaderValues() {
        if (preSyncFaderValues.isEmpty()) return;
        System.out.println("🎚️  FADER: Restoring non-sync channels to previous values");
        for (LXChannel channel : lx.engine.getChannels()) {
            if (channel != targetChannel) {
                Double value = preSyncFaderValues.get(channel);
                if (value != null) {
                    channel.fader.setValue(value);
                }
            }
        }
        preSyncFaderValues.clear();
    }
    
    /**
     * Called regularly to handle heartbeat and timeout logic
     */
    public void update(double deltaMs) {
        if (!syncEnabled.isOn()) return;
        
        // Retry enabling sync if it was requested while channels were still loading
        if (syncEnablePending && targetChannel == null) {
            enableSync();
        }
        
        long now = System.currentTimeMillis();
        
        // Listen for discovery packets
        listenForDiscoveryPackets();
        
        // Send heartbeat periodically
        if ((now - lastHeartbeatTime) > HEARTBEAT_INTERVAL_MS) {
            sendHeartbeat();
        }
        
        // Re-send current pattern to slave periodically so missed triggers can recover
        if (isMaster && isConnected && (now - lastSyncHeartbeatTime) > SYNC_HEARTBEAT_INTERVAL_MS) {
            sendSyncHeartbeat();
            lastSyncHeartbeatTime = now;
        }
        
        // Check for timeouts
        boolean wasConnected = isConnected;
        Set<String> timedOutPeers = new HashSet<>();
        lastSeenTime.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > CONNECTION_TIMEOUT_MS) {
                connectedPeers.remove(entry.getKey());
                timedOutPeers.add(entry.getKey());
                return true;
            }
            return false;
        });
        
        // Log timeout events
        for (String timedOutPeer : timedOutPeers) {
            System.out.println("⏰ TIMEOUT: Peer " + timedOutPeer + " timed out (last seen " + 
                              (now - lastSeenTime.getOrDefault(timedOutPeer, 0L)) + "ms ago)");
        }
        
        // Check if disconnected
        if (wasConnected && connectedPeers.isEmpty()) {
            System.out.println("🔌 ALL PEERS LOST: No more connected peers");
            onNetworkDisconnected();
        }
    }
    
    private void listenForDiscoveryPackets() {
        if (discoverySocket == null) return;
        
        try {
            discoverySocket.setSoTimeout(10); // Non-blocking with short timeout
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            discoverySocket.receive(packet);
            
            String message = new String(packet.getData(), 0, packet.getLength());
            if (message.contains("instanceId") && message.contains("isMaster")) {
                String instanceId = extractJsonValue(message, "instanceId");
                boolean isMaster = Boolean.parseBoolean(extractJsonValue(message, "isMaster"));
                
                // Don't process our own packets
                if (!instanceId.equals(this.instanceId)) {
                    onDiscoveryReceived(instanceId, isMaster);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            // Expected timeout for non-blocking behavior
        } catch (Exception e) {
            // Ignore other errors
        }
    }
    
    @Override
    public void dispose() {
        restoreAutoCycleState();
        detachChannelListener();
        disableSync();
        if (discoverySocket != null) {
            discoverySocket.close();
        }
        super.dispose();
    }
    
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            searchKey = "\"" + key + "\":";
            start = json.indexOf(searchKey);
            if (start == -1) return "";
            start += searchKey.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
