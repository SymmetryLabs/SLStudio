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
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class RemoteRenderer extends PointColorRenderer {
    private static final int MAX_PIXEL_MESSAGE_SIZE = 1024;
    private static final int MAX_STAT_QUEUE_SIZE = 2048;
    private static final int NS_BEFORE_RECONNECT = 100_000_000; /* == 100ms */
    private static final long REQUEST_UPDATE_PERIOD_NS = VolumeServer.CLIENT_SUBSCRIPTION_TIMEOUT_NS * 4 / 5;

    private final ViewController viewController;
    private PixelDataBrokerGrpc.PixelDataBrokerStub service;
    private ReceiverThread receiver = null;

    boolean collectStats = false;
    float packetsPerSecond = -1;
    float megabitsPerSecond = -1;
    long latestTick = -1;

    private long lastUpdateAt = -1;
    private long lastRequestAt = -1;

    private Deque<Long> lastPacketTimes = new ArrayDeque<>();
    private Deque<Integer> lastPacketSizes = new ArrayDeque<>();

    private final float[] incomingBuffer;

    // 1 means all pixels are sent; 2 means every other, 3 means every third, etc.
    private int cullFactor = 3;
    private List<Integer> pointMask;
    private int subscriptionId = 0;

    public RemoteRenderer(LX lx, LXModel model, ViewController vc) {
        super(lx, model);
        this.viewController = vc;
        this.incomingBuffer = new float[model.size * 4];
    }

    public void connect(ManagedChannel channel) {
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
        sendSubscriptionRequest(true);
    }

    public void disconnect() {
        service = null;
        /* incrementing the subscription ID ensures that we ignore any further updates
         * that come in after the call to disconnect. */
        subscriptionId++;
    }

    @Override
    public void dispose() {
        disconnect();
        if (receiver != null) {
            receiver.interrupt();
            receiver = null;
        }
        super.dispose();
    }

    public void setCullFactor(int newCullFactor) {
        if (newCullFactor == cullFactor) {
            return;
        }
        cullFactor = newCullFactor;
        if (receiver != null && receiver.isAlive()) {
            sendSubscriptionRequest(true);
        }
    }

    public int getCullFactor() {
        return cullFactor;
    }

    private synchronized void sendSubscriptionRequest(boolean newSubscription) {
        lastRequestAt = System.nanoTime();

        PixelDataRequest.Builder pdr = PixelDataRequest.newBuilder()
            .setShowName(ApplicationState.showName())
            .setRecvPort(receiver.port);
        if (cullFactor != 1) {
            pointMask = new ArrayList<>();
            for (int pointIndex = 0; pointIndex < model.size; pointIndex += cullFactor) {
                pointMask.add(pointIndex);
            }
            pdr.addAllPointMask(pointMask);
        } else {
            pointMask = null;
        }

        if (newSubscription) {
            subscriptionId++;
        }
        pdr.setSubscriptionId(subscriptionId);

        // this resets all colors to fully-transparent black, so the points will be
        // hidden until we get our next color buffer update
        Arrays.fill(incomingBuffer, 0);

        service.subscribe(pdr.build(), new StreamObserver<PixelDataHandshake>() {
            @Override
            public void onNext(PixelDataHandshake resp) {
                switch (resp.getStatus()) {
                    case OK:
                        ApplicationState.setWarning("RemoteRenderer", null);
                        break;
                    case SHOW_NAME_MISMATCH:
                        ApplicationState.setWarning("RemoteRenderer", resp.getMessage());
                        break;
                    case MASK_INDEX_OUT_OF_RANGE:
                        ApplicationState.setWarning("RemoteRenderer", "server and client models differ in size");
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("pixel data subscription ended with error:");
                t.printStackTrace();
                ApplicationState.setWarning("RemoteRenderer", t.getMessage());
            }

            @Override
            public void onCompleted() {
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

    public boolean isConnected() {
        return System.nanoTime() - lastUpdateAt < NS_BEFORE_RECONNECT;
    }

    private final class ReceiverThread extends Thread {
        final DatagramPacket packet;
        final DatagramSocket socket;
        final int port;
        final byte[] recvBuf;

        public ReceiverThread() throws SocketException {
            super("RemoteRenderer.Receiver");
            setDaemon(true);
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
            Pixels pixels = Pixels.parser().parseFrom(packet.getData(), packet.getOffset(), packet.getLength());
            if (pixels.getSubscriptionId() != subscriptionId) {
                return;
            }
            lastUpdateAt = System.nanoTime();

            if (service != null && System.nanoTime() - lastRequestAt > REQUEST_UPDATE_PERIOD_NS) {
                sendSubscriptionRequest(false);
            }

            byte[] data = pixels.getColors().toByteArray();
            Preconditions.checkState(data.length % 3 == 0);

            int windowSize = data.length / 3;
            float[] window = new float[4 * windowSize];
            for (int i = 0; i < windowSize; i++) {
                window[4 * i    ] = (float) (0xFF & data[3 * i    ]) / 255.f;
                window[4 * i + 1] = (float) (0xFF & data[3 * i + 1]) / 255.f;
                window[4 * i + 2] = (float) (0xFF & data[3 * i + 2]) / 255.f;
                window[4 * i + 3] = 1.f;
            }

            if (pointMask == null) {
                synchronized (incomingBuffer) {
                    System.arraycopy(window, 0, incomingBuffer, 4 * pixels.getOffset(), window.length);
                }
            } else {
                synchronized (incomingBuffer) {
                    for (int i = 0; i < windowSize; i++) {
                        int effidx = pixels.getOffset() + i;
                        if (effidx >= pointMask.size()) {
                            break;
                        }
                        int point = pointMask.get(effidx);
                        System.arraycopy(window, 4 * i, incomingBuffer, 4 * point, 4);
                    }
                }
            }
            latestTick = Math.max(latestTick, pixels.getTick());

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
                    megabitsPerSecond = (bytes / duration) / 125000.f; // 125k = 1M / 8 bits/byte

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
