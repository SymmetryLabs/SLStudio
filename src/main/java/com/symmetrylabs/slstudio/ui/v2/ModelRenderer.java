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
import java.nio.IntBuffer;
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

    /* bloom effect buffers and textures */
    private static final boolean BLOOM_ENABLED = false;
    protected final ShaderProgramWithProgramHandle blurShader;
    protected final ShaderProgramWithProgramHandle bloomBlendShader;
    protected final int bloomFBO;
    protected final int baseTex;
    protected final int bloomTex;
    protected final int blurFBO1;
    protected final int blurFBO2;
    protected final int blurTex1;
    protected final int blurTex2;

    /* renderQuad objects */
    protected final int quadVao;
    protected final int quadVbo;

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

        pointShader = new ShaderProgramWithProgramHandle(
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

        blurShader = new ShaderProgramWithProgramHandle(
            Gdx.files.internal("blur-vertex.glsl").readString(),
            Gdx.files.internal("blur-fragment.glsl").readString());
        if (!blurShader.isCompiled()) {
            throw new RuntimeException("blur shader compilation failed: " + pointShader.getLog());
        }
        blurShader.begin();
        blurShader.setUniformi("image", 0);

        bloomBlendShader = new ShaderProgramWithProgramHandle(
            Gdx.files.internal("bloom-blend-vertex.glsl").readString(),
            Gdx.files.internal("bloom-blend-fragment.glsl").readString());
        if (!bloomBlendShader.isCompiled()) {
            throw new RuntimeException("bloom blend shader compilation failed: " + pointShader.getLog());
        }
        bloomBlendShader.begin();
        bloomBlendShader.setUniformi("scene", 0);
        bloomBlendShader.setUniformi("bloomBlur", 1);

        vao = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(vao);

        positionVbo = GL41.glGenBuffers();
        updatePoints();
        model.addListener(m -> updatePoints());

        glColorBuffer = new float[4 * model.size];
        lxColorBuffer = new PolyBuffer(lx);
        colorVbo = GL41.glGenBuffers();

        GL41.glBindVertexArray(0);

        bloomFBO = GL41.glGenFramebuffers();
        bloomTex = GL41.glGenTextures();
        baseTex = GL41.glGenTextures();
        blurFBO1 = GL41.glGenFramebuffers();
        blurFBO2 = GL41.glGenFramebuffers();
        blurTex1 = GL41.glGenTextures();
        blurTex2 = GL41.glGenTextures();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(model.cx, model.cy, model.zMin - model.rMax);
        cam.lookAt(model.cx, model.cy, model.cz);
        cam.near = 1f;
        cam.far = 10000f;
        cam.update();

        float quadVertices[] = {
            // positions        // texture Coords
            -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
             1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
             1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
        };

        // setup plane VAO
        quadVao = GL41.glGenVertexArrays();
        GL41.glBindVertexArray(quadVao);
        quadVbo = GL41.glGenBuffers();
        GL41.glBindBuffer(GL41.GL_ARRAY_BUFFER, quadVbo);
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, quadVertices, GL41.GL_STATIC_DRAW);
        GL41.glEnableVertexAttribArray(0);
        GL41.glVertexAttribPointer(0, 3, GL41.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL41.glEnableVertexAttribArray(1);
        GL41.glVertexAttribPointer(1, 2, GL41.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
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
            vertdata[3 * i + 0] = model.points[i].x;
            vertdata[3 * i + 1] = model.points[i].y;
            vertdata[3 * i + 2] = model.points[i].z;
        }
        GL41.glBufferData(GL41.GL_ARRAY_BUFFER, vertdata, GL41.GL_DYNAMIC_DRAW);
    }

    public void setDisplayDensity(float density) {
        basePointSize = 2.2f / density;
    }

    private void configTexture(int width, int height) {
        GL41.glTexImage2D(
            GL41.GL_TEXTURE_2D, 0, GL41.GL_RGB16F, width, height, 0, GL41.GL_RGB, GL41.GL_FLOAT, (float[])null);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_LINEAR);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_LINEAR);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_S, GL41.GL_CLAMP_TO_EDGE);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_WRAP_T, GL41.GL_CLAMP_TO_EDGE);
    }

    /* this should only be run when the viewport changes size */
    public void setBackBufferSize(int width, int height) {
        GL41.glActiveTexture(GL41.GL_TEXTURE0);
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, bloomFBO);

        GL41.glBindTexture(GL41.GL_TEXTURE_2D, baseTex);
        configTexture(width, height);
        GL41.glFramebufferTexture2D(
            GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, baseTex, 0);

        GL41.glBindTexture(GL41.GL_TEXTURE_2D, bloomTex);
        configTexture(width, height);
        GL41.glFramebufferTexture2D(
            GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT1, GL41.GL_TEXTURE_2D, bloomTex, 0);

        IntBuffer drawBuffers = IntBuffer.allocate(2);
        drawBuffers.put(GL41.GL_COLOR_ATTACHMENT0);
        drawBuffers.put(GL41.GL_COLOR_ATTACHMENT1);
        GL41.glDrawBuffers(drawBuffers);

        if (GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER) != GL41.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("bloom framebuffer not complete!");
        }

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, blurFBO1);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, blurTex1);
        configTexture(width, height);
        GL41.glFramebufferTexture2D(
            GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, blurTex1, 0);
        if (GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER) != GL41.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("blur framebuffer 1 not complete!");
        }

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, blurFBO2);
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, blurTex2);
        configTexture(width, height);
        GL41.glFramebufferTexture2D(
            GL41.GL_FRAMEBUFFER, GL41.GL_COLOR_ATTACHMENT0, GL41.GL_TEXTURE_2D, blurTex2, 0);
        if (GL41.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER) != GL41.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("blur framebuffer 2 not complete!");
        }

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);
    }

    public void draw() {
        lx.engine.copyUIBuffer(lxColorBuffer, UI_COLOR_SPACE);
        int[] colors = (int[]) lxColorBuffer.getArray(UI_COLOR_SPACE);

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            glColorBuffer[4 * i + 0] = (float) ((0x00FF0000 & c) >> 16) / 255.f;
            glColorBuffer[4 * i + 1] = (float) ((0x0000FF00 & c) >> 8) / 255.f;
            glColorBuffer[4 * i + 2] = (float)  (0x000000FF & c) / 255.f;
            glColorBuffer[4 * i + 3] = 1.f;
        }

        if (BLOOM_ENABLED) {
            GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, bloomFBO);
            GL41.glClear(GL41.GL_COLOR_BUFFER_BIT);
        }

        GL41.glEnable(GL41.GL_PROGRAM_POINT_SIZE);

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

        if (BLOOM_ENABLED) {
            GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);

            /* done rendering, so next we compute the blur on bloomTex */
            blurShader.begin();
            GL41.glActiveTexture(GL41.GL_TEXTURE0);
            for (int i = 0; i < 5; i++) {
                GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, blurFBO1);
                blurShader.setUniformi("horizontal", 1);
                GL41.glBindTexture(GL41.GL_TEXTURE_2D, i == 0 ? bloomTex : blurTex2);
                renderQuad();
                GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, blurFBO2);
                blurShader.setUniformi("horizontal", 0);
                GL41.glBindTexture(GL41.GL_TEXTURE_2D, blurTex1);
                renderQuad();
            }
            GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, 0);

            /* now we have a blurred result in blurTex2, and the base scene in baseTex. Blend them and paint. */
            GL41.glClear(GL41.GL_COLOR_BUFFER_BIT);
            bloomBlendShader.begin();
            GL41.glActiveTexture(GL41.GL_TEXTURE0);
            GL41.glBindTexture(GL41.GL_TEXTURE_2D, baseTex);
            GL41.glActiveTexture(GL41.GL_TEXTURE1);
            GL41.glBindTexture(GL41.GL_TEXTURE_2D, blurTex2);
            renderQuad();
        }
    }

    private void renderQuad() {
        GL41.glBindVertexArray(quadVao);
        GL41.glDrawArrays(GL41.GL_TRIANGLE_STRIP, 0, 4);
        GL41.glBindVertexArray(0);
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
