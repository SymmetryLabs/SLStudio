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

public class SLStudioGDX extends ApplicationAdapter {
    private static final String DEFAULT_SHOW = "demo";
    private String showName;
    private Show show;
    private ModelRenderer renderer;
    private LX lx;

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
        show = ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        lx = new LX(model);

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
    }

    @Override
    public void render() {
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl20.glClearColor(0, 0, 0, 1);
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
        lx.dispose();
    }

    private void loadLxComponents() {
        LXClassLoader.findWarps().stream().forEach(lx::registerWarp);
        LXClassLoader.findEffects().stream().forEach(lx::registerEffect);
        LXClassLoader.findPatterns().stream().forEach(lx::registerPattern);

        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }
}
