package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.symmetrylabs.slstudio.server.VolumeCore;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

import org.lwjgl.glfw.GLFW;
import heronarts.lx.LXMappingEngine;

/**
 * The main class that runs the Volume application.
 *
 * Technically, our actual main method resides in {@link Lwjgl3Launcher}, whose sole job
 * is to initialize our window + OpenGL context and create an instance of VolumeApplication
 * attached to that context. Once the launcher does that and hands the VolumeApplication
 * instance off to libgdx, this class is what actually runs the app.
 */
public class VolumeApplication extends ApplicationAdapter implements VolumeCore.Listener {
    private final VolumeCore core;
    private RenderManager renderer;
    private LookEditor lookEditor;
    ViewController viewController;

    /* visible so that InternalsWindow can mutate it. */
    int clearRGB = 0x000000;

    /* disabled by default because on some platforms scaling makes things look much worse,
       and it's not obvious that scaling should be the default anyway. */
    boolean allowUiScale = false;
    private float osDensity;
    private SLCamera.InputController camController;

    VolumeApplication() {
        this.core = new VolumeCore(this) {
            @Override
            public void setWarning(String key, String message) {
                VolumeApplication.this.setWarning(key, message);
            }
        };
    }

    @Override
    public void create() {
        TextureManager.load("logo-overlay.png");

        UI.init(((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle(),
                "Mac OS X".equals(System.getProperty("os.name")));
        osDensity = Gdx.graphics.getDensity();
        UI.setDensity(osDensity);
        FontLoader.loadAll();

        core.create();
    }

    @Override
    public void render() {
        /* handle global keyboard inputs */
        if (UI.isKeyDown(GLFW.GLFW_KEY_M)) {
            core.lx.engine.mapping.setMode(LXMappingEngine.Mode.MIDI);
        } else {
            core.lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
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

        core.lx.engine.onDraw();

        camController.update();
        renderer.draw();
        UI.newFrame();
        lookEditor.setLook(core.lx.engine.getFocusedLook());
        WindowManager.get().draw();

        UI.setNextWindowPosition(UI.width - 30, UI.height - 30, 1, 1);
        UI.begin("Logo overlay",
                 UI.WINDOW_NO_RESIZE | UI.WINDOW_NO_MOVE | UI.WINDOW_NO_TITLE_BAR |
                 UI.WINDOW_NO_DOCKING | UI.WINDOW_NO_BACKGROUND | UI.WINDOW_ALWAYS_AUTO_RESIZE);
        TextureManager.draw("logo-overlay.png");
        UI.end();

        UI.render();
    }

    @Override
    public void dispose() {
        core.dispose();
        UI.shutdown();
    }

    void loadShow(String showName) {
        core.loadShow(showName);
    }

    @Override
    public void onCreateLX() {
        core.lx.registerExternal("volumeWindowManager", WindowManager.getVisibilitySource());
    }

    @Override
    public void onShowChangeStart() {
        WindowManager.reset();
        ConsoleWindow.reset();
        WindowManager.addPersistent(ConsoleWindow.WINDOW_NAME, () -> new ConsoleWindow(), false);
    }

    @Override
    public void onShowChangeFinished() {
        LX lx = core.lx;
        LXModel model = lx.model;

        renderer = new RenderManager(lx);
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

        viewController = new ViewController(lx, camController, renderer.cam, gnomon, markers, picker);

        ModelRenderer mr = new ModelRenderer(lx, model, viewController);
        renderer.add(mr);
        RemoteRenderer rr = new RemoteRenderer(lx, model, viewController);
        renderer.add(rr);

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
        WindowManager.addPersistent("Internals", () -> new InternalsWindow(lx, this), false);
        WindowManager.addPersistent("Master", () -> new MasterWindow(lx), true);
        WindowManager.addPersistent("Modulation", () -> new ModulationWindow(lx), false);
        WindowManager.addPersistent("OSC", () -> new OscWindow(lx), false);
        WindowManager.addPersistent("Remote Control", () -> new RemoteControlWindow(lx, viewController, rr), false);

        WindowManager.addPersistent("Developer/Imgui demo", SlimguiDemoWindow::new, false);
        WindowManager.addPersistent("Developer/Style editor", SlimguiStyleEditor::new, false);
        WindowManager.addPersistent("Developer/Imgui metrics", SlimguiMetricsWindow::new, false);
        WindowManager.addPersistent("Developer/About imgui", SlimguiAboutWindow::new, false);
    }

    @Override
    public void onDisposeLX() {
        lookEditor.dispose();
        renderer.dispose();
    }

    protected void setWarning(String key, String message) {
        ConsoleWindow.setWarning(key, message);
        if (message != null) {
            WindowManager.showPersistent(ConsoleWindow.WINDOW_NAME);
        }
    }
}
