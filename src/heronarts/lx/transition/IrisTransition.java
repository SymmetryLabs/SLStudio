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

package heronarts.lx.transition;


import processing.core.PApplet;
import processing.core.PConstants;

import heronarts.lx.HeronLX;

import java.lang.Math;

/**
 * An IrisTransition moves between content by opening or
 * closing a window to the center of the frame. Different
 * shapes are supported
 */
public class IrisTransition extends LXTransition {
    
    /**
     * Direction of the transition
     */
    public enum Direction {
        /**
         * New content opens out from the center
         */
        OUTWARD,
        
        /**
         * New content closes into the center, from outside
         */
        INWARD
    };
    
    /**
     * Shape of the transition
     */
    public enum Shape {
        /**
         * Transition is a circle equidistant from the center point
         */
        RADIAL,
        
        /**
         * Transition is a rectangle that spreads out proportionally
         */
        SQUARE
    };
    
    private final Direction direction; 
    private final Shape shape;
    private double depth;
    
    /**
     * Constructs a new default outward radial transition
     * 
     * @param lx
     */
    public IrisTransition(HeronLX lx) {
        this(lx, Shape.RADIAL);
    }
    
    /**
     * Constructs an OUTWARD transition of the given shape
     * 
     * @param lx
     * @param shape Shape of the transition
     */
    public IrisTransition(HeronLX lx, Shape shape) {
        this(lx, shape, Direction.OUTWARD);
    }
    
    /**
     * Constructs a RADIAL transition with specified direction 
     * 
     * @param lx
     * @param direction Direction of the transition
     */
    public IrisTransition(HeronLX lx, Direction direction) {
        this(lx, Shape.RADIAL, direction);
    }
    
    /**
     * Constructs a transition with specified shape and direction
     * 
     * @param lx
     * @param shape Shape of the transition
     * @param direction Direction of the transition
     */
    public IrisTransition(HeronLX lx, Shape shape, Direction direction) {
        super(lx);
        this.shape = shape;
        this.direction = direction;
        this.depth = 5.;
    }
    
    /**
     * Sets the depth of the transition 
     * 
     * @param depth Number of pixels across which the transition smears
     * @return This transition, for method chaining
     */
    public IrisTransition setDepth(double depth) {
        this.depth = depth;
        return this;
    }

    protected void computeBlend(int[] c1, int[] c2, double progress) {
        double distanceSign = 1;
        double distanceProgress = progress;
        if (this.direction == Direction.INWARD) {
            distanceSign = -1;
            distanceProgress = 1.-progress;
        }
        double width = (this.lx.width-1)/2.;
        double height = (this.lx.height-1)/2.;
        switch (this.shape) {
        case RADIAL:
            double blendPosition = -this.depth + (Math.sqrt(width*width + height*height) + this.depth*2.) * distanceProgress;
            for (int i = 0; i < this.lx.total; ++i) {
                double rowDist = this.lx.row(i) - height;
                double colDist = this.lx.column(i) - width;
                double nodePosition = Math.sqrt(rowDist*rowDist + colDist*colDist);
                double blendDistance = (blendPosition - nodePosition) / (this.depth / 2.) * distanceSign;
                if (blendDistance <= -1.) {
                    this.colors[i] = c1[i];
                } else if (blendDistance >= 1.) {
                    this.colors[i] = c2[i];
                } else {
                    this.colors[i] = PApplet.lerpColor(c1[i], c2[i], (float) ((blendDistance+1.)/2.), PConstants.RGB);
                }
            }
            break;
            
        case SQUARE:
            double xPosition = -this.depth + (width + 2*this.depth)*distanceProgress;
            double yPosition = -this.depth + (height + 2*this.depth)*distanceProgress;
            for (int i = 0; i < this.lx.total; ++i) {
                double xDistance = (xPosition - Math.abs(this.lx.column(i) - width)) / (this.depth / 2.) * distanceSign;
                double yDistance = (yPosition - Math.abs((this.lx.row(i) - height))) / (this.depth / 2.) * distanceSign;
                if (this.direction == Direction.OUTWARD && (xDistance <= -1. || yDistance <= -1.)) {
                    this.colors[i] = c1[i];
                } else if (this.direction == Direction.INWARD && (xDistance >= 1. && yDistance >= -1.)) {
                    this.colors[i] = c2[i];
                } else {
                    double blendDistance = (this.direction == Direction.OUTWARD) ? Math.min(xDistance, yDistance) : Math.max(xDistance, yDistance);
                    if (blendDistance <= -1.) {
                        this.colors[i] = c1[i];
                    } else if (blendDistance >= 1.) {
                        this.colors[i] = c2[i];
                    } else {
                        this.colors[i] = PApplet.lerpColor(c1[i], c2[i], (float) ((blendDistance+1.)/2.), PConstants.RGB);
                    }
                }
            }        
        }
    }
}
