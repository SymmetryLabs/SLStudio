package com.symmetrylabs.slstudio.ui.swing;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import glm.glm;
import glm.mat4x4.Mat4;
import glm.vec3.Vec3;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ModelRenderer {
    private static final PolyBuffer.Space UI_COLOR_SPACE = PolyBuffer.Space.SRGB8;

    /* kotlin interop is weird */
    private final glm glm_ = glm.INSTANCE;

    protected final LX lx;
    protected final LXModel model;
    protected final GL2ES2 gl;
    protected final int N;

    protected final int program;
    protected final int positionVbo;
    protected final int colorVbo;
    protected final int positionAttr = 0;
    protected final int colorAttr = 1;
    protected final int mvpUniform;

    protected final float[] renderColors;
    protected final PolyBuffer lxColors;

    public ModelRenderer(LX lx, LXModel model, GL2ES2 gl) {
        this.model = model;
        this.lx = lx;
        this.gl = gl;
        N = model.points.length;

        String[] vertSrc = new String[] { "#version 330 core\n", readShader("vertex.glsl") };
        String[] fragSrc = new String[] { "#version 330 core\n", readShader("fragment.glsl") };

        int vert = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        gl.glShaderSource(vert, 2, vertSrc, null);
        gl.glCompileShader(vert);
        assertShaderOK(vert, "vertex");

        int frag = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
        gl.glShaderSource(frag, 2, fragSrc, null);
        gl.glCompileShader(frag);
        assertShaderOK(frag, "fragment");

        program = gl.glCreateProgram();
        gl.glAttachShader(program, vert);
        gl.glAttachShader(program, frag);

        gl.glBindAttribLocation(program, positionAttr, "a_position");
        gl.glBindAttribLocation(program, colorAttr, "a_color");

        gl.glLinkProgram(program);
        assertLinkOK(program);

        mvpUniform = gl.glGetUniformLocation(program, "u_mvp");

        IntBuffer tmp = IntBuffer.allocate(2);
        gl.glGenBuffers(2, tmp);
        positionVbo = tmp.get(0);
        colorVbo = tmp.get(1);

        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, positionVbo);

        float[] vertdata = new float[3 * N];
        for (int i = 0; i < N; i++) {
            vertdata[3 * i + 0] = model.points[i].x;
            vertdata[3 * i + 1] = model.points[i].y;
            vertdata[3 * i + 2] = model.points[i].z;
        }
        FloatBuffer vertbuf = Buffers.newDirectFloatBuffer(vertdata);
        // 4 because a float is 4 bytes long.
        gl.glBufferData(GL2ES2.GL_ARRAY_BUFFER, 4 * 3 * N, vertbuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, 0);

        renderColors = new float[4 * N];
        lxColors = new PolyBuffer(lx);
    }

    private void assertShaderOK(int shader, String name) {
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGetShaderiv(shader, gl.GL_COMPILE_STATUS, tmp);
        if (tmp.get(0) == 1) {
            return;
        }
        gl.glGetShaderiv(shader, gl.GL_INFO_LOG_LENGTH, tmp);
        ByteBuffer buf = ByteBuffer.allocate(tmp.get(0));
        gl.glGetShaderInfoLog(shader, tmp.get(0), null, buf);
        String log = StandardCharsets.UTF_8.decode(buf).toString();
        throw new RuntimeException(name + " shader failed to load: " + log);
    }

    private void assertLinkOK(int program) {
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGetProgramiv(program, gl.GL_LINK_STATUS, tmp);
        if (tmp.get(0) == 1) {
            return;
        }
        gl.glGetProgramiv(program, gl.GL_INFO_LOG_LENGTH, tmp);
        ByteBuffer buf = ByteBuffer.allocate(tmp.get(0));
        gl.glGetProgramInfoLog(program, tmp.get(0), null, buf);
        String log = StandardCharsets.UTF_8.decode(buf).toString();
        throw new RuntimeException("program failed to link: " + log);
    }

    public void draw() {
        lx.engine.copyUIBuffer(lxColors, UI_COLOR_SPACE);
        int[] colors = (int[]) lxColors.getArray(UI_COLOR_SPACE);

        //gl.glPointSize(2);

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            renderColors[4 * i + 0] = (float) ((0x00FF0000 & c) >> 16) / 255.f;
            renderColors[4 * i + 1] = (float) ((0x0000FF00 & c) >> 8) / 255.f;
            renderColors[4 * i + 2] = (float)  (0x000000FF & c) / 255.f;
            renderColors[4 * i + 3] = 1.f;
        }

        /*
        Mat4 proj = glm_.perspective(glm_.radians(45f), 1.33f, 0.1f, 10f);
        Mat4 view = glm_.translate(new Mat4(1f), new Vec3(0, 0, -100));
        Mat4 model = new Mat4(1f);
        Mat4 mvp = proj.times(view).times(model);
        */

        FloatBuffer mvpBuf = FloatBuffer.allocate(16);
        //mvp.to(mvpBuf);
        mvpBuf.put(-0.83001506f);
        mvpBuf.put(0.0f);
        mvpBuf.put(0.0f);
        mvpBuf.put(340.30618f);
        mvpBuf.put(0.0f);
        mvpBuf.put(1.5108352f);
        mvpBuf.put(0.0f);
        mvpBuf.put(-87.62844f);
        mvpBuf.put(0.0f);
        mvpBuf.put(0.0f);
        mvpBuf.put(1.0002f);
        mvpBuf.put(886.37024f);
        mvpBuf.put(0.0f);
        mvpBuf.put(0.0f);
        mvpBuf.put(1.0f);
        mvpBuf.put(888.19275f);
        mvpBuf.rewind();

        gl.glUseProgram(program);
        gl.glUniformMatrix4fv(mvpUniform, 1, false, mvpBuf);

        gl.glEnableVertexAttribArray(positionAttr);
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, positionVbo);
        gl.glVertexAttribPointer(positionAttr, 3, GL2ES2.GL_FLOAT, false, 0, 0);

        gl.glEnableVertexAttribArray(colorAttr);
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, colorVbo);
        FloatBuffer colorBuf = Buffers.newDirectFloatBuffer(renderColors);
        gl.glBufferData(GL2ES2.GL_ARRAY_BUFFER, 4 * 4 * N, colorBuf, GL.GL_DYNAMIC_DRAW);
        gl.glVertexAttribPointer(colorAttr, 4, GL2ES2.GL_FLOAT, false, 0, 0);

        gl.glDrawArrays(GL2ES2.GL_POINTS, 0, N);

        gl.glDisableVertexAttribArray(positionAttr);
        gl.glDisableVertexAttribArray(colorAttr);

        gl.glUseProgram(0);
    }

    public void dispose() {
        gl.glDeleteBuffers(2, new int[] { positionVbo, colorVbo }, 0);
        gl.glDeleteProgram(program);
    }

    private static String readShader(String name) {
        StringBuilder sb = new StringBuilder();
        try {
            Files.lines(Paths.get("src/main/resources/" + name)).forEach(sb::append);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
