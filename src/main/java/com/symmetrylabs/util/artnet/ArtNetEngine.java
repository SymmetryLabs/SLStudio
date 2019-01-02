package com.symmetrylabs.util.artnet;

import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.dmx.DMXDataSnapshot;
import com.symmetrylabs.util.dmx.DMXEngine;
import fr.azelart.artnetstack.constants.Constants;
import fr.azelart.artnetstack.domain.artaddress.ArtAddress;
import fr.azelart.artnetstack.domain.artdmx.ArtDMX;
import fr.azelart.artnetstack.domain.artnet.ArtNetObject;
import fr.azelart.artnetstack.domain.artpoll.ArtPoll;
import fr.azelart.artnetstack.domain.artpollreply.ArtPollReply;
import fr.azelart.artnetstack.domain.arttimecode.ArtTimeCode;
import fr.azelart.artnetstack.domain.controller.Controller;
import fr.azelart.artnetstack.domain.controller.ControllerPortType;
import fr.azelart.artnetstack.domain.enums.PortInputOutputEnum;
import fr.azelart.artnetstack.domain.enums.PortTypeEnum;
import fr.azelart.artnetstack.listeners.ArtNetPacketListener;
import fr.azelart.artnetstack.server.ArtNetServer;
import fr.azelart.artnetstack.utils.ArtNetPacketEncoder;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.*;

import java.io.IOException;
import java.net.InterfaceAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArtNetEngine extends LXComponent {

    private final static int DEFAULT_NETWORK = 0;

    private final static int DEFAULT_SUBNET = 0;
    private final static int DEFAULT_INPUT_UNIVERSE = 0;
    private final static int DEFAULT_OUTPUT_UNIVERSE = 1;

    private final static int ARTNET_OUTPUT_PORT_INDEX = 0;
    private final static int ARTNET_INPUT_PORT_INDEX = 1;

    private final static String SHORT_NAME = "SLStudio";
    private final static String LONG_NAME = "SLStudio";

    public final DiscreteParameter subNet = (DiscreteParameter)
                    new DiscreteParameter("SubNet", DEFAULT_SUBNET, 0, 15)
                                    .setDescription("SubNet on which the engine sends and receives Art-Net messages")
                                    .setUnits(LXParameter.Units.INTEGER);

    public final DiscreteParameter inputUniverse = (DiscreteParameter)
                    new DiscreteParameter("Input Universe", DEFAULT_INPUT_UNIVERSE, 0, 15)
                                    .setDescription("Universe on which the engine listens for Art-Net message input")
                                    .setUnits(LXParameter.Units.INTEGER);

    public final DiscreteParameter outputUniverse = (DiscreteParameter)
                    new DiscreteParameter("Output Universe", DEFAULT_OUTPUT_UNIVERSE, 0, 15)
                                    .setDescription("Universe on which the engine transmits Art-Net message output")
                                    .setUnits(LXParameter.Units.INTEGER);

    public final BooleanParameter inputEnabled =
                    new BooleanParameter("Input Enabled", false)
                                    .setDescription("Enables or disables Art-Net engine input");

    public final BooleanParameter outputEnabled =
                    new BooleanParameter("Output Enabled", false)
                                    .setDescription("Enables or disables Art-Net engine output");

    public final StringParameter networkAddress =
                    new StringParameter("Network Address")
                                    .setDescription("Host address to which Art-Net socket is bound");

    /**
     * CAUTION:
     *
     * This is really stupid IMO, but Art-Net uses the terms input and output
     * backwards from how I would use it.
     *
     * Their output = DMX512 output, aka receiving ArtDMX and outputting DMX512.
     * This means an "output port" in Art-Net lingo is input from Art-Net.
     * Their input = input into the Art-Net network, aka transmitting ArtDMX,
     * aka our output.
     *
     * I'm going to use input and output to mean input coming from Art-Net
     * to SLStudio and output to Art-Net from SLStudio for our side of things
     * (settings, parameters, etc)
     *
     * However, to keep with Art-Net lingo, "Input-Port" and "Output-Port" will be
     * an Art-Net input or output port.
     *
     * So if we're setting the "input universe," as we would think of it, that
     * means we're receiving ArtDMX on that universe but it will be the Output-Port
     * that gets set with this input universe. Again, Output-Port is being used as
     * if we were receiving ArtDMX and outputting DMX512. It just so happens that
     * we're also the consumer of that DMX512, so all of their terminology seems backwards.
     *
     * - Kyle
     */

    private ArtNetServer artNetServer;
    private ControllerPortType inputPort;
    private Controller controller;
    private ControllerPortType outputPort;

    private final DMXEngine dmxEngine = new DMXEngine();
    private ConcurrentLinkedQueue<ArtDMX> pendingArtDMXPacket = new ConcurrentLinkedQueue<>();

    public ArtNetEngine(LX lx) {
        super(lx, "Art-Net");

        addParameter("subNet", this.subNet);
        addParameter("inputUniverse", this.inputUniverse);
        addParameter("outputUniverse", this.outputUniverse);
        addParameter("inputEnabled", this.inputEnabled);
        addParameter("outputEnabled", this.outputEnabled);

        createController();

        subNet.addListener(parameter -> {
            if (artNetServer != null) {
                artNetServer.addThreadTask(this::updateSubNet);
                dmxEngine.forceDirty();
            } else {
                updateSubNet();
            }
        });
        inputUniverse.addListener(parameter -> {
            if (artNetServer != null) {
                artNetServer.addThreadTask(this::updateInputUniverse);
                dmxEngine.forceDirty();
            } else {
                updateInputUniverse();
            }
        });

        outputUniverse.addListener(parameter -> {
            if (artNetServer != null) {
                artNetServer.addThreadTask(this::updateOutputUniverse);
                dmxEngine.forceDirty();
            } else {
                updateOutputUniverse();
            }
        });

        LXParameterListener enabledListener = parameter -> {
            if (inputEnabled.isOn() || outputEnabled.isOn()) {
                start();
            } else {
                stop();
            }
        };
        inputEnabled.addListener(enabledListener);
        outputEnabled.addListener(enabledListener);

        inputEnabled.addListener(parameter -> {
            if (artNetServer != null) {
                artNetServer.addThreadTask(this::updateInputEnabled);
            } else {
                updateInputEnabled();
            }
        });

        outputEnabled.addListener(parameter -> {
            if (artNetServer != null) {
                artNetServer.addThreadTask(this::updateOutputEnabled);
                dmxEngine.forceDirty();
            } else {
                updateOutputEnabled();
            }
        });
    }

    private void updateSubNet() {
        controller.setSubNetwork(subNet.getValuei());
    }

    private void updateInputUniverse() {
        outputPort.setUniverse(inputUniverse.getValuei());
    }

    private void updateOutputUniverse() {
        inputPort.setUniverse(outputUniverse.getValuei());
    }

    private void updateInputEnabled() {
        if (controller != null && outputPort != null) {
            if (inputEnabled.isOn()) {
                controller.getPortTypeMap().put(ARTNET_OUTPUT_PORT_INDEX, outputPort);
            } else {
                controller.getPortTypeMap().remove(ARTNET_OUTPUT_PORT_INDEX);
            }
        }
    }

    private void updateOutputEnabled() {
        if (controller != null && inputPort != null) {
            if (outputEnabled.isOn()) {
                controller.getPortTypeMap().put(ARTNET_INPUT_PORT_INDEX, inputPort);
            } else {
                controller.getPortTypeMap().remove(ARTNET_INPUT_PORT_INDEX);
            }
        }
    }

    private void createInputPort() {
        // Create input port
        inputPort = new ControllerPortType();
        inputPort.setType(PortTypeEnum.DMX512);
        inputPort.setDirection(PortInputOutputEnum.INPUT);
        inputPort.setUniverse(outputUniverse.getValuei());
    }

    private void createOutputPort() {
        // Create output port
        outputPort = new ControllerPortType();
        outputPort.setType(PortTypeEnum.DMX512);
        outputPort.setDirection(PortInputOutputEnum.OUTPUT);
        outputPort.setUniverse(inputUniverse.getValuei());
    }

    private void createController() {
        controller = new Controller();

        // Display
        controller.setScreen(false);
        controller.setShortName(SHORT_NAME);
        controller.setLongName(LONG_NAME);

        Map<Integer, ControllerPortType> ports = new HashMap<>();
        controller.setPortTypeMap(ports);

        createInputPort();
        createOutputPort();

        if (inputEnabled.isOn()) {
            ports.put(ARTNET_OUTPUT_PORT_INDEX, outputPort);
        }
        if (outputEnabled.isOn()) {
            ports.put(ARTNET_INPUT_PORT_INDEX, inputPort);
        }

        // Network
        controller.setNetwork(DEFAULT_NETWORK);
        controller.setSubNetwork(subNet.getValuei());
    }

    private void start() {
        if (artNetServer == null) {
            InterfaceAddress interfaceAddress = getSuitableArtNetInterfacesAddress();
            if (interfaceAddress == null) {
                System.out.println("No ip suitable for artNet (10.x.x.x or 2.x.x.x)");
                return;
            }

            try {
                artNetServer = new ArtNetServer(interfaceAddress.getAddress(),
                                interfaceAddress.getBroadcast(), Constants.DEFAULT_ART_NET_UDP_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            artNetServer.addListenerPacket(packetListener);
            dmxEngine.forceDirty();
            artNetServer.start();
            networkAddress.setValue(interfaceAddress.getAddress().getHostAddress());
        }
    }

    private InterfaceAddress getSuitableArtNetInterfacesAddress() {
        for (InterfaceAddress interfaceAddress : NetworkUtils.getInetInterfaceAddresses()) {
            if (interfaceAddress.getAddress().isLoopbackAddress())
                continue;
            byte mostSignificantByte = interfaceAddress.getAddress().getAddress()[0];
            if (mostSignificantByte == 2 || mostSignificantByte == 10) {
                return interfaceAddress;
            }
        }
        return null;
    }

    private void stop() {
        if (artNetServer != null) {
            artNetServer.stop();
            artNetServer = null;
            networkAddress.reset();
        }
    }

    public DMXEngine getDMXEngine() {
        return dmxEngine;
    }

    /**
     * Invoked by the main engine to dispatch all Art-Net messages on the
     * input queue.
     */
    public void dispatch() {
        dmxEngine.checkForDataChanges();
        ArtDMX artDMX;
        while ((artDMX = pendingArtDMXPacket.poll()) != null) {
            if (inputEnabled.isOn()) {
                int subNet = Integer.parseInt(artDMX.getSubNet(), 16);
                int universe = Integer.parseInt(artDMX.getUniverse(), 16);
                if (subNet == this.subNet.getValuei() && universe == inputUniverse.getValuei()) {
                    dmxEngine.onDataReceived(new DMXDataSnapshot(artDMX.getData()));
                }
            }
        }
        dmxEngine.storeCleanData();
    }

    private ArtNetPacketListener packetListener = new ArtNetPacketListener() {
        @Override
        public void onArtPoll(ArtPoll artPoll) {
            try {
                artNetServer.sendPacket(ArtNetPacketEncoder.encodeArtPollReplyPacket(controller,
                                artNetServer.getListenAddress(), artNetServer.getPort()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onArtDMX(ArtDMX artDMX) {
            // Received on Art-Net thread, process on main thread instead
            pendingArtDMXPacket.add(artDMX);
        }

        public void onArt(ArtNetObject artNetObject) {}
        public void onArtPollReply(ArtPollReply artPollReply) {}
        public void onArtTimeCode(ArtTimeCode artTimeCode) {}
        public void onArtAddress(ArtAddress artAddress) {}
    };

    private volatile boolean networkTaskPending = false;
    private int cachedSubUni;
    private int cachedNetwork;
    private int[] cachedDMXData = new int[512];
    private long lastSentDMXDataMs;

    public void processOutput() {
        if (!networkTaskPending && outputEnabled.isOn() && artNetServer != null
                        && ((System.currentTimeMillis() - lastSentDMXDataMs) > 800 || dmxEngine.isOutputDirty())) {
            cachedSubUni = getOutputSubUni();
            cachedNetwork = DEFAULT_NETWORK;
            System.arraycopy(dmxEngine.getStream().data, 0, cachedDMXData, 0, 512);
            networkTaskPending = true;
            dmxEngine.markDataSent();
            lastSentDMXDataMs = System.currentTimeMillis();
        }
    }

    public void sendOutput() {
        if (networkTaskPending) {
            if (artNetServer != null) {
                try {
                    artNetServer.sendPacket(ArtNetPacketEncoder.encodeArtDmxPacket(cachedSubUni, cachedNetwork, cachedDMXData));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            networkTaskPending = false;
        }
    }

    private int getOutputSubUni() {
        return (subNet.getValuei() << 4) + outputUniverse.getValuei();
    }
}
