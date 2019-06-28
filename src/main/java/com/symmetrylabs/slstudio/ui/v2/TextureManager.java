package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.slstudio.ApplicationState;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL41;

/**
 * Loads textures into video memory from image files and manages texture handles.
 * <p>
 * Texture handles in slimgui are all integers that correspond directly to GL texture
 * handles. This class keys textures by resource ID instead, and keeps track of the
 * integer handles and size of the associated texture. It also provides an implementation
 * for loading compressed images (PNGs, JPEGs, etc) into uncompressed bitmap textures.
 */
public class TextureManager {
    private static TextureManager INSTANCE = new TextureManager();

    /**
     * Load a texture from an image resource
     *
     * @param resourcePath the path to the resource in the SL classpath. If the texture has already been loaded, this is a very fast no-op.
     */
    public static void load(String resourcePath) {
        INSTANCE.loadImpl(resourcePath);
    }

    /**
     * Draws the entirety of a loaded texture at its native size
     *
     * @param resourcePath the path of the texture to draw. This texture must have been loaded already by a call to {@link this.load(String)}.
     */
    public static void draw(String resourcePath) {
        INSTANCE.drawImpl(resourcePath, -1, -1, 0, 0, 1, 1);
    }

    /**
     * Draws the entirety of a loaded texture at a specific size
     *
     * @param resourcePath the path of the texture to draw. This texture must have been loaded already by a call to {@link this.load(String)}.
     * @param w            the width at which to draw the texture
     * @param h            the height at which to draw the texture
     */
    public static void draw(String resourcePath, float w, float h) {
        INSTANCE.drawImpl(resourcePath, w, h, 0, 0, 1, 1);
    }

    /**
     * Draws a subsection of a texture at its native size
     *
     * @param resourcePath the path of the texture to draw. This texture must have been loaded already by a call to {@link this.load(String)}.
     * @param u0           the u-coordinate (ranging from 0 to 1) of the left edge of the clip rectangle
     * @param u1           the u-coordinate (ranging from 0 to 1) of the right edge of the clip rectangle
     * @param v0           the v-coordinate (ranging from 0 to 1) of the top edge of the clip rectangle
     * @param v1           the v-coordinate (ranging from 0 to 1) of the bottom edge of the clip rectangle
     */
    public static void draw(String resourcePath, float u0, float v0, float u1, float v1) {
        INSTANCE.drawImpl(resourcePath, -1, -1, u0, v0, u1, v1);
    }

    /**
     * Draws a subsection of a texture at a specific size
     *
     * @param resourcePath the path of the texture to draw. This texture must have been loaded already by a call to {@link this.load(String)}.
     * @param w            the width at which to draw the texture
     * @param h            the height at which to draw the texture
     * @param u0           the u-coordinate (ranging from 0 to 1) of the left edge of the clip rectangle
     * @param u1           the u-coordinate (ranging from 0 to 1) of the right edge of the clip rectangle
     * @param v0           the v-coordinate (ranging from 0 to 1) of the top edge of the clip rectangle
     * @param v1           the v-coordinate (ranging from 0 to 1) of the bottom edge of the clip rectangle
     */
    public static void draw(String resourcePath, float w, float h, float u0, float v0, float u1, float v1) {
        INSTANCE.drawImpl(resourcePath, w, h, u0, v0, u1, v1);
    }

    /**
     * Draw a button with an image on it, and return whether the button is pressed
     *
     * @param resourcePath the texture to draw on the button
     * @return true if the button was clicked in this frame
     */
    public static boolean button(String resourcePath) {
        return INSTANCE.buttonImpl(resourcePath, -1, -1, 0, 0, 1, 1);
    }

    /**
     * Draw a button with an image on it, and return whether the button is pressed
     *
     * @param resourcePath the texture to draw on the button
     * @param w            the width of the button
     * @param h            the height of the button
     * @return true if the button was clicked in this frame
     */
    public static boolean button(String resourcePath, float w, float h) {
        return INSTANCE.buttonImpl(resourcePath, w, h, 0, 0, 1, 1);
    }

    /**
     * Draw a button with a subsection of a texture drawn on it, and return whether the button is pressed
     *
     * @param resourcePath the texture to draw on the button
     * @param u0           the u-coordinate (ranging from 0 to 1) of the left edge of the clip rectangle
     * @param u1           the u-coordinate (ranging from 0 to 1) of the right edge of the clip rectangle
     * @param v0           the v-coordinate (ranging from 0 to 1) of the top edge of the clip rectangle
     * @param v1           the v-coordinate (ranging from 0 to 1) of the bottom edge of the clip rectangle
     * @return true if the button was clicked in this frame
     */
    public static boolean button(String resourcePath, float u0, float v0, float u1, float v1) {
        return INSTANCE.buttonImpl(resourcePath, -1, -1, u0, v0, u1, v1);
    }

    /**
     * Draw a button with a subsection of a texture drawn on it, and return whether the button is pressed
     *
     * @param resourcePath the texture to draw on the button
     * @param w            the width of the button
     * @param h            the height of the button
     * @param u0           the u-coordinate (ranging from 0 to 1) of the left edge of the clip rectangle
     * @param u1           the u-coordinate (ranging from 0 to 1) of the right edge of the clip rectangle
     * @param v0           the v-coordinate (ranging from 0 to 1) of the top edge of the clip rectangle
     * @param v1           the v-coordinate (ranging from 0 to 1) of the bottom edge of the clip rectangle
     * @return true if the button was clicked in this frame
     */
    public static boolean button(String resourcePath, float w, float h, float u0, float v0, float u1, float v1) {
        return INSTANCE.buttonImpl(resourcePath, w, h, u0, v0, u1, v1);
    }

    private static class Texture {
        int id, w, h;
    }

    private final Map<String, Texture> textures;

    private TextureManager() {
        textures = new HashMap<>();
    }

    private synchronized void loadImpl(String resourcePath) {
        /* early-exit so that we don't read the image unnecessarily; this should
           be cheap enough to call on every frame. */
        if (textures.containsKey(resourcePath)) {
            return;
        }

        BufferedImage img;
        URL imageUrl = getClass().getClassLoader().getResource(resourcePath);
        if (imageUrl == null) {
            ApplicationState.setWarning("TextureManager/" + resourcePath, "texture resource does not exist");
            return;
        }

        try {
            img = ImageIO.read(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationState.setWarning("TextureManager/" + resourcePath, "texture failed to load from disk");
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
        textures.put(resourcePath, t);
    }

    private synchronized void drawImpl(String resourcePath, float w, float h, float u0, float v0, float u1, float v1) {
        if (!textures.containsKey(resourcePath)) {
            ApplicationState.setWarning("TextureManager/" + resourcePath, "texture not loaded");
            return;
        }
        Texture t = textures.get(resourcePath);
        w = w == -1 ? t.w : w;
        h = h == -1 ? t.h : h;
        UI.image(t.id, w, h, u0, v0, u1, v1);
    }

    private synchronized boolean buttonImpl(String resourcePath, float w, float h, float u0, float v0, float u1, float v1) {
        if (!textures.containsKey(resourcePath)) {
            ApplicationState.setWarning("TextureManager/" + resourcePath, "texture not loaded");
            /* our best-effort is pretty low-effort: stick the resourcePath on a text button and show that instead */
            return UI.button(resourcePath);
        }
        Texture t = textures.get(resourcePath);
        w = w == -1 ? t.w : w;
        h = h == -1 ? t.h : h;
        return UI.imageButton(t.id, w, h, u0, v0, u1, v1);
    }

    private synchronized void releaseImpl() {
        for (Texture t : textures.values()) {
            GL41.glDeleteTextures(t.id);
        }
        textures.clear();
    }
}
