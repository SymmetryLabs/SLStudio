package com.symmetrylabs.slstudio.performance;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.*;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
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

        BooleanParameter pressed = null;
        LXListenableNormalizedParameter value = null;
        DiscreteParameter discrete = null;
        ColorParameter color = null;
        int clickCounter;
        boolean upsideDown;
//        BoundedParameter smoothValue;


        LinkedList<Float> valueQueue;

        final LXParameterListener pressedListener;
        final LXParameterListener valueListener;
        final LXParameterListener discreteListener;
        final LXParameterListener colorListener;

        int lastR;
        int lastG;
        int lastB;

        PaletteListener listener;








        public Module(PaletteListener listener, int serialIndex, int id, String uuid, Type type) {
            this.serialIndex = serialIndex;
            this.type = type;
            this.id = id;
            this.uuid = uuid;
            this.listener = listener;

            upsideDown = false;

            clickCounter = 0;


//            pressed = new BooleanParameter("pressed", false);
//            value = new BoundedParameter("value", 0, 0, 1);
//            if (type == Type.FADER) {
//                smoothValue = listener.lx.engine.crossfader; //new BoundedParameter("value", 0, 0, 1);
//
//            } else {
//                smoothValue = new BoundedParameter("value", 0, 0, 1);
//            }

//            pressed.addListener(new LXParameterListener() {
//                @Override
//                public void onParameterChanged(LXParameter lxParameter) {
//                    setLEDForState();
//                }
//            });

//            value.addListener(new LXParameterListener() {
//                @Override
//                public void onParameterChanged(LXParameter lxParameter) {
////                    setLEDForState();
//                }
//            });

//            valueQueue = new LinkedList<Float>();

            //setLEDForState();

            pressedListener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {

                }
            };

            valueListener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {

                }
            };

            discreteListener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {

                }
            };

            colorListener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
//                    LXColor.
                    int c = color.getColor();
                    int r = LXColor.red(c);
                    int g = LXColor.green(c);
                    int b = LXColor.blue(c);

                    r = r < 0 ? 256 + r : r;
                    g = g < 0 ? 255 + g : g;
                    b = b < 0 ? 255 + b : b;
                    setLED(r, g, b);
                }
            };
        }

        public void setUpsideDown(boolean ud) {
            upsideDown = ud;
        }

        public void removeListeners() {
            if (discrete != null) {
                discrete.removeListener(discreteListener);
            }
            discrete = null;

            if (value != null) {
                value.removeListener(valueListener);
            }
            value = null;

            if (pressed != null) {
                pressed.removeListener(pressedListener);
            }
            pressed = null;

            if (color != null) {
                color.removeListener(colorListener);
            }
            color = null;
        }

        void mapParameter(DiscreteParameter param) {
            if (discrete != null) {
                discrete.removeListener(discreteListener);
            }
            discrete = param;
            discrete.addListener(discreteListener);
        }

        void mapParameter(LXListenableNormalizedParameter param) {
            if (value != null) {
                value.removeListener(valueListener);
            }
            value = param;
            value.addListener(valueListener);
        }

        void mapParameter(BooleanParameter param) {
            if (pressed != null) {
                pressed.removeListener(pressedListener);
            }
            pressed = param;
            pressed.addListener(pressedListener);
        }

        void mapParameter(LXParameter param) {
            if (param instanceof  DiscreteParameter) {
                mapParameter((DiscreteParameter)param);
                return;
            }

            if (param instanceof  BooleanParameter) {
                mapParameter((BooleanParameter) param);
                return;
            }

            if (param instanceof  LXListenableNormalizedParameter) {
                mapParameter((LXListenableNormalizedParameter) param);
                return;
            }
        }

        void mapColor(ColorParameter c) {

            if (color != null) {
                color.removeListener(colorListener);
            }
            lastR = -1;
            lastG = -1;
            lastB = -1;

            if (c == null) {
                return;
            }

            color = c;
            color.addListener(colorListener);
            colorListener.onParameterChanged(color);
        }



        private void send(JsonElement json) {
            listener.writeSerialJSON(serialIndex, json);
        }


        public void setLED(int r, int g, int b) {

            if (lastR == r && lastG == g && lastB == b) {
                return;
            }

            lastR = r;
            lastG = g;
            lastB = b;

            JsonObject led = new JsonObject();
            led.add("i", new JsonPrimitive(id));
            led.add("m", new JsonPrimitive(0));
            led.add("r", new JsonPrimitive(r));
            led.add("g", new JsonPrimitive(g));
            led.add("b", new JsonPrimitive(b));

            JsonArray ledArr = new JsonArray();
            ledArr.add(led);

            JsonObject wrapper = new JsonObject();
            wrapper.add("led", ledArr);

            send(wrapper);
        }

//        void smoothValue() {
//            float v = value.getValuef();
//            valueQueue.add(v);
//            if (valueQueue.size() > 2) {
//                valueQueue.removeFirst();
//            }
//            float avg = 0;
//            for (float x : valueQueue) {
//                avg += x;
//            }
//            avg /= valueQueue.size();
//            final float s = avg;
//            Dispatcher.getInstance(listener.lx).dispatchEngine(new Runnable() {
//                @Override
//                public void run() {
//                    smoothValue.setValue(s);
//                }
//            });
//        }

        void setPressed(boolean p) {
            if (pressed != null) {
                boolean current = pressed.getValueb();
                pressed.setValue(p);
            }
        }

        void setValue(float v) {
            if (value != null) {
                float val = upsideDown ? (1.0f - v) : v;
                value.setNormalized(val);
            }
        }

        void setDiscrete(int clicks) {
            int clicksPerSlot = 3;
            if (clickCounter > 0 && clicks < 0) {
                clickCounter = 0;
            }
            if (clickCounter < 0 && clicks > 0) {
                clickCounter = 0;
            }
            clickCounter += clicks;
            if (Math.abs(clickCounter) >= clicksPerSlot) {
                int dir = clickCounter / clicksPerSlot;
                clickCounter = 0;
                if (discrete != null) {
                    int val = Math.max(discrete.getMinValue(), Math.min(discrete.getMaxValue(), discrete.getValuei() + dir));
                    discrete.setValue(val);
                }
            }
        }

        public void setValues(int[] values) {
            int clicks = 0;
            switch (type) {
                case HUB:
                    break;
                case BUTTON:
                    setPressed(values[0] == 1);
                    break;
                case DIAL:
                    setPressed(values[0] == 1);
                    setValue((float)values[3] / 255.0f);
                    clicks = values[1] > 0 ? -1 * values[1] : values[2];
                    setDiscrete(clicks);
                    break;
                case FADER:
                    setValue((float)values[0] / 255.0f);
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
            if (raw == null) {
                return;
            }
            JsonObject j = raw.getAsJsonObject();
            long elapsed = System.currentTimeMillis() - millis;

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
    private Table<Integer, Integer, Module> modules = null;
    public ListenableSet<Module> moduleSet;
    JsonParser parser;
    HashMap<Integer, JsonArray> pendingInputs;
    final LX lx;
    StringBuilder[] builders;


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
        builders = new StringBuilder[nSerials];
        for (int i = 0; i < nSerials; i++) {
            try {
                SerialPort port = (SerialPort) palettePorts.get(i).open("SLStudio", 1000);
                port.setSerialPortParams(1152000, 8, 1, 0);
                serials[i] = port;

                port.addEventListener(new PortListener(this, i));
                port.notifyOnDataAvailable(true);

                builders[i] = new StringBuilder();

            } catch (PortInUseException | UnsupportedCommOperationException | TooManyListenersException e) {
                e.printStackTrace();
            }
        }
    }

    JsonElement readSerialJSON(int serialIndex) {
        try {
            int objectCount = 0;
            StringBuilder builder = builders[serialIndex];
            InputStream stream = serials[serialIndex].getInputStream();
            boolean found = false;
            while (stream.available() > 0) {
                char c = (char)stream.read();
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
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                String line = builder.toString();
                builder.setLength(0);
                return parser.parse(line);
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void writeSerialJSON(int serialIndex, JsonElement json) {
        try {
            OutputStream outputStream = serials[serialIndex].getOutputStream();
            String data = json.toString() + "\n";
            System.out.print(data);
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void traversePalette(int serialIndex, JsonObject node, HashSet<Integer> found) {
        int id = node.get("i").getAsInt();
        String uuid = node.get("u").getAsString().substring(2);
        Module.Type type = Module.Type.values()[node.get("t").getAsInt()];

        if (!modules.contains(serialIndex, id)) {
            Module module = new Module(this, serialIndex, id, uuid, type);
            modules.put(serialIndex, id, module);
            moduleSet.add(module);
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
            Module mod = modules.get(serialIndex, id);
            mod.removeListeners();
            moduleSet.remove(mod);
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
//            mod.smoothValue();
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

        moduleSet = new ListenableSet<Module>();

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
