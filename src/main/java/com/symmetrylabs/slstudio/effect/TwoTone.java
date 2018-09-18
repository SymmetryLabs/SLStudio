package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.util.ColorUtils;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import org.apache.commons.math3.util.FastMath;

import java.util.stream.IntStream;

public class TwoTone extends LXEffect {
    CompoundParameter cutoff1 = new CompoundParameter("Lcut1", 1, 0, 100);
    CompoundParameter cutoff2 = new CompoundParameter("Lcut2", 50, 0, 100);
    ColorParameter c1 = new ColorParameter("c1", LXColor.RED);
    ColorParameter c2 = new ColorParameter("c2", LXColor.WHITE);
    BooleanParameter useGlobalC1 = new BooleanParameter("globalC1", true).setDescription("if true, uses global palette color for C1 instead of the C1 parameter");

    public TwoTone(LX lx) {
        super(lx);
        addParameter(cutoff1);
        addParameter(cutoff2);
        addParameter(c1);
        addParameter(c2);
        addParameter(useGlobalC1);
    }

    @Override
    public void run(double deltaMs, double amount) {
        int c1color = useGlobalC1.getValueb() ? palette.getColor() : c1.getColor();
        int c2color = c2.getColor();

        IntStream.range(0, colors.length).parallel().forEach(i -> {
            int c = colors[i];
            double r, g, b;
            r = FastMath.pow(((c & LXColor.RED_MASK) >>> LXColor.RED_SHIFT) / 255.0, 2.2);
            g = FastMath.pow(((c & LXColor.GREEN_MASK) >>> LXColor.GREEN_SHIFT) / 255.0, 2.2);
            b = FastMath.pow((c & LXColor.BLUE_MASK) / 255.0, 2.2);

            /* This math is based off of the colorspace calculations in ColorUtils.
             * Since all we need is the luminance, we can skip everything that is
             * only used for the u/v calculations. */
            double y = r * 0.2284569f + g * 0.7373523f + b * 0.0341908f;
            final double Y_r = 1.00000f;
            final double eps = 216f / 24389f;
            final double k = 24389f / 27f;

            double yy = y / Y_r;
            double L = yy > eps ? (116 * FastMath.cbrt(yy)) - 16 : k * yy;

            if (L < cutoff1.getValue()) {
                colors[i] = 0;
            } else if (L < cutoff2.getValue()) {
                colors[i] = c1color;
            } else {
                colors[i] = c2color;
            }
        });
    }
}
