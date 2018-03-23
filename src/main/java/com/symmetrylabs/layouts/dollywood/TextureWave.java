// package com.symmetrylabs.layouts.dollywood;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.color.LXColor;

// import heronarts.lx.parameter.CompoundParameter;
// import heronarts.lx.modulator.LXModulator;
// import heronarts.lx.modulator.SinLFO;
// import heronarts.lx.modulator.SawLFO;

// public class TextureWave extends TexturePattern {
//   public String getAuthor() {
//     return "Mark C. Slee";
//   }

//   private final int NUM_LEDS_PER_WING = 12;
    
//   public final CompoundParameter speed = (CompoundParameter)
//     new CompoundParameter("Speed", 1000, 4000, 250)
//     .setDescription("Speed of oscillation between sides of the leaf")
//     .setExponent(.5);
        
//   private final LXModulator[] side = new LXModulator[4];
//   private final int[] butterflyMask = new int[NUM_LEDS_PER_WING];
    
//   public TextureWave(LX lx) {
//     super(lx);
//     for (int i = 0; i < this.side.length; ++i) {
//       this.side[i] = startModulator(new SinLFO("Side", 0, 100, speed).setBasis(i / (float) this.side.length));
//     }
//     for (int i = 0; i < this.butterflyMask.length; ++i) {
//       this.butterflyMask[i] = 0x000000;
//     }
//     addParameter("speed", this.speed);
//   }
    
//   public void run(double deltaMs) {
//     int i = 0;
//     for (int ai = 0; ai < 4; ++ai) {
//       float side = this.side[ai].getValuef();
//       for (int li = 0; li < NUM_LEDS_PER_WING; ++li) {
//         if (li < 3) {
//           this.butterflyMask[i] = LXColor.gray(side);
//         } else if (li > 3) {
//           this.butterflyMask[i] = LXColor.gray(100 - side);
//         }
//         ++i;
//       }
//     }
//     setButterflyMask(this.butterflyMask);
//   }
// }