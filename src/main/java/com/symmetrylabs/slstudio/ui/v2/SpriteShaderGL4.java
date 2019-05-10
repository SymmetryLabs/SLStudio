package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * GL4 version of the shader used for 2D drawing in LibGDX.
 *
 * Because GDX "doesn't support GL4" (I showed them lol), their shaders don't
 * start with a "#version" directive and assume GL2. They did have the
 * unbelievable courtesy of allowing us to override their default shaders,
 * though, so this is a port of the shader code in SpriteBatch from GL2 to GL4.
 * You can create SpriteBatches by using the constructor that allows you to
 * pass in a shader program.
 *
 * Note that on many platforms (i.e., all but Known Troublemaker Macintosh
 * Operating System Ten) the default shader works fine, because some poor, abused
 * driver developer was made to make their GL4 implementation detect and accept
 * GL2 shader sources. MacOS seems to be the only one of the three that implements
 * the spec and nothing but the spec, so developing on Linux and Windows can
 * leave you with a false sense of correctness.
 */
public class SpriteShaderGL4 {
    public static ShaderProgram createDefaultShader() {
        /* adapted from the source of SpriteBatch to work with GL4 on MacOS */
        String vertexShader =
            "#version 330 core\n"
            + "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "out vec4 v_color;\n"
            + "out vec2 v_texCoords;\n"
            + "void main() {\n"
            + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "   v_color.a = v_color.a * (255.0/254.0);\n"
            + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "}\n";

        String fragmentShader =
            "#version 330 core\n"
            + "#ifdef GL_ES\n"
            + "#define LOWP lowp\n"
            + "precision mediump float;\n"
            + "#else\n"
            + "#define LOWP \n"
            + "#endif\n"
            + "in LOWP vec4 v_color;\n"
            + "in vec2 v_texCoords;\n"
            + "uniform sampler2D u_texture;\n"
            + "layout (location = 0) out vec4 FragColor;\n"
            + "void main() {\n"
            + "  FragColor = v_color * texture(u_texture, v_texCoords);\n" //
            + "}";

        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        return shader;
    }
}
