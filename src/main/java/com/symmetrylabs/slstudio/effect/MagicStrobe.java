package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

/**
 * Psychedelic color override effect. Keeps the pattern's brightness but
 * replaces its hue/saturation with an animated strobe color.
 */

public class MagicStrobe extends LXEffect {
    public int fps = 60; //for 60fps, not sure how to bind to actual fps
    public double framems = 1000.0 / fps;
    double lengthvar = 1;
    public double timemod = 0;
    double period = 0;
    public final CompoundParameter speed =
            new CompoundParameter("Speed", 1.0, 0.001, 0.5)
                    .setDescription("Strobe Speed");
    public final CompoundParameter divergance =
            new CompoundParameter("Divergance")
                    .setDescription("Chromatic Divergance");
    public final CompoundParameter phase =
            new CompoundParameter("Phase", 0.5)
                    .setDescription("Chromatic phase");
    public final BooleanParameter sync =
            new BooleanParameter("Frame Sync", true)
                    .setDescription("Synchronize effect to framerate");

    public MagicStrobe(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("divergance", this.divergance);
        addParameter("phase", this.phase);
        addParameter("sync", this.sync);
    }

    @Override
    public void run(double deltaMs, double amount) {
        this.framems = (0.9 * this.framems) + (0.1 * deltaMs);      //adaptive frame rate detection, basically a lazy man's moving average
        this.lengthvar = 1.0 / this.speed.getValue();               //change speed knob range
        if(this.sync.getValueb()){
            this.lengthvar = Math.round(this.lengthvar);            //makes sure the speed is an even number of frames, if selected, to prevent unwanted harmonics
        }
        this.period = this.lengthvar * this.framems;
        this.timemod = (this.timemod + deltaMs) % this.period;      //just makes the rate constant
        double hue = this.timemod / this.period;
        hue  = (hue + this.phase.getValue()) % 1.0;
        float saturation = (float) (this.divergance.getValue() * 100.0);

        for (int i = 0; i < this.colors.length; ++i) {
            int original = this.colors[i];
            float brightness = LXColor.b(original);
            double h = hue * 360.0;
            int override = LXColor.hsb((float) h, saturation, brightness);
            this.colors[i] = LXColor.lerp(original, override, (float) amount);
        }
    }
}
