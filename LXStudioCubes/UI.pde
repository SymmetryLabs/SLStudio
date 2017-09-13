class UIArtNet extends UICollapsibleSection {

    public UIArtNet(UI ui, LX lx, float x, float y, float w) {
        super(ui, x, y, w, 60);
        setTitle("ArtNet");
        setBackgroundColor(#404040); //ui.theme.getDeviceBackgroundColor()
        setBorderRounding(4);

        final UIButton button = new UIButton(5, 5, 30, 30);
        button.setLabel("on");
        button.setParameter(artNetRunner.running);
        button.addToContainer(this);
    }

}

// class MappableColors extends UICollapsibleSection {

//     public final List<MappableColor> mappableColors = new ArrayList<MappableColor>();

//     public MappableColors(UI ui, LX lx, float x, float y, float w) {
//         super(ui, x, y, w, 10*50);
//         setTitle("Mappable Colors");
//         setBackgroundColor(#404040); //ui.theme.getDeviceBackgroundColor()
//         setBorderRounding(4);

//         for (int i = 0; i < 5; i++) {
//             float ly = (y + MappableColor.HEADER_HEIGHT) + (i * MappableColor.HEIGHT);
//             MappableColor mappableColor = new MappableColor(i, 0, ly, w, MappableColor.HEIGHT);
//             this.mappableColors.add(mappableColor);
//             mappableColor.addToContainer(this);
//         }
//     }

//     private class MappableColor extends UI2dContainer {
//         private static final float HEADER_HEIGHT = 5;
//         private static final float HEIGHT = 70;

//         private UI2dContainer self = this;
//         private float y;

//         public final BooleanParameter enabled = new BooleanParameter("enabled", false);
//         public final CompoundParameter hue;

//         private MappableColor(int index, float x, float y, float w, float h) {
//             super(x, y, w, h);
//             this.setBackgroundColor(#232323);

//             this.y = HEADER_HEIGHT + index*HEIGHT;

//             this.hue = new CompoundParameter("color " + index, 0, 0, 360);;

//             new UILabel(5, x, 50, 12)
//             .setLabel("Color " + index)
//             .addToContainer(this);

//             enabled.setMode(BooleanParameter.Mode.MOMENTARY);
//             final UIButton button = new UIButton(60, y+12, 30, 30);
//             button.setLabel("on");
//             button.setParameter(enabled);
//             button.addToContainer(this);

//             final UIKnob knob = new UIKnob(5, y+12);
//             knob.setParameter(hue);
//             knob.addToContainer(this);

//             hue.addListener(new LXParameterListener() {
//                 public void onParameterChanged(LXParameter p) {
//                     //self.setBackgroundColor(lx.hsb(hue.getValuef(), 100, 100));
//                     redraw();
//                 }
//             });
//         }
//     }

// }

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