package com.symmetrylabs.slstudio.server;

import com.symmetrylabs.slstudio.ui.v2.RemoteRenderer;
import com.symmetrylabs.slstudio.ui.v2.ViewController;
import heronarts.lx.LX;
import heronarts.lx.data.ProjectData;
import heronarts.lx.data.ProjectLoaderGrpc;
import heronarts.lx.data.ProjectPullRequest;
import heronarts.lx.data.ProtoDataSource;
import heronarts.lx.mutation.LXMutationServer;
import heronarts.lx.osc.LXOscEngine;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class VolumeClient {
    /** All client state associated with an instance of LX, so that remote connections can outlast LX instances */
    private static class LXState {
        final LX lx;
        final ViewController vc;
        final RemoteRenderer renderer;

        LXState(LX lx, ViewController vc, RemoteRenderer renderer) {
            this.lx = lx;
            this.vc = vc;
            this.renderer = renderer;
        }
    }

    private LXState lxState = null;
    private boolean isShuttingDown = false;
    private boolean wantsConnection = false;
    private String target = null;

    private ManagedChannel serverChannel;
    private ProjectLoaderGrpc.ProjectLoaderStub projectService;

    public void onLXChanged(LX lx, ViewController vc, RemoteRenderer renderer) {
        if (lxState != null) {
            disconnectImpl();
        }
        lxState = new LXState(lx, vc, renderer);
        updateConnectionStates();
    }

    public void connect(String target) {
        if (isPartiallyConnected() && !target.equals(this.target)) {
            /* save this to a local because disconnectImpl sets serverChannel to null */
            ManagedChannel c = serverChannel;
            disconnectImpl();
            try {
                c.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.target = target;
        wantsConnection = true;
        updateConnectionStates();
    }

    public void disconnect() {
        wantsConnection = false;
        updateConnectionStates();
    }

    public RemoteRenderer getRemoteRenderer() {
        return lxState == null ? null : lxState.renderer;
    }

    private boolean isFullyConnected() {
        return isRendererReceiving() && isChannelConnected();
    }

    private boolean isPartiallyConnected() {
        return isRendererReceiving() || isChannelConnected();
    }

    public boolean isRendererReceiving() {
        return lxState != null && lxState.renderer.isConnected();
    }

    public boolean isChannelConnected() {
        if (serverChannel == null) {
            return false;
        }
        ConnectivityState state = serverChannel.getState(false);
        return state == ConnectivityState.IDLE || state == ConnectivityState.READY;
    }

    private void updateConnectionStates() {
        if (wantsConnection && !isFullyConnected()) {
            /* if the renderer stops receiving, ask it to reconnect but leave everything else intact. */
            if (!isRendererReceiving() && isChannelConnected()) {
                lxState.renderer.connect(serverChannel);
            } else {
                connectImpl();
            }
        } else if (!wantsConnection && isPartiallyConnected() && !isShuttingDown){
            disconnectImpl();
        }
    }

    private void connectImpl() {
        serverChannel = ManagedChannelBuilder.forAddress(target, LXMutationServer.PORT).usePlaintext().build();

        projectService = ProjectLoaderGrpc.newStub(serverChannel);
        projectService.pull(ProjectPullRequest.newBuilder().build(), new StreamObserver<ProjectData>() {
            @Override
            public void onNext(ProjectData value) {
                lxState.lx.engine.addTask(() ->
                    lxState.lx.getProject().load(lxState.lx, new ProtoDataSource("project loader from " + target, value)));
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("couldn't fetch project data from server: " + t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });

        isShuttingDown = false;
        lxState.lx.engine.mutations.sender.connect(serverChannel, target, true);
        lxState.renderer.connect(serverChannel);
        lxState.lx.engine.osc.transmitActive.setValue(false);
        lxState.lx.engine.osc.transmitHost.setValue(target);
        lxState.lx.engine.osc.transmitPort.setValue(LXOscEngine.DEFAULT_RECEIVE_PORT);
        lxState.lx.engine.osc.transmitActive.setValue(true);
        lxState.vc.setRemoteDataDisplayed(true);
    }

    private void disconnectImpl() {
        isShuttingDown = true;

        projectService = null;
        lxState.renderer.disconnect();
        lxState.lx.engine.mutations.sender.disconnect();

        serverChannel.shutdown();
        serverChannel = null;

        lxState.lx.engine.osc.transmitActive.setValue(false);
        lxState.vc.setRemoteDataDisplayed(false);
    }
}
