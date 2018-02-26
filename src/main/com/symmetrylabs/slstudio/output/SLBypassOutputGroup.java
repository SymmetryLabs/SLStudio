package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;

import java.net.DatagramSocket;
import java.net.SocketException;

public class SLBypassOutputGroup extends LXOutputGroup {

    private final int[] bypassColors;

    public SLBypassOutputGroup(LX lx, int[] bypassColors) {
        super(lx);
        this.bypassColors = bypassColors;
    }

    @Override
    public LXOutput send(int[] colors) {
        return super.send(bypassColors);
    }

}
