package com.symmetrylabs.slstudio.ui.v2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
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
import java.util.List;
import com.badlogic.gdx.math.Matrix4;

public class RenderManager {
    public interface Renderable {
        default void setDisplayProperties(int width, int height, float density) {}
        void draw(SLCamera camera);
        void dispose();

        default boolean isEnabled() {
            return true;
        }
    }

    public final SLCamera cam;
    public final ShaderProvider shaderProvider;
    protected final List<Renderable> renderables;

    public RenderManager(LX lx) {
        renderables = new ArrayList<>();
        shaderProvider = new ShaderProvider();

        printCapabilities();

        cam = new SLCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 67);
        cam.setPositionLH(lx.model.cx, lx.model.cy, lx.model.zMin - lx.model.rMax);
        cam.lookAtLH(lx.model.cx, lx.model.cy, lx.model.cz);
        cam.near = 1f;
        cam.far = 10000f;
        cam.update();
    }

    public void add(Renderable r) {
        renderables.add(r);
    }

    public void remove(Renderable r) {
        renderables.remove(r);
    }

    public void clear() {
        renderables.clear();
    }

    public void setDisplayProperties(int width, int height, float density) {
        for (Renderable r : renderables) {
            r.setDisplayProperties(width, height, density);
        }
    }

    public void draw() {
        GL41.glEnable(GL41.GL_DEPTH_TEST);
        /* set winding order to match left-handed-coordinates */
        GL41.glFrontFace(GL41.GL_CW);
        GL41.glDepthFunc(GL41.GL_LESS);
        for (Renderable r : renderables) {
            if (r.isEnabled()) {
                r.draw(cam);
            }
        }
        /* restore CCW winding to leave the render in a reasonable (cough cough) state */
        GL41.glFrontFace(GL41.GL_CCW);
        GL41.glDisable(GL41.GL_DEPTH_TEST);
    }

    public void dispose() {
        for (Renderable r : renderables) {
            r.dispose();
        }
        clear();
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

    public static class ShaderProvider extends BaseShaderProvider {
        public final DefaultShader.Config config;

        public ShaderProvider() {
            config = new DefaultShader.Config(
                Gdx.files.internal("gdx-vertex.glsl").readString(),
                Gdx.files.internal("gdx-fragment.glsl").readString());
        }

        @Override
        protected Shader createShader (final com.badlogic.gdx.graphics.g3d.Renderable renderable) {
            String prefix = DefaultShader.createPrefix(renderable, config);
            prefix = "#version 330 core\n\n" + prefix;
            return new DefaultShader(renderable, config, prefix, config.vertexShader, config.fragmentShader);
        }
    }
}
