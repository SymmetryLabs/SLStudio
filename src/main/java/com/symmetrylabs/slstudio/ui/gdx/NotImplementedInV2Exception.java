package com.symmetrylabs.slstudio.ui.gdx;

/**
 * An exception raised when a feature is not yet supported in UIv2.
 *
 * Prefer using this to other runtime exceptions for easy searching in the codebase
 * for places where we are not yet feature-complete.
 */
public class NotImplementedInV2Exception extends RuntimeException {
    public NotImplementedInV2Exception() {
        super("this feature is not yet implemented in UIv2");
    }

    public NotImplementedInV2Exception(String msg) {
        super(msg);
    }
}
