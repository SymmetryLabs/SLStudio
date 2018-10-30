package com.symmetrylabs.slstudio.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import heronarts.lx.model.LXModel;

public class ModelRenderer {
    protected LXModel model;
    protected Mesh mesh;
    protected ShaderProgram pointShader;
    public final Camera cam;

    public ModelRenderer(LXModel model) {
        this.model = model;

        String vert = Gdx.files.internal("vertex.glsl").readString();
        String frag = Gdx.files.internal("fragment.glsl").readString();
        pointShader = new ShaderProgram(vert, frag);

        int N = model.points.length;
        float[] vertdata = new float[3 * N];
        for (int i = 0; i < N; i++) {
            vertdata[3 * i + 0] = model.points[i].x;
            vertdata[3 * i + 1] = model.points[i].y;
            vertdata[3 * i + 2] = model.points[i].z;
        }
        mesh = new Mesh(true, N, 0, VertexAttribute.Position());
        mesh.setVertices(vertdata);

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(model.cx, model.cy, model.zMin - model.rMax);
        cam.lookAt(model.cx, model.cy, model.cz);
        cam.near = 1f;
        cam.far = 10000f;
        cam.update();

    }

    public void draw() {
        pointShader.begin();
        pointShader.setUniformMatrix("u_mvp", cam.combined);
        mesh.render(pointShader, GL20.GL_POINTS);
        pointShader.end();
    }

    public void dispose() {
        mesh.dispose();
    }
}
