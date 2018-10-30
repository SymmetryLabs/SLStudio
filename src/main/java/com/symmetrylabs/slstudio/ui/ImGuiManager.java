package com.symmetrylabs.slstudio.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import imgui.Context;
import imgui.IO;
import imgui.ImGui;
import imgui.impl.LwjglGlfw;
import imgui.impl.ImplGL3;

public class ImGuiManager {
    /* This must be created for everything else to work, because
         it's constructor sets some global state (wahoo) */
    private static final Context CONTEXT = new Context();
    public static final ImGui UI = ImGui.INSTANCE;
    public static final IO IO = UI.getIo();

    private Context context;
    private LwjglGlfw glfwImpl;
    private ImplGL3 renderer;

    public void create() {
        glfwImpl = LwjglGlfw.INSTANCE;
        renderer = ImplGL3.INSTANCE;

        glfwImpl.init(
            new uno.glfw.GlfwWindow(
                ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle()),
            false,
            LwjglGlfw.GlfwClientApi.OpenGL);
    }

    public void startFrame() {
        glfwImpl.newFrame();
    }

    public void endFrame() {
        UI.render();
        if(UI.getDrawData() != null) {
            renderer.renderDrawData(UI.getDrawData());
        }
    }

    public void dispose() {
        renderer.destroyDeviceObjects();
    }

    public static class DelegatingInputProcessor implements InputProcessor {
        private final InputProcessor delegate;

        public DelegatingInputProcessor(InputProcessor delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (IO.getWantCaptureKeyboard()) {
                return true;
            }
            return delegate.keyDown(keycode);
        }

        @Override
        public boolean keyTyped(char character) {
            if (IO.getWantCaptureKeyboard()) {
                return true;
            }
            return delegate.keyDown(character);
        }

        @Override
        public boolean keyUp(int keycode) {
            if (IO.getWantCaptureKeyboard()) {
                return true;
            }
            return delegate.keyUp(keycode);
        }

        @Override
        public boolean mouseMoved(int x, int y) {
            if (IO.getWantCaptureMouse()) {
                return true;
            }
            return delegate.mouseMoved(x, y);
        }

        @Override
        public boolean scrolled(int amount) {
            if (IO.getWantCaptureMouse()) {
                return true;
            }
            return delegate.scrolled(amount);
        }

        @Override
        public boolean touchDown(int x, int y, int pointer, int button) {
            if (IO.getWantCaptureMouse()) {
                return true;
            }
            return delegate.touchDown(x, y, pointer, button);
        }

        @Override
        public boolean touchDragged(int x, int y, int pointer) {
            if (IO.getWantCaptureMouse()) {
                return true;
            }
            return delegate.touchDragged(x, y, pointer);
        }

        @Override
        public boolean touchUp(int x, int y, int pointer, int button) {
            if (IO.getWantCaptureMouse()) {
                return true;
            }
            return delegate.touchUp(x, y, pointer, button);
        }
    }
}
