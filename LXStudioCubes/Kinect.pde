public class Kinect extends SLPattern {

  private final SawLFO index = new SawLFO(0, Cube.Type.LARGE.POINTS_PER_CUBE, Cube.Type.LARGE.POINTS_PER_CUBE*60);

  float xMax = 0;
  float xMin = 5000;
  float yMax = 0;
  float yMin = 5000;
  float zMax = 0;
  float zMin = 5000;

  CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);

  CompoundParameter zKinectRangeMin = new CompoundParameter("zMin", 500, 500, 3000);
  CompoundParameter zKinectRangeMax = new CompoundParameter("zMax", 3000, 500, 3000);

  CompoundParameter xTrim = new CompoundParameter("xTrim", 0, -model.photoBoothWall.xRange, model.photoBoothWall.xRange);
  CompoundParameter yTrim = new CompoundParameter("yTrim", 0, -model.photoBoothWall.yRange, model.photoBoothWall.yRange);

  public Kinect(LX lx) {
    super(lx);
    addModulator(index).start();

    addParameter(hue);

    addParameter(zKinectRangeMin);
    addParameter(zKinectRangeMax);

    addParameter(xTrim);
    addParameter(yTrim);

    this.xMax = model.photoBoothWall.xMax;
    this.xMin = model.photoBoothWall.xMin;
    this.yMax = model.photoBoothWall.yMax;
    this.yMin = model.photoBoothWall.yMin;
    this.zMax = model.photoBoothWall.xMax+200;
    this.zMin = model.photoBoothWall.zMin;

    //iterate through Symmetry model points to find xMin, xMax, yMin, yMax, zMin, zMax
    // for (Strip c : model.photoBoothWall.strips) {
    //   for (LXPoint p : c.points) {
    //     float xPos = p.x;
    //     float yPos = p.y;
    //     float zPos = p.z;

        // if (xPos > xMax) {
        //   xMax = xPos;
        // }
        // if (xPos < xMin) {
        //   xMin = xPos;
        // }
        // if (yPos > yMax) {
        //   yMax = yPos;
        // }
        // if (yPos < yMin) {
        //   yMin = yPos;
        // }
        // if (zPos > zMax) {
        //   zMax = zPos;
        // }
        // if (zPos < zMin) {
        //   zMin = zPos;
        // }


     // }
    //}

    // println(zMax);
    // println(zMin);
  }


  public void run(double deltaMs) {

    // only draw every 2nd pixel
    int skip = 2;

    // get raw depth data from Kinect as int array
    int[] depth = kinect2.getRawDepth();


    /* COMMENTING OUT WHILE EXPERIMENTING WITH Z-VALUE MAPPING
    int kinectZMax = 0;
    int kinectZMin = 5000;
    //iterate through kinect data array to find the maximum z value
    for (int x = 0; x < kinect2.depthWidth; x+=skip) {
      for (int y = 0; y < kinect2.depthHeight; y+=skip) {
        //pull the Z depth for the specific (x,y) coordinates
        int offset = x + y * kinect2.depthWidth;
        int kinectZ = Math.abs(depth[offset]);
        //as you iterate over Z values, test to see what the max Z and min Z values are
        if (kinectZ > kinectZMax) {
          kinectZMax = kinectZ;
        }
        if ((kinectZ < kinectZMin) && (kinectZ > 1)) {
          kinectZMin = kinectZ;
        }
      }
    }
    */

    int kinectZMax = 4500;
    int kinectZMin = 500;
    // int zKinectRangeMin = 500;
    // int zKinectRangeMax = 2000;
    int zScalarMin = 0;
    int zScalarMax = 1000;


    for (Strip c : model.photoBoothWall.strips) {

      int i = 0;

      //iterate through each LED light point in model
      for (LXPoint p : c.points) {

        //for each point, get the x, y, z coordinates
        float xPos = p.x+xTrim.getValuef();
        float yPos = p.y;//+yTrim.getValuef();
        float zPos = p.z;

        //map those x, y, z coordinates to the 512x424xZ-depth Kinect window
        float xMapped = map(xPos, xMin, xMax, 2, 510);
        float yMapped = map(yPos, yMin, yMax, 2, 422);
        float zMapped = map(zPos, zMin, zMax, zScalarMin, zScalarMax); // <-- relative z-value of LED pixel within model at (x,y)

        //use the same (x,y) coordinates to pull depth data from the Kinect
        // **DO YOU NEED TO RUN depthToPointCloudPos FUNCTION??**
        int xKinect = Math.round(xMapped);
        int yKinect = 422 - Math.round(yMapped); // subtract from max value in order to invert for screen coordinates

        //find the correct Z-depth in the [depth] array given the (x,y) coordinates
        int index = xKinect + yKinect * kinect2.depthWidth;
        int zKinect = depth[index];

        //map the Z-depth to the same Z-scale as the LED points, with the back of the Kinect depth cloud pushed behind the led array (ie last mapping value is larger)
        //float  zKinectMapped = map(zKinect, kinectZMin, kinectZMax, 0, 4000);

        //map the zKinectMapped integer to a float between 0 and 1 in order to interpolate across different hue values
        float hueMap = map(zKinect, 0, 1000, 0, 360);

        //map the zKinect depth value to a float between 0 and 1 in order to interpolate between two gradient colors
        float lerpMap = map(zKinect, kinectZMin, 1500, 0, 1);

        //OPTION 0A: HUE WITH DEPTH CLOUD
        if ((zKinect < zKinectRangeMin.getValuef() ) || (zKinect > zKinectRangeMax.getValuef()) || ((zKinect >= 0) && (zKinect <= 1))){
          //colors[p.index] = lx.hsb(200,50,25);
          colors[p.index] = lx.hsb(0,0,0);
        }
        else {
          //map kinect depth values to 0<>1000, same as LED-zposition values
          float kinectReMap = map(zKinect, zKinectRangeMin.getValuef(), zKinectRangeMax.getValuef(), zScalarMin, zScalarMax);
          if (kinectReMap < zMapped) {
            float hueMapAdjust = map(kinectReMap, zScalarMin, zScalarMax, 0, 360);
            colors[p.index] = lx.hsb(hueMapAdjust,100,100);
          }
          else {
            //colors[p.index] = lx.hsb(200,50,25);
            colors[p.index] = lx.hsb(0,0,0);
          }
        }
        ++i;
      }
    }

    for (Strip c : model.photoBoothWall.strips) {

      int i = 0;

      //iterate through each LED light point in model
      for (LXPoint p : c.points) {

        //for each point, get the x, y, z coordinates
        float xPos = p.x+xTrim.getValuef();
        float yPos = p.y;//+yTrim.getValuef();
        float zPos = p.z+200;

        //map those x, y, z coordinates to the 512x424xZ-depth Kinect window
        float xMapped = map(xPos, xMin, xMax, 2, 510);
        float yMapped = map(yPos, yMin, yMax, 2, 422);
        float zMapped = map(zPos, zMin, zMax, zScalarMin, zScalarMax); // <-- relative z-value of LED pixel within model at (x,y)

        //use the same (x,y) coordinates to pull depth data from the Kinect
        // **DO YOU NEED TO RUN depthToPointCloudPos FUNCTION??**
        int xKinect = Math.round(xMapped);
        int yKinect = 422 - Math.round(yMapped); // subtract from max value in order to invert for screen coordinates

        //find the correct Z-depth in the [depth] array given the (x,y) coordinates
        int index = xKinect + yKinect * kinect2.depthWidth;
        int zKinect = depth[index];

        //map the Z-depth to the same Z-scale as the LED points, with the back of the Kinect depth cloud pushed behind the led array (ie last mapping value is larger)
        //float  zKinectMapped = map(zKinect, kinectZMin, kinectZMax, 0, 4000);

        //map the zKinectMapped integer to a float between 0 and 1 in order to interpolate across different hue values
        float hueMap = map(zKinect, 0, 1000, 0, 360);

        //map the zKinect depth value to a float between 0 and 1 in order to interpolate between two gradient colors
        float lerpMap = map(zKinect, kinectZMin, 1500, 0, 1);

        //OPTION 0A: HUE WITH DEPTH CLOUD
        if ((zKinect < zKinectRangeMin.getValuef() ) || (zKinect > zKinectRangeMax.getValuef()) || ((zKinect >= 0) && (zKinect <= 1))){
          //colors[p.index] = lx.hsb(200,50,25);
          colors[p.index] = lx.hsb(0,0,0);
        }
        else {
          //map kinect depth values to 0<>1000, same as LED-zposition values
          float kinectReMap = map(zKinect, zKinectRangeMin.getValuef(), zKinectRangeMax.getValuef(), zScalarMin, zScalarMax);
          if (kinectReMap < zMapped) {
            float hueMapAdjust = map((kinectReMap+hue.getValuef()), zScalarMin, zScalarMax, 0, 360);
            colors[p.index] = lx.hsb(hueMapAdjust,100,100);
          }
          else {
            //colors[p.index] = lx.hsb(200,50,25);
            colors[p.index] = lx.hsb(0,0,0);
          }
        }
        ++i;
      }
    }
  }
}

PVector depthToPointCloudPos(int x, int y, float depthValue) {
  PVector point = new PVector();
  point.z = (depthValue);// / (1.0f); // Convert from mm to meters
  point.x = (x - CameraParams.cx) * point.z / CameraParams.fx;
  point.y = (y - CameraParams.cy) * point.z / CameraParams.fy;
  return point;
}

static class CameraParams {
  static float cx = 254.878f;
  static float cy = 205.395f;
  static float fx = 365.456f;
  static float fy = 365.456f;
  static float k1 = 0.0905474;
  static float k2 = -0.26819;
  static float k3 = 0.0950862;
  static float p1 = 0.0;
  static float p2 = 0.0;
}

// --- IDEAS ----
// 1. smooth out kinect value w/ Laplacian-style smoothing
// 2. trim out high and low values
// 3. set script-level thresholds to more easily tweak variables
// 4. set more discrete levels between different colors - like 8-10 total
// 5. establish a gradient - not just a crazy number of hues
// 6. definitely smooth out max Z-depth taken from Kinect