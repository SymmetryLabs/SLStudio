package com.symmetrylabs.slstudio.server;

import heronarts.lx.mutation.LXMutationServer;

import java.io.IOException;

public class VolumeServer implements VolumeCore.Listener {
    private final VolumeCore core;
    private LXMutationServer server;

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

    public void tick() {
        core.lx.engine.onDraw();
    }

    public void stop() {
        core.dispose();
    }

    @Override
    public void onCreateLX() {
        server = new LXMutationServer(core.lx);
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        server.dispose();
        server = null;
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
