package com.symmetrylabs.slstudio.performance;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.*;
import com.symmetrylabs.util.dispatch.Dispatcher;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.parameter.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import purejavacomm.*;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;


public class PaletteListener {

    static class Module {
        public enum Type {
            HUB,
            BUTTON,
            DIAL,
            FADER
        }

        public Type type;
        public int id;
        public String uuid;
        int serialIndex;

        BooleanParameter pressed;
        BoundedParameter value;
        BoundedParameter smoothValue;


        PaletteListener listener;
        LinkedList<Float> valueQueue;




        public Module(PaletteListener listener, int serialIndex, int id, String uuid, Type type) {
            this.serialIndex = serialIndex;
            this.type = type;
            this.id = id;
            this.uuid = uuid;
            this.listener = listener;


            pressed = new BooleanParameter("pressed", false);
            value = new BoundedParameter("value", 0, 0, 1);
            if (type == Type.FADER) {
                smoothValue = listener.lx.engine.crossfader; //new BoundedParameter("value", 0, 0, 1);

            } else {
                smoothValue = new BoundedParameter("value", 0, 0, 1);
            }

            pressed.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    setLEDForState();
                }
            });

            value.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
//                    setLEDForState();
                }
            });

            valueQueue = new LinkedList<Float>();

            //setLEDForState();
        }

        private void send(JsonElement json) {
            listener.writeSerialJSON(serialIndex, json);
        }

        private void setLEDForState() {
            if (type == Type.BUTTON) {
                setLED(pressed.isOn() ? 1 : 0, 0, 0);
            }
            if (type == Type.DIAL || type == Type.FADER) {
//                setLED(0, 0, value.getValuef());
            }
        }

        public void setLED(float r, float g, float b) {
            int rByte = (int)(r * 255);
            int gByte = (int)(g * 255);
            int bByte = (int)(b * 255);

            JsonObject led = new JsonObject();
            led.add("i", new JsonPrimitive(id));
            led.add("m", new JsonPrimitive(0));
            led.add("r", new JsonPrimitive(rByte));
            led.add("g", new JsonPrimitive(gByte));
            led.add("b", new JsonPrimitive(bByte));

            JsonArray ledArr = new JsonArray();
            ledArr.add(led);

            JsonObject wrapper = new JsonObject();
            wrapper.add("led", ledArr);

            send(wrapper);
        }

        void smoothValue() {
            float v = value.getValuef();
            valueQueue.add(v);
            if (valueQueue.size() > 2) {
                valueQueue.removeFirst();
            }
            float avg = 0;
            for (float x : valueQueue) {
                avg += x;
            }
            avg /= valueQueue.size();
            final float s = avg;
            Dispatcher.getInstance(listener.lx).dispatchEngine(new Runnable() {
                @Override
                public void run() {
                    smoothValue.setValue(s);
                }
            });
        }

        public void setValues(int[] values) {
            switch (type) {
                case HUB:
                    break;
                case BUTTON:
                    pressed.setValue(values[0] == 1);
                    break;
                case DIAL:
                    pressed.setValue(values[0] == 1);
                    value.setValue((float)values[3] / 255.0f);
                    break;
                case FADER:
                    value.setValue((float)values[0] / 255.0f);
                    break;
            }
        }

    }

    static class PortListener implements SerialPortEventListener {
        PaletteListener listener;
        int serialIndex;

        public PortListener(PaletteListener listener, int serialIndex) {
            this.listener = listener;
            this.serialIndex = serialIndex;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            long millis = System.currentTimeMillis();
            JsonElement raw = listener.readSerialJSON(serialIndex);
            JsonObject j = raw.getAsJsonObject();
            long elapsed = System.currentTimeMillis() - millis;
            System.out.printf("PARSE TOOK %d ms\n", elapsed);
            if (elapsed > 10) {
                System.out.println(j.toString());
            }

            if (j.has("in")) {
                listener.pendingInputs.put(serialIndex, j.get("in").getAsJsonArray());
            } else if (j.has("l")) {
                listener.onPaletteModules(serialIndex, j.get("l").getAsJsonObject());
            }
        }
    }



    int nSerials;
    SerialPort[] serials;
    BufferedReader[] serialInputs;
    Table<Integer, Integer, Module> modules = null;
    JsonParser parser;
    HashMap<Integer, JsonArray> pendingInputs;
    final LX lx;

    boolean isPalettePort(String portName) {
        return portName.contains("tty.usbmodem");
    }



    ArrayList<CommPortIdentifier> getPalettePorts() {
        ArrayList<CommPortIdentifier> ports = new ArrayList<CommPortIdentifier>();
        Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();
        while (portIdentifiers.hasMoreElements()) {
            CommPortIdentifier id = portIdentifiers.nextElement();
            if (isPalettePort(id.getName())) {
                ports.add(id);
            }
        }
        return ports;
    }





    void openPorts() {
        ArrayList<CommPortIdentifier> palettePorts = getPalettePorts();
        nSerials = palettePorts.size();
        serials = new SerialPort[nSerials];
        for (int i = 0; i < nSerials; i++) {
            try {
                SerialPort port = (SerialPort) palettePorts.get(i).open("SLStudio", 1000);
                port.setSerialPortParams(1152000, 8, 1, 0);
                serials[i] = port;

                port.addEventListener(new PortListener(this, i));
                port.notifyOnDataAvailable(true);

            } catch (PortInUseException | UnsupportedCommOperationException | TooManyListenersException e) {
                e.printStackTrace();
            }
        }
    }

    JsonElement readSerialJSON(int serialIndex) {
        try {
            int objectCount = 0;
            StringBuilder builder = new StringBuilder();
            InputStream stream = serials[serialIndex].getInputStream();
            while (true) {
                char c = (char)stream.read();
                System.out.print(c);
                if (objectCount == 0 && c != '{') {
                    continue;
                }
                builder.append(c);
                if (c == '{') {
                    objectCount++;
                }
                if (c == '}') {
                    objectCount--;
                    if (objectCount == 0) {
                        break;
                    }
                }
            }
            String line = builder.toString();
            return parser.parse(line);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void writeSerialJSON(int serialIndex, JsonElement json) {
        try {
            OutputStream outputStream = serials[serialIndex].getOutputStream();
            String data = json.toString() + "\n";
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void traversePalette(int serialIndex, JsonObject node, HashSet<Integer> found) {
        int id = node.get("i").getAsInt();
        String uuid = node.get("u").getAsString();
        Module.Type type = Module.Type.values()[node.get("t").getAsInt()];

        if (!modules.contains(serialIndex, id)) {
            Module module = new Module(this, serialIndex, id, uuid, type);
            modules.put(serialIndex, id, module);
        }

        found.add(id);

        for (JsonElement el : node.get("c").getAsJsonArray()) {
            if (!el.isJsonNull()) {
                traversePalette(serialIndex, el.getAsJsonObject(), found);
            }
        }

    }

    void onPaletteModules(int serialIndex, JsonObject obj) {
        if (modules == null) {
            modules = HashBasedTable.create();
        }
        HashSet<Integer> found = new HashSet<Integer>();
        traversePalette(serialIndex, obj, found);
        ArrayList<Integer> toRemove = new ArrayList<Integer>();
        for (Integer id : modules.row(serialIndex).keySet()) {
            if (!found.contains(id)) {
                toRemove.add(id);
                // RAPHTODO
            }
        }
        for (Integer id : toRemove) {
            modules.remove(serialIndex, id);
        }
    }

    void onInput(int serialIndex, JsonArray inputs) {
        if (modules == null) {
            return;
        }

        for (JsonElement el : inputs) {
            JsonObject input = el.getAsJsonObject();
            int id = input.get("i").getAsInt();
            JsonArray valArr = input.get("v").getAsJsonArray();
            int[] values = new int[valArr.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = valArr.get(i).getAsInt();
            }
            Module module = modules.get(serialIndex, id);
            module.setValues(values);
//            module
        }

    }

    void writeStart() {
        for (int i = 0; i < nSerials; i++) {
            JsonObject start = new JsonObject();
            start.add("start", new JsonPrimitive(0));
            writeSerialJSON(i, start);
        }
    }

    void updateParameters() {
        for (Map.Entry<Integer, JsonArray> entry : pendingInputs.entrySet()) {
            onInput(entry.getKey(), entry.getValue());
        }
        pendingInputs.clear();
        if (modules == null) {
            return;
        }
        for (Module mod : modules.values()) {
            mod.smoothValue();
        }
    }

    void readLoop(int serialIndex) {
        JsonElement raw = readSerialJSON(serialIndex);
        JsonObject j = raw.getAsJsonObject();
        if (j.has("in")) {
            pendingInputs.put(serialIndex, j.get("in").getAsJsonArray());
        } else if (j.has("l")) {
            onPaletteModules(serialIndex, j.get("l").getAsJsonObject());
        }
    }

    public class Reader extends Thread {

        int serialIndex;

        public Reader(int serialIndex) {
            super();
            this.serialIndex = serialIndex;
        }

        @Override
        public void run() {

            // Loop for ten iterations.

            while (true) {

                readLoop(serialIndex);


//                // Sleep for a while
//                try {
//                    readLoop(serialIndex);
////                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    // Interrupted exception will occur if
//                    // the Worker object's interrupt() method
//                    // is called. interrupt() is inherited
//                    // from the Thread class.
//                    break;
//                }
            }
        }

    }

    void startListeners() {
        for (int i = 0; i < nSerials; i++) {
            new Reader(i).start();
        }
    }

    PaletteListener(LX lx) {
        this.lx = lx;
        parser = new JsonParser();

        pendingInputs = new HashMap<Integer, JsonArray>();

        openPorts();
//        startListeners();
        writeStart();


        lx.engine.addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double v) {
                updateParameters();
            }
        });


    }
}
