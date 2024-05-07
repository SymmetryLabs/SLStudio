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

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXRunnableComponent;
import heronarts.lx.modulator.QuadraticEnvelope;

public class MotionSensor extends LXRunnableComponent {

    private LX lx;

    private static MotionSensor instance = null;

    private String ipAddress = null;

    private boolean lastState = false;

    private final List<Listener> listeners = new ArrayList<>();

    private int elapsedMillis = 0;

    private final QuadraticEnvelope fadeIn = new QuadraticEnvelope(0, 1, 1500);
    private final QuadraticEnvelope fadeOut = new QuadraticEnvelope(1, 0, 6000);

    private MotionSensor(LX lx, String ipAddress) {
        super("motionSensor");
        this.lx = lx;
        this.ipAddress = ipAddress;

        boolean keepRunning = true;

    Runnable task = () -> {
        while (keepRunning) {
            if (makeStateRequest()) {  // Assuming makeStateRequest returns a boolean indicating success
              keepRunning = false; // Exit the loop on successful connection
         }
            try {
               Thread.sleep(250);
            } catch (InterruptedException e) {
               keepRunning = false; // Exit loop on interrupt
              Thread.currentThread().interrupt(); // Reset interrupted flag
            }
        }
    };

        // Create a new thread and start it
        Thread thread = new Thread(task);
        thread.start();

        System.out.println("Motion Sensor initialized");
    }

    public static MotionSensor initialize(LX lx, String ipAddress) {
        if (instance == null) {
            instance = new MotionSensor(lx, ipAddress);
        }
        return instance;
    }

    public static MotionSensor getInstance() {
        return instance;
    }

    protected void run(double deltaMs) {
        //System.out.println(elapsedMillis);
        //System.out.println("MotionSensor.loop()");

        fadeIn.run(deltaMs);
        fadeOut.run(deltaMs);

        LXChannel motion = lx.engine.getChannel("motion");
        LXChannel ambient = lx.engine.getChannel("ambient");

        if (elapsedMillis > 5000 && elapsedMillis < 5100 && !fadeOut.isRunning()) {
            fadeOut.trigger();
            System.out.println("trigger fade out");
        }

        if (fadeIn.isRunning()) {
            motion.fader.setValue(fadeIn.getValue());
            ambient.fader.setValue(1 - fadeIn.getValue());

            //System.out.println("In: " + fadeIn.getValue());
        }

        if (fadeOut.isRunning()) {
            if (fadeOut.getValue() < 1) {
                motion.fader.setValue(fadeOut.getValue());
                ambient.fader.setValue(1 - fadeOut.getValue());
            }

            //System.out.println("Out: " + fadeOut.getValue());
        }

        elapsedMillis += deltaMs;

        if (elapsedMillis > 11000) {
            stop();
            // motion.fader.setValue(0);
            // ambient.fader.setValue(1);
            elapsedMillis = 0;
        }
    }

    private void triggerEvent() {
        if (isRunning()) {
            return;
        }

        for (Listener listener : listeners) {
            listener.onMotionDetected();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String formattedDate = formatter.format(date);
        System.out.println("[" + formattedDate + "] Motion Detected!");

        fadeIn.reset();
        fadeOut.reset();
        fadeIn.trigger();
        start();
    }

    private boolean makeStateRequest() {
    if (ipAddress == null) {
        return false;
    }

    HttpURLConnection connection = null;
    try {
        URL url = new URL("http://" + ipAddress + "/getValue");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String valueString = reader.readLine();
                int value = Integer.parseInt(valueString);

                boolean newState = value == 1;
                lastState = newState;
                return true; // Indicate a successful request
            }
        } else {
            System.out.println("Motion Sensor: HTTP response code " + responseCode);
            return false;
        }
    } catch (IOException e) {
        System.out.println("Motion Sensor: Error making HTTP request - " + e.getMessage());
        return false;
    } finally {
        if (connection != null) {
            connection.disconnect();
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
