import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.function.IntFunction;

final static float INCHES = 1;
final static float FEET = 12*INCHES;
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
 * Contains the model definitions for the cube structures.
 */



/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */

public static class SLModel extends LXModel {
  public final List<Tower> towers;
  public final List<Cube> cubes;
  public final List<Face> faces;
  public final List<Strip> strips;
  public final Map<String, Cube> cubeTable;
  private final Cube[] _cubes;

  public final List<LXModel> objModels;

  public final List<Panel> panels;
  public final Map<String, Panel> panelTable;

  public final List<HalfHeart> hearts;
  public final Map<String, HalfHeart> heartTable;

  public SLModel(List<LXModel> objModels, List<Tower> towers, Cube[] cubeArr, List<Strip> strips, List<HalfHeart> hearts, List<Panel> panels) {
    super(new Fixture(objModels, cubeArr, strips, panels));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    _cubes = cubeArr;

    // Make unmodifiable accessors to the model data
    List<Tower> towerList = new ArrayList<Tower>();
    List<Cube> cubeList = new ArrayList<Cube>();
    List<Face> faceList = new ArrayList<Face>();
    List<Strip> stripList = new ArrayList<Strip>();
    Map<String, Cube> _cubeTable = new HashMap<String, Cube>();
    Map<String, Panel> _panelTable = new HashMap<String, Panel>();
    Map<String, HalfHeart> _heartTable = new HashMap<String, HalfHeart>();
    
    for (Tower tower : towers) {
      towerList.add(tower);
      for (Cube cube : tower.cubes) {
        if (cube != null) {
          _cubeTable.put(cube.id, cube);
          cubeList.add(cube);
          for (Face face : cube.faces) {
            faceList.add(face);
            for (Strip strip : face.strips) {
              stripList.add(strip);
            }
          }
        }
      }
    }

    for (Strip strip : strips) {
      stripList.add(strip);
    }

    for (Panel panel : panels) {
      _panelTable.put(panel.id, panel);
    }
    for (HalfHeart heart : hearts) {
      _heartTable.put(heart.id, heart);
    }

    this.towers    = Collections.unmodifiableList(towerList);
    this.cubes     = Collections.unmodifiableList(cubeList);
    this.faces     = Collections.unmodifiableList(faceList);
    this.strips    = Collections.unmodifiableList(stripList);
    this.cubeTable = Collections.unmodifiableMap (_cubeTable);
    this.objModels = objModels;

    this.panels = panels;
    this.panelTable = Collections.unmodifiableMap (_panelTable);


    this.hearts = hearts;
    this.heartTable = Collections.unmodifiableMap (_heartTable);
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(List<LXModel> objModels, Cube[] cubeArr, List<Strip> strips, List<Panel> panels) {
      println("Number of obj models: " + objModels.size());
      for (LXModel model : objModels) {
        for (LXPoint point : model.points) {
          this.points.add(point);
        }
      }
      for (Cube cube : cubeArr) { 
        if (cube != null) { 
          for (LXPoint point : cube.points) { 
            this.points.add(point); 
          } 
        } 
      } 
      for (Strip strip : strips) {
        for (LXPoint point : strip.points) {
          this.points.add(point);
        }
      }
      for (Panel panel : panels) {
        for (LXPoint point : panel.points) {
          this.points.add(point);
        }
      }
    }
  }

  /**
   * TODO(mcslee): figure out better solution
   * 
   * @param index
   * @return
   */
  public Cube getCubeByRawIndex(int index) {
    return _cubes[index];
  }
  
  public Cube getCubeById(String id) {
    return this.cubeTable.get(id);
  }

  public Panel getPanelById(String id) {
    return this.panelTable.get(id);
  }

  public HalfHeart getHeartById(String id) {
    return this.heartTable.get(id);
  }
}

/**
 * Model of a set of cubes stacked in a tower
 */
public static class Tower extends LXModel {
  
  /**
   * Tower id
   */
  public final String id;
  
  /**
   * Immutable list of cubes
   */
  public final List<Cube> cubes;

  /**
   * Immutable list of faces
   */
  public final List<Face> faces;

  /**
     * Immutable list of strips
     */
  public final List<Strip> strips;

  /**
   * Constructs a tower model from these cubes
   * 
   * @param cubes Array of cubes
   */
  public Tower(String id, List<Cube> cubes) {
    super(cubes.toArray(new Cube[] {}));
    this.id   = id;

    List<Cube>  cubeList  = new ArrayList<Cube>();
    List<Face>  faceList  = new ArrayList<Face>();
    List<Strip> stripList = new ArrayList<Strip>();

    for (Cube cube : cubes) {
      cubeList.add(cube);
      for (Face face : cube.faces) {
        faceList.add(face);
        for (Strip strip : face.strips) {
          stripList.add(strip);
        }
      }
    }
    this.cubes = Collections.unmodifiableList(cubeList);
    this.faces = Collections.unmodifiableList(faceList);
    this.strips = Collections.unmodifiableList(stripList);
  }
}

public static class HalfHeart extends LXModel {

  private static final float PANEL_PADDING = 12;

  public final String id;
  public final List<Panel> panels;
  public final Type type;

  private enum Type {
    RIGHT, LEFT
  }

  public HalfHeart(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
    super(new Fixture(type, coordinates, rotations, transform));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    this.id = id;
    this.type = type;
    this.panels = Collections.unmodifiableList(fixture.panels);
  }

  public Panel getPanel(String id) {
    for (Panel panel : panels) {
      if (panel.id.equals(id)) {
        return panel;
      }
    }
    return null;
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Panel> panels = new ArrayList<Panel>();

    private Fixture(Type type, float[] coordinates, float[] rotations, LXTransform transform) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);

      if (type == Type.RIGHT) {
        transform.rotateZ((rotations[2]+45) * PI / 180);
        // row 1
        this.panels.add(new Panel("1",  Panel.Type.F, new float[] {PANEL_PADDING*0+10, PANEL_PADDING*0,    0}, new float[] {0, 0,  90}, transform));
        this.panels.add(new Panel("2",  Panel.Type.A, new float[] {PANEL_PADDING*1,    PANEL_PADDING*0+10,    0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("3",  Panel.Type.A, new float[] {PANEL_PADDING*2,    PANEL_PADDING*0+10,    0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("4",  Panel.Type.A, new float[] {PANEL_PADDING*3,    PANEL_PADDING*0+10,    0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("5",  Panel.Type.A, new float[] {PANEL_PADDING*4,    PANEL_PADDING*0+10,    0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("6",  Panel.Type.A, new float[] {PANEL_PADDING*5,    PANEL_PADDING*0+10,    0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("7",  Panel.Type.A, new float[] {PANEL_PADDING*6,    PANEL_PADDING*0+10,    0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("8",  Panel.Type.A, new float[] {PANEL_PADDING*7,    PANEL_PADDING*0+10,    0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("9",  Panel.Type.B, new float[] {PANEL_PADDING*8+10, PANEL_PADDING*0+10, 0}, new float[] {0, 0, 180}, transform));
        this.panels.add(new Panel("10", Panel.Type.E, new float[] {PANEL_PADDING*9+8,  PANEL_PADDING*0+4,  0}, new float[] {0, 0,  90}, transform));

        // row 2
        this.panels.add(new Panel("11", Panel.Type.F, new float[] {22+PANEL_PADDING*0, PANEL_PADDING*1, 0}, new float[] {0, 0, 90}, transform));
        this.panels.add(new Panel("12", Panel.Type.A, new float[] {12+PANEL_PADDING*1, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("13", Panel.Type.A, new float[] {12+PANEL_PADDING*2, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("14", Panel.Type.A, new float[] {12+PANEL_PADDING*3, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("15", Panel.Type.A, new float[] {12+PANEL_PADDING*4, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("16", Panel.Type.A, new float[] {12+PANEL_PADDING*5, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("17", Panel.Type.A, new float[] {12+PANEL_PADDING*6, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("18", Panel.Type.A, new float[] {12+PANEL_PADDING*7, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("19", Panel.Type.A, new float[] {12+PANEL_PADDING*8, PANEL_PADDING*1+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("20", Panel.Type.D, new float[] {12+PANEL_PADDING*9+6, PANEL_PADDING*1+10, 0}, new float[] {0, 0,180}, transform));

        // row 3
        this.panels.add(new Panel("21", Panel.Type.F, new float[] {34+PANEL_PADDING*0, PANEL_PADDING*2, 0}, new float[] {0, 0, 90}, transform));
        this.panels.add(new Panel("22", Panel.Type.A, new float[] {24+PANEL_PADDING*1, PANEL_PADDING*2+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("23", Panel.Type.A, new float[] {24+PANEL_PADDING*2, PANEL_PADDING*2+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("24", Panel.Type.A, new float[] {24+PANEL_PADDING*3, PANEL_PADDING*2+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("25", Panel.Type.A, new float[] {24+PANEL_PADDING*4, PANEL_PADDING*2+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("26", Panel.Type.A, new float[] {24+PANEL_PADDING*5, PANEL_PADDING*2+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("27", Panel.Type.A, new float[] {24+PANEL_PADDING*6, PANEL_PADDING*2+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("28", Panel.Type.A, new float[] {24+PANEL_PADDING*7, PANEL_PADDING*2+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("29", Panel.Type.C, new float[] {24+PANEL_PADDING*8, PANEL_PADDING*2+10, 0}, new float[] {0, 0,-90}, transform));

        // row 4
        this.panels.add(new Panel("30", Panel.Type.F, new float[] {46+PANEL_PADDING*0, PANEL_PADDING*3, 0}, new float[] {0, 0, 90}, transform));
        this.panels.add(new Panel("31", Panel.Type.A, new float[] {36+PANEL_PADDING*1, PANEL_PADDING*3+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("32", Panel.Type.A, new float[] {36+PANEL_PADDING*2, PANEL_PADDING*3+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("33", Panel.Type.A, new float[] {36+PANEL_PADDING*3, PANEL_PADDING*3+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("34", Panel.Type.A, new float[] {36+PANEL_PADDING*4, PANEL_PADDING*3+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("35", Panel.Type.A, new float[] {36+PANEL_PADDING*5, PANEL_PADDING*3+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("36", Panel.Type.A, new float[] {36+PANEL_PADDING*6, PANEL_PADDING*3+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("37", Panel.Type.A, new float[] {36+PANEL_PADDING*7, PANEL_PADDING*3+10, 0}, new float[] {0, 0, -90}, transform));

        // row 5
        this.panels.add(new Panel("38", Panel.Type.F, new float[] {58+PANEL_PADDING*0, PANEL_PADDING*4, 0}, new float[] {0, 0, 90}, transform));
        this.panels.add(new Panel("39", Panel.Type.A, new float[] {48+PANEL_PADDING*1, PANEL_PADDING*4+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("40", Panel.Type.A, new float[] {48+PANEL_PADDING*2, PANEL_PADDING*4+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("41", Panel.Type.A, new float[] {48+PANEL_PADDING*3, PANEL_PADDING*4+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("42", Panel.Type.A, new float[] {48+PANEL_PADDING*4, PANEL_PADDING*4+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("43", Panel.Type.A, new float[] {48+PANEL_PADDING*5, PANEL_PADDING*4+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("44", Panel.Type.B, new float[] {48+PANEL_PADDING*6, PANEL_PADDING*4+10, 0}, new float[] {0, 0,-90}, transform));

        // row 6
        this.panels.add(new Panel("45", Panel.Type.F, new float[] {70+PANEL_PADDING*0, PANEL_PADDING*5, 0}, new float[] {0, 0, 90}, transform));
        this.panels.add(new Panel("46", Panel.Type.A, new float[] {60+PANEL_PADDING*1, PANEL_PADDING*5+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("47", Panel.Type.A, new float[] {60+PANEL_PADDING*2, PANEL_PADDING*5+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("48", Panel.Type.A, new float[] {60+PANEL_PADDING*3, PANEL_PADDING*5+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("49", Panel.Type.A, new float[] {60+PANEL_PADDING*4, PANEL_PADDING*5+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("50", Panel.Type.E, new float[] {60+PANEL_PADDING*5+6, PANEL_PADDING*5+8, 0}, new float[] {0, 0,180}, transform));

        // row 7
        this.panels.add(new Panel("51", Panel.Type.F, new float[] {82+PANEL_PADDING*0, PANEL_PADDING*6, 0}, new float[] {0, 0, 90}, transform));
        this.panels.add(new Panel("52", Panel.Type.A, new float[] {72+PANEL_PADDING*1, PANEL_PADDING*6+10, 0}, new float[] {0, 0, -90}, transform));
        this.panels.add(new Panel("53", Panel.Type.C, new float[] {72+PANEL_PADDING*2, PANEL_PADDING*6, 0}, new float[] {0, 0, 0}, transform));
        this.panels.add(new Panel("54", Panel.Type.D, new float[] {72+PANEL_PADDING*3, PANEL_PADDING*6+6, 0}, new float[] {0, 0, -90}, transform));
      }

      if (type == Type.LEFT) {
        // STILL CHANGES TO MAKE
        transform.rotateY(PI);
        transform.rotateZ((rotations[2]+45) * PI / 180);
        // row 1
        this.panels.add(new Panel("1",  Panel.Type.F, new float[] {PANEL_PADDING*0+10, PANEL_PADDING*0,    0}, new float[] {180+25, 0,  180}, transform));
        this.panels.add(new Panel("2",  Panel.Type.A, new float[] {PANEL_PADDING*1+10,    PANEL_PADDING*0,    0}, new float[] {180+25, 0, -180}, transform));
        this.panels.add(new Panel("3",  Panel.Type.A, new float[] {PANEL_PADDING*2+10,    PANEL_PADDING*0,    0}, new float[] {180+25, 0, -180}, transform));
        this.panels.add(new Panel("4",  Panel.Type.A, new float[] {PANEL_PADDING*3+10,    PANEL_PADDING*0,    0}, new float[] {180+25, 0, -180}, transform));
        this.panels.add(new Panel("5",  Panel.Type.A, new float[] {PANEL_PADDING*4+10,    PANEL_PADDING*0,    0}, new float[] {180+25, 0, -180}, transform));
        this.panels.add(new Panel("6",  Panel.Type.A, new float[] {PANEL_PADDING*5+10,    PANEL_PADDING*0,    0}, new float[] {180+25, 0, -180}, transform));
        this.panels.add(new Panel("7",  Panel.Type.A, new float[] {PANEL_PADDING*6+10,    PANEL_PADDING*0,    0}, new float[] {180+25, 0, -180}, transform));
        this.panels.add(new Panel("8",  Panel.Type.A, new float[] {PANEL_PADDING*7+10,    PANEL_PADDING*0,    0}, new float[] {180+25, 0, -180}, transform));
        this.panels.add(new Panel("9",  Panel.Type.C, new float[] {PANEL_PADDING*8, PANEL_PADDING*0+11-2, 4.5}, new float[] {180+25, -7, 0}, transform));
        this.panels.add(new Panel("10", Panel.Type.E, new float[] {PANEL_PADDING*9+8-0.5,  PANEL_PADDING*0+4+0.5,  -2}, new float[] {25, 16,  88}, transform));

        // // row 2
        this.panels.add(new Panel("11", Panel.Type.F, new float[] {22+PANEL_PADDING*0, PANEL_PADDING*1-1, 5}, new float[] {180+16.7, 0, 180}, transform));
        this.panels.add(new Panel("12", Panel.Type.A, new float[] {12+PANEL_PADDING*1+10, PANEL_PADDING*1-1, 5}, new float[] {180+16.7, 0, -180}, transform));
        this.panels.add(new Panel("13", Panel.Type.A, new float[] {12+PANEL_PADDING*2+10, PANEL_PADDING*1-1, 5}, new float[] {180+16.7, 0, -180}, transform));
        this.panels.add(new Panel("14", Panel.Type.A, new float[] {12+PANEL_PADDING*3+10, PANEL_PADDING*1-1, 5}, new float[] {180+16.7, 0, -180}, transform));
        this.panels.add(new Panel("15", Panel.Type.A, new float[] {12+PANEL_PADDING*4+10, PANEL_PADDING*1-1, 5}, new float[] {180+16.7, 0, -180}, transform));
        this.panels.add(new Panel("16", Panel.Type.A, new float[] {12+PANEL_PADDING*5+10, PANEL_PADDING*1-1, 5}, new float[] {180+16.7, 0, -180}, transform));
        this.panels.add(new Panel("17", Panel.Type.A, new float[] {12+PANEL_PADDING*6+10, PANEL_PADDING*1-1, 5}, new float[] {180+16.7, 0, -180}, transform));
        this.panels.add(new Panel("18", Panel.Type.A, new float[] {12+PANEL_PADDING*7+10, PANEL_PADDING*1-1, 4}, new float[] {180+16.7, -7, -180}, transform));
        this.panels.add(new Panel("19", Panel.Type.A, new float[] {12+PANEL_PADDING*8+10, PANEL_PADDING*1-0.5, 1}, new float[] {180+16.7, -16, -180}, transform));
        this.panels.add(new Panel("20", Panel.Type.D, new float[] {12+PANEL_PADDING*9+5, PANEL_PADDING*1+10, 0}, new float[] {16.7, 30,180}, transform));

        // // row 3
        this.panels.add(new Panel("21", Panel.Type.F, new float[] {34+PANEL_PADDING*0, PANEL_PADDING*2-1, 8.5}, new float[] {180+8.4, 0, 180}, transform));
        this.panels.add(new Panel("22", Panel.Type.A, new float[] {24+PANEL_PADDING*1+10, PANEL_PADDING*2-1, 8.5}, new float[] {180+8.4, 0, -180}, transform));
        this.panels.add(new Panel("23", Panel.Type.A, new float[] {24+PANEL_PADDING*2+10, PANEL_PADDING*2-1, 8.5}, new float[] {180+8.4, 0, -180}, transform));
        this.panels.add(new Panel("24", Panel.Type.A, new float[] {24+PANEL_PADDING*3+10, PANEL_PADDING*2-1, 8.5}, new float[] {180+8.4, 0, -180}, transform));
        this.panels.add(new Panel("25", Panel.Type.A, new float[] {24+PANEL_PADDING*4+10, PANEL_PADDING*2-1, 8.5}, new float[] {180+8.4, 0, -180}, transform));
        this.panels.add(new Panel("26", Panel.Type.A, new float[] {24+PANEL_PADDING*5+10, PANEL_PADDING*2-1, 8.5}, new float[] {180+8.4, 0, -180}, transform));
        this.panels.add(new Panel("27", Panel.Type.A, new float[] {24+PANEL_PADDING*6+10, PANEL_PADDING*2-1, 7.5}, new float[] {180+8.4, -7, -180}, transform));
        this.panels.add(new Panel("28", Panel.Type.A, new float[] {24+PANEL_PADDING*7+10, PANEL_PADDING*2-1, 4.5}, new float[] {180+8.4, -16, -180}, transform));
        this.panels.add(new Panel("29", Panel.Type.B, new float[] {24+PANEL_PADDING*8+0.5, PANEL_PADDING*2-1, 3.5}, new float[] {8, 180+30,90}, transform));

        // // row 4
        this.panels.add(new Panel("30", Panel.Type.F, new float[] {46+PANEL_PADDING*0, PANEL_PADDING*3-1, 10}, new float[] {180, 0, 180}, transform));
        this.panels.add(new Panel("31", Panel.Type.A, new float[] {36+PANEL_PADDING*1+10, PANEL_PADDING*3-1, 10}, new float[] {180, 0, -180}, transform));
        this.panels.add(new Panel("32", Panel.Type.A, new float[] {36+PANEL_PADDING*2+10, PANEL_PADDING*3-1, 10}, new float[] {180, 0, -180}, transform));
        this.panels.add(new Panel("33", Panel.Type.A, new float[] {36+PANEL_PADDING*3+10, PANEL_PADDING*3-1, 10}, new float[] {180, 0, -180}, transform));
        this.panels.add(new Panel("34", Panel.Type.A, new float[] {36+PANEL_PADDING*4+10, PANEL_PADDING*3-1, 10}, new float[] {180, 0, -180}, transform));
        this.panels.add(new Panel("35", Panel.Type.A, new float[] {36+PANEL_PADDING*5+10, PANEL_PADDING*3-1, 9}, new float[] {180, -7, -180}, transform));
        this.panels.add(new Panel("36", Panel.Type.A, new float[] {36+PANEL_PADDING*6+10, PANEL_PADDING*3-1, 6}, new float[] {180, -16, -180}, transform));
        this.panels.add(new Panel("37", Panel.Type.A, new float[] {36+PANEL_PADDING*7+9, PANEL_PADDING*3-1, 0}, new float[] {180, -30, -180}, transform));

        // // row 5
        this.panels.add(new Panel("38", Panel.Type.F, new float[] {58+PANEL_PADDING*0, PANEL_PADDING*4-1, 10}, new float[] {180-8.4, 0, 180}, transform));
        this.panels.add(new Panel("39", Panel.Type.A, new float[] {48+PANEL_PADDING*1+10, PANEL_PADDING*4-1, 10}, new float[] {180-8.4, 0, -180}, transform));
        this.panels.add(new Panel("40", Panel.Type.A, new float[] {48+PANEL_PADDING*2+10, PANEL_PADDING*4-1, 10}, new float[] {180-8.4, 0, -180}, transform));
        this.panels.add(new Panel("41", Panel.Type.A, new float[] {48+PANEL_PADDING*3+10, PANEL_PADDING*4-1, 10}, new float[] {180-8.4, 0, -180}, transform));
        this.panels.add(new Panel("42", Panel.Type.A, new float[] {48+PANEL_PADDING*4+10, PANEL_PADDING*4-1, 8.5}, new float[] {180-8.4, -6, -180}, transform));
        this.panels.add(new Panel("43", Panel.Type.A, new float[] {48+PANEL_PADDING*5+10, PANEL_PADDING*4-1, 5.5}, new float[] {180-8.4,-16, -180}, transform));
        this.panels.add(new Panel("44", Panel.Type.C, new float[] {48+PANEL_PADDING*6, PANEL_PADDING*4-1, 4.5}, new float[] {-8.4, 180+30, 90}, transform));

        // // row 6
        this.panels.add(new Panel("45", Panel.Type.F, new float[] {70+PANEL_PADDING*0, PANEL_PADDING*5-1, 8}, new float[] {180-16.7, 0, 180}, transform));
        this.panels.add(new Panel("46", Panel.Type.A, new float[] {60+PANEL_PADDING*1+10, PANEL_PADDING*5-1, 8}, new float[] {180-16.7, 0, -180}, transform));
        this.panels.add(new Panel("47", Panel.Type.A, new float[] {60+PANEL_PADDING*2+10, PANEL_PADDING*5-1, 8}, new float[] {180-16.7, 0, -180}, transform));
        this.panels.add(new Panel("48", Panel.Type.A, new float[] {60+PANEL_PADDING*3+10, PANEL_PADDING*5-1, 7}, new float[] {180-16.7, -6, -180}, transform));
        this.panels.add(new Panel("49", Panel.Type.A, new float[] {60+PANEL_PADDING*4+10, PANEL_PADDING*5-1.5, 4}, new float[] {180-16.7, -15, -180}, transform));
        this.panels.add(new Panel("50", Panel.Type.E, new float[] {60+PANEL_PADDING*5+6, PANEL_PADDING*5+8, 5}, new float[] {0, 0, 0}, transform));

        // // row 7
        this.panels.add(new Panel("51", Panel.Type.F, new float[] {82+PANEL_PADDING*0, PANEL_PADDING*6-1, 4}, new float[] {180-25, 0, 180}, transform));
        this.panels.add(new Panel("52", Panel.Type.A, new float[] {72+PANEL_PADDING*1+10, PANEL_PADDING*6-1, 4}, new float[] {180-25, 0, -180}, transform));
        this.panels.add(new Panel("53", Panel.Type.B, new float[] {72+PANEL_PADDING*2+10, PANEL_PADDING*6-1.25, 3}, new float[] {180-25, -5, 180}, transform));
        this.panels.add(new Panel("54", Panel.Type.D, new float[] {72+PANEL_PADDING*3, PANEL_PADDING*6+6-2, 0.5}, new float[] {-25, 15, -90}, transform));
      }


      transform.pop();
    }
  }
}

public static class Panel extends LXModel {

  public final String id;

  private enum Type {
    // from bottom right corners (go from biggest to smallest)
    A (new float[][] { // square
      {10, 0 }, { 9, 0 }, { 8, 0 }, { 7, 0 }, { 6, 0 }, { 5, 0 }, { 4, 0 }, { 3, 0 }, { 2, 0 }, { 1, 0 }, { 0, 0 },
      { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 }, { 7, 1 }, { 8, 1 }, { 9, 1 }, {10, 1 },
      {10, 2 }, { 9, 2 }, { 8, 2 }, { 7, 2 }, { 6, 2 }, { 5, 2 }, { 4, 2 }, { 3, 2 }, { 2, 2 }, { 1, 2 }, { 0, 2 },
      { 0, 3 }, { 1, 3 }, { 2, 3 }, { 3, 3 }, { 4, 3 }, { 5, 3 }, { 6, 3 }, { 7, 3 }, { 8, 3 }, { 9, 3 }, {10, 3 },
      {10, 4 }, { 9, 4 }, { 8, 4 }, { 7, 4 }, { 6, 4 }, { 5, 4 }, { 4, 4 }, { 3, 4 }, { 2, 4 }, { 1, 4 }, { 0, 4 },
      { 0, 5 }, { 1, 5 }, { 2, 5 }, { 3, 5 }, { 4, 5 }, { 5, 5 }, { 6, 5 }, { 7, 5 }, { 8, 5 }, { 9, 5 }, {10, 5 },
      {10, 6 }, { 9, 6 }, { 8, 6 }, { 7, 6 }, { 6, 6 }, { 5, 6 }, { 4, 6 }, { 3, 6 }, { 2, 6 }, { 1, 6 }, { 0, 6 },
      { 0, 7 }, { 1, 7 }, { 2, 7 }, { 3, 7 }, { 4, 7 }, { 5, 7 }, { 6, 7 }, { 7, 7 }, { 8, 7 }, { 9, 7 }, {10, 7 },
      {10, 8 }, { 9, 8 }, { 8, 8 }, { 7, 8 }, { 6, 8 }, { 5, 8 }, { 4, 8 }, { 3, 8 }, { 2, 8 }, { 1, 8 }, { 0, 8 },
      { 0, 9 }, { 1, 9 }, { 2, 9 }, { 3, 9 }, { 4, 9 }, { 5, 9 }, { 6, 9 }, { 7, 9 }, { 8, 9 }, { 9, 9 }, {10, 9 },
      {10, 10}, { 9, 10}, { 8, 10}, { 7, 10}, { 6, 10}, { 5, 10}, { 4, 10}, { 3, 10}, { 2, 10}, { 1, 10}, { 0, 10}
    }),

    B (new float[][] { // almost square 1
      {10, 0 }, { 9, 0 }, { 8, 0 }, { 7, 0 }, { 6, 0 }, { 5, 0 }, { 4, 0 }, { 3, 0 }, { 2, 0 }, { 1, 0 }, { 0, 0 },
      { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 }, { 7, 1 }, { 8, 1 }, { 9, 1 }, {10, 1 },
      {10, 2 }, { 9, 2 }, { 8, 2 }, { 7, 2 }, { 6, 2 }, { 5, 2 }, { 4, 2 }, { 3, 2 }, { 2, 2 }, { 1, 2 }, { 0, 2 },
      { 0, 3 }, { 1, 3 }, { 2, 3 }, { 3, 3 }, { 4, 3 }, { 5, 3 }, { 6, 3 }, { 7, 3 }, { 8, 3 }, { 9, 3 }, {10, 3 },
      {10, 4 }, { 9, 4 }, { 8, 4 }, { 7, 4 }, { 6, 4 }, { 5, 4 }, { 4, 4 }, { 3, 4 }, { 2, 4 }, { 1, 4 }, { 0, 4 },
      { 0, 5 }, { 1, 5 }, { 2, 5 }, { 3, 5 }, { 4, 5 }, { 5, 5 }, { 6, 5 }, { 7, 5 }, { 8, 5 }, { 9, 5 }, {10, 5 },
      {10, 6 }, { 9, 6 }, { 8, 6 }, { 7, 6 }, { 6, 6 }, { 5, 6 }, { 4, 6 }, { 3, 6 }, { 2, 6 }, { 1, 6 }, { 0, 6 },
      { 0, 7 }, { 1, 7 }, { 2, 7 }, { 3, 7 }, { 4, 7 }, { 5, 7 }, { 6, 7 }, { 7, 7 }, { 8, 7 }, { 9, 7 }, {10, 7 },
      {10, 8 }, { 9, 8 }, { 8, 8 }, { 7, 8 }, { 6, 8 }, { 5, 8 }, { 4, 8 }, { 3, 8 }, { 2, 8 }, { 1, 8 },
      { 4, 9 }, { 5, 9 }, { 6, 9 }, { 7, 9 }, { 8, 9 }, { 9, 9 }, {10, 9 },
      {10, 10}, { 9, 10}, { 8, 10}, { 7, 10}
    }),

    C (new float[][] { // almost square 2
      {10, 0 }, { 9, 0 }, { 8, 0 }, { 7, 0 }, { 6, 0 }, { 5, 0 }, { 4, 0 }, { 3, 0 }, { 2, 0 }, { 1, 0 }, { 0, 0 },
      { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 }, { 7, 1 }, { 8, 1 }, { 9, 1 }, {10, 1 },
      {10, 2 }, { 9, 2 }, { 8, 2 }, { 7, 2 }, { 6, 2 }, { 5, 2 }, { 4, 2 }, { 3, 2 }, { 2, 2 }, { 1, 2 }, { 0, 2 },
      { 0, 3 }, { 1, 3 }, { 2, 3 }, { 3, 3 }, { 4, 3 }, { 5, 3 }, { 6, 3 }, { 7, 3 }, { 8, 3 }, { 9, 3 }, {10, 3 },
      {10, 4 }, { 9, 4 }, { 8, 4 }, { 7, 4 }, { 6, 4 }, { 5, 4 }, { 4, 4 }, { 3, 4 }, { 2, 4 }, { 1, 4 }, { 0, 4 },
      { 0, 5 }, { 1, 5 }, { 2, 5 }, { 3, 5 }, { 4, 5 }, { 5, 5 }, { 6, 5 }, { 7, 5 }, { 8, 5 }, { 9, 5 }, {10, 5 },
      {10, 6 }, { 9, 6 }, { 8, 6 }, { 7, 6 }, { 6, 6 }, { 5, 6 }, { 4, 6 }, { 3, 6 }, { 2, 6 }, { 1, 6 }, { 0, 6 },
      { 0, 7 }, { 1, 7 }, { 2, 7 }, { 3, 7 }, { 4, 7 }, { 5, 7 }, { 6, 7 }, { 7, 7 }, { 8, 7 }, { 9, 7 }, {10, 7 },
      { 9, 8 }, { 8, 8 }, { 7, 8 }, { 6, 8 }, { 5, 8 }, { 4, 8 }, { 3, 8 }, { 2, 8 }, { 1, 8 }, { 0, 8 },
      { 0, 9 }, { 1, 9 }, { 2, 9 }, { 3, 9 }, { 4, 9 }, { 5, 9 }, { 6, 9 },
      { 0, 10}, { 1, 10}, { 2, 10}, { 3, 10}
    }),

    D (new float[][] { // almost triangle 1
      { 6, 8 },
      { 5, 7 }, { 6, 7 },
      { 6, 6 }, { 5, 6 }, { 4, 6 },
      { 3, 5 }, { 4, 5 }, { 5, 5 }, { 6, 5 },
      { 6, 4 }, { 5, 4 }, { 4, 4 }, { 3, 4 }, { 2, 4 },
      { 2, 3 }, { 3, 3 }, { 4, 3 }, { 5, 3 }, { 6, 3 },
      { 6, 2 }, { 5, 2 }, { 4, 2 }, { 3, 2 }, { 2, 2 }, { 1, 2 },
      { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 },
      { 6, 0 }, { 5, 0 }, { 4, 0 }, { 3, 0 }, { 2, 0 }, { 1, 0 }, { 0, 0 }
    }),

    E (new float[][] { // almost triange 2
      { 6, 0 },
      { 5, 1 }, { 6, 1 },
      { 6, 2 }, { 5, 2 }, { 4, 2 },
      { 3, 3 }, { 4, 3 }, { 5, 3 }, { 6, 3 },
      { 6, 4 }, { 5, 4 }, { 4, 4 }, { 3, 4 }, { 2, 4 },
      { 2, 5 }, { 3, 5 }, { 4, 5 }, { 5, 5 }, { 6, 5 },
      { 6, 6 }, { 5, 6 }, { 4, 6 }, { 3, 6 }, { 2, 6 }, { 1, 6 },
      { 0, 7 }, { 1, 7 }, { 2, 7 }, { 3, 7 }, { 4, 7 }, { 5, 7 }, { 6, 7 },
      { 6, 8 }, { 5, 8 }, { 4, 8 }, { 3, 8 }, { 2, 8 }, { 1, 8 }, { 0, 8 }
    }),

    F (new float[][] { // triangle
      { 9, 0 }, { 8, 0 }, { 7, 0 }, { 6, 0 }, { 5, 0 }, { 4, 0 }, { 3, 0 }, { 2, 0 }, { 1, 0 }, { 0, 0 },
      { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 }, { 7, 1 }, { 8, 1 },
      { 7, 2 }, { 6, 2 }, { 5, 2 }, { 4, 2 }, { 3, 2 }, { 2, 2 }, { 1, 2 }, { 0, 2 },
      { 0, 3 }, { 1, 3 }, { 2, 3 }, { 3, 3 }, { 4, 3 }, { 5, 3 }, { 6, 3 },
      { 5, 4 }, { 4, 4 }, { 3, 4 }, { 2, 4 }, { 1, 4 }, { 0, 4 },
      { 0, 5 }, { 1, 5 }, { 2, 5 }, { 3, 5 }, { 4, 5 },
      { 3, 6 }, { 2, 6 }, { 1, 6 }, { 0, 6 },
      { 0, 7 }, { 1, 7 }, { 2, 7 },
      { 1, 8 }, { 0, 8 },
      { 0, 9 }
    });

    public final float[][] coordinates;

    private Type(float[][] coordinates) {
      this.coordinates = coordinates;
    }

  }

  public Panel(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
    super(new Fixture(type, coordinates, rotations, transform));
    this.id = id;
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Panel.Type type, float[] coordinates, float[] rotations, LXTransform transform) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);

      for (int i = 0; i < type.coordinates.length; i++) {
        transform.push();
        transform.translate(type.coordinates[i][0], type.coordinates[i][1], 0);
        this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
        transform.pop();
      }

      transform.pop();
    }
  }
}

/**
 * Model of a single cube, which has an orientation and position on the
 * car. The position is specified in x,y,z coordinates with rotation. The
 * x axis is left->right, y is bottom->top, and z is front->back.
 * 
 * A cube's x,y,z position is specified as the left, bottom, front corner.
 * 
 * Dimensions are all specified in real-world inches.
 */
public static class Cube extends LXModel {

  public enum Type {

    //            Edge     |  LEDs   |  LEDs
    //            Length   |  Per    |  Per
    //            Inches   |  Meter  |  Edge
    SMALL         (12,        72,       15),
    MEDIUM        (18,        60,       23),
    LARGE         (24,        30,       15),
    LARGE_DOUBLE  (24,        60,       30);
    

    public final float EDGE_WIDTH;
    public final float EDGE_HEIGHT;

    public final int POINTS_PER_STRIP;
    public final int POINTS_PER_CUBE;
    public final int POINTS_PER_FACE;

    public final int LEDS_PER_METER;

    public final Face.Metrics FACE_METRICS;

    private Type(float edgeLength, int ledsPerMeter, int ledsPerStrip) {
      this.EDGE_WIDTH = this.EDGE_HEIGHT = edgeLength;

      this.POINTS_PER_STRIP = ledsPerStrip;
      this.POINTS_PER_CUBE = STRIPS_PER_CUBE*POINTS_PER_STRIP;
      this.POINTS_PER_FACE = Face.STRIPS_PER_FACE*POINTS_PER_STRIP;

      this.LEDS_PER_METER = ledsPerMeter;

      this.FACE_METRICS = new Face.Metrics(
        new Strip.Metrics(this.EDGE_WIDTH, POINTS_PER_STRIP, ledsPerMeter), 
        new Strip.Metrics(this.EDGE_HEIGHT, POINTS_PER_STRIP, ledsPerMeter)
      );
    }

  };

  public static final Type CUBE_TYPE_WITH_MOST_PIXELS = Type.LARGE_DOUBLE;

  public final static int FACES_PER_CUBE = 4; 

  public final static int STRIPS_PER_CUBE = FACES_PER_CUBE*Face.STRIPS_PER_FACE;

  public final static float CHANNEL_WIDTH = 1.5f;

  public final Type type;

  public final String id;

  /**
   * Immutable list of all cube faces
   */
  public final List<Face> faces;

  /**
   * Immutable list of all strips
   */
  public final List<Strip> strips;

  /**
   * Front left corner x coordinate 
   */
  public final float x;

  /**
   * Front left corner y coordinate 
   */
  public final float y;

  /**
   * Front left corner z coordinate 
   */
  public final float z;

  /**
   * Rotation about the x-axis 
   */
  public final float rx;

  /**
   * Rotation about the y-axis 
   */
  public final float ry;

  /**
   * Rotation about the z-axis 
   */
  public final float rz;

  public Cube(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t, Type type) {
    super(new Fixture(x, y, z, rx, ry, rz, t, type));
    Fixture fixture = (Fixture) this.fixtures.get(0);
    this.type     = type;
    this.id       = id;

    while (rx < 0) rx += 360;
    while (ry < 0) ry += 360;
    while (rz < 0) rz += 360;
    rx = rx % 360;
    ry = ry % 360;
    rz = rz % 360;

    this.x = x; 
    this.y = y;
    this.z = z;
    this.rx = rx;
    this.ry = ry;
    this.rz = rz;

    this.faces = Collections.unmodifiableList(fixture.faces);
    this.strips = Collections.unmodifiableList(fixture.strips);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Face> faces = new ArrayList<Face>();
    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t, Cube.Type type) {
      // LXTransform t = new LXTransform();
      t.push();
      t.translate(x, y, z);
      t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
      t.rotateX(rx * PI / 180.);
      t.rotateY(ry * PI / 180.);
      t.rotateZ(rz * PI / 180.);
      t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

      for (int i = 0; i < FACES_PER_CUBE; i++) {
        Face face = new Face(type.FACE_METRICS, (ry + 90*i) % 360, t);
        this.faces.add(face);
        for (Strip s : face.strips) {
          this.strips.add(s);
        }
        for (LXPoint p : face.points) {
          this.points.add(p);
        }
        t.translate(type.EDGE_WIDTH, 0, 0);
        t.rotateY(HALF_PI);
      }
      t.pop();
    }
  }
}

/**
 * A face is a component of a cube. It is comprised of four strips forming
 * the lights on this side of a cube. A whole cube is formed by four faces.
 */
public static class Face extends LXModel {

  public final static int STRIPS_PER_FACE = 3;

  public static class Metrics {
    final Strip.Metrics horizontal;
    final Strip.Metrics vertical;

    public Metrics(Strip.Metrics horizontal, Strip.Metrics vertical) {
      this.horizontal = horizontal;
      this.vertical = vertical;
    }
  }

  /**
   * Immutable list of strips
   */
  public final List<Strip> strips;

  /**
   * Rotation of the face about the y-axis
   */
  public final float ry;

  Face(Metrics metrics, float ry, LXTransform transform) {
    super(new Fixture(metrics, ry, transform));
    Fixture fixture = (Fixture) this.fixtures.get(0);
    this.ry = ry;
    this.strips = Collections.unmodifiableList(fixture.strips);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(Metrics metrics, float ry, LXTransform transform) {
      transform.push();
      for (int i = 0; i < STRIPS_PER_FACE; i++) {
        boolean isHorizontal = (i % 2 == 0);
        Strip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
        Strip strip = new Strip(stripMetrics, ry, transform, isHorizontal);
        this.strips.add(strip);
        transform.translate(isHorizontal ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
        transform.rotateZ(HALF_PI);
        for (LXPoint p : strip.points) {
          this.points.add(p);
        }
      }
      transform.pop();
    }
  }
}

/**
 * A strip is a linear run of points along a single edge of one cube.
 */
public static class Strip extends LXModel {

  public static final float INCHES_PER_METER = 39.3701;

  public static class Metrics {

    public final float length;
    public final int numPoints;
    public final int ledsPerMeter;

    public final float POINT_SPACING;

    public Metrics(float length, int numPoints, int ledsPerMeter) {
      this.length = length;
      this.numPoints = numPoints;
      this.ledsPerMeter = ledsPerMeter;
      this.POINT_SPACING = INCHES_PER_METER / ledsPerMeter;
    }

    public Metrics(int numPoints, float spacing) {
      this.length = numPoints * spacing;
      this.numPoints = numPoints;
      this.ledsPerMeter = (int)floor((INCHES_PER_METER / this.length) * numPoints);
      this.POINT_SPACING = spacing;
    }
  }

  public final Metrics metrics;

  /**
   * Whether this is a horizontal strip
   */
  public final boolean isHorizontal;

  /**
   * Rotation about the y axis
   */
  public final float ry;

  public Object obj1 = null, obj2 = null;

  Strip(Metrics metrics, float ry, List<LXPoint> points, boolean isHorizontal) {
    super(points);
    this.isHorizontal = isHorizontal;
    this.metrics = metrics;   
    this.ry = ry;
  }

  Strip(Metrics metrics, float ry, LXTransform transform, boolean isHorizontal) {
    super(new Fixture(metrics, ry, transform));
    this.metrics = metrics;
    this.isHorizontal = isHorizontal;
    this.ry = ry;
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Metrics metrics, float ry, LXTransform transform) {
      float offset = (metrics.length - (metrics.numPoints - 1) * metrics.POINT_SPACING) / 2.f;
      transform.push();
      transform.translate(offset, -Cube.CHANNEL_WIDTH/2.f, 0);
      for (int i = 0; i < metrics.numPoints; i++) {
        LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
        this.points.add(point);
        transform.translate(metrics.POINT_SPACING, 0, 0);
      }
      transform.pop();
    }
  }
}