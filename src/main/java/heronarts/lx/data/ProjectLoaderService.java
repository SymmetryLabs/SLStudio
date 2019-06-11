package heronarts.lx.data;

import heronarts.lx.LX;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProjectLoaderService extends ProjectLoaderGrpc.ProjectLoaderImplBase {
    private final LX lx;
    public final Lock projectLoadLock = new ReentrantLock();

    public ProjectLoaderService(LX lx) {
        this.lx = lx;
    }

    @Override
    public void reset(ProjectData pd, StreamObserver<ProjectLoadResponse> response) {
        doLoad(pd, response, true);
    }

    @Override
    public void patch(final ProjectData pd, StreamObserver<ProjectLoadResponse> response) {
        doLoad(pd, response, false);
    }

    private void doLoad(ProjectData pd, StreamObserver<ProjectLoadResponse> response, boolean newProject) {
        projectLoadLock.lock();
        try {
            lx.engine.addTask(() -> {
                try {
                    if (newProject) {
                        lx.newProject();
                    }
                    lx.getProject().load(lx, new ProtoDataSource("mutation server request", pd));
                    response.onNext(ProjectLoadResponse.newBuilder().build());
                    response.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    response.onError(e);
                } finally {
                    projectLoadLock.unlock();
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
