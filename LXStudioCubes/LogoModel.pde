public static class Ceiling extends LXModel {

    public final List<Strip> strips;

    public Ceiling(List<Strip> strips) {
        super(new Fixture(strips));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.strips = Collections.unmodifiableList(fixture.strips);
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Strip> strips;

        private Fixture(List<Strip> strips) {
            this.strips = strips;

            for (Strip strip : strips) {
                for (LXPoint point : strip.points) {
                    this.points.add(point);
                }
            }
        }
    }
}

public static class Pillar extends LXModel {

    public final List<Strip> strips;

    public Pillar(List<Strip> strips) {
        super(new Fixture(strips));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.strips = Collections.unmodifiableList(fixture.strips);
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Strip> strips;

        private Fixture(List<Strip> strips) {
            this.strips = strips;

            for (Strip strip : strips) {
                for (LXPoint point : strip.points) {
                    this.points.add(point);
                }
            }
        }
    }
}

public static class Desk extends LXModel {

  public final List<Strip> strips;

  public final List<OutputGroup> outputGroups;

  public Desk(String[] ids, float[] coordinates, float[] rotations, LXTransform t) {
    super(new Fixture(ids, coordinates, rotations, t));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    this.strips = Collections.unmodifiableList(fixture.strips);
    this.outputGroups = Collections.unmodifiableList(fixture.outputGroups);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Strip> strips = new ArrayList<Strip>();

    private final List<OutputGroup> outputGroups = new ArrayList<OutputGroup>();

    private Fixture(String[] ids, float[] coordinates, float[] rotations, LXTransform t) {
      t.push();
      t.translate(coordinates[0], coordinates[1], coordinates[2]);
      //t.translate(DESK_LENGTH/2, DESK_HEIGHT/2, DESK_WIDTH/2);
      t.rotateX(rotations[0] * PI / 180.);
      t.rotateY(rotations[1] * PI / 180.);
      t.rotateZ(rotations[2] * PI / 180.);
      //t.translate(-DESK_LENGTH/2, -DESK_HEIGHT/2, -DESK_WIDTH/2);

      Strip.Metrics verticalMetrics   = new Strip.Metrics(DESK_VERTICAL_NUM_POINTS,   DEFAULT_PIXEL_PITCH);
      Strip.Metrics horizontalMetrics = new Strip.Metrics(DESK_HORIZONTAL_NUM_POINTS, DEFAULT_PIXEL_PITCH);

      // 1) left, top
      OutputGroup outputGroup1 = new OutputGroup(ids[0]);
      t.push();
      t.translate(0, 0, 0);
          t.push();
          t.rotateZ(-90 * PI / 180.);
          Strip stripV1 = new Strip("-", verticalMetrics, rotations[1], t, false);
          this.strips.add(stripV1);
          outputGroup1.add(stripV1);
          t.pop();

          t.push();
          t.translate(0, DESK_HEIGHT, 0);
          Strip stripH1 = new Strip("-", horizontalMetrics, rotations[1], t, true);
          this.strips.add(stripH1);
          outputGroup1.add(stripH1);
          t.pop();
      t.pop();
      this.outputGroups.add(outputGroup1);

      // 2) left, bottom
      OutputGroup outputGroup2 = new OutputGroup(ids[1]);
      t.push();
      t.translate(0, 0, -DESK_STRIP_SPACING);
          t.push();
          t.rotateZ(-90 * PI / 180.);
          Strip stripV2 = new Strip("-", verticalMetrics, rotations[1], t, false);
          this.strips.add(stripV2);
          outputGroup2.add(stripV2);
          t.pop();

          t.push();
          t.translate(0, DESK_HEIGHT, 0);
          Strip stripH2 = new Strip("-", horizontalMetrics, rotations[1], t, true);
          this.strips.add(stripH2);
          outputGroup2.add(stripH2);
          t.pop();
      t.pop();
      this.outputGroups.add(outputGroup2);

      // 3) right, top
      OutputGroup outputGroup3 = new OutputGroup(ids[2]);
      t.push();
      t.translate(DESK_LENGTH, 0, 0);
          t.push();
          t.rotateZ(-90 * PI / 180.);
          Strip stripV3 = new Strip("-", verticalMetrics, rotations[1], t, false);
          this.strips.add(stripV3);
          outputGroup3.add(stripV3);
          t.pop();

          t.push();
          t.translate(0, DESK_HEIGHT, 0);
          t.rotateY(180 * PI / 180.);
          Strip stripH3 = new Strip("-", horizontalMetrics, rotations[1], t, true);
          this.strips.add(stripH3);
          outputGroup3.add(stripH3);
          t.pop();
      t.pop();
      this.outputGroups.add(outputGroup3);

      // 4) right, top
      OutputGroup outputGroup4 = new OutputGroup(ids[3]);
      t.push();
      t.translate(DESK_LENGTH, 0, -DESK_STRIP_SPACING);
          t.push();
          t.rotateZ(-90 * PI / 180.);
          Strip stripV4 = new Strip("-", verticalMetrics, rotations[1], t, false);
          this.strips.add(stripV4);
          outputGroup4.add(stripV4);
          t.pop();

          t.push();
          t.translate(0, DESK_HEIGHT, 0);
          t.rotateY(180 * PI / 180.);
          Strip stripH4 = new Strip("-", horizontalMetrics, rotations[1], t, true);
          this.strips.add(stripH4);
          outputGroup4.add(stripH4);
          t.pop();
      t.pop();
      this.outputGroups.add(outputGroup4);

      t.pop();

      for (Strip strip : strips) {
        for (LXPoint point : strip.points) {
            this.points.add(point);
        }
      }
    }
  }
}

public static class RubrikLogo extends LXModel {

    public static final float WIDTH = 32.5;

    public final List<Box> boxes;

    public final List<Triangle> triangles;

    public final List<Strip> strips;

    public final List<OutputGroup> outputGroups;

    public RubrikLogo(String[] ids, float[] coordinates, float[] rotations, LXTransform t) {
        super(new Fixture(ids, coordinates, rotations, t));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.boxes = Collections.unmodifiableList(fixture.boxes);
        this.triangles = Collections.unmodifiableList(fixture.triangles);
        this.strips = Collections.unmodifiableList(fixture.strips);
        this.outputGroups = Collections.unmodifiableList(fixture.outputGroups);
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Box> boxes = new ArrayList<Box>();

        private final List<Triangle> triangles = new ArrayList<Triangle>();

        private final List<Strip> strips = new ArrayList<Strip>();

        private final List<OutputGroup> outputGroups = new ArrayList<OutputGroup>();

        private Fixture(String[] ids, float[] coordinates, float[] rotations, LXTransform t) {
            t.push();
            t.translate(coordinates[0], coordinates[1], coordinates[2]);
            //t.translate(type.WIDTH/2, type.HEIGHT/2, type.WIDTH/2);
            t.rotateX(rotations[0] * PI / 180.);
            t.rotateY(rotations[1] * PI / 180.);
            t.rotateZ(rotations[2] * PI / 180.);
            //t.translate(-type.WIDTH/2, -type.HEIGHT/2, -type.WIDTH/2);

            for(int i = 1; i < 9; i++) {
                switch (i) {
                    case 1: // top left
                        OutputGroup outputGroup1 = new OutputGroup(ids[0]);

                        Triangle triangle1 = new Triangle(ids[0], new float[] { 12, WIDTH+6, 0}, new float[] { 0, 0, 30 }, t);
                        this.triangles.add(triangle1);
                        outputGroup1.add((LXModel)triangle1);

                        Box smallBox1 = new Box(ids[0], new float[] { 7, WIDTH-1.5, 0 }, new float[] { 0, 0, 90 }, t, Box.Type.SMALL);
                        this.boxes.add(smallBox1);
                        outputGroup1.add((LXModel)smallBox1);

                        Triangle triangle8 = new Triangle(ids[0], new float[] { 4, WIDTH-2.5, 0}, new float[] { 0, 0, 65 }, t);
                        this.triangles.add(triangle8);
                        outputGroup1.add((LXModel)triangle8);

                        this.outputGroups.add(outputGroup1);
                        break;

                    case 2: // top right
                        OutputGroup outputGroup2 = new OutputGroup(ids[1]);

                        Box largeBox1 = new Box(ids[1], new float[] { WIDTH/2, WIDTH, 0 }, new float[] { 0, 0, 225 }, t, Box.Type.LARGE);
                        this.boxes.add(largeBox1);
                        outputGroup2.add((LXModel)largeBox1);

                        this.outputGroups.add(outputGroup2);
                        break;

                    case 3: // right top
                        OutputGroup outputGroup3 = new OutputGroup(ids[2]);

                        Triangle triangle3 = new Triangle(ids[2], new float[] { WIDTH+6.5 , WIDTH-0.5, 0}, new float[] { 0, 0, -65 }, t);
                        this.triangles.add(triangle3);
                        outputGroup3.add((LXModel)triangle3);

                        Box smallBox2 = new Box(ids[2], new float[] { WIDTH-1, WIDTH-2.5, 0 }, new float[] { 0, 0, 0 }, t, Box.Type.SMALL);
                        this.boxes.add(smallBox2);
                        outputGroup3.add((LXModel)smallBox2);

                        Triangle triangle2 = new Triangle(ids[0], new float[] { WIDTH-2, WIDTH+7, 0}, new float[] { 0, 0, -31 }, t);
                        this.triangles.add(triangle2);
                        outputGroup3.add((LXModel)triangle2);

                        this.outputGroups.add(outputGroup3);
                        break;

                    case 4: // right bottom
                        OutputGroup outputGroup4 = new OutputGroup(ids[3]);

                        Box largeBox2 = new Box(ids[3], new float[] { WIDTH+1, WIDTH/2+1.5, 0 }, new float[] { 0, 0, 135 }, t, Box.Type.LARGE);
                        this.boxes.add(largeBox2);
                        outputGroup4.add((LXModel)largeBox2);

                        this.outputGroups.add(outputGroup4);
                        break;

                    case 5: // bottom right
                        OutputGroup outputGroup5 = new OutputGroup(ids[4]);

                        Triangle triangle5 = new Triangle(ids[4], new float[] { WIDTH, 6, 0}, new float[] { 0, 0, -152 }, t);
                        this.triangles.add(triangle5);
                        outputGroup5.add((LXModel)triangle5);

                        Box smallBox3 = new Box(ids[4], new float[] { WIDTH-1.5, 8, 0 }, new float[] { 0, 0, 90 }, t, Box.Type.SMALL);
                        this.boxes.add(smallBox3);
                        outputGroup5.add((LXModel)smallBox3);

                        Triangle triangle4 = new Triangle(ids[4], new float[] { WIDTH+7, 15, 0}, new float[] { 0, 0, -112 }, t);
                        this.triangles.add(triangle4);
                        outputGroup5.add((LXModel)triangle4);

                        this.outputGroups.add(outputGroup5);
                        break;

                    case 6: // bottom left
                        OutputGroup outputGroup6 = new OutputGroup(ids[5]);

                        Box largeBox3 = new Box(ids[5], new float[] { WIDTH/2+1.5, 1, 0 }, new float[] { 0, 0, 45 }, t, Box.Type.LARGE);
                        this.boxes.add(largeBox3);
                        outputGroup6.add((LXModel)largeBox3);

                        this.outputGroups.add(outputGroup6);
                        break;

                    case 7: // left bottom
                        OutputGroup outputGroup7 = new OutputGroup(ids[6]);

                        Triangle triangle7 = new Triangle(ids[6], new float[] { 5.5, 13, 0}, new float[] { 0, 0, -242 }, t);
                        this.triangles.add(triangle7);
                        outputGroup7.add((LXModel)triangle7);

                        Box smallBox4 = new Box(ids[6], new float[] { 7, 7, 0 }, new float[] { 0, 0, 0 }, t, Box.Type.SMALL);
                        this.boxes.add(smallBox4);
                        outputGroup7.add((LXModel)smallBox4);

                        Triangle triangle6 = new Triangle(ids[6], new float[] { 14, 5, 0}, new float[] { 0, 0, -200 }, t);
                        this.triangles.add(triangle6);
                        outputGroup7.add((LXModel)triangle6);

                        this.outputGroups.add(outputGroup7);
                        break;

                    case 8: // left top
                        OutputGroup outputGroup8 = new OutputGroup(ids[7]);

                        Box largeBox4 = new Box(ids[7], new float[] { 0, WIDTH/2, 0 }, new float[] { 0, 0, 315 }, t, Box.Type.LARGE);
                        this.boxes.add(largeBox4);
                        outputGroup8.add((LXModel)largeBox4);

                        this.outputGroups.add(outputGroup8);
                        break;
                }
            }

            for (Box box : boxes) {
                for (Strip strip : box.strips) {
                    this.strips.add(strip);
                }
                for (LXPoint point : box.points) {
                    this.points.add(point);
                }
            }

            for (Triangle triangle : triangles) {
                for (LXPoint point : triangle.points) {
                    this.points.add(point);
                }
            }

            t.pop();
        }
    }
}

private static class OutputGroup {

    public final String id;

    private final List<LXPoint> points = new ArrayList<LXPoint>();

    private OutputGroup(String id) {
        this.id = id;
    }

    public void add(LXModel model) {
        for (LXPoint p : model.points) {
            this.points.add(p);
        }
    }

    public List<LXPoint> getPoints() {
        return points;
    }
}

public static class Triangle extends LXModel {

    public static final float PIXEL_PITCH = 0.65;

    public final String id;

    public Triangle(String id, float[] coordinates, float[] rotations, LXTransform t) {
        super(new Fixture(coordinates, rotations, t));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.id = id;
    }

    private static class Fixture extends LXAbstractFixture {

        private Fixture(float[] coordinates, float[] rotations, LXTransform t) {
            t.push();
            t.translate(coordinates[0], coordinates[1], coordinates[2]);
            t.rotateX(rotations[0] * PI / 180.);
            t.rotateY(rotations[1] * PI / 180.);
            t.rotateZ(rotations[2] * PI / 180.);

            for (int i = 0; i < 3; i++) {
                t.push();
                t.translate(PIXEL_PITCH*2, PIXEL_PITCH/2*i, 0);
                this.points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.pop();
            }

            for (int i = 0; i < 3; i++) {
                t.push();
                t.translate(PIXEL_PITCH+PIXEL_PITCH*i, PIXEL_PITCH*2, 0);
                this.points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.pop();
            }

            for (int i = 3; i > 0; i--) {
                t.push();
                t.translate(PIXEL_PITCH*i, PIXEL_PITCH*3, 0);
                this.points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.pop();
            }

            for (int i = 0; i < 5; i++) {
                t.push();
                t.translate(PIXEL_PITCH*i, PIXEL_PITCH*4, 0);
                this.points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.pop();
            }

            for (int i = 5; i > 0; i--) {
                t.push();
                t.translate(PIXEL_PITCH*(i-1), PIXEL_PITCH*5, 0);
                this.points.add(new LXPoint(t.x(), t.y(), t.z()));
                t.pop();
            }

            t.pop();
        }
    }

}

public static class Box extends LXModel {

    public static final float PIXEL_PITCH = 1;

    public final String id;

    public final List<Strip> strips;

    public enum Type {
        SMALL(7),
        LARGE(11);

        public final int NUM_POINTS_PER_STRIP;
        public final int NUM_ROWS_OF_STRIPS;
        public final float WIDTH;

        private Type(int size) {
            this.NUM_POINTS_PER_STRIP = size;
            this.NUM_ROWS_OF_STRIPS = size;
            this.WIDTH = size * PIXEL_PITCH;
        }
    }

    public Box(String id, float[] coordinates, float[] rotations, LXTransform t, Type type) {
        super(new Fixture(coordinates, rotations, t, type));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.id = id;
        this.strips = Collections.unmodifiableList(fixture.strips);
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Strip> strips = new ArrayList<Strip>();

        private Fixture(float[] coordinates, float[] rotations, LXTransform t, Box.Type type) {
            t.push();
            t.translate(coordinates[0], coordinates[1], coordinates[2]);
            t.translate(type.WIDTH/2, type.WIDTH/2, 0);
            t.rotateX(rotations[0] * PI / 180.);
            t.rotateY(rotations[1] * PI / 180.);
            t.rotateZ(rotations[2] * PI / 180.);
            t.translate(-type.WIDTH/2, -type.WIDTH/2, 0);

            Strip.Metrics metrics = new Strip.Metrics(type.NUM_POINTS_PER_STRIP, PIXEL_PITCH);

            t.translate(0, type.WIDTH, 0);
            for (int i = 0; i < type.NUM_ROWS_OF_STRIPS; i++) {
                if (i + 1 % 2 == 0) {
                    t.push();
                    t.translate(type.WIDTH, 0, 0);
                    t.rotateZ(180 * PI / 180.);
                }

                Strip strip = new Strip("-", metrics, rotations[1], t, false);
                this.strips.add(strip);

                for (LXPoint point : strip.points) {
                    this.points.add(point);
                }

                if (i + 1 % 2 == 0) {
                    t.pop();
                }

                t.translate(0, -PIXEL_PITCH, 0);
            }

            t.pop();
        }
    }
}