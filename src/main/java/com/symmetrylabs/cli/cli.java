package com.symmetrylabs.cli;

import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.shows.cubes.CubesController;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.output.OutputControl;
import com.symmetrylabs.slstudio.server.VolumeCore;
import heronarts.lx.LX;
import heronarts.lx.data.LXVersion;
import heronarts.lx.data.Project;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

public class cli implements VolumeCore.Listener, ApplicationState.Provider {
    private static final String DEFAULT_SHOW = "demo";
    private final VolumeCore.Listener listener;
    NetworkMonitor networkMonitor;
    private static final LXVersion RUNTIME_VERSION = LXVersion.VOLUME_WITH_LOOKS;

    public LX lx;
    private String showName;
    private CubesShow show;
    private OutputControl outputControl;

    public cli() {
        this.listener = this;

        ApplicationState.setProvider(this);
        loadShow(DEFAULT_SHOW, true);

        Collection<CubesController> ccs = show.getSortedControllers();

        for (CubesController controller : ccs){
            System.out.println();
        }
    }

    private void loadLxComponents() {
        LXClassLoader.findWarps().forEach(lx::registerWarp);
        LXClassLoader.findEffects().forEach(lx::registerEffect);
        LXClassLoader.findPatterns().forEach(lx::registerPattern);
        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }

    protected void loadShow(String showName, boolean firstOpen) {
        System.out.println("opening show " + showName);
        if (lx != null) {
            disposeLX();
        }

        listener.onShowChangeStart();

        this.showName = showName;
        show = (CubesShow) ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        if (model.modelId == null || !model.modelId.equals(showName)) {
            throw new IllegalStateException("model for show %s has incorrect model ID: expecting " + showName + " but got " + model.modelId);
        }

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


//        if (firstOpen) {
//            File lastProjectFile = new File(SLStudioLX.PROJECT_FILE_NAME);
//            if (lastProjectFile.exists()) {
//                try {
//                    List<String> lastProjectLines = Files.readAllLines(lastProjectFile.toPath());
//                    if (lastProjectLines.size() > 0) {
//                        File lastProject = new File(lastProjectLines.get(0));
//                        if (lastProject.exists()) {
//                            lx.openProject(Project.createLegacyProject(lastProject));
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
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

    @Override
    public void onCreateLX() {

    }

    @Override
    public void onShowChangeStart() {

    }

    @Override
    public void onShowChangeFinished() {

    }

    @Override
    public void onDisposeLX() {

    }

    @Override
    public String showName() {
        return this.showName;
    }

    @Override
    public OutputControl outputControl() {
        return null;
    }

    @Override
    public void setWarning(String key, String message) {

    }

    @Override
    public ApplicationState.Mode interfaceMode() {
        return null;
    }
}
