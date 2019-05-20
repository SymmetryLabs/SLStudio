package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.data.Project;
import heronarts.lx.model.LXModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.symmetrylabs.util.Utils;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import heronarts.lx.model.LXPoint;
import com.symmetrylabs.slstudio.output.OutputControl;
import heronarts.lx.data.LXVersions;
import org.lwjgl.glfw.GLFW;
import heronarts.lx.LXMappingEngine;

public class SLStudioGDX extends ApplicationAdapter implements ApplicationState.Provider {
    private static final String DEFAULT_SHOW = "demo";
    public static final int RUNTIME_VERSION = LXVersions.VOLUME_WITH_LOOKS;

    private String showName;
    private Show show;
    private RenderManager renderer;
    private LX lx;
    private OutputControl outputControl;
    private LookEditor lookEditor;
    public ViewController viewController;

    /* visible so that InternalsWindow can mutate it. */
    int clearRGB = 0x000000;
    /* disabled by default because on some platforms scaling makes things look much worse,
       and it's not obvious that scaling should be the default anyway. */
    boolean allowUiScale = false;
    float osDensity;

    SLCamera.InputController camController;

    int lastBufWidth = 0, lastBufHeight;

    @Override
    public void create() {
        TextureManager.load("logo-overlay.png");

        String sn;
        try {
            sn = Files.readAllLines(Paths.get(SLStudio.SHOW_FILE_NAME)).get(0);
        } catch (IOException e) {
            System.err.println(
                "couldn't read " + SLStudio.SHOW_FILE_NAME + ": " + e.getMessage());
            sn = DEFAULT_SHOW;
        }
        UI.init(((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle(),
                "Mac OS X".equals(System.getProperty("os.name")));
        osDensity = Gdx.graphics.getDensity();
        UI.setDensity(osDensity);
        FontLoader.loadAll();

        /* TODO: we should remove any need to know what the "sketch path" is, because
             it means we can only run SLStudio from source, but for now we assume that
             the process is started from the root of the SLStudio repository and set
             the sketch path to that. */
        Utils.setSketchPath(Paths.get(System.getProperty("user.dir")).toString());

        ApplicationState.setProvider(this);

        loadShow(sn, true);
    }

    void loadShow(String showName) {
        loadShow(showName, false);
    }

    protected void loadShow(String showName, boolean firstOpen) {
        System.out.println("opening show " + showName);
        if (lx != null) {
            disposeLX();
        }

        WindowManager.reset();
        ConsoleWindow.reset();
        WindowManager.addPersistent(ConsoleWindow.WINDOW_NAME, () -> new ConsoleWindow(), false);

        this.showName = showName;
        show = ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        lx = new LX(model);
        lx.registerExternal("volumeWindowManager", WindowManager.getVisibilitySource());

        outputControl = new OutputControl(lx);

        // make sure that ApplicationState is fully filled out before setupLx is called
        show.setupLx(lx);

        renderer = new RenderManager(lx);
        ModelRenderer mr = new ModelRenderer(lx, model);
        renderer.add(mr);
        GnomonRenderable gnomon = new GnomonRenderable(model, renderer.shaderProvider);
        renderer.add(gnomon);
        MarkerRenderable markers = new MarkerRenderable(lx);
        renderer.add(markers);
        ModelPicker picker = new ModelPicker(lx.model, renderer.cam);

        camController = new SLCamera.InputController(renderer.cam);
        camController.setTargetLH(model.cx, model.cy, model.cz);
        camController.translateUnits = model.xRange;
        camController.scrollFactor *= -0.2f;

        Gdx.input.setInputProcessor(new DelegatingInputProcessor(camController, picker));

        loadLxComponents();

        viewController = new ViewController(lx, camController, renderer.cam, gnomon, markers, picker);

        lookEditor = new LookEditor(lx);
        /* we want WindowManager to handle all of the drawing so it can manage the interaction between
           the UI running and the engine running, so we put it in charge of drawing the look editor */
        WindowManager.addTransient(lookEditor);

        /* These windows aren't really transient but we don't want them to appear in
           the Window menu and they don't have a close button, so there's no risk of
           them disappearing. */
        WindowManager.addTransient(new MainMenu(lx, this));
        WindowManager.addTransient(picker);
        WindowManager.addTransient(new CaptionWindow(lx));

        WindowManager.addPersistent("Audio", () -> new AudioWindow(lx), false);
        WindowManager.addPersistent("Color swatches", () -> new ColorSwatchWindow(lx, lookEditor), false);
        WindowManager.addPersistent("Internals", () -> new InternalsWindow(lx, this, mr), false);
        WindowManager.addPersistent("Master", () -> new MasterWindow(lx), true);
        WindowManager.addPersistent("Modulation", () -> new ModulationWindow(lx), false);

        WindowManager.addPersistent("Developer/Imgui demo", () -> new SlimguiDemoWindow(), false);
        WindowManager.addPersistent("Developer/Style editor", () -> new SlimguiStyleEditor(), false);
        WindowManager.addPersistent("Developer/Imgui metrics", () -> new SlimguiMetricsWindow(), false);
        WindowManager.addPersistent("Developer/About imgui", () -> new SlimguiAboutWindow(), false);

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
                            lx.openProject(Project.createLegacyProject(lastProject, RUNTIME_VERSION));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void render() {
        /* handle global keyboard inputs */
        if (UI.isKeyDown(GLFW.GLFW_KEY_M)) {
            lx.engine.mapping.setMode(LXMappingEngine.Mode.MIDI);
        } else {
            lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
        }

        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();

        UI.width = Gdx.graphics.getWidth();
        UI.height = Gdx.graphics.getHeight();
        UI.setDensity(allowUiScale ? osDensity : 1.f);

        Gdx.gl20.glViewport(0, 0, w, h);
        renderer.setDisplayProperties(w, h, UI.density);

        float clearR = ((clearRGB >> 16) & 0xFF) / 255.f;
        float clearG = ((clearRGB >>  8) & 0xFF) / 255.f;
        float clearB = ((clearRGB      ) & 0xFF) / 255.f;
        Gdx.gl20.glClearColor(clearR, clearG, clearB, 1);

        Gdx.gl20.glClear(
            GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
            | (Gdx.graphics.getBufferFormat().coverageSampling
                 ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        renderer.cam.viewportHeight = Gdx.graphics.getHeight();
        renderer.cam.viewportWidth = Gdx.graphics.getWidth();
        renderer.cam.update();

        lx.engine.onDraw();

        camController.update();
        renderer.draw();
        UI.newFrame();
        lookEditor.setLook(lx.engine.getFocusedLook());
        WindowManager.get().draw();

        UI.setNextWindowPosition(UI.width - 30, UI.height - 30, 1, 1);
        UI.begin("Logo overlay",
                 UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_MOVE | UI.WINDOW_NO_TITLE_BAR |
                 UI.WINDOW_NO_DOCKING | UI.WINDOW_NO_BACKGROUND | UI.WINDOW_ALWAYS_AUTO_RESIZE);
        TextureManager.draw("logo-overlay.png");
        UI.end();

        UI.render();
    }

    private void disposeLX() {
        NetworkMonitor.shutdownInstance(lx);
        lookEditor.dispose();
        renderer.dispose();
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
    public void dispose() {
        disposeLX();
        UI.shutdown();
    }

    private void loadLxComponents() {
        LXClassLoader.findWarps().stream().forEach(lx::registerWarp);
        LXClassLoader.findEffects().stream().forEach(lx::registerEffect);
        LXClassLoader.findPatterns().stream().forEach(lx::registerPattern);

        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }

    @Override // ApplicationState.Provider
    public String showName() {
        return showName;
    }

    @Override // ApplicationState.Provider
    public OutputControl outputControl() {
        return outputControl;
    }

    @Override // ApplicationState.Provider
    public void setWarning(String key, String message) {
        ConsoleWindow.setWarning(key, message);
        if (message != null) {
            WindowManager.showPersistent(ConsoleWindow.WINDOW_NAME);
        }
    }

    @Override // ApplicationState.Provider
    public ApplicationState.Mode interfaceMode() {
        return ApplicationState.Mode.VOLUME;
    }
}
