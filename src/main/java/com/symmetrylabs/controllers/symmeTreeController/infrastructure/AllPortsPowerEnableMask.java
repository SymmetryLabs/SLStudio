package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.google.gson.annotations.Expose;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.util.hardware.powerMon.ControllerWithPowerFeedback;
import com.symmetrylabs.util.persistance.ClassWriterLoader;
import org.joda.time.DateTime;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;

// persistent port power mask mapped to controllers.  Should be indexed on a per-show basis.
public class AllPortsPowerEnableMask {
    private static String PERSISTENT_PORT_MASK = "persistent-port-mask.json";

    @Expose
    int totalTwigsKilled = 0;

    @Expose
    private final TreeMap<String, Byte> portMaskByControllerHumanID = new TreeMap<>();

    public void loadControllerSetMaskStateTo_RAM(Collection<DiscoverableController> controllerSet) {
        totalTwigsKilled = 0;
        controllerSet.stream().filter(powerController -> powerController instanceof ControllerWithPowerFeedback)
            .map(powerController -> (ControllerWithPowerFeedback) powerController)
            .forEach(this::saveStateByController);
    }

    private void saveStateByController(ControllerWithPowerFeedback controller){
        Byte portMask;
        portMask = filterIntToByte(controller.getLastSample().powerOnStateMask);
        portMaskByControllerHumanID.put(controller.getHumanId(), portMask);

        for (int i = 0; i < 8; i++){
            if ((portMask >> i & 0x1) == 0x1){
                totalTwigsKilled++;
            }
        }
    }

    public void applyStateToController(ControllerWithPowerFeedback controller){
        Byte portMask;
        portMask = portMaskByControllerHumanID.get(controller.getHumanId());
        if (portMask!=null){
            controller.setPortPowerMask(portMask);
        }
    }

    private Byte filterIntToByte(int powerOnStateMask) {
        assert (powerOnStateMask >= 0 && powerOnStateMask <= 0xff);
        return (byte) powerOnStateMask;
    }

    // Apply VolumeApp port masks in memory to controllers
    public void RAM_ApplyMaskAllControllers(Collection<DiscoverableController> controllerSet) {
        controllerSet.stream().filter(powerController -> powerController instanceof ControllerWithPowerFeedback)
            .map(powerController -> (ControllerWithPowerFeedback) powerController)
            .forEach(this::applyStateToController);
    }

    String fmtNow(){
        DateTime now = DateTime.now();
        return now.getYear() + "-" + now.getMonthOfYear() + "-" + now.getDayOfMonth() + "_" + now.getHourOfDay() + "-" + now.getMinuteOfHour();
    }

    public void saveToDisk () throws IOException {
        new ClassWriterLoader<>(PERSISTENT_PORT_MASK, AllPortsPowerEnableMask.class, ApplicationState.showName()).writeObj(this);
        new ClassWriterLoader<>(fmtNow() + PERSISTENT_PORT_MASK, AllPortsPowerEnableMask.class, ApplicationState.showName()).writeObj(this); // save with date
    }

    public void postToFirebase() {

        String filePath = "src/main/resources/" + PERSISTENT_PORT_MASK;
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String fmtdate = fmtNow();

        String json_in_file = contentBuilder.toString();
        String symmetryBaseURL = "https://oslotree-aa3e5.firebaseio.com/";
        String resourceURI = "oslo/"+ fmtdate + ".json";
        String firebaseURL = symmetryBaseURL + resourceURI;

//        try {
//            String cmdString = "curl -X PUT -d " + "'" + json_in_file.replaceAll("\n","") + "' " + firebaseURL;
//
//            Runtime rt = Runtime.getRuntime();
//
//            System.out.println("Using cmd string to post to firebase: \n" + cmdString);
//            Process proc = rt.exec(cmdString);
//
//            BufferedReader stdInput = new BufferedReader(new
//                InputStreamReader(proc.getInputStream()));
//            BufferedReader stdError = new BufferedReader(new
//                InputStreamReader(proc.getErrorStream()));
//
//            // Read the output from the command
//            System.out.println("Response from firebase: \n");
//            String s = null;
//            while ((s = stdInput.readLine()) != null) {
//                System.out.println(s);
//            }
//
//            System.out.println("Here is the standard error of the CURL command (if any):\n");
//            while ((s = stdError.readLine()) != null) {
//                System.out.println(s);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // ... fucking A.. not sure why proc doesnt just execute the string.  Works fine in command line.
        // trying another way.

        // OK this PUT works... goddamn.
        URL url = null;
        try {
            url = new URL(firebaseURL);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
            out.write(json_in_file);
            out.close();
            httpCon.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static AllPortsPowerEnableMask loadFromDisk (){
        return new ClassWriterLoader<>(PERSISTENT_PORT_MASK, AllPortsPowerEnableMask.class).loadObj();
    }
}

