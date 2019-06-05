package com.symmetrylabs.slstudio.server;

import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.output.OutputControl;
import com.symmetrylabs.util.Utils;
import heronarts.lx.LX;
import heronarts.lx.data.LXVersion;
import heronarts.lx.data.Project;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class VolumeCore implements ApplicationState.Provider {
    private static final String DEFAULT_SHOW = "demo";
    public static final LXVersion RUNTIME_VERSION = LXVersion.VOLUME_WITH_LOOKS;

    public interface Listener {
        void onCreateLX();
        void onShowChangeStart();
        void onShowChangeFinished();
        void onDisposeLX();
    }

    private final Listener listener;
    public LX lx;
    private String showName;
    private OutputControl outputControl;
    private Show show;

    public VolumeCore(Listener listener) {
        this.listener = listener;
    }

    public void create() {
        String sn;
        try {
            sn = Files.readAllLines(Paths.get(SLStudio.SHOW_FILE_NAME)).get(0);
        } catch (IOException e) {
            System.err.println(
                "couldn't read " + SLStudio.SHOW_FILE_NAME + ": " + e.getMessage());
            sn = DEFAULT_SHOW;
        }

        ApplicationState.setProvider(this);

        /* TODO: we should remove any need to know what the "sketch path" is, because
             it means we can only run SLStudio from source, but for now we assume that
             the process is started from the root of the SLStudio repository and set
             the sketch path to that. */
        Utils.setSketchPath(Paths.get(System.getProperty("user.dir")).toString());

        loadShow(sn, true);
    }

    public void loadShow(String showName) {
        loadShow(showName, false);
    }

    protected void loadShow(String showName, boolean firstOpen) {
        System.out.println("opening show " + showName);
        if (lx != null) {
            disposeLX();
        }

        listener.onShowChangeStart();

        this.showName = showName;
        show = ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        lx = new LX(RUNTIME_VERSION, model);
        listener.onCreateLX();

        outputControl = new OutputControl(lx);

        // make sure that ApplicationState is fully filled out before setupLx is called
        show.setupLx(lx);

        loadLxComponents();

        listener.onShowChangeFinished();

        lx.engine.isMultithreaded.setValue(true);
        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.start();

        show.setupUi(lx);

        if (firstOpen) {
            File lastProjectFile = new File(SLStudioLX.PROJECT_FILE_NAME);
            if (lastProjectFile.exists()) {
                try {
                    List<String> lastProjectLines = Files.readAllLines(lastProjectFile.toPath());
                    if (lastProjectLines.size() > 0) {
                        File lastProject = new File(lastProjectLines.get(0));
                        if (lastProject.exists()) {
                            lx.openProject(Project.createLegacyProject(lastProject));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void disposeLX() {
        listener.onDisposeLX();
        NetworkMonitor.shutdownInstance(lx);

        lx.engine.stop();
        /* we have to call onDraw here because onDraw is the only thing that pokes the
             engine to actually kill the engine thread. This is a byproduct of P3LX calling
             onDraw on every frame, and P3LX needing to kill the engine thread from the
             thread that calls onDraw. We don't have the same requirements, so we mark
             the thread as needing shutdown (using stop() above) then immediately call
             onDraw to get it to actually shut down the thread. */
        lx.engine.onDraw();
        lx.dispose();
        LXPoint.resetIdCounter();
    }

    public void dispose() {
        disposeLX();
    }

    @Override
    public String showName() {
        return showName;
    }

    @Override
    public ApplicationState.Mode interfaceMode() {
        return ApplicationState.Mode.VOLUME;
    }

    @Override
    public OutputControl outputControl() {
        return outputControl;
    }

    /** Left as an exercise to the reader. */
    @Override
    public abstract void setWarning(String key, String message);

    private void loadLxComponents() {
        LXClassLoader.findWarps().forEach(lx::registerWarp);
        LXClassLoader.findEffects().forEach(lx::registerEffect);
        LXClassLoader.findPatterns().forEach(lx::registerPattern);
        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }
}
