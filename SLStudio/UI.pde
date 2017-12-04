import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI3dComponent;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import org.apache.commons.math3.util.FastMath;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static processing.core.PApplet.println;

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

class UIBlobs extends UI3dComponent {
  com.symmetrylabs.util.BlobTracker tracker;
  final float SIZE_SCALE = 12;
  final float VELOCITY_SCALE = 1;

  UIBlobs() { }

  protected void onDraw(UI ui, PGraphics pg) {
    for (com.symmetrylabs.util.BlobTracker.Blob b : com.symmetrylabs.util.BlobTracker.getInstance(lx).getBlobs()) {
      float x = b.pos.x;
      float y = b.pos.y;
      float z = b.pos.z;
      float size = SIZE_SCALE * b.size;
      pg.strokeWeight(1);
      pg.stroke(255, 0, 128);
      for (int d = -1; d < 2; d += 2) {
        for (int e = -1; e < 2; e += 2) {
          pg.line(x + d*size, y, z, x, y + e*size, z);
          pg.line(x, y + d*size, z, x, y, z + e*size);
          pg.line(x, y, z + d*size, x + e*size, y, z);
        }
      }
      pg.stroke(128, 0, 255);
      PVector end = PVector.add(b.pos, PVector.mult(b.vel, VELOCITY_SCALE));
      pg.line(x, y, z, end.x, end.y, end.z);
    }
  }
}

class UIOutputs extends UICollapsibleSection {
    UIOutputs(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 500);
        setTitle();

        UIButton testOutput = new UIButton(0, 0, w/2 - 8, 19) {
          @Override
          public void onToggle(boolean isOn) { }
        }.setLabel("Test Broadcast").setParameter(outputControl.testBroadcast);
        testOutput.addToContainer(this);

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {}
          .setParameter(outputControl.enabled).setBorderRounding(4));

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        final UIItemList.ScrollList outputList = new UIItemList.ScrollList(ui, 0, 22, w-8, 454);

        for (Pixlite pixlite : pixlites) { 
            items.add(new PixliteItem(pixlite));
            pixlite.enabled.setValue(false);
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
            return "(" + pixlite.ipAddress + ")" + pixlite.slice.id;
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