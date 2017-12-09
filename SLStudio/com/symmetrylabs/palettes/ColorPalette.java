package com.symmetrylabs.palettes;

/**
 * A com.symmetrylabs.palettes.ColorPalette is a thing that assigns a color value to every real number. Palettes are
 * typically designed to generate all their interesting values between 0 and 1; however, implementations must accept any
 * value of p. For example, in a typical linear gradient palette, the gradient would run from p = 0 to p = 1, and then
 * it might be a constant for all p <= 0 and a constant for all p >= 1, or it might repeat the gradient from p = 1 to p
 * = 2, from p = 2 to p = 3, and so on, or it might zigzag back and forth to avoid creating sharp jumps at integer
 * values of p.
 * <p>
 * getColor returns an integer 0xAARRGGBB value, and is responsible for making sure that the top byte is a reasonable
 * alpha value (usually 0xff).
 */
public interface ColorPalette {
    int getColor(double p);
}
