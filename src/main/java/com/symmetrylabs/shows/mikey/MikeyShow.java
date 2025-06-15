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
        MikeyPixlite pixlite1 = new MikeyPixlite(lx, "192.168.0.200", model, 0, java.util.Set.of());      // strips 0-31
        lx.addOutput(pixlite1);
        MikeyPixlite pixlite2 = new MikeyPixlite(lx, "192.168.0.201", model, 32, java.util.Set.of());      // strips 32-63
        lx.addOutput(pixlite2);
        MikeyPixlite pixlite3 = new MikeyPixlite(lx, "192.168.0.202", model, 64, java.util.Set.of());      // strips 64-95
        lx.addOutput(pixlite3); 
        MikeyPixlite pixlite4 = new MikeyPixlite(lx, "192.168.0.203", model, 96, java.util.Set.of(3)); // strips 96-127, skip port 3
        lx.addOutput(pixlite4); 
        MikeyPixlite pixlite5 = new MikeyPixlite(lx, "192.168.0.204", model, 128, java.util.Set.of());     // strips 128-159
        lx.addOutput(pixlite5); 
        MikeyPixlite pixlite6 = new MikeyPixlite(lx, "192.168.0.205", model, 160, java.util.Set.of());      // strips 160-191
        lx.addOutput(pixlite6); 
        MikeyPixlite pixlite7 = new MikeyPixlite(lx, "192.168.0.206", model, 192, java.util.Set.of());     // strips 192-223
        lx.addOutput(pixlite7); 
        MikeyPixlite pixlite8 = new MikeyPixlite(lx, "192.168.0.207", model, 224, java.util.Set.of());      // strips 224-255
        lx.addOutput(pixlite8); 
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
    // Map Pixlite index (1-based, as in setupLx) to set of unused ports
    // Example: Pixlite 4 skips port 3 (index 3)
    java.util.Map<Integer, java.util.Set<Integer>> unusedPixlitePorts = new java.util.HashMap<>();
    unusedPixlitePorts.put(4, java.util.Set.of(3)); // Pixlite 4 skips port 3
    // Add more as needed, e.g. unusedPixlitePorts.put(2, Set.of(2, 3));

    // We'll need to pass this map to both the model and the Pixlite outputs

            int barAngle = -60;
            int spacing = -60;
            int verticalBar = 22;
            int horizontalBar = 24;
            int cloudSpacing = 30;
            float rotateZ = -1.57f;
            float rotateZ90= 1.57f;
            float rotateZ180= -1.57f*2;
            int ft = 19;
            float tempSpacer = -5;
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            Strip.Metrics cloudStripMetrics128 = new Strip.Metrics(128, 1); //strip config long
            Strip.Metrics cloudStripMetrics22 = new Strip.Metrics(22, 1); //strip config side
            LXTransform globalTransform = new LXTransform();

            // Keep track of which strip groups belong to which Pixlite/port
            // Each Pixlite controls 8 groups, each group is 4 strips
            // For each group, only add if that Pixlite/port is not in the unused set
            int stripGroupIndex = 0;
            for (int pixliteIdx = 1; pixliteIdx <= 8; pixliteIdx++) {
                java.util.Set<Integer> unusedPorts = unusedPixlitePorts.getOrDefault(pixliteIdx, java.util.Set.of());
                for (int port = 0; port < 8; port++) {
                    if (unusedPorts.contains(port)) {
                        stripGroupIndex++;
                        continue; // Skip this group for the model
                    }
                    // You need to call your addFourStripGroup logic here with the correct parameters for each group.
                    // For now, to preserve your existing code, we'll just increment the index and let you map your actual addFourStripGroup calls as needed.
                    // Example:
                    // addFourStripGroup(strips, t, ...params...); // Use your actual parameters for each group
                    stripGroupIndex++;
                }
            }
            // Add 8 groups of addFourStripGroup labeled as 200-1 through 200-4 in the comments
            addFourStripGroup(strips,t,16.35f*ft, -13.7f*ft,0,rotateZ180); //200-1
            addFourStripGroup(strips,t,18.35f*ft, -13.7f*ft,0,rotateZ180); //200-2
            addFourStripGroup(strips,t,8.85f*ft, -20.7f*ft,0,rotateZ180); //200-3
            addFourStripGroup(strips,t,10.35f*ft, -20.7f*ft,0,rotateZ180); //200-4
            addFourStripGroup(strips,t,16.35f*ft, -20.7f*ft,0,rotateZ180); //200-5
            addFourStripGroup(strips,t,18.35f*ft, -20.7f*ft,0,rotateZ180); //200-6
            addFourStripGroup(strips,t,12.35f*ft, -20.7f*ft,0,rotateZ180); //200-7
            addFourStripGroup(strips,t,14.35f*ft, -20.7f*ft,0,rotateZ180); //200-8

            //Add 8 groups of addFourStripGroup labeled as 201-1 through 201-4 in the comments
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-1
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-2
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-3
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-4
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-5
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-6
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-7
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //201-8    

            //Add 8 groups of addFourStripGroup labeled as 202-1 through 202-4 in the comments
            addFourStripGroup(strips, t, 11.5f*ft, -7*ft, 0, 0); //202-1
            addFourStripGroup(strips, t, 10f*ft, -7f*ft, 0, 0); //202-2
            addFourStripGroup(strips, t, 7.5f*ft, -3.6f*ft, 0, rotateZ); //202-3
            addFourStripGroup(strips, t, 7.5f*ft, -1.6f*ft, 0, rotateZ); //202-4
            addFourStripGroup(strips,t,15.5f*ft,0*ft,0,0); //202-5
            addFourStripGroup(strips,t,13.5f*ft,0*ft,0,0); //202-6
            addFourStripGroup(strips, t, 11.5f*ft, 0*ft, 0, 0); //202-7
            addFourStripGroup(strips, t, 10*ft, 0*ft, 0, 0); //202-8


            //Add 8 groups of addFourStripGroup labeled as 203-1 through 203-4 in the comments
            addFourStripGroup(strips,t,15.5f*ft,-7*ft,0,0); //203-1
            addFourStripGroup(strips,t,13.5f*ft,-7*ft,0,0); //203-2
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //203-3
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //203-4
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //203-5
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //203-6
            addFourStripGroup(strips,t,19.5f*ft,0*ft,0,0); //203-7
            addFourStripGroup(strips,t,17.5f*ft,0*ft,0,0); //203-8

            //Add 8 groups of addFourStripGroup labeled as 204-1 through 204-4 in the comments  
            addFourStripGroup(strips,t,5.85f*ft,-20.7f*ft,0,rotateZ180); //204-1    
            addFourStripGroup(strips,t,7.35f*ft,-20.7f*ft,0,rotateZ180); //204-2
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //204-3
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //204-4
            addFiveStripGroup(strips, t, 5.75f*ft, -18.7f*ft, 0, rotateZ180); //204-5
            addFiveStripGroup(strips, t, 4.1f*ft, -18.7f*ft, 0, rotateZ180); //204-6
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //204-7
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //204-8

            //205
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //205-1
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //205-2
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //205-3
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //205-4
            addThreeStripGroup(strips, t, -3.2f*ft, -4*ft, 0, 0); //205-5
            addThreeStripGroup(strips, t, -1.2f*ft, -4*ft, 0, 0); //205-6
            addFourStripGroup(strips,t,.5f*ft,-4*ft,0,rotateZ90); //205-7
            addFourStripGroup(strips,t,.5f*ft,-5.6f*ft,0,rotateZ90); //205-8

            //206
            //add addFourStripGroup for 206
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-1
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-2
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-3
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-4
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-5
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-6
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-7
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-8

            //207
            addFourStripGroup(strips,t,7.35f*ft,-13.7f*ft,0,rotateZ180); //207-1
            addFourStripGroup(strips,t,5.85f*ft,-13.7f*ft,0,rotateZ180); //207-2
            addFourStripGroup(strips,t, 4.35f*ft,-13.7f*ft,0,rotateZ180); //207-3
            addFourStripGroup(strips,t, 2.85f*ft,-13.7f*ft,0,rotateZ180); //207-4
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-5
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-6
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-7
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-8

            //208
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-1
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-2
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-3
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-4
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-5
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-6
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-7
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //208-8
            return new MikeyModel(strips);
        }

        private static void addThreeStripGroup(List<Strip> strips, LXTransform t, float x, float y, float z, float rotateZ) {
            Strip.Metrics cloudStripMetrics90 = new Strip.Metrics(90, 1);
            Strip.Metrics cloudStripMetrics15 = new Strip.Metrics(15, 1);
            t.push();
                t.translate(x, y, z);
                t.rotateZ(rotateZ);
                t.push();
                    t.rotateZ(-1.57); //start going down
                    Strip strip1 = new Strip(strips.size() + "/1", cloudStripMetrics90, t);     
                    strips.add(strip1);
                t.pop();
                t.push();
                    t.translate(0,-90,0); //translate down
                    Strip strip2 = new Strip(strips.size() + "/2", cloudStripMetrics15, t);
                    strips.add(strip2);
                t.pop();
                t.push();
                    t.translate(0,-90,0); //translate down
                    t.translate(15,0,0); //translate right
                    t.rotateZ(1.57);
                    Strip strip3 = new Strip(strips.size() + "/3", cloudStripMetrics90, t);     
                    strips.add(strip3);
                t.pop();
            t.pop();
        }

        private static void addFourStripGroup(List<Strip> strips, LXTransform t, float x, float y, float z, float rotateZ) {
            Strip.Metrics cloudStripMetrics128 = new Strip.Metrics(128, 1);
            Strip.Metrics cloudStripMetrics22 = new Strip.Metrics(22, 1);
            t.push();
                // t.rotateZ(-1.57f*3);
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
                        t.rotateZ(1.57*-2); //rotate right
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
        private static void addFiveStripGroup(List<Strip> strips, LXTransform t, float x, float y, float z, float rotateZ) {
            //add a strip metric with 90 and 15 points
            Strip.Metrics cloudStripMetrics90 = new Strip.Metrics(90, 1);
            Strip.Metrics cloudStripMetrics15 = new Strip.Metrics(15, 1);

            //add a group of strips with 5 strips that goes down 90 points then rotates left -1.57 
            // goes back up rotates left goes 15 points goes up then rotates left -1.57 goes back up 
            // then rotates right then 15 points then rotates left and then back down 90 points
            t.push();
                t.translate(x, y, z);
                t.rotateZ(rotateZ);
                t.push();
                    t.rotateZ(-1.57); //start going down
                    Strip strip1 = new Strip(strips.size() + "/1", cloudStripMetrics90, t);     
                    strips.add(strip1);
                t.pop();
                t.push();
                    t.translate(0,-90,0); //translate down
                    Strip strip2 = new Strip(strips.size() + "/2", cloudStripMetrics15, t);
                    strips.add(strip2);
                t.pop();
                t.push();
                    t.translate(0,-90,0); //translate down
                    t.translate(15,0,0); //translate right
                    t.rotateZ(1.57); //rotate left
                    Strip strip3 = new Strip(strips.size() + "/3", cloudStripMetrics90, t);     
                    strips.add(strip3);
                t.pop();
                t.push();
                    t.translate(0,-90,0); //translate down
                    t.translate(15,0,0); //translate left
                    t.translate(0,90,0); //translate up
                    Strip strip4 = new Strip(strips.size() + "/4", cloudStripMetrics15, t);     
                    strips.add(strip4);
                t.pop();
                t.push();
                    t.translate(0,-90,0); //translate down
                    t.translate(15,0,0); //translate right
                    t.translate(0,90,0); //translate up
                    t.translate(15,0,0); //translate right
                    t.rotateZ(-1.57);
                    Strip strip5 = new Strip(strips.size() + "/5", cloudStripMetrics90, t);     
                    strips.add(strip5);
                t.pop();
            t.pop();


        }
    }

    static class MikeyPixlite extends SimplePixlite {
        // Add unusedPorts parameter to constructor
        public MikeyPixlite(LX lx, String ip, MikeyModel model, int stripOffset, java.util.Set<Integer> unusedPorts) {
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

            int usedPort = 0;
            for (int i = 0; i < numIterations; i++) {
                if (unusedPorts.contains(i)) {
                    continue; // skip this port/output
                }
                int baseIndex = (i * 4) + stripOffset;
                if (baseIndex + 3 >= totalStripsInModel) {
                    System.out.println("MikeyPixlite: Attempted to access strip out of bounds, skipping output group. BaseIndex: " + baseIndex + ", TotalStrips: " + totalStripsInModel);
                    continue;
                }
                addPixliteOutput(
                    new PointsGrouping(String.valueOf(usedPort + 1)) // Only increment for used ports
                        .addPoints(model.getStripByIndex(baseIndex).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 1).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 2).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 3).getPoints())
                );
                usedPort++;
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
