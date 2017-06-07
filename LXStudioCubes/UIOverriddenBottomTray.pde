// import java.util.Collections;
// import java.util.HashMap;
// import java.util.Map;
// import processing.core.PGraphics;
// import processing.event.KeyEvent;

// public class UIOverriddenBottomTray extends UI2dContext {

//   public static final int PADDING = 8;
//   public static final int HEIGHT = UIOverriddenMixer.HEIGHT + 2*PADDING;
//   public static final int CLOSED_HEIGHT = UIMixerStripControls.HEIGHT + 2*UIOverriddenMixer.PADDING + 2*PADDING;
//   private static final int SEPARATOR = 16;

//   private final UI ui;
//   private final LX lx;
//   public final UIOverriddenMixer mixer;
//   public final UI2dContainer rightSection;

//   private final Map<LXBus, UIDeviceBin> mutableDeviceBins = new HashMap<LXBus, UIDeviceBin>();
//   public final Map<LXBus, UIDeviceBin> deviceBins = Collections.unmodifiableMap(this.mutableDeviceBins);
//   public final UIClipView clipView;

//   public UIOverriddenBottomTray(UI ui, LX lx) {
//     super(ui, 0, ui.getHeight() - HEIGHT - UIContextualHelpBar.VISIBLE_HEIGHT, ui.getWidth(), HEIGHT);
//     this.ui = ui;
//     this.lx = lx;
//     setBackgroundColor(ui.theme.getPaneBackgroundColor());

//     this.mixer = new UIOverriddenMixer(ui, lx, PADDING, PADDING, HEIGHT-2*PADDING);
//     this.mixer.addToContainer(this);

//     float rightX = getRightSectionX();
//     this.rightSection = (UI2dContainer)
//       new UI2dContainer(rightX, PADDING, getContentWidth() - rightX - PADDING, UIOverriddenMixer.HEIGHT)
//       .setBackgroundColor(ui.theme.getPaneInsetColor())
//       .setBorderRounding(4)
//       .addToContainer(this);

//     this.clipView = new UIClipView(ui, lx, UIDeviceBin.PADDING, UIDeviceBin.PADDING, this.rightSection.getContentWidth() - 2*UIDeviceBin.PADDING);
//     this.clipView.addToContainer(this.rightSection);

//     for (LXChannel channel : lx.engine.getChannels()) {
//       addChannel(channel);
//     }
//     addChannel(lx.engine.masterChannel);

//     lx.engine.addListener(new LXEngine.Listener() {
//       public void channelAdded(LXEngine engine, LXChannel channel) {
//         addChannel(channel);
//         onChannelFocus();
//       }

//       public void channelRemoved(LXEngine engine, LXChannel channel) {
//         removeChannel(channel);
//         onChannelFocus();
//       }

//       public void channelMoved(LXEngine engine, LXChannel channel) {
//         onChannelFocus();
//       }
//     });

//    lx.engine.focusedChannel.addListener(new LXParameterListener() {
//       public void onParameterChanged(LXParameter p) {
//         onChannelFocus();
//       }
//     });

//     reflow();
//     onChannelFocus();
//   }

//   private float getRightSectionX() {
//     return this.mixer.getWidth() + SEPARATOR;
//   }

//   private void addChannel(LXBus channel) {
//     UIDeviceBin deviceBin = new UIDeviceBin(ui, channel, this.rightSection.getContentHeight() - UIDeviceBin.HEIGHT - UIDeviceBin.PADDING, this.rightSection.getContentWidth() - 2*UIDeviceBin.PADDING);
//     this.mutableDeviceBins.put(channel, deviceBin);
//     deviceBin.setVisible(false);
//     deviceBin.addToContainer(this.rightSection);
//   }

//   private void removeChannel(LXBus channel) {
//     this.mutableDeviceBins.remove(channel).removeFromContainer();
//   }

//   void onChannelFocus() {
//     LXBus focusedChannel = lx.engine.getFocusedChannel();
//     for (LXBus channel : this.mutableDeviceBins.keySet()) {
//       UIDeviceBin deviceBin = this.mutableDeviceBins.get(channel);
//       deviceBin.setVisible(channel == focusedChannel);
//     }
//   }

//   @Override
//   public void reflow() {
//     float deviceX = this.mixer.getWidth() + SEPARATOR;
//     if (this.rightSection != null) {
//       this.rightSection.setX(deviceX);
//       this.rightSection.setWidth(getContentWidth() - deviceX - PADDING);
//       if (this.clipView != null) {
//         this.clipView.setWidth(this.rightSection.getWidth() - 2*UIDeviceBin.PADDING);
//       }
//       for (UIDeviceBin deviceBin : this.mutableDeviceBins.values()) {
//         deviceBin.setWidth(this.rightSection.getContentWidth() - 2*UIDeviceBin.PADDING);
//       }
//     }
//   }

//   @Override
//   public void onDraw(UI ui, PGraphics pg) {
//     pg.stroke(ui.theme.getPrimaryColor());
//     float channelX = PADDING + UIMixer.PADDING + UIMixer.STRIP_SPACING * lx.engine.focusedChannel.getValuei() + UIMixerStripControls.WIDTH/2;
//     float binX = this.mixer.getX() + this.mixer.getWidth() + SEPARATOR + 12;
//     float b = 4;
//     pg.strokeWeight(2);
//     pg.line(channelX, this.height-PADDING, channelX, this.height-b-1);
//     pg.line(binX, this.height-b-1, binX, this.height-PADDING);
//     pg.line(channelX+1, this.height-b, binX-1, this.height-b);
//     pg.strokeWeight(1);
//   }

//   @Override
//   public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
//     if (keyCode == java.awt.event.KeyEvent.VK_N && (keyEvent.isControlDown() || keyEvent.isMetaDown())) {
//       lx.engine.addChannel();
//       lx.engine.focusedChannel.setValue(lx.engine.getChannels().size()-1);
//     }
//   }

// }