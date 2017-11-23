//import org.timothyb89.lifx.net.BroadcastListener;
//import java.math.BigInteger;

  /*
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 */

void setupOutputs(final LX lx) {
  final Pixlite testPixlite = setupTestBroadcastPixlite(lx);
  lx.addOutput(testPixlite);

  for (Pixlite pixlite : pixlites) {
    lx.addOutput(pixlite);
  }

  outputControl.testBroadcast.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      if (((BooleanParameter)parameter).getValueb()) {
        testPixlite.enabled.setValue(true);
        for (Pixlite pixlite : pixlites) {
          pixlite.enabled.setValue(false);
        }
      } else {
        testPixlite.enabled.setValue(false);
        for (Pixlite pixlite : pixlites) {
          pixlite.enabled.setValue(true);
        }
      }
    }
  });
}

Pixlite setupTestBroadcastPixlite(LX lx) {
  Slice slice = null;

  for (Slice s : model.slices) {
    if (s.type == Slice.Type.FULL) {
      s = slice;
      break;
    }
    println("No full slice in model!!! Test Broadcast needs at least one full sun!!!");
  }

  return new Pixlite(lx, "10.200.1.255", slice);
}

/*
 * Output Component
 *---------------------------------------------------------------------------*/
public final class OutputControl extends LXComponent {
  public final BooleanParameter enabled;

  public final ControllerResetModule controllerResetModule = new ControllerResetModule(lx);

  public final BooleanParameter broadcastPacket = new BooleanParameter("Broadcast packet enabled", false);
  public final BooleanParameter testBroadcast   = new BooleanParameter("Test broadcast enabled", false);

  public OutputControl(LX lx) {
    super(lx, "Output Control");
    this.enabled = lx.engine.output.enabled;

    addParameter(testBroadcast);
    
    enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        for (Pixlite pixlite : pixlites) {
          pixlite.enabled.setValue(((BooleanParameter)parameter).getValueb());
        }
      };
    });
  }
}

/*
 * UIOutput Window
 *---------------------------------------------------------------------------*/
// class UIOutputs extends UICollapsibleSection {
//     UIOutputs(LX lx, UI ui, float x, float y, float w) {
//         super(ui, x, y, w, 124);

//         final SortedSet<SLController> sortedControllers = new TreeSet<SLController>(new Comparator<SLController>() {
//             int compare(SLController o1, SLController o2) {
//                 try {
//                     return Integer.parseInt(o1.cubeId) - Integer.parseInt(o2.cubeId);
//                 } catch (NumberFormatException e) {
//                     return o1.cubeId.compareTo(o2.cubeId);
//                 }
//             }
//         });

//         final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
//         for (SLController c : controllers) { sortedControllers.add(c); }
//         for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
//         final UIItemList.ScrollList outputList = new UIItemList.ScrollList(ui, 0, 22, w-8, 78);

//         outputList.setItems(items).setSingleClickActivate(true);
//         outputList.addToContainer(this);

//         setTitle(items.size());

//         controllers.addListener(new ListListener<SLController>() {
//           void itemAdded(final int index, final SLController c) {
//             dispatcher.dispatchUi(new Runnable() {
//                 public void run() {
//                     if (c.networkDevice != null) c.networkDevice.version.addListener(deviceVersionListener);
//                     sortedControllers.add(c);
//                     items.clear();
//                         for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
//                     outputList.setItems(items);
//                     setTitle(items.size());
//                     redraw();
//                 }
//             });
//           }
//           void itemRemoved(final int index, final SLController c) {
//             dispatcher.dispatchUi(new Runnable() {
//                 public void run() {
//                     if (c.networkDevice != null) c.networkDevice.version.removeListener(deviceVersionListener);
//                     sortedControllers.remove(c);
//                     items.clear();
//                         for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
//                     outputList.setItems(items);
//                     setTitle(items.size());
//                     redraw();
//                 }
//             });
//           }
//         });

//         UIButton testOutput = new UIButton(0, 0, w/2 - 8, 19) {
//           @Override
//           public void onToggle(boolean isOn) { }
//         }.setLabel("Test Broadcast").setParameter(outputControl.testBroadcast);
//         testOutput.addToContainer(this);

//         UIButton resetCubes = new UIButton(w/2-6, 0, w/2 - 1, 19) {
//           @Override
//           public void onToggle(boolean isOn) { 
//             outputControl.controllerResetModule.enabled.setValue(isOn);
//           }
//         }.setMomentary(true).setLabel("Reset Controllers");
//         resetCubes.addToContainer(this);

//         addTopLevelComponent(new UIButton(4, 4, 12, 12) {}
//           .setParameter(outputControl.enabled).setBorderRounding(4));

//         outputControl.enabled.addListener(new LXParameterListener() {
//           public void onParameterChanged(LXParameter parameter) {
//             redraw();
//           };
//         });
//     }

//     private final IntListener deviceVersionListener = new IntListener() {
//         public void onChange(int version) {
//             dispatcher.dispatchUi(new Runnable() {
//             public void run() { redraw(); }
//             });
//         }
//     };

//     private void setTitle(int count) {
//         setTitle("OUTPUT (" + count + ")");
//         setTitleX(20);
//     }

//     class ControllerItem extends UIItemList.AbstractItem {
//         final SLController controller;

//         ControllerItem(SLController _controller) {
//           this.controller = _controller;
//           controller.enabled.addListener(new LXParameterListener() {
//             public void onParameterChanged(LXParameter parameter) { redraw(); }
//           });
//         }

//         String getLabel() {
//             if (controller.networkDevice != null && controller.networkDevice.version.get() != -1) {
//                 return controller.cubeId + " (v" + controller.networkDevice.version + ")";
//             } else {
//                 return controller.cubeId;
//             }
//         }

//         boolean isSelected() { 
//             return controller.enabled.isOn();
//         }

//         @Override
//         boolean isActive() {
//             return controller.enabled.isOn();
//         }

//         @Override
//         public int getActiveColor(UI ui) {
//             return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
//         }

//         @Override
//         public void onActivate() {
//             if (!outputControl.enabled.getValueb())
//                 return;
//             controller.enabled.toggle();
//         }

//         // @Override
//         // public void onDeactivate() {
//         //     println("onDeactivate");
//         //     controller.enabled.setValue(false);
//         // }
//     }
// }

/*
 * Gamma Correction
 *---------------------------------------------------------------------------*/
static final int redGamma[] = new int[256];
static final int greenGamma[] = new int[256];
static final int blueGamma[] = new int[256];

final float[][] gammaSet = {
  { 2, 2.1, 2.8 },
  { 2, 2.2, 2.8 },
};

final DiscreteParameter gammaSetIndex = new DiscreteParameter("GMA", gammaSet.length+1);
final BoundedParameter redGammaFactor = new BoundedParameter("RGMA", 2, 1, 4);
final BoundedParameter greenGammaFactor = new BoundedParameter("GGMA", 2.2, 1, 4);
final BoundedParameter blueGammaFactor = new BoundedParameter("BGMA", 2.8, 1, 4);

void setupGammaCorrection() {
  final float redGammaOrig = redGammaFactor.getValuef();
  final float greenGammaOrig = greenGammaFactor.getValuef();
  final float blueGammaOrig = blueGammaFactor.getValuef();
  gammaSetIndex.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      if (gammaSetIndex.getValuei() == 0) {
        redGammaFactor.reset(redGammaOrig);
        greenGammaFactor.reset(greenGammaOrig);
        blueGammaFactor.reset(blueGammaOrig);
      } else {
        redGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][0]);
        greenGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][1]);
        blueGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][2]);
      }
    }
  });
  redGammaFactor.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      buildGammaCorrection(redGamma, parameter.getValuef());
    }
  });
  buildGammaCorrection(redGamma, redGammaFactor.getValuef());
  greenGammaFactor.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      buildGammaCorrection(greenGamma, parameter.getValuef());
    }
  });
  buildGammaCorrection(greenGamma, greenGammaFactor.getValuef());
  blueGammaFactor.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      buildGammaCorrection(blueGamma, parameter.getValuef());
    }
  });
  buildGammaCorrection(blueGamma, blueGammaFactor.getValuef());
}

void buildGammaCorrection(int[] gammaTable, float gammaCorrection) {
  for (int i = 0; i < 256; i++) {
    gammaTable[i] = (int)(pow(1.0 * i / 255, gammaCorrection) * 255 + 0.5);
  }
}