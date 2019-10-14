package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PVector;
import org.openkinect.processing.Kinect2;
import static processing.core.PApplet.map;
import static processing.core.PApplet.constrain;

import com.symmetrylabs.slstudio.pattern.base.DPat;
import com.symmetrylabs.slstudio.SLStudio;

public class Kinect extends DPat {

    public final CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
    public final CompoundParameter zKinectRangeMin = new CompoundParameter("zMin", 0, 500, 3000);
    public final CompoundParameter zKinectRangeMax = new CompoundParameter("zMax", 2475, 500, 3000);

    private int kinectZMax = 4500;
    private int kinectZMin = 500;
    // int zKinectRangeMin = 500;
    // int zKinectRangeMax = 2000;
    private int zScalarMin = 0;
    private int zScalarMax = 1000;

    // only draw every 2nd pixel
    private int skip = 2;

    // get raw depth data from Kinect as int array
    private int[] depth = null; //kinect2.getRawDepth();

    private Kinect2 kinect2;

    public Kinect(LX lx) {
        super(lx);

        addParameter(hue);
        addParameter(zKinectRangeMin);
        addParameter(zKinectRangeMax);

        kinect2 = new Kinect2(SLStudio.applet);
        kinect2.initDepth();
        kinect2.initDevice();
    }

    @Override
    public void StartRun(double deltaMs) {
        depth = kinect2.getRawDepth();
    }

    @Override
    public int CalcPoint(PVector p) {
        //for each point, get the x, y, z coordinates
        float xPos = p.x;
        float yPos = p.y;
        float zPos = p.z;

        //map those x, y, z coordinates to the 512x424xZ-depth Kinect window
        float xMapped = map(xPos, lx.model.xMin, lx.model.xMax, 2, 510);
        float yMapped = map(yPos, lx.model.yMin, lx.model.yMax, 2, 422);
        float zMapped = map(zPos, lx.model.zMin, lx.model.zMax, zScalarMin, zScalarMax); // <-- relative z-value of LED pixel within model at (x,y)

        //use the same (x,y) coordinates to pull depth data from the Kinect
        // **DO YOU NEED TO RUN depthToPointCloudPos FUNCTION??**
        int xKinect = Math.round(xMapped);
        int yKinect = 422 - Math.round(yMapped); // subtract from max value in order to invert for screen coordinates

        //find the correct Z-depth in the [depth] array given the (x,y) coordinates
        int index = xKinect + yKinect * kinect2.depthWidth;
        int zKinect = depth[constrain(index, 0, depth.length-1)];
        //int zKinect = depth[constrain(0, depth.length, index)];

        //map the Z-depth to the same Z-scale as the LED points, with the back of the Kinect depth cloud pushed behind the led array (ie last mapping value is larger)
        //float  zKinectMapped = map(zKinect, kinectZMin, kinectZMax, 0, 4000);

        //map the zKinectMapped integer to a float between 0 and 1 in order to interpolate across different hue values
        float hueMap = map(zKinect, 0, 1000, 0, 360);

        //map the zKinect depth value to a float between 0 and 1 in order to interpolate between two gradient colors
        float lerpMap = map(zKinect, kinectZMin, 1500, 0, 1);

        /*
        //OPTION 0: ONE-COLOR DEPTH
        if ((zKinect < zKinectRangeMin ) || (zKinect > zKinectRangeMax) || ((zKinect >= 0) && (zKinect <= 1))){
          colors[p.index] = lx.hsb(0,0,0);
        }
        else {
          //map kinect depth values to 0<>1000, same as LED-zposition values
          float kinectReMap = map(zKinect, zKinectRangeMin, zKinectRangeMax, zScalarMin, zScalarMax);
          if (kinectReMap < zMapped) {
            colors[p.index] = lx.hsb(100,100,100);
          }
          else {
            colors[p.index] = lx.hsb(0,0,0);
          }
        }
        */


        //OPTION 0A: HUE WITH DEPTH CLOUD
        if ((zKinect < zKinectRangeMin.getValuef() ) || (zKinect > zKinectRangeMax.getValuef()) || ((zKinect >= 0) && (zKinect <= 1))){
          //colors[p.index] = lx.hsb(200,50,25);
          return lx.hsb(0,0,0);
        }
        else {
          //map kinect depth values to 0<>1000, same as LED-zposition values
          float kinectReMap = map(zKinect, zKinectRangeMin.getValuef(), zKinectRangeMax.getValuef(), zScalarMin, zScalarMax);
          if (kinectReMap < zMapped) {
            float hueMapAdjust = map(kinectReMap, zScalarMin, zScalarMax, 0, 360);
            return lx.hsb((hueMapAdjust+hue.getValuef())%360,100,100);
          }
          else {
            //colors[p.index] = lx.hsb(200,50,25);
            return lx.hsb(0,0,0);
          }
        }



        /*
        //OPTION 1: HUE
        //test to see if zKinectMapped is greater than zMapped
        if ((zKinect < 1) && (zKinect >= 0)) {
          colors[p.index] = lx.hsb(0,0,0);
        }
        else if (zKinectMapped > 3000) {
          colors[p.index] = lx.hsb(0,0,0);
        }
        else {
          colors[p.index] = lx.hsb(hueMap,100,100);
        }
        // OPTION 2: TWO-COLOR-GRADIENT
        int color1 = 16528716;
        int color2 = 704444;
        if ((zKinect < 1) && (zKinect >= 0)) {
          colors[p.index] = lx.hsb(0,0,0);
        }
        else if (zKinect > 1500) {
          colors[p.index] = lx.hsb(0,0,0);
        }
        else {
          colors[p.index] = LXColor.lerp(color1,color2,lerpMap);
        }
        */



      //   ++i;
      // }
    //}
    }
}
