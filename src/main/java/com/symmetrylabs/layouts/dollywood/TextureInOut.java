// package com.symmetrylabs.layouts.dollywood;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.color.LXColor;

// import heronarts.lx.parameter.CompoundParameter;
// import heronarts.lx.modulator.LXModulator;

// public class TextureInOut extends TexturePattern {
//   public String getAuthor() {
//     return "Mark C. Slee";
//   }
    
//   public final CompoundParameter speed = (CompoundParameter)
//     new CompoundParameter("Speed", 1000, 5000, 200)
//     .setExponent(.5)
//     .setDescription("Speed of the motion");
        
//   public final CompoundParameter size = (CompoundParameter)
//     new CompoundParameter("Size", 2, 1, 4)
//     .setDescription("Size of the streak");
    
//   private final LXModulator[] leaves = new LXModulator[LeafAssemblage.NUM_LEAVES]; 
//   private final int[] assemblageMask = new int[LeafAssemblage.NUM_LEDS];
    
//   public TextureInOut(LX lx) {
//     super(lx);
//     addParameter("speed", this.speed);
//     addParameter("size", this.size);
//     for (int i = 0; i < this.leaves.length; ++i) {
//       final int ii = i;
//       this.leaves[i] = startModulator(new SinLFO(0, (Leaf.NUM_LEDS-1)/2., new FunctionalParameter() {
//         public double getValue() {
//           return speed.getValue() * (1 + .05 * ii); 
//         }
//       }).randomBasis());
//     }
//   }
    
//   public void run(double deltaMs) {
//     int ai = 0;
//     float falloff = 100 / this.size.getValuef();
//     for (LXModulator leaf : this.leaves) {
//       float pos = leaf.getValuef();
//       for (int i = 0; i < Leaf.NUM_LEDS; ++i) {
//         float d = abs(i - (LeafAssemblage.NUM_LEDS-1)/2.);       
//         float b = max(0, 100 - falloff * abs(i - pos)); 
//         this.assemblageMask[ai++] = LXColor.gray(b); 
//       }
//     }
//     setAssemblageMask(this.assemblageMask);
//   }
// }