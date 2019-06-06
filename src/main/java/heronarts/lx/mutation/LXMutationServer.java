package heronarts.lx.mutation;

import heronarts.lx.LX;
import heronarts.lx.data.*;
import io.grpc.stub.StreamObserver;

public class LXMutationServer extends MutationServiceGrpc.MutationServiceImplBase {
    private final LX lx;

    public LXMutationServer(LX lx) {
        this.lx = lx;
    }

    @Override
    public void apply(Mutation mut, StreamObserver<MutationResult> response) {
        System.out.println("received " + mut);
        lx.engine.mutations.enqueue(new LXMutationQueue.MutationRequest(mut, e -> {
            if (e == null) {
                response.onNext(MutationResult.newBuilder().build());
                response.onCompleted();
            } else {
                response.onError(e);
            }
        }));
    }
}
