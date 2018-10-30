package com.symmetrylabs.slstudio.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;

import static com.symmetrylabs.slstudio.ui.ImGuiManager.UI;

public class SLStudioGDX extends ApplicationAdapter {
    String showName;
    Show show;
    ModelRenderer renderer;
    ImGuiManager ui;

    CameraInputController camController;

    @Override
    public void create() {
        showName = "pilots";
        show = ShowRegistry.getShow(showName);

        LXModel model = show.buildModel();
        renderer = new ModelRenderer(model);

        ui = new ImGuiManager();
        ui.create();

        camController = new CameraInputController(renderer.cam);
        camController.target.set(model.cx, model.cy, model.cz);
        Gdx.input.setInputProcessor(
            new ImGuiManager.DelegatingInputProcessor(camController));
    }

    @Override
    public void render() {
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camController.update();

        renderer.draw();
        ui.startFrame();

        UI.text("framerate: %.0f fps", ImGuiManager.IO.getFramerate());
        UI.text("Hi");

        ui.endFrame();
    }

    @Override
    public void dispose () {
        renderer.dispose();
        ui.dispose();
    }
}
