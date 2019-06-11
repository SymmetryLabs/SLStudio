package heronarts.lx.data;

import heronarts.lx.LX;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.Semaphore;

public class ProjectLoaderService extends ProjectLoaderGrpc.ProjectLoaderImplBase {
    private final LX lx;
    public final Semaphore projectLoadSemaphore = new Semaphore(1);

    public ProjectLoaderService(LX lx) {
        this.lx = lx;
    }

    @Override
    public void push(ProjectData pd, StreamObserver<ProjectLoadResponse> response) {
        try {
            projectLoadSemaphore.acquire();
            lx.engine.addTask(() -> {
                try {
                    lx.getProject().load(lx, new ProtoDataSource("mutation server request", pd));
                    response.onNext(ProjectLoadResponse.newBuilder().build());
                    response.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    response.onError(e);
                } finally {
                    projectLoadSemaphore.release();
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
