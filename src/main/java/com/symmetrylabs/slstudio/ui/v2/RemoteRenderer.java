package com.symmetrylabs.slstudio.ui.v2;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.server.VolumeServer;
import com.symmetrylabs.slstudio.streaming.PixelData;
import com.symmetrylabs.slstudio.streaming.PixelDataRequest;
import com.symmetrylabs.slstudio.streaming.PixelDataServiceGrpc;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;

public class RemoteRenderer extends PointColorRenderer {
    protected final ViewController viewController;
    private ManagedChannel channel;
    private PixelDataServiceGrpc.PixelDataServiceStub service;
    private PixelData lastReceived = null;
    private long lastFrameReceivedAt = 0;
    private long lastFrameGap = -1;

    public RemoteRenderer(LX lx, LXModel model, ViewController vc) {
        super(lx, model);
        this.viewController = vc;
    }

    public void connect(String target) {
        channel = ManagedChannelBuilder.forAddress(target, VolumeServer.PIXEL_DATA_PORT).usePlaintext().build();
        service = PixelDataServiceGrpc.newStub(channel);
        startStream();
    }

    public void disconnect() {
        channel.shutdownNow();
        channel = null;
        service = null;
    }

    @Override
    public void dispose() {
        disconnect();
        super.dispose();
    }

    long getLastFrameTimeNanos() {
        return lastFrameGap;
    }

    protected void startStream() {
        service.subscribe(PixelDataRequest.newBuilder().build(), new StreamObserver<PixelData>() {
            @Override
            public void onNext(PixelData value) {
                lastReceived = value;

                long time = System.nanoTime();
                lastFrameGap = time - lastFrameReceivedAt;
                lastFrameReceivedAt = time;

                ApplicationState.setWarning("RemoteRenderer", null);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("pixel data subscription ended with error:");
                t.printStackTrace();
                ApplicationState.setWarning("RemoteRenderer", t.getMessage());
            }

            @Override
            public void onCompleted() {
                startStream();
            }
        });
    }

    @Override
    protected void fillGLBuffer() {
        /* copy to a variable so that our reference is stable even if a new one comes in */
        final PixelData pd = lastReceived;
        if (pd == null) {
            Arrays.fill(glColorBuffer, 0);
            return;
        }
        final ByteString bs = pd.getPixelData();
        Preconditions.checkState(bs.size() % 3 == 0);
        for (int i = 0; i < bs.size() / 3; i++) {
            glColorBuffer[4 * i    ] = (float) (0xFF & bs.byteAt(3 * i    )) / 255.f;
            glColorBuffer[4 * i + 1] = (float) (0xFF & bs.byteAt(3 * i + 1)) / 255.f;
            glColorBuffer[4 * i + 2] = (float) (0xFF & bs.byteAt(3 * i + 2)) / 255.f;
            glColorBuffer[4 * i + 3] = 1.f;
        }
    }

    @Override
    public boolean isEnabled() {
        return viewController.isRemoteDataDisplayed();
    }
}
