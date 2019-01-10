package com.symmetrylabs.slstudio.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXModel;
import org.lwjgl.opengl.GL11;

public class ModelRenderer {
    private static final PolyBuffer.Space UI_COLOR_SPACE = PolyBuffer.Space.SRGB8;

    public final Camera cam;
    protected final LXModel model;
    protected final ShaderProgram pointShader;
    protected final float[] glColorBuffer;
    protected final PolyBuffer lxColorBuffer;
    protected final VertexBufferObject positionVbo;
    protected final VertexBufferObject colorVbo;
    protected final LX lx;

    public ModelRenderer(LX lx, LXModel model) {
        this.model = model;
        this.lx = lx;

        String vert = Gdx.files.internal("vertex-330.glsl").readString();
        String frag = Gdx.files.internal("fragment-330.glsl").readString();
        pointShader = new ShaderProgram(vert, frag);
        if (!pointShader.isCompiled()) {
            throw new RuntimeException("shader compilation failed: " + pointShader.getLog());
        }

        int N = model.points.length;
        positionVbo = new VertexBufferObject(false, N, VertexAttribute.Position());
        updatePoints();
        model.addListener(m -> updatePoints());

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(model.cx, model.cy, model.zMin - model.rMax);
        cam.lookAt(model.cx, model.cy, model.cz);
        cam.near = 1f;
        cam.far = 10000f;
        cam.update();

        glColorBuffer = new float[4 * N];
        lxColorBuffer = new PolyBuffer(lx);
        colorVbo = new VertexBufferObject(false, N, VertexAttribute.ColorUnpacked());
    }

    private void updatePoints() {
        int N = model.points.length;
        float[] vertdata = new float[3 * N];
        for (int i = 0; i < N; i++) {
            vertdata[3 * i + 0] = model.points[i].x;
            vertdata[3 * i + 1] = model.points[i].y;
            vertdata[3 * i + 2] = model.points[i].z;
        }
        positionVbo.setVertices(vertdata, 0, 3 * N);
    }

    public void draw() {
        lx.engine.copyUIBuffer(lxColorBuffer, UI_COLOR_SPACE);
        int[] colors = (int[]) lxColorBuffer.getArray(UI_COLOR_SPACE);

        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glPointSize(2);

        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            glColorBuffer[4 * i + 0] = (float) ((0x00FF0000 & c) >> 16) / 255.f;
            glColorBuffer[4 * i + 1] = (float) ((0x0000FF00 & c) >> 8) / 255.f;
            glColorBuffer[4 * i + 2] = (float)  (0x000000FF & c) / 255.f;
            glColorBuffer[4 * i + 3] = 1.f;
        }

        pointShader.begin();
        pointShader.setUniformMatrix("u_mvp", cam.combined);

        pointShader.enableVertexAttribute("a_position");
        positionVbo.bind(pointShader);

        pointShader.enableVertexAttribute("a_color");
        colorVbo.bind(pointShader);
        colorVbo.setVertices(glColorBuffer, 0, model.points.length * 4);

        //Gdx.gl.glDrawArrays(GL30.GL_POINTS, 0, model.points.length);

        positionVbo.unbind(pointShader);
        colorVbo.unbind(pointShader);

        pointShader.disableVertexAttribute("a_color");
        pointShader.disableVertexAttribute("a_position");
        pointShader.end();

        Gdx.gl.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);

        GL11.glDisable(GL11.GL_POINT_SMOOTH);
    }

    public void dispose() {
        colorVbo.dispose();
        positionVbo.dispose();
    }
}
