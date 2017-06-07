// import java.util.Collections;
// import java.util.HashMap;
// import java.util.Map;
// import processing.core.PConstants;

// public class UIOverriddenMixer extends UI2dContainer {

//   public final static int PADDING = 6;
//   private final static int CHILD_MARGIN = 1;
//   public final static int STRIP_SPACING = UIMixerStripControls.WIDTH + CHILD_MARGIN;
//   public final static int HEIGHT = UIMixerStrip.HEIGHT + 2*PADDING;

//   private final Map<LXChannel, UIChannelStrip> mutableChannelStrips = new HashMap<LXChannel, UIChannelStrip>();
//   public final Map<LXChannel, UIChannelStrip> channelStrips = Collections.unmodifiableMap(this.mutableChannelStrips);

//   public final UIButton addChannelButton;
//   public final UIMasterStrip masterStrip;
//   public final UISceneStrip sceneStrip;

//   final LX lx;

//   public UIOverriddenMixer(final UI ui, final LX lx, float x, float y, float h) {
//     super(x, y, 0, h);
//     this.lx = lx;

//     setBackgroundColor(ui.theme.getPaneInsetColor());
//     setBorderRounding(4);
//     setLayout(UI2dContainer.Layout.HORIZONTAL);
//     setChildMargin(CHILD_MARGIN);
//     setPadding(0, PADDING, 0, PADDING);

//     for (LXChannel channel : lx.engine.getChannels()) {
//       UIChannelStrip strip = new UIChannelStrip(ui, this, lx, channel);
//       this.mutableChannelStrips.put(channel, strip);
//       strip.addToContainer(this);
//     }

//     this.addChannelButton = new UIButton(0, PADDING + UIClipLauncher.HEIGHT + UIMixerStrip.SPACING, 20, UIMixerStripControls.HEIGHT) {
//       @Override
//       public void onToggle(boolean on) {
//         if (!on) {
//           lx.engine.addChannel();
//           lx.engine.focusedChannel.setValue(lx.engine.getChannels().size()-1);
//         }
//       }
//     };
//     this.addChannelButton
//     .setLabel("+")
//     .setMomentary(true)
//     .setInactiveColor(0xff393939) // TODO(mcslee): control disabled color?
//     .setBorder(false)
//     .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
//     .setDescription("New Channel: add another channel")
//     .addToContainer(this);

//     this.masterStrip = (UIMasterStrip) new UIMasterStrip(ui, this, lx).addToContainer(this);
//     this.sceneStrip = (UISceneStrip) new UISceneStrip(ui, this, lx).addToContainer(this);

//     lx.engine.addListener(new LXEngine.Listener() {
//       public void channelAdded(LXEngine engine, LXChannel channel) {
//         UIChannelStrip strip = new UIChannelStrip(ui, UIMixer.this, lx, channel);
//         mutableChannelStrips.put(channel, strip);
//         strip.addToContainer(UIMixer.this, channel.getIndex());
//       }

//       public void channelRemoved(LXEngine engine, LXChannel channel) {
//         mutableChannelStrips.remove(channel).removeFromContainer();
//       }

//       public void channelMoved(LXEngine engine, LXChannel channel) {
//         mutableChannelStrips.get(channel).setContainerIndex(channel.getIndex());
//       }
//     });
//   }

// }