package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.symmetrylabs.util.IterationUtils;
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
import org.lwjgl.opengl.GL41;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;


public class MarkerRenderable implements RenderManager.Renderable {
    private final LX lx;
    private final GdxGraphicsAdapter pg;
    boolean visible = true;

    public MarkerRenderable(LX lx) {
        this.lx = lx;
        pg = new GdxGraphicsAdapter(new GL4ShapeRenderer());
    }

    @Override
    public void draw(SLCamera cam) {
        if (!visible) {
            return;
        }
        GL41.glEnable(GL41.GL_LINE_SMOOTH);
        pg.setCamera(cam);
        pg.textBatch.begin();
        pg.textBatch.setProjectionMatrix(cam.combined);
        IterationUtils.forEachIgnoreModification(lx.engine.getFocusedLook().channels, chan -> {
            IterationUtils.forEachIgnoreModification(chan.getWarps(), warp -> drawText(pg, warp));
            IterationUtils.forEachIgnoreModification(chan.getPatterns(), pattern -> drawText(pg, pattern));
            IterationUtils.forEachIgnoreModification(chan.getEffects(), effect -> drawText(pg, effect));
        });
        pg.textBatch.end();

        pg.renderer.setProjectionMatrix(cam.combined);
        pg.renderer.begin(ShapeRenderer.ShapeType.Line);
        IterationUtils.forEachIgnoreModification(lx.engine.getFocusedLook().channels, chan -> {
            IterationUtils.forEachIgnoreModification(chan.getWarps(), warp -> drawLines(pg, warp));
            IterationUtils.forEachIgnoreModification(chan.getPatterns(), pattern -> drawLines(pg, pattern));
            IterationUtils.forEachIgnoreModification(chan.getEffects(), effect -> drawLines(pg, effect));
        });
        pg.renderer.end();

        GL41.glDisable(GL41.GL_LINE_SMOOTH);
    }

    @Override
    public void dispose() {
    }

    private void drawText(GdxGraphicsAdapter pg, LXComponent component) {
        if (component instanceof MarkerSource) {
            MarkerSource ms = (MarkerSource) component;
            ms.drawTextMarkers(pg);
        }
    }

    private void drawLines(GdxGraphicsAdapter pg, LXComponent component) {
        if (component instanceof MarkerSource) {
            MarkerSource ms = (MarkerSource) component;
            if (ms.drawLineMarkers(pg)) {
                return;
            }
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

    public static class GL4ShapeRenderer extends ShapeRenderer {
        public GL4ShapeRenderer() {
            super(10000, createDefaultShader());
        }
    }

    static private String createVertexShader() {
        String shader = "#version 330 core\n";

        shader += "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n";

        shader += "uniform mat4 u_projModelView;\n";
        shader += "out vec4 v_col;\n";

        shader += "void main() {\n" + "   gl_Position = u_projModelView * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "   v_col = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n";
        shader += "   gl_PointSize = 1.0;\n";
        shader += "}\n";
        return shader;
    }

    static private String createFragmentShader() {
        String shader = "#version 330 core\n";
        shader += "#ifdef GL_ES\nprecision mediump float;\n#endif\n";
        shader += "in vec4 v_col;\n";
        shader += "out vec4 FragColor;\n";
        shader += "void main() {\n   FragColor = v_col;\n}";
        return shader;
    }

    /** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
    static public ShaderProgram createDefaultShader() {
        String vertexShader = createVertexShader();
        String fragmentShader = createFragmentShader();
        ShaderProgram program = new ShaderProgram(vertexShader, fragmentShader);
        if (!program.isCompiled()) {
            System.out.println("VERTEX:");
            System.out.println(vertexShader);
            System.out.println("FRAGMENT:");
            System.out.println(fragmentShader);
            throw new RuntimeException("marker shader compilation failed: " + program.getLog());
        }
        return program;
    }
}
