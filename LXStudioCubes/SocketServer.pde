import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import processing.net.*;

import javax.swing.*;
import java.awt.Window;

float textRotX = 0;
float textRotY = 0;
float textRotZ = 0;

float textCoordX = 0;
float textCoordY = 0;
float textCoordZ = 0;

float textScaleX = 1;
float textScaleY = 1;


class AppServer {

  LX lx;

  final BooleanParameter enabled = new BooleanParameter("Automapping server enabled");

  ParseClientTask parseClientTask;
  ServerDiscovery serverDiscovery;

  AppServer(LX lx) {
    this.lx = lx;

    parseClientTask = new ParseClientTask();
    lx.engine.addLoopTask(parseClientTask);

    serverDiscovery = new ServerDiscovery();


    enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (enabled.isOn()) {
          start();
        } else {
          stop();
        }
      }
    });
    enabled.setValue(true);

  }

  void start() {
    parseClientTask.start();
  }

  void stop() {
    parseClientTask.stop();
  }
}

public class ParseClientTask implements LXLoopTask {
  Gson gson = new Gson();

  Server server;
  ClientCommunicator communicator;


  ParseClientTask() {
    registerMethod("dispose", this);
  }

  public void loop(double deltaMs) {
    if (server == null) return;
    if (server.clients == null) {
      stop();
      return;
    }
    if (communicator != null) {
      communicator.processPendingResponses();
    }

    automappingController.setClientsConnected(server.clientCount > 0);

    try {
      Client client = server.available();
      if (client == null) return;

      String whatClientSaid = client.readStringUntil('\n');
      if (whatClientSaid == null) return;

      System.out.print("Request: " + whatClientSaid);

      Map<String, Object> message = null;
      try {
        message = gson.fromJson(whatClientSaid.trim(), new TypeToken<Map<String, Object>>() {}.getType());
      } catch (Exception e) {
        System.out.println(e);
        System.out.println("Got: " + message);
        return;
      }

      if (message == null) return;

      String method = (String)message.get("command");
      Object id = message.get("id");
      @SuppressWarnings("unchecked")
      Map<String, Object> args = (Map)message.get("args");


      if (method == null) return;
      if (args == null) args = new HashMap<String, Object>();

      if (method.equals("mapping.startCalibration")) {
        automappingController.startCalibration();
        communicator.sendResponse(id);
        return;
      }

      if (method.equals("debug.setText")) {
        println("SIZE", CubesModel.Cube.Type.LARGE.EDGE_WIDTH);

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

        println("ARGS", args);

        String mac = (String)args.get("id");


        ArrayList<ArrayList<Double>> jsonTransform = (ArrayList<ArrayList<Double>>)args.get("transform");



        ArrayList<Double> rvec = (ArrayList<Double>)args.get("rvec");
        ArrayList<Double> tvec = (ArrayList<Double>)args.get("tvec");

        automappingController.addCube(jsonTransform, rvec, tvec, mac);



        // automappingController.addCubeRvecTvec(rvec, tvec, mac);


        communicator.sendResponse(id);
        return;
      }

      if (method.equals("mapping.startMapping")) {
        // List<Double> pixelOrder = (List)args.get("pixelOrder");
        // int[] pixelOrderArray = null;
        // if (pixelOrder != null) {
        //   pixelOrderArray = automappingController.getPixelOrder();
        //   if (pixelOrderArray == null || pixelOrderArray.length != pixelOrder.size()) {
        //     pixelOrderArray = new int[pixelOrder.size()];
        //     for (int i = 0; i < pixelOrder.size(); i++) {
        //       pixelOrderArray[i] = pixelOrder.get(i).intValue();
        //     }
        //   }
        // }
        // automappingController.setPixelOrder(pixelOrderArray);
        automappingController.startMapping();

        HashMap<String, Object> mappingState = new HashMap<String, Object>();
        mappingState.put("mapped", automappingController.getMappedIds());
        mappingState.put("mappedTransforms", automappingController.getMappedTransforms());
        mappingState.put("unmapped", automappingController.getUnmappedIds());

        communicator.sendResponse(id, mappingState);
        return;
      }

      if (method.equals("mapping.mapNextCube")) {
        String cubeId = (String)args.get("id");
        automappingController.mapNextCube(cubeId);

        // ArrayList<HashMap> points = new ArrayList<HashMap>();
        ArrayList<LXPoint> points = automappingController.getPointsForId(cubeId);

        ArrayList<HashMap<String, Float>> xyz = new ArrayList<HashMap<String, Float>>();
        for (LXPoint p : points) {
          HashMap<String, Float> hm = new HashMap<String, Float>();
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
        automappingController.showNextCube(cubeId);
        communicator.sendResponse(id);
        return;
      }

      if (method.equals("mapping.showAutomappingPattern")) {
        String cubeId = (String)args.get("id");
        println("MAPPING NEXT CUBE");
        automappingController.mapNextCube(cubeId);
        return;
      }

      if (method.equals("showError")) {
        println("GOT SHOW ERROR");
        String error = (String)args.get("message");
        automappingController.showError(error, false);
        return;
      }

      if (method.equals("dismissError")) {
        automappingController.dismissError();
        return;
      }




      if (method.equals("mapping.showImageForPixel")) {
        Number pixelIndex = (Number)args.get("pixelIndex");
        if (pixelIndex != null) {
          automappingController.showImageForPixel(pixelIndex.intValue());
          communicator.sendDelayedResponse(id, 2);
        } else {
          communicator.sendErrorResponse(id);
        }
        return;
      }

      if (method.equals("mapping.showAllOn")) {
        automappingController.showAll(true);
        communicator.sendDelayedResponse(id, 2);
        return;
      }

      if (method.equals("mapping.showAllOff")) {
        automappingController.showAll(false);
        communicator.sendDelayedResponse(id, 2);
        return;

      }

      if (method.equals("mapping.disableAutomapping")) {
          automappingController.disableAutomapping();
          communicator.sendResponse(id);
        return;
      }

      if (method.equals("saveFile")) {
        // String fileName = (String)args.get("fileName");
        // String content = (String)args.get("content");
        // if (content != null && fileName != null) {
        //   saveBytes(dataPath(fileName), content.getBytes());
        //   communicator.sendResponse(id);
        // } else {
        //   communicator.sendErrorResponse(id);
        // }

        // TODO
        return;

      }

      if (method.equals("setCubeTransforms")) {
        // List cubeTransforms = (List)args.get("cubeTransforms");
        // if (cubeTransforms != null) {
        //   println("cubeTransforms: "+cubeTransforms);
        //   saveBytes(dataPath("cube_transforms.json"), new Gson().toJson(cubeTransforms).getBytes());
        //   communicator.sendResponse(id);
        // } else {
        //   communicator.sendErrorResponse(id);
        // }

        // TODO
        return;
      }

      // if no method matches...
      communicator.sendErrorResponse(id);

    } catch (Exception e) {
      println("CAUGH A SERVER EXCEPTION");
      e.printStackTrace();
    }
  }

  void start() {
    if (server != null) {
      server.stop();
    }
    server = new Server(LXStudioCubes.this, 8724);
    communicator = new ClientCommunicator(server);
    automappingController.communicator = communicator;
  }

  void stop() {
    if (server != null) {
      server.stop();
      server = null;
      communicator = null;
    }
  }

  void dispose() {
    stop();
  }
}

class DelayedResponse {
  int frameDelay;
  Object response;
  DelayedResponse(int frameDelay, Object response) {
    this.frameDelay = frameDelay;
    this.response = response;
  }
}

class ClientCommunicator {
  Gson gson = new Gson();

  Server server;

  List<DelayedResponse> delayedResponses = new ArrayList<DelayedResponse>();

  ClientCommunicator(Server server) {
    this.server = server;
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

  void sendCommand(String command, Object args) {
    Map<String, Object> json = new HashMap<String, Object>();
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
    server.write(gson.toJson(message) + "\r\n");
  }

  void disconnectClient(Client client) {
    client.dispose();
    server.disconnect(client);
  }
}
