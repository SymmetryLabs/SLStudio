  /**
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\  
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 *
 * This file implements the mapping functions needed to lay out the physical
 * cubes and the output ports on the panda board. It should only be modified
 * when physical changes or tuning is being done to the structure.
 */

/* Notes
 *
 * 1) x: increases to the right, y: increases upward, z: increases away from you
 * 2) positive rotations on each axis go counter-clockwise when looking in the direction that the axis increases positively
 */

// TODO
// 1) Make groups on model for each component of the model
// 2) Make a test pattern that does the following
//    a) shows orientation of all strips (indicates what side controller is on)
//    b) shows four different colors to showcase orientation on desks
// 3) Figure out nice way of handling ids on logos and outputing
// 4) Put proper dimensions in for DESK_WIDTH, etc...

// Global Transformations
static final float globalOffsetX = 0;
static final float globalOffsetY = 0;
static final float globalOffsetZ = 0;

static final float globalRotationX = 0;
static final float globalRotationY = 0;
static final float globalRotationZ = 0;

// Global Constants
static final float INCHES_PER_METER = 39.3701;

static final int   STANDARD_NUM_POINTS = 73;
static final float DEFAULT_PIXEL_PITCH = (1.f/60) * INCHES_PER_METER;
static final float STANDARD_STRIP_LENGTH = DEFAULT_PIXEL_PITCH * STANDARD_NUM_POINTS;

// Ceiling Constants
static final int OUTER_SHORT_NUM_POINTS = STANDARD_NUM_POINTS - 16;
static final int INNER_SHORT_NUM_POINTS = STANDARD_NUM_POINTS - 10;

static final float OUTER_SHORT_STRIP_LENGTH = DEFAULT_PIXEL_PITCH * OUTER_SHORT_NUM_POINTS;
static final float INNER_SHORT_STRIP_LENGTH = DEFAULT_PIXEL_PITCH * INNER_SHORT_NUM_POINTS;

static final float OUTER_WIDTH = (STANDARD_STRIP_LENGTH*6) + (OUTER_SHORT_STRIP_LENGTH);
static final float INNER_WIDTH = (STANDARD_STRIP_LENGTH*2) + (INNER_SHORT_STRIP_LENGTH);

static final float INNER_PADDING = (OUTER_WIDTH - INNER_WIDTH) / 2.f;

// Pillar Constants
static final float PILLAR_HEIGHT = (STANDARD_STRIP_LENGTH*5);
static final float PILLAR_WIDTH = INNER_WIDTH;

// Desk Constants
static final int DESK_VERTICAL_NUM_POINTS = 50;
static final int DESK_HORIZONTAL_NUM_POINTS = 9;

static final float DESK_WIDTH = 35;
static final float DESK_HEIGHT = DESK_VERTICAL_NUM_POINTS * DEFAULT_PIXEL_PITCH;
static final float DESK_LENGTH = 40;

static final float DESK_STRIP_SPACING = 5;
static final int DESK_NUM_POINTS = STANDARD_NUM_POINTS - 14;

// Logo Constants
static final float LOGO_RIGGING_HEIGHT = 160;

// Photobooth Constants
static final int PHOTO_NUM_POINTS = 240;
static final float PHOTO_STRIP_SPACING = 3;
static final float PHOTO_PIXEL_PITCH = (1.f/144) * INCHES_PER_METER;
static final int NUM_PHOTO_STRIPS = 16;

static final float PHOTO_HEIGHT = PHOTO_NUM_POINTS * PHOTO_PIXEL_PITCH;
static final float PHOTO_WIDTH = NUM_PHOTO_STRIPS * PHOTO_STRIP_SPACING;

/* Rubrik Logos ---------------------------------------------------------------------------------------------------------------------*/
static final RubrikLogoConfig[] RUBRIK_LOGOS_CONFIG = {
  /* (Notes)
   * Refer to wiring diagram from Tim... (as far as what the terms (top, left), (right, bottom) and such mean
   * Turn on the RubrikTestPattern to show the colors in both the visualizer and on the structure.
   * Use the RubrikTestPattern to compare the visualizer to the physical logo to make sure it's mapped/oriented correctly.
   *
   * RubrikLogoConfig ordering:
   *    { controller ids... }, { x, y, z }, { xRot, yRot, zRot }
   *
   * The ids go in the following order:
   * (Top, Left: RED)
   * (Top, Right: BLUE)
   * (Right, Top: GREEN)
   * (Right, Bottom: ORANGE)
   * (Bottom, Right: DARKER PURPLE)
   * (Bottom, Left: TEAL)
   * (Left, Bottom: YELLOW)
   * (Left, Top: LIGHTER PURPLE)
   */

  new RubrikLogoConfig(
    new String[] { "0", "0", "0", "0", "0", "0", "0", "0" },
    new float[] { OUTER_WIDTH/2-20, LOGO_RIGGING_HEIGHT, -250 },
    new float[] { 0, 0, 0 }
  ),

  new RubrikLogoConfig(
    new String[] { "0", "0", "0", "0", "0", "0", "0", "0" },
    new float[] { 80, LOGO_RIGGING_HEIGHT, -OUTER_WIDTH/2+20 },
    new float[] { 0, 90, 0 }
  ),

  new RubrikLogoConfig(
    new String[] { "0", "0", "0", "0", "0", "0", "0", "0" },
    new float[] { 250, LOGO_RIGGING_HEIGHT, -OUTER_WIDTH/2-20 },
    new float[] { 0, -90, 0 }
  ),

  new RubrikLogoConfig(
    new String[] { "0", "0", "0", "0", "0", "0", "0", "0" },
    new float[] { OUTER_WIDTH/2+20, LOGO_RIGGING_HEIGHT, -72 },
    new float[] { 0, 180, 0 }
  ),
};
/* -----------------------------------------------------------------------------------------------------------------------------------*/

/* Desks ----------------------------------------------------------------------------------------------------------------------------*/
static final DeskConfig[] DESK_CONFIG = {
    /* (Notes)
     * Turn on the RubrikTestPattern to show the colors in both the visualizer and on the structure.
     * Use the RubrikTestPattern to compare the visualizer to the physical desks to make sure they're mapped/oriented correctly.
     *
     * DeskConfig ordering: { controller ids }, { x, y, z }, yRot
     *
     * The ids go in the following order:
     * (Left, Top: RED)
     * (Left, Bottom: YELLOW)
     * (Right, Top: GREEN)
     * (Right, Bottom: BLUE)
     */

    new DeskConfig(new String[] { "0", "0", "0", "0" }, new float[] { 0,           0,            0 }, -45),
    new DeskConfig(new String[] { "0", "0", "0", "0" }, new float[] { OUTER_WIDTH, 0,            0 },  45),
    new DeskConfig(new String[] { "0", "0", "0", "0" }, new float[] { OUTER_WIDTH, 0, -OUTER_WIDTH }, -45),
    new DeskConfig(new String[] { "0", "0", "0", "0" }, new float[] { 0,           0, -OUTER_WIDTH },  45)
};
/* -----------------------------------------------------------------------------------------------------------------------------------*/

/* Photo Booth ----------------------------------------------------------------------------------------------------------------------*/
static final float photoBoothOffsetX = OUTER_WIDTH/2 - PHOTO_WIDTH/2;
static final float photoBoothOffsetY = 0;
static final float photoBoothOffsetZ = -OUTER_WIDTH;

static final float photoBoothRotationX = 0;
static final float photoBoothRotationY = 0;
static final float photoBoothRotationZ = 0;

static final StripConfig[] PHOTO_BOOTH_STRIP_CONFIG = {
  /* (Notes)
   * StripConfig ordering: id, { x, y, z }, { xRot, yRot, zRot }, numPoints, pixelPitch }
   *
   * The order of these bars goes from left to right. But since the wall is double-sided, it doesn't matter too much...
   * just make sure its orientated correctly on the y-axis when you map it.
   */

  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*0,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*1,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*2,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*3,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*4,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*5,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*6,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*7,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*8,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*9,  0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*10, 0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*11, 0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*12, 0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*13, 0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*14, 0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
  new StripConfig("0", new float[] {PHOTO_STRIP_SPACING*15, 0, 0 }, new float[] { 0, 0, 90 }, PHOTO_NUM_POINTS, PHOTO_PIXEL_PITCH),
};
/* -----------------------------------------------------------------------------------------------------------------------------------*/


/* Ceiling ---------------------------------------------------------------------------------------------------------------------------*/
static final float ceilingOffsetX = 0;
static final float ceilingOffsetY = PILLAR_HEIGHT;
static final float ceilingOffsetZ = 0;

static final float ceilingRotationX = 90;
static final float ceilingRotationY = 0;
static final float ceilingRotationZ = 0;

static final StripConfig[] CEILING_CONFIG = {
  // looking down, strips go left to right
  // the top can be arbitrary

  // controller id, {x, y, z}, {xRot, yRot, zRot}, num leds

  // CEILING | OUTER ------------------------------------------------------------------------------------------------------------
  // outer (top)
  new StripConfig("0", new float[] { STANDARD_STRIP_LENGTH*0, 0, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { STANDARD_STRIP_LENGTH*1, 0, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { STANDARD_STRIP_LENGTH*2, 0, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { STANDARD_STRIP_LENGTH*3, 0, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { STANDARD_STRIP_LENGTH*4, 0, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { STANDARD_STRIP_LENGTH*5, 0, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { STANDARD_STRIP_LENGTH*6, 0, 0 }, new float[] { 0, 0, 0 }, OUTER_SHORT_NUM_POINTS),

  // outer (right)
  new StripConfig("0", new float[] { OUTER_WIDTH, -STANDARD_STRIP_LENGTH*0, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH, -STANDARD_STRIP_LENGTH*1, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH, -STANDARD_STRIP_LENGTH*2, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH, -STANDARD_STRIP_LENGTH*3, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH, -STANDARD_STRIP_LENGTH*4, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH, -STANDARD_STRIP_LENGTH*5, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH, -STANDARD_STRIP_LENGTH*6, 0 }, new float[] { 0, 0, -90 }, OUTER_SHORT_NUM_POINTS),

  // outer (bottom)
  new StripConfig("0", new float[] { OUTER_WIDTH-STANDARD_STRIP_LENGTH*0, -OUTER_WIDTH, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH-STANDARD_STRIP_LENGTH*1, -OUTER_WIDTH, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH-STANDARD_STRIP_LENGTH*2, -OUTER_WIDTH, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH-STANDARD_STRIP_LENGTH*3, -OUTER_WIDTH, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH-STANDARD_STRIP_LENGTH*4, -OUTER_WIDTH, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH-STANDARD_STRIP_LENGTH*5, -OUTER_WIDTH, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { OUTER_WIDTH-STANDARD_STRIP_LENGTH*6, -OUTER_WIDTH, 0 }, new float[] { 0, 0, -180 }, OUTER_SHORT_NUM_POINTS),

  // outer (left)
  new StripConfig("0", new float[] { 0, -OUTER_WIDTH+STANDARD_STRIP_LENGTH*6, 0 }, new float[] { 0, 0, -270 }, OUTER_SHORT_NUM_POINTS),
  new StripConfig("0", new float[] { 0, -OUTER_WIDTH+STANDARD_STRIP_LENGTH*5, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { 0, -OUTER_WIDTH+STANDARD_STRIP_LENGTH*4, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { 0, -OUTER_WIDTH+STANDARD_STRIP_LENGTH*3, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { 0, -OUTER_WIDTH+STANDARD_STRIP_LENGTH*2, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { 0, -OUTER_WIDTH+STANDARD_STRIP_LENGTH*1, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { 0, -OUTER_WIDTH+STANDARD_STRIP_LENGTH*0, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),

  // CEILING | INNER -------------------------------------------------------------------------------------------------------------
  // inner (top)
  new StripConfig("0", new float[] { INNER_PADDING+INNER_WIDTH-STANDARD_STRIP_LENGTH*0, -INNER_PADDING, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING+INNER_WIDTH-STANDARD_STRIP_LENGTH*1, -INNER_PADDING, 0 }, new float[] { 0, 0, -180 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING+INNER_WIDTH-STANDARD_STRIP_LENGTH*2, -INNER_PADDING, 0 }, new float[] { 0, 0, -180 }, INNER_SHORT_NUM_POINTS),

  // // inner (left)
  new StripConfig("0", new float[] { INNER_PADDING, -INNER_PADDING-STANDARD_STRIP_LENGTH*0, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING, -INNER_PADDING-STANDARD_STRIP_LENGTH*1, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING, -INNER_PADDING-STANDARD_STRIP_LENGTH*2, 0 }, new float[] { 0, 0, -90 }, INNER_SHORT_NUM_POINTS),

  // // inner (bottom)
  new StripConfig("0", new float[] { INNER_PADDING+STANDARD_STRIP_LENGTH*0, -INNER_PADDING-INNER_WIDTH, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING+STANDARD_STRIP_LENGTH*1, -INNER_PADDING-INNER_WIDTH, 0 }, new float[] { 0, 0, 0 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING+STANDARD_STRIP_LENGTH*2, -INNER_PADDING-INNER_WIDTH, 0 }, new float[] { 0, 0, 0 }, INNER_SHORT_NUM_POINTS),

  // // inner (right)
  new StripConfig("0", new float[] { INNER_PADDING+INNER_WIDTH, -INNER_PADDING-INNER_WIDTH+STANDARD_STRIP_LENGTH*0, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING+INNER_WIDTH, -INNER_PADDING-INNER_WIDTH+STANDARD_STRIP_LENGTH*1, 0 }, new float[] { 0, 0, -270 }, STANDARD_NUM_POINTS),
  new StripConfig("0", new float[] { INNER_PADDING+INNER_WIDTH, -INNER_PADDING-INNER_WIDTH+STANDARD_STRIP_LENGTH*2, 0 }, new float[] { 0, 0, -270 }, INNER_SHORT_NUM_POINTS),

};
/* -----------------------------------------------------------------------------------------------------------------------------------*/

/* Pillar ----------------------------------------------------------------------------------------------------------------------------*/
static final float pillarOffsetX = INNER_PADDING;
static final float pillarOffsetY = PILLAR_HEIGHT;
static final float pillarOffsetZ = -INNER_PADDING;

static final float pillarRotationX = 0;
static final float pillarRotationY = 0;
static final float pillarRotationZ = 0;

static final StripConfig[] PILLAR_CONFIG = {

    // back left corner
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*0, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*1, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*2, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*3, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*4, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*5, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),

    // back right corner
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*0, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*1, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*2, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*3, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*4, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*5, 0 }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),

    // front right corner
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*0, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*1, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*2, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*3, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*4, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { PILLAR_WIDTH, -STANDARD_STRIP_LENGTH*5, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),

    // front left corner
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*0, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*1, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*2, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*3, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*4, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
    new StripConfig("0", new float[] { 0, -STANDARD_STRIP_LENGTH*5, -PILLAR_WIDTH }, new float[] { 0, 0, -90 }, STANDARD_NUM_POINTS),
};
/* -----------------------------------------------------------------------------------------------------------------------------------*/

static class RubrikLogoConfig {
  String[] ids;
  float[] coordinates;
  float[] rotations;

  RubrikLogoConfig(String[] ids, float[] coordinates, float[] rotations) {
    this.ids = ids;
    this.coordinates = coordinates;
    this.rotations = rotations;
  }
}

static class DeskConfig {
  String[] ids;
  float[] coordinates;
  float[] rotations;

  DeskConfig(String[] ids, float[] coordinates, float yRot) {
    this.ids = ids;
    this.coordinates = coordinates;
    this.rotations = new float[] { 0, yRot, 0 };
  }
};

static class StripConfig {
  String id;
  int numPoints;
  float pixelPitch;
  float x;
  float y;
  float z;
  float xRot;
  float yRot;
  float zRot;

  StripConfig(String id, float[] coords, float[] rotations, int numPoints) {
    this(id, coords, rotations, numPoints, DEFAULT_PIXEL_PITCH);
  }

  StripConfig(String id, float[] coords, float[] rotations, int numPoints, float pixelPitch) {
    this.id = id;
    this.numPoints = numPoints;
    this.pixelPitch = pixelPitch;
    this.x = coords[0];
    this.y = coords[1];
    this.z = coords[2];
    this.xRot = rotations[0];
    this.yRot = rotations[1];
    this.zRot = rotations[2];
  }
}

Map<String, String> macToPhysid = new HashMap<String, String>();
Map<String, String> physidToMac = new HashMap<String, String>();

public SLModel buildModel() {

  byte[] bytes = loadBytes("physid_to_mac.json");
  if (bytes != null) {
    try {
      JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
      for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
        macToPhysid.put(entry.getValue().getAsString(), entry.getKey());
        physidToMac.put(entry.getKey(), entry.getValue().getAsString());
      }
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    }
  }

  // Any global transforms
  LXTransform globalTransform = new LXTransform();
  globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
  globalTransform.rotateY(globalRotationY * PI / 180.);
  globalTransform.rotateX(globalRotationX * PI / 180.);
  globalTransform.rotateZ(globalRotationZ * PI / 180.);

  /* Cubes ----------------------------------------------------------*/
  List<Tower> towers = new ArrayList<Tower>();
  List<Cube> allCubes = new ArrayList<Cube>();
  /*-----------------------------------------------------------------*/

  /* Strips ----------------------------------------------------------*/
  List<Strip> strips = new ArrayList<Strip>();

  // Ceiling
  Ceiling ceiling;
  List<Strip> ceilingStrips = new ArrayList<Strip>();

  globalTransform.push();
  globalTransform.translate(ceilingOffsetX, ceilingOffsetY, ceilingOffsetZ);
  globalTransform.rotateX(ceilingRotationX * PI / 180.);
  globalTransform.rotateY(ceilingRotationY * PI / 180.);
  globalTransform.rotateZ(ceilingRotationZ * PI / 180.);

  for (StripConfig stripConfig : CEILING_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, stripConfig.pixelPitch);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateX(stripConfig.xRot * PI / 180.);
    globalTransform.rotateY(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    Strip strip = new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true);
    strips.add(strip);
    ceilingStrips.add(strip);
    globalTransform.pop();
  }
  globalTransform.pop();
  ceiling = new Ceiling(ceilingStrips);

  // Pillar
  Pillar pillar;
  List<Strip> pillarStrips = new ArrayList<Strip>();

  globalTransform.push();
  globalTransform.translate(pillarOffsetX, pillarOffsetY, pillarOffsetZ);
  globalTransform.rotateX(pillarRotationX * PI / 180.);
  globalTransform.rotateY(pillarRotationY * PI / 180.);
  globalTransform.rotateZ(pillarRotationZ * PI / 180.);

  for (StripConfig stripConfig : PILLAR_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, stripConfig.pixelPitch);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateX(stripConfig.xRot * PI / 180.);
    globalTransform.rotateY(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    Strip strip = new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true);
    strips.add(strip);
    pillarStrips.add(strip);
    globalTransform.pop();
  }
  globalTransform.pop();
  pillar = new Pillar(pillarStrips);

  // Rubrik Logos (Real models)
  List<RubrikLogo> logos = new ArrayList<RubrikLogo>();

  for (RubrikLogoConfig logoConfig : RUBRIK_LOGOS_CONFIG) {
    logos.add(new RubrikLogo(logoConfig.ids, logoConfig.coordinates, logoConfig.rotations, globalTransform));
  }

  // Desks (Real Models)
  List<Desk> desks = new ArrayList<Desk>();

  for (DeskConfig deskConfig : DESK_CONFIG) {
    desks.add(new Desk(deskConfig.ids, deskConfig.coordinates, deskConfig.rotations, globalTransform));
  }

  // Photobooth (Make into a real model)
  globalTransform.push();
  globalTransform.translate(photoBoothOffsetX, photoBoothOffsetY, photoBoothOffsetZ);
  globalTransform.rotateX(photoBoothRotationX * PI / 180.);
  globalTransform.rotateY(photoBoothRotationY * PI / 180.);
  globalTransform.rotateZ(photoBoothRotationZ * PI / 180.);

  PhotoBoothWall photoBoothWall = null;
  List<Strip> photoBoothStrips = new ArrayList<Strip>();

  for (StripConfig stripConfig : PHOTO_BOOTH_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, stripConfig.pixelPitch);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateX(stripConfig.xRot * PI / 180.);
    globalTransform.rotateY(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    Strip strip = new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true);
    strips.add(strip);
    photoBoothStrips.add(strip);
    globalTransform.pop();
  }

  photoBoothWall = new PhotoBoothWall(photoBoothStrips);
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  // not used
  Cube[] allCubesArr = new Cube[allCubes.size()];
  for (int i = 0; i < allCubesArr.length; i++) {
    allCubesArr[i] = allCubes.get(i);
  }

  return new SLModel(towers, allCubesArr, strips, ceiling, pillar, logos, desks, photoBoothWall);
}

public SLModel getModel() {
  return buildModel();
}

/*
 * Mapping Pattern
 *---------------------------------------------------------------------------*/
public class MappingPattern extends SLPattern {
  private final SinLFO pulse = new SinLFO(20, 100, 800);

  public color mappedAndOnNetworkColor    = LXColor.GREEN;
  public color mappedButNotOnNetworkColor = LXColor.BLACK;
  public color unMappedButOnNetworkColor  = LXColor.BLACK;

  public MappingPattern(LX lx) {
    super(lx);
    addModulator(pulse).start();

    final LXParameterListener resetBasis = new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        pulse.setBasis(0);
      }
    };

    mappingMode.mode.addListener(resetBasis);
    mappingMode.displayMode.addListener(resetBasis);
    mappingMode.selectedMappedFixture.addListener(resetBasis);
    mappingMode.selectedUnMappedFixture.addListener(resetBasis);
  }

  public void run(double deltaMs) {
    if (!mappingMode.enabled.isOn()) return;
    setColors(0);
    updateColors();

    if (mappingMode.inMappedMode())
      loopMappedFixtures(deltaMs);
    else loopUnMappedFixtures(deltaMs);
  }

  private void updateColors() {
    mappedButNotOnNetworkColor = lx.hsb(LXColor.h(LXColor.RED), 100, pulse.getValuef());
    unMappedButOnNetworkColor = lx.hsb(LXColor.h(LXColor.BLUE), 100, pulse.getValuef());
  }

  private void loopMappedFixtures(double deltaMs) {
    if (mappingMode.inDisplayAllMode()) {

      for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
        setFixtureColor(id, mappedAndOnNetworkColor);

      for (String id : mappingMode.fixturesMappedButNotOnNetwork)
        setFixtureColor(id, mappedButNotOnNetworkColor);

    } else {
      String selectedId = mappingMode.selectedMappedFixture.getOption();

      for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
        setFixtureColor(id, mappedAndOnNetworkColor, true);

      if (mappingMode.fixturesMappedAndOnTheNetwork.contains(selectedId))
        setFixtureColor(selectedId, mappedAndOnNetworkColor);

      if (mappingMode.fixturesMappedButNotOnNetwork.contains(selectedId))
        setFixtureColor(selectedId, mappedButNotOnNetworkColor);
    }
  }

  private void loopUnMappedFixtures(double deltaMs) {
    for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
      setFixtureColor(id, mappedAndOnNetworkColor, true);
  }

  private void setFixtureColor(String id, color col) {
    setFixtureColor(id, col, false);
  }

  private void setFixtureColor(String id, color col, boolean dotted) {
    if (id.equals("-")) return;

    // we iterate all cubes and call continue here because multiple cubes might have zero as id
    for (Cube c : model.cubes) {
      if (!c.id.equals(id)) continue;

      LXPoint[] points = c.points;
      for (int i = 0; i < points.length; i++) {
        if (dotted)
          col = (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.GREEN, 0.2) : LXColor.BLACK;

        setColor(points[i].index, col);
      }
    }
  }
}

/*
 * Mapping Mode
 * (TODO) 
 *  1) iterate through mapped cubes in order (tower by tower, cube by cube)
 *  2) get cubes not mapped but on network to pulse
 *  3) get a "display orientation" mode
 *---------------------------------------------------------------------------*/
public static enum MappingModeType {MAPPED, UNMAPPED};
public static enum MappingDisplayModeType {ALL, ITERATE};

public class MappingMode {
  private LXChannel mappingChannel = null;
  private LXPattern mappingPattern = null;

  public final BooleanParameter enabled;
  public final EnumParameter<MappingModeType> mode;
  public final EnumParameter<MappingDisplayModeType> displayMode;
  //public final BooleanParameter displayOrientation;

  public final DiscreteParameter selectedMappedFixture;
  public final DiscreteParameter selectedUnMappedFixture;

  public final List<String> fixturesMappedAndOnTheNetwork = new ArrayList<String>();
  public final List<String> fixturesMappedButNotOnNetwork = new ArrayList<String>();
  public final List<String> fixturesOnNetworkButNotMapped = new ArrayList<String>();

  MappingMode(LX lx) {
    this.enabled = new BooleanParameter("enabled", false)
     .setDescription("Mapping Mode: toggle on/off");

    this.mode = new EnumParameter<MappingModeType>("mode", MappingModeType.MAPPED)
     .setDescription("Mapping Mode: toggle between mapped/unmapped fixtures");

    this.displayMode = new EnumParameter<MappingDisplayModeType>("displayMode", MappingDisplayModeType.ALL)
     .setDescription("Mapping Mode: display all mapped/unmapped fixtures");

    // this.displayOrientation = new BooleanParameter("displayOrientation", false)
    //  .setDescription("Mapping Mode: display colors on strips to indicate it's orientation");

    for (Cube cube : model.cubes)
      fixturesMappedButNotOnNetwork.add(cube.id);

    this.selectedMappedFixture = new DiscreteParameter("selectedMappedFixture", fixturesMappedButNotOnNetwork.toArray());
    this.selectedUnMappedFixture = new DiscreteParameter("selectedUnMappedFixture", new String[] {"-"});

    controllers.addListener(new ListListener<SLController>() {
      void itemAdded(final int index, final SLController c) {
        if (isFixtureMapped(c.cubeId)) {
          fixturesMappedButNotOnNetwork.remove(c.cubeId);
          fixturesMappedAndOnTheNetwork.add(c.cubeId);
        } else {
          fixturesOnNetworkButNotMapped.add(c.cubeId);
        }

        Object[] arr1 = fixturesMappedAndOnTheNetwork.toArray();
        Object[] arr2 = fixturesOnNetworkButNotMapped.toArray();

        selectedMappedFixture.setObjects(arr1.length > 0 ? arr1 : new String[] {"-"});
        selectedUnMappedFixture.setObjects(arr2.length > 0 ? arr2 : new String[] {"-"});
      }
      void itemRemoved(final int index, final SLController c) {}
    });

    enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (((BooleanParameter)p).isOn())
          addChannel();
        else removeChannel();
      }
    });
  }

  private boolean isFixtureMapped(String id) {
    for (Cube fixture : model.cubes) {
      if (fixture.id.equals(id))
        return true;
    }
    return false;
  }

  public boolean inMappedMode() {
    return mode.getObject() == MappingModeType.MAPPED;
  }

  public boolean inUnMappedMode() {
    return mode.getObject() == MappingModeType.UNMAPPED;
  }

  public boolean inDisplayAllMode() {
    return displayMode.getObject() == MappingDisplayModeType.ALL;
  }

  public boolean inIterateFixturesMode() {
    return displayMode.getObject() == MappingDisplayModeType.ITERATE;
  }

  public String getSelectedMappedFixtureId() {
    return (String)mappingMode.selectedMappedFixture.getOption();
  }

  public String getSelectedUnMappedFixtureId() {
    return (String)mappingMode.selectedUnMappedFixture.getOption();
  }

  public boolean isSelectedUnMappedFixture(String id) {
    return id.equals(mappingMode.selectedUnMappedFixture.getOption());
  }

  public color getUnMappedColor() {
    // if (mappingPattern != null)
    //   return mappingPattern.getUnMappedButOnNetworkColor;
    // return 0;
    return LXColor.RED; // temp
  }

  private void addChannel() {
    mappingPattern = new MappingPattern(lx);
    mappingChannel = lx.engine.addChannel(new LXPattern[] {mappingPattern});

    for (LXChannel channel : lx.engine.channels)
      channel.cueActive.setValue(false);

    mappingChannel.fader.setValue(1);
    mappingChannel.label.setValue("Mapping");
    mappingChannel.cueActive.setValue(true);
  }

  private void removeChannel() {
    lx.engine.removeChannel(mappingChannel);
    mappingChannel = null;
    mappingPattern = null;
  }
}

/*
 * Mapping Mode: UI Window
 *---------------------------------------------------------------------------*/
class UIMapping extends UICollapsibleSection {

  UIMapping(LX lx, UI ui, float x, float y, float w) {
    super(ui, x, y, w, 124);
    setTitle("MAPPING");
    setTitleX(20);

    addTopLevelComponent(new UIButton(4, 4, 12, 12) {
      @Override
      public void onToggle(boolean isOn) {
        redraw();
      }
    }.setParameter(mappingMode.enabled).setBorderRounding(4));

    final UIToggleSet toggleMode = new UIToggleSet(0, 2, getContentWidth(), 18)
     .setEvenSpacing().setParameter(mappingMode.mode);
    toggleMode.addToContainer(this);

    final UIMappedPanel mappedPanel = new UIMappedPanel(ui, 0, 20, getContentWidth(), 40);
    mappedPanel.setVisible(mappingMode.inMappedMode());
    mappedPanel.addToContainer(this);

    final UIUnMappedPanel unMappedPanel = new UIUnMappedPanel(ui, 0, 20, getContentWidth(), 40);
    unMappedPanel.setVisible(mappingMode.inUnMappedMode());
    unMappedPanel.addToContainer(this);

    mappingMode.mode.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        mappedPanel.setVisible(mappingMode.inMappedMode());
        unMappedPanel.setVisible(mappingMode.inUnMappedMode());
        redraw();
      }
    });
  }

  private class UIMappedPanel extends UI2dContainer {
    UIMappedPanel(UI ui, float x, float y, float w, float h) {
      super(x, y, w, h);

      final UIToggleSet toggleDisplayMode = new UIToggleSet(0, 2, 112, 18)
       .setEvenSpacing().setParameter(mappingMode.displayMode);
      toggleDisplayMode.addToContainer(this);

      final UILabel selectedFixtureLabel = new UILabel(0, 24, getContentWidth(), 54)
        .setLabel("");
      selectedFixtureLabel.setBackgroundColor(#333333)
        .setFont(createFont("ArialUnicodeMS-10.vlw", 43))
        .setTextAlignment(PConstants.CENTER, PConstants.TOP);
      selectedFixtureLabel.addToContainer(this);
      mappingMode.selectedMappedFixture.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          if (mappingMode.inMappedMode())
            selectedFixtureLabel.setLabel(mappingMode.getSelectedMappedFixtureId());
        }
      });

      mappingMode.displayMode.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          String label = mappingMode.inDisplayAllMode()
            ? "" : mappingMode.getSelectedMappedFixtureId();
          selectedFixtureLabel.setLabel(label);
          redraw();
        }
      });

      final UIButton decrementSelectedFixture = new UIButton(122, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedMappedFixture.decrement(1);
        }
      }.setLabel("-").setMomentary(true);
      decrementSelectedFixture.addToContainer(this);

      final UIButton incrementSelectedFixture = new UIButton(147, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedMappedFixture.increment(1);
        }
      }.setLabel("+").setMomentary(true);
      incrementSelectedFixture.addToContainer(this);
    }
  }

  private class UIUnMappedPanel extends UI2dContainer {
    UIUnMappedPanel(UI ui, float x, float y, float w, float h) {
      super(x, y, w, h);

      final UIToggleSet toggleDisplayMode = new UIToggleSet(0, 2, 112, 18)
       .setEvenSpacing().setParameter(mappingMode.displayMode);
      toggleDisplayMode.addToContainer(this);

      final UILabel selectedFixtureLabel = new UILabel(0, 24, getContentWidth(), 54)
        .setLabel("");
      selectedFixtureLabel.setBackgroundColor(#333333)
        .setFont(createFont("ArialUnicodeMS-10.vlw", 43))
        .setTextAlignment(PConstants.CENTER, PConstants.TOP);
      selectedFixtureLabel.addToContainer(this);
      mappingMode.selectedUnMappedFixture.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          if (mappingMode.inUnMappedMode())
            selectedFixtureLabel.setLabel(mappingMode.getSelectedUnMappedFixtureId());
        }
      });

      mappingMode.displayMode.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          String label = mappingMode.inDisplayAllMode()
            ? "" : mappingMode.getSelectedUnMappedFixtureId();
          selectedFixtureLabel.setLabel(label);
          redraw();
        }
      });

      final UIButton decrementSelectedFixture = new UIButton(122, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedUnMappedFixture.decrement(1);
        }
      }.setLabel("-").setMomentary(true);
      decrementSelectedFixture.addToContainer(this);

      final UIButton incrementSelectedFixture = new UIButton(147, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedUnMappedFixture.increment(1);
        }
      }.setLabel("+").setMomentary(true);
      incrementSelectedFixture.addToContainer(this);
    }

  }
}
