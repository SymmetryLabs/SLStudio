package com.symmetrylabs.shows.mikey;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.List;

public class MikeyShow implements Show {
    public static final String SHOW_NAME = "mikey";

    @Override
    public SLModel buildModel() {
        return MikeyModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        MikeyPixlite pixlite = new MikeyPixlite(lx, "192.168.0.50", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            Strip.Metrics metrics = new Strip.Metrics(200, 1); //strip config
            t.push();
            Strip strip = new Strip("1", metrics, t);         //create the first strip
            strips.add(strip);                                                  //add the first strip to strip array
            t.translate(0, 2, 0);                                   //translate to 2nd strip location
            Strip stripRev = new Strip("2", metrics, t);                   //create the 2nd strip
            List<LXPoint> reverse_these = Lists.reverse(stripRev.getPoints()); //reflect 2nd strip
            Strip stripReversed = new Strip(metrics, reverse_these);  //create new strip with 2nd strips reflection
            strips.add(stripReversed);
            t.pop();

            // t.translate(100, 14, 0);
            // t.push();
            // Strip strip2 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip2);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip2Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these2 = Lists.reverse(strip2Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed2 = new Strip(metrics, reverse_these2);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed2);
            // t.pop();

            // t.translate(-95, 28, 0);
            // t.push();
            // Strip strip3 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip3);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip3Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these3 = Lists.reverse(strip3Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed3 = new Strip(metrics, reverse_these3);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed3);
            // t.pop();

            // t.translate(70, 14, 0);
            // t.push();
            // Strip strip4 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip4);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip4Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these4 = Lists.reverse(strip4Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed4 = new Strip(metrics, reverse_these4);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed4);
            // t.pop();

            // t.translate(-120, 14, 0);
            // t.push();
            // Strip strip5 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip5);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip5Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these5 = Lists.reverse(strip5Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed5 = new Strip(metrics, reverse_these5);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed5);
            // t.pop();

            // t.translate(139+85, 0, 0);
            // t.push();
            // Strip strip6 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip6);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip6Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these6 = Lists.reverse(strip6Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed6 = new Strip(metrics, reverse_these6);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed6);
            // t.pop();

            // t.translate(-128, 14, 0);
            // t.push();
            // Strip strip7 = new Strip("1", metrics, t);         //create the first strip
            // List<LXPoint> reverse_these7 = Lists.reverse(strip7.getPoints()); //reflect 2nd strip
            // Strip stripReversed7 = new Strip(metrics, reverse_these7);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed7);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip7Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // strips.add(strip7Rev);
            // t.pop();

            // t.translate(-80, 14, 0);
            // t.push();
            // Strip strip8 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip8);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip8Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these8 = Lists.reverse(strip8Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed8 = new Strip(metrics, reverse_these8);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed8);
            // t.pop();

            // t.translate(134, 32, 0);
            // t.push();
            // Strip strip9 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip9);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip9Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these9 = Lists.reverse(strip9Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed9 = new Strip(metrics, reverse_these9);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed9);
            // t.pop();

            // t.translate(-81, 14, 0);
            // t.push();
            // Strip strip10 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip10);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip10Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these10 = Lists.reverse(strip10Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed10 = new Strip(metrics, reverse_these10);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed10);
            // t.pop();






            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(0).getPoints()).addPoints(model.getStripByIndex(1).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("4").addPoints(model.getStripByIndex(2).getPoints()).addPoints(model.getStripByIndex(3).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("1").addPoints(model.getStripByIndex(4).getPoints()).addPoints(model.getStripByIndex(5).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("3").addPoints(model.getStripByIndex(6).getPoints()).addPoints(model.getStripByIndex(7).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("6").addPoints(model.getStripByIndex(8).getPoints()).addPoints(model.getStripByIndex(9).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("8").addPoints(model.getStripByIndex(10).getPoints()).addPoints(model.getStripByIndex(11).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("7").addPoints(model.getStripByIndex(12).getPoints()).addPoints(model.getStripByIndex(13).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("5").addPoints(model.getStripByIndex(14).getPoints()).addPoints(model.getStripByIndex(15).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("10").addPoints(model.getStripByIndex(16).getPoints()).addPoints(model.getStripByIndex(17).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("9").addPoints(model.getStripByIndex(18).getPoints()).addPoints(model.getStripByIndex(19).getPoints()));
//

//            addPixliteOutput(
//                new PointsGrouping("1").addPoints(model.getStripByIndex(0).getPoints()).addPoints(model.getStripByIndex(1).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("2").addPoints(model.getStripByIndex(2).getPoints()).addPoints(model.getStripByIndex(3).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("3").addPoints(model.getStripByIndex(4).getPoints()).addPoints(model.getStripByIndex(5).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("4").addPoints(model.getStripByIndex(6).getPoints()).addPoints(model.getStripByIndex(7).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("5").addPoints(model.getStripByIndex(8).getPoints()).addPoints(model.getStripByIndex(9).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("6").addPoints(model.getStripByIndex(10).getPoints()).addPoints(model.getStripByIndex(11).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("7").addPoints(model.getStripByIndex(12).getPoints()).addPoints(model.getStripByIndex(13).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("8").addPoints(model.getStripByIndex(14).getPoints()).addPoints(model.getStripByIndex(15).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("9").addPoints(model.getStripByIndex(16).getPoints()).addPoints(model.getStripByIndex(17).getPoints()));
//            addPixliteOutput(
//                new PointsGrouping("10").addPoints(model.getStripByIndex(18).getPoints()).addPoints(model.getStripByIndex(19).getPoints()));

        }

        @Override
        public SimplePixlite addPixliteOutput(PointsGrouping pointsGrouping) {
            try {
                SimplePixliteOutput spo = new SimplePixliteOutput(pointsGrouping);
                spo.setLogConnections(false);
                addChild(spo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }
    }



//    static class BarConfig {
//        final String id;
//        final float x;
//        final float y;
//        final float z;
//        final float rx;
//        final float ry;
//        final float rz;
//        final int channel;
//
//        BarConfig(String id, float[] coordinates, float[] rotations) {
//            this(id, coordinates, rotations, 0);
//        }
//
//        BarConfig(String id, float[] coordinates, float[] rotations, int channel) {
//            this.id = id;
//            this.x = coordinates[0];
//            this.y = coordinates[1];
//            this.z = coordinates[2];
//            this.rx = rotations[0];
//            this.ry = rotations[1];
//            this.rz = rotations[2];
//            this.channel = channel;
//        }
//    }



}
