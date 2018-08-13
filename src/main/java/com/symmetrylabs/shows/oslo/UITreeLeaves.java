package com.symmetrylabs.shows.oslo;

import com.symmetrylabs.shows.oslo.TreeModel;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShapeOpenGL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class UITreeLeaves extends UI3dComponent {

    private final P3LX lx;
    private final TreeModel tree;
    protected final PImage texImage;
    private LeafShape shape;

    public UITreeLeaves(P3LX lx, PApplet applet, TreeModel tree) {
        this.lx = lx;
        this.tree = tree;
        this.texImage = applet.loadImage("leaf.png");
    }

    // Uses PShape functionality to render with a faster shader using VBOs.
    // Only the color buffer is pushed to the GPU on each frame.
    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        if (this.shape == null) {
            this.shape = new LeafShape(pg);
        }
        this.shape.updateColors(pg, lx.getColors());
        pg.shape(this.shape);
    }

    class LeafShape extends PShapeOpenGL {

        private final IntBuffer tintBuffer;
        private final boolean BIG_ENDIAN =
            ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

        LeafShape(PGraphics pg) {
            super((PGraphicsOpenGL) pg, PShape.GEOMETRY);
            set3D(true);

            setTexture(texImage);
            setTextureMode(NORMAL);
            setStroke(false);
            setFill(false);
            beginShape(QUADS);
            for (TreeModel.Leaf leaf : tree.leaves) {
                vertex(leaf.coords[0].x, leaf.coords[0].y, leaf.coords[0].z, 0, 1);
                vertex(leaf.coords[1].x, leaf.coords[1].y, leaf.coords[1].z, 0, 0);
                vertex(leaf.coords[2].x, leaf.coords[2].y, leaf.coords[2].z, 1, 0);
                vertex(leaf.coords[3].x, leaf.coords[3].y, leaf.coords[3].z, 1, 1);
            }
            endShape(CLOSE);
            markForTessellation();
            updateTessellation();
            initBuffers();

            this.tintBuffer = ByteBuffer
            .allocateDirect(tree.leaves.size() * 4 * Integer.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();
        }

        void updateColors(PGraphics pg, int[] colors) {
            // This is hacky as fuck! But couldn't find a better way to do this.
            // This reaches inside the PShapeOpenGL guts and updates ONLY the
            // vertex color buffer object with new data on each rendering pass.

            this.tintBuffer.rewind();
            if (BIG_ENDIAN) {
                for (int i = 0; i < colors.length; i += TreeModel.Leaf.NUM_LEDS) {
                    int nativeARGB = (colors[i] >>> 24) | (colors[i] << 8);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                }
            } else {
                for (int i = 0; i < colors.length; i += TreeModel.Leaf.NUM_LEDS) {
                    int rb = colors[i] & 0x00ff00ff;
                    int nativeARGB = (colors[i] & 0xff00ff00) | (rb << 16) | (rb >> 16);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                }
            }
            this.tintBuffer.position(0);
            pgl.bindBuffer(PGL.ARRAY_BUFFER, bufPolyColor.glId);
            pgl.bufferData(PGL.ARRAY_BUFFER, tree.leaves.size() * 4 * Integer.SIZE/8, this.tintBuffer, PGL.STREAM_DRAW);
            pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);
        }
    }
}
