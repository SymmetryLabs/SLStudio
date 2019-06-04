package heronarts.lx.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ProjectFileSource {
    enum Type {
        LEGACY_FILE,
        DIRECTORY,
        IN_MEMORY,
    }

    String sourceDescription();
    InputStream inputStream(ProjectFileType fileType, String id) throws IOException;
    OutputStream outputStream(ProjectFileType fileType, String id) throws IOException;
}
