
package com.symmetrylabs.shows.firefly;

import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;
import com.symmetrylabs.shows.firefly.BFBase;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class BFNoise extends BFBase {

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();
    CompoundParameter scale = new CompoundParameter("scale", 0.01f, 0.01f, 1f);
    CompoundParameter speed = new CompoundParameter("speed", 0.1f, 0.01f, 1f);
    CompoundParameter angleSpeed = new CompoundParameter("aspeed", 0.2f, 0f, 10f);
    BooleanParameter unique = new BooleanParameter("uniq", true);

    BooleanParameter oppose = new BooleanParameter("oppose", true);
    DiscreteParameter palette = new DiscreteParameter("palette", paletteLibrary.getNames());
    // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

    ZigzagPalette pal = new ZigzagPalette();

    float currentPos = 0f;
    float angleOffset = 0f;
    boolean angleBack = false;

    public BFNoise(LX lx) {
        super(lx);
        addParameter(scale);
        addParameter(speed);
        addParameter(angleSpeed);
        addParameter(oppose);
        addParameter(unique);
        addParameter(palette);
        addParameter(palStart);
        addParameter(palStop);
        addParameter(palShift);
        addParameter(palBias);
        addParameter(palCutoff);
        palette.setValue(3);
    }

    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    protected void renderButterfly(double drawDeltaMs, LUButterfly butterfly, int randomInt) {
        pal.setPalette(paletteLibrary.get(palette.getOption()));
        pal.setBottom(palStart.getValue());
        pal.setTop(palStop.getValue());
        pal.setBias(palBias.getValue());
        pal.setShift(palShift.getValue());
        pal.setCutoff(palCutoff.getValue());
        for (LXPoint p : butterfly.allPoints) {
            float hue = 360 * (float)(randomInt)/1000f;
            colors[p.index] = LXColor.hsb(hue, 100, 100);
        }
        float uniqueOffset = 0f;
        if (unique.getValueb()) {
            uniqueOffset = butterfly.runIndex;
        }
        int i = 0;
        for (LXPoint left : butterfly.left) {
            float bright = SLStudio.applet.noise(i * scale.getValuef() + currentPos + uniqueOffset, 0);
            int color = pal.getColor(bright);
            colors[left.index] = color;
            i++;
        }
        if (oppose.getValueb()) {
            for (i = 0; i < butterfly.right.size(); i++) {
                float bright = SLStudio.applet.noise(i * scale.getValuef() + currentPos + uniqueOffset, scale.getValuef());
                int color = pal.getColor(bright);
                colors[butterfly.right.get(i).index] = color;
            }
        } else {
            for (i = butterfly.right.size()-1; i >= 0; i--) {
                float bright = SLStudio.applet.noise((7 - i) * scale.getValuef() + currentPos + uniqueOffset, scale.getValuef());
                int color = pal.getColor(bright);
                colors[butterfly.right.get(i).index] = color;
            }
        }
        currentPos += (speed.getValuef()/1000f) * 8f * scale.getValuef();
        if (currentPos > 100f) {
            currentPos = 0f;
        }
    }

    @Override
    void postDraw(double drawDeltaMs) {
        if (!angleBack)
            angleOffset += angleSpeed.getValuef();
        else
            angleOffset -= angleSpeed.getValuef();
        // Palettes don't wrap, so when
        if (!angleBack && angleOffset >= 360f) {
            angleBack = true;
            angleOffset -= angleSpeed.getValuef();
        } else if (angleBack && angleOffset < 0f) {
            angleBack = false;
            angleOffset += angleSpeed.getValuef();
        }
    }

    @Override
    protected void renderFlower(double drawDeltaMs, LUFlower flower, int randomInt) {
        float flowerAngle = (flower.flowerConfig.azimuth + angleOffset) / 360f;
        flower.setColor(colors, pal.getColor(flowerAngle));
    }
}
