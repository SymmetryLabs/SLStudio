// UI3dComponent getUIVenue() {
//   switch (environment) {
//   case SATELLITE: return new UISatellite();
//   case MIDWAY: return new UIMidway();
//   }
//   return null;
// }

// abstract class UIVenue extends UI3dComponent {

//   final static float LOGO_SIZE = 100*INCHES;
//   final PImage LOGO = loadImage("envelop-logo-clear.png");

//   @Override
//   public void onDraw(UI ui, PGraphics pg) {
//     pg.stroke(#000000);
//     pg.fill(#202020);
//     drawFloor(ui, pg);
    
//     // Logo
//     pg.noFill();
//     pg.noStroke();
//     pg.beginShape();
//     pg.texture(LOGO);
//     pg.textureMode(NORMAL);
//     pg.vertex(-LOGO_SIZE, .1, -LOGO_SIZE, 0, 1);
//     pg.vertex(LOGO_SIZE, .1, -LOGO_SIZE, 1, 1);
//     pg.vertex(LOGO_SIZE, .1, LOGO_SIZE, 1, 0);
//     pg.vertex(-LOGO_SIZE, .1, LOGO_SIZE, 0, 0);
//     pg.endShape(CLOSE);
    
//     // Speakers
//     pg.fill(#000000);
//     pg.stroke(#202020);
//     for (Column column : venue.columns) {
//       pg.translate(column.cx, 0, column.cz);
//       pg.rotateY(-column.theta);
//       pg.translate(0, 9*INCHES, 0);
//       pg.rotateX(Column.SPEAKER_ANGLE);
//       pg.box(21*INCHES, 16*INCHES, 15*INCHES);
//       pg.rotateX(-Column.SPEAKER_ANGLE);
//       pg.translate(0, 6*FEET-9*INCHES, 0);
//       pg.box(21*INCHES, 16*INCHES, 15*INCHES);
//       pg.translate(0, 11*FEET + 3*INCHES - 6*FEET, 0);
//       pg.rotateX(-Column.SPEAKER_ANGLE);
//       pg.box(21*INCHES, 16*INCHES, 15*INCHES);
//       pg.rotateX(Column.SPEAKER_ANGLE);
//       pg.rotateY(+column.theta);
//       pg.translate(-column.cx, -11*FEET - 3*INCHES, -column.cz);
//     }
//   }
  
//   protected abstract void drawFloor(UI ui, PGraphics pg);
// }

// class UISatellite extends UIVenue {
//   public void drawFloor(UI ui, PGraphics pg) {
//     pg.beginShape();
//     for (PVector v : Satellite.PLATFORM_POSITIONS) {
//       pg.vertex(v.x, 0, v.y);
//     }
//     pg.endShape(CLOSE);
//     pg.beginShape(QUAD_STRIP);
//     for (int vi = 0; vi <= Satellite.PLATFORM_POSITIONS.length; ++vi) {
//       PVector v = Satellite.PLATFORM_POSITIONS[vi % Satellite.PLATFORM_POSITIONS.length];
//       pg.vertex(v.x, 0, v.y);
//       pg.vertex(v.x, -8*INCHES, v.y);
//     }
//     pg.endShape();
//   }
// }
  
// class UIMidway extends UIVenue {
    
//   @Override
//   public void onDraw(UI ui, PGraphics pg) {
//     super.onDraw(ui, pg);
    
//     // Desk
//     pg.translate(0, 20*INCHES, -Midway.DEPTH/2 + 18*INCHES);
//     pg.box(6*FEET, 40*INCHES, 36*INCHES);
//     pg.translate(0, -20*INCHES, Midway.DEPTH/2 - 18*INCHES);
    
//     // Subwoofers
//     for (PVector pv : Midway.SUB_POSITIONS) {
//       pg.translate(pv.x, 10*INCHES, pv.y);
//       pg.rotateY(-QUARTER_PI);
//       pg.box(29*INCHES, 20*INCHES, 29*INCHES);
//       pg.rotateY(QUARTER_PI);
//       pg.translate(-pv.x, -10*INCHES, -pv.y);
//     }
//   }
  
//   @Override
//   protected void drawFloor(UI ui, PGraphics pg) {
//     // Floor
//     pg.translate(0, -4*INCHES, 0);
//     pg.box(Midway.WIDTH, 8*INCHES, Midway.DEPTH);
//     pg.translate(0, 4*INCHES, 0);
//   }        
// }

class UIOutputs extends UICollapsibleSection {
    UIOutputs(UI ui, float x, float y, float w) {
        super(ui, x, y, w, 124);

        final SortedSet<SLController> sortedControllers = new TreeSet<SLController>(new Comparator<SLController>() {
            int compare(SLController o1, SLController o2) {
                try {
                    return Integer.parseInt(o1.cubeId) - Integer.parseInt(o2.cubeId);
                } catch (NumberFormatException e) {
                    return o1.cubeId.compareTo(o2.cubeId);
                }
            }
        });
        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (SLController b : controllers) { sortedControllers.add(b); }
        for (SLController b : sortedControllers) { items.add(new ControllerItem(b)); }
        final UIItemList outputList = new UIItemList(ui, 1, 0, w-11, 20);

        outputList.setItems(items).addToContainer(this);

        setTitle(items.size());

        controllers.addListener(new ListListener<SLController>() {
          void itemAdded(final int index, final SLController c) {
            dispatcher.dispatchUi(new Runnable() {
                public void run() {
                    if (c.networkDevice != null) c.networkDevice.version.addListener(deviceVersionListener);
                    sortedControllers.add(c);
                    items.clear();
                        for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                    outputList.setItems(items);
                    setTitle(items.size());
                    redraw();
                }
            });
          }
          void itemRemoved(final int index, final SLController c) {
            dispatcher.dispatchUi(new Runnable() {
                public void run() {
                    if (c.networkDevice != null) c.networkDevice.version.removeListener(deviceVersionListener);
                    sortedControllers.remove(c);
                    items.clear();
                        for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                    outputList.setItems(items);
                    setTitle(items.size());
                    redraw();
                }
            });
          }
        });

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {
          @Override
          public void onToggle(boolean on) {
            for (SLController c : controllers)
                c.enabled.setValue(on);
          }
        }.setBorderRounding(4));
    }

    private final IntListener deviceVersionListener = new IntListener() {
        public void onChange(int version) {
            dispatcher.dispatchUi(new Runnable() {
            public void run() {
                    redraw();
                }
            });
        }
    };

    private void setTitle(int count) {
        setTitle("OUTPUT (" + count + ")");
        setTitleX(20);
    }

    class ControllerItem extends UIItemList.AbstractItem {
        final SLController controller;

        ControllerItem(SLController _controller) {
          this.controller = _controller;
          controller.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) { redraw(); }
          });
        }

        String getLabel() {
            if (controller.networkDevice != null && controller.networkDevice.version.get() != -1) {
                return controller.cubeId + " (v" + controller.networkDevice.version + ")";
            } else {
                return controller.cubeId;
            }
        }

        boolean isSelected() { 
            return controller.enabled.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            println("onActivate");
            controller.enabled.toggle();
        }

        @Override
        public void onDeactivate() {
            println("onDeactivate");
            controller.enabled.setValue(false);
        }
    }
}

// class UIEnvelopSource extends UICollapsibleSection {
//   UIEnvelopSource(UI ui, float x, float y, float w) {
//     super(ui, x, y, w, 124);
//     setTitle("StudioCubes SOURCE");
//     new UIEnvelopMeter(ui, studioCubes.source, 0, 0, getContentWidth(), 60).addToContainer(this);    
//     UIAudio.addGainAndRange(this, 64, studioCubes.source.gain, studioCubes.source.range);
//     UIAudio.addAttackAndRelease(this, 84, studioCubes.source.attack, studioCubes.source.release);
//   }
// }

// class UIEnvelopDecode extends UICollapsibleSection {
//   UIEnvelopDecode(UI ui, float x, float y, float w) {
//     super(ui, x, y, w, 124);
//     setTitle("STUDIOCUBES DECODE");
//     new UIEnvelopMeter(ui, studioCubes.decode, 0, 0, getContentWidth(), 60).addToContainer(this);
//     UIAudio.addGainAndRange(this, 64, studioCubes.decode.gain, studioCubes.decode.range);
//     UIAudio.addAttackAndRelease(this, 84, studioCubes.decode.attack, studioCubes.decode.release);
//   }
// }

// class UIEnvelopMeter extends UI2dComponent {
    
//   private final StudioCubes.Meter meter;
  
//   public UIEnvelopMeter(UI ui, StudioCubes.Meter meter, float x, float y, float w, float h) {
//     super(x, y, w, h);
//     this.meter = meter;
//     setBackgroundColor(ui.theme.getDarkBackgroundColor());
//     setBorderColor(ui.theme.getControlBorderColor());
//   }
  
//   public void onDraw(UI ui, PGraphics pg) {
//     BoundedParameter[] channels = this.meter.getChannels();
//     float bandWidth = ((width-2) - (channels.length-1)) / channels.length;
    
//     pg.noStroke();
//     pg.fill(ui.theme.getPrimaryColor());
//     int x = 1;
//     for (int i = 0; i < channels.length; ++i) {
//       int nextX = Math.round(1 + (bandWidth+1) * (i+1));
//       float h = (this.height-2) * channels[i].getValuef(); 
//       pg.rect(x, this.height-1-h, nextX-x-1, h);
//       x = nextX;
//     }
        
//     // TODO(mcslee): do this properly, on a timer
//     redraw();
//   }
// }

// class UISoundObjects extends UI3dComponent {
//   final PFont objectLabelFont; 

//   UISoundObjects() {
//     this.objectLabelFont = loadFont("Arial-Black-24.vlw");
//   }
  
//   public void onDraw(UI ui, PGraphics pg) {
//     for (StudioCubes.Source.Channel channel : studioCubes.source.channels) {
//       if (channel.active) {
//         float tx = channel.tx;
//         float ty = channel.ty;
//         float tz = channel.tz;
//         pg.directionalLight(40, 40, 40, .5, -.4, 1);
//         pg.ambientLight(40, 40, 40);
//         pg.translate(tx, ty, tz);
//         pg.noStroke();
//         pg.fill(0xff00ddff);
//         pg.sphere(6*INCHES);
//         pg.noLights();
//         pg.scale(1, -1);
//         pg.textAlign(CENTER, CENTER);
//         pg.textFont(objectLabelFont);
//         pg.textSize(4);
//         pg.fill(#00ddff);
//         pg.text(Integer.toString(channel.index), 0, -1*INCHES, -6.1*INCHES);
//         pg.scale(1, -1);
//         pg.translate(-tx, -ty, -tz);
//       }
//     }    
//   }
// }