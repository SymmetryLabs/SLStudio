package com.symmetrylabs.slstudio.aivj;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.LXComponent;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AiVj extends LXComponent {

    public final Recorder recorder;
    public final Player player;
    private final BooleanParameter enabled;

    public AiVj(LX lx) {
        super(lx, "AI VJ");
        this.recorder = new Recorder();
        this.player = new Player();

        String slstudioPath = Paths.get("").toAbsolutePath().toString();
        String userName = System.getProperty("user.name");
        String py = "data_generation";
        String pyWithSpotify = "data_generation_w_spotify";
        String pyRun = "run";
        String cmd = slstudioPath + "/AI_VJ/";

        //this.enabled = lx.engine.output.enabled;
        this.enabled = new BooleanParameter("enabled", false)
            .setDescription("AI VJ Mode: toggle on/off");

        String LOGFILE = "out.txt";
        PrintWriter out;
    /*    enabled.addListener(p -> {
            if (((BooleanParameter)p).isOn()) {
                addChannel();
            }
            else {
                removeChannel();
        });*/

        recorder.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {


                if (((BooleanParameter)parameter).isOn()) {

                    // populate processes that will run scripts natively
                    System.out.println("script runtime: " );
                    System.out.println(Integer.toString(recorder.runtime.getValuei()));
                    System.out.println("/Users/aaronopp/anaconda/envs/py36/bin/python" + cmd + py + ".py" + userName + Integer.toString(recorder.runtime.getValuei()));
                    String[] commandVjRecord = new String[] {"/Users/aaronopp/anaconda/envs/py36/bin/python", cmd + py + ".py", userName, Integer.toString(recorder.runtime.getValuei())};
                    ProcessBuilder pbVjRecord = new ProcessBuilder(commandVjRecord);

                    String[] commandSpotify = new String[] {"/Users/aaronopp/anaconda/envs/py36/bin/python", cmd + pyWithSpotify + ".py", userName, Integer.toString(recorder.runtime.getValuei())};
                    ProcessBuilder pbSpotify = new ProcessBuilder(commandSpotify);

                    String[] commandLogger = new String[] {"processing-java", "--sketch=" + slstudioPath + "/AI_VJ/logger", "--run"};
                    ProcessBuilder pbLogger = new ProcessBuilder(commandLogger);



                    try {

                        if (recorder.generateSpotifyData.isOn()) {

                            System.out.println("spotify status true");
                            pbSpotify.redirectError();
                            Process processSpotify = pbSpotify.start();

                            InputStream is = processSpotify.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is);
                            BufferedReader br = new BufferedReader(isr);
                            String line;
                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                            }



//                            BufferedReader reader =
//                                new BufferedReader(new InputStreamReader(processSpotify.getInputStream()));
//                            StringBuilder builder = new StringBuilder();
//                            String line = null;
//                            while ( (line = reader.readLine()) != null) {
//                                builder.append(line);
//                                builder.append(System.getProperty("line.separator"));
//                            }
//                            String result = builder.toString();
//
//                            System.out.println(result);

                        } else {

                            System.out.println("spotify status false");
                            pbVjRecord.redirectError();
                            Process processDataGeneration = pbVjRecord.start();
//
//                            InputStream is = processDataGeneration.getInputStream();
//                            InputStreamReader isr = new InputStreamReader(is);
//                            BufferedReader br = new BufferedReader(isr);
//                            String line;
//                            while ((line = br.readLine()) != null) {
//                                System.out.println(line);
//                            }
//                            BufferedReader reader =
//                                new BufferedReader(new InputStreamReader(processDataGeneration.getInputStream()));
//                            StringBuilder builder = new StringBuilder();
//                            String line = null;
//                            while ( (line = reader.readLine()) != null) {
//                                builder.append(line);
//                                builder.append(System.getProperty("line.separator"));
//                            }
//                            String result = builder.toString();
//
//                            System.out.println(result);
                        }


                        pbLogger.redirectError();
                        Process processLogger = pbLogger.start();


                    } catch (Exception e) {
                        System.out.println("There was an error starting AI VJ data generation");
                        e.printStackTrace();
                    }
                }
            }
        });

        player.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) {

                    System.out.println("script runtime: " );
                    System.out.println(Integer.toString(player.runtime.getValuei()));

                    String[] commandVjRun = new String[] {"python", cmd + pyRun + ".py", userName, Integer.toString(player.runtime.getValuei())};
                    ProcessBuilder pbVjRun = new ProcessBuilder(commandVjRun);

                    try {
                        pbVjRun.redirectError();
                        Process pVjRun = pbVjRun.start();

                    } catch(Exception e){
                        System.out.println("There was an error starting AI VJ");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public class Recorder {
        public final BooleanParameter isRunning = new BooleanParameter("isRunning", false);
        public final DiscreteParameter runtime = new DiscreteParameter("runtime", 10, 1, 60);
        public final BooleanParameter generateSpotifyData = new BooleanParameter("generateSpotifyData", true);
    }

    public class Player {
        public final BooleanParameter isRunning = new BooleanParameter("isRunning", false);
        public final DiscreteParameter runtime = new DiscreteParameter("runtime", 10, 1, 60);
    }

}
