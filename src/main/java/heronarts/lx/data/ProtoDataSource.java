package heronarts.lx.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProtoDataSource implements ProjectDataSource {
    private final String sourceDescription;
    private final ProjectData data;

    public ProtoDataSource(String sourceDescription, ProjectData data) {
        this.sourceDescription = sourceDescription;
        this.data = data;
    }

    @Override
    public String sourceDescription() {
        return String.format("proto loaded from %s", sourceDescription);
    }

    @Override
    public InputStream inputStream(ProjectFileType fileType, String id) throws IOException {
        for (int i = 0; i < data.getFileCount(); i++) {
            ProjectFile pf = data.getFile(i);
            if (!pf.getType().equals(fileType.protoType)) {
                continue;
            }
            if ((pf.getId() == null && id == null) || (id != null && id.equals(pf.getId()))) {
                return new ByteArrayInputStream(pf.getData().toByteArray());
            }
        }
        return null;
    }
}
