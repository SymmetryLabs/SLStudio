package com.symmetrylabs.shows.magicleap;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.LX;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.parameter.*;
import heronarts.lx.transform.LXVector;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import static com.symmetrylabs.util.MathUtils.*;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.net.SocketException;
import java.net.UnknownHostException;

public class PongGame<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = MagicLeapShow.SHOW_NAME;

    private LXOscEngine.Transmitter oscTransmitter = null;

    final List<Strip> scoreboardStrips = new ArrayList<>();

    final float MIN_VELOCITY_PER_AXIS = 0.075f;

    final CompoundParameter paddleHeight = new CompoundParameter("padHght", model.yRange*0.15f, model.yRange*0.1f, model.yRange*0.5f);
    final CompoundParameter paddleWidth = new CompoundParameter("padWdth", model.yRange*0.05f, model.yRange*0.01f, model.yRange*0.2f);

    final CompoundParameter ballRadius = new CompoundParameter("ballRad", model.yRange*0.05f, model.yRange*0.025f, model.yRange*0.1f);
    final CompoundParameter ballVelocity = new CompoundParameter("ballVel", 0);
    final BooleanParameter resetBall = new BooleanParameter("resetBall").setMode(BooleanParameter.Mode.MOMENTARY);
    final BooleanParameter autoBallVelocity = new BooleanParameter("autoBall", true);

    final LinearEnvelope ballVelocityDriver = new LinearEnvelope(0, 1, 50000);

    private int redScore = 0;
    private int blueScore = 0;

    private Ball ball = new Ball();
    private RedPaddle redPaddle = new RedPaddle();
    private BluePaddle bluePaddle = new BluePaddle();

    public PongGame(LX lx) {
        super(lx);
        addParameter(paddleHeight);
        addParameter(paddleWidth);
//

        addParameter(redPaddle.position);
        addParameter(bluePaddle.position);
//        addParameter(bluePosition);

        addParameter(ballRadius);
        addParameter(ballVelocity);
        addParameter(resetBall);
        addParameter(autoBallVelocity);

        addModulator(ballVelocityDriver).trigger();

        try {
            this.oscTransmitter = lx.engine.osc.transmitter("localhost", 3344);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        lx.engine.getChannel(0).enabled.addListener(parameter -> {
            // getChannel() returns null?
            if (getChannel().enabled.isOn()) {
                reset();
                System.out.println("Reset Pong Game");
            }
        });
    }

    public void run(double deltaMs) {
        setColors(0);
        ball.run();
        redPaddle.run();
        bluePaddle.run();
        drawScoreboard();
    }

    public void reset() {
        redScore = 0;
        blueScore = 0;
        ball.reset();
        bluePaddle.position.setValue(0.5f);
        redPaddle.position.setValue(0.5f);
    }

    void scoreRed() {
        redScore++;

        if (redScore >= 3) {
            triggerVictory(LXColor.RED);
        }
        System.out.println("red score: " + redScore + ", blueScore: " + blueScore);
    }

    void scoreBlue() {
        blueScore++;

        if (blueScore >= 3) {
            triggerVictory(LXColor.BLUE);
        }
        System.out.println("red score: " + redScore + ", blueScore: " + blueScore);
    }

    void triggerVictory(int col) {
        redScore = 0;
        blueScore = 0;
        reset();

        if (oscTransmitter != null) {
            try {
                OscMessage message1 = new OscMessage("/lx/channel/Pong/enabled").add(0);
                oscTransmitter.send(message1);

                OscMessage message2 = new OscMessage("/lx/channel/Cycle/enabled").add(1);
                oscTransmitter.send(message2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void drawScoreboard() {
        if (scoreboardStrips.size() == 0) {
            for (Strip strip : model.getStrips()) {
                if (Math.abs(strip.cy - model.yMax) < 2 && Math.abs(strip.cz - model.zMin) < 1) {
                    scoreboardStrips.add(strip);
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            if (i < redScore) {
                for (LXPoint p : scoreboardStrips.get(i).points) {
                    colors[p.index] = LXColor.RED;
                }
            }

            if (i < blueScore) {
                for (LXPoint p : scoreboardStrips.get(5-i).points) {
                    colors[p.index] = LXColor.BLUE;
                }
            }
        }
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        switch (keyChar) {
            case 'w': redPaddle.velocity = 0.01f; break;
            case 's': redPaddle.velocity = -0.01f; break;
        }
    }

    public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
        switch (keyChar) {
            case 'w':    redPaddle.velocity = 0; break;
            case 's': bluePaddle.velocity = 0; break;
        }
    }

    class RedPaddle {
        final CompoundParameter position = new CompoundParameter("redPos", 0.5f);
        float velocity = 0;

        void run() {
            float yPosVal = (model.yRange - paddleHeight.getValuef()) * position.getValuef();
            draw(yPosVal);
            detectCollision(yPosVal);
            position.setValue(position.getValue() + velocity);
        }

        void draw(float yPosVal) {
            for (LXPoint p : model.points) {
                if (p.x < model.xMin + paddleWidth.getValuef()
                    && p.y > yPosVal && p.y < yPosVal + paddleHeight.getValuef()) {
                    colors[p.index] = LXColor.RED;
                }
            }
        }

        void detectCollision(float yPosVal) {
            if (ball.position.y > yPosVal && ball.position.y < yPosVal + paddleHeight.getValuef()) {
                if (ball.position.x < paddleWidth.getValuef()) {
                    ball.velocity.x = ball.velocity.x * -1;
                }
            } else {
                if (ball.position.x < paddleWidth.getValuef()) {
                    ball.velocity.y = ball.velocity.y * -1;
                }
            }
        }
    }

    class BluePaddle {
        final CompoundParameter position = new CompoundParameter("bluePos", 0.5f);
        float velocity = 0;

        void run() {
            float yPosVal = (model.yRange - paddleHeight.getValuef()) * position.getValuef();
            draw(yPosVal);
            detectCollision(yPosVal);
            position.setValue(position.getValue() + velocity);
        }

        void draw(float yPosVal) {
            for (LXPoint p : model.points) {
                if (p.x > model.xMax - paddleWidth.getValuef()
                    && p.y > yPosVal && p.y < yPosVal + paddleHeight.getValuef()) {
                    colors[p.index] = LXColor.BLUE;
                }
            }
        }

        void detectCollision(float yPosVal) {
            //for (LXPoint p : model.points) {
            if (ball.position.y > yPosVal && ball.position.y < yPosVal + paddleHeight.getValuef()) {
                if (ball.position.x > model.xMax - paddleWidth.getValuef()) {
                    ball.velocity.x = ball.velocity.x * -1;
                }
            } else {
                if (ball.position.x > model.xMax - paddleWidth.getValuef()) {
                    ball.velocity.y = ball.velocity.y * -1;
                }
            }
            //}
        }
    }


    class Ball {
        final float MIN_VELOCITY = 0.5f;
        final float VELOCITY_SCALAR = 5f;

        LXVector position = new LXVector(model.cx, model.cy, 0);
        LXVector velocity = new LXVector(random(-1, 1), random(-1, 1), 0).normalize();

        Ball() {
            ballVelocity.addListener((parameter) -> {
                setVelocity();
            });

            resetBall.addListener((parameter) -> {
                if (((BooleanParameter)parameter).isOn()) {
                    reset();
                }
            });
        }

        void setVelocity() {
            ball.velocity.normalize().mult(MIN_VELOCITY + ballVelocity.getValuef()*VELOCITY_SCALAR);
        }

        void reset() {
            if (autoBallVelocity.isOn()) {
                ballVelocityDriver.trigger();
            }
            position.set(model.cx, model.cy);
            velocity.set(random(-1, 1), random(-1, 1)).normalize();
            setVelocity();
        }

        void run() {
            if (autoBallVelocity.isOn()) {
                ballVelocity.setValue(ballVelocityDriver.getValuef());
            }

            position.add(velocity);

            if (abs(velocity.y) < MIN_VELOCITY_PER_AXIS) {
                velocity.y = (velocity.y > 0) ? MIN_VELOCITY_PER_AXIS : -MIN_VELOCITY_PER_AXIS;
            }
            if (abs(velocity.x) < MIN_VELOCITY_PER_AXIS) {
                velocity.x = (velocity.x > 0) ? MIN_VELOCITY_PER_AXIS : -MIN_VELOCITY_PER_AXIS;
            }

            if (position.y + ballRadius.getValuef() > model.yMax
                || position.y - ballRadius.getValuef() < model.yMin) {
                velocity.y = velocity.y * -1;
            }
            if (position.x - ballRadius.getValuef()*2 > model.xMax) {
                scoreRed();
                this.reset();
            }
            if (position.x + ballRadius.getValuef()*2 < model.xMin) {
                scoreBlue();
                this.reset();
            }

            for (LXPoint p : model.points) {
                if (dist(p.x, p.y, position.x, position.y) < ballRadius.getValuef()) {
                    colors[p.index] = LXColor.WHITE;
                }
            }
        }
    }
}

