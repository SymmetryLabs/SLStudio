package heronarts.lx.mutation;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class LXMutationSender {
    private String target;
    private MutationServiceGrpc.MutationServiceStub mutationService;

    public void connect(ManagedChannel channel, String target, boolean fetchState) {
        this.target = target;
        mutationService = MutationServiceGrpc.newStub(channel);

    }

    public void disconnect() {
        target = null;
        mutationService = null;
    }

    public void send(Mutation m) {
        if (mutationService == null) {
            return;
        }
        System.out.println("sending " + m);
        mutationService.apply(m, new StreamObserver<MutationResult>() {
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
