package com.symmetrylabs.slstudio.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.ShowRegistry;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import imgui.Context;
import imgui.ImGui;
import imgui.impl.LwjglGlfw; /* letters! */
import imgui.impl.ImplGL3; /* letters! */

public class SLStudioGDX extends ApplicationAdapter {
    String showName;
    Show show;
    ModelRenderer renderer;
    Context uiContext;
    ImGui ui;
    LwjglGlfw uiImpl;
    ImplGL3 uiRenderer;
    imgui.IO io;

    @Override
    public void create() {
        showName = "pilots";
        show = ShowRegistry.getShow(showName);
        System.out.println("\n---- Show: " + showName + " ----");
        LXModel model = show.buildModel();
        printModelStats(model);
        renderer = new ModelRenderer(model);

        uiContext = new Context();
        ui = ImGui.INSTANCE;
        uiImpl = LwjglGlfw.INSTANCE;
        uiRenderer = ImplGL3.INSTANCE;
        io = ui.getIo();

        uiImpl.init(
            new uno.glfw.GlfwWindow(
                ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle()),
            false,
            LwjglGlfw.GlfwClientApi.OpenGL);
    }

    @Override
    public void render() {
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        uiImpl.newFrame();
        renderer.draw();

        ui.text("framerate: %.0f fps", io.getFramerate());
        ui.text("Hi");
        ui.render();
        if(ui.getDrawData() != null) {
            uiRenderer.renderDrawData(ui.getDrawData());
        }
    }

    void printModelStats(LXModel model) {
        System.out.println("# of points: " + model.points.length);
        System.out.println("model.xMin: " + model.xMin);
        System.out.println("model.xMax: " + model.xMax);
        System.out.println("model.xRange: " + model.xRange);
        System.out.println("model.yMin: " + model.yMin);
        System.out.println("model.yMax: " + model.yMax);
        System.out.println("model.yRange: " + model.yRange);
        System.out.println("model.zMin: " + model.zMin);
        System.out.println("model.zMax: " + model.zMax);
        System.out.println("model.zRange: " + model.zRange + "\n");
    }

    @Override
    public void dispose () {
        renderer.dispose();
        uiRenderer.destroyDeviceObjects();
    }
}
