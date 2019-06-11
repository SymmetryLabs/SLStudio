package heronarts.lx.data;

import heronarts.lx.LX;
import io.grpc.stub.StreamObserver;

public class ProjectLoaderService extends ProjectLoaderGrpc.ProjectLoaderImplBase {
    private final LX lx;

    public ProjectLoaderService(LX lx) {
        this.lx = lx;
    }

    @Override
    public void reset(ProjectData pd, StreamObserver<ProjectLoadResponse> response) {
        lx.newProject();
        patch(pd, response);
    }

    @Override
    public void patch(final ProjectData pd, StreamObserver<ProjectLoadResponse> response) {
        try {
            lx.engine.addTask(() -> {
                try {
                    lx.getProject().load(lx, new ProtoDataSource("mutation server request", pd));
                    response.onNext(ProjectLoadResponse.newBuilder().build());
                    response.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    response.onError(e);
                }
            });
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
