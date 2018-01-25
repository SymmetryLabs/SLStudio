package com.symmetrylabs.slstudio.aivj;

import heronarts.lx.LX;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;

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
        String[] command = new String[] {"python" ,cmd + pyWithSpotify + ".py", userName, "5" };
        ProcessBuilder pb = new ProcessBuilder(command);

        String[] commandLogger = new String[] {"processing-java", "--sketch=" + slstudioPath + "/AI_VJ/logger", "--run"};
        ProcessBuilder pbLogger = new ProcessBuilder(commandLogger);

        String[] commandVjRun = new String[] {"python", cmd + pyRun + ".py", userName, "5"};
        ProcessBuilder pbVjRun = new ProcessBuilder(commandVjRun);

        String LOGFILE = "out.txt";
        PrintWriter out;

        recorder.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) {
                    try {
                        pb.redirectError();
                        Process p = pb.start();
                        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        pbLogger.redirectError();
                        Process pLogger = pbLogger.start();
                        BufferedReader inLogger = new BufferedReader(new InputStreamReader(pLogger.getInputStream()));

                    } catch (Exception e) {
                        System.out.println("There was an error starting AI VJ data generation");
                        e.printStackTrace();
                    }
                ]
            }
        });

        player.isRunning.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) {
                    try {
                        pbVjRun.redirectError();
                        Process pVjRun = pbVjRun.start();
                        BufferedReader inVjRun = new BufferedReader(new InputStreamReader(pVjRun.getInputStream()));

                        for (Integer y = 0; y < 50; y++) {
                            String ret = inVjRun.readLine();
                        };

                    } catch(Exception e){
                        System.out.println("There was an error starting AI VJ");
                        e.printStackTrace();
                    }
                }
            }
        });

                new UIButton(0, 0, ADD_BUTTON_WIDTH, 16) {
                    @Override
                    public void onToggle(boolean on) {
                        if (on) {
                            System.out.println("VJ run button pressed");

                            try {

                                pbVjRun.redirectError();
                                Process pVjRun = pbVjRun.start();
                                BufferedReader inVjRun = new BufferedReader(new InputStreamReader(pVjRun.getInputStream()));


                                for (Integer y = 0; y < 50; y++) {
                                    String ret = inVjRun.readLine();
                                    System.out.println("value is: "+ ret);
                                };

                                //String ret2 = in.readLine();
                                System.out.println("done!");
                            }
                            catch(IOException ioe){
                                ioe.printStackTrace();
                            }
                        } //  IF OFF
                        //else if {}
                        // pb.destroy();

                    }
                }
                    .setLabel("Run")
                    .setInactiveColor(ui.theme.getDeviceBackgroundColor())
                    .setBorderRounding(4)
                    .setDescription("start generating data for training AI VJ")
                    .addToContainer(bar);

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