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
static final float globalRotationY = 90;
static final float globalRotationZ = 0;

public SLModel buildModel() {

  // Any global transforms
  LXTransform transform = new LXTransform();
  transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
  transform.rotateX(globalRotationX * PI / 180.);
  transform.rotateY(globalRotationY * PI / 180.);
  transform.rotateZ(globalRotationZ * PI / 180.);

  final List<Wicket> wickets = new ArrayList<Wicket>();

  // Courtyard 1
  wickets.add(new Wicket("A", "10.200.1.24", Wicket.Type.INSIDE, new float[] {0, 0,  0*FEET}, new float[] {0, 180, 0}, transform));

  // Building 1
  //2
  wickets.add(new Wicket("B", "10.200.1.11", Wicket.Type.INSIDE, new float[] {0, 0,  28*FEET}, new float[] {0, 0, 0}, transform));
  //3
  wickets.add(new Wicket("C", "10.200.1.27", Wicket.Type.INSIDE, new float[] {0, 0,  43*FEET}, new float[] {0, 0, 0}, transform));

  // 36th Street
  //4 This Wicket Is Not ON 12/18/17
  wickets.add(new Wicket("D", "10.200.1.15", Wicket.Type.INSIDE, new float[] {0, 0, 72*FEET}, new float[] {0, 0, 0}, transform));
  //5
  wickets.add(new Wicket("E", "10.200.1.12", Wicket.Type.INSIDE, new float[] {0, 0, 83*FEET}, new float[] {0, 0, 0}, transform));
  
  // Building 2
  //6 
  wickets.add(new Wicket("F", "10.200.1.14", Wicket.Type.INSIDE, new float[] {0, 0, 106*FEET}, new float[] {0, 0, 0}, transform));
  //7
  wickets.add(new Wicket("G", "10.200.1.17", Wicket.Type.INSIDE, new float[] {0, 0, 122*FEET}, new float[] {0, 0, 0}, transform));

  // Courtyard 2
  //8
  wickets.add(new Wicket("H", "10.200.1.18", Wicket.Type.INSIDE, new float[] {0, 0, 147*FEET}, new float[] {0, 180, 0}, transform));
  //9
  wickets.add(new Wicket("I", "10.200.1.19", Wicket.Type.INSIDE, new float[] {0, 0, 157*FEET}, new float[] {0, 180, 0}, transform));
  //10
  wickets.add(new Wicket("J", "10.200.1.20", Wicket.Type.INSIDE, new float[] {0, 0, 167*FEET}, new float[] {0, 180, 0}, transform));

  // Building 3
  //11
  wickets.add(new Wicket("K", "10.200.1.21", Wicket.Type.INSIDE, new float[] {0, 0, 192*FEET}, new float[] {0, 0, 0}, transform));
  //12
  wickets.add(new Wicket("L", "10.200.1.22", Wicket.Type.INSIDE, new float[] {0, 0, 208*FEET}, new float[] {0, 0, 0}, transform));

  // 35th Street
  //13
  wickets.add(new Wicket("M", "10.200.1.23", Wicket.Type.INSIDE, new float[] {0, 0, 237*FEET}, new float[] {0, 180, 0}, transform));
  //14
  wickets.add(new Wicket("N", "10.200.1.28", Wicket.Type.INSIDE, new float[] {0, 0, 248*FEET}, new float[] {0, 180, 0}, transform)); //

  // Building 4
  //wickets.add(new Wicket("O", "10.200.1.24", Wicket.Type.INSIDE, new float[] {0, 0, 276*FEET}, new float[] {0, 0, 0}, transform)); //
  //15
  wickets.add(new Wicket("P", "10.200.1.26", Wicket.Type.INSIDE, new float[] {0, 0, 287*FEET}, new float[] {0, 180, 0}, transform)); //
  //16
  wickets.add(new Wicket("Q", "10.200.1.25", Wicket.Type.INSIDE, new float[] {0, 0, 311*FEET}, new float[] {0, 180, 0}, transform)); //
  
  // Courtyard 3
  //17
  wickets.add(new Wicket("R", "10.200.1.10", Wicket.Type.INSIDE, new float[] {0, 0, 331*FEET}, new float[] {0, 180, 0}, transform));
  //18
  wickets.add(new Wicket("S", "10.200.1.16", Wicket.Type.INSIDE, new float[] {0, 0, 341*FEET}, new float[] {0, 180, 0}, transform));
  //19
  wickets.add(new Wicket("T", "10.200.1.13", Wicket.Type.INSIDE, new float[] {0, 0, 351*FEET}, new float[] {0, 180, 0}, transform));


  // unknown?
  
  //wickets.add(new Wicket("R", "10.200.1.27", Wicket.Type.INSIDE, new float[] {0, 0, 321*FEET}, new float[] {0, 180, 0}, transform)); //
 // wickets.add(new Wicket("S", "10.200.1.28", Wicket.Type.INSIDE, new float[] {0, 0, 331*FEET}, new float[] {0, 180, 0}, transform)); //
  
 // wickets.add(new Wicket("O", "10.200.1.13", Wicket.Type.INSIDE, new float[] {0, 0,  43*FEET}, new float[] {0, 0, 0}, transform));



  Wicket[] wicketArr = new Wicket[wickets.size()];
  for (int i = 0; i < wicketArr.length; i++) {
    wicketArr[i] = wickets.get(i);
  }

  return new SLModel(wickets, wicketArr);
}