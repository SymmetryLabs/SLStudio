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
import heronarts.lx.osc.OscArgument;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.osc.OscTypeTag;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameter.Units;

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
    public static final int TARGET_CHANNEL = 14;  // Legacy default channel (0-based, so 14 = 15th channel)
    public static final int TARGET_PATTERN_INDEX = 0;  // First pattern
    public static final int SYNC_CHANNEL_COUNT = 8;  // Number of sync-able channels
    public static final int HEARTBEAT_INTERVAL_MS = 2000;  // 2 seconds
    public static final int SYNC_HEARTBEAT_INTERVAL_MS = 2000;  // Re-send current pattern every 2 seconds
    public static final int CONNECTION_TIMEOUT_MS = 10000;  // 10 seconds (5x heartbeat for robustness)
    
    private final LX lx;
    private final NetworkMonitor networkMonitor;
    private final LXOscEngine oscEngine;
    private DatagramSocket discoverySocket;
    private final LXOscEngine.Receiver oscReceiver;
    private final List<LXOscEngine.Transmitter> oscTransmitters = new ArrayList<>();
    
    // Sync state
    public final BooleanParameter syncEnabled = new BooleanParameter("Sync", false)
        .setDescription("Enable network pattern synchronization");

    public final BooleanParameter[] syncChannelEnabled = new BooleanParameter[SYNC_CHANNEL_COUNT];
    public final DiscreteParameter[] syncChannelParam = new DiscreteParameter[SYNC_CHANNEL_COUNT];
    
    // Legacy migration parameter: old projects saved this as the single sync channel.
    public final DiscreteParameter targetChannelParam = new DiscreteParameter("targetChannelParam", TARGET_CHANNEL + 1, 1, 65);
    private final BooleanParameter legacyMigrated = new BooleanParameter("legacyMigrated", false)
        .setDescription("Legacy channel migration has been applied");

    private final Set<String> connectedPeers = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastSeenTime = new ConcurrentHashMap<>();
    private long lastHeartbeatTime = 0;
    private long lastSyncHeartbeatTime = 0;
    private boolean syncEnablePending = false;
    private boolean isMaster = false;
    private boolean isConnected = false;
    private boolean applyingRemoteSyncEnable = false;
    
    // Instance identification
    private final String instanceId;
    private final Random random = new Random();
    
    // Broadcast addresses across all active, non-loopback IPv4 interfaces (not just Wi-Fi)
    private final List<InetAddress> broadcastAddresses = new ArrayList<>();
    
    // Per-channel sync state
    private final List<LXChannel> activeSyncChannels = new ArrayList<>();
    private final Map<LXChannel, Double> preSyncFaderValues = new HashMap<>();
    private final Map<LXChannel, Boolean> preSyncAutoCycleEnabled = new HashMap<>();
    private final Map<LXChannel, Boolean> channelInTransition = new HashMap<>();
    private final Map<LXChannel, Integer> channelPendingPattern = new HashMap<>();
    
    private final LXChannel.Listener channelListener = new LXChannel.AbstractListener() {
        @Override
        public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
            if (syncEnabled.isOn() && activeSyncChannels.contains(channel)) {
                channelInTransition.put(channel, true);
                if (isMaster && isConnected) {
                    sendSyncTrigger(channel);
                }
            }
        }
        
        @Override
        public void patternDidChange(LXChannel channel, LXPattern pattern) {
            if (syncEnabled.isOn() && activeSyncChannels.contains(channel)) {
                channelInTransition.put(channel, false);
                if (!isMaster && isConnected) {
                    applyPendingPatternChange(channel);
                }
            }
        }
    };
    
    private final LXOscListener oscListener = (OscMessage message) -> {
        String address = message.getAddressPattern().getValue();
        if (address.equals("/slstudio/sync/enable")) {
            handleSyncEnableMessage(message);
        } else if (address.startsWith("/slstudio/sync/")) {
            handleSyncMessage(message);
        }
    };
    
    private final LXOscListener engineOscListener = (OscMessage message) -> {
        String address = message.getAddressPattern().getValue();
        if (address.equals("/slstudio/sync/enable")) {
            handleSyncEnableMessage(message);
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
            initializeNetworkInterfaces();
            
            this.discoverySocket = new DatagramSocket(DISCOVERY_PORT);
            this.discoverySocket.setBroadcast(true);
            this.oscReceiver = oscEngine.receiver(SYNC_OSC_PORT);
            this.oscReceiver.addListener(oscListener);
            for (InetAddress broadcastAddress : broadcastAddresses) {
                this.oscTransmitters.add(oscEngine.transmitter(broadcastAddress, SYNC_OSC_PORT));
            }
            this.oscEngine.addEngineListener(this.engineOscListener);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize network for sync", e);
        }
        
        // Initialize per-channel sync parameters (channel 1 first, then empty slots)
        for (int i = 0; i < SYNC_CHANNEL_COUNT; i++) {
            this.syncChannelEnabled[i] = new BooleanParameter("SyncCh" + (i + 1) + "En", i == 0)
                .setDescription("Enable synchronization for channel slot " + (i + 1));
            this.syncChannelParam[i] = new DiscreteParameter("SyncCh" + (i + 1), i == 0 ? TARGET_CHANNEL + 1 : 1, 1, 65)
                .setDescription("Channel number (1-based) for sync slot " + (i + 1));
            this.syncChannelParam[i].setUnits(Units.INTEGER);
        }
        
        // Legacy migration formatting
        this.targetChannelParam.setDescription("Legacy sync channel (migrated to syncChannelParam0)");
        this.targetChannelParam.setUnits(Units.INTEGER);
        
        // Setup parameter listeners (order matters: channel params must load before sync enable)
        for (int i = 0; i < SYNC_CHANNEL_COUNT; i++) {
            addParameter("syncChannelEnabled" + i, this.syncChannelEnabled[i]);
            addParameter("syncChannelParam" + i, this.syncChannelParam[i]);
        }
        // Legacy migration: load after new channel params so we can migrate only when slot 0 is still default
        addParameter("legacyMigrated", this.legacyMigrated);
        addParameter("targetChannelParam", this.targetChannelParam);
        addParameter("syncEnabled", this.syncEnabled);
        // Note: LXComponent.addParameter() automatically registers this as a listener
        
        System.out.println("NetworkSyncManager initialized with instance ID: " + instanceId);
    }
    
    private String generateInstanceId() {
        return "slstudio-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
    }
    
    /**
     * Discover broadcast addresses on every active, non-loopback IPv4 interface.
     * Previously we guessed a single "Wi-Fi" interface by name, which silently
     * fails (both instances never discover each other, both stay MASTER) if the
     * heuristic picks the wrong adapter (e.g. Ethernet, USB adapter, VPN, etc.)
     * or if the box has multiple interfaces. Broadcasting on all of them removes
     * that guesswork.
     */
    private void initializeNetworkInterfaces() {
        try {
            boolean foundAny = false;
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual() || iface.isPointToPoint()) {
                    continue;
                }
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    InetAddress address = addr.getAddress();
                    InetAddress broadcast = addr.getBroadcast();
                    if (address instanceof Inet4Address && broadcast != null) {
                        foundAny = true;
                        broadcastAddresses.add(broadcast);
                        System.out.println("🛜 SYNC IFACE: Using " + iface.getName() + " (" + iface.getDisplayName() +
                            ") at " + address.getHostAddress() + " broadcast " + broadcast.getHostAddress());
                    }
                }
            }
            
            if (!foundAny) {
                throw new RuntimeException("No active IPv4 network interfaces with a broadcast address were found");
            }
            
            System.out.println("🛜 SYNC IFACE: Broadcasting sync/discovery on " + broadcastAddresses.size() + " interface(s). " +
                "If two instances never discover each other, verify they show overlapping subnets above, " +
                "and check for firewalls/AP client isolation blocking UDP ports " + DISCOVERY_PORT + " and " + SYNC_OSC_PORT + ".");
        } catch (SocketException e) {
            throw new RuntimeException("Failed to enumerate network interfaces", e);
        }
    }
    
    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == syncEnabled) {
            boolean enabled = syncEnabled.isOn();
            if (!applyingRemoteSyncEnable) {
                // Broadcast immediately so a press on either machine turns sync on/off on both,
                // regardless of current master/connected state (which aren't established yet
                // the very first time the button is pressed).
                sendSyncEnable(enabled);
            }
            sendEngineSyncEnable(enabled);
            if (enabled) {
                enableSync();
            } else {
                disableSync();
            }
        } else if (p == targetChannelParam && !legacyMigrated.isOn()) {
            // Migrate old project files to the new slot-0 format
            int legacyValue = targetChannelParam.getValuei();
            if (legacyValue != TARGET_CHANNEL + 1 && syncChannelParam[0].getValuei() == TARGET_CHANNEL + 1) {
                System.out.println("🔄 MIGRATION: Legacy sync channel " + legacyValue + " -> slot 0");
                syncChannelParam[0].setValue(legacyValue);
                syncChannelEnabled[0].setValue(true);
            }
            legacyMigrated.setValue(true);
        } else {
            // Any channel enable or channel select change while sync is active => re-sync
            for (int i = 0; i < SYNC_CHANNEL_COUNT; i++) {
                if (p == syncChannelEnabled[i] || p == syncChannelParam[i]) {
                    if (syncEnabled.isOn()) {
                        System.out.println("🔄 CHANNEL CHANGE: Sync slot " + (i + 1) + " changed, re-syncing...");
                        disableSync();
                        enableSync();
                    }
                    return;
                }
            }
        }
    }
    
    private void enableSync() {
        // Build the list of active sync channels. Skip duplicates; first enabled occurrence wins.
        activeSyncChannels.clear();
        Set<Integer> seenChannels = new HashSet<>();
        for (int i = 0; i < SYNC_CHANNEL_COUNT; i++) {
            if (syncChannelEnabled[i].isOn()) {
                int channelNumber = syncChannelParam[i].getValuei();
                int channelIndex = channelNumber - 1;
                if (seenChannels.add(channelIndex)) {
                    LXChannel channel = lx.engine.getChannel(channelIndex);
                    if (channel != null) {
                        activeSyncChannels.add(channel);
                    } else {
                        System.err.println("⚠️  WARNING: Channel " + channelNumber + " not found for sync, retrying...");
                        syncEnablePending = true;
                        activeSyncChannels.clear();
                        return;
                    }
                }
            }
        }
        if (activeSyncChannels.isEmpty()) {
            System.out.println("🟡 SYNC ENABLED: No channels selected for synchronization");
            return;
        }
        syncEnablePending = false;
        
        System.out.println("🟢 SYNC ENABLED: Network synchronization activated");
        System.out.println("🆔 INSTANCE ID: " + instanceId);
        isMaster = true;  // First instance assumes master role
        lastHeartbeatTime = System.currentTimeMillis();
        
        StringBuilder channelList = new StringBuilder();
        for (int i = 0; i < activeSyncChannels.size(); i++) {
            if (i > 0) channelList.append(", ");
            channelList.append(activeSyncChannels.get(i).getIndex() + 1);
        }
        System.out.println("🎯 TARGETS: Channels [" + channelList + "]");
        System.out.println("👑 INITIAL ROLE: MASTER (assuming until we detect other instances)");
        
        attachChannelListeners();
        saveAutoCycleState();
        applyAutoCycleForRole();
        
        // Start discovery
        startDiscovery();
    }
    
    private void attachChannelListeners() {
        for (LXChannel channel : activeSyncChannels) {
            channel.addListener(channelListener);
        }
    }
    
    private void detachChannelListeners() {
        for (LXChannel channel : activeSyncChannels) {
            channel.removeListener(channelListener);
        }
    }
    
    private void saveAutoCycleState() {
        for (LXChannel channel : activeSyncChannels) {
            if (!preSyncAutoCycleEnabled.containsKey(channel)) {
                preSyncAutoCycleEnabled.put(channel, channel.autoCycleEnabled.isOn());
            }
        }
    }
    
    private void applyAutoCycleForRole() {
        boolean desiredAutoCycle = isMaster;
        for (LXChannel channel : activeSyncChannels) {
            if (channel.autoCycleEnabled.isOn() != desiredAutoCycle) {
                System.out.println("🔄 AUTO-CYCLE: Setting channel " + (channel.getIndex() + 1) + 
                    " to " + desiredAutoCycle + " (" + (isMaster ? "MASTER" : "SLAVE") + ")");
                channel.autoCycleEnabled.setValue(desiredAutoCycle);
            }
        }
    }
    
    private void restoreAutoCycleState() {
        for (Map.Entry<LXChannel, Boolean> entry : preSyncAutoCycleEnabled.entrySet()) {
            LXChannel channel = entry.getKey();
            boolean desired = entry.getValue();
            if (channel.autoCycleEnabled.isOn() != desired) {
                System.out.println("🔄 RESTORE: Auto-cycle on channel " + (channel.getIndex() + 1) + 
                    " restored to " + desired);
                channel.autoCycleEnabled.setValue(desired);
            }
        }
        preSyncAutoCycleEnabled.clear();
    }
    
    private void disableSync() {
        syncEnablePending = false;
        System.out.println("🔴 SYNC DISABLED: Network synchronization deactivated");
        System.out.println("🔌 CLEANUP: Clearing all connections and stopping discovery");
        isMaster = false;
        connectedPeers.clear();
        lastSeenTime.clear();
        
        if (isConnected) {
            // Restore auto-cycle and other channels first, then fade synced channels down
            restoreAutoCycleState();
            restoreOtherChannelFaderValues();
            for (LXChannel channel : activeSyncChannels) {
                System.out.println("🎚️  FADER: Fading channel " + (channel.getIndex() + 1) + " down to 0.0 (sync disabled)");
                setChannelFader(channel, 0.0f, 1.0f);
            }
        }
        isConnected = false;
        detachChannelListeners();
        activeSyncChannels.clear();
        channelInTransition.clear();
        channelPendingPattern.clear();
        preSyncAutoCycleEnabled.clear();
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
            for (InetAddress broadcastAddress : broadcastAddresses) {
                DatagramPacket packet = new DatagramPacket(data, data.length, 
                    broadcastAddress, DISCOVERY_PORT);
                discoverySocket.send(packet);
            }
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
        
        if (!activeSyncChannels.isEmpty()) {
            // Focus the first active sync channel
            LXChannel focusChannel = activeSyncChannels.get(0);
            System.out.println("🎨 PATTERN SYNC: Focusing channel " + (focusChannel.getIndex() + 1));
            lx.engine.getFocusedLook().setFocusedChannel(focusChannel);
            
            // Save non-synced channel fader values and fade them down
            saveOtherChannelFaderValues();
            fadeOtherChannelsDown();
            
            // Fade up all synced channels
            for (LXChannel channel : activeSyncChannels) {
                System.out.println("🎚️  FADER: Fading channel " + (channel.getIndex() + 1) + " up to 1.0");
                setChannelFader(channel, 1.0f, 1.0f);
            }
            
            // Apply auto-cycle based on role (master ON, slave OFF)
            applyAutoCycleForRole();
            
            // If master, trigger initial sync
            if (isMaster) {
                System.out.println("📡 MASTER ACTION: Sending initial sync to all slaves");
                sendInitialSync();
            }
        } else {
            System.out.println("⚠️  WARNING: No sync channels active!");
        }
    }
    
    private void onNetworkDisconnected() {
        System.out.println("🔌 NETWORK DISCONNECTED: Lost connection to all peers");
        System.out.println("👑 ROLE CHANGE: " + (!isMaster ? "Was slave, now promoted to MASTER" : "Was master, no longer connected"));
        connectedPeers.clear();
        
        if (isConnected) {
            // Apply auto-cycle for role and restore other channels first, then fade synced channels down
            applyAutoCycleForRole();
            restoreOtherChannelFaderValues();
            for (LXChannel channel : activeSyncChannels) {
                System.out.println("🎚️  FADER: Fading channel " + (channel.getIndex() + 1) + " down to 0.0");
                setChannelFader(channel, 0.0f, 1.0f);
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
        if (!isMaster || activeSyncChannels.isEmpty()) return;
        
        try {
            for (LXChannel channel : activeSyncChannels) {
                int patternIndex = channel.getActivePatternIndex();
                LXPattern pattern = channel.getActivePattern();
                String syncMsg = String.format(
                    "{\"channel\":%d,\"patternIndex\":%d,\"patternName\":\"%s\",\"faderValue\":1.0,\"fadeTime\":1.0,\"timestamp\":%d}",
                    channel.getIndex(), patternIndex, pattern.getLabel(), System.currentTimeMillis()
                );
                
                OscMessage message = new OscMessage("/slstudio/sync/initial");
                message.add(syncMsg);
                for (LXOscEngine.Transmitter transmitter : oscTransmitters) {
                    transmitter.send(message);
                }
                
                System.out.println("📡 OSC SYNC: Sent initial sync - channel " + (channel.getIndex() + 1) + " - " + syncMsg);
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to send initial sync: " + e.getMessage());
        }
    }
    
    private void sendSyncTrigger(LXChannel channel) {
        if (!isMaster || channel == null || !activeSyncChannels.contains(channel)) return;
        
        try {
            int patternIndex = channel.getNextPatternIndex();
            LXPattern pattern = channel.getNextPattern();
            sendSyncMessage(channel, patternIndex, pattern, false);
            
        } catch (Exception e) {
            System.err.println("Failed to send sync trigger: " + e.getMessage());
        }
    }
    
    private void sendSyncHeartbeat() {
        if (!isMaster || activeSyncChannels.isEmpty()) return;
        
        try {
            for (LXChannel channel : activeSyncChannels) {
                int patternIndex = channel.getNextPatternIndex();
                LXPattern pattern = channel.getNextPattern();
                sendSyncMessage(channel, patternIndex, pattern, true);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to send sync heartbeat: " + e.getMessage());
        }
    }
    
    private void sendSyncMessage(LXChannel channel, int patternIndex, LXPattern pattern, boolean isHeartbeat) {
        String syncMsg = String.format(
            "{\"channel\":%d,\"patternIndex\":%d,\"patternName\":\"%s\",\"timestamp\":%d}",
            channel.getIndex(), patternIndex, pattern.getLabel(), System.currentTimeMillis()
        );
        
        OscMessage message = new OscMessage("/slstudio/sync/trigger");
        message.add(syncMsg);
        // Send multiple copies to guard against UDP packet loss
        try {
            for (int i = 0; i < 3; i++) {
                for (LXOscEngine.Transmitter transmitter : oscTransmitters) {
                    transmitter.send(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to send sync message: " + e.getMessage());
            return;
        }
        
        String label = isHeartbeat ? "HB" : "TX";
        System.out.println("📤 SYNC " + label + ": Channel " + (channel.getIndex() + 1) + " pattern index " + patternIndex + " (" + pattern.getLabel() + ")");
        lastSyncHeartbeatTime = System.currentTimeMillis();
    }
    
    private void handleSyncEnableMessage(OscMessage message) {
        System.out.println("📥 SYNC ENABLE RX: Received message: " + message);
        try {
            boolean enabled;
            OscArgument arg;
            try {
                arg = message.get(0);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("⚠️  SYNC ENABLE RX: No arguments provided, expected 0/1 or false/true");
                return;
            }
            char typeTag = arg.getTypeTag();
            switch (typeTag) {
                case OscTypeTag.INT:
                case OscTypeTag.CHAR:
                case OscTypeTag.RGBA:
                case OscTypeTag.MIDI:
                    enabled = arg.toInt() != 0;
                    break;
                case OscTypeTag.FLOAT:
                case OscTypeTag.DOUBLE:
                    enabled = arg.toFloat() != 0;
                    break;
                case OscTypeTag.STRING:
                case OscTypeTag.SYMBOL:
                    String value = arg.toString().trim().toLowerCase();
                    enabled = value.equals("1") || value.equals("true") || value.equals("on");
                    break;
                case OscTypeTag.TRUE:
                    enabled = true;
                    break;
                case OscTypeTag.FALSE:
                default:
                    enabled = false;
                    break;
            }
            System.out.println("📥 SYNC ENABLE RX: Parsed value " + enabled + " (typeTag: " + typeTag + ")");
            if (syncEnabled.isOn() != enabled) {
                System.out.println("📥 SYNC ENABLE RX: Setting syncEnabled to " + (enabled ? "ON" : "OFF"));
                applyingRemoteSyncEnable = true;
                try {
                    syncEnabled.setValue(enabled);
                } finally {
                    applyingRemoteSyncEnable = false;
                }
            } else {
                System.out.println("📥 SYNC ENABLE RX: Already " + (enabled ? "ON" : "OFF") + ", no change");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to handle sync enable message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendSyncEnable(boolean enabled) {
        if (oscTransmitters.isEmpty()) return;
        try {
            OscMessage message = new OscMessage("/slstudio/sync/enable");
            message.add(enabled ? 1 : 0);
            for (LXOscEngine.Transmitter transmitter : oscTransmitters) {
                transmitter.send(message);
            }
            System.out.println("📤 SYNC ENABLE TX: " + (enabled ? "ON" : "OFF"));
        } catch (IOException e) {
            System.err.println("Failed to send sync enable message: " + e.getMessage());
        }
    }
    
    private void sendEngineSyncEnable(boolean enabled) {
        if (this.oscEngine == null || !this.oscEngine.transmitActive.isOn()) return;
        try {
            OscMessage message = new OscMessage("/slstudio/sync/enable");
            message.add(enabled ? 1 : 0);
            this.oscEngine.enqueueMessage(message);
            System.out.println("📤 ENGINE SYNC ENABLE TX: " + (enabled ? "ON" : "OFF"));
        } catch (Exception e) {
            System.err.println("Failed to send engine sync enable message: " + e.getMessage());
        }
    }
    
    private void handleSyncMessage(OscMessage message) {
        if (!syncEnabled.isOn() || isMaster) return;
        
        try {
            String jsonData = message.getString(0);
            int channelIndex = Integer.parseInt(extractJsonValue(jsonData, "channel"));
            int patternIndex = Integer.parseInt(extractJsonValue(jsonData, "patternIndex"));
            LXChannel channel = lx.engine.getChannel(channelIndex);
            
            if (channel != null && activeSyncChannels.contains(channel)) {
                int currentIndex = channel.getActivePatternIndex();
                int nextIndex = channel.getNextPatternIndex();
                boolean inTransition = channelInTransition.getOrDefault(channel, false);
                System.out.println("📥 SYNC RX: Channel " + (channelIndex + 1) + " request pattern index " + patternIndex + ", current " + currentIndex + ", next " + nextIndex + ", inTransition " + inTransition);
                if (patternIndex < 0 || patternIndex >= channel.getPatterns().size()) {
                    System.err.println("⚠️  WARNING: Received invalid pattern index " + patternIndex);
                } else if (inTransition) {
                    if (patternIndex != nextIndex && patternIndex != currentIndex) {
                        System.out.println("📥 SYNC RX: Queuing channel " + (channelIndex + 1) + " pattern index " + patternIndex + " until current transition finishes");
                        channelPendingPattern.put(channel, patternIndex);
                    }
                } else if (patternIndex != currentIndex) {
                    System.out.println("📥 SYNC RX: Switching channel " + (channelIndex + 1) + " to pattern index " + patternIndex);
                    channel.goIndex(patternIndex);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Failed to handle sync message: " + e.getMessage());
        }
    }
    
    private void applyPendingPatternChange(LXChannel channel) {
        Integer pending = channelPendingPattern.get(channel);
        if (pending == null || pending < 0 || channel == null) return;
        int currentIndex = channel.getActivePatternIndex();
        if (pending != currentIndex) {
            System.out.println("🔄 PENDING: Applying channel " + (channel.getIndex() + 1) + " queued pattern index " + pending);
            channel.goIndex(pending);
        }
        channelPendingPattern.remove(channel);
    }
    
    private void setChannelFader(LXChannel channel, float value, float time) {
        if (channel != null) {
            channel.fader.setValue(value);
        }
    }
    
    private void saveOtherChannelFaderValues() {
        preSyncFaderValues.clear();
        for (LXChannel channel : lx.engine.getChannels()) {
            if (!activeSyncChannels.contains(channel)) {
                preSyncFaderValues.put(channel, channel.fader.getValue());
            }
        }
    }
    
    private void fadeOtherChannelsDown() {
        System.out.println("🎚️  FADER: Fading all non-sync channels down to 0.0");
        for (LXChannel channel : lx.engine.getChannels()) {
            if (!activeSyncChannels.contains(channel)) {
                channel.fader.setValue(0.0f);
            }
        }
    }
    
    private void restoreOtherChannelFaderValues() {
        if (preSyncFaderValues.isEmpty()) return;
        System.out.println("🎚️  FADER: Restoring non-sync channels to previous values");
        for (LXChannel channel : lx.engine.getChannels()) {
            if (!activeSyncChannels.contains(channel)) {
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
        if (syncEnablePending && activeSyncChannels.isEmpty()) {
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
            // Drain every packet currently queued, not just one. We broadcast heartbeats
            // on every discovered interface, which causes the OS to loop several
            // self-echoed copies back to our own listening socket per heartbeat. Reading
            // only one packet per update() tick let a backlog of self-echoes starve out
            // (or risk overflowing the OS buffer and dropping) the peer's real packets.
            for (int i = 0; i < 32; i++) {
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
            }
        } catch (java.net.SocketTimeoutException e) {
            // Expected: no more packets queued right now
        } catch (Exception e) {
            // Ignore other errors
        }
    }
    
    @Override
    public void dispose() {
        restoreAutoCycleState();
        detachChannelListeners();
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
