package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.Gdx;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import org.lwjgl.opengl.GL41;

abstract class PointColorRenderer implements RenderManager.Renderable {
    protected static final PolyBuffer.Space UI_COLOR_SPACE = PolyBuffer.Space.SRGB8;

    /* Visible so it can be modified in Internals window */
    static float scalePointSize = 0.1f;

    protected final LXModel model;
    protected final LX lx;
    protected final float[] glColorBuffer;

    private final com.symmetrylabs.slstudio.ui.v2.ShaderHandleProgram pointShader;
    private final int vao;
    private final int positionVbo;
    private final int colorVbo;
    private final int mvpUniform;
    private final int colorAttr;
    private final int positionAttr;
    private final int pointSizeUniform;
    private float basePointSize;

    public PointColorRenderer(LX lx, LXModel model) {
        this.model = model;
        this.lx = lx;

        pointShader = new com.symmetrylabs.slstudio.ui.v2.ShaderHandleProgram(
            Gdx.files.internal("point-vertex.glsl").readString(),
            Gdx.files.internal("point-fragment.glsl").readString());
        if (!pointShader.isCompiled()) {
            throw new RuntimeException("point shader compilation failed: " + pointShader.getLog());
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
        colorVbo = GL41.glGenBuffers();

        GL41.glBindVertexArray(0);
    }

    /**
     * Update vertex position VBO. Assumes the VAO is already bound.
     */
    private void updatePoints() {
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, positionVbo);
        int N = model.points.length;
        float[] vertdata = new float[3 * N];
        for (int i = 0; i < N; i++) {
            vertdata[3 * i    ] = model.points[i].x;
            vertdata[3 * i + 1] = model.points[i].y;
            vertdata[3 * i + 2] = model.points[i].z;
        }
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertdata, GL41.GL_DYNAMIC_DRAW);
    }

    @Override
    public void setDisplayProperties(int width, int height, float density) {
        basePointSize = 4.f * density;
    }

    protected abstract void fillGLBuffer();

    @Override
    public void draw(SLCamera cam) {
        fillGLBuffer();

        GL41.glEnable(GL41.GL_PROGRAM_POINT_SIZE);

        pointShader.begin();

        GL41.glBindVertexArray(vao);
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, colorVbo);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, glColorBuffer, GL41.GL_DYNAMIC_DRAW);
        GL41.glEnableVertexAttribArray(colorAttr);
        GL41.glVertexAttribPointer(colorAttr, 4, GL41.GL_FLOAT, false, 0, 0);

        GL41.glUniformMatrix4fv(mvpUniform, false, cam.combined.val);
        GL41.glUniform1f(pointSizeUniform, scalePointSize * basePointSize);

        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, positionVbo);
        GL41.glEnableVertexAttribArray(positionAttr);
        GL41.glVertexAttribPointer(positionAttr, 3, GL41.GL_FLOAT, false, 0, 0);

        GL41.glDrawArrays(GL41.GL_POINTS, 0, model.points.length);

        GL41.glDisableVertexAttribArray(positionAttr);
        GL41.glDisableVertexAttribArray(colorAttr);
        GL41.glBindVertexArray(0);

        GL41.glDisable(GL41.GL_PROGRAM_POINT_SIZE);
    }

    @Override
    public void dispose() {
        GL41.glDeleteBuffers(colorVbo);
        GL41.glDeleteBuffers(positionVbo);
        GL41.glDeleteVertexArrays(vao);
        pointShader.dispose();
    }
}
