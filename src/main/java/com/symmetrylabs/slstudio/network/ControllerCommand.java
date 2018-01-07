package com.symmetrylabs.slstudio.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;


public class ControllerCommand {

    public final InetAddress addr;
    public final byte[] command;
    public final int responseSize;

    private ControllerCommandCallback callback;

    ControllerCommand(InetAddress addr, byte[] command) {
        this(addr, command, -1, null);
    }

    ControllerCommand(
        final InetAddress addr, final byte[] command,
        final int responseSize, final ControllerCommandCallback callback
    ) {
        this.addr = addr;
        this.command = command;
        this.responseSize = responseSize;
        this.callback = callback;

        NetworkManager.getInstance().getExecutor().submit(new Runnable() {
            public void run() {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket();
                } catch (SocketException e) {
                    return;
                }
                try {
                    try {
                        socket.setSoTimeout(1000);
                    } catch (SocketException e) {
                        return;
                    }
                    try {
                        socket.send(new java.net.DatagramPacket(command, command.length,
                            new InetSocketAddress(addr, 7890)
                        ));
                    } catch (IOException e) {
                        return;
                    }

                    if (responseSize < 1 || callback == null) return;

                    do {
                        byte[] response = new byte[responseSize];
                        java.net.DatagramPacket packet = new java.net.DatagramPacket(response, response.length);
                        try {
                            socket.receive(packet);
                            if (callback != null) callback.onResponse(packet);
                        } catch (IOException e) {
                            break;
                        }
                    } while (!addr.isMulticastAddress());
                } finally {
                    socket.close();
                    if (callback != null) callback.onFinish();
                }
            }
        });
    }
}
