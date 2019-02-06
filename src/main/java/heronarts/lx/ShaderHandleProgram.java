package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;


/**
 * An extension of ShaderProgram that allows us to access the OpenGL handle of the compiled shader.
 *
 * libgdx has a really nice shader compilation utility that we'd like to use, but we eventually want
 * to make the raw GL calls ourself (so that we can use GL4 instead of GL2, which is all libgdx exposes).
 * Here, we intercept calls to the protected method createProgram, which lets us store the OpenGL
 * program handle for the shader program so that we can call glUseProgram ourself.
 */
public class ShaderHandleProgram extends ShaderProgram {
    int handle;

    public ShaderHandleProgram(String vert, String frag) {
        super(vert, frag);
    }

    @Override
    protected int createProgram() {
        handle = super.createProgram();
        return handle;
    }
}
