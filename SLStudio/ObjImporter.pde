public class ObjImporter {

  private final String path;

  private final ObjConfigReader configReader;

  private final ObjConfig config;

  private final List<LXModel> models = new ArrayList<LXModel>();
  
  public ObjImporter(String path, LXTransform transform) {
    this.path = sketchPath("") + path;
    this.configReader = new ObjConfigReader(this.path);
    this.config = configReader.readConfig("global");

    if (config.enabled) {
      buildModels(transform);
    }
  }

  public List<LXModel> getModels() {
    return models;
  }

  private void buildModels(LXTransform transform) {
    transform.push();
    transform.translate(config.x, config.y, config.z);
    transform.rotateX(config.xRotation * PI / 180.);
    transform.rotateY(config.yRotation * PI / 180.);
    transform.rotateZ(config.zRotation * PI / 180.);

    for (File file : loadObjFiles()) {
      try {
        ObjModelBuilder objModelBuilder = new ObjModelBuilder(file, configReader);

        if (objModelBuilder.config.enabled) {
          this.models.add(objModelBuilder.buildModel(transform));
        }
      } catch (Exception e) { 
        println("Problem with obj file: " + file.getName());
      }
    }

    transform.pop();
  }

  private List<File> loadObjFiles() {
    File[] directory = new File(path).listFiles();
    List<File> objFiles = new ArrayList<File>();

    for (File file : directory) {
      if (file.getName().matches("([^\\s]+(\\.(?i)(obj))$)")) {
        
        // don't allow an obj fixture called 'global'
        if (!file.getName().equals("global.obj")) {
          objFiles.add(file);
        }
      }
    }

    return objFiles;
  }
}

class ObjConfig {
  boolean enabled = true;
  float x = 0;
  float y = 0;
  float z = 0;
  float xRotation = 0;
  float yRotation = 0;
  float zRotation = 0;
  float scale = 1.0;
}

class ObjConfigReader {

  private final String CONFIG_FILENAME = "objConfig.txt";

  private String path;

  private ObjConfigReader(String path) {
    this.path = path;
  }

  private ObjConfig readConfig(String fileName) {
    ObjConfig config = new ObjConfig();

    try {
      File file = new File(path + "/" + CONFIG_FILENAME);
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        if (line.contains(fileName + ".enabled")) config.enabled = line.contains("true");
        else if (line.contains(fileName + ".x")) config.x = extractFloat(line);
        else if (line.contains(fileName + ".y")) config.y = extractFloat(line);
        else if (line.contains(fileName + ".z")) config.z = extractFloat(line);
        else if (line.contains(fileName + ".rotateX")) config.xRotation = extractFloat(line);
        else if (line.contains(fileName + ".rotateY")) config.yRotation = extractFloat(line);
        else if (line.contains(fileName + ".rotateZ")) config.zRotation = extractFloat(line);
        else if (line.contains(fileName + ".scale")) config.scale = extractFloat(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return config;
  }

  private float extractFloat(String line) {

    String str = line.replaceAll("[^-+.0123456789]", "");
    if (str.startsWith(".")) {
      str = str.substring(1);
    }

    return Float.valueOf(str);
  }

}

class ObjModelBuilder {

  private String name;

  private ObjConfig config;

  private List<float[]> vertices;

  private float xMin = 0, xMax = 0;
  private float yMin = 0, yMax = 0;
  private float zMin = 0, zMax = 0;

  private ObjModelBuilder(File file, ObjConfigReader configReader) {
    this.name = file.getName().substring(0, file.getName().lastIndexOf('.'));
    this.config = configReader.readConfig(name);

    readVertices(file);
    calculateBounds();
    scaleFixture();
    centerFixture();
  }

  private void readVertices(File file) {
    this.vertices = new ArrayList<float[]>();

    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

      String line;
      while ((line = bufferedReader.readLine()) != null) {

        // if it's a vertex
        if (line.startsWith("v ")) {
          String[] vertexString = line.substring(2).split("\\s+");

          float x = Float.parseFloat(vertexString[0]) * INCHES_PER_METER;
          float y = Float.parseFloat(vertexString[1]) * INCHES_PER_METER;
          float z = Float.parseFloat(vertexString[2]) * INCHES_PER_METER;

          this.vertices.add(new float[] {x, y, z});
        }
      }
    } catch (Exception e) { 
      println("Problem reading vertices in obj file: " + name);
    }
  }

  private void calculateBounds() {
    for (float[] vertex : this.vertices) {
      if (vertex[0] < xMin) xMin = vertex[0];
      if (vertex[0] > xMax) xMax = vertex[0];
      if (vertex[1] < yMin) yMin = vertex[1];
      if (vertex[1] > yMax) yMax = vertex[1];
      if (vertex[2] < zMin) zMin = vertex[2];
      if (vertex[2] > zMax) zMax = vertex[2];
    }
  }

  private void scaleFixture() {
    for (float[] vertex : this.vertices) {
      vertex[0] = config.scale * (vertex[0] += (-xMin));
      vertex[1] = config.scale * (vertex[1] += (-yMin));
      vertex[2] = config.scale * (vertex[2] += (-zMin));
    }
  }

  private void centerFixture() {
    calculateBounds();
    float translateX = (-Math.abs(xMax - xMin)/2) + (-xMin);
    float translateY = (-Math.abs(yMax - yMin)/2) + (-yMin);
    float translateZ = (-Math.abs(zMax - zMin)/2) + (-zMin);

    for (float[] vertex : this.vertices) {
      vertex[0] += translateX;
      vertex[1] += translateY;
      vertex[2] += translateZ;
    }
  }

  private LXModel buildModel(LXTransform transform) {
    List<LXPoint> points = new ArrayList<LXPoint>();

    for (float[] vertex : this.vertices) {
      transform.push();
      transform.translate(config.x, config.y, config.z);
      transform.rotateX(config.xRotation * PI / 180.);
      transform.rotateY(config.yRotation * PI / 180.);
      transform.rotateZ(config.zRotation * PI / 180.);

      transform.translate(vertex[0], vertex[1], vertex[2]);

      LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
      points.add(point);

      transform.pop();
    }
    
    println("Created a fixture with obj file: " + name);
    return new LXModel(points);
  }

  private LXModel getModel() {
    return model;
  }
}