import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public LXStudio lx;
public SLModel model;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public OutputControl outputControl;
public MappingMode mappingMode;

void setup() {
  long setupStart = System.nanoTime();
  size(1280, 800, P3D);

  model = buildModel();

  lx = new LXStudio(this, model) {
    @Override
    protected void initialize(LXStudio lx, LXStudio.UI ui) {

      // Output
      (dispatcher = new Dispatcher(lx)).start();
      (networkMonitor = new NetworkMonitor(lx)).start();
      setupGammaCorrection();
      setupOutputs(lx);
      outputControl = new OutputControl(lx);
      lx.engine.registerComponent("outputControl", outputControl);

      // Mapping
      mappingMode = new MappingMode(lx);
      //lx.engine.registerComponent("mappingMode", mappingMode);
      //lx.engine.addLoopTask((LXLoopTask)mappingMode);

      // try {
      //   lx.engine.output.gammaCorrection.setValue(1);
      //   lx.engine.output.enabled.setValue(false);
      //   lx.addOutput(getOutput(lx));
      // } catch (Exception x) {
      //   throw new RuntimeException(x);
      // }
        
      // OSC drivers
      // try {
      //   lx.engine.osc.receiver(3344).addListener(new EnvelopOscControlListener(lx));
      //   lx.engine.osc.receiver(3355).addListener(new EnvelopOscSourceListener());
      //   lx.engine.osc.receiver(3366).addListener(new EnvelopOscMeterListener());
      // } catch (SocketException sx) {
      //   throw new RuntimeException(sx);
      // }

      // lx.engine.midi.addListener(new LXMidiListener() {
      //   public void noteOnReceived(MidiNoteOn note) {
      //     println("noteOnReceived");
      //   }
      //   public void noteOffReceived(MidiNote note) {
      //     println("noteOffReceived");
      //   }
      //   public void controlChangeReceived(MidiControlChange cc) {
      //     println("controlChangeReceived");
      //   }
      //   public void programChangeReceived(MidiProgramChange pc) {
      //     println("programChangeReceived");
      //   }
      //   public void pitchBendReceived(MidiPitchBend pitchBend) {
      //     println("pitchBendReceived");
      //   }
      //   public void aftertouchReceived(MidiAftertouch aftertouch) {
      //     println("aftertouchReceived");
      //   }
      // });
        
      lx.registerPatterns(new Class[]{
        heronarts.p3lx.pattern.SolidColorPattern.class,
        IteratorTestPattern.class
      });
      lx.registerEffects(new Class[]{ 
        FlashEffect.class,
        BlurEffect.class,
        DesaturationEffect.class
      });
    
      ui.theme.setPrimaryColor(#008ba0);
      ui.theme.setSecondaryColor(#00a08b);
      ui.theme.setAttentionColor(#a00044);
      ui.theme.setFocusColor(#0094aa);
      ui.theme.setControlBorderColor(#292929);
    } 
    
    @Override
    protected void onUIReady(LXStudio lx, LXStudio.UI ui) {
      ui.leftPane.audio.setVisible(true);
      //ui.main.setPhi(PI/32).setMinRadius(2*FEET).setMaxRadius(48*FEET);
      new UIOutputs(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);
      new UIMapping(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 4);
    }
  };
  long setupFinish = System.nanoTime();
  println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

void draw() {
  background(lx.ui.theme.getDarkBackgroundColor());
}