package com.symmetrylabs.slstudio.aivj;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;

public class AiVj {

    public final Recorder recorder;
    public final Player player;

    public AiVj() {
        this.recorder = new Recorder();
        this.player = new Player();

        String slstudioPath = Paths.get("").toAbsolutePath().toString();
        String userName = System.getProperty("user.name");
        String py = "data_generation";
        String pyWithSpotify = "data_generation_w_spotify";
        String pyRun = "run";
        String cmd = slstudioPath + "/AI_VJ/";




        String LOGFILE = "out.txt";
        PrintWriter out;

        recorder.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) {

                    // populate processes that will run scripts natively
                    System.out.println("script runtime: " );
                    System.out.println(Integer.toString(recorder.runtime.getValuei()));

                    String[] commandVjRecord = new String[] {"python", cmd + py + ".py", userName, Integer.toString(recorder.runtime.getValuei())};
                    ProcessBuilder pbVjRecord = new ProcessBuilder(commandVjRecord);

                    String[] commandSpotify = new String[] {"python", cmd + pyWithSpotify + ".py", userName, Integer.toString(recorder.runtime.getValuei())};
                    ProcessBuilder pbSpotify = new ProcessBuilder(commandSpotify);

                    String[] commandLogger = new String[] {"processing-java", "--sketch=" + slstudioPath + "/AI_VJ/logger", "--run"};
                    ProcessBuilder pbLogger = new ProcessBuilder(commandLogger);



                    try {

                        if (recorder.generateSpotifyData.isOn()) {

                            System.out.println("spotify status true");
                            pbSpotify.redirectError();
                            Process processSpotify = pbSpotify.start();


                        } else {

                            System.out.println("spotify status false");
                            pbVjRecord.redirectError();
                            Process processDataGeneration = pbVjRecord.start();

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
