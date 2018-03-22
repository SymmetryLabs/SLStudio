package com.symmetrylabs.layouts.dollywood;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import com.symmetrylabs.layouts.oslo.TreeModel;
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
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class UIButterflies extends UI3dComponent {

    private final P3LX lx;
    protected final PImage texImageUpper;
    protected final PImage texImageLower;
    private ButterflyShape shapeUpperLeft;
    private ButterflyShape shapeUpperRight;
    private ButterflyShape shapeLowerLeft;
    private ButterflyShape shapeLowerRight;

    private List<DollywoodModel.Butterfly> butterflies;
    private List<DollywoodModel.Wing> wings;

    public UIButterflies(P3LX lx, PApplet applet, List<DollywoodModel.Butterfly> butterflies) {
        this.lx = lx;
        this.butterflies = butterflies;
        this.wings = ((DollywoodModel)lx.model).getWings();
        this.texImageUpper = applet.loadImage("butterfly_wing_upper.png");
        this.texImageLower = applet.loadImage("butterfly_wing_lower.png");
    }

    // Uses PShape functionality to render with a faster shader using VBOs.
    // Only the color buffer is pushed to the GPU on each frame.
    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        // Upper left wings
        if (this.shapeUpperLeft == null) {
            List<DollywoodModel.Wing> wings = new ArrayList<>();
            for (DollywoodModel.Butterfly butterfly : butterflies) wings.add(butterfly.wings.get(0));
            this.shapeUpperLeft = new ButterflyShape(pg, texImageUpper, wings);
        }
        this.shapeUpperLeft.updateColors(pg, lx.getColors());
        pg.shape(this.shapeUpperLeft);

        // Upper right wings
        if (this.shapeUpperRight == null) {
            List<DollywoodModel.Wing> wings = new ArrayList<>();
            for (DollywoodModel.Butterfly butterfly : butterflies) wings.add(butterfly.wings.get(1));
            this.shapeUpperRight = new ButterflyShape(pg, texImageUpper, wings);
        }
        this.shapeUpperRight.updateColors(pg, lx.getColors());
        pg.shape(this.shapeUpperRight);

        // Lower left wings
        if (this.shapeLowerLeft == null) {
            List<DollywoodModel.Wing> wings = new ArrayList<>();
            for (DollywoodModel.Butterfly butterfly : butterflies) wings.add(butterfly.wings.get(2));
            this.shapeLowerLeft = new ButterflyShape(pg, texImageLower, wings);
        }
        this.shapeLowerLeft.updateColors(pg, lx.getColors());
        pg.shape(this.shapeLowerLeft);

        // Lower right wings
        if (this.shapeLowerRight == null) {
            List<DollywoodModel.Wing> wings = new ArrayList<>();
            for (DollywoodModel.Butterfly butterfly : butterflies) wings.add(butterfly.wings.get(3));
            this.shapeLowerRight = new ButterflyShape(pg, texImageLower, wings);
        }
        this.shapeLowerRight.updateColors(pg, lx.getColors());
        pg.shape(this.shapeLowerRight);
    }

    class ButterflyShape extends PShapeOpenGL {
        private final List<DollywoodModel.Wing> wings;
        private final List<LXPoint> points = new ArrayList<>();
        private final IntBuffer tintBuffer;
        private final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

        ButterflyShape(PGraphics pg, PImage textureImg, List<DollywoodModel.Wing> wings) {
            super((PGraphicsOpenGL) pg, PShape.GEOMETRY);
            this.wings = wings;
            for (DollywoodModel.Wing wing : wings) {
                for (LXPoint p : wing.points) {
                    this.points.add(p);
                }
            }

            set3D(true);
            setTexture(textureImg);
            setTextureMode(NORMAL);
            setStroke(false);
            setFill(false);
            beginShape(QUADS);
            for (DollywoodModel.Wing wing : wings) {
                vertex(wing.coords[0].x, wing.coords[0].y, wing.coords[0].z, 0, 1);
                vertex(wing.coords[1].x, wing.coords[1].y, wing.coords[1].z, 0, 0);
                vertex(wing.coords[2].x, wing.coords[2].y, wing.coords[2].z, 1, 0);
                vertex(wing.coords[3].x, wing.coords[3].y, wing.coords[3].z, 1, 1);
            }
            endShape(CLOSE);
            markForTessellation();
            updateTessellation();
            initBuffers();

            this.tintBuffer = ByteBuffer
            .allocateDirect(wings.size() * 4 * Integer.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();
        }

        void updateColors(PGraphics pg, int[] colors) {
            // This is hacky as fuck! But couldn't find a better way to do this.
            // This reaches inside the PShapeOpenGL guts and updates ONLY the
            // vertex color buffer object with new data on each rendering pass.
            // ^ from Mark's original Tenere implementation
            this.tintBuffer.rewind();
            if (BIG_ENDIAN) {
                for (DollywoodModel.Wing wing : wings) {
                    int index = wing.points[wing.points.length/2].index;
                    int nativeARGB = (colors[index] >>> 24) | (colors[index] << 8);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                }
            } else {
                for (DollywoodModel.Wing wing : wings) {
                    int index = wing.points[wing.points.length/2].index;
                    int rb = colors[index] & 0x00ff00ff;
                    int nativeARGB = (colors[index] & 0xff00ff00) | (rb << 16) | (rb >> 16);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                    this.tintBuffer.put(nativeARGB);
                }
            }
            this.tintBuffer.position(0);
            pgl.bindBuffer(PGL.ARRAY_BUFFER, bufPolyColor.glId);
            pgl.bufferData(PGL.ARRAY_BUFFER, wings.size() * 4 * Integer.SIZE/8, this.tintBuffer, PGL.STREAM_DRAW);
            pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);
        }
    }
}
