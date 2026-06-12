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
import heronarts.lx.osc.OscMessage;
import heronarts.lx.parameter.BooleanParameter;
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
    public static final int CONNECTION_TIMEOUT_MS = 10000;  // 10 seconds (more tolerant)
    
    private final LX lx;
    private final NetworkMonitor networkMonitor;
    private final LXOscEngine oscEngine;
    private DatagramSocket discoverySocket;
    private final LXOscEngine.Receiver oscReceiver;
    private final LXOscEngine.Transmitter oscTransmitter;
    
    // Sync state
    public final BooleanParameter syncEnabled = new BooleanParameter("Sync", false)
        .setDescription("Enable network pattern synchronization");
    
    private boolean isMaster = false;
    private long lastRoleChangeTime = 0;
    private static final long ROLE_STABILITY_MS = 3000; // Wait 3 seconds before role changes
    
    private final Set<String> connectedPeers = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastSeenTime = new ConcurrentHashMap<>();
    private long lastHeartbeatTime = 0;
    
    // Instance identification
    private final String instanceId;
    private final Random random = new Random();
    
    // Fader automation
    private boolean isConnected = false;
    private LXChannel targetChannel = null;
    
    public NetworkSyncManager(LX lx) {
        super(lx, "NetworkSync");
        this.lx = lx;
        this.instanceId = generateInstanceId();
        
        // Initialize network components
        this.networkMonitor = NetworkMonitor.getInstance(lx);
        this.oscEngine = lx.engine.osc;
        
        try {
            this.discoverySocket = new DatagramSocket(DISCOVERY_PORT);
            this.discoverySocket.setBroadcast(true);
            this.oscReceiver = oscEngine.receiver(SYNC_OSC_PORT);
            this.oscTransmitter = oscEngine.transmitter("255.255.255.255", SYNC_OSC_PORT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize network for sync", e);
        }
        
        // Setup parameter listeners
        addParameter("syncEnabled", this.syncEnabled);
        // Note: LXComponent.addParameter() automatically registers this as a listener
        
        System.out.println("NetworkSyncManager initialized with instance ID: " + instanceId);
    }
    
    private String generateInstanceId() {
        return "slstudio-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
    }
    
    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == syncEnabled) {
            if (syncEnabled.isOn()) {
                enableSync();
            } else {
                disableSync();
            }
        }
    }
    
    private void enableSync() {
        System.out.println("🟢 SYNC ENABLED: Network synchronization activated");
        System.out.println("🆔 INSTANCE ID: " + instanceId);
        isMaster = true;  // First instance assumes master role
        lastHeartbeatTime = System.currentTimeMillis();
        
        // Get target channel
        targetChannel = lx.engine.getChannel(TARGET_CHANNEL);
        if (targetChannel == null) {
            System.err.println("⚠️  WARNING: Channel " + TARGET_CHANNEL + " not found for sync");
            return;
        }
        
        System.out.println("🎯 TARGET: Channel " + TARGET_CHANNEL + ", Pattern " + TARGET_PATTERN_INDEX);
        System.out.println("👑 INITIAL ROLE: MASTER (assuming until we detect other instances)");
        
        // Start discovery
        startDiscovery();
    }
    
    private void disableSync() {
        System.out.println("🔴 SYNC DISABLED: Network synchronization deactivated");
        System.out.println("🔌 CLEANUP: Clearing all connections and stopping discovery");
        isMaster = false;
        connectedPeers.clear();
        lastSeenTime.clear();
        
        // Fade out channel if connected
        if (isConnected && targetChannel != null) {
            System.out.println("🎚️  FADER: Fading channel " + TARGET_CHANNEL + " down to 0.0 (sync disabled)");
            setChannelFader(0.0f, 1.0f);
        }
        isConnected = false;
    }
    
    private void startDiscovery() {
        if (isMaster) {
            // Send initial discovery immediately
            sendDiscoveryPacket();
        }
    }
    
    private void sendDiscoveryPacket() {
        if (!isMaster || !syncEnabled.isOn() || discoverySocket == null) return;
        
        try {
            String discoveryMsg = String.format(
                "{\"instanceId\":\"%s\",\"isMaster\":true,\"timestamp\":%d}",
                instanceId, System.currentTimeMillis()
            );
            
            byte[] data = discoveryMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, 
                InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            discoverySocket.send(packet);
            lastHeartbeatTime = System.currentTimeMillis();
            
            System.out.println("📡 DISCOVERY: Sent heartbeat as MASTER (ID: " + instanceId + ")");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to send discovery packet: " + e.getMessage());
        }
    }
    
    private void sendSlavePacket() {
        if (isMaster || !syncEnabled.isOn() || discoverySocket == null) return;
        
        try {
            String discoveryMsg = String.format(
                "{\"instanceId\":\"%s\",\"isMaster\":false,\"timestamp\":%d}",
                instanceId, System.currentTimeMillis()
            );
            
            byte[] data = discoveryMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, 
                InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            discoverySocket.send(packet);
            
            System.out.println("📡 DISCOVERY: Sent packet as SLAVE (ID: " + instanceId + ")");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to send slave packet: " + e.getMessage());
        }
    }
    
    private void onDiscoveryReceived(String senderId, boolean senderIsMaster) {
        if (!syncEnabled.isOn()) return;
        
        long now = System.currentTimeMillis();
        System.out.println("🔍 DISCOVERY: Received packet from " + senderId + 
                          " (isMaster: " + senderIsMaster + ", we are: " + isMaster + 
                          ", time: " + now + ")");
        
        lastSeenTime.put(senderId, now);
        
        // Master/slave election with stability check
        System.out.println("🏛️  ELECTION: Comparing IDs - ours: " + instanceId + " vs theirs: " + senderId);
        System.out.println("🏛️  ELECTION: Comparison result: " + senderId.compareTo(instanceId));
        
        long timeSinceRoleChange = now - lastRoleChangeTime;
        boolean canChangeRole = timeSinceRoleChange > ROLE_STABILITY_MS;
        
        if (isMaster && senderIsMaster) {
            // Both think they're master - lower ID wins
            if (senderId.compareTo(instanceId) < 0 && canChangeRole) {
                // Other instance becomes master, we become slave
                isMaster = false;
                lastRoleChangeTime = now;
                System.out.println("🔄 ROLE CHANGE: Demoted to slave, master is: " + senderId);
            } else {
                System.out.println("👑 ROLE CONFLICT: We remain master (our ID: " + instanceId + 
                                  " vs their ID: " + senderId + ")" + 
                                  (canChangeRole ? "" : " [stability lock]"));
            }
        } else if (!isMaster && !senderIsMaster) {
            // Both are slaves - one should become master
            if (instanceId.compareTo(senderId) < 0 && canChangeRole) {
                // We become master
                isMaster = true;
                lastRoleChangeTime = now;
                System.out.println("🔄 ROLE CHANGE: Promoted to master (we have lower ID: " + instanceId + 
                                  " vs " + senderId + ")");
            }
        } else if (!isMaster && senderIsMaster) {
            // We are slave, they are master - this is correct
            System.out.println("✅ SLAVE MODE: We are slave, they are master (" + senderId + ")");
        } else if (isMaster && !senderIsMaster) {
            // We are master, they are slave - this is correct
            System.out.println("✅ MASTER MODE: We are master, they are slave (" + senderId + ")");
        }
        
        // Check if this is a new connection
        boolean wasConnected = isConnected;
        boolean wasNewPeer = !connectedPeers.contains(senderId);
        connectedPeers.add(senderId);
        
        if (wasNewPeer) {
            System.out.println("🤝 NEW PEER: Added " + senderId + " to connected peers");
        }
        
        if (!wasConnected && connectedPeers.size() > 0) {
            onNetworkConnected();
        }
        
        // If we're master and this is a new slave, send initial sync
        if (isMaster && !senderIsMaster) {
            if (wasNewPeer) {
                System.out.println("📤 SYNC TRIGGER: Sending initial sync to new slave " + senderId);
                sendInitialSync();
            }
        }
    }
    
    private void onNetworkConnected() {
        System.out.println("🌐 NETWORK CONNECTED: Connected to " + connectedPeers.size() + " peers");
        System.out.println("📋 PEER LIST: " + connectedPeers);
        System.out.println("👑 OUR ROLE: " + (isMaster ? "MASTER" : "SLAVE"));
        isConnected = true;
        
        if (targetChannel != null) {
            System.out.println("🎨 PATTERN SYNC: Switching to channel " + TARGET_CHANNEL + 
                              ", pattern " + TARGET_PATTERN_INDEX);
            // Switch to target channel and pattern
            lx.engine.getFocusedLook().setFocusedChannel(targetChannel);
            targetChannel.goPattern(targetChannel.getPattern(TARGET_PATTERN_INDEX));
            
            // Fade up
            System.out.println("🎚️  FADER: Fading channel " + TARGET_CHANNEL + " up to 1.0");
            setChannelFader(1.0f, 1.0f);
            
            // If master, trigger initial sync
            if (isMaster) {
                System.out.println("📡 MASTER ACTION: Sending initial sync to all slaves");
                sendInitialSync();
            }
        } else {
            System.out.println("⚠️  WARNING: Target channel " + TARGET_CHANNEL + " not found!");
        }
    }
    
    private void onNetworkDisconnected() {
        System.out.println("🔌 NETWORK DISCONNECTED: Lost connection to all peers");
        System.out.println("👑 ROLE CHANGE: " + (!isMaster ? "Was slave, now promoted to MASTER" : "Was master, no longer connected"));
        isConnected = false;
        connectedPeers.clear();
        
        // If we were slave, try to become master
        if (!isMaster) {
            isMaster = true;
            System.out.println("🔄 PROMOTION: Promoted to master (no other peers detected)");
        }
        
        // Fade out channel
        if (targetChannel != null) {
            System.out.println("🎚️  FADER: Fading channel " + TARGET_CHANNEL + " down to 0.0");
            setChannelFader(0.0f, 1.0f);
        }
    }
    
    private void sendInitialSync() {
        if (!isMaster || targetChannel == null) return;
        
        try {
            LXPattern pattern = targetChannel.getPattern(TARGET_PATTERN_INDEX);
            String syncMsg = String.format(
                "{\"channel\":%d,\"patternIndex\":%d,\"patternName\":\"%s\",\"faderValue\":1.0,\"fadeTime\":1.0,\"timestamp\":%d}",
                TARGET_CHANNEL, TARGET_PATTERN_INDEX, pattern.getLabel(), System.currentTimeMillis()
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
            LXPattern pattern = targetChannel.getFocusedPattern();
            String syncMsg = String.format(
                "{\"channel\":%d,\"patternIndex\":%d,\"patternName\":\"%s\",\"timestamp\":%d}",
                TARGET_CHANNEL, TARGET_PATTERN_INDEX, pattern.getLabel(), System.currentTimeMillis()
            );
            
            OscMessage message = new OscMessage("/slstudio/sync/trigger");
            message.add(syncMsg);
            oscTransmitter.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send sync trigger: " + e.getMessage());
        }
    }
    
    private void handleSyncMessage(OscMessage message) {
        if (!syncEnabled.isOn()) return;
        
        try {
            String jsonData = message.getString(0);
            // Parse JSON and handle sync
            // For now, just switch to target pattern
            if (targetChannel != null) {
                targetChannel.goPattern(targetChannel.getPattern(TARGET_PATTERN_INDEX));
                setChannelFader(1.0f, 0.5f);  // Quick fade up
            }
            
        } catch (Exception e) {
            System.err.println("Failed to handle sync message: " + e.getMessage());
        }
    }
    
    private void setChannelFader(float value, float time) {
        if (targetChannel != null) {
            targetChannel.fader.setValue(value);
        }
    }
    
    /**
     * Called regularly to handle heartbeat and timeout logic
     */
    public void update(double deltaMs) {
        if (!syncEnabled.isOn()) return;
        
        long now = System.currentTimeMillis();
        
        // Listen for discovery packets
        listenForDiscoveryPackets();
        
        // Send heartbeat packets with redundancy for reliability
        if ((now - lastHeartbeatTime) > HEARTBEAT_INTERVAL_MS) {
            if (isMaster) {
                // Send 3 packets for redundancy (UDP can lose packets)
                sendDiscoveryPacket();
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                sendDiscoveryPacket();
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                sendDiscoveryPacket();
            } else {
                // Send 3 packets for redundancy
                sendSlavePacket();
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                sendSlavePacket();
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                sendSlavePacket();
            }
            lastHeartbeatTime = now;
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
        
        // Log timeout events with more detail
        for (String timedOutPeer : timedOutPeers) {
            Long lastSeen = lastSeenTime.get(timedOutPeer);
            long timeSince = lastSeen != null ? (now - lastSeen) : -1;
            System.out.println("⏰ TIMEOUT: Peer " + timedOutPeer + " timed out (last seen " + 
                              timeSince + "ms ago, threshold=" + CONNECTION_TIMEOUT_MS + "ms)");
            
            // Try to recover connection by sending extra discovery packets
            if (isMaster) {
                try {
                    System.out.println("🔄 RECOVERY: Sending extra discovery packet for timeout recovery");
                    sendDiscoveryPacket();
                } catch (Exception e) {
                    System.err.println("❌ ERROR: Failed to send recovery packet: " + e.getMessage());
                }
            }
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
    
    /**
     * Called when pattern changes on the target channel
     */
    public void onPatternChanged() {
        if (syncEnabled.isOn() && isMaster && isConnected) {
            sendSyncTrigger();
        }
    }
    
    @Override
    public void dispose() {
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
