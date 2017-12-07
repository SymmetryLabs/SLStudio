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
  transform.rotateY(globalRotationY * PI / 180.);
  transform.rotateX(globalRotationX * PI / 180.);
  transform.rotateZ(globalRotationZ * PI / 180.);

  final List<Wicket> wickets = new ArrayList<Wicket>();

  wickets.add(new Wicket("A", "10.200.1.10", Wicket.Type.INSIDE, new float[] {0, 0,       0}, new float[] {0, 0, 0}, transform));
  wickets.add(new Wicket("B", "10.200.1.11", Wicket.Type.INSIDE, new float[] {0, 0, 12*FEET}, new float[] {0, 0, 0}, transform));
  wickets.add(new Wicket("C", "10.200.1.12", Wicket.Type.INSIDE, new float[] {0, 0, 36*FEET}, new float[] {0, 0, 0}, transform)); // actually outside but INSIDE for now
  wickets.add(new Wicket("D", "10.200.1.13", Wicket.Type.INSIDE, new float[] {0, 0, 44*FEET}, new float[] {0, 0, 0}, transform)); // actually outside but INSIDE for now
  wickets.add(new Wicket("E", "10.200.1.14", Wicket.Type.INSIDE, new float[] {0, 0, 68*FEET}, new float[] {0, 0, 0}, transform));

  Wicket[] wicketArr = new Wicket[wickets.size()];
  for (int i = 0; i < wicketArr.length; i++) {
    wicketArr[i] = wickets.get(i);
  }

  return new SLModel(wickets, wicketArr);
}