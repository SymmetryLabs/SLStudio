package com.symmetrylabs.slstudio.ui.v2;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.server.VolumeServer;
import com.symmetrylabs.slstudio.streaming.PixelDataHandshake;
import com.symmetrylabs.slstudio.streaming.Pixels;
import com.symmetrylabs.slstudio.streaming.PixelDataRequest;
import com.symmetrylabs.slstudio.streaming.PixelDataBrokerGrpc;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Deque;

public class RemoteRenderer extends PointColorRenderer {
    private static final int MAX_PIXEL_MESSAGE_SIZE = 1024;
    private static final int MAX_STAT_QUEUE_SIZE = 2048;

    protected final ViewController viewController;
    private ManagedChannel channel;
    private PixelDataBrokerGrpc.PixelDataBrokerStub service;
    private ReceiverThread receiver = null;

    boolean collectStats = false;
    float packetsPerSecond = -1;
    float megabitsPerSecond = -1;
    long latestTick = -1;

    private Deque<Long> lastPacketTimes = new ArrayDeque<>();
    private Deque<Integer> lastPacketSizes = new ArrayDeque<>();

    private final float[] incomingBuffer;

    public RemoteRenderer(LX lx, LXModel model, ViewController vc) {
        super(lx, model);
        this.viewController = vc;
        this.incomingBuffer = new float[model.size * 4];
    }

    public void connect(String target) {
        channel = ManagedChannelBuilder.forAddress(target, VolumeServer.PIXEL_DATA_PORT).usePlaintext().build();
        service = PixelDataBrokerGrpc.newStub(channel);
        if (receiver == null) {
            try {
                receiver = new ReceiverThread();
                receiver.start();
            } catch (SocketException e) {
                ApplicationState.setWarning("RemoteRender", e.getMessage());
                e.printStackTrace();
            }
        }
        startStream();
    }

    public void disconnect() {
        if (channel != null) {
            channel.shutdown();
        }
        channel = null;
        service = null;
    }

    @Override
    public void dispose() {
        disconnect();
        receiver.interrupt();
        super.dispose();
    }

    private void startStream() {
        service.subscribe(PixelDataRequest.newBuilder().setRecvPort(receiver.port).build(), new StreamObserver<PixelDataHandshake>() {
            @Override
            public void onNext(PixelDataHandshake value) {
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("pixel data subscription ended with error:");
                t.printStackTrace();
                ApplicationState.setWarning("RemoteRenderer", t.getMessage());
            }

            @Override
            public void onCompleted() {
                ApplicationState.setWarning("RemoteRenderer", null);
            }
        });
    }

    @Override
    protected void fillGLBuffer() {
        synchronized (incomingBuffer) {
            System.arraycopy(incomingBuffer, 0, glColorBuffer, 0, incomingBuffer.length);
        }
    }

    @Override
    public boolean isEnabled() {
        return viewController.isRemoteDataDisplayed();
    }

    private final class ReceiverThread extends Thread {
        final DatagramPacket packet;
        final DatagramSocket socket;
        final int port;
        final byte[] recvBuf;

        public ReceiverThread() throws SocketException {
            socket = new DatagramSocket();
            port = socket.getLocalPort();
            recvBuf = new byte[MAX_PIXEL_MESSAGE_SIZE];
            packet = new DatagramPacket(recvBuf, MAX_PIXEL_MESSAGE_SIZE);
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    socket.receive(packet);
                    loadPixelData();
                } catch (IOException e) {
                    if (!isInterrupted()) {
                        System.err.println("Exception in pixel data listener:");
                        e.printStackTrace();
                    }
                }
            }
        }

        private void loadPixelData() throws InvalidProtocolBufferException {
            Pixels p = Pixels.parser().parseFrom(packet.getData(), packet.getOffset(), packet.getLength());
            byte[] data = p.getColors().toByteArray();
            Preconditions.checkState(data.length % 3 == 0);
            int npoints = data.length / 3;
            float[] window = new float[4 * npoints];
            for (int i = 0; i < npoints; i++) {
                window[4 * i    ] = (float) (0xFF & data[3 * i    ]) / 255.f;
                window[4 * i + 1] = (float) (0xFF & data[3 * i + 1]) / 255.f;
                window[4 * i + 2] = (float) (0xFF & data[3 * i + 2]) / 255.f;
                window[4 * i + 3] = 1.f;
            }
            synchronized (incomingBuffer) {
                System.arraycopy(window, 0, incomingBuffer, 4 * p.getOffset(), window.length);
            }
            latestTick = Math.max(latestTick, p.getTick());

            if (collectStats) {
                lastPacketTimes.addLast(System.nanoTime());
                lastPacketSizes.addLast(packet.getLength());
                if (lastPacketSizes.size() > MAX_STAT_QUEUE_SIZE) {
                    float duration = 1e-9f * (lastPacketTimes.peekLast() - lastPacketTimes.peekFirst());

                    packetsPerSecond = (float) lastPacketTimes.size() / duration;

                    float bytes = 0;
                    for (Integer size : lastPacketSizes) {
                        bytes += size;
                    }
                    megabitsPerSecond = (8.0f / (1 << 6)) * (bytes / duration);

                    lastPacketTimes.clear();
                    lastPacketSizes.clear();
                }

            } else {
                lastPacketTimes.clear();
                lastPacketSizes.clear();
                packetsPerSecond = -1;
                megabitsPerSecond = -1;
            }
        }
    }
}
