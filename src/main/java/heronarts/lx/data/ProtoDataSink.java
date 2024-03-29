package heronarts.lx.data;

import com.google.protobuf.ByteString;
import heronarts.lx.LX;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProtoDataSink implements ProjectDataSink {
    public interface ProtoReadyCallback {
        void onProtoReady(ProjectData data);
    }

    private static class FileStream {
        ProjectFileType type;
        String id;
        ByteArrayOutputStream stream;

        FileStream(ProjectFileType type, String id, ByteArrayOutputStream stream) {
            this.type = type;
            this.id = id;
            this.stream = stream;
        }
    }

    private final ProtoReadyCallback callback;
    private final String sinkDescription;
    private List<FileStream> liveStreams = new ArrayList<>();
    private ProjectData.Builder currentBuilder = null;

    public ProtoDataSink(String sinkDescription, ProtoReadyCallback callback) {
        this.sinkDescription = sinkDescription;
        this.callback = callback;
    }

    @Override
    public String sinkDescription() {
        return String.format("proto wrapper over %s", sinkDescription);
    }

    @Nullable
    @Override
    public OutputStream outputStream(ProjectFileType fileType, String id) throws IOException {
        for (FileStream fs : liveStreams) {
            if (((fs.id == null && id == null) || (fs.id != null && fs.id.equals(id))) && fs.type == fileType) {
                return fs.stream;
            }
        }
        FileStream fs = new FileStream(fileType, id, new ByteArrayOutputStream());
        liveStreams.add(fs);
        return fs.stream;
    }

    @Override
    public void onWriteStart(LX lx, Project project) {
        liveStreams.clear();
        currentBuilder = ProjectData.newBuilder()
            .setVersion(lx.version.versionCode)
            .setModelName(lx.model.modelId);
    }

    @Override
    public void onWriteFinish() throws IOException {
        for (FileStream fs : liveStreams) {
            ProjectFile.Builder pf = ProjectFile.newBuilder()
                .setType(fs.type.protoType)
                .setData(ByteString.copyFrom(fs.stream.toByteArray()));
            if (fs.id != null) {
                pf.setId(fs.id);
            }
            currentBuilder.addFile(pf);
        }

        callback.onProtoReady(currentBuilder.build());
        liveStreams.clear();
        currentBuilder = null;
    }
}
