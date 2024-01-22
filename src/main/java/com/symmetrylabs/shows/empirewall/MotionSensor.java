package com.symmetrylabs.shows.empirewall;

import java.util.*;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;


import heronarts.lx.LXRunnableComponent;
import heronarts.lx.modulator.QuadraticEnvelope;

public class MotionSensor extends LXRunnableComponent {

    private static MotionSensor instance = null;

    private String ipAddress = null;

    private boolean lastState = false;

    private final List<Listener> listeners = new ArrayList<>();

    //Map<String, Float> previousFaders = new HashMap<>();

    private int elapsedMillis = 0;

    private final QuadraticEnvelope fadeIn = new QuadraticEnvelope(0, 1, 2000);
    private final QuadraticEnvelope fadeOut = new QuadraticEnvelope(1, 0, 4000);

    fadeIn.addListener(() => {
        Channel motion = lx.engine.getChannel("motion");
        motion.fader.setValue(fadeIn.getValue());

        Channel ambient = lx.engine.getChannel("ambient");
        ambient.fader.setValue(1 - fadeIn.getValue());
    });

    fadeOut.addListener(() => {
        Channel motion = lx.engine.getChannel("motion");
        motion.fader.setValue(1 - fadeIn.getValue());

        Channel ambient = lx.engine.getChannel("ambient");
        ambient.fader.setValue(fadeIn.getValue());
    });

    private MotionSensor(String ipAddress) {
        super("motionSensor");

        this.ipAddress = ipAddress;

        Runnable task = () => {
            while(true) {
                //if (enabled.isEnabled()) {
                    makeStateRequest();
                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {
                    }
                //}
            }
        };

        // Create a new thread and start it
        Thread thread = new Thread(task);
        thread.start();

        System.out.println("Motion Sensor initialized");
    }

    public static MotionSensor initialize(String ipAddress) {
        if (instance == null) {
            instance = new MotionSensor(ipAddress);
        }
        return instance;
    }

    public static MotionSensor getInstance() {
        return instance;
    }

    protected void run(double deltaMs) {
        if (elapsedMillis += deltaMs > 5000) {
            fadeOut.trigger();
        }
    }

    // private void storeFaderValues() {
    //     previousFaders.clear();

    //     for (Channel channel : lx.engine.getChannels()) {
    //         if (channel.getLabel().equals("motion")) {
    //             continue;
    //         }

    //         previousFaders.put(channel.getLabel(), channel.fader.getValue());
    //     }
    // }

    private void triggerEvent() {
        for (Listener listener : listeners) {
            listener.onMotionDetected();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String formattedDate = formatter.format(date);
        System.out.println("[" + formattedDate + "] Motion Detected!");

        //storeFaderValues();
        fadeIn.trigger();
        //fadeOut.trigger();
    }

    private void makeStateRequest() {
        if (ipAddress == null) {
            return;
        }

        HttpURLConnection connection = null;

        try {
            URL url = new URL("http://" + ipAddress + "/getValue");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000); // 5-second timeout for connecting
            connection.setReadTimeout(2000);   // 5-second timeout for reading response

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String valueString = reader.readLine();
                    int value = Integer.parseInt(valueString);

                    boolean newState = value == 1;
                    if (!lastState && newState) {
                        triggerEvent();
                    }
                    lastState = newState;
                }
            } else {
                System.out.println("Motion Sensor: HTTP response code " + responseCode);
            }

        } catch (IOException e) {
            System.out.println("Motion Sensor: Error making HTTP request - " + e.getMessage());

        } finally {
            if (connection != null) {
                connection.disconnect(); // Ensure disconnection even with errors
            }
        }
    }

    public void addListener(Listener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

    public static interface Listener {
		public void onMotionDetected();
	}
}
