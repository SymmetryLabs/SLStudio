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
        MikeyModel model = (MikeyModel) lx.model;
        //add a total of 12 controllers
        MikeyPixlite pixlite1 = new MikeyPixlite(lx, "192.168.0.192", model, 0);      // strips 0-7
        lx.addOutput(pixlite1);
        MikeyPixlite pixlite2 = new MikeyPixlite(lx, "192.168.0.193", model, 8);      // strips 8-15
        lx.addOutput(pixlite2);
        MikeyPixlite pixlite3 = new MikeyPixlite(lx, "192.168.0.194", model, 16);      // strips 16-23
        lx.addOutput(pixlite3); 
        MikeyPixlite pixlite4 = new MikeyPixlite(lx, "192.168.0.195", model, 24);      // strips 24-31
        lx.addOutput(pixlite4); 
        MikeyPixlite pixlite5 = new MikeyPixlite(lx, "192.168.0.196", model, 32);      // strips 32-39
        lx.addOutput(pixlite5); 
        MikeyPixlite pixlite6 = new MikeyPixlite(lx, "192.168.0.197", model, 40);      // strips 40-47
        lx.addOutput(pixlite6); 
        MikeyPixlite pixlite7 = new MikeyPixlite(lx, "192.168.0.198", model, 48);      // strips 48-55
        lx.addOutput(pixlite7); 
        MikeyPixlite pixlite8 = new MikeyPixlite(lx, "192.168.0.199", model, 56);      // strips 56-63
        lx.addOutput(pixlite8); 
        MikeyPixlite pixlite9 = new MikeyPixlite(lx, "192.168.0.200", model, 64);      // strips 64-71
        lx.addOutput(pixlite9); 
        MikeyPixlite pixlite10 = new MikeyPixlite(lx, "192.168.0.201", model, 72);      // strips 72-79
        lx.addOutput(pixlite10); 
        MikeyPixlite pixlite11 = new MikeyPixlite(lx, "192.168.0.202", model, 80);      // strips 80-87
        lx.addOutput(pixlite11); 
        MikeyPixlite pixlite12 = new MikeyPixlite(lx, "192.168.0.203", model, 88);      // strips 88-95
        lx.addOutput(pixlite12); 
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }
        
        /**
         * Adds a group of 4 strips in a rectangular pattern
         * @param strips The list to add strips to
         * @param t The transform to use for positioning
         * @param xSpacing X translation
         * @param ySpacing Y translation
         * @param zSpacing Z translation
         * @param rotation Initial Z rotation
         */

        public static MikeyModel create() {
            int barAngle = -60;
            int spacing = -60;
            int verticalBar = 22;
            int horizontalBar = 24;
            int cloudSpacing = 30;
            float rotateZ = -1.57f;
            int tempSpacer = -256;
            int ft = 24;
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            Strip.Metrics cloudStripMetrics128 = new Strip.Metrics(128, 1); //strip config long
            Strip.Metrics cloudStripMetrics22 = new Strip.Metrics(22, 1); //strip config side



            //Add 8 groups of addFourStripGroup labeled as 200-1 through 200-4 in the comments
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-1
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-5
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-6
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-7
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //200-8

            //Add 8 groups of addFourStripGroup labeled as 201-1 through 201-4 in the comments
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-1
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-5
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-6
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-7
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //201-8    

            //Add 8 groups of addFourStripGroup labeled as 202-1 through 202-4 in the comments
            addFourStripGroup(strips, t, 10*ft, 7*ft, 0, 0); //202-7
            addFourStripGroup(strips, t, 8*ft, 7*ft, 0, 0); //202-8
            addFourStripGroup(strips, t, 7*ft, 4*ft, 0, rotateZ); //202-3
            addFourStripGroup(strips, t, 7*ft, 2*ft, 0, rotateZ); //202-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //202-5
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //202-6
            addFourStripGroup(strips, t, 10*ft, 0, 0, 0); //202-7
            addFourStripGroup(strips, t, 8*ft, 0, 0, 0); //202-8


            //Add 8 groups of addFourStripGroup labeled as 203-1 through 203-4 in the comments
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-1
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-5
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-6
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-7
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //203-8

            //Add 8 groups of addFourStripGroup labeled as 204-1 through 204-4 in the comments  
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-1    
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-5
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-6
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-7
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //204-8

            //205
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //205-1
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //205-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //205-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //205-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //205-5

            //206
            //add addFourStripGroup for 206
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //206-1
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //206-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //206-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //206-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //206-5

            //207
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //207-1
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //207-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //207-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //207-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //207-5

            //208
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //208-1
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //208-2
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //208-3
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //208-4
            addFourStripGroup(strips,t,tempSpacer,0,0,0); //208-5

            addFourStripGroup(strips, t, 7*ft, 4*ft, 0, rotateZ);
            addFourStripGroup(strips, t, 7*ft, 2*ft, 0, rotateZ);

            // //Add two more groups with a x of 10 and y of 0 and a x of 8 and y of 0
            addFourStripGroup(strips, t, 10*ft, 0, 0, 0);
            addFourStripGroup(strips, t, 8*ft, 0, 0, 0);
            // //add two more with a x of 10 and y of 7 and x of 8 and y of 7
            // addFourStripGroup(strips, t, 10*ft, 7*ft, 0, 0);
            // addFourStripGroup(strips, t, 8*ft, 7*ft, 0, 0);

            return new MikeyModel(strips);
        }

        private static void addFourStripGroup(List<Strip> strips, LXTransform t, float x, float y, float z, float rotateZ) {
            Strip.Metrics cloudStripMetrics128 = new Strip.Metrics(128, 1);
            Strip.Metrics cloudStripMetrics22 = new Strip.Metrics(22, 1);
            t.push();
                t.translate(x, y, z);
                t.rotateZ(rotateZ);
                t.push();
                            t.rotateZ(-1.57); //start going down
                            Strip strip1 = new Strip(strips.size() + "/1", cloudStripMetrics128, t);     
                            strips.add(strip1);
                            // System.out.println("Creating strip at t.x()=" + t.x() + " t.y()=" + t.y() + " t.z()=" + t.z());
                        t.pop();
                        t.push();
                            t.translate(0,-128,0); //translate down
                            t.rotateZ(1.57*-2);
                            Strip strip2 = new Strip(strips.size() + "/2", cloudStripMetrics22, t);
                            strips.add(strip2);
                            // System.out.println("Creating strip at t.x()=" + t.x() + " t.y()=" + t.y() + " t.z()=" + t.z());
                        t.pop();
                        t.push();
                            t.translate(0,-128,0); //translate down
                            t.translate(-22,0,0); //translate left
                            t.rotateZ(-1.57*-5);
                            Strip strip3 = new Strip(strips.size() + "/3", cloudStripMetrics128, t);     
                            strips.add(strip3);
                            // System.out.println("Creating strip at t.x()=" + t.x() + " t.y()=" + t.y() + " t.z()=" + t.z());
                        t.pop();
                        t.push();
                            t.translate(0,-128,0); //translate down
                            t.translate(-22,0,0); //translate left
                            t.translate(0,128,0); //translate up
                            t.rotateZ(-1.57*-4);
                            Strip strip4 = new Strip(strips.size() + "/4", cloudStripMetrics22, t);     
                            strips.add(strip4);
                            // System.out.println("Creating strip at t.x()=" + t.x() + " t.y()=" + t.y() + " t.z()=" + t.z());
                        t.pop();
            t.pop();
        }
    }

    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model, int stripOffset) {
            super(lx, ip);

            int totalStripsInModel = model.getStrips().size();
            // Ensure stripOffset is within bounds to avoid negative availableStrips
            if (stripOffset < 0) stripOffset = 0; // Or handle error
            if (stripOffset >= totalStripsInModel) {
                // No strips to process if offset is beyond or at the total number of strips
                return; 
            }

            int availableStripsForPixlite = totalStripsInModel - stripOffset;
            int numPossibleGroups = availableStripsForPixlite / 4; // Integer division gives full groups
            
            // Each Pixlite is designed for up to 8 outputs (groups of 4 strips)
            int numIterations = Math.min(8, numPossibleGroups);

            for (int i = 0; i < numIterations; i++) {
                // Calculate base strip index for this output, stepping by 4 each time
                int baseIndex = (i * 4) + stripOffset;
                
                // Defensive check, though numIterations should prevent this
                if (baseIndex + 3 >= totalStripsInModel) {
                    System.out.println("MikeyPixlite: Attempted to access strip out of bounds, skipping output group. BaseIndex: " + baseIndex + ", TotalStrips: " + totalStripsInModel);
                    continue;
                }

                addPixliteOutput(
                    new PointsGrouping(String.valueOf(i + 1))
                        .addPoints(model.getStripByIndex(baseIndex).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 1).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 2).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 3).getPoints())
                        );
            }
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
