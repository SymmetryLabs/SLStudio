package com.symmetrylabs.slstudio.server;

import com.google.protobuf.ByteString;
import com.symmetrylabs.slstudio.streaming.PixelDataBrokerGrpc;
import com.symmetrylabs.slstudio.streaming.PixelDataHandshake;
import com.symmetrylabs.slstudio.streaming.Pixels;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.mutation.LXMutationServer;
import com.symmetrylabs.slstudio.streaming.PixelDataRequest;
import heronarts.lx.osc.LXOscEngine;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VolumeServer implements VolumeCore.Listener {
    public static final int PIXEL_DATA_PORT = 3032;
    private static final int MAX_COLOR_DATA_SIZE_BYTES = 450;
    private static final int POINTS_PER_UDP_PACKET = MAX_COLOR_DATA_SIZE_BYTES / 3;

    private static final float MAX_TRANSMIT_FPS = 30.f;
    private static final long MIN_TRANSMIT_PERIOD_NS = (long) Math.ceil(1e9f / MAX_TRANSMIT_FPS);

    private class Client {
        DatagramSocket clientSock;
        List<DatagramPacket> packets;

        Client(DatagramSocket clientSock) {
            this.clientSock = clientSock;
            this.packets = new ArrayList<>();
            for (int i = 0; i < offsets.length; i++) {
                packets.add(new DatagramPacket(new byte[0], 0, 0, clientSock.getRemoteSocketAddress()));
            }
        }
    }

    private final VolumeCore core;
    private LXMutationServer mutationServer;
    private Server pixelDataServer;

    private PolyBuffer lxColorBuffer = null;
    private byte[] transmitBuffer = null;
    private int[] offsets = null;
    private final List<Client> clients = new ArrayList<>();
    private final List<Client> newClients = new ArrayList<>();

    private long lastTransmit = 0;

    private int tickCount = 0;

    public VolumeServer() {
        this.core = new VolumeCore(this) {
            @Override
            public void setWarning(String key, String message) {
                if (message != null) {
                    System.err.println(String.format("WARN %s: %s", key, message));
                }
            }
        };
    }

    public void start() {
        core.create();
    }

    private void tick() {
        /* this is a hack but it's also The Only Way To Be Sure. */
        core.lx.engine.osc.receiveHost.setValue("0.0.0.0");
        core.lx.engine.osc.receivePort.setValue(LXOscEngine.DEFAULT_RECEIVE_PORT);
        core.lx.engine.osc.receiveActive.setValue(true);

        core.lx.engine.onDraw();

        tickCount++;
        /* roll over manually (so we don't roll over into negative values */
        if (tickCount > (1L << 30)) {
            tickCount = 0;
        }

        synchronized (newClients) {
            clients.addAll(newClients);
            newClients.clear();
        }
        long transmitTIme = System.nanoTime();
        if (transmitTIme - lastTransmit > MIN_TRANSMIT_PERIOD_NS) {
            lastTransmit = transmitTIme;
            if (!clients.isEmpty()) {
                core.lx.engine.copyUIBuffer(lxColorBuffer, PolyBuffer.Space.SRGB8);
                int[] colors = (int[]) lxColorBuffer.getArray(PolyBuffer.Space.SRGB8);
                for (int i = 0; i < colors.length; i++) {
                    int c = colors[i];
                    transmitBuffer[3 * i] = LXColor.red(c);
                    transmitBuffer[3 * i + 1] = LXColor.green(c);
                    transmitBuffer[3 * i + 2] = LXColor.blue(c);
                }
                Pixels[] pd = new Pixels[offsets.length];
                for (int i = 0; i < offsets.length; i++) {
                    int len = (i + 1 < offsets.length ? offsets[i + 1] : transmitBuffer.length / 3) - offsets[i];
                    pd[i] = Pixels.newBuilder()
                        .setOffset(offsets[i])
                        .setColors(ByteString.copyFrom(transmitBuffer, 3 * offsets[i], 3 * len))
                        .setTick(tickCount)
                        .build();
                }
                byte[][] bufs = new byte[pd.length][];
                for (int i = 0; i < pd.length; i++) {
                    bufs[i] = pd[i].toByteArray();
                }

                Iterator<Client> c = clients.iterator();
                while (c.hasNext()) {
                    Client client = c.next();
                    for (int i = 0; i < pd.length; i++) {
                        byte[] buf = bufs[i];
                        DatagramPacket p = client.packets.get(i);
                        p.setData(buf);
                        p.setLength(buf.length);
                    }
                    try {
                        for (DatagramPacket p : client.packets) {
                            client.clientSock.send(p);
                        }
                    } catch (IOException e) {
                        System.out.println("failed to send to " + client.toString() + ", removing connection");
                        e.printStackTrace();
                        c.remove();
                    }
                }
            }
        }
    }

    public void stop() {
        core.dispose();
    }

    @Override
    public void onCreateLX() {
        mutationServer = new LXMutationServer(core.lx);
        pixelDataServer = ServerBuilder
            .forPort(PIXEL_DATA_PORT)
            .addService(ServerInterceptors.intercept(new PixelDataBrokerImpl(), new IPCapturingRequestInterceptor()))
            .build();

        try {
            mutationServer.start();
            pixelDataServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lxColorBuffer = new PolyBuffer(core.lx);

        transmitBuffer = new byte[3 * core.lx.model.size];
        int colorPackets = (int) Math.ceil((float) core.lx.model.size / (float) POINTS_PER_UDP_PACKET);
        offsets = new int[colorPackets];
        for (int i = 0; i < colorPackets; i++) {
            offsets[i] = i * POINTS_PER_UDP_PACKET;
        }
    }

    @Override
    public void onShowChangeStart() {
    }

    @Override
    public void onShowChangeFinished() {
    }

    @Override
    public void onDisposeLX() {
        mutationServer.dispose();
        mutationServer = null;
        pixelDataServer.shutdownNow();
        pixelDataServer = null;

        for (Client c : clients) {
            c.clientSock.close();
        }
        clients.clear();
        synchronized (newClients) {
            newClients.clear();
        }

        lxColorBuffer = null;
        transmitBuffer = null;
    }

    private class PixelDataBrokerImpl extends PixelDataBrokerGrpc.PixelDataBrokerImplBase {
        @Override
        public void subscribe(PixelDataRequest req, StreamObserver<PixelDataHandshake> response) {
            System.out.println("got subscription request: " + req);
            try {
                DatagramSocket sock = new DatagramSocket();
                sock.connect(new InetSocketAddress(req.getRecvAddress(), req.getRecvPort()));
                synchronized (newClients) {
                    newClients.add(new Client(sock));
                }
            } catch (SocketException e) {
                e.printStackTrace();
                response.onError(e);
                return;
            }
            response.onNext(PixelDataHandshake.newBuilder().build());
            response.onCompleted();
        }
    }

    /**
     * This abomination intercepts incoming requests, looks for PixelDataRequests, and when it finds one,
     * if it doesn't have its IP address filled out, we fill it in with the address we received.
     *
     * This is the only way for us to get the IP address for the sender. We could just have senders
     * tell us what IP address to send to, but then they would have to figure out which of their interfaces
     * they want us to send to, when the process of sending us the subscription request is sufficient
     * to discover which interface is the one they can reach the server from.
     */
    private class IPCapturingRequestInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
            SocketAddress remote = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
            final String remoteAddrString =
                remote == null ? "" :
                    remote instanceof InetSocketAddress
                        ? ((InetSocketAddress) remote).getHostString()
                        : remote.toString();

            ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
                @Override
                public void onMessage(ReqT message) {
                    if (message instanceof PixelDataRequest) {
                        PixelDataRequest incoming = (PixelDataRequest) message;
                        if (incoming.getRecvAddress() == null || incoming.getRecvAddress().equals("")) {
                            PixelDataRequest newMessage = PixelDataRequest
                                .newBuilder((PixelDataRequest) message)
                                .setRecvAddress(remoteAddrString)
                                .build();
                            message = (ReqT) newMessage;
                        }
                    }
                    super.onMessage(message);
                }
            };
        }
    }

    public static void main(String[] args) {
        VolumeServer server = new VolumeServer();
        server.start();
        try {
            //noinspection InfiniteLoopStatement
            while (true) server.tick();
        } finally {
            server.stop();
        }
    }
}
