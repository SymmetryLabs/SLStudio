package com.symmetrylabs.cli;

import com.symmetrylabs.slstudio.server.VolumeClient;
import com.symmetrylabs.slstudio.server.VolumeCore;
import com.symmetrylabs.slstudio.ui.v2.VolumeApplication;

public class CommandLineInterface {
    private VolumeCore core;
    public CommandLineInterface(VolumeClient client){
        this.core = core;
    }

    public void exec(String args) {
        System.out.println(args);
    }
}
