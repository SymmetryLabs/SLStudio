package heronarts.lx.mutation;

import heronarts.lx.LX;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class LXMutationServer {
    public static final int MUTATION_SERVER_PORT = 3031;
    private final LX lx;
    private final Server server;

    public LXMutationServer(LX lx) {
        this.lx = lx;
        server = ServerBuilder.forPort(MUTATION_SERVER_PORT)
            .addService(new ServiceImpl(lx))
            .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println(String.format("LXMutationServer started, listening on " + MUTATION_SERVER_PORT));
    }

    public void dispose() {
        server.shutdown();
    }

    private static class ServiceImpl extends MutationServiceGrpc.MutationServiceImplBase {
        private final LX lx;

        ServiceImpl(LX lx) {
            this.lx = lx;
        }

        @Override
        public void apply(Mutation mut, StreamObserver<MutationResult> response) {
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
}
