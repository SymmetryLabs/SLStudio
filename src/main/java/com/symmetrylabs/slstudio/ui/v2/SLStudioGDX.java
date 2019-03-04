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
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.symmetrylabs.util.Utils;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import heronarts.lx.model.LXPoint;
import com.symmetrylabs.slstudio.output.OutputControl;

public class SLStudioGDX extends ApplicationAdapter implements ApplicationState.Provider {
    private static final String DEFAULT_SHOW = "demo";
    private String showName;
    private Show show;
    private RenderManager renderer;
    private LX lx;
    private OutputControl outputControl;
    private LookEditor lookEditor;

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

        loadShow(sn);
    }

    void loadShow(String showName) {
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

        camController = new SLCamera.InputController(renderer.cam);
        camController.setTargetLH(model.cx, model.cy, model.cz);
        camController.translateUnits = model.xRange;
        camController.scrollFactor *= -0.2f;

        Gdx.input.setInputProcessor(new DelegatingInputProcessor(camController));

        loadLxComponents();

        /* The main menu isn't really transient but we don't want it to appear in
           the Window menu and it doesn't have a close button, so there's no risk of
           it disappearing. */
        WindowManager.addTransient(new MainMenu(lx, this));
        WindowManager.addPersistent("Audio", () -> new AudioWindow(lx), false);
        WindowManager.addPersistent("Channels", () -> new ChannelWindow(lx), false);
        WindowManager.addPersistent("Internals", () -> new InternalsWindow(lx, this, mr), false);
        WindowManager.addPersistent("Master", () -> new MasterWindow(lx), true);
        WindowManager.addPersistent("Modulation", () -> new ModulationWindow(lx), false);
        WindowManager.addPersistent(
            "View", () -> new CameraControlWindow(lx, camController, renderer.cam, gnomon, markers), true);

        WindowManager.addPersistent("Imgui demo", () -> new SlimguiDemoWindow(), false);
        WindowManager.addPersistent("Imgui style editor", () -> new SlimguiStyleEditor(), false);
        WindowManager.addPersistent("Imgui metrics", () -> new SlimguiMetricsWindow(), false);
        WindowManager.addPersistent("About imgui", () -> new SlimguiAboutWindow(), false);

        lookEditor = new LookEditor(lx);

        lx.engine.isMultithreaded.setValue(true);
        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.start();

        show.setupUi(lx);
    }

    @Override
    public void render() {
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
        WindowManager.get().draw();

        lookEditor.draw();

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

    @Override
    public void setWarning(String key, String message) {
        ConsoleWindow.setWarning(key, message);
        if (message != null) {
            WindowManager.showPersistent(ConsoleWindow.WINDOW_NAME);
        }
    }

}
