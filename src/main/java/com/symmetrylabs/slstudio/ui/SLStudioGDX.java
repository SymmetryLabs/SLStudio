package com.symmetrylabs.slstudio.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

import static com.symmetrylabs.slstudio.ui.ImGuiManager.UI;

public class SLStudioGDX extends ApplicationAdapter {
    private String showName;
    private Show show;
    private ModelRenderer renderer;
    private ImGuiManager ui;
    private LX lx;
    private PatternWindow patternWindow;

    CameraInputController camController;

    @Override
    public void create() {
        showName = "pilots";
        show = ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        lx = new LX(model);

        renderer = new ModelRenderer(lx, model);

        ui = new ImGuiManager();
        ui.create();

        camController = new CameraInputController(renderer.cam);
        camController.target.set(model.cx, model.cy, model.cz);
        Gdx.input.setInputProcessor(
            new ImGuiManager.DelegatingInputProcessor(camController));

        loadLxComponents();

        patternWindow = new PatternWindow(lx, showName);
    }

    @Override
    public void render() {
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(
            GL20.GL_COLOR_BUFFER_BIT
            | (Gdx.graphics.getBufferFormat().coverageSampling
                 ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        lx.engine.onDraw();

        camController.update();

        renderer.draw();
        ui.startFrame();

        UI.begin("Internals", null, 0);
        UI.text("framerate: %.0f fps", ImGuiManager.IO.getFramerate());
        UI.end();

        patternWindow.draw();

        ui.endFrame();
    }

    @Override
    public void dispose () {
        renderer.dispose();
        ui.dispose();
    }

    private void loadLxComponents() {
        // Add all warps
        LXClassLoader.findWarps().stream().forEach(lx::registerWarp);

        // Add all effects
        LXClassLoader.findEffects().stream().forEach(lx::registerEffect);

        // Add all patterns
        LXClassLoader.findPatterns().stream().forEach(lx::registerPattern);

        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
    }
}
