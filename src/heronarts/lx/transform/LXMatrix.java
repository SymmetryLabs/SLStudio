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

/**
 * A 4x4 matrix for 3-D transformations
 */
public class LXMatrix {
    
    public enum RotationMode {
        RIGHT_HANDED,
        LEFT_HANDED
    };
    
    private final RotationMode rotationMode;
    
    public float
        m11=1, m12=0, m13=0, m14=0,
        m21=0, m22=1, m23=0, m24=0,
        m31=0, m32=0, m33=1, m34=0,
        m41=0, m42=0, m43=0, m44=1;
    
    /**
     * Makes a new identity matrix.
     */
    public LXMatrix() {
        this(RotationMode.RIGHT_HANDED);
    }
    
    /**
     * Makes a new matrix with the given rotation mode
     * 
     * @param rotationMode Rotation mode
     */
    public LXMatrix(RotationMode rotationMode) {
        this.rotationMode = rotationMode; 
    }
    
    /**
     * Copies the existing matrix
     * 
     * @param m matrix
     */
    public LXMatrix(LXMatrix m) {
        m11 = m.m11; m12 = m.m12; m13 = m.m13; m14 = m.m14;
        m21 = m.m21; m22 = m.m22; m23 = m.m23; m24 = m.m24;
        m31 = m.m31; m32 = m.m32; m33 = m.m33; m34 = m.m34;
        m41 = m.m41; m42 = m.m42; m43 = m.m43; m44 = m.m44;
        this.rotationMode = m.rotationMode;
    }
    
    /**
     * Multiplies the matrix by another matrix
     * 
     * @param m
     */
    public void multiply(LXMatrix m) {
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
    
    public float x() {
        return m14;
    }
    
    public float y() {
        return m24;
    }
    
    public float z() {
        return m34;
    }
    
    public LXMatrix translate(float tx, float ty, float tz) {
        LXMatrix m = new LXMatrix();
        m.m14 = tx;
        m.m24 = ty;
        m.m34 = tz;
        multiply(m);
        return this;
    }
    
    public LXMatrix rotateX(float rx) {
        if (this.rotationMode == RotationMode.RIGHT_HANDED) {
            rx = -rx;
        }
        LXMatrix m = new LXMatrix();
        m.m22 = (float) Math.cos(rx);
        m.m23 = (float) -Math.sin(rx);
        m.m32 = -m.m23;
        m.m33 = m.m22;
        multiply(m);
        return this;
    }
    
    public LXMatrix rotateY(float ry) {
        if (this.rotationMode == RotationMode.LEFT_HANDED) {
            ry = -ry;
        }
        LXMatrix m = new LXMatrix();
        m.m11 = (float) Math.cos(ry);
        m.m13 = (float) -Math.sin(ry);
        m.m31 = -m.m13;
        m.m33 = m.m11;
        multiply(m);
        return this;
    }
    
    public LXMatrix rotateZ(float rz) {
        if (this.rotationMode == RotationMode.RIGHT_HANDED) {
            rz = -rz;
        }
        LXMatrix m = new LXMatrix();
        m.m11 = (float) Math.cos(rz);
        m.m12 = (float) -Math.sin(rz);
        m.m21 = -m.m12;
        m.m22 = m.m11;
        multiply(m);
        return this;
    }
}