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
        
    private class Matrix {
        
        private float
            m11=1, m12=0, m13=0, m14=0,
            m21=0, m22=1, m23=0, m24=0,
            m31=0, m32=0, m33=1, m34=0,
            m41=0, m42=0, m43=0, m44=1;
        
        private Matrix() {}
        
        private Matrix(Matrix m) {
            m11 = m.m11; m12 = m.m12; m13 = m.m13; m14 = m.m14;
            m21 = m.m21; m22 = m.m22; m23 = m.m23; m24 = m.m24;
            m31 = m.m31; m32 = m.m32; m33 = m.m33; m34 = m.m34;
            m41 = m.m41; m42 = m.m42; m43 = m.m43; m44 = m.m44;
        }
        
        private void multiply(Matrix m) {
            float a11 = m11*m.m11 + m12*m.m21 + m13*m.m31 + m14*m.m41;
            float a12 = m11*m.m12 + m12*m.m22 + m13*m.m32 + m14*m.m42;
            float a13 = m11*m.m13 + m12*m.m23 + m13*m.m33 + m14*m.m43;
            float a14 = m11*m.m14 + m12*m.m24 + m13*m.m34 + m14*m.m44;
            
            float a21 = m21*m.m11 + m22*m.m21 + m23*m.m31 + m24*m.m41;
            float a22 = m21*m.m12 + m22*m.m22 + m23*m.m32 + m24*m.m42;
            float a23 = m21*m.m13 + m22*m.m23 + m23*m.m33 + m24*m.m43;
            float a24 = m21*m.m14 + m22*m.m24 + m23*m.m34 + m24*m.m44;
            
            float a31 = m31*m.m11 + m32*m.m21 + m33*m.m31 + m34*m.m41;
            float a32 = m31*m.m12 + m32*m.m22 + m33*m.m32 + m34*m.m42;
            float a33 = m31*m.m13 + m32*m.m23 + m33*m.m33 + m34*m.m43;
            float a34 = m31*m.m14 + m32*m.m24 + m33*m.m34 + m34*m.m44;        

            float a41 = m41*m.m11 + m42*m.m21 + m43*m.m31 + m44*m.m41;
            float a42 = m41*m.m12 + m42*m.m22 + m43*m.m32 + m44*m.m42;
            float a43 = m41*m.m13 + m42*m.m23 + m43*m.m33 + m44*m.m43;
            float a44 = m41*m.m14 + m42*m.m24 + m43*m.m34 + m44*m.m44;        
        
            m11 = a11; m12 = a12; m13 = a13; m14 = a14;
            m21 = a21; m22 = a22; m23 = a23; m24 = a24;
            m31 = a31; m32 = a32; m33 = a33; m34 = a34;
            m41 = a41; m42 = a42; m43 = a43; m44 = a44;
        }
    }
    
    private final Stack<Matrix> matrices;
    
    /**
     * Constructs a new transform
     */
    public LXTransform() {
        this.matrices = new Stack<Matrix>();
        this.matrices.push(new Matrix());
    }
    
    private Matrix matrix() {
        return matrices.peek();
    }
    
    /**
     * Gets the current x, y, z of the transform as a vector
     * 
     * @return Vector of current position
     */
    public PVector vector() {
        Matrix m = matrix();
        return new PVector(m.m14, m.m24, m.m34);
    }
    
    /**
     * Gets the x value of the transform
     * 
     * @return x value of transform
     */
    public float x() {
        return matrix().m14; 
    }
    
    /**
     * Gets the y value of the transform
     * 
     * @return y value of transform
     */
    public float y() {
        return matrix().m24;
    }
    
    /**
     * Gets the z value of the transform
     * 
     * @return z value of transform
     */
    public float z() {
        return matrix().m34;
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
        Matrix m = new Matrix();
        m.m14 = tx;
        m.m24 = ty;
        m.m34 = tz;
        matrix().multiply(m);
        return this;
    }
    
    /**
     * Rotates about the x axis
     * 
     * @param rx Degrees, in radians
     * @return this, for method chaining
     */
    public LXTransform rotateX(float rx) {
        Matrix m = new Matrix();
        m.m22 = (float) Math.cos(-rx);
        m.m23 = (float) -Math.sin(-rx);
        m.m32 = -m.m23;
        m.m33 = m.m22;
        matrix().multiply(m);
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
    public LXTransform rotateY(float rx) {
        Matrix m = new Matrix();
        m.m11 = (float) Math.cos(rx);
        m.m13 = (float) -Math.sin(rx);
        m.m31 = -m.m13;
        m.m33 = m.m11;
        matrix().multiply(m);
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
    public LXTransform rotateZ(float rx) {
        Matrix m = new Matrix();
        m.m11 = (float) Math.cos(-rx);
        m.m12 = (float) -Math.sin(-rx);
        m.m21 = -m.m12;
        m.m22 = m.m11;
        matrix().multiply(m);
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
        this.matrices.push(new Matrix(this.matrices.peek()));
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
