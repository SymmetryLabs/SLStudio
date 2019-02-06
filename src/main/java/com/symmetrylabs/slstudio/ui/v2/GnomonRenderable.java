package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;
import heronarts.lx.model.LXModel;


public class GnomonRenderable implements RenderManager.Renderable {
    private ModelBatch batch;
    private Model origin;
    private Model arrowX;
    private Model arrowY;
    private Model arrowZ;
    private List<ModelInstance> instances;
    private Environment environment;
    boolean visible;

    public GnomonRenderable(LXModel m) {
        ModelBuilder modelBuilder = new ModelBuilder();

        float d = 100f;

        origin = modelBuilder.createBox(
            5f, 5f, 5f,
            new Material(ColorAttribute.createDiffuse(Color.WHITE)),
            Usage.Position | Usage.Normal);
        arrowX = modelBuilder.createArrow(
            new Vector3(0f, 0f, 0f), new Vector3(d, 0f, 0f),
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            Usage.Position | Usage.Normal);
        arrowY = modelBuilder.createArrow(
            new Vector3(0f, 0f, 0f), new Vector3(0f, d, 0f),
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            Usage.Position | Usage.Normal);
        arrowZ = modelBuilder.createArrow(
            new Vector3(0f, 0f, 0f), new Vector3(0f, 0f, d),
            new Material(ColorAttribute.createDiffuse(Color.BLUE)),
            Usage.Position | Usage.Normal);

        instances = new ArrayList<>();
        instances.add(new ModelInstance(origin));
        instances.add(new ModelInstance(arrowX));
        instances.add(new ModelInstance(arrowY));
        instances.add(new ModelInstance(arrowZ));

        batch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    @Override
    public void draw(SLCamera cam) {
        if (!visible) {
            return;
        }
        batch.begin(cam);
        for (ModelInstance instance : instances) {
            batch.render(instance, environment);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        origin.dispose();
        arrowX.dispose();
        arrowY.dispose();
        arrowZ.dispose();
    }
}
