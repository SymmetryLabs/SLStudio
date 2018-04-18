// package com.symmetrylabs.layouts.dollywood;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.color.LXColor;

// import heronarts.lx.parameter.CompoundParameter;
// import heronarts.lx.parameter.FunctionalParameter;
// import heronarts.lx.modulator.LXModulator;
// import heronarts.lx.modulator.SinLFO;
// import heronarts.lx.modulator.SawLFO;
// import static com.symmetrylabs.util.MathUtils.*;

// public class TextureInOut extends TexturePattern {
//   public String getAuthor() {
//     return "Mark C. Slee";
//   }

//   private final int NUM_LEDS_PER_WING = 12;
    
//   public final CompoundParameter speed = (CompoundParameter)
//     new CompoundParameter("Speed", 1000, 5000, 200)
//     .setExponent(.5)
//     .setDescription("Speed of the motion");
        
//   public final CompoundParameter size = (CompoundParameter)
//     new CompoundParameter("Size", 2, 1, 4)
//     .setDescription("Size of the streak");
    
//   private final LXModulator[] wings = new LXModulator[NUM_LEDS_PER_WING]; 
//   private final int[] wingMask = new int[NUM_LEDS_PER_WING];
    
//   public TextureInOut(LX lx) {
//     super(lx);
//     addParameter("speed", this.speed);
//     addParameter("size", this.size);
//     for (int i = 0; i < this.wings.length; ++i) {
//       final int ii = i;
//       this.wings[i] = startModulator(new SinLFO(0, (NUM_LEDS_PER_WING-1)/2.f, new FunctionalParameter() {
//         public double getValue() {
//           return speed.getValue() * (1 + .05f * ii); 
//         }
//       }).randomBasis());
//     }
//   }
    
//   public void run(double deltaMs) {
//     int ai = 0;
//     float falloff = 100 / this.size.getValuef();
//     for (LXModulator wing : this.wings) {
//       float pos = wing.getValuef();
//       for (int i = 0; i < NUM_LEDS_PER_WING; ++i) {
//         float d = abs(i - (NUM_LEDS_PER_WING-1)/2.f);
//         float b = max(0, 100 - falloff * abs(i - pos));
//         this.wingMask[ai++] = LXColor.gray(b);
//       }
//     }
//     setWingMask(this.wingMask);
//   }
// }