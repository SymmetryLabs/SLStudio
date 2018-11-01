package com.symmetrylabs.slstudio.ui.gdx;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;

public class ParameterWindow {
    private final LX lx;
    private final LXPattern pattern;
    private final String name;

    public ParameterWindow(LX lx, String name, LXPattern pattern) {
        this.lx = lx;
        this.name = name;
        this.pattern = pattern;
    }

    public void draw() {
        UI.begin(name);
        UI.end();
    }
}
