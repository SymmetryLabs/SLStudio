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
        final BooleanParameter pongIsPressed = new BooleanParameter("IsPressed", false);
        final BooleanParameter snakeIsPressed = new BooleanParameter("IsPressed", false);
        final BooleanParameter upIsPressed = new BooleanParameter("IsPressed", false);

        /*
         PONG: r (both controllers)
         SNAKE: f (both controllers)
         RED-UP: w
         RED (DOWN): s
         RED (LEFT): a
         RED (RIGHT): d
         BLUE (UP): i
         BLUE (DOWN): k
         BLUE (LEFT): j
         BLUE (RIGHT): l
         */


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
                            lx.engine.getFocusedLook().focusedChannel.setValue(1);
                        }
                        pongIsPressed.setValue(true);
                        break;
                    case 'f':
                        // Play Snake
                        if (!pong.enabled.isOn() && !snake.enabled.isOn()) {
                            snake.enabled.setValue(true);
                            cycle.enabled.setValue(false);
                            pong.enabled.setValue(false);
                            lx.engine.getFocusedLook().focusedChannel.setValue(0);
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
                    lx.engine.getFocusedLook().focusedChannel.setValue(2);
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
