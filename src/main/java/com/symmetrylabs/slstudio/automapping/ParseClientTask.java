package com.symmetrylabs.slstudio.automapping;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.model.LXPoint;
import processing.net.Client;
import processing.net.Server;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.model.CubesModel;

public class ParseClientTask implements LXLoopTask {
    public static final int PORT = 8724;

    Gson gson = new Gson();

    Server server;
    ClientCommunicator communicator;

    float textRotX = 0;
    float textRotY = 0;
    float textRotZ = 0;

    float textCoordX = 0;
    float textCoordY = 0;
    float textCoordZ = 0;

    float textScaleX = 1;
    float textScaleY = 1;

    private Automapper automapper;

    ParseClientTask(Automapper automapper) {
        this.automapper = automapper;
    }

    public void loop(double deltaMs) {
        if (server == null)
            return;

        if (server.clients == null) {
            stop();
            return;
        }

        if (communicator != null) {
            communicator.processPendingResponses();
        }

        automapper.setClientsConnected(server.clientCount > 0);

        try {
            Client client = server.available();
            if (client == null)
                return;

            String whatClientSaid = client.readStringUntil('\n');
            if (whatClientSaid == null)
                return;

            System.out.print("Request: " + whatClientSaid);

            Map<String, Object> message = null;
            try {
                message = gson.fromJson(whatClientSaid.trim(), new TypeToken<Map<String, Object>>() {}.getType());
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Got: " + message);
                return;
            }

            if (message == null)
                return;

            String method = (String)message.get("command");
            Object id = message.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map)message.get("args");


            if (method == null)
                return;
            if (args == null) args = new HashMap<>();

            if (method.equals("mapping.startCalibration")) {
                automapper.startCalibration();
                communicator.sendResponse(id);
                return;
            }

            if (method.equals("debug.setText")) {
                System.out.println("SIZE: " + CubesModel.Cube.Type.LARGE.EDGE_WIDTH);

                textScaleX = (float)(double)args.get("textScaleX");
                textScaleY = (float)(double)args.get("textScaleY");

                textRotX = (float)(double)args.get("textRotX");
                textRotY = (float)(double)args.get("textRotY");
                textRotZ = (float)(double)args.get("textRotZ");

                textCoordX = (float)(double)args.get("textCoordX");
                textCoordY = (float)(double)args.get("textCoordY");
                textCoordZ = (float)(double)args.get("textCoordZ");

                communicator.sendResponse(id);
                return;
            }

            if (method.equals("mapping.sendCubeTransform")) {
                LXTransform transform = new LXTransform();
                LXMatrix matrix = transform.getMatrix();

                System.out.println("ARGS: " + args);

                String mac = (String)args.get("id");

                List<List<Double>> jsonTransform = (List<List<Double>>)args.get("transform");

                List<Double> rvec = (List<Double>)args.get("rvec");
                List<Double> tvec = (List<Double>)args.get("tvec");

                automapper.addCube(jsonTransform, rvec, tvec, mac);

                // automapper.addCubeRvecTvec(rvec, tvec, mac);

                communicator.sendResponse(id);
                return;
            }

            if (method.equals("mapping.startMapping")) {
                //List<Double> pixelOrder = (List)args.get("pixelOrder");
                //int[] pixelOrderArray = null;
                //if (pixelOrder != null) {
                //    pixelOrderArray = automapper.getPixelOrder();
                //    if (pixelOrderArray == null || pixelOrderArray.length != pixelOrder.size()) {
                //        pixelOrderArray = new int[pixelOrder.size()];
                //        for (int i = 0; i < pixelOrder.size(); i++) {
                //            pixelOrderArray[i] = pixelOrder.get(i).intValue();
                //        }
                //    }
                //}
                //automapper.setPixelOrder(pixelOrderArray);
                automapper.startMapping();

                Map<String, Object> mappingState = new HashMap<>();
                mappingState.put("mapped", automapper.getMappedIds());
                mappingState.put("mappedTransforms", automapper.getMappedTransforms());
                mappingState.put("unmapped", automapper.getUnmappedIds());

                communicator.sendResponse(id, mappingState);
                return;
            }

            if (method.equals("mapping.mapNextCube")) {
                String cubeId = (String)args.get("id");
                automapper.mapNextCube(cubeId);

                // ArrayList<HashMap> points = new ArrayList<HashMap>();
                List<LXPoint> points = automapper.getPointsForId(cubeId);

                List<Map<String, Float>> xyz = new ArrayList<>();
                for (LXPoint p : points) {
                    Map<String, Float> hm = new HashMap<>();
                    hm.put("x", p.x);
                    hm.put("y", p.y);
                    hm.put("z", p.z);
                    xyz.add(hm);
                }

                communicator.sendResponse(id, xyz);
                return;
            }

            if (method.equals("mapping.showNextCube")) {
                String cubeId = (String)args.get("id");
                automapper.showNextCube(cubeId);
                communicator.sendResponse(id);
                return;
            }

            if (method.equals("mapping.showAutomappingPattern")) {
                String cubeId = (String)args.get("id");
                System.out.println("MAPPING NEXT CUBE");
                automapper.mapNextCube(cubeId);
                return;
            }

            if (method.equals("showError")) {
                System.out.println("GOT SHOW ERROR");
                String error = (String)args.get("message");
                automapper.showError(error, false);
                return;
            }

            if (method.equals("dismissError")) {
                automapper.dismissError();
                return;
            }

            if (method.equals("mapping.showImageForPixel")) {
                Number pixelIndex = (Number)args.get("pixelIndex");
                if (pixelIndex != null) {
                    automapper.showImageForPixel(pixelIndex.intValue());
                    communicator.sendDelayedResponse(id, 2);
                } else {
                    communicator.sendErrorResponse(id);
                }
                return;
            }

            if (method.equals("mapping.showAllOn")) {
                automapper.showAll(true);
                communicator.sendDelayedResponse(id, 2);
                return;
            }

            if (method.equals("mapping.showAllOff")) {
                automapper.showAll(false);
                communicator.sendDelayedResponse(id, 2);
                return;

            }

            if (method.equals("mapping.disableAutomapping")) {
                    automapper.disableAutomapping();
                    communicator.sendResponse(id);
                return;
            }

            if (method.equals("saveFile")) {
                //String fileName = (String)args.get("fileName");
                //String content = (String)args.get("content");
                //if (content != null && fileName != null) {
                //    saveBytes(dataPath(fileName), content.getBytes());
                //    communicator.sendResponse(id);
                //} else {
                //    communicator.sendErrorResponse(id);
                //}

                // TODO
                return;

            }

            if (method.equals("setCubeTransforms")) {
                //List cubeTransforms = (List)args.get("cubeTransforms");
                //if (cubeTransforms != null) {
                //    println("cubeTransforms: "+cubeTransforms);
                //    saveBytes(dataPath("cube_transforms.json"), new Gson().toJson(cubeTransforms).getBytes());
                //    communicator.sendResponse(id);
                //} else {
                //    communicator.sendErrorResponse(id);
                //}

                // TODO
                return;
            }

            // if no method matches...
            communicator.sendErrorResponse(id);

        } catch (Exception e) {
            System.out.println("CAUGH A SERVER EXCEPTION");
            e.printStackTrace();
        }
    }

    public synchronized void start() {
        if (server != null)
            return;

        System.out.println("Starting ParseClientTask on port " + PORT);

        server = new Server(SLStudio.applet, PORT);
        communicator = new ClientCommunicator(server);
        automapper.communicator = communicator;
    }

    public synchronized void stop() {
        if (server == null)
            return;

        System.out.println("Stopping ParseClientTask");

        server.stop();
        server = null;
        communicator = null;
    }
}
