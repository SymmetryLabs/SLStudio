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

package heronarts.lx.client;

import heronarts.lx.Touch;

/**
 * Concrete class for a touchscreen interface from a client. Methods to
 * update the touch state are package-level access, so client implementations
 * may modify them.
 */
public class ClientTouch implements Touch {
    
    private double x = NO_TOUCH;
    private double y = NO_TOUCH;
    private boolean isActive = false;
    
    void setX(double x) {
        this.x = x;
    }
    
    void setY(double y) {
        this.y = y;
    }
    
    void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public boolean isActive() {
        return this.isActive;
    }

}
