package com.symmetrylabs.shows.empirewall;

import java.util.*;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class MotionSensor {

    private static MotionSensor instance = null;

    private String ipAddress = null;

    private boolean lastState = false;

    private final List<Listener> listeners = new ArrayList<>();

    private MotionSensor(String ipAddress) {
        this.ipAddress = ipAddress;

        Runnable task = () -> {
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

    private void triggerEvent() {
        System.out.println("Motion captured!");

        for (Listener listener : listeners) {
            listener.onMotionDetected();
        }
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
