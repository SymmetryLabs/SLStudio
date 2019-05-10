package com.symmetrylabs.color;

import org.junit.Assert;
import org.junit.Test;
import java.util.Random;

public class SpacesTest {
    private static final int CIE_FUNCTION_TRIALS = 100_000;
    private static final double CIE_FUNCTION_ALLOWED_SQUARED_RESIDUAL = 1e-10;

    @Test
    public void testCieFunctionsAreInverses() {
        Random r = new Random(0); // deterministic seed

        for (int i = 0; i < CIE_FUNCTION_TRIALS; i++) {
            double L = r.nextDouble();
            double Y = Spaces.cie_lightness_to_luminance(L);
            double L_ = Spaces.cie_luminance_to_lightness(Y);
            double residual = Math.pow(L - L_, 2);
            if (residual > CIE_FUNCTION_ALLOWED_SQUARED_RESIDUAL) {
                Assert.fail(
                    String.format(
                        "L = %E -> Y = %E -> L' = %E (delta = %E > max %E)",
                        L, Y, L_, residual, CIE_FUNCTION_ALLOWED_SQUARED_RESIDUAL));
            }
        }

        for (int i = 0; i < CIE_FUNCTION_TRIALS; i++) {
            double Y = r.nextDouble();
            double L = Spaces.cie_luminance_to_lightness(Y);
            double Y_ = Spaces.cie_lightness_to_luminance(L);
            double residual = Math.pow(Y - Y_, 2);
            if (residual > CIE_FUNCTION_ALLOWED_SQUARED_RESIDUAL) {
                Assert.fail(
                    String.format(
                        "Y = %E -> L = %E -> Y' = %E (delta = %E > max %E)",
                        Y, L, Y_, residual, CIE_FUNCTION_ALLOWED_SQUARED_RESIDUAL));
            }
        }
    }
}
