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
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.0.0.42", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            int barAngle = -60;
            int spacing = -60;
            int verticalBar = 22;
            int horizontalBar = 24;
            int pillarSpacingArch1 = -81;
            int pillarSpacingArch2 = -73;
            int pillarspacingArch3 = -74;
            int arch1Leds = 92;
            int arch2Leds = 69;
            int arch3Leds = 67;
            int arch4Leds = 86;
            int arch5Leds = 69;
            int arch6Leds = 69;
            int pillarLeds37 = 37;
            int pillarLeds36 = 36;
            int archLedsDj = horizontalBar*3;

            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            Strip.Metrics pillarMetrics37 = new Strip.Metrics(38, 1); //strip config
            Strip.Metrics pillarMetrics36 = new Strip.Metrics(37, 1); //strip config

            Strip.Metrics arch1Metrics = new Strip.Metrics(92, 1); //strip config
            Strip.Metrics arch2Metrics = new Strip.Metrics(69, 1); //strip config
            Strip.Metrics arch3Metrics = new Strip.Metrics(67, 1); //strip config
            Strip.Metrics arch4Metrics = new Strip.Metrics(86, 1); //strip config
            Strip.Metrics arch5Metrics = new Strip.Metrics(69, 1); //strip config
            Strip.Metrics arch6Metrics = new Strip.Metrics(69, 1); //strip config
            Strip.Metrics archDjMetrics = new Strip.Metrics(archLedsDj, .75f); //strip config

            //PILLAR 1
            t.push();
            //Rotate so strip is facing down
            t.rotateZ(-1.571f); 
            Strip strip1 = new Strip("1", pillarMetrics37, t);         //create the first strip
            strips.add(strip1);  
            t.pop();
            
            //ARCH 1
            t.push();
            //rotate so strip is facing towards FOH
            t.rotateY(1.571f);
            Strip strip2 = new Strip("1", arch1Metrics, t);         //create the turn in the first strip
            strips.add(strip2);

            //PILLAR 2
            t.translate(arch1Leds, 0, 0);
            //undo strip is facing toward foh rotation
            t.rotateY(-1.571f);
            //rotate strip down
            t.rotateZ(-1.571f);
            Strip strip3 = new Strip("1", pillarMetrics37, t);         //create the first strip
            strips.add(strip3);  
            
            //ARCH 2
            //undo rotate strip down
            t.rotateZ(1.571f);
            //rotate strip towards foh            
            t.rotateY(1.571f);
            Strip strip4 = new Strip("1", arch2Metrics, t);         //create the turn in the first strip
            strips.add(strip4);

            //PILLAR 3
            t.translate(arch2Leds, 0, 0);
            //undo strip is facing toward foh rotation
            t.rotateY(-1.571f);
            //rotate strip down
            t.rotateZ(-1.571f);
            Strip strip5 = new Strip("1", pillarMetrics36, t);         //create the first strip
            strips.add(strip5);

            //ARCH 3
            //undo rotate strip down
            t.rotateZ(1.571f);
            //rotate strip towards foh            
            t.rotateY(1.571f);
            Strip strip6 = new Strip("1", arch2Metrics, t);         //create the turn in the first strip
            strips.add(strip6);

            //PILLAR 4
            t.translate(arch3Leds, 0, 0);
            //undo strip is facing toward foh rotation
            t.rotateY(-1.571f);
            //rotate strip down
            t.rotateZ(-1.571f);
            Strip strip7 = new Strip("1", pillarMetrics36, t);         //create the first strip
            strips.add(strip7);
            t.pop();

            //RIGHT SIDE TRANSLATED FROM LEFT
            t.translate(archLedsDj, 0, 0);

            //PILLAR 5
            t.push();
            //Rotate so strip is facing down
            t.rotateZ(-1.571f); 
            Strip strip8 = new Strip("1", pillarMetrics37, t);         //create the first strip
            strips.add(strip8);  
            t.pop();
            
            //ARCH 4
            t.push();
            //rotate so strip is facing towards FOH
            t.rotateY(1.571f);
            Strip strip9 = new Strip("1", arch1Metrics, t);         //create the turn in the first strip
            strips.add(strip9);

            //PILLAR 6
            t.translate(arch1Leds, 0, 0);
            //undo strip is facing toward foh rotation
            t.rotateY(-1.571f);
            //rotate strip down
            t.rotateZ(-1.571f);
            Strip strip10 = new Strip("1", pillarMetrics37, t);         //create the first strip
            strips.add(strip10);  
            
            //ARCH 5
            //undo rotate strip down
            t.rotateZ(1.571f);
            //rotate strip towards foh            
            t.rotateY(1.571f);
            Strip strip11 = new Strip("1", arch2Metrics, t);         //create the turn in the first strip
            strips.add(strip11);

            //PILLAR 7
            t.translate(arch2Leds, 0, 0);
            //undo strip is facing toward foh rotation
            t.rotateY(-1.571f);
            //rotate strip down
            t.rotateZ(-1.571f);
            Strip strip12 = new Strip("1", pillarMetrics37, t);         //create the first strip
            strips.add(strip12);

            //ARCH 6
            //undo rotate strip down
            t.rotateZ(1.571f);
            //rotate strip towards foh            
            t.rotateY(1.571f);
            Strip strip13 = new Strip("1", arch2Metrics, t);         //create the turn in the first strip
            strips.add(strip13);

            //PILLAR 8
            t.translate(arch3Leds, 0, 0);
            //undo strip is facing toward foh rotation
            t.rotateY(-1.571f);
            //rotate strip down
            t.rotateZ(-1.571f);
            Strip strip14 = new Strip("1", pillarMetrics37, t);         //create the first strip
            strips.add(strip14);
            t.pop();

            // t.pop();
            //ARCH LEDS ABOVE DJ
            t.rotateY(-.314);
            Strip strip15 = new Strip("1", archDjMetrics, t);         //create the first strip
            strips.add(strip15);



        

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
            //PILLAR 1
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(0).getPoints()));
            //ARCH 1
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(1).getPoints()));
            //PILLAR 2
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(2).getPoints()));
            //ARCH 2
            addPixliteOutput(
                new PointsGrouping("8").addPoints(model.getStripByIndex(3).getPoints()));
            //PILLAR 3
            addPixliteOutput(
                new PointsGrouping("5").addPoints(model.getStripByIndex(4).getPoints()));
            //ARCH 3
            addPixliteOutput(
                new PointsGrouping("7").addPoints(model.getStripByIndex(5).getPoints()));
            //PILLAR 4
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(6).getPoints()));
            //PILLAR 5
            addPixliteOutput(
                new PointsGrouping("16").addPoints(model.getStripByIndex(7).getPoints()));
            //ARCH 4
            addPixliteOutput(
                new PointsGrouping("15").addPoints(model.getStripByIndex(8).getPoints()));
            //PILLAR 6
            addPixliteOutput(
                new PointsGrouping("14").addPoints(model.getStripByIndex(9).getPoints()));
            //ARCH 5
            addPixliteOutput(
                new PointsGrouping("12").addPoints(model.getStripByIndex(10).getPoints()));
            //PILLAR 7
            addPixliteOutput(
                new PointsGrouping("9").addPoints(model.getStripByIndex(11).getPoints()));
            //ARCH 6
            addPixliteOutput(
                new PointsGrouping("11").addPoints(model.getStripByIndex(12).getPoints()));
            //PILLAR 8
            addPixliteOutput(
                new PointsGrouping("10").addPoints(model.getStripByIndex(13).getPoints()));
            //ARCH OVER DJ
            addPixliteOutput(
                new PointsGrouping("13").addPoints(model.getStripByIndex(14).getPoints()));

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
