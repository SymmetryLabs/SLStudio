import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dContext;
import heronarts.p3lx.ui.UI3dComponent;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import net.jafama.FastMath;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.KeyEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static processing.core.PApplet.println;
import static processing.core.PConstants.*;

import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;

class UISpeed extends UI2dContainer {
  public UISpeed(UI ui, final LX lx, float x, float y, float w) {
    super(x, y, w, 20);
    setBackgroundColor(#404040); //ui.theme.getDeviceBackgroundColor()
    setBorderRounding(4);

    new UILabel(5, 2, 50, 12)
    .setLabel("SPEED")
    .addToContainer(this);

    new UISlider(45, 0, 130, 20)
    .setParameter(lx.engine.speed)
    .setShowLabel(false)
    .addToContainer(this);
  }
}

class UIFramerate extends UI2dContext {
  private final P3LX lx;
  private final PFont monospacedFont;
  private final DecimalFormat fpsFormat = new DecimalFormat("0.0");
  private final DecimalFormat elapsedFormat = new DecimalFormat("0.00");

  UIFramerate(UI ui, final P3LX lx, float x) {
    super(ui, x, 0, 700, 30);
    this.lx = lx;
    this.monospacedFont = createFont("Monospaced", 15);
    setVisible(true);
  }

  private long lastDebugPrint = millis();

  protected void onDraw(UI ui, PGraphics pg) {
    pg.textFont(monospacedFont);
    pg.clear();
    pg.textSize(15);
    pg.textAlign(LEFT, TOP);
    if (lx.engine.isThreaded()) {
      // pg.text(String.format("Engine: %2$.1f UI: %2$.1f",
      //   lx.engine.frameRate(), lx.applet.frameRate), 0, 0);
      pg.text(String.format("Engine FPS: %4s UI: %4s  //  Frame: %5sms (avg) %5sms (worst)",
        fpsFormat.format(lx.engine.frameRate()),
        fpsFormat.format(lx.applet.frameRate),
        elapsedFormat.format(lx.engine.timer.runAvgNanos / 1000000.0),
        elapsedFormat.format(lx.engine.timer.runWorstNanos / 1000000.0)), 0, 0);
      if (lx.engine.timer.runLastNanos >= 100000000
          && lx.engine.timer.runLastNanos == lx.engine.timer.runWorstNanos
          && lastDebugPrint + 500 < millis()) {
        lastDebugPrint = millis();
        StringBuilder sb = new StringBuilder();
        sb.append("LXEngine::run() " + ((int) (lx.engine.timer.runLastNanos / 1000000)) + "ms\n");
        if (((int) (lx.engine.timer.oscNanos / 1000000)) != 0) {
          sb.append("LXEngine::run()::osc " + ((int) (lx.engine.timer.oscNanos / 1000000)) + "ms\n");
        }
        if (((int) (lx.engine.timer.inputNanos / 1000000)) != 0) {
          sb.append("LXEngine::run()::inputs " + ((int) (lx.engine.timer.inputNanos / 1000000)) + "ms\n");
        }
        if (((int) (lx.engine.timer.channelNanos / 1000000)) != 0) {
          sb.append("LXEngine::run()::channels " + ((int) (lx.engine.timer.channelNanos / 1000000)) + "ms\n");
          for (LXChannel channel : lx.engine.channels) {
            if (((int) (channel.timer.loopNanos / 1000000)) != 0) {
              sb.append("LXEngine::" + channel.getLabel() + "::loop() " + ((int) (channel.timer.loopNanos / 1000000)) + "ms\n");
              LXPattern pattern = channel.getActivePattern();
              if (((int) (pattern.timer.runNanos / 1000000)) != 0) {
                sb.append("LXEngine::" + channel.getLabel() + "::" + pattern.getLabel() + "::run() " + ((int) (pattern.timer.runNanos / 1000000)) + "ms\n");
              }
              for (LXEffect effect : channel.getEffects()) {
                if (((int) (effect.timer.runNanos / 1000000)) != 0) {
                  sb.append("LXEngine::" + channel.getLabel() + "::" + effect.getLabel() + "::loop() " + ((int) (effect.timer.runNanos / 1000000)) + "ms\n");
                }
              }
            }
          }
        }
        if (((int) (lx.engine.timer.fxNanos / 1000000)) != 0) {
          sb.append("LXEngine::run()::effects " + ((int) (lx.engine.timer.fxNanos / 1000000)) + "ms\n");
          for (LXEffect effect : lx.engine.masterChannel.getEffects()) {
            if (((int) (effect.timer.runNanos / 1000000)) != 0) {
              sb.append("LXEngine::" + effect.getLabel() + "::loop() " + ((int) (effect.timer.runNanos / 1000000)) + "ms\n");
            }
          }
        }
        System.out.println(sb);
      }
    } else {
      pg.text(String.format("FPS: %02.1f", lx.applet.frameRate), 0, 0);
    }


    redraw();
  }
}

class UIAxes extends UI3dComponent {
  UIAxes() {
    setVisible(false);
  }
  protected void onDraw(UI ui, PGraphics pg) {
    pg.strokeWeight(1);

    pg.stroke(255, 0, 0);
    pg.line(0, 0, 0, 1000, 0, 0);

    pg.stroke(0, 255, 0);
    pg.line(0, 0, 0, 0, 1000, 0);

    pg.stroke(0, 0, 255);
    pg.line(0, 0, 0, 0, 0, 1000);
  }
}

class UICubeMapDebug extends UI3dComponent {
  private final P3LX lx;

  UICubeMapDebug(P3LX lx) {
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
    if (pattern.allSunsParams.getValueb()) {
//      for (final LXPoint point : pattern.model.points) {
//        drawPoint(pg, point);
//      }

      renderBox(pattern, pg, pattern.origin, pattern.bboxSize);
    } else {
      for (final Sun sun : pattern.model.suns) {
        int sunIndex = pattern.model.suns.indexOf(sun);

        if (pattern.sunSwitchParams.get(sunIndex).getValueb()) {
          //sun.computeBoundingBox();
          //println((sunIndex + 1) + ": " + sun.boundingBox.size);

//          for (final LXPoint point : sun.points) {
//            drawPoint(pg, point);
//          }

          renderBox(pattern, pg, pattern.originForSun(sun), pattern.bboxForSun(sun));
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
    pg.vertex(-cubeSize/2, cubeSize/2, -cubeSize/2, 0,          0);
    pg.vertex( cubeSize/2, cubeSize/2, -cubeSize/2, imageWidth, 0);
    pg.vertex( cubeSize/2,  -cubeSize/2, -cubeSize/2, imageWidth, imageHeight);
    pg.vertex(-cubeSize/2,  -cubeSize/2, -cubeSize/2, 0,          imageHeight);
    pg.endShape();

    // Back
    pg.beginShape();
    pg.texture(pattern.pgB);
    pg.vertex(cubeSize/2, cubeSize/2, cubeSize/2, 0,          0);
    pg.vertex( -cubeSize/2, cubeSize/2, cubeSize/2, imageWidth, 0);
    pg.vertex( -cubeSize/2,  -cubeSize/2, cubeSize/2, imageWidth, imageHeight);
    pg.vertex(cubeSize/2,  -cubeSize/2, cubeSize/2, 0,          imageHeight);
    pg.endShape();

    // Down
    pg.beginShape();
    pg.texture(pattern.pgD);
    pg.vertex(-cubeSize/2, cubeSize/2, -cubeSize/2, 0,          0);
    pg.vertex( cubeSize/2, cubeSize/2, -cubeSize/2, imageWidth, 0);
    pg.vertex( cubeSize/2, cubeSize/2,  cubeSize/2, imageWidth, imageHeight);
    pg.vertex(-cubeSize/2, cubeSize/2,  cubeSize/2, 0,          imageHeight);
    pg.endShape();

    // Up
    pg.beginShape();
    pg.texture(pattern.pgU);
    pg.vertex(-cubeSize/2, -cubeSize/2, -cubeSize/2, 0,          0);
    pg.vertex( cubeSize/2, -cubeSize/2, -cubeSize/2, imageWidth, 0);
    pg.vertex( cubeSize/2, -cubeSize/2,  cubeSize/2, imageWidth, imageHeight);
    pg.vertex(-cubeSize/2, -cubeSize/2,  cubeSize/2, 0,          imageHeight);
    pg.endShape();

    // Right
    pg.beginShape();
    pg.texture(pattern.pgR);
    pg.vertex(cubeSize/2, -cubeSize/2, cubeSize/2, imageHeight,          imageHeight);
    pg.vertex(cubeSize/2,  cubeSize/2, cubeSize/2, imageWidth, 0);
    pg.vertex(cubeSize/2,  cubeSize/2,  -cubeSize/2, 0, 0);
    pg.vertex(cubeSize/2, -cubeSize/2,  -cubeSize/2, 0,          imageHeight);
    pg.endShape();

    // Left
    pg.beginShape();
    pg.texture(pattern.pgL);
    pg.vertex(-cubeSize/2, -cubeSize/2, -cubeSize/2, imageWidth,          imageHeight);
    pg.vertex(-cubeSize/2,  cubeSize/2, -cubeSize/2, imageWidth, 0);
    pg.vertex(-cubeSize/2,  cubeSize/2,  cubeSize/2, 0, 0);
    pg.vertex(-cubeSize/2, -cubeSize/2,  cubeSize/2, 0,          imageHeight);
    pg.endShape();

    pg.tint(255, 255);
    pg.popMatrix();
  }
}

class UIMarkerPainter extends UI3dComponent {
  Set<MarkerSource> sources;

  UIMarkerPainter() {
    sources = new HashSet<MarkerSource>();
  }

  void addSource(MarkerSource source) {
    sources.add(source);
  }

  void removeSource(MarkerSource source) {
    sources.remove(source);
  }

  protected void onDraw(UI ui, PGraphics pg) {
    for (MarkerSource source : sources) {
      for (Marker marker : source.getMarkers()) {
        marker.draw(pg);
      }
    }
  }
}

class UIOutputs extends UICollapsibleSection {
    UIOutputs(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 500);
        setTitle();

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {}
          .setParameter(outputControl.enabled).setBorderRounding(4));

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        final UIItemList.ScrollList outputList = new UIItemList.ScrollList(ui, 0, 0, w-8, 476);

        for (Pixlite pixlite : pixlites) { 
            items.add(new PixliteItem(pixlite));
        }

        outputList.setItems(items).setSingleClickActivate(true);
        outputList.addToContainer(this);
    }


    private void setTitle() {
        setTitle("OUTPUT");
        setTitleX(20);
    }

    class PixliteItem extends UIItemList.AbstractItem {
        final Pixlite pixlite;

        PixliteItem(Pixlite pixlite) {
          this.pixlite = pixlite;
          pixlite.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) { redraw(); }
          });
        }

        String getLabel() {
            return "(" + pixlite.ipAddress + ") " + pixlite.slice.id;
        }

        boolean isSelected() {
            return pixlite.enabled.isOn();
        }

        @Override
        boolean isActive() {
            return pixlite.enabled.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            if (!outputControl.enabled.getValueb())
                return;
            pixlite.enabled.toggle();
        }
    }
}
