package heronarts.lx.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

public class InvertEffect extends LXEffect {
    public InvertEffect(LX lx) {
        super(lx);
    }

    @Override
    protected void run(double deltaMs, double amount) {
        for (int i = 0; i < colors.length; ++i) {
            this.colors[i] = LXColor.hsb(
                0,
                0,
                100 - LXColor.b(colors[i])
            );
        }
    }
}
