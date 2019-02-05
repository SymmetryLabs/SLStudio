package com.symmetrylabs.slstudio.ui.v2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.lwjgl.opengl.GL41;
import javax.imageio.ImageIO;
import java.io.IOException;
import com.symmetrylabs.slstudio.ApplicationState;

public class TextureManager {
    private static TextureManager INSTANCE = new TextureManager();

    public static void load(String resourcePath) {
        INSTANCE.loadImpl(resourcePath);
    }

    public static void draw(String key) {
        INSTANCE.drawImpl(key, -1, -1, 0, 0, 1, 1);
    }

    public static void draw(String key, float w, float h) {
        INSTANCE.drawImpl(key, w, h, 0, 0, 1, 1);
    }

    public static void draw(String key, float u0, float v0, float u1, float v1) {
        INSTANCE.drawImpl(key, -1, -1, u0, v0, u1, v1);
    }

    public static void draw(String key, float w, float h, float u0, float v0, float u1, float v1) {
        INSTANCE.drawImpl(key, w, h, u0, v0, u1, v1);
    }

    public static boolean button(String key) {
        return INSTANCE.buttonImpl(key, -1, -1, 0, 0, 1, 1);
    }

    public static boolean button(String key, float w, float h) {
        return INSTANCE.buttonImpl(key, w, h, 0, 0, 1, 1);
    }

    public static boolean button(String key, float u0, float v0, float u1, float v1) {
        return INSTANCE.buttonImpl(key, -1, -1, u0, v0, u1, v1);
    }

    public static boolean button(String key, float w, float h, float u0, float v0, float u1, float v1) {
        return INSTANCE.buttonImpl(key, w, h, u0, v0, u1, v1);
    }

    private static class Texture {
        int id, w, h;
    }

    private final Map<String, Texture> textures;

    protected TextureManager() {
        textures = new HashMap<>();
    }

    protected void loadImpl(String resourcePath) {
        /* early-exit so that we don't read the image unnecessarily; this should
           be cheap enough to call on every frame. */
        if (textures.containsKey(resourcePath)) {
            return;
        }
        try {
            loadImpl(resourcePath, ImageIO.read(getClass().getClassLoader().getResource(resourcePath)));
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationState.setWarning("TextureManager/" + resourcePath, "texture failed to load from disk");
        }
    }

    protected synchronized void loadImpl(String key, BufferedImage img) {
        if (textures.containsKey(key)) {
            return;
        }

        Texture t = new Texture();
        t.w = img.getWidth();
        t.h = img.getHeight();

        int[] pixels = new int[t.w * t.h];
        img.getRGB(0, 0, t.w, t.h, pixels, 0, t.w);

        ByteBuffer buf = ByteBuffer.allocateDirect(t.w * t.h * 4);
        for (int j = 0; j < t.h; j++) {
            for (int i = 0; i < t.w; i++) {
                int pixel = pixels[j * t.w + i];
                buf.put((byte) ((pixel >> 16) & 0xFF));
                buf.put((byte) ((pixel >> 8) & 0xFF));
                buf.put((byte) (pixel & 0xFF));
                buf.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buf.flip();

        t.id = GL41.glGenTextures();
        GL41.glBindTexture(GL41.GL_TEXTURE_2D, t.id);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MIN_FILTER, GL41.GL_LINEAR);
        GL41.glTexParameteri(GL41.GL_TEXTURE_2D, GL41.GL_TEXTURE_MAG_FILTER, GL41.GL_LINEAR);
        GL41.glPixelStorei(GL41.GL_UNPACK_ROW_LENGTH, 0);
        GL41.glTexImage2D(GL41.GL_TEXTURE_2D, 0, GL41.GL_RGBA8, t.w, t.h, 0, GL41.GL_RGBA, GL41.GL_UNSIGNED_BYTE, buf);
        textures.put(key, t);
    }

    protected synchronized void drawImpl(String key, float w, float h, float u0, float v0, float u1, float v1) {
        if (!textures.containsKey(key)) {
            ApplicationState.setWarning("TextureManager/" + key, "texture not loaded");
            return;
        }
        Texture t = textures.get(key);
        w = w == -1 ? t.w : w;
        h = h == -1 ? t.h : h;
        UI.image(t.id, w, h, u0, v0, u1, v1);
    }

    protected synchronized boolean buttonImpl(String key, float w, float h, float u0, float v0, float u1, float v1) {
        if (!textures.containsKey(key)) {
            ApplicationState.setWarning("TextureManager/" + key, "texture not loaded");
            /* best-effort: stick the key on a text button and show that instead */
            return UI.button(key);
        }
        Texture t = textures.get(key);
        w = w == -1 ? t.w : w;
        h = h == -1 ? t.h : h;
        return UI.imageButton(t.id, w, h, u0, v0, u1, v1);
    }

    protected synchronized void releaseImpl() {
        for (Texture t : textures.values()) {
            GL41.glDeleteTextures(t.id);
        }
        textures.clear();
    }
}
