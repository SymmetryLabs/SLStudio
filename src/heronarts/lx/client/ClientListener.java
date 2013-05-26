/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.client;


import heronarts.lx.HeronLX;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class ClientListener {

    private static final int MAX_PACKET_LEN = 256;
    private static final int DEFAULT_PORT = 9001;
    
    private static final String PROTOCOL_TERMINATOR = ";";
    private static final String PROTOCOL_DELIMITER = ",";

    private final HashSet<HeronLX> listeners;
    
    private final DatagramChannel channel;
    private final ByteBuffer buffer;
    private final ClientTouch touch;
    
    private static ClientListener instance = null; 
    
    public static ClientListener getInstance() {
        if (ClientListener.instance == null) {
            ClientListener.instance = new ClientListener();
        }
        return ClientListener.instance;
    }
    
    private ClientListener() {
        this(null, DEFAULT_PORT);
    }
    
    private ClientListener(String ip, int port) {
        this.buffer = ByteBuffer.allocate(MAX_PACKET_LEN);
        this.touch = new ClientTouch();
        this.listeners = new HashSet<HeronLX>();
        try {
            this.channel = DatagramChannel.open();
            if (ip != null) {
                this.channel.socket().bind(new InetSocketAddress(ip, port));
            } else {
                this.channel.socket().bind(new InetSocketAddress(port));
            }
            this.channel.configureBlocking(false);
        } catch (IOException iox) {
            throw new RuntimeException("Could not bind DatagramChannel to " + ip + ":" + port, iox);
        }
    }

    public void addListener(HeronLX lx) {
        this.listeners.add(lx);
    }
    
    public void removeListener(HeronLX lx) {
        this.listeners.remove(lx);
    }
    
    public ClientTouch touch() {
        return this.touch;
    }
    
    private void handlePacket(String[] parts) {
        try {
            String command = parts[0];
            if (command.equals("setBaseHue")) {
                for (HeronLX lx : this.listeners) {
                    lx.setBaseHue(Integer.parseInt(parts[1]));
                }
            } else if (command.equals("setBrightness")) {
                for (HeronLX lx : this.listeners) {
                    double bVal = Double.parseDouble(parts[1]);
                    lx.setBrightness(bVal);
                }
            } else if (command.equals("goPrev")) {
                for (HeronLX lx : this.listeners){
                    lx.goPrev();
                }
            } else if (command.equals("goNext")) {
                for (HeronLX lx : this.listeners) {
                    lx.goNext();
                }
            } else if (command.equals("flash")) {
                for (HeronLX lx : this.listeners) {
                    lx.flash();
                }
            } else if (command.equals("touchBegan")) {
                this.touch.setActive(true);
                this.touch.setX(Double.parseDouble(parts[1]));
                this.touch.setY(Double.parseDouble(parts[2]));
            } else if (command.equals("touchMoved")) {
                this.touch.setActive(true);
                this.touch.setX(Double.parseDouble(parts[1]));
                this.touch.setY(Double.parseDouble(parts[2]));
            } else if (command.equals("touchEnded")) {
                this.touch.setActive(false);
            } else {
                System.out.println("Unknown protocol command: " + parts[0]);
            }
        } catch (Exception x) {
            System.out.println(x);
        }
    }
    
    public void listen() {
        try {
            this.buffer.clear();
            while (this.channel.receive(buffer) != null) {
                String bufString = new String(this.buffer.array(), 0, this.buffer.position());
                int fromIndex = 0;
                int semiIndex;
                while ((semiIndex = bufString.indexOf(PROTOCOL_TERMINATOR, fromIndex)) != -1) {
                    this.handlePacket(bufString.substring(fromIndex, semiIndex).split(PROTOCOL_DELIMITER));
                    fromIndex = semiIndex + 1;
                }
                if (fromIndex < bufString.length()) {
                    System.out.println("Dangling data left in packet: " + bufString);
                }
                this.buffer.clear();
            }
        } 
        catch (IOException iox) {
            System.out.println(iox);
        }
    }
}
