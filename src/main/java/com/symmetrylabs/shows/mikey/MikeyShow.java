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
import com.symmetrylabs.slstudio.model.DoubleStrip;
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
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.2", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            int barAngle = -60;
            int spacing = -60;
            int barFlip = 180;
            int topSpacing = 80;
            int topHeight = 200;
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            // Strip.Metrics metrics = new Strip.Metrics(139, 1); //strip config
            DoubleStrip.Metrics metrics = new DoubleStrip.Metrics(139, -1, 2); // 1" front/back gap
            t.push();
            for (int i=0; i<8; i++) {
                if (i==4) {
                    t.translate(0, 0, 24); //TRANSLATE BACK BAR
                }
                t.push();
                if (i<4) {
                    t.rotateZ(barAngle);
                }
                else {
                    t.translate(0, topHeight, 0);
                }
                if (i==5) {
                    t.translate(topSpacing,0,0);    
                }
                t.translate(spacing,0,0);
                if (i>=6) {
                    t.translate(12, -100, 0);
                }
                if (i==7) {
                    t.translate(12,10,0);
                }
                // t.rotateX(-60);
                DoubleStrip strip = new DoubleStrip("1", metrics, t);         //create the first strip
                strips.add(strip); 
                // if (i==4) {
                //     t.translate(0, 0, -24); //TRANSLATE BACK BAR BACK
                // }                                                 //add the first strip to strip array
                t.pop();
                if (i<=3) {
                  t.translate(0, 12, 0);  
                }
                
                               
        }
            t.pop();
            t.scaleX(-1);
            t.push();
            for (int i=0; i<8; i++) {
                if (i==4) {
                    t.translate(0, 0, 24); //TRANSLATE BACK BAR
                }
                t.push();
                if (i<4) {
                    t.rotateZ(barAngle);
                }
                else {
                    t.translate(0, topHeight, 0);
                }
                if (i==5) {
                    t.translate(topSpacing,0,0);    
                }
                t.translate(spacing,0,0);
                if (i>=6) {
                    t.translate(12, -100, 0);
                }
                if (i==7) {
                    t.translate(12,10,0);
                }
                // t.rotateX(-60);
                DoubleStrip strip = new DoubleStrip("1", metrics, t);         //create the first strip
                strips.add(strip); 
                // if (i==4) {
                //     t.translate(0, 0, -24); //TRANSLATE BACK BAR BACK
                // }                                                 //add the first strip to strip array
                t.pop();
                if (i<=3) {
                  t.translate(0, 12, 0);  
                }
                
                
        }
            t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(60);
            // Strip strip2 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip2);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip2Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these2 = Lists.reverse(strip2Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed2 = new Strip(metrics, reverse_these2);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed2);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(60);
            // Strip strip3 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip3);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip3Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these3 = Lists.reverse(strip3Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed3 = new Strip(metrics, reverse_these3);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed3);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(60);
            // Strip strip4 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip4);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip4Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these4 = Lists.reverse(strip4Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed4 = new Strip(metrics, reverse_these4);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed4);
            // t.pop();

            // t.translate(160, -12*4, 0);
            // t.push();
            // t.rotateZ(-60);
            // Strip strip5 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip5);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip5Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these5 = Lists.reverse(strip5Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed5 = new Strip(metrics, reverse_these5);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed5);
            // t.pop();

            // t.translate(-12, 0, 0);
            // t.push();
            // t.rotateZ(-60);
            // Strip strip6 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip6);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip6Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these6 = Lists.reverse(strip6Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed6 = new Strip(metrics, reverse_these6);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed6);
            // t.pop();

            // t.translate(-12, 0, 0);
            // t.push();
            // t.rotateZ(-60);
            // Strip strip7 = new Strip("1", metrics, t);         //create the first strip
            // List<LXPoint> reverse_these7 = Lists.reverse(strip7.getPoints()); //reflect 2nd strip
            // Strip stripReversed7 = new Strip(metrics, reverse_these7);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed7);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip7Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // strips.add(strip7Rev);
            // t.pop();

            // t.translate(-12, 0, 0);
            // t.push();
            // t.rotateZ(-60);
            // Strip strip8 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip8);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip8Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these8 = Lists.reverse(strip8Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed8 = new Strip(metrics, reverse_these8);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed8);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip9 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip9);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip9Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these9 = Lists.reverse(strip9Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed9 = new Strip(metrics, reverse_these9);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed9);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip10 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip10);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip10Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these10 = Lists.reverse(strip10Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed10 = new Strip(metrics, reverse_these10);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed10);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip11 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip11);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip11Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these11 = Lists.reverse(strip11Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed11 = new Strip(metrics, reverse_these11);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed11);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip12 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip12);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip12Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these12 = Lists.reverse(strip12Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed12 = new Strip(metrics, reverse_these12);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed12);
            // t.pop();

            // //SIDE ROOM BARS
            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip13 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip13);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip13Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these13 = Lists.reverse(strip13Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed13 = new Strip(metrics, reverse_these13);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed13);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip14 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip14);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip14Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these14 = Lists.reverse(strip14Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed14 = new Strip(metrics, reverse_these14);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed14);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip15 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip15);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip15Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these15 = Lists.reverse(strip15Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed15 = new Strip(metrics, reverse_these15);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed15);
            // t.pop();

            // t.translate(12, 0, 0);
            // t.push();
            // t.rotateZ(-120);
            // Strip strip16 = new Strip("1", metrics, t);         //create the first strip
            // strips.add(strip16);                                                  //add the first strip to strip array
            // t.translate(0, 2, 0);                                   //translate to 2nd strip location
            // Strip strip16Rev = new Strip("2", metrics, t);                   //create the 2nd strip
            // List<LXPoint> reverse_these16 = Lists.reverse(strip16Rev.getPoints()); //reflect 2nd strip
            // Strip stripReversed16 = new Strip(metrics, reverse_these16);  //create new strip with 2nd strips reflection
            // strips.add(stripReversed16);
            // t.pop();




            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            // for (int i = startStrip; i <= endStrip; i++){
            //     addPixliteOutput(
            //     new PointsGrouping((i+1)+"").addPoints(model.getStripByIndex(i).getPoints()));
            // }

            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(3).getPoints()));
            addPixliteOutput(
                new PointsGrouping("5").addPoints(model.getStripByIndex(4).getPoints()));
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(5).getPoints()));
            addPixliteOutput(
                new PointsGrouping("7").addPoints(model.getStripByIndex(6).getPoints()));
            addPixliteOutput(
                new PointsGrouping("8").addPoints(model.getStripByIndex(7).getPoints()));
            addPixliteOutput(
                new PointsGrouping("9").addPoints(model.getStripByIndex(8).getPoints()));
            addPixliteOutput(
                new PointsGrouping("10").addPoints(model.getStripByIndex(9).getPoints()));
            addPixliteOutput(
                new PointsGrouping("11").addPoints(model.getStripByIndex(10).getPoints()));
            addPixliteOutput(
                new PointsGrouping("12").addPoints(model.getStripByIndex(11).getPoints()));
            addPixliteOutput(
                new PointsGrouping("13").addPoints(model.getStripByIndex(12).getPoints()));
            addPixliteOutput(
                new PointsGrouping("14").addPoints(model.getStripByIndex(13).getPoints()));
            addPixliteOutput(
                new PointsGrouping("15").addPoints(model.getStripByIndex(12).getPoints()));
            addPixliteOutput(
                new PointsGrouping("16").addPoints(model.getStripByIndex(13).getPoints()));

            // addPixliteOutput(
            //     new PointsGrouping("9").addPoints(model.getStripByIndex(8).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("2").addPoints(model.getStripByIndex(2).getPoints()).addPoints(model.getStripByIndex(3).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("3").addPoints(model.getStripByIndex(4).getPoints()).addPoints(model.getStripByIndex(5).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("4").addPoints(model.getStripByIndex(6).getPoints()).addPoints(model.getStripByIndex(7).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("5").addPoints(model.getStripByIndex(8).getPoints()).addPoints(model.getStripByIndex(9).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("6").addPoints(model.getStripByIndex(10).getPoints()).addPoints(model.getStripByIndex(11).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("7").addPoints(model.getStripByIndex(12).getPoints()).addPoints(model.getStripByIndex(13).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("8").addPoints(model.getStripByIndex(14).getPoints()).addPoints(model.getStripByIndex(15).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("9").addPoints(model.getStripByIndex(16).getPoints()).addPoints(model.getStripByIndex(17).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("10").addPoints(model.getStripByIndex(18).getPoints()).addPoints(model.getStripByIndex(19).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("11").addPoints(model.getStripByIndex(20).getPoints()).addPoints(model.getStripByIndex(21).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("12").addPoints(model.getStripByIndex(22).getPoints()).addPoints(model.getStripByIndex(23).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("13").addPoints(model.getStripByIndex(24).getPoints()).addPoints(model.getStripByIndex(25).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("14").addPoints(model.getStripByIndex(26).getPoints()).addPoints(model.getStripByIndex(27).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("15").addPoints(model.getStripByIndex(28).getPoints()).addPoints(model.getStripByIndex(29).getPoints()));
            // addPixliteOutput(
            //     new PointsGrouping("16").addPoints(model.getStripByIndex(30).getPoints()).addPoints(model.getStripByIndex(31).getPoints()));
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
}
