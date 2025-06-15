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
        //add a total of 6 controllers
        //I'm doing a hacky thing here where I'm offsetting the strips by the number of strips 
        //per controller but some controllers have more strips than others because some clouds
        //are longer than others and are created with more strips. So each controller is offset 
        //by the number of strips in the previous controller. This is a hack and I'm lazy

        MikeyPixlite pixlite1 = new MikeyPixlite(lx, "192.168.0.200", model, 0, java.util.Set.of());      // strips 0-31
        lx.addOutput(pixlite1);
        MikeyPixlite pixlite2 = new MikeyPixlite(lx, "192.168.0.202", model, 32, java.util.Set.of());  
        lx.addOutput(pixlite2);
        MikeyPixlite pixlite3 = new MikeyPixlite(lx, "192.168.0.203", model, 64, java.util.Set.of());      //64-95
        lx.addOutput(pixlite3); 
        MikeyPixlite pixlite4 = new MikeyPixlite(lx, "192.168.0.204", model, 97, java.util.Set.of());   //96-127
        lx.addOutput(pixlite4); 
        MikeyPixlite pixlite5 = new MikeyPixlite(lx, "192.168.0.205", model, 133, java.util.Set.of());     //129-160
        lx.addOutput(pixlite5); 
        MikeyPixlite pixlite6 = new MikeyPixlite(lx, "192.168.0.207", model, 163, java.util.Set.of());      //161-192
        lx.addOutput(pixlite6);
        // lx.addOutput(pixlite2);
        // MikeyPixlite pixlite3 = new MikeyPixlite(lx, "192.168.0.203", model, 64, java.util.Set.of(2,3,4,5));      //64-95
        // lx.addOutput(pixlite3); 
        // MikeyPixlite pixlite4 = new MikeyPixlite(lx, "192.168.0.204", model, 96, java.util.Set.of(2,3,6,7)); //96-127, skip port 3
        // lx.addOutput(pixlite4); 
        // MikeyPixlite pixlite5 = new MikeyPixlite(lx, "192.168.0.205", model, 128, java.util.Set.of(0,1,2,3));     //128-159
        // lx.addOutput(pixlite5); 
        // MikeyPixlite pixlite6 = new MikeyPixlite(lx, "192.168.0.207", model, 160, java.util.Set.of(0,1,2,3));      //160-191
        // lx.addOutput(pixlite6);
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

            //1-31
            addFourStripGroup(strips,t,16.35f*ft, -13.7f*ft,0,rotateZ180); //200-1
            addFourStripGroup(strips,t,18.35f*ft, -13.7f*ft,0,rotateZ180); //200-2
            addFourStripGroup(strips,t,8.85f*ft, -20.7f*ft,0,rotateZ180); //200-3
            addFourStripGroup(strips,t,10.35f*ft, -20.7f*ft,0,rotateZ180); //200-4
            addFourStripGroup(strips,t,16.35f*ft, -20.7f*ft,0,rotateZ180); //200-5
            addFourStripGroup(strips,t,18.35f*ft, -20.7f*ft,0,rotateZ180); //200-6
            addFourStripGroup(strips,t,12.35f*ft, -20.7f*ft,0,rotateZ180); //200-7
            addFourStripGroup(strips,t,14.35f*ft, -20.7f*ft,0,rotateZ180); //200-8

            //Add 8 groups of addFourStripGroup labeled as 202-1 through 202-4 in the comments
            //32-63
            addFourStripGroup(strips, t, 11.5f*ft, -7*ft, 0, 0); //202-1
            addFourStripGroup(strips, t, 10f*ft, -7f*ft, 0, 0); //202-2
            addFourStripGroup(strips, t, 7.5f*ft, -3.6f*ft, 0, rotateZ); //202-3
            addFourStripGroup(strips, t, 7.5f*ft, -1.6f*ft, 0, rotateZ); //202-4
            addFourStripGroup(strips,t,15.5f*ft,0*ft,0,0); //202-5
            addFourStripGroup(strips,t,13.5f*ft,0*ft,0,0); //202-6
            addFourStripGroup(strips, t, 11.5f*ft, 0*ft, 0, 0); //202-7
            addFourStripGroup(strips, t, 10*ft, 0*ft, 0, 0); //202-8

            //64-95 
            //FIVE STRIP GROUP 3-4
            //Add 8 groups of addFourStripGroup labeled as 203-1 through 203-4 in the comments
            addFourStripGroup(strips,t,15.5f*ft,-7*ft,0,0); //203-1 //64-68
            addFourStripGroup(strips,t,13.5f*ft,-7*ft,0,0); //203-2 //69-72
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //203-3 //73-76
            addFiveStripGroup(strips,t,19.5f*ft, 0*ft, 0,-1.57f,0,0); //203-4 //77-81
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //203-5 //82-85
            addFourStripGroup(strips,t,tempSpacer*ft,0*ft,0,0); //203-6 //86-89
            addFourStripGroup(strips,t,19.5f*ft,0*ft,0,0); //203-7 //90-93
            addFourStripGroup(strips,t,17.5f*ft,0*ft,0,0); //203-8 //94-97

            //96-131
            //FIVE STRIPS 4-5, 4-6
            //start at 102 in the comments and incriment the same


            addFourStripGroup(strips,t,5.85f*ft,-20.7f*ft,0,rotateZ180); //204-1  //98-101   
            addFourStripGroup(strips,t,7.35f*ft,-20.7f*ft,0,rotateZ180); //204-2 //102-105
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //204-3 //106-109
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //204-4 //110-113
            addFiveStripGroup(strips, t, 5f*ft, -18.7f*ft, 0, 0,0,rotateZ180); //204-5 //114-118 //five strip cloud
            addFiveStripGroup(strips, t, 3.2f*ft, -18.7f*ft, 0, 0,0,rotateZ180); //204-6 //119-123 //five strip cloud
            addFiveStripGroup(strips,t, 1.5f*ft, -17.2f*ft,0,0,0,rotateZ180); //204-7 //124-128 //five strip cloud
            addFiveStripGroup(strips,t, -.85f*ft, -17.2f*ft,0,0,0,rotateZ180); //204-8 //129-133 //five strip cloud

            //Three Strip Group 5-5, 5-6
            //205
            //132-163
            addFourStriptoOneStripGroup(strips,t,.5f*ft,.5f*ft,0,0); //205-1 //134-136
            addFourStriptoOneStripGroup(strips,t,.5f*ft,1*ft,0,0); //205-2 //137-140
            addFourStriptoOneStripGroup(strips,t,.5f*ft,1.5f*ft,0,0); //205-3 //141-144
            addFourStriptoOneStripGroup(strips,t,.5f*ft,2*ft,0,0); //205-4 //145-148
            addThreeStripGroup(strips, t, -.7f*ft, -4*ft, 0, 0); //205-5 //149-151
            addThreeStripGroup(strips, t, 1.3f*ft, -4*ft, 0, 0); //205-6 //152-155
            addFourStripGroup(strips,t,.5f*ft,-4*ft,0,rotateZ90); //205-7 //156-159
            addFourStripGroup(strips,t,.5f*ft,-5.6f*ft,0,rotateZ90); //205-8 //160-163

            //206
            //add addFourStripGroup for 206
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-1
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-2
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-3
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-4
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-5
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-6
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-7
            // addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //206-8

            //207
            //162-193
            addFourStripGroup(strips,t,7.35f*ft,-13.7f*ft,0,rotateZ180); //207-1 //164-168
            addFourStripGroup(strips,t,5.85f*ft,-13.7f*ft,0,rotateZ180); //207-2 //169-172
            addFourStripGroup(strips,t, 2.35f*ft,-13.7f*ft,0,rotateZ180); //207-3 //173-176
            addFourStripGroup(strips,t, 3.85f*ft,-13.7f*ft,0,rotateZ180); //207-4 //177-180
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-5 //181-184
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-6 //185-188
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-7 //189-192
            addFourStripGroup(strips,t,tempSpacer*ft,0,0,0); //207-8 //193-196
            return new MikeyModel(strips);
        }

        //Create a strip group thats 4 strips that starts going left and then rotates in the x axis
        private static void addFourStriptoOneStripGroup(List<Strip> strips, LXTransform t, float x, float y, float z, float rotateZ) {
            Strip.Metrics cloudStripMetrics100 = new Strip.Metrics(100, 1);
            Strip.Metrics cloudStripMetrics50 = new Strip.Metrics(50, 1);
            t.push();
                t.translate(x, y, z);
                t.rotateZ(rotateZ);
                t.push();
                    t.translate(0,0,100); //translate 100 into the Z
                    t.rotateY(1.57*1); //start going Up From Z (rotate on X axis)
                    Strip strip1 = new Strip(strips.size() + "/1", cloudStripMetrics50, t);     
                    strips.add(strip1);
                t.pop();
                t.push();
                    t.translate(0,0,50); //translate down 50 into the Z
                    t.rotateY(1.57*1); //start going Up From Z (rotate on X axis)
                    Strip strip2 = new Strip(strips.size() + "/2", cloudStripMetrics50, t);
                    strips.add(strip2);
                t.pop();
                t.push();
                    Strip strip3 = new Strip(strips.size() + "/3", cloudStripMetrics100, t);     
                    strips.add(strip3);
                t.pop();
                t.push();
                    t.translate(100,0,0); //translate down
                    Strip strip4 = new Strip(strips.size() + "/4", cloudStripMetrics100, t);     
                    strips.add(strip4);
                t.pop();
            t.pop();
        }
            

        private static void addThreeStripGroup(List<Strip> strips, LXTransform t, float x, float y, float z, float rotateZ) {
            Strip.Metrics cloudStripMetrics140 = new Strip.Metrics(140, 1);
            Strip.Metrics cloudStripMetrics22 = new Strip.Metrics(22, 1);
            t.push();
                t.translate(x, y, z);
                t.rotateZ(rotateZ);
                t.push();
                    t.rotateZ(-1.57); //start going down
                    Strip strip1 = new Strip(strips.size() + "/1", cloudStripMetrics140, t);     
                    strips.add(strip1);
                t.pop();
                t.push();
                    t.translate(0,-140,0); //translate down
                    Strip strip2 = new Strip(strips.size() + "/2", cloudStripMetrics22, t);
                    strips.add(strip2);
                t.pop();
                t.push();
                    t.translate(0,-140,0); //translate down
                    t.translate(22,0,0); //translate right
                    t.rotateZ(1.57);
                    Strip strip3 = new Strip(strips.size() + "/3", cloudStripMetrics140, t);     
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
        private static void addFiveStripGroup(List<Strip> strips, LXTransform t, float x, float y, float z, float rotateX, float rotateY, float rotateZ) {
            //add a strip metric with 90 and 15 points
            Strip.Metrics cloudStripMetrics90 = new Strip.Metrics(90, 1);
            Strip.Metrics cloudStripMetrics15 = new Strip.Metrics(15, 1);

            //add a group of strips with 5 strips that goes down 90 points then rotates left -1.57 
            // goes back up rotates left goes 15 points goes up then rotates left -1.57 goes back up 
            // then rotates right then 15 points then rotates left and then back down 90 points
            t.push();
                t.translate(x, y, z);
                t.rotateX(rotateX);
                t.rotateY(rotateY);
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
            if (stripOffset < 0) stripOffset = 0;
            if (stripOffset >= totalStripsInModel) {
                return;
            }
            //Each controller has a different number of strips based in its cloud types/sizes
            //so we need to set the group sizes based on the ip address so that each one gets the
            //right number of strips
            //the groupsizes array is the number of strips per cloud
            // Default: 8 outputs, all 4-strip groups
            int[] groupSizes = {4,4,4,4,4,4,4,4};
            if ("192.168.0.204".equals(ip)) {
                groupSizes = new int[]{4,4,4,4,5,5,5,5};
            } else if ("192.168.0.205".equals(ip)) {
                groupSizes = new int[]{4,4,4,4,3,3,4,4};
            }
            else if ("192.168.0.203".equals(ip)) {
                groupSizes = new int[]{4,4,4,5,4,4,4,4};
            }

            int baseIndex = stripOffset;
            for (int i = 0; i < groupSizes.length; i++) {
                int groupSize = groupSizes[i];
                if (unusedPorts.contains(i)) {
                    baseIndex += groupSize;
                    continue;
                }
                //if the group size is 3 then it's a 3 strip cloud 
                //if the group size is 4, then it's a 4-strip cloud
                //if the group size is 5, then it's a 5-strip cloud
                if (groupSize == 5) {
                if (baseIndex + 4 >= totalStripsInModel) {
                    System.out.println("MikeyPixlite: Attempted to access strip out of bounds (5-strip output), skipping. BaseIndex: " + baseIndex + ", TotalStrips: " + totalStripsInModel);
                    baseIndex += groupSize;
                    continue;
                }
                    System.out.println("[OUT] Pixlite " + ip + " Output " + (i+1) + " (5-strip) strips " + baseIndex + " to " + (baseIndex+4));
                addPixliteOutput(
                    new PointsGrouping(String.valueOf(i + 1))
                        .addPoints(model.getStripByIndex(baseIndex).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 1).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 2).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 3).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 4).getPoints())
                );
            } else if (groupSize == 3) {
                if (baseIndex + 2 >= totalStripsInModel) {
                    System.out.println("MikeyPixlite: Attempted to access strip out of bounds (3-strip output), skipping. BaseIndex: " + baseIndex + ", TotalStrips: " + totalStripsInModel);
                    baseIndex += groupSize;
                    continue;
                }
                System.out.println("[OUT] Pixlite " + ip + " Output " + (i+1) + " (3-strip) strips " + baseIndex + " to " + (baseIndex+2));
                addPixliteOutput(
                    new PointsGrouping(String.valueOf(i + 1))
                        .addPoints(model.getStripByIndex(baseIndex).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 1).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 2).getPoints())
                );
            } else {
                if (baseIndex + 3 >= totalStripsInModel) {
                    System.out.println("MikeyPixlite: Attempted to access strip out of bounds (4-strip output), skipping. BaseIndex: " + baseIndex + ", TotalStrips: " + totalStripsInModel);
                    baseIndex += groupSize;
                    continue;
                }
                System.out.println("[OUT] Pixlite " + ip + " Output " + (i+1) + " (4-strip) strips " + baseIndex + " to " + (baseIndex+3));
                addPixliteOutput(
                    new PointsGrouping(String.valueOf(i + 1))
                        .addPoints(model.getStripByIndex(baseIndex).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 1).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 2).getPoints())
                        .addPoints(model.getStripByIndex(baseIndex + 3).getPoints())
                );
            }
            baseIndex += groupSize;
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