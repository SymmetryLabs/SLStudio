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

  void keyPressed(KeyEvent keyEvent) {
    if (keyEvent.getKey() == 'x') {
      toggleVisible();
      println("toggle axes");
    }
  }
}

class UIBlobs extends UI3dComponent {
  BlobTracker tracker;
  final float SIZE_SCALE = 12;
  final float VELOCITY_SCALE = 1;

  UIBlobs() { }

  protected void onDraw(UI ui, PGraphics pg) {
    for (BlobTracker.Blob b : BlobTracker.getInstance(lx).getBlobs()) {
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
        super(ui, x, y, w, 124);
        setTitle();

        UIButton testOutput = new UIButton(0, 0, w/2 - 8, 19) {
          @Override
          public void onToggle(boolean isOn) { }
        }.setLabel("Test Broadcast").setParameter(outputControl.testBroadcast);
        testOutput.addToContainer(this);

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {}
          .setParameter(outputControl.enabled).setBorderRounding(4));

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        final UIItemList.ScrollList outputList = new UIItemList.ScrollList(ui, 0, 22, w-8, 78);

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