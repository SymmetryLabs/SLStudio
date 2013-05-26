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

public interface Touch {
    public double getX();
    public double getY();
    public boolean isActive();
    
    public class NullTouch implements Touch {
        public double getX() {
            return -1;
        }
        public double getY() {
            return -1;
        }
        public boolean isActive() {
            return false;
        }
    }
}
