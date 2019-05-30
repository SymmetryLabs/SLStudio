package heronarts.lx.mutation;

import heronarts.lx.LX;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class LXMutationSender {
    private final LX lx;

    private String target;
    private ManagedChannel channel;
    private MutationServiceGrpc.MutationServiceStub service;

    LXMutationSender(LX lx) {
        this.lx = lx;
    }

    public ConnectivityState getStatus() {
        if (channel != null) {
            return channel.getState(false);
        }
        return ConnectivityState.SHUTDOWN;
    }

    public void connect(String target) {
        this.target = target;
        channel = ManagedChannelBuilder.forAddress(target, LXMutationServer.PORT).usePlaintext().build();
        service = MutationServiceGrpc.newStub(channel);
    }

    public void disconnect() {
        if (channel != null) {
            channel.shutdownNow();
        }
        target = null;
        channel = null;
        service = null;
    }

    public void send(Mutation m) {
        if (channel == null || channel.isShutdown()) {
            return;
        }
        System.out.println("sending " + m);
        service.apply(m, new StreamObserver<MutationResult>() {
            @Override
            public void onNext(MutationResult value) {
                System.out.println("mutation " + m + " succeeded on " + target);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("mutation " + m + " failed on " + target);
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });
    }
}
