package com.symmetrylabs.color;

import org.junit.Assert;
import org.junit.Test;
import java.util.Random;

public class PerceptualColorScaleTest {
    @Test
    public void testPowerScaleOnWhite() {
        int white = 0xFFFFFFFF;
        Assert.assertEquals(Ops8.power(white), 1.0, 1e-10);

        for (double sc = 0.05; sc <= 1; sc += 0.01) {
            PerceptualColorScale pcs = new PerceptualColorScale(new double[] {2.0, 2.1, 2.8}, sc);
            int scaled = pcs.apply8(white);
            double outPower = Ops8.power(scaled);
            Assert.assertEquals(sc, outPower, 0.01);
        }
    }
}
