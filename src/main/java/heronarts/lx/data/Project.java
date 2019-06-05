package heronarts.lx.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import heronarts.lx.LX;


/**
 * Project is the serialization interface for the state of an LXEngine.
 *
 * It contains sinks and sources for data, and is able to patch the state
 * of the current LXEngine by loading partial or full data from it's underlying data source.
 */
public class Project {
    private ProjectDataSource defaultSource;
    private ProjectDataSink defaultSink;

    public Project() {
    }

    public String getName() {
        return defaultSource == null ? null : defaultSource.sourceDescription();
    }

    public void setDefaultSource(ProjectDataSource pfs) {
        defaultSource = pfs;
    }

    public void setDefaultSink(ProjectDataSink pfs) {
        defaultSink = pfs;
    }

    public void save(LX lx) {
        save(lx, defaultSink);
    }

    public void save(LX lx, ProjectDataSink pfs) {
        try {
            pfs.onWriteStart(lx, this);
            LegacyProjectLoader.save(this, lx, pfs);
            pfs.onWriteFinish();
        } catch (IOException e) {
            System.err.println("couldn't write to " + pfs.sinkDescription() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void load(LX lx) {
        load(lx, defaultSource);
    }

    public void load(LX lx, ProjectDataSource pfs) {
        try {
            LegacyProjectLoader.load(this, lx, pfs);
        } catch (JsonParseException | IOException e) {
            System.err.println("couldn't load from " + pfs.sourceDescription() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return String.format("project (source=%s, sink=%s)", defaultSource.sourceDescription(), defaultSink.sinkDescription());
    }

    public static Project createLegacyProject(File file) {
        Project p = new Project();
        LxpProjectDataSinkSource lxp = new LxpProjectDataSinkSource(file.toPath());
        p.setDefaultSource(lxp);
        p.setDefaultSink(lxp);
        return p;
    }

    @Deprecated
    public Path getRoot() {
        if (defaultSource instanceof LxpProjectDataSinkSource) {
            return ((LxpProjectDataSinkSource) defaultSource).filePath;
        }
        return null;
    }
}
