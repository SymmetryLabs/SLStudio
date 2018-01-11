package com.symmetrylabs.slstudio.automapping;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import processing.net.Client;
import processing.net.Server;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.CubesModel;

public class ClientCommunicator implements LXLoopTask {
    public static final int PORT = 8724;

    private static class DelayedResponse {
        private int frameDelay;
        private Object response;

        public DelayedResponse(int frameDelay, Object response) {
            this.frameDelay = frameDelay;
            this.response = response;
        }
    }

    private float textRotX = 0;
    private float textRotY = 0;
    private float textRotZ = 0;

    private float textCoordX = 0;
    private float textCoordY = 0;
    private float textCoordZ = 0;

    private float textScaleX = 1;
    private float textScaleY = 1;

    private Gson gson = new Gson();
    private List<DelayedResponse> delayedResponses = new ArrayList<>();
    private Server server;
    private final Automapper automapper;

    ClientCommunicator(Automapper automapper) {
        this.automapper = automapper;
    }

    public synchronized void start() {
        if (server != null)
            return;

        System.out.println("Starting ClientCommunicator on port " + PORT);

        server = new Server(SLStudio.applet, PORT);
    }

    public synchronized void stop() {
        if (server == null)
            return;

        System.out.println("Stopping ClientCommunicator");

        server.stop();
        server = null;
    }

    public void loop(double deltaMs) {
        if (server == null)
            return;

        if (server.clients == null) {
            stop();
            return;
        }

        processPendingResponses();

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
                sendResponse(id);
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

                sendResponse(id);
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

                sendResponse(id);
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

                sendResponse(id, mappingState);
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

                sendResponse(id, xyz);
                return;
            }

            if (method.equals("mapping.showNextCube")) {
                String cubeId = (String)args.get("id");
                automapper.showNextCube(cubeId);
                sendResponse(id);
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
                    sendDelayedResponse(id, 2);
                } else {
                    sendErrorResponse(id);
                }
                return;
            }

            if (method.equals("mapping.showAllOn")) {
                automapper.showAll(true);
                sendDelayedResponse(id, 2);
                return;
            }

            if (method.equals("mapping.showAllOff")) {
                automapper.showAll(false);
                sendDelayedResponse(id, 2);
                return;

            }

            if (method.equals("mapping.disableAutomapping")) {
                    automapper.disableAutomapping();
                    sendResponse(id);
                return;
            }

            if (method.equals("saveFile")) {
                //String fileName = (String)args.get("fileName");
                //String content = (String)args.get("content");
                //if (content != null && fileName != null) {
                //    saveBytes(dataPath(fileName), content.getBytes());
                //    sendResponse(id);
                //} else {
                //    sendErrorResponse(id);
                //}

                // TODO
                return;

            }

            if (method.equals("setCubeTransforms")) {
                //List cubeTransforms = (List)args.get("cubeTransforms");
                //if (cubeTransforms != null) {
                //    println("cubeTransforms: "+cubeTransforms);
                //    saveBytes(dataPath("cube_transforms.json"), new Gson().toJson(cubeTransforms).getBytes());
                //    sendResponse(id);
                //} else {
                //    sendErrorResponse(id);
                //}

                // TODO
                return;
            }

            // if no method matches...
            sendErrorResponse(id);
        }
        catch (Exception e) {
            System.out.println("CAUGHT A SERVER EXCEPTION");
            e.printStackTrace();
        }
    }

    void processPendingResponses() {
        for (int i = 0; i < delayedResponses.size(); i++) {
            DelayedResponse delayedResponse = delayedResponses.get(i);
            delayedResponse.frameDelay--;
            if (delayedResponse.frameDelay <= 0) {
                delayedResponses.remove(i);
                i--;
                // System.out.println("Sent delayed response: " + gson.toJson(delayedResponse.response));
                sendMessage(delayedResponse.response);
            }
        }
    }

    void addDelayedResponse(int frameDelay, Object response) {
        delayedResponses.add(new DelayedResponse(frameDelay, response));
    }

    public void sendCommand(String command, Object args) {
        Map<String, Object> json = new HashMap<>();
        json.put("command", command);
        json.put("args", args);
        sendMessage(json);
        System.out.println("Sent command: " + command);
    }

    void sendResponse(Object id) {
        sendResponse(id, null);
    }

    void sendErrorResponse(Object id) {
        sendErrorResponse(id, null);
    }

    void sendDelayedResponse(Object id, int frameDelay) {
        sendDelayedResponse(id, null, frameDelay);
    }

    void sendDelayedResponse(Object id, Object response, int frameDelay) {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("response-to", id);
        json.put("response", response);
        addDelayedResponse(frameDelay, json);
    }

    void sendResponse(Object id, Object response) {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("response-to", id);
        json.put("response", response);
        sendMessage(json);
        System.out.println("Sent response: " + gson.toJson(json));
    }

    void sendErrorResponse(Object id, Object error) {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("response-to", id);
        json.put("error", error);
        sendMessage(json);
        System.out.println("Sent response: " + gson.toJson(json));
    }

    void sendMessage(Object message) {
        if (server == null)
            return;

        server.write(gson.toJson(message) + "\r\n");
    }

    void disconnectClient(Client client) {
        client.dispose();

        if (server == null)
            return;

        server.disconnect(client);
    }
}
