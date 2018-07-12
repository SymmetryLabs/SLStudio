// file this will log to. will always append!
String LOGFILE = "out.json";

import oscP5.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

OscP5 oscP5;
PrintWriter out;

/**
 * Creates a new file including all subfolders
 */
void createFile(File f) {
  File parentDir = f.getParentFile();
  try {
    parentDir.mkdirs(); 
    f.createNewFile();
  }
  catch(Exception e) {
    e.printStackTrace();
  }
}   

void setup() {
  println("running setup");
  size(400, 400);
  frameRate(25);
  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this, 3131);

  File f = new File(dataPath(LOGFILE));
  if (!f.exists()) {
    println(f);
    createFile(f);
  } else {
  }
  try {
    out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));    
  }
  catch (IOException e) {
    e.printStackTrace();
  }
}

void draw() {
  background(0);
}

void oscEvent(OscMessage m) {

  JSONObject log = new JSONObject();
  JSONArray data = new JSONArray();

  log.setString("route", m.addrPattern());
  log.setString("raw", new String(m.getBytes()));
  
  Date d = new Date();
  log.setLong("time", d.getTime());
  log.setJSONArray("data", data);

  String tag = m.typetag();
  for (int i = 0; i < tag.length(); i++) {
    char c = tag.charAt(i);
    OscArgument arg = m.get(i);
    switch(c) {
    case 'i':
      data.append(arg.intValue());
      break;
    case 'f':
      data.append(arg.floatValue());
      break;
    case 's':
      data.append(arg.stringValue());
      break;
    case 'b':
      data.append(new String(arg.blobValue()));
      break;
    default:
      // println("UNKNOWN TYPE TAG");
      exit();
    }
    //Process char
  }
  
  // println("NEAT");

  //log.
  
  log.write(out, "compact");
  out.println("");

  //out.println(log.toString("compact"));
  out.flush();
}