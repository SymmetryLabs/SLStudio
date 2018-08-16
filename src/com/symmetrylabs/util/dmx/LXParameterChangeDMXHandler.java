package com.symmetrylabs.util.dmx;

import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;

public class LXParameterChangeDMXHandler implements DMXHandler {

    private DMXStream stream;
    private final int dmxIndex;
    private final LXParameter parameter;

    public LXParameterChangeDMXHandler(int dmxIndex, LXParameter parameter) {
        this.dmxIndex = dmxIndex;
        this.parameter = parameter;
        if (parameter instanceof LXListenableParameter) {
            ((LXListenableParameter) parameter).addListener(parameter1 -> updateValue());
        }
    }

    private double getRawParameterValue() {
        if (parameter instanceof LXNormalizedParameter) {
            return ((LXNormalizedParameter) parameter).getNormalized();
        } else {
            return parameter.getValue();
        }
    }

    private void setRawParameterValue(double value) {
        if (parameter instanceof LXNormalizedParameter) {
            ((LXNormalizedParameter) parameter).setNormalized(value);
        } else {
            parameter.setValue(value);
        }
    }

    private void updateValue() {
        if (stream != null) {
            stream.data[dmxIndex] = (int) Math.round(getRawParameterValue() * 255);
        }
    }

    @Override
    public void onDMXStreamChanged(DMXStream stream) {
        this.stream = stream;
        updateValue();
    }

    @Override
    public void onDMXDataReceived(DMXStream stream) {
        int value = stream.data[dmxIndex];
        double normalized = value / 255.0;
        if (getRawParameterValue() != normalized) {
            setRawParameterValue(normalized);
        }
    }
}