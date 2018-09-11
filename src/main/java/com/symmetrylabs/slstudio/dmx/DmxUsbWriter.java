package com.symmetrylabs.slstudio.dmx;

import com.fazecast.jSerialComm.SerialPort;

public class DmxUsbWriter implements AutoCloseable {
    private static final byte DMX_PRO_MESSAGE_START = (byte)0x7E;
    private static final byte DMX_PRO_MESSAGE_END = (byte)0xE7;
    private static final byte DMX_PRO_SEND_PACKET = (byte)6;
    private static final int DMX_PRO_MAX_MESSAGE_SIZE = 6 + 512;
    private static final int DMX_PRO_BAUD_RATE = 115200;

    private SerialPort port = null;
    private int maxChannel = -1;

    byte[] message = new byte[DMX_PRO_MAX_MESSAGE_SIZE];

    private SerialPort guessSerialPort() {
        for (SerialPort port : SerialPort.getCommPorts()) {
            if (port.getPortDescription().startsWith("DMX")) {
                return port;
            }
        }

        return null;
    }

    private SerialPort serialPortByName(String serialPortName) {
        for (SerialPort port : SerialPort.getCommPorts()) {
            if (serialPortName.equals(port.getSystemPortName())) {
                return port;
            }
        }

        return null;
    }

    private void setupFrame() {
        //System.out.println("Setting up frame, maxChannel = " + maxChannel);
        int dataSize = maxChannel + 2;
        message[0] = DMX_PRO_MESSAGE_START;
        message[1] = DMX_PRO_SEND_PACKET;
        message[2] = (byte)(dataSize & 255);
        message[3] = (byte)((dataSize >> 8) & 255);
        message[4] = 0;
        message[maxChannel + 6] = DMX_PRO_MESSAGE_END;
    }

    public DmxUsbWriter() {
        port = guessSerialPort();

        if (port != null) {
            System.out.println("Guessed DMX USB device: " + port.getSystemPortName()
                    + " | " + port.getPortDescription());
            port.openPort();
        }
        else {
            System.out.println("WARNING: Unable to guess DMX USB device.");
        }

        setupFrame();
    }

    public DmxUsbWriter(String serialPortName) {
        port = serialPortByName(serialPortName);

        if (port != null) {
            System.out.println("Found DMX USB device: " + port.getSystemPortName()
                    + " | " + port.getPortDescription());
            port.openPort();
        }
        else {
            System.out.println("WARNING: Unable to find DMX USB device '" + serialPortName + "'.");
        }

        setupFrame();
    }


    public void setUniverseNumber(int universeNumber) {
    }

    public void setChannelData(int channel, int value) {
        if (channel > maxChannel) {
            maxChannel = channel;
            setupFrame();
        }

        message[channel + 5] = (byte)value;
    }

    public void setChannelData(int firstChannel, int[] values) {
        int channel = firstChannel;

        for (int value : values) {
            setChannelData(channel, value);
            ++channel;
        }
    }

    public synchronized void send() {
        if (port == null) {
            System.out.println("WARNING: DMX serial port is null.");
            return;
        }

        if (!port.isOpen()) {
            System.out.println("WARNING: DMX serial port is closed.");
            return;
        }

        port.writeBytes(message, 7 + maxChannel);
    }

    @Override
    public synchronized void close() {
        if (port != null) {
            port.closePort();
            port = null;
        }
    }
}
