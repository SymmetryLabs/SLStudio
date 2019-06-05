package heronarts.lx.mutation;

import heronarts.lx.LX;
import heronarts.lx.data.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class LXMutationServer {
    public static final int PORT = 3031;
    private final LX lx;
    private final Server server;

    public LXMutationServer(LX lx) {
        this.lx = lx;
        server = ServerBuilder.forPort(PORT)
            .addService(new MutationServiceImpl(lx))
            .addService(new ProjectServiceImpl(lx))
            .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println(String.format("LXMutationServer started, listening on " + PORT));
    }

    public void dispose() {
        server.shutdown();
    }

    private static class MutationServiceImpl extends MutationServiceGrpc.MutationServiceImplBase {
        private final LX lx;

        MutationServiceImpl(LX lx) {
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

    private static class ProjectServiceImpl extends ProjectLoaderGrpc.ProjectLoaderImplBase {
        private final LX lx;

        ProjectServiceImpl(LX lx) {
            this.lx = lx;
        }

        @Override
        public void reset(ProjectData pd, StreamObserver<ProjectLoadResponse> response) {
            lx.newProject();
            patch(pd, response);
        }

        @Override
        public void patch(ProjectData pd, StreamObserver<ProjectLoadResponse> response) {
            try {
                lx.getProject().load(lx, new ProtoDataSource("mutation server request", pd));
                response.onNext(ProjectLoadResponse.newBuilder().build());
                response.onCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                response.onError(e);
            }
        }

        @Override
        public void pull(ProjectPullRequest ppr, StreamObserver<ProjectData> response) {
            try {
                lx.getProject().save(lx, new ProtoDataSink("mutation server request", response::onNext));
                response.onCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                response.onError(e);
            }
        }
    }
}
