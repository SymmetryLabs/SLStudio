/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL2;
import processing.core.PGraphics;
import processing.opengl.PGL;
import processing.opengl.PJOGL;
import processing.opengl.PShader;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;

/**
 * Same as a UIPointCloud, except this version uses GLSL to draw
 * the points with a vertex shader.
 */
public class UIGLPointCloud extends UIPointCloud {

    private final PShader shader;
    private final FloatBuffer vertexData;
    private int vertexBufferObjectName;
    private boolean alphaTestEnabled = false;

    private static final float[] NO_ATTENUATION = { 1, 0, 0 };

    /**
     * Point cloud for everything in the LX instance
     *
     * @param lx LX instance
     */
    public UIGLPointCloud(P3LX lx) {
        this(lx, lx.model);
    }

    /**
     * Point cloud for points in the specified model
     *
     * @param lx LX instance
     * @param model Model to draw
     */
    public UIGLPointCloud(P3LX lx, LXModel model) {
        super(lx, model);

        // Load shader
        this.shader = lx.applet.loadShader("frag.glsl", "vert.glsl");

        // Create a buffer for vertex data
        this.vertexData = ByteBuffer
            .allocateDirect(model.size * 7 * Float.SIZE/8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

        // Put all the points into the buffer
        this.vertexData.rewind();
        for (LXPoint point : model.points) {
            // Each point has 7 floats, XYZRGBA
            this.vertexData.put(point.x);
            this.vertexData.put(point.y);
            this.vertexData.put(point.z);
            this.vertexData.put(0f);
            this.vertexData.put(0f);
            this.vertexData.put(0f);
            this.vertexData.put(1f);
        }
        this.vertexData.position(0);

        // Generate a buffer binding
        IntBuffer resultBuffer = ByteBuffer
            .allocateDirect(1 * Integer.SIZE/8)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

        PGL pgl = this.lx.applet.beginPGL();
        pgl.genBuffers(1, resultBuffer); // Generates a buffer, places its id in resultBuffer[0]
        this.vertexBufferObjectName = resultBuffer.get(0); // Grab our buffer name
        this.lx.applet.endPGL();
    }

    /**
     * Enable alpha testing for dense point clouds to minimize some forms of
     * visible billboard aliasing across overlapping points;
     *
     * @param alphaTestEnabled Whether alpha test enabled
     * @return this
     */
    public UIGLPointCloud setAlphaTestEnabled(boolean alphaTestEnabled) {
        this.alphaTestEnabled = alphaTestEnabled;
        return this;
    }

    @Override
    protected void onDraw(UI ui, PGraphics pg) {
        int[] colors = this.lx.getColors();

        // Put our new colors in the vertex data
        int i = 0;
        for (LXPoint p : this.model.points) {
            int c = colors[p.index];
            this.vertexData.put(7*i + 3, (0xff & (c >> 16)) / 255f); // R
            this.vertexData.put(7*i + 4, (0xff & (c >> 8)) / 255f); // G
            this.vertexData.put(7*i + 5, (0xff & (c)) / 255f); // B
            ++i;
        }

        // Get PGL context
        PGL pgl = pg.beginPGL();

        // Bind to our vertex buffer object, place the new color data
        pgl.bindBuffer(PGL.ARRAY_BUFFER, this.vertexBufferObjectName);
        pgl.bufferData(PGL.ARRAY_BUFFER, this.model.size * 7 * Float.SIZE/8, this.vertexData, PGL.DYNAMIC_DRAW);

        // Set up shader
        this.shader.bind();
        int vertexLocation = pgl.getAttribLocation(this.shader.glProgram, "vertex");
        int colorLocation = pgl.getAttribLocation(this.shader.glProgram, "color");
        pgl.enableVertexAttribArray(vertexLocation);
        pgl.enableVertexAttribArray(colorLocation);
        pgl.vertexAttribPointer(vertexLocation, 3, PGL.FLOAT, false, 7 * Float.SIZE/8, 0);
        pgl.vertexAttribPointer(colorLocation, 4, PGL.FLOAT, false, 7 * Float.SIZE/8, 3 * Float.SIZE/8);

        this.shader.set("pointSize", this.pointSize);
        if (this.pointSizeAttenuation != null) {
            this.shader.set("attenuation", this.pointSizeAttenuation, 3);
        } else {
            this.shader.set("attenuation", NO_ATTENUATION, 3);
        }

        // GL2 properties
        GL2 gl2 = (com.jogamp.opengl.GL2) ((PJOGL)pgl).gl;
        gl2.glEnable(GL2.GL_POINT_SPRITE);
        gl2.glEnable(GL2.GL_POINT_SMOOTH);
        gl2.glDisable(GL2.GL_TEXTURE_2D);
        gl2.glPointSize(this.pointSize);
        gl2.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);
        if (this.alphaTestEnabled) {
            gl2.glEnable(GL2.GL_ALPHA_TEST);
            gl2.glAlphaFunc(GL2.GL_NOTEQUAL, GL2.GL_ZERO);
        }

        // Draw the arrays
        pgl.drawArrays(PGL.POINTS, 0, this.model.size);

        // Unbind
        if (this.alphaTestEnabled) {
            gl2.glDisable(GL2.GL_ALPHA_TEST);
        }
        pgl.disableVertexAttribArray(vertexLocation);
        pgl.disableVertexAttribArray(colorLocation);
        this.shader.unbind();
        pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);

        // Done!
        pg.endPGL();
    }

}
