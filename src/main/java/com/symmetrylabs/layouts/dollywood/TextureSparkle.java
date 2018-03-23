// package com.symmetrylabs.layouts.dollywood;

// import heronarts.lx.LX;
// import heronarts.lx.LXPattern;
// import heronarts.lx.color.LXColor;

// import heronarts.lx.parameter.CompoundParameter;
// import heronarts.lx.modulator.SinLFO;

// public class TextureSparkle extends TexturePattern {
//   public String getAuthor() {
//     return "Mark C. Slee";
//   }
    
//   private final SinLFO[] levels = new SinLFO[LeafAssemblage.NUM_LEDS]; 
    
//   private final int[] assemblageMask = new int[LeafAssemblage.NUM_LEDS];
    
//   public final CompoundParameter speed = (CompoundParameter)
//     new CompoundParameter("Speed", 1000, 5000, 200)
//     .setExponent(.5)
//     .setDescription("Speed of the sparkling");
        
//   public final CompoundParameter bright = (CompoundParameter)
//     new CompoundParameter("Bright", 60, 20, 100)
//     .setDescription("Brightness of the sparkling");
    
//   public TextureSparkle(LX lx) {
//     super(lx);
//     addParameter("speed", this.speed);
//     addParameter("bright", this.bright);
//     for (int i = 0; i < this.levels.length; ++i) {
//       this.levels[i] = new SinLFO(0, 0, 1000);
//       initialize(this.levels[i]);
//       startModulator(this.levels[i].randomBasis());
//     }
//   }
    
//   private void initialize(SinLFO level) {
//     level.setRange(0, random(this.bright.getValuef(), 100)).setPeriod(min(7000, speed.getValuef() * random(1, 2)));
//   }
    
//   public void run(double deltaMs) {
//     for (int i = 0; i < this.levels.length; ++i) {
//       if (this.levels[i].loop()) {
//         initialize(this.levels[i]);
//       }
//       this.assemblageMask[i] = LXColor.gray(this.levels[i].getValuef());
//     }
//     setAssemblageMask(this.assemblageMask);
//   }
// }