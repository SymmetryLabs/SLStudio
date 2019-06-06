package com.symmetrylabs.slstudio.server;

import com.google.protobuf.ByteString;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.streaming.*;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.data.Project;
import heronarts.lx.data.ProjectLoaderService;
import heronarts.lx.mutation.LXMutationServer;
import heronarts.lx.osc.LXOscEngine;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VolumeServer implements VolumeCore.Listener {
    static final int VOLUME_SERVER_PORT = 3032;

    private static final String PROJECT_STORE = "volume-server-project.lxp";
    private static final long SAVE_PROJECT_PERIOD_NS = 5_000_000_000L; /* = 5 sec */

    private static final int MAX_COLOR_DATA_SIZE_BYTES = 450;
    private static final int POINTS_PER_UDP_PACKET = MAX_COLOR_DATA_SIZE_BYTES / 3;

    private static final float MAX_TRANSMIT_FPS = 30.f;
    private static final long MIN_TRANSMIT_PERIOD_NS = (long) Math.ceil(1e9f / MAX_TRANSMIT_FPS);

    private class Client {
        final String target;
        final DatagramSocket clientSock;
        final List<DatagramPacket> packets;
        final int[][] packetPoints;
        final byte[][] colorBuffers;
        final int[] offsets;
        final int subscriptionId;

        Client(String target, DatagramSocket clientSock, List<Integer> pointMask, int subscriptionId) {
            this.target = target;
            this.clientSock = clientSock;
            this.packets = new ArrayList<>();
            this.subscriptionId = subscriptionId;

            if (pointMask != null) {
                for (Integer p : pointMask) {
                    if (p >= core.lx.model.size) {
                        throw new IndexOutOfBoundsException();
                    }
                }
            }

            if (pointMask == null || pointMask.isEmpty()) {
                int size = core.lx.model.size;
                int packetCount = (int) Math.ceil((float) size / POINTS_PER_UDP_PACKET);
                packetPoints = new int[packetCount][];

                int remaining = size;
                int pointIndex = 0;
                for (int i = 0; i < packetCount; i++) {
                    packetPoints[i] = new int[Math.min(remaining, POINTS_PER_UDP_PACKET)];
                    for (int j = 0; j < packetPoints[i].length; j++) {
                        packetPoints[i][j] = pointIndex++;
                    }
                    remaining -= packetPoints[i].length;
                }
            } else {
                int packetCount = (int) Math.ceil((float) pointMask.size() / POINTS_PER_UDP_PACKET);
                packetPoints = new int[packetCount][];

                int packetIdx = 0;
                int idx = 0;
                int remaining = pointMask.size();
                while (remaining > 0) {
                    int[] packetIndexes = new int[Math.min(remaining, POINTS_PER_UDP_PACKET)];
                    for (int i = 0; i < packetIndexes.length; i++) {
                        packetIndexes[i] = pointMask.get(idx++);
                    }
                    packetPoints[packetIdx++] = packetIndexes;
                    remaining -= packetIndexes.length;
                }
            }

            colorBuffers = new byte[packetPoints.length][];
            for (int i = 0; i < colorBuffers.length; i++) {
                colorBuffers[i] = new byte[packetPoints[i].length * 3];
            }

            for (int i = 0; i < packetPoints.length; i++) {
                packets.add(new DatagramPacket(new byte[0], 0, 0, clientSock.getRemoteSocketAddress()));
            }

            offsets = new int[packetPoints.length];
            int offset = 0;
            for (int i = 0; i < packetPoints.length; i++) {
                offsets[i] = offset;
                offset += packetPoints[i].length;
            }
        }
    }

    private final VolumeCore core;
    private Server grpcServer;

    private PolyBuffer lxColorBuffer = null;
    private final List<Client> clients = new ArrayList<>();
    private final List<Client> newClients = new ArrayList<>();

    private long lastTransmit = 0;

    private long lastSaveTime = -1;

    public VolumeServer() {
        this.core = new VolumeCore(this) {
            @Override
            public void setWarning(String key, String message) {
                if (message != null) {
                    System.err.println(String.format("WARN %s: %s", key, message));
                }
            }
        };
        core.lx.openProject(Project.createLegacyProject(new File(PROJECT_STORE)));
    }

    public void start() {
        core.create();
    }

    private void tick() {
        if (System.nanoTime() - lastSaveTime > SAVE_PROJECT_PERIOD_NS) {
            core.lx.engine.logTimers();
            core.lx.saveProject();
            lastSaveTime = System.nanoTime();
        }

        /* this is a hack but it's also The Only Way To Be Sure. */
        core.lx.engine.osc.receiveHost.setValue("0.0.0.0");
        core.lx.engine.osc.receivePort.setValue(LXOscEngine.DEFAULT_RECEIVE_PORT);
        core.lx.engine.osc.receiveActive.setValue(true);

        core.lx.engine.onDraw();
        if (!core.lx.engine.isThreadRunning()) {
            throw new IllegalStateException("engine thread stopped unexpectedly");
        }

        synchronized (newClients) {
            /* if a client is reconnecting, remove its old record */
            for (Client newClient : newClients) {
                Iterator<Client> citer = clients.iterator();
                while (citer.hasNext()) {
                    Client c = citer.next();
                    if (c.target.equals(newClient.target)) {
                        c.clientSock.disconnect();
                        citer.remove();
                    }
                }
            }
            clients.addAll(newClients);
            newClients.clear();
        }
        long transmitTIme = System.nanoTime();
        if (transmitTIme - lastTransmit > MIN_TRANSMIT_PERIOD_NS) {
            lastTransmit = transmitTIme;
            if (!clients.isEmpty()) {
                core.lx.engine.copyUIBuffer(lxColorBuffer, PolyBuffer.Space.SRGB8);
                int[] colors = (int[]) lxColorBuffer.getArray(PolyBuffer.Space.SRGB8);

                Iterator<Client> c = clients.iterator();
                while (c.hasNext()) {
                    Client client = c.next();

                    for (int packet = 0; packet < client.packetPoints.length; packet++) {
                        int[] packetPoints = client.packetPoints[packet];
                        byte[] buffer = client.colorBuffers[packet];
                        for (int packetIndex = 0; packetIndex < packetPoints.length; packetIndex++) {
                            int color = colors[packetPoints[packetIndex]];
                            buffer[3 * packetIndex    ] = LXColor.redByteUnsafe(color);
                            buffer[3 * packetIndex + 1] = LXColor.greenByteUnsafe(color);
                            buffer[3 * packetIndex + 2] = LXColor.blueByteUnsafe(color);
                        }

                        Pixels p = Pixels.newBuilder()
                            .setColors(ByteString.copyFrom(buffer))
                            .setOffset(client.offsets[packet])
                            .setTick(core.lx.engine.getTickCount())
                            .setSubscriptionId(client.subscriptionId)
                            .build();
                        byte[] encoded = p.toByteArray();
                        client.packets.get(packet).setData(encoded);
                        client.packets.get(packet).setLength(encoded.length);
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
        grpcServer = ServerBuilder
            .forPort(VOLUME_SERVER_PORT)
            .addService(ServerInterceptors.intercept(new PixelDataBrokerImpl(), new IPCapturingRequestInterceptor()))
            .addService(new LXMutationServer(core.lx))
            .addService(new ProjectLoaderService(core.lx))
            .build();

        try {
            grpcServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lxColorBuffer = new PolyBuffer(core.lx);
    }

    @Override
    public void onShowChangeStart() {
    }

    @Override
    public void onShowChangeFinished() {
    }

    @Override
    public void onDisposeLX() {
        grpcServer.shutdown();
        try {
            grpcServer.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        grpcServer = null;

        for (Client c : clients) {
            c.clientSock.close();
        }
        clients.clear();
        synchronized (newClients) {
            newClients.clear();
        }

        lxColorBuffer = null;
    }

    private class PixelDataBrokerImpl extends PixelDataBrokerGrpc.PixelDataBrokerImplBase {
        @Override
        public void subscribe(PixelDataRequest req, StreamObserver<PixelDataHandshake> response) {
            PixelDataHandshake.Builder res = PixelDataHandshake.newBuilder();
            if (req.getShowName().equals(ApplicationState.showName())) {
                DatagramSocket sock = null;
                try {
                    sock = new DatagramSocket();
                    sock.connect(new InetSocketAddress(req.getRecvAddress(), req.getRecvPort()));
                    synchronized (newClients) {
                        newClients.add(new Client(req.getRecvAddress(), sock, req.getPointMaskList(), req.getSubscriptionId()));
                    }
                    res.setStatus(PixelDataSubscriptionStatus.OK);
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (sock != null) sock.close();
                    res.setStatus(PixelDataSubscriptionStatus.MASK_INDEX_OUT_OF_RANGE);
                } catch (SocketException e) {
                    if (sock != null) sock.close();
                    e.printStackTrace();
                    response.onError(e);
                    return;
                }
            } else {
                res.setStatus(PixelDataSubscriptionStatus.SHOW_NAME_MISMATCH)
                    .setMessage(String.format(
                        "server show name is %s, client requested pixel data for '%s'",
                        ApplicationState.showName(), req.getShowName()));
            }
            response.onNext(res.build());
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
