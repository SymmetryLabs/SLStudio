package com.symmetrylabs.slstudio.server;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.ui.v2.RemoteRenderer;
import com.symmetrylabs.slstudio.ui.v2.ViewController;
import com.symmetrylabs.slstudio.ui.v2.VolumeApplication;
import heronarts.lx.LX;
import heronarts.lx.data.*;
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

    private final VolumeApplication app;
    private LXState lxState = null;
    private boolean isShuttingDown = false;
    private boolean wantsConnection = false;
    private boolean pushProjectOnNextConnection = false;
    private String target = null;

    private ManagedChannel serverChannel;
    private ProjectLoaderGrpc.ProjectLoaderStub projectService;

    public VolumeClient(VolumeApplication app) {
        this.app = app;
    }

    public void onLXChanged(LX lx, ViewController vc, RemoteRenderer renderer) {
        if (lxState != null) {
            disconnectImpl(true);
        }
        lxState = new LXState(lx, vc, renderer);
        updateConnectionStates();
    }

    public void connect(String target, boolean pushProjectOnConnect) {
        if (isPartiallyConnected() && !target.equals(this.target)) {
            disconnectImpl(true);
        }
        this.target = target;
        this.pushProjectOnNextConnection = pushProjectOnConnect;
        wantsConnection = true;
        updateConnectionStates();
    }

    public void disconnect() {
        wantsConnection = false;
        updateConnectionStates();
    }

    public String getTarget() {
        return target;
    }

    public void onDraw() {
        /* When we're connected to a remote client, VolumeClient owns OSC settings.
         * This is a poor fix for the fact that, when we push project data back and
         * forth, we overwrite OSC settings with the remote's settings; the appropriate
         * way to fix that is just mask out the OSC part of the project data somewhere,
         * but it's not clear where yet. */
        lxState.lx.engine.osc.transmitHost.setValue(target);
        lxState.lx.engine.osc.transmitPort.setValue(LXOscEngine.DEFAULT_RECEIVE_PORT);
        lxState.lx.engine.osc.transmitActive.setValue(true);
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
            disconnectImpl(false);
        }
    }

    private void connectImpl() {
        serverChannel = ManagedChannelBuilder.forAddress(target, VolumeServer.VOLUME_SERVER_PORT).usePlaintext().build();

        projectService = ProjectLoaderGrpc.newStub(serverChannel);

        if (pushProjectOnNextConnection) {
            lxState.lx.getProject().save(
                lxState.lx, new ProtoDataSink(
                    "mutation server request",
                    pd ->projectService.push(pd, new StreamObserver<ProjectLoadResponse>() {
                        @Override
                        public void onNext(ProjectLoadResponse value) {
                            System.out.println("pushed project data to " + target);
                            ApplicationState.setWarning("VolumeClient", null);
                        }

                        @Override
                        public void onError(Throwable t) {
                            System.err.println("couldn't push project data to server: " + t.getMessage());
                            t.printStackTrace();
                            ApplicationState.setWarning("VolumeClient", t.getMessage());
                        }

                        @Override
                        public void onCompleted() {
                        }
                    })));
            pushProjectOnNextConnection = false;
        } else {
            projectService.pull(ProjectPullRequest.newBuilder().build(), new StreamObserver<ProjectData>() {
                @Override
                public void onNext(ProjectData value) {
                    String remoteModelName = value.getModelName();
                    if (remoteModelName != null) {
                        if (!ApplicationState.showName().equals(remoteModelName)) {
                            System.out.println(String.format("start change show to match server: remote=%s, local=%s", remoteModelName, ApplicationState.showName()));
                            /* if we need to change shows, we just change shows and discard the project info,
                             * which would be discarded anyway when we reinitialize LX. When we get the new
                             * LX, we'll get the callback, which will prompt us to load project data again.
                             * This is a little wasteful, but one extra round trip ain't no thing. */
                            app.loadShow(remoteModelName);
                            return;
                        }
                    }
                    lxState.lx.engine.addTask(() ->
                        lxState.lx.getProject().load(lxState.lx, new ProtoDataSource("project loader from " + target, value)));
                    ApplicationState.setWarning("VolumeClient", null);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("couldn't fetch project data from server: " + t.getMessage());
                    t.printStackTrace();
                    ApplicationState.setWarning("VolumeClient", t.getMessage());
                }

                @Override
                public void onCompleted() {
                }
            });
        }

        isShuttingDown = false;
        lxState.lx.engine.mutations.sender.connect(serverChannel, target, true);
        lxState.renderer.connect(serverChannel);
        lxState.lx.engine.osc.transmitActive.setValue(false);
        lxState.lx.engine.osc.transmitHost.setValue(target);
        lxState.lx.engine.osc.transmitPort.setValue(LXOscEngine.DEFAULT_RECEIVE_PORT);
        lxState.lx.engine.osc.transmitActive.setValue(true);
        lxState.vc.setRemoteDataDisplayed(true);
    }

    private void disconnectImpl(boolean waitForDisconnection) {
        if (serverChannel == null) {
            return;
        }

        isShuttingDown = true;

        projectService = null;
        lxState.renderer.disconnect();
        lxState.lx.engine.mutations.sender.disconnect();

        serverChannel.shutdown();
        if (waitForDisconnection) {
            try {
                serverChannel.awaitTermination(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serverChannel = null;

        lxState.lx.engine.osc.transmitActive.setValue(false);
        lxState.vc.setRemoteDataDisplayed(false);
    }
}
