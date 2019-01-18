package com.symmetrylabs.slstudio.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation which, when used, indicates that the annotated component should be hidden from the user interface.
 *
 * This can be used to hide patterns, effects, and warps from the W/E/P chooser.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HiddenComponent {}
