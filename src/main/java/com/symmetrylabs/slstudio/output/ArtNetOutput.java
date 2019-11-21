package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.network.NetworkMonitor;
import heronarts.lx.LX;
import heronarts.lx.output.LXOutputGroup;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.symmetrylabs.util.listenable.SetListener;

public class ArtNetOutput extends LXOutputGroup {
    public final String ipAddress;

    public ArtNetOutput(LX lx, String ipAddress) {
        super(lx);
        this.ipAddress = ipAddress;

//        enabled.setValue(false);
        NetworkMonitor.getInstance(lx).artNetDeviceList.addListenerWithInit(
            new SetListener<InetAddress>() {
                @Override
                public void onItemAdded(InetAddress added) {
                    if (added.getHostAddress().equals(ipAddress)) {
                        ArtNetOutput.this.enabled.setValue(true);
                    }
                }

                @Override
                public void onItemRemoved(InetAddress added) {
                    if (added.getHostAddress().equals(ipAddress)) {
                        ArtNetOutput.this.enabled.setValue(false);
                    }
                }
            });
    }
}
