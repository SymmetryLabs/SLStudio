package com.symmetrylabs.slstudio.envelop;

import heronarts.lx.LX;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

import static com.symmetrylabs.util.MathConstants.HALF_PI;
import static com.symmetrylabs.util.MathConstants.PI;

public class EnvelopOscListener implements LXOscListener {
    private Envelop envelop;
    private LX lx;

    public EnvelopOscListener(LX lx, Envelop envelop) {
        this.lx = lx;
        this.envelop = envelop;
    }

    private static int getIndex(OscMessage message) {
        String[] parts = message.getAddressPattern().toString().split("/");
        try {
            return Integer.valueOf(parts[parts.length - 1]);
        } catch (Exception x) {
            return -1;
        }
    }

    public void oscMessage(OscMessage message) {
        if (message.matches("/envelop/tempo/beat")) {
            lx.tempo.trigger(message.getInt()-1);
        } else if (message.matches("/envelop/tempo/bpm")) {
            lx.tempo.setBpm(message.getDouble());
        } else if (message.matches("/envelop/meter/decode")) {
            envelop.decode.setLevels(message);
        } else if (message.hasPrefix("/envelop/meter/source")) {
            int index = getIndex(message) - 1;
            if (index >= 0 && index < envelop.source.channels.length) {
                envelop.source.setLevel(index, message);
            }
        } else if (message.hasPrefix("/envelop/source")) {
            int index = getIndex(message) - 1;
            if (index >= 0 && index < envelop.source.channels.length) {
                Envelop.Source.Channel channel = envelop.source.channels[index];
                float rx = 0, ry = 0, rz = 0;
                String type = message.getString();
                if (type.equals("xyz")) {
                    rx = message.getFloat();
                    ry = message.getFloat();
                    rz = message.getFloat();
                } else if (type.equals("aed")) {
                    float azimuth = message.getFloat() / 180.f * PI;
                    float elevation = message.getFloat() / 180.f * PI;
                    float radius = message.getFloat();
                    rx = radius * (float)Math.cos(-azimuth + HALF_PI) * (float)Math.cos(elevation);
                    ry = radius * (float)Math.sin(-azimuth + HALF_PI) * (float)Math.cos(elevation);
                    rz = radius * (float)Math.sin(elevation);
                }
                channel.xyz.set(rx, ry, rz);
                channel.active = true;
                channel.tx = lx.model.cx + rx * lx.model.xRange/2;
                channel.ty = lx.model.cy + rz * lx.model.yRange/2;
                channel.tz = lx.model.cz + ry * lx.model.zRange/2;
            }
        }
    }
}
