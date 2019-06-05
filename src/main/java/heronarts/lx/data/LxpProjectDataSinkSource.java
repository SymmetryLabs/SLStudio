package heronarts.lx.data;

import heronarts.lx.LX;

import java.io.*;
import java.nio.file.Path;

public class LxpProjectDataSinkSource implements ProjectDataSource, ProjectDataSink {
    protected final Path filePath;

    public LxpProjectDataSinkSource(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public String sourceDescription() {
        return String.format("lxp loader %s", filePath);
    }

    @Override
    public String sinkDescription() {
        return String.format("lxp saver %s", filePath);
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

    @Override
    public void onWriteStart(LX lx, Project project) {
    }

    @Override
    public void onWriteFinish() {
    }
}
