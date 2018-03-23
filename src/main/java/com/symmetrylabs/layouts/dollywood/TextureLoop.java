// package com.symmetrylabs.layouts.dollywood;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.color.LXColor;

// import heronarts.lx.parameter.CompoundParameter;
// import heronarts.lx.modulator.LXModulator;

// public class TextureLoop extends TexturePattern {
//   public String getAuthor() {
//     return "Mark C. Slee";
//   }
    
//   public final CompoundParameter speed = (CompoundParameter)
//     new CompoundParameter("Speed", 500, 2000, 200)
//     .setExponent(.5)
//     .setDescription("Speed of the loop motion");    
    
//   public final CompoundParameter size =
//     new CompoundParameter("Size", 3, 1, 12) // arbitrarily made 12 since wings have varying sizes
//     .setDescription("Size of the thread");
    
//   public LXModulator pos = startModulator(new SawLFO(0, 12, speed)); // arbitrarily made 12 since wings have varying sizes
    
//   private final int[] leafMask = new int[Leaf.NUM_LEDS];
    
//   public TextureLoop(LX lx) {
//     super(lx);
//     addParameter("rate", this.speed);
//     addParameter("size", this.size);
//   }
    
//   public void run(double deltaMs) {
//     float pos = this.pos.getValuef();
//     float falloff = 100 / this.size.getValuef();
//     for (int i = 0; i < this.leafMask.length; ++i) {
//       this.leafMask[i] = LXColor.gray(max(0, 100 - falloff * LXUtils.wrapdistf(i, pos, Leaf.NUM_LEDS)));
//     }
//     setLeafMask(this.leafMask);
//   }
// }