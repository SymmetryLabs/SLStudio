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