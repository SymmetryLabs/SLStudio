package com.symmetrylabs.slstudio.ui;

import com.symmetrylabs.slstudio.model.NissanCar;
import com.symmetrylabs.slstudio.pattern.raven.P3CubeMapPattern;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import org.apache.commons.math3.util.FastMath;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class UICubeMapDebug extends UI3dComponent {
    private final P3LX lx;

    public UICubeMapDebug(P3LX lx) {
        setVisible(false);
        this.lx = lx;
    }

    protected void onDraw(final UI ui, final PGraphics pg) {
        lx.engine.channels.stream()
            .filter(new Predicate<LXChannel>() {
                @Override
                public boolean test(final LXChannel channel) {
                    return channel.enabled.getValueb() && channel.fader.getValue() > 0;
                }
            })
            .map(new Function<LXChannel, LXPattern>() {
                @Override
                public LXPattern apply(final LXChannel channel) {
                    return channel.getActivePattern();
                }
            })
            .forEach(new Consumer<LXPattern>() {
                @Override
                public void accept(final LXPattern lxPattern) {
                    if (lxPattern instanceof P3CubeMapPattern) {
                        UICubeMapDebug.this.drawCubeFor((P3CubeMapPattern) lxPattern, pg);
                    }
                }
            });
    }

    private void drawCubeFor(final P3CubeMapPattern pattern, final PGraphics pg) {
        if (pattern.allCarsParams.getValueb()) {
//      for (final LXPoint point : pattern.model.points) {
//        drawPoint(pg, point);
//      }

            renderBox(pattern, pg, pattern.origin, pattern.bboxSize);
        } else {
            for (final NissanCar car : pattern.getModel().getCars()) {
                int carIndex = pattern.getModel().getCars().indexOf(car);

                if (pattern.carSwitchParams.get(carIndex).getValueb()) {
                    //sun.computeBoundingBox();
                    //println((sunIndex + 1) + ": " + sun.boundingBox.size);

//          for (final LXPoint point : sun.points) {
//            drawPoint(pg, point);
//          }

                    renderBox(pattern, pg, pattern.originForCar(car), pattern.bboxForCar(car));
                }
            }
        }
    }

    private void drawPoint(final PGraphics pg, LXPoint v) {
        pg.strokeWeight(10);

        pg.stroke(255, 255, 0);
        pg.point(v.x, v.y, v.z);
    }

    private void renderBox(final P3CubeMapPattern pattern, final PGraphics pg, final PVector origin, final PVector bbox) {
        final float cubeSize = FastMath.max(bbox.x, FastMath.max(bbox.y, bbox.z));

        int imageWidth = pattern.pgB.width;
        int imageHeight = pattern.pgB.height;

        pg.tint(255, 126);
        pg.strokeWeight(0);

        pg.pushMatrix();
        pg.translate(origin.x, origin.y, origin.z);

        // Front
        pg.beginShape();
        pg.texture(pattern.pgF);
        pg.vertex(-cubeSize / 2, cubeSize / 2, -cubeSize / 2, 0, 0);
        pg.vertex(cubeSize / 2, cubeSize / 2, -cubeSize / 2, imageWidth, 0);
        pg.vertex(cubeSize / 2, -cubeSize / 2, -cubeSize / 2, imageWidth, imageHeight);
        pg.vertex(-cubeSize / 2, -cubeSize / 2, -cubeSize / 2, 0, imageHeight);
        pg.endShape();

        // Back
        pg.beginShape();
        pg.texture(pattern.pgB);
        pg.vertex(cubeSize / 2, cubeSize / 2, cubeSize / 2, 0, 0);
        pg.vertex(-cubeSize / 2, cubeSize / 2, cubeSize / 2, imageWidth, 0);
        pg.vertex(-cubeSize / 2, -cubeSize / 2, cubeSize / 2, imageWidth, imageHeight);
        pg.vertex(cubeSize / 2, -cubeSize / 2, cubeSize / 2, 0, imageHeight);
        pg.endShape();

        // Down
        pg.beginShape();
        pg.texture(pattern.pgD);
        pg.vertex(-cubeSize / 2, cubeSize / 2, -cubeSize / 2, 0, 0);
        pg.vertex(cubeSize / 2, cubeSize / 2, -cubeSize / 2, imageWidth, 0);
        pg.vertex(cubeSize / 2, cubeSize / 2, cubeSize / 2, imageWidth, imageHeight);
        pg.vertex(-cubeSize / 2, cubeSize / 2, cubeSize / 2, 0, imageHeight);
        pg.endShape();

        // Up
        pg.beginShape();
        pg.texture(pattern.pgU);
        pg.vertex(-cubeSize / 2, -cubeSize / 2, -cubeSize / 2, 0, 0);
        pg.vertex(cubeSize / 2, -cubeSize / 2, -cubeSize / 2, imageWidth, 0);
        pg.vertex(cubeSize / 2, -cubeSize / 2, cubeSize / 2, imageWidth, imageHeight);
        pg.vertex(-cubeSize / 2, -cubeSize / 2, cubeSize / 2, 0, imageHeight);
        pg.endShape();

        // Right
        pg.beginShape();
        pg.texture(pattern.pgR);
        pg.vertex(cubeSize / 2, -cubeSize / 2, cubeSize / 2, imageHeight, imageHeight);
        pg.vertex(cubeSize / 2, cubeSize / 2, cubeSize / 2, imageWidth, 0);
        pg.vertex(cubeSize / 2, cubeSize / 2, -cubeSize / 2, 0, 0);
        pg.vertex(cubeSize / 2, -cubeSize / 2, -cubeSize / 2, 0, imageHeight);
        pg.endShape();

        // Left
        pg.beginShape();
        pg.texture(pattern.pgL);
        pg.vertex(-cubeSize / 2, -cubeSize / 2, -cubeSize / 2, imageWidth, imageHeight);
        pg.vertex(-cubeSize / 2, cubeSize / 2, -cubeSize / 2, imageWidth, 0);
        pg.vertex(-cubeSize / 2, cubeSize / 2, cubeSize / 2, 0, 0);
        pg.vertex(-cubeSize / 2, -cubeSize / 2, cubeSize / 2, 0, imageHeight);
        pg.endShape();

        pg.tint(255, 255);
        pg.popMatrix();
    }
}
