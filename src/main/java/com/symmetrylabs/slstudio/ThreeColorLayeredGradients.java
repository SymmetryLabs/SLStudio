// package com.symmetrylabs.slstudio.pattern;

// import heronarts.lx.LX;
// import heronarts.lx.model.LXPoint;
// import heronarts.lx.parameter.CompoundParameter;
// import heronarts.lx.LXUtils;

// public class ThreeColorLayeredGradients extends SLPattern {

//   public final CompoundParameter bottomPositionX = new CompoundParameter("bPosX", model.xMin, model.xMin, model.xMax);
//   public final CompoundParameter bottomPositionY = new CompoundParameter("bPosY", model.yRange/2, model.yMin, model.yMax);
//   public final CompoundParameter bottomWidthX = new CompoundParameter("bWidX", model.xRange/3, 0, model.xRange);
//   public final CompoundParameter bottomWidthY = new CompoundParameter("bWidY", model.yRange, 0, model.yRange);
//   public final CompoundParameter bottomHue = new CompoundParameter("bHue", 0, 0, 360);
//   public final CompoundParameter bottomSat = new CompoundParameter("bSat", 100, 0, 100);
//   public final CompoundParameter bottomBri = new CompoundParameter("bBri", 100, 100, 100);
//   public final CompoundParameter bottomFalloff = new CompoundParameter("bFall", 25, 0, 200);

//   public final CompoundParameter middlePositionX = new CompoundParameter("mPosX", model.xMin, model.xMin, model.xMax);
//   public final CompoundParameter middlePositionY = new CompoundParameter("mPosY", model.yRange/2, model.yMin, model.yMax);
//   public final CompoundParameter middleWidthX = new CompoundParameter("mWidX", model.xRange/3, 0, model.xRange);
//   public final CompoundParameter middleWidthY = new CompoundParameter("mWidY", model.yRange, 0, model.yRange);
//   public final CompoundParameter middleHue = new CompoundParameter("mHue", 0, 0, 360);
//   public final CompoundParameter middleSat = new CompoundParameter("mSat", 100, 0, 100);
//   public final CompoundParameter middleBri = new CompoundParameter("mBri", 100, 100, 100);
//   public final CompoundParameter middleFalloff = new CompoundParameter("mFall", 25, 0, 200);

//   public final CompoundParameter topPositionX = new CompoundParameter("tPosX", model.xMin, model.xMin, model.xMax);
//   public final CompoundParameter topPositionY = new CompoundParameter("tPosY", model.yRange/2, model.yMin, model.yMax);
//   public final CompoundParameter topWidthX = new CompoundParameter("tWidX", model.xRange/3, 0, model.xRange);
//   public final CompoundParameter topWidthY = new CompoundParameter("tWidY", model.yRange, 0, model.yRange);
//   public final CompoundParameter topHue = new CompoundParameter("tHue", 0, 0, 360);
//   public final CompoundParameter topSat = new CompoundParameter("tSat", 100, 0, 100);
//   public final CompoundParameter topBri = new CompoundParameter("tBri", 100, 100, 100);
//   public final CompoundParameter topFalloff = new CompoundParameter("tFall", 25, 0, 200);

//   public ThreeColorLayeredGradients(LX lx) {
//     super(lx);
//     addParameter(bottomPositionX);
//     addParameter(bottomPositionY);
//     addParameter(bottomWidthX);
//     addParameter(bottomWidthY);
//     addParameter(bottomHue);
//     addParameter(bottomSat);
//     addParameter(bottomBri);
//     addParameter(bottomFalloff);

//     addParameter(middlePositionX);
//     addParameter(middlePositionY);
//     addParameter(middleWidthX);
//     addParameter(middleWidthY);
//     addParameter(middleHue);
//     addParameter(middleSat);
//     addParameter(middleBri);
//     addParameter(middleFalloff);

//     addParameter(topPositionX);
//     addParameter(topPositionY);
//     addParameter(topWidthX);
//     addParameter(topWidthY);
//     addParameter(topHue);
//     addParameter(topSat);
//     addParameter(topBri);
//     addParameter(topFalloff);
//   }

//   public void run(double deltaMs) {
//     setColors(0);

//     int col = 0;

//     for (LXPoint p : model.points) {

//       float briVal = LXUtils.distance(p.x, p.y, bottomPositionX.getValuef(), bottomPositionY.getValuef());

//     }
//   }

// }
