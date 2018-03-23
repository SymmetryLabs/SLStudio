// package com.symmetrylabs.layouts.dollywood;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.color.LXColor;

// public class TextureWave extends TexturePattern {
//   public String getAuthor() {
//     return "Mark C. Slee";
//   }
    
//   public final CompoundParameter speed = (CompoundParameter)
//     new CompoundParameter("Speed", 1000, 4000, 250)
//     .setDescription("Speed of oscillation between sides of the leaf")
//     .setExponent(.5);
        
//   private final LXModulator[] side = new LXModulator[LeafAssemblage.NUM_LEAVES];
//   private final int[] assemblageMask = new int[LeafAssemblage.NUM_LEDS];
    
//   public TextureWave(LX lx) {
//     super(lx);
//     for (int i = 0; i < this.side.length; ++i) {
//       this.side[i] = startModulator(new SinLFO("Side", 0, 100, speed).setBasis(i / (float) this.side.length));
//     }
//     for (int i = 0; i < this.assemblageMask.length; ++i) {
//       this.assemblageMask[i] = #000000;
//     }
//     addParameter("speed", this.speed);
//   }
    
//   public void run(double deltaMs) {
//     int i = 0;
//     for (int ai = 0; ai < LeafAssemblage.NUM_LEAVES; ++ai) {
//       float side = this.side[ai].getValuef();
//       for (int li = 0; li < Leaf.NUM_LEDS; ++li) {
//         if (li < 3) {
//           this.assemblageMask[i] = LXColor.gray(side);
//         } else if (li > 3) {
//           this.assemblageMask[i] = LXColor.gray(100 - side);
//         }
//         ++i;
//       }
//     }
//     setAssemblageMask(this.assemblageMask);
//   }
// }