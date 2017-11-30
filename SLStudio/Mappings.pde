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

static final float globalOffsetX = 0;
static final float globalOffsetY = 0;
static final float globalOffsetZ = 0;

static final float globalRotationX = 0;
static final float globalRotationY = 0;
static final float globalRotationZ = 0;

static final float objOffsetX = 0;
static final float objOffsetY = 0;
static final float objOffsetZ = 0;

static final float objRotationX = 0;
static final float objRotationY = 0;
static final float objRotationZ = 0;

static final float INCHES_PER_METER = 39.3701;

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
    }  catch (JsonSyntaxException e) {
      e.printStackTrace();
    }
  }

  // Any global transforms
  LXTransform transform = new LXTransform();
  transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
  transform.rotateX(globalRotationX * PI / 180.);
  transform.rotateY(globalRotationY * PI / 180.);
  transform.rotateZ(globalRotationZ * PI / 180.);

  /* Suns ------------------------------------------------------------*/
  List<Sun> suns = new ArrayList<Sun>();

  suns.add(new Sun("sun1", Sun.Type.ONE_THIRD, new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform,
    new int[][] { // completed
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 155, 157, 155, 157, 157, 159,  // 51 - 60
        158, 159, 159, 159, 161, 161, 161, 161, 161        // 61 - 69
      },
    }
  ));

  // suns.add(new Sun("sun2", Sun.Type.ONE_THIRD, new float[] {1300, -36, -125}, new float[] {0, 0, 0}, transform));

  suns.add(new Sun("sun3", Sun.Type.ONE_THIRD, new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform,
    new int[][] { // completed
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 155, 157, 155, 157, 157, 159,  // 51 - 60
        158, 159, 159, 159, 161, 161, 161, 161, 161        // 61 - 69
      },
    }
  ));

  suns.add(new Sun("sun4", Sun.Type.ONE_HALF, new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform,
    new int[][] { // completed
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 155, 157, 155, 157, 157, 159,  // 51 - 60
        158, 159, 159, 159, 161, 161, 161, 161, 161        // 61 - 69
      },
    }
  ));

  suns.add(new Sun("sun5", Sun.Type.ONE_HALF, new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform, 
    new int[][] { // completed
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 152, 153, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  72,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 150, 151, 154, 153,  // 41 - 50
        154, 155, 155, 155, 157, 157, 155, 159, 157, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      }
    }
  ));

  suns.add(new Sun("sun6", Sun.Type.TWO_THIRDS, new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform, 
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 138, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 153,  // 41 - 50
        155, 158, 155, 155, 157, 157, 155, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
        149, 151, 151, 153, 153, 153, 155, 155, 155, 157,  //  1 - 10 
        157, 157, 157, 159, 159, 159, 159, 159, 159, 161,  // 11 - 20
        161, 161                                           // 21 - 22
      },
      { // Bottom - Back
        149, 151, 151, 153, 154, 153, 154, 155, 155, 157,  //  1 - 10
        157, 155, 157, 159, 159, 159, 159, 159, 161, 161,  // 11 - 20 
        161, 161                                           // 21 - 22
      },
    }
  ));
  
  // suns.add(new Sun("sun7", Sun.Type.TWO_THIRDS, new float[] {1420, 30, 155}, new float[] {0, 0, 0}, transform));

  suns.add(new Sun("sun8", Sun.Type.TWO_THIRDS, new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform, 
    new int[][] {
      { // Top - Front
          9,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 115, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 135, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 153, 153,  // 41 - 50
        153, 155, 155, 155, 157, 157, 157, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Top - Back
          0,  25,  35,  43,  49,  55,  59,  65,  69,  73,  //  1 - 10
         77,  81,  85,  89,  91,  95,  97, 101, 103, 107,  // 11 - 20
        109, 111, 113, 114, 119, 121, 123, 125, 127, 129,  // 21 - 30
        129, 131, 133, 135, 137, 137, 139, 141, 141, 143,  // 31 - 40
        145, 145, 147, 147, 149, 149, 151, 151, 154, 154,  // 41 - 50
        154, 158, 155, 155, 157, 157, 155, 157, 159, 159,  // 51 - 60
        159, 159, 159, 161, 161, 161, 161, 161, 161        // 61 - 69
      },
      { // Bottom - Front
        149, 151, 151, 153, 153, 153, 155, 155, 155, 157,  //  1 - 10 
        157, 157, 157, 159, 159, 159, 159, 159, 161, 161,  // 11 - 20
        161, 161                                           // 21 - 22
      },
      { // Bottom - Back
        149, 151, 151, 153, 154, 154, 154, 155, 155, 157,  //  1 - 10
        157, 157, 157, 159, 159, 159, 159, 159, 161, 161,  // 11 - 20 
        161, 161                                           // 21 - 22
      },
    }
  ));

  // suns.add(new Sun("sun9", Sun.Type.TWO_THIRDS, new float[] {2000, 30, 120}, new float[] {0, 0, 0}, transform));
  // suns.add(new Sun("sun10", Sun.Type.FULL, new float[] {1650, 160, 0}, new float[] {0, 0, 0}, transform));
  // suns.add(new Sun("sun11", Sun.Type.FULL, new float[] {400, 120, 60}, new float[] {0, 0, 0}, transform));

  /* Obj Importer ----------------------------------------------------*/
  List<LXModel> objModels = new ObjImporter("data", transform).getModels();

  return new SLModel(suns);
}

public SLModel getModel() {
  return buildModel();
}