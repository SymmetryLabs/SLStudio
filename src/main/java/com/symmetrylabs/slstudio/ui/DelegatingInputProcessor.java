package com.symmetrylabs.slstudio.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Input;

public class DelegatingInputProcessor implements InputProcessor {
    private final InputProcessor delegate;

    public DelegatingInputProcessor(InputProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean keyDown(int keycode) {
        UI.keyDown(Lwjgl3Input.getGlfwKeyCode(keycode));
        if (UI.wantCaptureKeyboard()) {
            return true;
        }
        return delegate.keyDown(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        UI.addInputCharacter(character);
        if (UI.wantCaptureKeyboard()) {
            return true;
        }
        return delegate.keyDown(character);
    }

    @Override
    public boolean keyUp(int keycode) {
        UI.keyUp(Lwjgl3Input.getGlfwKeyCode(keycode));
        if (UI.wantCaptureKeyboard()) {
            return true;
        }
        return delegate.keyUp(keycode);
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        if (UI.wantCaptureMouse()) {
            return true;
        }
        return delegate.mouseMoved(x, y);
    }

    @Override
    public boolean scrolled(int amount) {
        UI.scrolled(amount);
        if (UI.wantCaptureMouse()) {
            return true;
        }
        return delegate.scrolled(amount);
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (UI.wantCaptureMouse()) {
            return true;
        }
        return delegate.touchDown(x, y, pointer, button);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (UI.wantCaptureMouse()) {
            return true;
        }
        return delegate.touchDragged(x, y, pointer);
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (UI.wantCaptureMouse()) {
            return true;
        }
        return delegate.touchUp(x, y, pointer, button);
    }
}
