package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.lwjgl.opengl.GL41;
import com.symmetrylabs.slstudio.ui.GraphicsAdapter;


public class GdxGraphicsAdapter implements GraphicsAdapter {
    public final ShapeRenderer renderer;

    public GdxGraphicsAdapter(ShapeRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void strokeWeight(float weight) {
        GL41.glLineWidth(weight);
    }

    @Override
    public void stroke(int r, int g, int b) {
        renderer.setColor((float) r / 255.f, (float) g / 255.f, (float) b / 255.f, 1.f);
    }

    @Override
    public void line(float x0, float y0, float z0, float x1, float y1, float z1) {
        renderer.line(x0, y0, z0, x1, y1, z1);
    }
}
