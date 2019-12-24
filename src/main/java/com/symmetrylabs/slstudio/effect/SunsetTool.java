package com.symmetrylabs.slstudio.effect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.json.JSONObject;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SunsetTool {

    private static ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);

    public static void sunsetChecker() {
        final boolean isItSunset = SunsetTool.sunset();
        final Runnable checker = new Runnable() {
            public void run() {
                if (!isItSunset){
                    System.out.println("not sunset time");
                }
                else {
                    System.out.println("it is currently sunset time");
                }
            }
        };
        final ScheduledFuture<?> beeperHandle =
            scheduler.scheduleAtFixedRate(checker, 1, 1, SECONDS);

    }

    public static boolean sunset() {




        HttpURLConnection connection;
        URL url;
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        int timeOffsetUTC = 0;

        try {

            Date time = new Date(System.currentTimeMillis() +  (3600000 * timeOffsetUTC));
//            Date reLoadStart = new Date(60);
//            Date reLoadEnd = new Date (1000 * 60 *5);
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa");
//            System.out.println(dateFormat.format(reLoadEnd));
//            System.out.println(dateFormat.format(reLoadStart));


//            /TODO only check the sunrise time once a day
//            if(dateFormat.parse(dateFormat.format(time)).after(dateFormat.parse(dateFormat.format(reLoadStart))) &&
//              dateFormat.parse(dateFormat.format(time)).before(dateFormat.parse(dateFormat.format(reLoadEnd))))
//            {

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


                try {
                    Date sunSetOffset = new Date(String.valueOf(dateFormat.parse(sunSetTime(responseContent.toString()))));
                    dateFormat.format(sunSetOffset);
                    dateFormat.format(time);
                    sunSetOffset.setMinutes(sunSetOffset.getMinutes() - 45);

                    Date sunSetEndOffset = new Date(String.valueOf(dateFormat.parse(sunSetTime(responseContent.toString()))));
                    dateFormat.format(sunSetEndOffset);
                    sunSetOffset.setMinutes(sunSetEndOffset.getMinutes() - 45);



                    System.out.println("Todays current time: " + dateFormat.format(time));
                // Check to see if the current time falls within the sunset time
                try {
                    if (dateFormat.parse(dateFormat.format(time)).after(dateFormat.parse(dateFormat.format(sunSetOffset)))
                        &&  dateFormat.parse(dateFormat.format(time)).before(dateFormat.parse(dateFormat.format(sunSetEndOffset))))
                    {
                        System.out.println("Current time is during Sunset Today");
                        System.out.println("Sunset offset is " + dateFormat.format(sunSetOffset));
                        System.out.println("Sunset End offset is " + dateFormat.format(sunSetEndOffset));

                        return true;
                    } else {
                        System.out.println("Current time is not during Sunset Today");
                        System.out.println("Sunset offset is " + dateFormat.format(sunSetOffset));
                        System.out.println("Sunset End offset is " + dateFormat.format(sunSetEndOffset));


                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                } catch (ParseException e) {
                    e.printStackTrace();
                }




            } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String sunSetEnd(String responseBody) {

        JSONObject sunsettime = new JSONObject(responseBody);
        JSONObject resultsobject = sunsettime.getJSONObject("results");
        String SunsetendString = resultsobject.getString("nautical_twilight_end");
        System.out.println("Todays Sunset End Time: " + SunsetendString);
        return SunsetendString;


    }


    public static String sunSetTime(String responseBody) {

        JSONObject sunsettime = new JSONObject(responseBody);
        JSONObject resultsobject = sunsettime.getJSONObject("results");
        String SunsetTimeString = resultsobject.getString("sunset");
        System.out.println("Todays Sunset Time: " + SunsetTimeString);
        return SunsetTimeString;


    }

    public static void main (String[] args){
        sunsetChecker();
    }
}
