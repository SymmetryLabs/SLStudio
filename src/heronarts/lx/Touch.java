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

/**
 * Interface for a touch-screen controller. Only supports single-touch. This
 * object is used to access the state of the user's finger.
 */
public interface Touch {

    /**
     * Constant returned from coordinate methods if there is no touch.
     */
    public static final double NO_TOUCH = -1;

    /**
     * Get the x-position of the user touch.
     * 
     * @return x coordinate of user touch, -1 if no touch
     */
    public double getX();

    /**
     * Get the y-position of the user touch.
     * 
     * @return y coordinate of user touch, -1 if no touch
     */
    public double getY();

    /**
     * Whether the user's finger is touching the screen.
     * 
     * @return Whether the user is currently touching the controller.
     */
    public boolean isActive();

    /**
     * A dummy helper class for the case where there is no touch-screen input.
     */
    public class NullTouch implements Touch {
        public double getX() {
            return NO_TOUCH;
        }

        public double getY() {
            return NO_TOUCH;
        }

        public boolean isActive() {
            return false;
        }
    }
}
