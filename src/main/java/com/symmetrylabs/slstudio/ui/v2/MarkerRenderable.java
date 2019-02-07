package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXModel;
import java.util.ArrayList;
import java.util.List;
import heronarts.lx.warp.LXWarp;
import heronarts.lx.LXEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.symmetrylabs.slstudio.ApplicationState;


public class MarkerRenderable implements RenderManager.Renderable {
    private final LX lx;

    public MarkerRenderable(LX lx) {
        this.lx = lx;
    }

    @Override
    public void draw(SLCamera cam) {
        GdxGraphicsAdapter pg = new GdxGraphicsAdapter(new ShapeRenderer());
        pg.renderer.setProjectionMatrix(cam.combined);
        pg.renderer.begin(ShapeRenderer.ShapeType.Line);

        for (LXChannel chan : lx.engine.getChannels()) {
            for (LXWarp warp : chan.getWarps()) {
                drawComponent(pg, warp);
            }
            for (LXPattern pat : chan.getPatterns()) {
                drawComponent(pg, pat);
            }
            for (LXEffect effect : chan.getEffects()) {
                drawComponent(pg, effect);
            }
        }

        pg.renderer.end();
    }

    @Override
    public void dispose() {
    }

    private void drawComponent(GdxGraphicsAdapter pg, LXComponent component) {
        if (component instanceof MarkerSource) {
            MarkerSource ms = (MarkerSource) component;
            for (Marker m : ms.getMarkers()) {
                pg.renderer.identity();
                try {
                    m.draw(pg);
                } catch (NotImplementedInV2Exception e) {
                    ApplicationState.setWarning(m.getClass().getSimpleName(), "marker type is not supported in UIv2");
                }
            }
        }
    }
}
