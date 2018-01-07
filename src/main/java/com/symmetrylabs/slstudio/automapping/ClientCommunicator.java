package com.symmetrylabs.slstudio.automapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import processing.net.Client;
import processing.net.Server;

public class ClientCommunicator {

    class DelayedResponse {
        int frameDelay;
        Object response;
        DelayedResponse(int frameDelay, Object response) {
            this.frameDelay = frameDelay;
            this.response = response;
        }
    }

    Gson gson = new Gson();

    Server server;

    List<DelayedResponse> delayedResponses = new ArrayList<>();

    public ClientCommunicator(Server server) {
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
        server.write(gson.toJson(message) + "\r\n");
    }

    void disconnectClient(Client client) {
        client.dispose();
        server.disconnect(client);
    }
}
