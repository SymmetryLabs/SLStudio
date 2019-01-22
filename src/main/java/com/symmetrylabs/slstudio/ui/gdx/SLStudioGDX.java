package com.symmetrylabs.slstudio.ui.gdx;

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

public class SLStudioGDX extends ApplicationAdapter implements ApplicationState.Provider {
    private static final String DEFAULT_SHOW = "demo";
    private String showName;
    private Show show;
    private ModelRenderer renderer;
    private LX lx;

    private int clearRGB;
    private float clearR, clearG, clearB;

    CameraInputController camController;

    @Override
    public void create() {
        try {
            showName = Files.readAllLines(Paths.get(SLStudio.SHOW_FILE_NAME)).get(0);
        } catch (IOException e) {
            System.err.println(
                "couldn't read " + SLStudio.SHOW_FILE_NAME + ": " + e.getMessage());
            showName = DEFAULT_SHOW;
        }
        ApplicationState.setProvider(this);
        show = ShowRegistry.getShow(showName);

        /* TODO: we should remove any need to know what the "sketch path" is, because
             it means we can only run SLStudio from source, but for now we assume that
             the process is started from the root of the SLStudio repository and set
             the sketch path to that. */
        Utils.setSketchPath(Paths.get(System.getProperty("user.dir")).toString());

        LXModel model = show.buildModel();
        lx = new LX(model);
        show.setupLx(lx);

        renderer = new ModelRenderer(lx, model);

        UI.init(((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle());

        camController = new CameraInputController(renderer.cam);
        camController.target.set(model.cx, model.cy, model.cz);
        camController.translateUnits = model.xRange;
        camController.scrollFactor *= -0.2f;

        Gdx.input.setInputProcessor(new DelegatingInputProcessor(camController));

        loadLxComponents();

        WindowManager.get().add(new MainMenu(lx));
        WindowManager.get().add(new PatternWindow(lx, showName));
        WindowManager.get().add(new ProjectWindow(lx));

        lx.engine.isMultithreaded.setValue(true);
        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.start();

        show.setupUi(lx);

        /* clearR/clearG/clearB will be set on first frame */
        clearRGB = 0x222222;
    }

    @Override
    public void render() {
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl20.glClearColor(clearR, clearG, clearB, 1);
        Gdx.gl20.glClear(
            GL20.GL_COLOR_BUFFER_BIT
            | (Gdx.graphics.getBufferFormat().coverageSampling
                 ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        renderer.cam.viewportHeight = Gdx.graphics.getHeight();
        renderer.cam.viewportWidth = Gdx.graphics.getWidth();
        renderer.cam.update();

        lx.engine.onDraw();

        camController.update();

        renderer.draw();

        UI.newFrame();

        UI.begin("Internals");
        UI.text("engine average: % 4.0fms, % 3.0ffps",
                        1e-6f * lx.engine.timer.runAvgNanos,
                        1e9f / lx.engine.timer.runAvgNanos);
        UI.text("    worst-case: % 4.0fms, % 3.0ffps",
                        1e-6f * lx.engine.timer.runWorstNanos,
                        1e9f / lx.engine.timer.runWorstNanos);
        UI.text("ui frame rate:  % 3.0ffps", UI.getFrameRate());
        clearRGB = UI.colorPicker("background", clearRGB);
        clearR = ((clearRGB >> 16) & 0xFF) / 255.f;
        clearG = ((clearRGB >>  8) & 0xFF) / 255.f;
        clearB = ((clearRGB      ) & 0xFF) / 255.f;
        UI.end();

        WindowManager.get().draw();

        UI.showDemoWindow();

        UI.render();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        UI.shutdown();
        lx.engine.stop();
        /* we have to call onDraw here because onDraw is the only thing that pokes the
             engine to actually kill the engine thread. This is a byproduct of P3LX calling
             onDraw on every frame, and P3LX needing to kill the engine thread from the
             thread that calls onDraw. We don't have the same requirements, so we mark
             the thread as needing shutdown (using stop() above) then immediately call
             onDraw to get it to actually shut down the thread. */
        lx.engine.onDraw();
        lx.dispose();
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
}
