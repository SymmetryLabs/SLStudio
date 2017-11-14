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
    public UIAxes() {
        mappingMode.running.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                visible.setValue(((BooleanParameter)parameter).getValueb());
            }
        });
        visible.setValue(false);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        pg.strokeWeight(1);
        pg.stroke(LXColor.GREEN);
        pg.line(0, 0, 0, 1000, 0, 0);
        pg.stroke(LXColor.BLUE);
        pg.line(0, 0, 0, 0, 1000, 0);
        pg.stroke(LXColor.RED);
        pg.line(0, 0, 0, 0, 0, 1000);
    }
}

public class UIStage extends UI3dComponent {
    private final int NUM_SIDES = 1000;
    private final float HEIGHT_BIG = 1.9*12;
    private final float RADIUS_BIG = 22*12;
    private final float HEIGHT_MEDIUM = HEIGHT_BIG*2/3;
    private final float RADIUS_MEDIUM = RADIUS_BIG+24;
    private final float HEIGHT_SMALL = HEIGHT_BIG*1/3;
    private final float RADIUS_SMALL = RADIUS_BIG+48;

    public UIStage() {
        visible.setValue(true);
    }

    protected void onDraw(UI ui, PGraphics pg) {
        // pg.stroke(255, 0, 0);
        // float angle = 360 / NUM_SIDES;
        // float halfHeight = HEIGHT / 2;
        // // draw top shape
        // pg.beginShape();
        // for (int i = 0; i < NUM_SIDES; i++) {
        //     float x = cos( radians( i * angle ) ) * RADIUS;
        //     float y = sin( radians( i * angle ) ) * RADIUS;
        //     pg.vertex( x, y, -halfHeight );
        // }
        // pg.endShape(CLOSE);
        // // draw bottom shape
        // pg.beginShape();
        // for (int i = 0; i < NUM_SIDES; i++) {
        //     float x = cos( radians( i * angle ) ) * RADIUS;
        //     float y = sin( radians( i * angle ) ) * RADIUS;
        //     pg.vertex( x, y, halfHeight );
        // }
        // pg.endShape(CLOSE);

        // rect
        pg.pushMatrix();
        pg.fill(0);
        pg.translate(0, -HEIGHT_BIG/2-3, 0);
        pg.box(RADIUS_SMALL*2 + 8*50, HEIGHT_BIG-1, 12*9);

        pg.pushMatrix();
        pg.translate(-RADIUS_BIG-13*12, 5*10.5, 0);
        pg.box(12*16, 10*12, 12*9);
        pg.popMatrix();

        pg.pushMatrix();
        pg.translate(2*RADIUS_BIG-7*15.5, 5*12, 0);
        pg.box(12*16, 10*12, 12*9);
        pg.popMatrix();

        pg.popMatrix();

        // big
        pg.translate(0, -HEIGHT_BIG-2, 0);
        pg.fill(100, 100, 100);

        pg.pushMatrix();
        pg.translate(0, HEIGHT_BIG, 0);
        pg.rotateX(PI/2);
        pg.ellipse(0, 0, RADIUS_BIG*2, RADIUS_BIG*2);
        pg.popMatrix();

        float angle = 0;
        float angleIncrement = TWO_PI / NUM_SIDES;
        pg.beginShape(QUAD_STRIP);
        for (int i = 0; i < NUM_SIDES + 1; ++i) {
          pg.vertex(RADIUS_BIG*cos(angle), 0, RADIUS_BIG*sin(angle));
          pg.vertex(RADIUS_BIG*cos(angle), HEIGHT_BIG, RADIUS_BIG*sin(angle));
          angle += angleIncrement;
        }
        pg.endShape();

        // medium
        pg.pushMatrix();
        pg.translate(0, 0, 0);

        pg.pushMatrix();
        pg.translate(0, HEIGHT_MEDIUM, 0);
        pg.rotateX(PI/2);
        pg.ellipse(0, 0, RADIUS_MEDIUM*2, RADIUS_MEDIUM*2);
        pg.popMatrix();

        angle = 0;
        angleIncrement = TWO_PI / NUM_SIDES;
        pg.beginShape(QUAD_STRIP);
        for (int i = 0; i < NUM_SIDES + 1; ++i) {
          pg.vertex(RADIUS_MEDIUM*cos(angle), 0, RADIUS_MEDIUM*sin(angle));
          pg.vertex(RADIUS_MEDIUM*cos(angle), HEIGHT_MEDIUM, RADIUS_MEDIUM*sin(angle));
          angle += angleIncrement;
        }
        pg.endShape();

        // small
        pg.pushMatrix();
        pg.translate(0, 0, 0);

        pg.pushMatrix();
        pg.translate(0, HEIGHT_SMALL, 0);
        pg.rotateX(PI/2);
        pg.ellipse(0, 0, RADIUS_SMALL*2, RADIUS_SMALL*2);
        pg.popMatrix();

        angle = 0;
        angleIncrement = TWO_PI / NUM_SIDES;
        pg.beginShape(QUAD_STRIP);
        for (int i = 0; i < NUM_SIDES + 1; ++i) {
          pg.vertex(RADIUS_SMALL*cos(angle), 0, RADIUS_SMALL*sin(angle));
          pg.vertex(RADIUS_SMALL*cos(angle), HEIGHT_SMALL, RADIUS_SMALL*sin(angle));
          angle += angleIncrement;
        }
        pg.endShape();

        pg.popMatrix();
        pg.popMatrix();
    }
}


class MappingMode extends LXRunnableComponent {

    public ModelMutator modelMutator;

    public MappingPattern pattern;

    public BooleanParameter displayOrientation;

    public MappingMode(SLStudio lx, SLModel model) {
        super(lx);
        this.modelMutator = new ModelMutator(model);
        this.displayOrientation = new BooleanParameter("displayOrientation", true);
    }

    protected void run(double deltaMs) {
        for (LXEffect effect : lx.engine.masterChannel.getEffects()) {
            effect.enabled.setValue(false);
        }

        setPattern();
        pattern.enabled.setValue(true);
    }

    protected void onStart() {
        setPattern();
        lx.ui.axes.visible.setValue(true);
    }

    protected void onStop() {
        for (LXEffect effect : lx.engine.masterChannel.getEffects()) {
            effect.enabled.setValue(true);
        }

        LXEffect effect = lx.engine.masterChannel.getEffect("MappingPattern");

        if (effect != null) {
            lx.engine.masterChannel.removeEffect(effect);
        }

        this.pattern = null;
        lx.ui.axes.visible.setValue(false);
    }

    private void setPattern() {
        if (pattern == null || lx.engine.masterChannel.getEffect("MappingPattern") == null) {
            this.pattern = new MappingPattern(lx);
            lx.engine.masterChannel.addEffect(pattern);
        }
    }

    private class MappingPattern extends LXEffect {

        private MappingPattern(LX lx) {
            super(lx);
        }

        @Override
        public void run(double deltaMs, double amount) {
            for (Cube cube : ((SLModel)model).cubes) {
                if (cube.id.equals("")) {
                    for (LXPoint p : cube.points) {
                        colors[p.index] = LXColor.scaleBrightness(LXColor.RED, 0.25);
                    }
                } else {
                    for (LXPoint p : cube.points) {
                        colors[p.index] = LXColor.scaleBrightness(LXColor.GREEN, 0.25);
                    }
                }
            }

            Cube cube = (Cube)mappingMode.modelMutator.getSelectedFixture();

            if (mappingMode.displayOrientation.getValueb()) {
                int si = 0;
                color sc = 0;
                color r = LXColor.RED;
                color g = LXColor.GREEN;
                color b = LXColor.BLUE;

                for (Strip strip : cube.strips) {
                  int faceI = si / Face.STRIPS_PER_FACE;

                  switch (faceI) {
                    case 0: sc = r; break;
                    case 1: sc = g; break;
                    case 2: sc = b; break;
                    case 3: sc = r|g|b; break;
                  }
                  if (si % Face.STRIPS_PER_FACE == 2) {
                    sc = r|g;
                  }

                  for (LXPoint p : strip.points) {
                    colors[p.index] = sc;
                  }
                  ++si;
                }
            } else {
                if (cube.id.equals("")) {
                    for (LXPoint p : cube.points) {
                        colors[p.index] = LXColor.RED;
                    }
                } else {
                    for (LXPoint p : cube.points) {
                        colors[p.index] = LXColor.GREEN;
                    }
                }
            }

            // if (cube.id.equals("")) {
            //     for (LXPoint p : cube.points) {
            //         colors[p.index] = LXColor.RED;
            //     }
            // } else {
            //     for (LXPoint p : cube.points) {
            //         colors[p.index] = LXColor.GREEN;
            //     }
            // }
        }
    }
}

class ModelMutator {

    private final ObjectParameter<MutableModel> selectedFixture;

    public ModelMutator(SLModel model) {
        MutableModel[] fixtures = new MutableModel[model.cubes.size()];

        for (int i = 0; i < model.cubes.size(); i++) {
            fixtures[i] = (MutableModel)model.cubes.get(i);
        }

        this.selectedFixture = new ObjectParameter<MutableModel>("selectedFixture", fixtures, fixtures[0]);
    }

    public MutableModel getSelectedFixture() {
        return selectedFixture.getObject();
    }

    public void translateX(float amount) {
        if (mappingMode.running.getValueb()) {
            getSelectedFixture().translateX(amount);
        }
    }

    public void translateY(float amount) {
        if (mappingMode.running.getValueb()) {
            getSelectedFixture().translateY(amount);
        }
    }

    public void translateZ(float amount) {
        if (mappingMode.running.getValueb()) {
            getSelectedFixture().translateZ(amount);
        }
    }

    public void rotateX(float amount) {
        if (mappingMode.running.getValueb()) {
            getSelectedFixture().rotateX(amount);
        }
    }

    public void rotateY(float amount) {
        if (mappingMode.running.getValueb()) {
            getSelectedFixture().rotateY(amount);
        }
    }

    public void rotateZ(float amount) {
        if (mappingMode.running.getValueb()) {
            getSelectedFixture().rotateZ(amount);
        }
    }

    public void decrementSelectedFixture() {
        selectedFixture.decrement();
    }

    public void incrementSelectedFixture() {
        selectedFixture.increment();
    }
}

// public class MappingPattern extends SLPattern {
//   private final SinLFO pulse = new SinLFO(20, 100, 800);

//   public MappingPattern(LX lx) {
//     super(lx);
//     addModulator(pulse).start();

//     mappingMode.modelMutator.selectedFixture.addListener(
//       new LXParameterListener() {
//         public void onParameterChanged(LXParameter p) {
//           pulse.setBasis(0);
//         }
//       }
//     );
//   }

//   public void run(double deltaMs) {
//     setColors(lx.hsb(0, 100, 20));

//     LXModel selectedFixture = mappingMode.modelMutator.getSelectedFixture();

//     if (mappingMode.displayOrientation.getValueb()) {
//         println("d");
//         int si = 0;
//         color sc = 0;
//         color r = LXColor.RED;
//         color g = LXColor.GREEN;
//         color b = LXColor.BLUE;

//         for (Strip strip : ((Cube)selectedFixture).strips) {
//           int faceI = si / Face.STRIPS_PER_FACE;

//           switch (faceI) {
//             case 0: sc = r; break;
//             case 1: sc = g; break;
//             case 2: sc = b; break;
//             case 3: sc = r|g|b; break;
//           }
//           if (si % Face.STRIPS_PER_FACE == 2) {
//             sc = r|g;
//           }
//           setColor(strip, sc);
//           ++si;
//         }
//     } else {
//         for (LXPoint p : selectedFixture.points) {
//             colors[p.index] = LXColor.GREEN;
//         }
//     }
//   }

//   public void onActive() {
//     pulse.setBasis(0);
//   }
// }

class UIMapping extends UICollapsibleSection {
    public UIMapping(UI ui, final LX lx, float x, float y, float w) {
        super(ui, x, y, w, 300);
        setBackgroundColor(#404040);
        setBorderRounding(4);

        setTitle("MAPPING");
        setTitleX(20);

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {
          @Override
          public void onToggle(boolean isOn) {
            redraw();
          }
        }.setParameter(mappingMode.running).setBorderRounding(4));

        final UIButton decrementSelectedVirtualFixture = new UIButton(0, 3, 60, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.decrementSelectedFixture();
            }
          }
        }.setLabel("Fixture -").setMomentary(true);
        decrementSelectedVirtualFixture.addToContainer(this);

        final UIButton incrementSelectedVirtualFixture = new UIButton(62, 3, 60, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.incrementSelectedFixture();
            }
          }
        }.setLabel("Fixture +").setMomentary(true);
        incrementSelectedVirtualFixture.addToContainer(this);

        final UIButton displayFixtureOrientation = new UIButton(137, 3, 110, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              
            }
          }
        }.setLabel("Display Orientation");
        displayFixtureOrientation.setParameter(mappingMode.displayOrientation);
        displayFixtureOrientation.addToContainer(this);

        /**
         * Translate X
         *---------------------------------------------------------------------------*/
        new UILabel(5, 32, 60, 12)
        .setLabel("Translate X:")
        .addToContainer(this);

        final UIButton translateMinusEvenMoreX = new UIButton(72, 30, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateX(-25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-25").setMomentary(true);
        translateMinusEvenMoreX.addToContainer(this);

        final UIButton translateMinusMoreX = new UIButton(99, 30, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateX(-10);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-10").setMomentary(true);
        translateMinusMoreX.addToContainer(this);

        final UIButton translateMinusX = new UIButton(126, 30, 33, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateX(-0.25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-0.25").setMomentary(true);
        translateMinusX.addToContainer(this);

        final UIButton translateAddX = new UIButton(161, 30, 33, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateX(0.25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+0.25").setMomentary(true);
        translateAddX.addToContainer(this);

        final UIButton translateAddMoreX = new UIButton(196, 30, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateX(10);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+10").setMomentary(true);
        translateAddMoreX.addToContainer(this);

        final UIButton translateAddEvenMoreX = new UIButton(223, 30, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateX(25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+25").setMomentary(true);
        translateAddEvenMoreX.addToContainer(this);

        /**
         * Translate Y
         *---------------------------------------------------------------------------*/
        new UILabel(5, 55, 60, 12)
        .setLabel("Translate Y:")
        .addToContainer(this);

        final UIButton translateMinusEvenMoreY = new UIButton(72, 53, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateY(-25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-25").setMomentary(true);
        translateMinusEvenMoreY.addToContainer(this);

        final UIButton translateMinusMoreY = new UIButton(99, 53, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateY(-10);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-10").setMomentary(true);
        translateMinusMoreY.addToContainer(this);

        final UIButton translateMinusY = new UIButton(126, 53, 33, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateY(-0.25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-0.25").setMomentary(true);
        translateMinusY.addToContainer(this);

        final UIButton translateAddY = new UIButton(161, 53, 33, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateY(0.25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+0.25").setMomentary(true);
        translateAddY.addToContainer(this);

        final UIButton translateAddMoreY = new UIButton(196, 53, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateY(10);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+10").setMomentary(true);
        translateAddMoreY.addToContainer(this);

        final UIButton translateAddEvenMoreY = new UIButton(223, 53, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateY(25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+25").setMomentary(true);
        translateAddEvenMoreY.addToContainer(this);

        /**
         * Translate Z
         *---------------------------------------------------------------------------*/
        new UILabel(5, 78, 60, 12)
        .setLabel("Translate Z:")
        .addToContainer(this);

        final UIButton translateMinusEvenMoreZ = new UIButton(72, 76, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateZ(-25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-25").setMomentary(true);
        translateMinusEvenMoreZ.addToContainer(this);

        final UIButton translateMinusMoreZ = new UIButton(99, 76, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateZ(-10);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-10").setMomentary(true);
        translateMinusMoreZ.addToContainer(this);

        final UIButton translateMinusZ = new UIButton(126, 76, 33, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateZ(-0.25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-0.25").setMomentary(true);
        translateMinusZ.addToContainer(this);

        final UIButton translateAddZ = new UIButton(161, 76, 33, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateZ(0.25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+0.25").setMomentary(true);
        translateAddZ.addToContainer(this);

        final UIButton translateAddMoreZ = new UIButton(196, 76, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateZ(10);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+10").setMomentary(true);
        translateAddMoreZ.addToContainer(this);

        final UIButton translateAddEvenMoreZ = new UIButton(223, 76, 25, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.translateZ(25);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+25").setMomentary(true);
        translateAddEvenMoreZ.addToContainer(this);

        /**
         * Rotate X
         *---------------------------------------------------------------------------*/
        new UILabel(5, 101, 60, 12)
        .setLabel("Rotate X:")
        .addToContainer(this);

        final UIButton rotateMinusMoreX = new UIButton(72, 100, 44, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateX(-45);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-45deg").setMomentary(true);
        rotateMinusMoreX.addToContainer(this);

        final UIButton rotateMinusX = new UIButton(118, 100, 41, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateX(-1);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-1deg").setMomentary(true);
        rotateMinusX.addToContainer(this);

        final UIButton rotateAddX = new UIButton(161, 100, 41, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateX(1);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+1deg").setMomentary(true);
        rotateAddX.addToContainer(this);

        final UIButton rotateAddMoreX = new UIButton(204, 100, 44, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateX(45);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+45deg").setMomentary(true);
        rotateAddMoreX.addToContainer(this);


        /**
         * Rotate Y
         *---------------------------------------------------------------------------*/
        new UILabel(5, 124, 60, 12)
        .setLabel("Rotate Y:")
        .addToContainer(this);

        final UIButton rotateMinusMoreY = new UIButton(72, 123, 44, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateY(-45);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-45deg").setMomentary(true);
        rotateMinusMoreY.addToContainer(this);

        final UIButton rotateMinusY = new UIButton(118, 123, 41, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateY(-1);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-1deg").setMomentary(true);
        rotateMinusY.addToContainer(this);

        final UIButton rotateAddY = new UIButton(161, 123, 41, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateY(1);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+1deg").setMomentary(true);
        rotateAddY.addToContainer(this);

        final UIButton rotateAddMoreY = new UIButton(204, 123, 44, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateY(45);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+45deg").setMomentary(true);
        rotateAddMoreY.addToContainer(this);

        /**
         * Rotate Z
         *---------------------------------------------------------------------------*/
        new UILabel(5, 147, 60, 12)
        .setLabel("Rotate Z:")
        .addToContainer(this);

        final UIButton rotateMinusMoreZ = new UIButton(72, 146, 44, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateZ(-45);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-45deg").setMomentary(true);
        rotateMinusMoreZ.addToContainer(this);

        final UIButton rotateMinusZ = new UIButton(118, 146, 41, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateZ(-1);
              lx.model.update(true, true);
            }
          }
        }.setLabel("-1deg").setMomentary(true);
        rotateMinusZ.addToContainer(this);

        final UIButton rotateAddZ = new UIButton(161, 146, 41, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateZ(1);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+1deg").setMomentary(true);
        rotateAddZ.addToContainer(this);

        final UIButton rotateAddMoreZ = new UIButton(204, 146, 44, 20) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
              mappingMode.modelMutator.rotateZ(45);
              lx.model.update(true, true);
            }
          }
        }.setLabel("+45deg").setMomentary(true);
        rotateAddMoreZ.addToContainer(this);

        /**
         * Network
         *---------------------------------------------------------------------------*/
        new UILabel(47, 168, 300, 20)
        .setLabel("Assign a fixture from network")
        .addToContainer(this);

        final UIButton decrementSelectedNetworkFixture = new UIButton(1, 200, 81, 24) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {

            }
          }
        }.setLabel("Fixture -").setMomentary(true);
        decrementSelectedNetworkFixture.addToContainer(this);

        final UIButton incrementSelectedNetworkFixture = new UIButton(84, 200, 81, 24) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {

            }
          }
        }.setLabel("Fixture +").setMomentary(true);
        incrementSelectedNetworkFixture.addToContainer(this);

        final UIButton assignNetworkFixture = new UIButton(166, 200, 81, 24) {
          @Override
          protected void onToggle(boolean active) {
            if (active) {
                setLabel("Unassign");
                setBackgroundColor(#752323);
            } else {
                setLabel("Assign");
                setBackgroundColor(#237523);
            }
          }
        }.setLabel("Assign");
        assignNetworkFixture.setBackgroundColor(#237523).addToContainer(this);

        new UILabel(1, 229, 245, 46)
        .setLabel("").setBackgroundColor(#343434)
        .addToContainer(this);

        // final UIButton rotateAddZ = new UIButton(180, 95, 45, 20) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().rotateX(-1);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("-1deg").setMomentary(true);
        // rotateAddZ.addToContainer(this);

        // // selected fixture
        // final UIButton selectedFixtureMinus = new UIButton(4, 4, 45, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.decrementSelectedFixture();
        //     }
        //   }
        // }.setLabel("fixture-").setMomentary(true);
        // selectedFixtureMinus.addToContainer(this);

        // final UIButton selectedFixtureAdd = new UIButton(54, 4, 45, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.incrementSelectedFixture();
        //     }
        //   }
        // }.setLabel("fixture+").setMomentary(true);
        // selectedFixtureAdd.addToContainer(this);

        // // x
        // final UIButton minusX = new UIButton(4, 24, 25, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().translateX(-0.25);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("x-").setMomentary(true);
        // minusX.addToContainer(this);

        // final UIButton addX = new UIButton(29, 24, 25, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().translateX(0.25);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("x+").setMomentary(true);
        // addX.addToContainer(this);

        // // y
        // final UIButton minusY = new UIButton(4, 44, 25, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().translateY(-0.25);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("y-").setMomentary(true);
        // minusY.addToContainer(this);

        // final UIButton addY = new UIButton(29, 44, 25, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().translateY(0.25);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("y+").setMomentary(true);
        // addY.addToContainer(this);

        // // z
        // final UIButton minusZ = new UIButton(4, 64, 25, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().translateZ(-0.25);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("z-").setMomentary(true);
        // minusZ.addToContainer(this);

        // final UIButton addZ = new UIButton(29, 64, 25, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().translateZ(0.25);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("z+").setMomentary(true);
        // addZ.addToContainer(this);

        // // rotate X
        // final UIButton rotateMinusX = new UIButton(57, 24, 35, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().rotateX(-5);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("xRot-").setMomentary(true);
        // rotateMinusX.addToContainer(this);

        // final UIButton rotateAddX = new UIButton(93, 24, 35, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().rotateX(5);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("xRot+").setMomentary(true);
        // rotateAddX.addToContainer(this);

        // // rotate Y
        // final UIButton rotateMinusY = new UIButton(57, 44, 35, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().rotateY(-5);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("yRot-").setMomentary(true);
        // rotateMinusY.addToContainer(this);

        // final UIButton rotateAddY = new UIButton(93, 44, 35, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().rotateY(10);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("yRot+").setMomentary(true);
        // rotateAddY.addToContainer(this);

        // // rotate Z
        // final UIButton rotateMinusZ = new UIButton(57, 64, 35, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().rotateZ(-5);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("zRot-").setMomentary(true);
        // rotateMinusZ.addToContainer(this);

        // final UIButton rotateAddZ = new UIButton(93, 64, 35, 18) {
        //   @Override
        //   protected void onToggle(boolean active) {
        //     if (active) {
        //       modelMutator.getSelectedFixture().rotateZ(5);
        //       lx.model.update(true, true);
        //     }
        //   }
        // }.setLabel("zRot+").setMomentary(true);
        // rotateAddZ.addToContainer(this);
    }
}

// class UIEnvelopSource extends UICollapsibleSection {
//   UIEnvelopSource(UI ui, float x, float y, float w) {
//     super(ui, x, y, w, 124);
//     setTitle("ENVELOP SOURCE");
//     new UIEnvelopMeter(ui, envelop.source, 0, 0, getContentWidth(), 60).addToContainer(this);    
//     UIAudio.addGainAndRange(this, 64, envelop.source.gain, envelop.source.range);
//     UIAudio.addAttackAndRelease(this, 84, envelop.source.attack, envelop.source.release);
//   }
// }

// class UIEnvelopDecode extends UICollapsibleSection {
//   UIEnvelopDecode(UI ui, float x, float y, float w) {
//     super(ui, x, y, w, 124);
//     setTitle("ENVELOP DECODE");
//     new UIEnvelopMeter(ui, envelop.decode, 0, 0, getContentWidth(), 60).addToContainer(this);
//     UIAudio.addGainAndRange(this, 64, envelop.decode.gain, envelop.decode.range);
//     UIAudio.addAttackAndRelease(this, 84, envelop.decode.attack, envelop.decode.release);
//   }
// }

// class UIEnvelopMeter extends UI2dContainer {
      
//   public UIEnvelopMeter(UI ui, Envelop.Meter meter, float x, float y, float w, float h) {
//     super(x, y, w, h);
//     setBackgroundColor(ui.theme.getDarkBackgroundColor());
//     setBorderColor(ui.theme.getControlBorderColor());
    
//     NormalizedParameter[] channels = meter.getChannels();
//     float bandWidth = ((width-2) - (channels.length-1)) / channels.length;
//     int xp = 1;
//     for (int i = 0; i < channels.length; ++i) {
//       int nextX = Math.round(1 + (bandWidth+1) * (i+1));
//       new UIEnvelopChannel(channels[i], xp, 1, nextX-xp-1, this.height-2).addToContainer(this);
//       xp = nextX;
//     }
//   }
  
//   class UIEnvelopChannel extends UI2dComponent implements UIModulationSource {
    
//     private final NormalizedParameter channel;
//     private float lev = 0;
    
//     UIEnvelopChannel(final NormalizedParameter channel, float x, float y, float w, float h) {
//       super(x, y, w, h);
//       this.channel = channel;
//       addLoopTask(new LXLoopTask() {
//         public void loop(double deltaMs) {
//           float l2 = UIEnvelopChannel.this.height * channel.getNormalizedf();
//           if (l2 != lev) {
//             lev = l2;
//             redraw();
//           }
//         }
//       });
//     }
    
//     public void onDraw(UI ui, PGraphics pg) {
//       if (lev > 0) {
//         pg.noStroke();
//         pg.fill(ui.theme.getPrimaryColor());
//         pg.rect(0, this.height-lev, this.width, lev);
//       }
//     }
    
//     public LXNormalizedParameter getModulationSource() {
//       return this.channel;
//     }
//   }
// }

// class UISoundObjects extends UI3dComponent {
//   final PFont objectLabelFont; 

//   UISoundObjects() {
//     this.objectLabelFont = loadFont("Arial-Black-24.vlw");
//   }
  
//   public void onDraw(UI ui, PGraphics pg) {
//     for (Envelop.Source.Channel channel : envelop.source.channels) {
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