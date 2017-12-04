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

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;void setupOutputs(final LX lx) {
  // final Pixlite testPixlite = setupTestBroadcastPixlite(lx);
  // lx.addOutput(testPixlite);

  for (Pixlite pixlite : pixlites) {
    pixlite.enabled.setValue(true);
    lx.addOutput(pixlite);
  }

  // outputControl.testBroadcast.addListener(new LXParameterListener() {
  //   public void onParameterChanged(LXParameter parameter) {
  //     if (((BooleanParameter)parameter).getValueb()) {
  //       testPixlite.enabled.setValue(true);
  //       for (Pixlite pixlite : pixlites) {
  //         pixlite.enabled.setValue(false);
  //       }
  //     } else {
  //       testPixlite.enabled.setValue(false);
  //       for (Pixlite pixlite : pixlites) {
  //         pixlite.enabled.setValue(true);
  //       }
  //     }
  //   }
  // });
}

// Pixlite setupTestBroadcastPixlite(LX lx) {
//   Slice slice = null;

//   for (Slice s : model.slices) {
//     if (s.type == Slice.Type.FULL) {
//       s = slice;
//       break;
//     }
//     println("No full slice in model!!! Test Broadcast needs at least one full sun!!!");
//   }

//   return new Pixlite(lx, "10.200.1.255", slice);
// }

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