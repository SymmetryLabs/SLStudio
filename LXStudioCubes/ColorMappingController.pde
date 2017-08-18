public class ColorMappingController {
  private final AutomappingController amc;
  private final HashMap<String, ColorMappingClientState> clientsById;
  private final Queue<ColorMappingClientState> requestQueue;

  ColorMappingController(AutomappingController amc) {
    this.amc = amc;
    clientsById = new HashMap<String, ColorMappingClientState>();
    requestQueue = new LinkedList<ColorMappingClientState>();
  }

  private boolean looping = false;
  private void ensureLooping() {
    if (!looping) {
      registerMethod("post", this);
      looping = true;
    }
  }

  // Called repeatedly by the Processing runtime after drawing has
  // completed and the frame is done (no drawing allowed).
  // https://github.com/processing/processing/wiki/Library-Basics#library-methods
  public void post() {
    ColorMappingClientState state = requestQueue.peek();
    if (state != null) {
      // Notify the client whose request is waiting at the front of the
      // queue that their requested colors have been displayed.
      state.maybeSendResponse();
    }
  }

  int getClientCount() {
    return clientsById.size();
  }

  int getRequestCount() {
    return requestQueue.size();
  }

  boolean handleMethod(Client client, Map<String, Object> message) {
    final String id = (String) message.get("id");
    final String clientId = client.ip();
    final String method = (String) message.get("command");
    final Map<String, Object> args = (Map) message.get("args");

    if (! method.startsWith("colorMapping.")) {
      return false;
    }

    // Clients connect to the ColorMappingController by sending the
    // colorMapping.attachClient command, which returns data about the
    // fixtures to be mapped, including the pixel count of each fixture.
    if (method.equals("colorMapping.attachClient")) {
      if (!clientsById.containsKey(clientId)) {
        boolean wasEmpty = clientsById.size() == 0;

        ColorMappingClientState state =
          new ColorMappingClientState(amc.communicator);

        clientsById.put(clientId, state);

        if (wasEmpty) {
          amc.startMapping();
        }
      }

      Map<String, Integer> pixelCounts = new HashMap<String, Integer>();
      for (SLController c : controllers) {
        pixelCounts.put(c.getMacAddressString(), c.numPixels);
      }

      Map<String, Object> response = new HashMap<String, Object>();
      response.put("mapped", amc.getMappedIds());
      response.put("mappedTransforms", amc.getMappedTransforms());
      response.put("unmapped", amc.getUnmappedIds());
      response.put("pixelCounts", pixelCounts);

      amc.communicator.sendResponse(id, response);

      return true;
    }

    // Connected clients make requests for the server to display certain
    // pixel colors by sending the colorMapping.requestColors command. The
    // "request" argument is a JSON object mapping fixture IDs (i.e. MAC
    // addresses) to LED requests. These LED requests, in turn, are JSON
    // objects that map numeric pixel indexes to color strings, which
    // consist of combinations of the letters 'r', 'g', and 'b' (e.g., "r"
    // for red, "gb" for cyan, "br" for magenta). This way of specifying a
    // color was much easier than trying to send bytes encoded as integers
    // over the network. Obviously not all colors are expressible, but we
    // only care about optically discernible colors. Any pixels not
    // explicitly specified are assumed to be LXColor.BLACK.
    //
    // The server responds to these requests asynchronously, whenever it
    // gets around to displaying the requested colors. This allows
    // multiple clients to make requests simultaneously, so you could (for
    // example) set up three cameras around the installation, all vying to
    // collect information about the LEDs at the same time. The mapping
    // process could then proceed from all angles at once without any
    // further intervention.
    if (method.equals("colorMapping.requestColors")) {
      ColorMappingClientState state = clientsById.get(clientId);

      if (state == null) {
        amc.communicator.sendErrorResponse(id);
        return true;
      }

      // TODO Make the removal O(1) somehow.
      requestQueue.remove(state);

      state.setPendingRequest(id, (Map) args.get("request"));

      requestQueue.add(state);

      ensureLooping();

      return true;
    }

    // Finally, a client can inform the server that it has observed the
    // results of an LED request, thus unblocking other client requests,
    // by sending the colorMapping.finishRequest command. Ideally, the
    // client should send this command immediately after capturing a
    // suitable image, but before any expensive processing, so that other
    // clients can make progress while that processing is happening.
    if (method.equals("colorMapping.finishRequest")) {
      ColorMappingClientState state = clientsById.get(clientId);
      if (state != null) {
        requestQueue.remove(state);
        state.clearPendingRequest();
      }
      amc.communicator.sendResponse(id);
      return true;
    }

    return false;
  }

  // Called to synchronize clientsById with the list of connected Client
  // objects. Thanks to this system, clients don't need to send an
  // explicit command like colorMapping.detachClient. A listener interface
  // would be better, but Processing doesn't seem to expose that.
  void updateActiveClients(Client[] clients) {
    if (clientsById.isEmpty()) {
      return;
    }

    Set<String> activeClientIds = new HashSet<String>();

    for (Client client : clients) {
      if (client != null && client.active()) {
        activeClientIds.add(client.ip());
      }
    }

    for (String clientId : clientsById.keySet()) {
      if (!activeClientIds.contains(clientId)) {
        ColorMappingClientState state = clientsById.get(clientId);

        println("Removing client " + clientId + ": " + state);

        if (state != null) {
          state.request = null;
          requestQueue.remove(state);
        }

        clientsById.remove(clientId);
      }
    }

    if (clientsById.isEmpty()) {
      // TODO Is this too aggressive? Will we ever want to revert back
      // to normal automapping after color mapping is finished?
      amc.disableAutomapping();
    }
  }

  int getPixelColor(String fixtureId, int pixelIndex) {
    ColorMappingClientState state = requestQueue.peek();

    if (state == null) {
      return LXColor.BLACK;
    }

    return state.getPixelColor(fixtureId, pixelIndex);
  }
}

private class ColorMappingClientState {
  private ClientCommunicator communicator = null;
  private String requestId = null;
  private Map<String, Object> request = null;
  private boolean needToCallGetPixelColor = false;

  ColorMappingClientState(ClientCommunicator communicator) {
    this.communicator = communicator;
  }

  // Called from ColorMappingController#post after each turn of the
  // drawing loop. Since we don't (can't?) know if the colors we sent to
  // the LED fixture have actually been displayed yet, we rely on a
  // heuristic: did getPixelColor need to be called in order to display
  // the current request, and has it been called?
  void maybeSendResponse() {
    if (!needToCallGetPixelColor && requestId != null) {
      communicator.sendResponse(requestId);
      // Send the response only once.
      requestId = null;
      // We don't set request = null here, because we want the LEDs to
      // continue displaying the current request until the client sends
      // the colorMapping.finishRequest command.
    }
  }

  void setPendingRequest(
    String requestId,
    Map<String, Object> request
  ) {
    clearPendingRequest();

    for (Object colors : request.values()) {
      if (((Map) colors).size() > 0) {
        needToCallGetPixelColor = true;
        break;
      }
    }

    this.request = request;
    this.requestId = requestId;
  }

  void clearPendingRequest() {
    needToCallGetPixelColor = false;
    this.request = null;
    if (requestId != null) {
      communicator.sendErrorResponse(requestId);
      requestId = null;
    }
  }

  int getPixelColor(String fixtureId, int pixelIndex) {
    if (! request.containsKey(fixtureId)) {
      return LXColor.BLACK;
    }

    Map<String, String> colors = (Map) request.get(fixtureId);

    String rgbChars = colors.get("" + pixelIndex);

    if (rgbChars == null) {
      return LXColor.BLACK;
    }

    rgbChars = rgbChars.toLowerCase();

    int c = LXColor.BLACK;

    // TODO Allow different brightness?

    if (rgbChars.indexOf("r") >= 0) {
      c |= LXColor.RED;
    }

    if (rgbChars.indexOf("g") >= 0) {
      c |= LXColor.GREEN;
    }

    if (rgbChars.indexOf("b") >= 0) {
      c |= LXColor.BLUE;
    }

    // Now that we've called getPixelColor and gotten a color that was
    // relevant to the current request, we can send a response to the
    // client the next time maybeSendResponse is called.
    needToCallGetPixelColor = false;

    return c;
  }
}
