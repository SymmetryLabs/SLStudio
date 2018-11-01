package com.symmetrylabs.slstudio.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

public class SLStudioGDX extends ApplicationAdapter {
    private String showName;
    private Show show;
    private ModelRenderer renderer;
    private LX lx;
    private PatternWindow patternWindow;
    private ChannelWindow channelWindow;

    CameraInputController camController;

    @Override
    public void create() {
        showName = "pilots";
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

        patternWindow = new PatternWindow(lx, showName);
        channelWindow = new ChannelWindow(lx);

        lx.engine.start();
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
        UI.text("engine: % 4.0fms, % 3.0ffps",
                        1e-6f * lx.engine.timer.runNanos,
                        1e9f / lx.engine.timer.runNanos);
        UI.end();

        patternWindow.draw();
        channelWindow.draw();

        UI.showDemoWindow();

        UI.render();
    }

    @Override
    public void dispose () {
        renderer.dispose();
        UI.shutdown();
        lx.engine.stop();
    }

    private void loadLxComponents() {
        LXClassLoader.findWarps().stream().forEach(lx::registerWarp);
        LXClassLoader.findEffects().stream().forEach(lx::registerEffect);
        LXClassLoader.findPatterns().stream().forEach(lx::registerPattern);

        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }
}
