package com.symmetrylabs.slstudio.effect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.json.JSONObject;

public class SunsetTool {

    public static boolean sunset() {




        HttpURLConnection connection;
        URL url;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();


        try {
            url = new URL("https://api.sunrise-sunset.org/json?lat=37.7956&lng=-122.3933");
            try {
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();

                if (status > 299) {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);

                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                }
//                    System.out.println(responseContent.toString());
                      sunsetend(responseContent.toString());

                Date time = new Date();
//                String sunsetend = sunsetend(responseContent.toString());

                DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa");
                dateFormat.format(time);
                System.out.println("Todays current time: " + dateFormat.format(time));

                try {
                    if (dateFormat.parse(dateFormat.format(time)).after(dateFormat.parse(parse(responseContent.toString())))
                    &&  dateFormat.parse(dateFormat.format(time)).before(dateFormat.parse(sunsetend(responseContent.toString()))))
                        {
                        System.out.println("Current time is during sunset today");
                        return true;
                    } else {
                        System.out.println("Current time is not during sunset today");
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String sunsetend(String responseBody) {

        JSONObject sunsettime = new JSONObject(responseBody);
        JSONObject resultsobject = sunsettime.getJSONObject("results");
        String SunsetendString = resultsobject.getString("nautical_twilight_end");
        System.out.println("Todays Sunset End Time: " + SunsetendString);
        return SunsetendString;


    }


    public static String parse(String responseBody) {

        JSONObject sunsettime = new JSONObject(responseBody);
        JSONObject resultsobject = sunsettime.getJSONObject("results");
        String SunsetTimeString = resultsobject.getString("sunset");
        System.out.println("Todays Sunset Time: " + SunsetTimeString);
        return SunsetTimeString;


    }
}
