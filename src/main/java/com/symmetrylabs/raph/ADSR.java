package com.symmetrylabs.raph;

import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;


public class ADSR {
    final CompoundParameter attack = (CompoundParameter)
        new CompoundParameter("Attack", 50, 25, 1000)
            .setExponent(2)
            .setUnits(LXParameter.Units.MILLISECONDS)
            .setDescription("Sets the attack time of the notes");

    final CompoundParameter decay = (CompoundParameter)
        new CompoundParameter("Decay", 500, 50, 3000)
            .setExponent(2)
            .setUnits(LXParameter.Units.MILLISECONDS)
            .setDescription("Sets the decay time of the notes");

    final CompoundParameter sustain = (CompoundParameter)
        new CompoundParameter("Sustain", .5)
            .setExponent(2)
            .setDescription("Sets the sustain level of the notes");

    final CompoundParameter release = (CompoundParameter)
        new CompoundParameter("Release", 500, 50, 5000)
            .setExponent(2)
            .setUnits(LXParameter.Units.MILLISECONDS)
            .setDescription("Sets the decay time of the notes");
}
