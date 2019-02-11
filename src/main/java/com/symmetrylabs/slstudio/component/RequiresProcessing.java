package com.symmetrylabs.slstudio.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation which, when used, indicates that the annotated component requires Processing to be used.
 *
 * This can be used to hide patterns, effects, and warps from the W/E/P chooser in the new UI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresProcessing {}
