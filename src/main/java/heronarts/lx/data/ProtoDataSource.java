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
        StringBuilder fileSummary = new StringBuilder();
        for (ProjectFile pf : data.getFileList()) {
            fileSummary.append(pf.getType().toString());
            if (pf.getId() != null) {
                fileSummary.append(String.format(" (id=%s)", pf.getId()));
            }
            fileSummary.append(" ");
        }
        return String.format("proto with files %sloaded from %s", fileSummary.toString(), sourceDescription);
    }

    @Override
    public InputStream inputStream(ProjectFileType fileType, String id) throws IOException {
        for (int i = 0; i < data.getFileCount(); i++) {
            ProjectFile pf = data.getFile(i);
            if (!pf.getType().equals(fileType.protoType)) {
                continue;
            }
            if ((pf.getId().equals("") && id == null) || (id != null && id.equals(pf.getId()))) {
                return new ByteArrayInputStream(pf.getData().toByteArray());
            }
        }
        return null;
    }
}
