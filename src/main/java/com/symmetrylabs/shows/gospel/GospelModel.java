package com.symmetrylabs.shows.gospel;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXVector;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.transform.LXTransform;
import com.symmetrylabs.slstudio.model.SLModel;


public class GospelModel extends StripsModel<Strip> {

  static float PI = (float)Math.PI;

  public static class StairsModel extends LXModel {

    public final List<Strip> strips;

    public StairsModel(List<Strip> strips) {
      super("stairs", new Fixture(strips));
      Fixture fixture = (Fixture) this.fixtures.get(0);

      this.strips = fixture.strips;
    }

    private static class Fixture extends LXAbstractFixture {

      public final List<Strip> strips = new ArrayList<Strip>();

      private Fixture(List<Strip> strips) {
        for (Strip strip : strips) {
          this.strips.add(strip);

          for (LXPoint p : strip.points) {
            this.points.add(p);
          }
        }
      }

    }

  }

  public static class BarModel extends LXModel {

    public final List<Strip> strips;

    public BarModel(List<Strip> strips) {
      super("bar", new Fixture(strips));
      Fixture fixture = (Fixture) this.fixtures.get(0);

      this.strips = fixture.strips;
    }

    private static class Fixture extends LXAbstractFixture {

      public final List<Strip> strips = new ArrayList<Strip>();

      private Fixture(List<Strip> strips) {
        for (Strip strip : strips) {
          this.strips.add(strip);

          for (LXPoint p : strip.points) {
            this.points.add(p);
          }
        }
      }

    }

  }

  static class StripConfig {
    String id;
    List<String> classes;
    int numPoints;
    float length;
    float x;
    float y;
    float z;
    float xRot;
    float yRot;
    float zRot;

    StripConfig(String id, String[] classes, float[] coordinates, float[] rotations, int numPoints) {
      this(id, classes, coordinates[0], coordinates[1], coordinates[2], rotations[0], rotations[1], rotations[2], numPoints);
    }

    StripConfig(String id, float[] coordinates, float[] rotations, int numPoints) {
      this(id, new String[0], coordinates[0], coordinates[1], coordinates[2], rotations[0], rotations[1], rotations[2], numPoints);
    }

    StripConfig(String id, String[] classes, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints) {
      this.id = id;
      this.classes = Arrays.asList(classes);
      this.numPoints = numPoints;
      this.x = x;
      this.y = y;
      this.z = z;
      this.xRot = xRot;
      this.yRot = yRot;
      this.zRot = zRot;
    }
  }


  static final float globalOffsetX = 0;
  static final float globalOffsetY = 0;
  static final float globalOffsetZ = 0;

  static final float globalRotationX = 0;
  static final float globalRotationY = 0;
  static final float globalRotationZ = 0;

  static final float CUBE_WIDTH = 24;
  static final float CUBE_HEIGHT = 24;
  static final float TOWER_WIDTH = 24;
  static final float TOWER_HEIGHT = 24;
  static final float CUBE_SPACING = 2.5f;

  static final float TOWER_VERTICAL_SPACING = 2.5f;
  static final float TOWER_RISER = 14;
  static final float SP = 24;
  static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

  // static final BulbConfig[] BULB_CONFIG = {
  //     // new BulbConfig("lifx-1", -50, 50, -30),
  //     // new BulbConfig("lifx-2", 0, 50, 0),
  //     // new BulbConfig("lifx-3", -65, 20, -100),
  //     // new BulbConfig("lifx-4", 0, 0, 0),
  //     // new BulbConfig("lifx-5", 0, 0, 0),
  // };

  static final float PIXEL_PITCH = 1.25f; // double check measurement

  static final float CONTOUR_WIDTH = 5; // needs measurement;
  static final float DISTANCE_TO_FLOOR = 100;


  /**
   * Vip Lounge
   *---------------------------------------------------------------------------------------------*/
  static final float vip_lounge_offset_x = 0;
  static final float vip_lounge_offset_y = 0;
  static final float vip_lounge_offset_z = 0;
  static final float vip_lounge_rotation_x = 0;
  static final float vip_lounge_rotation_y = 0;
  static final float vip_lounge_rotation_z = 0;

  static final StripConfig[] VIP_LOUGNE_STRIP_CONFIG = {
    //strip id, {x, y, z}, {xRot, yRot, zRot}, num leds, length

    // horizontals - bottom
    new StripConfig("vip-lounge-strip1", new float[] { 0, 0, 0}, new float[] {0, 0, 0}, 44),
    new StripConfig("vip-lounge-strip2", new float[] {64, 0, 0}, new float[] {0, 0, 0}, 141),

    // horizontals - middle bottom
    new StripConfig("vip-lounge-strip3", new float[] {  0, 62, 0}, new float[] {0, 0, 0}, 92),
    new StripConfig("vip-lounge-strip4", new float[] {123, 62, 0}, new float[] {0, 0, 0}, 98),

    // horizontals - middle top
    new StripConfig("vip-lounge-strip5", new float[] {0,   68, 0}, new float[] {0, 0, 0}, 121),
    new StripConfig("vip-lounge-strip6", new float[] {163, 68, 0}, new float[] {0, 0, 0}, 66),

    // // horizontals - top
    new StripConfig("vip-lounge-strip7", new float[] {0, 130, 0}, new float[] {0, 0, 0}, 71),
    new StripConfig("vip-lounge-strip8", new float[] {97, 130, 0}, new float[] {0, 0, 0}, 119+7), 

    // // verticals - left
    new StripConfig("vip-lounge-strip9",  new float[] {0, 0, 0}, new float[] {0, 0, 90}, 50), 
    new StripConfig("vip-lounge-strip10", new float[] {0, 68, 0}, new float[] {0, 0, 90}, 49), 

    // // verticals - right 
    new StripConfig("vip-lounge-strip11", new float[] {238, 0, 0}, new float[] {0, 0, 90}, 39), 
    new StripConfig("vip-lounge-strip12", new float[] {240, 49, 0}, new float[] {0, 0, 0}, 3),
    new StripConfig("vip-lounge-strip13", new float[] {244, 48.5f, 0}, new float[] {0, 0, 90}, 10), 
    new StripConfig("vip-lounge-strip14", new float[] {244, 68  , 0}, new float[] {0, 0, 90}, 49), 

    // // bottom angle (left, right)
    new StripConfig("vip-lounge-strip15", new float[] {55, 1, 0}, new float[] {0, 0, 45}, 69), 
    new StripConfig("vip-lounge-strip16", new float[] {63, 1, 0}, new float[] {0, 0, 45}, 67), 

    // // top angle (left, right)
    new StripConfig("vip-lounge-strip17", new float[] {152, 68, 0}, new float[] {0, 0, 137.7f}, 72), 
    new StripConfig("vip-lounge-strip18", new float[] {162, 67, 0}, new float[] {0, 0, 137.7f}, 74), 


    // floor - top
    new StripConfig("vip-lounge-strip19", new float[] {260, 55, DISTANCE_TO_FLOOR-10}, new float[] {0, 0, 180}, 100), 
    new StripConfig("vip-lounge-strip20", new float[] {135, 55, DISTANCE_TO_FLOOR-10}, new float[] {0, 0, 136}, 97), 
    new StripConfig("vip-lounge-strip21", new float[] {50, 230-94, DISTANCE_TO_FLOOR-10}, new float[] {0, 0, -90-180}, 78),

    // floor - bottom
    new StripConfig("vip-lounge-strip22", new float[] {260, 69, DISTANCE_TO_FLOOR}, new float[] {0, 0, 180}, 100), 
    new StripConfig("vip-lounge-strip23", new float[] {135, 69, DISTANCE_TO_FLOOR}, new float[] {0, 0, 136}, 79),
    new StripConfig("vip-lounge-strip24", new float[] {65, 230-94, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90-180}, 80),
  };

  /**
   * Long Skinny
   *---------------------------------------------------------------------------------------------*/
  static final float long_skinny_offset_x = 0;
  static final float long_skinny_offset_y = 136;
  static final float long_skinny_offset_z = 0;
  static final float long_skinny_rotation_x = 0;
  static final float long_skinny_rotation_y = 0;
  static final float long_skinny_rotation_z = 0;

  static final StripConfig[] LONG_SKINNY_RUN_STRIP_CONFIG = {

    // horizontals - bottom 
    new StripConfig("long-skinny-strip1", new float[] {  0, 0, 0}, new float[] {0, 0, 0}, 98),
    new StripConfig("long-skinny-strip2", new float[] {132, 0, 0}, new float[] {0, 0, 0}, 121),
    new StripConfig("long-skinny-strip3", new float[] {294, 0, 0}, new float[] {0, 0, 0}, 84),
    new StripConfig("long-skinny-strip4", new float[] {407, 0, 0}, new float[] {0, 0, 0}, 79),

    // horizontals - top
    new StripConfig("long-skinny-strip5", new float[] { -1, 24, 0}, new float[] {0, 0, 0}, 129),
    new StripConfig("long-skinny-strip6", new float[] {170, 24, 0}, new float[] {0, 0, 0}, 68),
    new StripConfig("long-skinny-strip7", new float[] {260, 24, 0}, new float[] {0, 0, 0}, 144),
    new StripConfig("long-skinny-strip8", new float[] {448, 24, 0}, new float[] {0, 0, 0}, 76),

    // verticals & angles
    new StripConfig("long-skinny-strip9", new float[] {0, 1, 0}, new float[] {0, 0, 90}, 18), 

    new StripConfig("long-skinny-strip10", new float[] {124, 1, 0}, new float[] {0, 0, 33}, 32), 
    new StripConfig("long-skinny-strip11", new float[] {134, 1, 0}, new float[] {0, 0, 33}, 32), 

    new StripConfig("long-skinny-strip12", new float[] {284, -1, 0}, new float[] {0, 0, 144.5f}, 33), 
    new StripConfig("long-skinny-strip13", new float[] {293, -1.5f, 0}, new float[] {0, 0, 144}, 35), 

    new StripConfig("long-skinny-strip14", new float[] {399, 2, 0}, new float[] {0, 0, 28}, 33), 
    new StripConfig("long-skinny-strip15", new float[] {407, 2, 0}, new float[] {0, 0, 28}, 33), 

    new StripConfig("long-skinny-strip16", new float[] {505, 2, 0}, new float[] {0, 0, 28}, 32)
  };

  /**
   * Columns (and ceiling above)
   *---------------------------------------------------------------------------------------------*/

  static final float columns_offset_x = -32;
  static final float columns_offset_y = 203;
  static final float columns_offset_z = 0;
  static final float columns_rotation_x = 0;
  static final float columns_rotation_y = 0;
  static final float columns_rotation_z = 0;

  static final StripConfig[] COLUMNS_STRIP_CONFIG = {
    // ceiling
    new StripConfig("columns-strip1", new float[] {40+28, 0, 12}, new float[] {0, 0, 0}, 49),
    new StripConfig("columns-strip2", new float[] {40+28+60, 0, 12}, new float[] {0, 0, 0}, 149),
    new StripConfig("columns-strip3", new float[] {40+28+60+183, 0, 12}, new float[] {0, 0, 0}, 149),
    new StripConfig("columns-strip4", new float[] {40+28+60+183+182, 0, 12}, new float[] {0, 0, 0}, 145),

    new StripConfig("columns-strip5", new float[] {40+28, 19, 12}, new float[] {0, 0, 0}, 49),
    new StripConfig("columns-strip6", new float[] {40+28+61, 19, 12}, new float[] {0, 0, 0}, 146),
    new StripConfig("columns-strip7", new float[] {40+28+61+182, 19, 12}, new float[] {0, 0, 0}, 143),
    new StripConfig("columns-strip8", new float[] {40+28+61+182+179, 19, 12}, new float[] {0, 0, 0}, 151),

    // first square (TRIM POSITION!!!)
    new StripConfig("columns-strip9", new float[] {150, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 26), // top
    new StripConfig("columns-strip10", new float[] {150+32, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 25), // right
    new StripConfig("columns-strip11", new float[] {150+32, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -180}, 26), // bottom
    new StripConfig("columns-strip12", new float[] {150, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -270}, 26), // left

    // second square (TRIM POSITION!!!)
    new StripConfig("columns-strip13", new float[] {321, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 27), // top
    new StripConfig("columns-strip14", new float[] {321+32, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 25), // right
    new StripConfig("columns-strip15", new float[] {321+32, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -180}, 26), // bottom
    new StripConfig("columns-strip16", new float[] {321, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -270}, 26), // left

    // third square (TRIM POSITION!!!)
    new StripConfig("columns-strip17", new float[] {491, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 28), // top
    new StripConfig("columns-strip18", new float[] {491+32, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 27), // right
    new StripConfig("columns-strip19", new float[] {491+32, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -180}, 26), // bottom
    new StripConfig("columns-strip20", new float[] {491, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -270}, 26), // left

  };

  /**
   * Stairs
   *---------------------------------------------------------------------------------------------*/

  static final float stairs_offset_x = 283;
  static final float stairs_offset_y = 30;
  static final float stairs_offset_z = 40;
  static final float stairs_rotation_x = 0;
  static final float stairs_rotation_y = 0;
  static final float stairs_rotation_z = 175;

  static final StripConfig[] STAIRS_STRIP_CONFIG = {
    // PROBLEMS TO FIX!!!
    //top to bottom (just straight strips for now)
    new StripConfig("stairs-strip1", new String[] {"stairs"}, new float[] {0, 0, 0}, new float[] {0, 0, 0}, 34), // top
    new StripConfig("stairs-strip2", new String[] {"stairs"}, new float[] {-3.5f, -12, 8}, new float[] {0, 0, 0}, 46),
    new StripConfig("stairs-strip3", new String[] {"stairs"}, new float[] {-8.5f, -24, 16}, new float[] {0, 0, 0}, 51),
    new StripConfig("stairs-strip4", new String[] {"stairs"}, new float[] {-8.5f, -36, 24}, new float[] {0, 0, 0}, 51),
    new StripConfig("stairs-strip5", new String[] {"stairs"}, new float[] {-8.5f, -48, 32}, new float[] {0, 0, 0}, 51),
    new StripConfig("stairs-strip6", new String[] {"stairs"}, new float[] {-8.5f, -60, 40}, new float[] {0, 0, 0}, 51),
    new StripConfig("stairs-strip7", new String[] {"stairs"}, new float[] {-8.5f, -72, 48}, new float[] {0, 0, 0}, 51),
    new StripConfig("stairs-strip8", new String[] {"stairs"}, new float[] {-8.5f, -84, 56}, new float[] {0, 0, 0}, 51),
    new StripConfig("stairs-strip9", new String[] {"stairs"}, new float[] {-20, -96, 64}, new float[] {0, 0, 0}, 73), // bottom

  };

  /**
   * Big Long Section
   *---------------------------------------------------------------------------------------------*/

  static final float big_long_section_offset_x = 108; // TRIM THIS !!!
  static final float big_long_section_offset_y = 203+25;
  static final float big_long_section_offset_z = 0;
  static final float big_long_section_rotation_x = 0;
  static final float big_long_section_rotation_y = 0;
  static final float big_long_section_rotation_z = 0;

  static final StripConfig[] BIG_LONG_SECTION_STRIP_CONFIG = {

    // horizontal bottom
    new StripConfig("big-long-section-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 199), // contains corner pixel
    new StripConfig("big-long-section-strip2", new float[] {255, 0, 0}, new float[] {0, 0, 0}, 73),
    new StripConfig("big-long-section-strip3", new float[] {255+100, 0, 0}, new float[] {0, 0, 0}, 153),

    // horizontal top
    new StripConfig("big-long-section-strip4", new float[] {83, 54, 0}, new float[] {0, 0, 0}, 84),
    new StripConfig("big-long-section-strip5", new float[] {83+112, 54, 0}, new float[] {0, 0, 0}, 185),
    new StripConfig("big-long-section-strip6", new float[] {83+112+237, 54, 0}, new float[] {0, 0, 0}, 93),

    // angles
    new StripConfig("big-long-section-strip7", new float[] {1, 1, 0}, new float[] {0, 0, 35}, 73),

    new StripConfig("big-long-section-strip8", new float[] {250, -2, 0}, new float[] {0, 0, 138}, 70),
    new StripConfig("big-long-section-strip9", new float[] {255, -1, 0}, new float[] {0, 0, 138}, 69),

    new StripConfig("big-long-section-strip10", new float[] {112+237-2, 2, 0}, new float[] {0, 0, 34}, 72),
    new StripConfig("big-long-section-strip11", new float[] {112+237+6, 2, 0}, new float[] {0, 0, 34}, 71),
  };

  /**
   * Stage and Entrance
   *---------------------------------------------------------------------------------------------*/
  static final float stage_and_entrance_offset_x = 155;
  static final float stage_and_entrance_offset_y = 295;
  static final float stage_and_entrance_offset_z = 0;
  static final float stage_and_entrance_rotation_x = 0;
  static final float stage_and_entrance_rotation_y = 0;
  static final float stage_and_entrance_rotation_z = 0;

  static final StripConfig[] STAGE_AND_ENTRANCE_STRIP_CONFIG = {
    // strip id, {x, y, z}, {xRot, yRot, zRot}, num leds, length

    // bottom long horizontal
    new StripConfig("stage-and-entrance-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 150),
    new StripConfig("stage-and-entrance-strip2", new float[] {188, 0, 0}, new float[] {0, 0, 0}, 150),
    new StripConfig("stage-and-entrance-strip3", new float[] {376, 0, 0}, new float[] {0, 0, 0}, 60),

    // bathrooms
    new StripConfig("stage-and-entrance-strip4", new float[] {451, 0, 0}, new float[] {0, 0, 90}, 72),
    new StripConfig("stage-and-entrance-strip5", new float[] {451, 90, 0}, new float[] {0, 0, 180}, 29),

    // stage (angle, then stage)
    new StripConfig("stage-and-entrance-strip6", new float[] {1, 1, 0}, new float[] {0, 0, 33}, 101),
    new StripConfig("stage-and-entrance-strip7", new float[] {109, 70, 0}, new float[] {0, 0, 0}, 158), // NEEDS TO BE AN ANGLE!! UGH

    // floor
    new StripConfig("stage-and-entrance-strip8", new float[] {325, 65, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 26),
    new StripConfig("stage-and-entrance-strip9", new float[] {325, 65-32, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 75),
    new StripConfig("stage-and-entrance-strip10", new float[] {325+94, 65-32, DISTANCE_TO_FLOOR}, new float[] {0, 0, 90}, 49),

    // back hall
    new StripConfig("back-hall-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 170),
    // new StripConfig("back-hall-strip2", new float[] {188, 0, 0}, new float[] {0, 0, 0}, 150),
    // new StripConfig("back-hall-strip3", new float[] {376, 0, 0}, new float[] {0, 0, 0}, 150),

  };


  /**
   * VJ Booth
   *---------------------------------------------------------------------------------------------*/
  static final float vj_booth_offset_x = 306; // (PLACE VJ BOOTH!!!)
  static final float vj_booth_offset_y = 56; // (PLACE VJ BOOTH!!!)
  static final float vj_booth_offset_z = 0;
  static final float vj_booth_rotation_x = 0;
  static final float vj_booth_rotation_y = 0;
  static final float vj_booth_rotation_z = 0;

  static final StripConfig[] VJ_BOOTH_STRIP_CONFIG = {
   // strip id, {x, y, z}, {xRot, yRot, zRot}, num leds, length

   new StripConfig("vj_booth-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 35),
   new StripConfig("vj_booth-strip2", new float[] {43, 0, 0}, new float[] {0, 0, -90}, 33), // vertical
   new StripConfig("vj_booth-strip3", new float[] {43, -39, 0}, new float[] {0, 0, 0}, 47),

   new StripConfig("vj_booth-strip4", new float[] {0, -1, 0}, new float[] {0, 0, 90}, 8), // tiny part
   new StripConfig("vj_booth-strip5", new float[] {0, 8, 0}, new float[] {0, 0, 0}, 81),
   new StripConfig("vj_booth-strip6", new float[] {100, 8, 0}, new float[] {0, 0, -90}, 39),

   // horizontals - bottom
   new StripConfig("vj_booth-strip7", new float[] {0, 14, 0}, new float[] {0, 0, 0}, 82), // bottom

   // horizontals - top
   new StripConfig("vj_booth-strip8", new float[] {3, 14+60, 0}, new float[] {0, 0, 0}, 25), // top
   new StripConfig("vj_booth-strip9", new float[] {32+7+3, 14+60, 0}, new float[] {0, 0, 0}, 103), // top 2

   // verticals
   new StripConfig("vj_booth-strip10", new float[] {0, 14, 0}, new float[] {0, 0, 90}, 49),

   // angles on the right
   new StripConfig("vj_booth-strip11", new float[] {100, 14, 0}, new float[] {0, 0, 67}, 26),
   new StripConfig("vj_booth-strip12", new float[] {110+5, 47, 0}, new float[] {0, 0, 25}, 52),

   // contour angles
   new StripConfig("vj_booth-strip13", new float[] {31+3, 14+60, 0}, new float[] {0, 0, -20}, 72),
   new StripConfig("vj_booth-strip14", new float[] {31+9+3, 14+60-1, 0}, new float[] {0, 0, -20}, 64),

   // over the stairs
   new StripConfig("vj_booth-strip15", new float[] {-52.5f, 14+60, 0}, new float[] {0, 0, 0}, 44), // top

   // bottom of the bar
   new StripConfig("vj_booth-strip16", new float[] {250, 150, DISTANCE_TO_FLOOR}, new float[] {0, 0, -122}, 75), // left
   new StripConfig("vj_booth-strip17", new float[] {250-50, 150-78, DISTANCE_TO_FLOOR}, new float[] {0, 0, -122}, 75),
   new StripConfig("vj_booth-strip18", new float[] {250-100, 150-156, DISTANCE_TO_FLOOR}, new float[] {0, 0, -122}, 75), // right

  };

  /**
   * Bar
   *---------------------------------------------------------------------------------------------*/
  static final float bar_offset_x = 510;
  static final float bar_offset_y = 0;
  static final float bar_offset_z = 0;
  static final float bar_rotation_x = 90;
  static final float bar_rotation_y = 57;
  static final float bar_rotation_z = 0;
  static final float BAR_SHELF_WIDTH = 22;

  static final StripConfig[] BAR_STRIP_CONFIG = {
   // strip id, {x, y, z}, {xRot, yRot, zRot}, num leds, length

    new StripConfig("bar-strip1", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*0, 0, 0}, new float[] {0, 0, 90}, 49),
    new StripConfig("bar-strip2", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*1, 0, 0}, new float[] {0, 0, 90}, 49),

    new StripConfig("bar-strip3", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*1+3, 0, 0}, new float[] {0, 0, 90}, 49),
    new StripConfig("bar-strip4", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*2+3, 0, 0}, new float[] {0, 0, 90}, 49),

    new StripConfig("bar-strip5", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*2+6, 0, 0}, new float[] {0, 0, 90}, 49),
    new StripConfig("bar-strip6", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*3+6, 0, 0}, new float[] {0, 0, 90}, 49),

    new StripConfig("bar-strip7", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*3+9, 0, 0}, new float[] {0, 0, 90}, 49),
    new StripConfig("bar-strip8", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*4+9, 0, 0}, new float[] {0, 0, 90}, 49),

    new StripConfig("bar-strip9", new String[] {"bar"}, new float[]  {BAR_SHELF_WIDTH*5+12, 0, 0}, new float[] {0, 0, 90}, 49),
    new StripConfig("bar-strip10", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*6+12, 0, 0}, new float[] {0, 0, 90}, 49),

    new StripConfig("bar-strip11", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*6+15, 0, 0}, new float[] {0, 0, 90}, 49),
    new StripConfig("bar-strip12", new String[] {"bar"}, new float[] {BAR_SHELF_WIDTH*7+15, 0, 0}, new float[] {0, 0, 90}, 49),
  };
  
  public GospelModel() {
    super("gospel", new Fixture());
    Fixture fixture = (Fixture) this.fixtures.get(0);
    for (Strip strip : fixture.strips) {
      this.strips.add(strip);
      this.stripTable.put(strip.modelId, strip);
      }
  }
  
  public static class Fixture extends LXAbstractFixture {

    public final ArrayList<Strip> strips = new ArrayList<Strip>();

    Fixture() {

      // Any global transforms
      LXTransform globalTransform = new LXTransform();
      globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
      globalTransform.rotateY(globalRotationY * PI / 180.);
      globalTransform.rotateX(globalRotationX * PI / 180.);
      globalTransform.rotateZ(globalRotationZ * PI / 180.);

      /* Strips ----------------------------------------------------------*/
      // Vip Lounge
      globalTransform.push();
      globalTransform.translate(vip_lounge_offset_x, vip_lounge_offset_y, vip_lounge_offset_z);
      globalTransform.rotateX(vip_lounge_rotation_x * PI / 180.);
      globalTransform.rotateY(vip_lounge_rotation_y * PI / 180.);
      globalTransform.rotateZ(vip_lounge_rotation_z * PI / 180.);

      for (StripConfig stripConfig : VIP_LOUGNE_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        strips.add(new Strip(stripConfig.id, metrics, globalTransform));

        globalTransform.pop();
      }
      globalTransform.pop();
      /*-----------------------------------------------------------------*/

      // Long Skinny
      globalTransform.push();
      globalTransform.translate(long_skinny_offset_x, long_skinny_offset_y, long_skinny_offset_z);
      globalTransform.rotateX(long_skinny_rotation_x *  PI / 180.);
      globalTransform.rotateY(long_skinny_rotation_y *  PI / 180.);
      globalTransform.rotateZ(long_skinny_rotation_z *  PI / 180.);

      for (StripConfig stripConfig : LONG_SKINNY_RUN_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        strips.add(new Strip(stripConfig.id, metrics, globalTransform));

        globalTransform.pop();
      }
      globalTransform.pop();
      /*-----------------------------------------------------------------*/

      // Columns (and ceiling above)
      globalTransform.push();
      globalTransform.translate(columns_offset_x, columns_offset_y, columns_offset_z);
      globalTransform.rotateX(columns_rotation_x *  PI / 180.);
      globalTransform.rotateY(columns_rotation_y *  PI / 180.);
      globalTransform.rotateZ(columns_rotation_z *  PI / 180.);

      for (StripConfig stripConfig : COLUMNS_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        strips.add(new Strip(stripConfig.id, metrics, globalTransform));

        globalTransform.pop();
      }
      globalTransform.pop();
      /*-----------------------------------------------------------------*/

      // Stairs
      List<Strip> stairsStrips = new ArrayList<Strip>();

      globalTransform.push();
      globalTransform.translate(stairs_offset_x, stairs_offset_y, stairs_offset_z);
      globalTransform.rotateX(stairs_rotation_x *  PI / 180.);
      globalTransform.rotateY(stairs_rotation_y *  PI / 180.);
      globalTransform.rotateZ(stairs_rotation_z *  PI / 180.);

      for (StripConfig stripConfig : STAIRS_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        Strip strip = new Strip(stripConfig.id, metrics, globalTransform);
        strips.add(strip);
        stairsStrips.add(strip);

        globalTransform.pop();
      }

      StairsModel stairs = new StairsModel(stairsStrips);
      globalTransform.pop();
      /*-----------------------------------------------------------------*/

      // Big Long Section
      globalTransform.push();
      globalTransform.translate(big_long_section_offset_x, big_long_section_offset_y, big_long_section_offset_z);
      globalTransform.rotateX(big_long_section_rotation_x *  PI / 180.);
      globalTransform.rotateY(big_long_section_rotation_y *  PI / 180.);
      globalTransform.rotateZ(big_long_section_rotation_z *  PI / 180.);

      for (StripConfig stripConfig : BIG_LONG_SECTION_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        strips.add(new Strip(stripConfig.id, metrics, globalTransform));

        globalTransform.pop();
      }
      globalTransform.pop();
      /*-----------------------------------------------------------------*/

      // Stage and Entrance
      globalTransform.push();
      globalTransform.translate(stage_and_entrance_offset_x, stage_and_entrance_offset_y, stage_and_entrance_offset_z);
      globalTransform.rotateX(stage_and_entrance_rotation_x *  PI / 180.);
      globalTransform.rotateY(stage_and_entrance_rotation_y *  PI / 180.);
      globalTransform.rotateZ(stage_and_entrance_rotation_z *  PI / 180.);

      for (StripConfig stripConfig : STAGE_AND_ENTRANCE_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        strips.add(new Strip(stripConfig.id, metrics, globalTransform));

        globalTransform.pop();
      }
      globalTransform.pop();
      /*-----------------------------------------------------------------*/


      // VJ Booth
      globalTransform.push();
      globalTransform.translate(vj_booth_offset_x, vj_booth_offset_y, vj_booth_offset_z);
      globalTransform.rotateX(vj_booth_rotation_x *  PI / 180.);
      globalTransform.rotateY(vj_booth_rotation_y *  PI / 180.);
      globalTransform.rotateZ(vj_booth_rotation_z *  PI / 180.);

      for (StripConfig stripConfig : VJ_BOOTH_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        strips.add(new Strip(stripConfig.id, metrics, globalTransform));

        globalTransform.pop();
      }
      globalTransform.pop();
      /*-----------------------------------------------------------------*/

      // bar
      List<Strip> barStrips = new ArrayList<Strip>();

      globalTransform.push();
      globalTransform.translate(bar_offset_x, bar_offset_y, bar_offset_z);
      globalTransform.rotateX(bar_rotation_x *  PI / 180.);
      globalTransform.rotateY(bar_rotation_y *  PI / 180.);
      globalTransform.rotateZ(bar_rotation_z *  PI / 180.);

      for (StripConfig stripConfig : BAR_STRIP_CONFIG) {
        Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

        globalTransform.push();
        globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
        globalTransform.rotateY(stripConfig.xRot * PI / 180.);
        globalTransform.rotateX(stripConfig.yRot * PI / 180.);
        globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

        Strip strip = new Strip(stripConfig.id, metrics, globalTransform);
        strips.add(strip);
        stairsStrips.add(strip);

        globalTransform.pop();
      }

      BarModel bar = new BarModel(stairsStrips);
      globalTransform.pop();

      for (Strip strip : strips) {
        for (LXPoint point : strip.points) {
          this.points.add(point);
        }
      }
    }
  }

  public LXPoint[] splicePoints(String stripId, int startIndex, int numPoints) {
    LXPoint[] points = getStripById(stripId).points;

    if (startIndex < 0 || startIndex >= points.length || startIndex + numPoints - 1 > points.length) {
      throw new RuntimeException("OutputGroup.addPoints() out of bounds. (Strip Id: " + stripId + ", startIndex: " + startIndex + ", numPointsToSplice: " + numPoints + ", numPointsOnStrip: " + points.length + ")");
    }

    return Arrays.copyOfRange(points, startIndex, startIndex + numPoints-1);
  }
}
