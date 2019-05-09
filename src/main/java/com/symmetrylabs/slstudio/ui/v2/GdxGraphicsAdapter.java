package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.lwjgl.opengl.GL41;
import com.symmetrylabs.slstudio.ui.GraphicsAdapter;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;


public class GdxGraphicsAdapter implements GraphicsAdapter {
    public final ShapeRenderer renderer;
    public final BitmapFont font;
    public final SpriteBatch textBatch;
    protected SLCamera camera;

    public GdxGraphicsAdapter(ShapeRenderer renderer) {
        this.renderer = renderer;

        font = new BitmapFont(
            Gdx.files.internal("fonts/InputSansGdxSDF.fnt"),
            Gdx.files.internal("fonts/InputSansGdxSDF.png"),
            false);

        /* mega-hacks for MacOS's failure to create a SpriteBatch */
        SpriteBatch b = null;
        try {
            b = new SpriteBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        textBatch = b;
    }

    public SLCamera getCamera() {
        return camera;
    }

    void setCamera(SLCamera camera) {
        this.camera = camera;
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
