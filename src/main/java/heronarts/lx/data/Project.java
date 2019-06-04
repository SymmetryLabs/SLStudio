package heronarts.lx.data;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;


public class Project {
    protected final LXVersion runtimeVersion;
    private ProjectFileSource defaultSource;

    public Project(LXVersion runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public LXVersion getRuntimeVersion() {
        return runtimeVersion;
    }

    public String getName() {
        return defaultSource.sourceDescription();
    }

    public void setDefaultSource(ProjectFileSource pfs) {
        defaultSource = pfs;
    }

    public void save(LX lx) {
        save(lx, defaultSource);
    }

    public void save(LX lx, ProjectFileSource pfs) {
        LegacyProjectLoader.save(this, lx, pfs);
    }

    public void load(LX lx) {
        load(lx, defaultSource);
    }

    public void load(LX lx, ProjectFileSource pfs) {
        LegacyProjectLoader.load(this, lx, pfs);
    }

    @Override
    public String toString() {
        return String.format("project @ %s (version=%s)", defaultSource.sourceDescription(), runtimeVersion);
    }

    public static Project createLegacyProject(File file, LXVersion runtimeVersion) {
        Project p = new Project(runtimeVersion);
        p.setDefaultSource(new LxpProjectFileSource(file.toPath()));
        return p;
    }

    @Deprecated
    public Path getRoot() {
        if (defaultSource instanceof LxpProjectFileSource) {
            return ((LxpProjectFileSource) defaultSource).filePath;
        }
        return null;
    }
}
