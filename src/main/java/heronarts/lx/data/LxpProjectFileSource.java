package heronarts.lx.data;

import java.io.*;
import java.nio.file.Path;

public class LxpProjectFileSource implements ProjectFileSource {
    protected final Path filePath;

    public LxpProjectFileSource(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public String sourceDescription() {
        return String.format("lxp loader %s", filePath);
    }

    @Override
    public InputStream inputStream(ProjectFileType fileType, String id) throws IOException {
        if (fileType == ProjectFileType.LegacyProjectFile) {
            return new FileInputStream(filePath.toFile());
        }
        return null;
    }

    @Override
    public OutputStream outputStream(ProjectFileType fileType, String id) throws IOException {
        if (fileType == ProjectFileType.LegacyProjectFile) {
            return new FileOutputStream(filePath.toFile());
        }
        return null;
    }
}
