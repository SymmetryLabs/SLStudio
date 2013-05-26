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

package heronarts.lx;

import java.lang.Math;

public class LXUtils {
    public static double constrain(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }
    
    public static int constrain(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }
    
    public static double random(double min, double max) {
        return min + Math.random() * (max-min);
    }
    
        public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
    
    public static double lerp(double v1, double v2, double amt) {
        return v1 + (v2-v1)*amt;
    }
    
    public static double tri(double t) {
        t = t - Math.floor(t);
        if (t < 0.25) {
            return t * 4;
        } else if (t < 0.75) {
            return 1 - 4*(t-0.25);
        } else {
            return -1 + 4*(t-0.75);
        }
    }
    
    public static float trif(float t) {
        return (float)LXUtils.tri(t);
    }
    
    public static double avg(double v1, double v2) {
        return (v1 + v2) / 2.;
    }
    
    public static float avgf(float v1, float v2) {
        return (float)LXUtils.avg(v1, v2);
    }
    
    public static double wrapdist(double v1, double v2, double mod) {
        v1 = v1 % mod;
        v2 = v2 % mod;
        if (v1 < v2) {
            return Math.min(v2 - v1, v1 + mod - v2); 
        } else {
            return Math.min(v1 - v2, v2 + mod - v1); 
        }
    }
    
    public static float wrapdistf(float v1, float v2, float mod) {
        return (float)LXUtils.wrapdist(v1, v2, mod);
    }
}
