//package com.symmetrylabs.patterns;

// public class Noise extends CubesPattern {

//   public final CompoundParameter scale = new CompoundParameter("Scale", 10, 5, 40);
//   public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 0, -6, 6);
//   public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 0, -6, 6);
//   public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 1, -6, 6);
//   public final CompoundParameter floor = new CompoundParameter("Floor", 0, -2, 2);
//   public final CompoundParameter range = new CompoundParameter("Range", 1, .2, 4);
//   public final CompoundParameter xOffset = new CompoundParameter("XOffs", 0, -1, 1);
//   public final CompoundParameter yOffset = new CompoundParameter("YOffs", 0, -1, 1);
//   public final CompoundParameter zOffset = new CompoundParameter("ZOffs", 0, -1, 1);

//   public Noise(LX lx) {
//     super(lx);
//     addParameter(scale);
//     addParameter(floor);
//     addParameter(range);
//     addParameter(xSpeed);
//     addParameter(ySpeed);
//     addParameter(zSpeed);
//     addParameter(xOffset);
//     addParameter(yOffset);
//     addParameter(zOffset);
//   }

//   private class Accum {
//     private float accum = 0;
//     private int equalCount = 0;
//     private float sign = 1;

//     void accum(double deltaMs, float speed) {
//       float newAccum = (float) (this.accum + this.sign * deltaMs * speed / 4000.);
//       if (newAccum == this.accum) {
//         if (++this.equalCount >= 5) {
//           this.equalCount = 0;
//           this.sign = -sign;
//           newAccum = this.accum + sign*.01;
//         }
//       }
//       this.accum = newAccum;
//     }
//   };

//   private final Accum xAccum = new Accum();
//   private final Accum yAccum = new Accum();
//   private final Accum zAccum = new Accum();

//   @Override
//   public void run(double deltaMs) {
//     xAccum.accum(deltaMs, xSpeed.getValuef());
//     yAccum.accum(deltaMs, ySpeed.getValuef());
//     zAccum.accum(deltaMs, zSpeed.getValuef());

//     float sf = scale.getValuef() / 1000.;
//     float rf = range.getValuef();
//     float ff = floor.getValuef();
//     float xo = xOffset.getValuef();
//     float yo = yOffset.getValuef();
//     float zo = zOffset.getValuef();
//     for (LXPoint p :  model.points) {
//       float b = ff + rf * noise(sf*p.x + xo + xAccum.accum, sf*p.y + yo + yAccum.accum, sf*p.z + zo + zAccum.accum);
//       colors[p.index] = palette.getColor(p, constrain(b*100, 0, 100));
//     }
//   }
// }
