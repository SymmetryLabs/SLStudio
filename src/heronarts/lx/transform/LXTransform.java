/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.transform;

import processing.core.PVector;

import java.util.Stack;

/**
 * A transform is a matrix stack, quite similar to the OpenGL implementation.
 * This class can be used to push a point around in 3-d space. The matrix itself
 * is not directly exposed, but the x,y,z values are.
 */
public class LXTransform {
    
    private final LXMatrix.RotationMode rotationMode;
    
    private final Stack<LXMatrix> matrices;
    
    /**
     * Constructs a new transform
     */
    public LXTransform() {
        this(LXMatrix.RotationMode.RIGHT_HANDED);
    }
    
    public LXTransform(LXMatrix.RotationMode rotationMode) {
        this.rotationMode = rotationMode; 
        this.matrices = new Stack<LXMatrix>();
        this.matrices.push(new LXMatrix(rotationMode));        
    }
        
    public LXMatrix getMatrix() {
        return this.matrices.peek();
    }
    
    /**
     * Gets the current x, y, z of the transform as a vector
     * 
     * @return Vector of current position
     */
    public PVector vector() {
        LXMatrix m = getMatrix();
        return new PVector(m.m14, m.m24, m.m34);
    }
    
    /**
     * Gets the x value of the transform
     * 
     * @return x value of transform
     */
    public float x() {
        return getMatrix().m14; 
    }
    
    /**
     * Gets the y value of the transform
     * 
     * @return y value of transform
     */
    public float y() {
        return getMatrix().m24;
    }
    
    /**
     * Gets the z value of the transform
     * 
     * @return z value of transform
     */
    public float z() {
        return getMatrix().m34;
    }
    
    /**
     * Translates the point
     * 
     * @param tx x translation
     * @param ty y translation
     * @param tz z translation
     * @return this, for method chaning
     */
    public LXTransform translate(float tx, float ty, float tz) {
        getMatrix().translate(tx, ty, tz);
        return this;
    }
    
    /**
     * Rotates about the x axis
     * 
     * @param rx Degrees, in radians
     * @return this, for method chaining
     */
    public LXTransform rotateX(float rx) {
        getMatrix().rotateX(rx);
        return this;
    }
    
    public LXTransform rotateX(double rx) {
        return rotateX((float)rx);
    }
    
    /**
     * Rotates about the y axis
     * 
     * @param ry Degrees, in radians
     * @return this, for method chaining
     */
    public LXTransform rotateY(float ry) {
        getMatrix().rotateY(ry);
        return this;
    }
    
    public LXTransform rotateY(double ry) {
        return rotateY((float)ry);
    }
    
    /**
     * Rotates about the z axis
     * 
     * @param rz Degrees, in radians
     * @return this, for method chaining
     */
    public LXTransform rotateZ(float rz) {
        getMatrix().rotateZ(rz);
        return this;
    }
    
    public LXTransform rotateZ(double rz) {
        return rotateZ((float)rz);
    }
    
    /**
     * Pushes the matrix stack, future operations can be undone by pop()
     * 
     * @return this, for method chaining
     */
    public LXTransform push() {
        this.matrices.push(new LXMatrix(this.matrices.peek()));
        return this;
    }
    
    /**
     * Pops the matrix stack, to its previous state
     * 
     * @return this, for method chaining
     */
    public LXTransform pop() {
        this.matrices.pop();
        return this;
    }

}
