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
 * Annoying wrapper to create compatibility between processing 1 and 2
 */
public class LXKeyEvent {
    
    public enum Action {
        PRESSED,
        RELEASED,
        TYPED
    };
    
    class UnsupportedActionException extends Exception {
        
        public static final long serialVersionUID = 1;
        
        public final int action;
        
        UnsupportedActionException(int action) {
            super("Unsupported key action type: " + action);
            this.action = action;
        }
    }
    
    private final Action action;
    private final char keyChar;
    private final int keyCode;
    private final boolean isControlDown;
    private final boolean isShiftDown;
    private final boolean isMetaDown;
    private final boolean isAltDown;
    private final int modifiers;
    
    LXKeyEvent(processing.event.KeyEvent keyEvent) throws UnsupportedActionException {
        switch (keyEvent.getAction()) {
        case processing.event.KeyEvent.PRESS:
            this.action = Action.PRESSED;
            break;
        case processing.event.KeyEvent.RELEASE:
            this.action = Action.RELEASED;
            break;
        case processing.event.KeyEvent.TYPE:
            this.action = Action.TYPED;
            break;
        default:
            throw new UnsupportedActionException(keyEvent.getAction());
        }
        
        this.keyChar = keyEvent.getKey();
        this.keyCode = keyEvent.getKeyCode();
        this.isControlDown = keyEvent.isControlDown();
        this.isShiftDown = keyEvent.isShiftDown();
        this.isAltDown = keyEvent.isAltDown();
        this.isMetaDown = keyEvent.isMetaDown();
        this.modifiers = keyEvent.getModifiers();
    }
    
    LXKeyEvent(java.awt.event.KeyEvent keyEvent) throws UnsupportedActionException {
        switch (keyEvent.getID()) {
        case java.awt.event.KeyEvent.KEY_PRESSED:
            this.action = Action.PRESSED;
            break;
        case java.awt.event.KeyEvent.KEY_RELEASED:
            this.action = Action.RELEASED;
            break;
        case java.awt.event.KeyEvent.KEY_TYPED:
            this.action = Action.TYPED;
            break;
        default:
            throw new UnsupportedActionException(keyEvent.getID());
        }
        
        this.keyChar = keyEvent.getKeyChar();
        this.keyCode = keyEvent.getKeyCode();
        this.isControlDown = keyEvent.isControlDown();
        this.isShiftDown = keyEvent.isShiftDown();
        this.isAltDown = keyEvent.isAltDown();
        this.isMetaDown = keyEvent.isMetaDown();
        this.modifiers = keyEvent.getModifiers();
    }
    
    public Action getAction() {
        return this.action;
    }
    
    public char getKeyChar() {
        return this.keyChar;
    }
    
    public int getKeyCode() {
        return this.keyCode;
    }
    
    public boolean isControlDown() {
        return this.isControlDown;
    }

    public boolean isShiftDown() {
        return this.isShiftDown;
    }
    
    public boolean isMetaDown() {
        return this.isMetaDown;
    }
    
    public boolean isAltDown() {
        return this.isAltDown();
    }
    
    public int getModifiers() {
        return this.modifiers;
    }
}
