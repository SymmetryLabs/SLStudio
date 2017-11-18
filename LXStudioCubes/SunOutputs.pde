// public class SunOutput {

//   private final Sun sun;
//   private final List<Pixlite> pixlites = new ArrayList<Pixlite>();

//   public SunOutput(Sun sun, ) {
//     this.sun = sun;

//     // for (int i = 0; i < ipAddresses.length; i++) {
//     //   this.pixlites.add(new Pixlite(ipAddresses[i], sun.slices.get(i)));
//     // }
//   }
// }

// public class Pixlite {

//   public final String ipAddress;
//   public final List<OutputGroup> outputs = new ArrayList<OutputGroup>();

//   public Pixlite(String ipAddress) {
//     this.ipAddress = ipAddress;
    
//   }

// }

// public class Strip {



// }

// public class OutputGroupDatagrams {

//   private OutputGroup group = null;
//   private LXDatagram[] datagrams = new LXDatagram[2];

//   public OutputGroupDatagrams(String id, String ipAddress, int output) {
//     for (OutputGroup group : outputGroups) {
//       if (group.id.equals(id)) {
//         this.group = group;
//       }
//     }

//     if (group == null) {
//       println("TRYING TO OUTPUT TO A OUTPUTGROUP THAT IS NOT IN MODEL (ID: " + id + ")");
//     } else {

//       // build array of indices (note that we output to 2 strips by going up and down the one strip in model)
//       int counter = 0;
//       int[] indices = new int[group.getPoints().size()];
//       for (int i = 0; i < group.getPoints().size(); i++) {
//         indices[counter++] = group.getPoints().get(i).index;
//       }
      
//       // create array of indices for each datagram/universe
//       int counter1 = 0;
//       int[] firstIndices = new int[indices.length > 170 ? 170 : indices.length];
//       for (int i = 0; i < firstIndices.length; i++) {
//         firstIndices[i] = indices[counter1++];
//       }
//       int[] secondIndices = new int[indices.length - firstIndices.length];
//       for (int i = 0; i < secondIndices.length; i++) {
//         secondIndices[i] = indices[counter1++];
//       }

//       this.datagrams[0] = new ArtNetDatagram(ipAddress, firstIndices, output*2-2);
//       this.datagrams[1] = new ArtNetDatagram(ipAddress, secondIndices, output*2-1);
//     }
//   }

//   public LXDatagram[] getDatagrams() {
//     return datagrams;
//   }
// }

// public class OutputGroup {

//   public final String id;
  
//   private final List<LXPoint> points = new ArrayList<LXPoint>();

//   public OutputGroup(String id) {
//     this.id = id;
//   }

//   public OutputGroup(String id, List<LXPoint> points) {
//     this.id = id;
//     addPoints(points);
//   }

//   public OutputGroup(String id, LXModel fixture) {
//     this.id = id;
//     addPoints(Arrays.asList(fixture.points));
//   }

//   public OutputGroup(List<LXPoint> points) {
//     this("", points);
//   }

//   public OutputGroup(LXModel fixture) {
//     this("", Arrays.asList(fixture.points));
//   }

//   public List<LXPoint> getPoints() {
//     return points;
//   }

//   public OutputGroup addPoints(List<LXPoint> points) {
//     for (LXPoint point : points) {
//       this.points.add(point);
//     }
//     return this;
//   }

//   public OutputGroup addPoints(List<LXPoint> points, boolean reverse) {
//     List<LXPoint> localPoints = new ArrayList<LXPoint>(points);

//     if (reverse) {
//       Collections.reverse(localPoints);
//     }

//     addPoints(localPoints);
//     return this;
//   }

//   public OutputGroup addPoints(LXModel fixture) {
//     addPoints(Arrays.asList(fixture.points));
//     return this;
//   }

//   public OutputGroup addPoints(LXModel fixture, boolean reverse) {
//     addPoints(Arrays.asList(fixture.points), reverse);
//     return this;
//   }

//   public OutputGroup reversePoints() {
//     Collections.reverse(points);
//     return this;
//   }
// }