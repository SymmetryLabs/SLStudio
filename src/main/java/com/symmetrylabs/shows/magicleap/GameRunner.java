package com.symmetrylabs.shows.magicleap;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.LXChannel;
import heronarts.p3lx.ui.UIEventHandler;

import java.net.SocketException;
import heronarts.p3lx.ui.UIEventHandler;
import processing.event.KeyEvent;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

public class GameRunner {

    public GameRunner(SLStudioLX lx) {
//        try {
//            lx.engine.osc.receiver(3344).addListener(message -> {
//                try {
//                    String[] parts = message.getAddressPattern().getValue().split("/");
//
//                    System.out.println(message.getAddressPattern());
//
////                    if (parts[1].equals("lx")) {
////                        if (parts[2].equals("snake")) {
////                            int enabled = message.getInt();
////                            lx.engine.getChannel("Snake").enabled.setValue(enabled);
////                        }
////                    }
//
//                } catch (Exception x) {
//                    System.err.println("[OSC] No route for message: " + message.getAddressPattern().getValue());
//                }
//            });
//        } catch (SocketException e) {
//            throw new RuntimeException(e);
//        }

        final BooleanParameter pongIsPressed = new BooleanParameter("IsPressed", false);
        final BooleanParameter snakeIsPressed = new BooleanParameter("IsPressed", false);
        final BooleanParameter upIsPressed = new BooleanParameter("IsPressed", false);

        lx.ui.addEventHandler(new UIEventHandler() {
            public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {

                LXChannel snake = lx.engine.getChannel("Snake");
                LXChannel pong = lx.engine.getChannel("Pong");
                LXChannel cycle = lx.engine.getChannel("Cycle");

                switch (keyChar) {
                    case 'r':
                        // Play Pong
                        if (!pong.enabled.isOn() && !snake.enabled.isOn()) {
                            pong.enabled.setValue(true);
                            snake.enabled.setValue(false);
                            cycle.enabled.setValue(false);
                            lx.engine.focusedChannel.setValue(1);
                        }
                        pongIsPressed.setValue(true);
                        break;
                    case 'f':
                        // Play Snake
                        if (!pong.enabled.isOn() && !snake.enabled.isOn()) {
                            snake.enabled.setValue(true);
                            cycle.enabled.setValue(false);
                            pong.enabled.setValue(false);
                            lx.engine.focusedChannel.setValue(0);
                        }
                        snakeIsPressed.setValue(true);
                        break;
                    case 'w':
                        upIsPressed.setValue(true);
                        break;
                }
            }

            public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {

                LXChannel snake = lx.engine.getChannel("Snake");
                LXChannel pong = lx.engine.getChannel("Pong");
                LXChannel cycle = lx.engine.getChannel("Cycle");

                if (pongIsPressed.isOn() && snakeIsPressed.isOn() && upIsPressed.isOn()) {
                    snake.enabled.setValue(false);
                    pong.enabled.setValue(false);
                    cycle.enabled.setValue(true);
                    lx.engine.focusedChannel.setValue(2);
                }

                switch (keyChar) {
                    case 'r':
                        pongIsPressed.setValue(false);
                        break;
                    case 'f':
                        snakeIsPressed.setValue(false);
                        break;
                    case 'w':
                        upIsPressed.setValue(false);
                        break;
                }
            }
        });
    }
}
