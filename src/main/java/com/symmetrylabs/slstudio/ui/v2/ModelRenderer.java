package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import java.util.ArrayList;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GLCapabilities;

public class ModelRenderer {
    private static final PolyBuffer.Space UI_COLOR_SPACE = PolyBuffer.Space.SRGB8;

    public final Camera cam;
    protected final LXModel model;
    protected final ShaderProgramWithProgramHandle pointShader;
    protected final float[] glColorBuffer;
    protected final PolyBuffer lxColorBuffer;
    protected final int vao;
    protected final int positionVbo;
    protected final int colorVbo;
    protected final LX lx;
    protected final int mvpUniform;
    protected final int colorAttr;
    protected final int positionAttr;
    protected final int pointSizeUniform;
    protected float basePointSize;

    /**
     * An extension of ShaderProgram that allows us to access the OpenGL handle of the compiled shader.
     *
     * libgdx has a really nice shader compilation utility that we'd like to use, but we eventually want
     * to make the raw GL calls ourself (so that we can use GL4 instead of GL2, which is all libgdx exposes).
     * Here, we intercept calls to the protected method createProgram, which lets us store the OpenGL
     * program handle for the shader program so that we can call glUseProgram ourself.
     */
    private static class ShaderProgramWithProgramHandle extends ShaderProgram {
        int handle;

        public ShaderProgramWithProgramHandle(String vert, String frag) {
            super(vert, frag);
        }

        @Override
        protected int createProgram() {
            handle = super.createProgram();
            return handle;
        }
    }

    public ModelRenderer(LX lx, LXModel model) {
        this.model = model;
        this.lx = lx;

        printCapabilities();

        String vert = Gdx.files.internal("vertex-330.glsl").readString();
        String frag = Gdx.files.internal("fragment-330.glsl").readString();
        pointShader = new ShaderProgramWithProgramHandle(vert, frag);
        if (!pointShader.isCompiled()) {
            throw new RuntimeException("shader compilation failed: " + pointShader.getLog());
        }
        pointSizeUniform = GL41.glGetUniformLocation(pointShader.handle, "u_pointSize");
        if (pointSizeUniform < 0) {
            throw new RuntimeException("shader program does not have uniform u_pointSize");
        }
        mvpUniform = GL41.glGetUniformLocation(pointShader.handle, "u_mvp");
        if (mvpUniform < 0) {
            throw new RuntimeException("shader program does not have uniform u_mvp");
        }
        colorAttr = GL41.glGetAttribLocation(pointShader.handle, "a_color");
        if (colorAttr < 0) {
            throw new RuntimeException("shader program does not have attribute a_color");
        }
        positionAttr = GL41.glGetAttribLocation(pointShader.handle, "a_position");
        if (positionAttr < 0) {
            throw new RuntimeException("shader program does not have attribute a_position");
        }

        vao = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(vao);

        positionVbo = GL41.glGenBuffers();
        updatePoints();
        model.addListener(m -> updatePoints());

        glColorBuffer = new float[4 * model.size];
        lxColorBuffer = new PolyBuffer(lx);
        colorVbo = GL41.glGenBuffers();

        GL41.glBindVertexArray(0);

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(model.cx, model.cy, model.zMin - model.rMax);
        cam.lookAt(model.cx, model.cy, model.cz);
        cam.near = 1f;
        cam.far = 10000f;
        cam.update();
    }

    /**
     * Update vertex position VBO. Assumes the VAO is already bound.
     */
    private void updatePoints() {
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, positionVbo);
        int N = model.points.length;
        float[] vertdata = new float[3 * N];
        for (int i = 0; i < N; i++) {
            vertdata[3 * i + 0] = model.points[i].x;
            vertdata[3 * i + 1] = model.points[i].y;
            vertdata[3 * i + 2] = model.points[i].z;
        }
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertdata, GL41.GL_DYNAMIC_DRAW);
    }

    public void setDisplayDensity(float density) {
        basePointSize = 2.2f / density;
    }

    public void draw() {
        lx.engine.copyUIBuffer(lxColorBuffer, UI_COLOR_SPACE);
        int[] colors = (int[]) lxColorBuffer.getArray(UI_COLOR_SPACE);

        GL41.glEnable(GL41.GL_PROGRAM_POINT_SIZE);

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            glColorBuffer[4 * i + 0] = (float) ((0x00FF0000 & c) >> 16) / 255.f;
            glColorBuffer[4 * i + 1] = (float) ((0x0000FF00 & c) >> 8) / 255.f;
            glColorBuffer[4 * i + 2] = (float)  (0x000000FF & c) / 255.f;
            glColorBuffer[4 * i + 3] = 1.f;
        }

        pointShader.begin();

        GL41.glBindVertexArray(vao);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, colorVbo);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, glColorBuffer, GL41.GL_DYNAMIC_DRAW);
        GL41.glEnableVertexAttribArray(colorAttr);
        GL41.glVertexAttribPointer(colorAttr, 4, GL41.GL_FLOAT, false, 0, 0);

        GL41.glUniformMatrix4fv(mvpUniform, false, cam.combined.val);
        GL41.glUniform1f(pointSizeUniform, basePointSize);

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, positionVbo);
        GL41.glEnableVertexAttribArray(positionAttr);
        GL41.glVertexAttribPointer(positionAttr, 3, GL41.GL_FLOAT, false, 0, 0);

        GL41.glDrawArrays(GL41.GL_POINTS, 0, model.points.length);

        GL41.glDisableVertexAttribArray(positionAttr);
        GL41.glDisableVertexAttribArray(colorAttr);
        GL41.glBindVertexArray(0);

        GL41.glDisable(GL41.GL_PROGRAM_POINT_SIZE);
    }

    public void dispose() {
        GL41.glDeleteBuffers(colorVbo);
        GL41.glDeleteBuffers(positionVbo);
        GL41.glDeleteVertexArrays(vao);
        pointShader.dispose();
    }

    private static void printCapabilities() {
        GLCapabilities glCaps = GL.getCapabilities();
        ArrayList<String> supportedVersions = new ArrayList<>();
        if (glCaps.OpenGL46) supportedVersions.add("4.6");
        if (glCaps.OpenGL45) supportedVersions.add("4.5");
        if (glCaps.OpenGL44) supportedVersions.add("4.4");
        if (glCaps.OpenGL43) supportedVersions.add("4.3");
        if (glCaps.OpenGL42) supportedVersions.add("4.2");
        if (glCaps.OpenGL41) supportedVersions.add("4.1");
        if (glCaps.OpenGL40) supportedVersions.add("4.0");
        if (glCaps.OpenGL33) supportedVersions.add("3.3");
        if (glCaps.OpenGL32) supportedVersions.add("3.2");
        if (glCaps.OpenGL31) supportedVersions.add("3.1");
        if (glCaps.OpenGL30) supportedVersions.add("3.0");
        if (glCaps.OpenGL21) supportedVersions.add("2.1");
        if (glCaps.OpenGL20) supportedVersions.add("2.0");
        if (glCaps.OpenGL15) supportedVersions.add("1.5");
        if (glCaps.OpenGL14) supportedVersions.add("1.4");
        if (glCaps.OpenGL13) supportedVersions.add("1.3");
        if (glCaps.OpenGL12) supportedVersions.add("1.2");
        if (glCaps.OpenGL11) supportedVersions.add("1.1");
        System.out.println("supported GL versions: " + String.join(", ", supportedVersions));
    }
}
