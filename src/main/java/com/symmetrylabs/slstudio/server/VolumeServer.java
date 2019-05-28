package com.symmetrylabs.slstudio.server;

import com.google.protobuf.ByteString;
import com.symmetrylabs.slstudio.streaming.Pixels;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.mutation.LXMutationServer;
import com.symmetrylabs.slstudio.streaming.PixelDataRequest;
import com.symmetrylabs.slstudio.streaming.PixelDataServiceGrpc;
import heronarts.lx.osc.LXOscEngine;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VolumeServer implements VolumeCore.Listener {
    public static final int PIXEL_DATA_PORT = 3032;
    public static final int MAX_SENDS_PER_CLIENT = 3600;

    private static class Client {
        StreamObserver<Pixels> pixelStream;
        int sent = 0;

        Client(StreamObserver<Pixels> pd) {
            pixelStream = pd;
        }
    }

    private final VolumeCore core;
    private LXMutationServer mutationServer;
    private Server pixelDataServer;

    private PolyBuffer lxColorBuffer = null;
    private byte[] transmitBuffer = null;
    private List<Client> clients = new ArrayList<>();

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

        if (!clients.isEmpty()) {
            core.lx.engine.copyUIBuffer(lxColorBuffer, PolyBuffer.Space.SRGB8);
            int[] colors = (int[]) lxColorBuffer.getArray(PolyBuffer.Space.SRGB8);
            for (int i = 0; i < colors.length; i++) {
                int c = colors[i];
                transmitBuffer[3 * i    ] = LXColor.red(c);
                transmitBuffer[3 * i + 1] = LXColor.green(c);
                transmitBuffer[3 * i + 2] = LXColor.blue(c);
            }
            Pixels pd = Pixels.newBuilder().setColors(ByteString.copyFrom(transmitBuffer)).build();
            Iterator<Client> c = clients.iterator();
            while (c.hasNext()) {
                Client client = c.next();
                client.pixelStream.onNext(pd);
                if (client.sent++ > MAX_SENDS_PER_CLIENT) {
                    client.pixelStream.onCompleted();
                    c.remove();
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
        pixelDataServer = ServerBuilder.forPort(PIXEL_DATA_PORT).addService(new PixelDataServiceImpl()).build();

        try {
            mutationServer.start();
            pixelDataServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lxColorBuffer = new PolyBuffer(core.lx);
        transmitBuffer = new byte[core.lx.model.size * 3];
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
            c.pixelStream.onCompleted();
        }
        clients.clear();

        lxColorBuffer = null;
        transmitBuffer = null;
    }

    private class PixelDataServiceImpl extends PixelDataServiceGrpc.PixelDataServiceImplBase {
        @Override
        public void subscribe(PixelDataRequest req, StreamObserver<Pixels> response) {
            clients.add(new Client(response));
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
