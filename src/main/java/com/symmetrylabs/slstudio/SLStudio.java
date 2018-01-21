package com.symmetrylabs.slstudio;

import java.util.Map;

import com.symmetrylabs.layouts.Layout;
import processing.core.PApplet;

import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.output.OPCOutput;

import com.symmetrylabs.layouts.cubes.CubesLayout;
import com.symmetrylabs.slstudio.mappings.Mappings;
import com.symmetrylabs.slstudio.output.OutputControl;
import com.symmetrylabs.slstudio.palettes.ArrayPalette;
import com.symmetrylabs.slstudio.palettes.ImageLibrary;
import com.symmetrylabs.slstudio.palettes.LinePaletteExtractor;
import com.symmetrylabs.slstudio.palettes.PaletteExtractor;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import com.symmetrylabs.slstudio.performance.APC40Listener;
import com.symmetrylabs.slstudio.performance.FoxListener;
import com.symmetrylabs.slstudio.performance.PerformanceManager;
import com.symmetrylabs.slstudio.pixlites.Pixlite;
import com.symmetrylabs.slstudio.ui.UISpeed;
import com.symmetrylabs.util.BlobTracker;
import com.symmetrylabs.util.DrawHelper;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.Utils;

import static com.symmetrylabs.util.DistanceConstants.*;


public class SLStudio extends PApplet {

    public static SLStudio applet;

    private SLStudioLX lx;
    private Layout layout;
    private Dispatcher dispatcher;
    private Mappings mappings;
    public OutputControl outputControl;
    public Pixlite[] pixlites;
    public APC40Listener apc40Listener;
    public PerformanceManager performanceManager;
    private BlobTracker blobTracker;

    public final BooleanParameter mappingModeEnabled = new BooleanParameter("Mappings");
    public Map<String, int[]> mappingColorsPerPixlite;

    static public void main(String[] passedArgs) {
        System.setProperty("com.aparapi.enableShowGeneratedOpenCL", "true");
        System.setProperty("com.aparapi.dumpProfilesOnExit", "true");

        String[] appletArgs = new String[] { SLStudio.class.getName() };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }

    @Override
    public void settings() {
        size(displayWidth, displayHeight, P3D);
    }

    @Override
    public void setup() {
        long setupStart = System.nanoTime();
        applet = this;

        Utils.setSketchPath(sketchPath());

        layout = new CubesLayout();

        LXModel model = layout.buildModel();
        printModelStats(model);

        PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();
        loadPalettes(paletteLibrary);

        new SLStudioLX(this, model, true) {

            @Override
            protected void initialize(SLStudioLX lx, SLStudioLX.UI ui) {
                SLStudio.this.lx = lx;
                super.initialize(lx, ui);

                SLStudio.this.dispatcher = Dispatcher.getInstance(lx);

                layout.setupLx(lx);

                lx.addOutput(new OPCOutput(lx, "localhost", 11122));

                outputControl = new OutputControl(lx);
                lx.engine.registerComponent("outputControl", outputControl);
                pixlites = setupPixlites();

                SLStudio.this.apc40Listener = new APC40Listener(lx);
                new FoxListener(lx);

                SLStudio.this.performanceManager = new PerformanceManager(lx);
                lx.engine.registerComponent("performanceManager", performanceManager);

                blobTracker = BlobTracker.getInstance(lx);

                ui.theme.setPrimaryColor(0xff008ba0);
                ui.theme.setSecondaryColor(0xff00a08b);
                ui.theme.setAttentionColor(0xffa00044);
                ui.theme.setFocusColor(0xff0094aa);
                ui.theme.setControlBorderColor(0xff292929);
            }

            @Override
            protected void onUIReady(SLStudioLX lx, SLStudioLX.UI ui) {
                ui.leftPane.audio.setVisible(true);
                ui.preview.setCenter(lx.model.cx, lx.model.cy, lx.model.cz);
                ui.preview.setPhi(0).setMinRadius(0 * FEET).setMaxRadius(150 * FEET).setRadius(150 * FEET);
                new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);

                layout.setupUi(lx, ui);
            }
        };

        lx.engine.isChannelMultithreaded.setValue(true);
        lx.engine.isNetworkMultithreaded.setValue(true);
        lx.engine.audio.enabled.setValue(true);
        lx.engine.output.enabled.setValue(false);

        performanceManager.start(lx.ui);

        long setupFinish = System.nanoTime();
        println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms");
    }

    void printModelStats(LXModel model) {
        println("-- Model ----");
        println("# of points: " + model.points.length);
        println("model.xMin: " + model.xMin);
        println("model.xMax: " + model.xMax);
        println("model.xRange: " + model.xRange);
        println("model.yMin: " + model.yMin);
        println("model.yMax: " + model.yMax);
        println("model.yRange: " + model.yRange);
        println("model.zMin: " + model.zMin);
        println("model.zMax: " + model.zMax);
        println("model.zRange: " + model.zRange + "\n");
    }

    void loadPalettes(PaletteLibrary pl) {
    /* Images loaded remotely from the Internet */
    /*
    pl.set("cities.london", new com.symmetrylabs.slstudio.palettes.DeckChairSource("5568230b7b2853502527fd4e"), new com.symmetrylabs.slstudio.palettes.ArcPaletteExtractor(0.44, 100));
    pl.set("cities.paris", new com.symmetrylabs.slstudio.palettes.DeckChairSource("5568862a7b28535025280c72"), new com.symmetrylabs.slstudio.palettes.ArcPaletteExtractor(0.46, 100));
    pl.set("cities.sydney", new com.symmetrylabs.slstudio.palettes.DeckChairSource("599d6375096641f2272bacf4"), new com.symmetrylabs.slstudio.palettes.ArcPaletteExtractor(1, 100));
    pl.set("cities.san_francisco", new com.symmetrylabs.slstudio.palettes.UrlImageSource("http://icons.wunderground.com/webcamramdisk/a/m/ampledata/1/current.jpg"), new com.symmetrylabs.slstudio.palettes.ArcPaletteExtractor(0.65, 100));
    pl.set("sunsets.sunset", new com.symmetrylabs.slstudio.palettes.UrlImageSource("https://pbs.twimg.com/media/DO9Ok2JU8AEjXa1.jpg"), new com.symmetrylabs.slstudio.palettes.ArcPaletteExtractor(0.622, 100));
    pl.set("sunsets.orange", new com.symmetrylabs.slstudio.palettes.UrlImageSource("https://c.pxhere.com/photos/0e/29/sunrise_beach_sea_ocean_water_sunset_sky_sun-1332581.jpg!d"), new com.symmetrylabs.slstudio.palettes.ArcPaletteExtractor(0.5, 100));
    */

        pl.put("sky.orange", new ZigzagPalette(new int[]{
            0x230402, 0x2d0a06, 0x340b05, 0x3a0b05, 0x3a0501, 0x420602, 0x520701,
            0x7c1103, 0xfe9100, 0xfdc200, 0xfdee00, 0xfdfc00, 0xfefe00, 0xfefb00,
            0xfff507, 0xfde80a, 0xfcd905, 0xfec601, 0xfdbc00, 0xfeb500, 0xfdb000,
            0xfeb000, 0xfdac00, 0xfea700, 0xfda800, 0xfea800, 0xfea700, 0xfda600,
            0xfda100, 0xfe9900, 0xfc8500, 0xfd6a00, 0xfc5000, 0x3b0401, 0x2b0400,
            0x260402, 0x270907, 0x220905, 0x1e0606, 0x200b08, 0x1d0a07
        }));

        pl.put("galaxies", new ArrayPalette(new int[]{
            0x34240f, 0x33292c, 0x4f2812, 0x251217, 0x342942, 0x36212b, 0x382331,
            0x3e2c3d, 0x412627, 0x493140, 0x332126, 0x381a38, 0x7b5e6a, 0x3f2517,
            0x38271f, 0x252a2e, 0x2f2131, 0x352834, 0x412633, 0x4e223b, 0x2e2231,
            0x372336, 0x331c23, 0x35211a, 0x3b2436, 0x3c1e2d, 0x39262c, 0x312629,
            0x402a30, 0x3c263e, 0x3b2533, 0x412834, 0x43272c, 0x3e222c, 0x3e2325,
            0x3e2337, 0x3a2226, 0x35241b, 0x312432, 0x392241, 0x3a252e, 0x3b261e,
            0x413534, 0x49353c, 0x2d3151, 0x4d3639, 0x4f2d35, 0x4d243b, 0x44273f,
            0x4b3541, 0x493037, 0x4c3030, 0x462c2a, 0x3f2a3e, 0x462e41, 0x4d293a,
            0x462b38, 0x4c2d40, 0x46372f, 0x463538, 0x48323d, 0x483a44, 0x482f48,
            0x52302f, 0x63433d, 0x4c2b3c, 0x443434, 0x3d3235, 0x3c2b2f, 0x3d2845,
            0x3c281f, 0x38273a, 0x322831, 0x352530, 0x3a2225, 0x40261f, 0x362625,
            0x3b3123, 0x594035, 0x412f2f, 0x3b221f, 0x3a242d, 0x453638, 0x5e4a46,
            0x423333, 0x3c2e27, 0x463738, 0x3b2739, 0x38252d, 0x402e25, 0x412d1f,
            0x36212a, 0x312329, 0x372622, 0x34241c, 0x3f2c2d, 0x938997, 0x3c2934,
            0x35282d, 0x3d2c36, 0x3d283a, 0x332733, 0x35221d, 0x372629, 0x3d232a,
            0x332934, 0x606280, 0x68566b, 0x3c2c2e, 0x513d4d, 0x3a2c1c, 0x6b4a51,
            0x3e2928, 0x3a3531, 0x3c2933, 0x372923, 0x3a242a, 0x4a373f, 0x342136,
            0x4c3239, 0x857d8b, 0x837788, 0x372c33, 0x2e2223, 0x2f242f, 0x301a28,
            0x382b2f, 0x372a35, 0x3c2c33, 0x56434f, 0x3f302d, 0x382b33, 0x514e65,
            0x3b2830, 0x342a2d, 0x3e2c34, 0x43343f, 0x39283c, 0x3c2f39, 0x917fa2,
            0x45303c, 0x8a7486, 0x453832, 0x3b2f31, 0x42302c, 0x3f3034, 0x352c2b,
            0x342a2b, 0x473d41, 0x3d2c32, 0x3a2a35, 0x382b2e, 0x3f2e27, 0x352740,
            0x3a2c35, 0x403135, 0x402d26, 0x372f2a, 0x3b2f2e, 0x3d2822, 0x332434,
            0x3e2e31, 0x422f2c, 0x3d2f2b, 0x412f2b, 0x3b2e2f, 0x352529, 0x463427,
            0x362a2c, 0x3e3235, 0x382737, 0x352b2d, 0x3a2e25, 0x342629, 0x342a31,
            0x2f2220, 0x3a2d28, 0x33282a, 0x372933, 0x484047, 0x35292f, 0x362e3c,
            0x3c323a, 0x3e4158, 0x39242e, 0x403641, 0x3b3532, 0x3c2f20, 0x372e39,
            0x362c32, 0x322733, 0x37292b, 0x503945, 0x433d33, 0x3b3647, 0x3f3835,
            0x44393a, 0x31363e, 0x41353e, 0x353243, 0x3c333c, 0x38313d, 0x544f53,
            0x423635, 0x3a3b4d, 0x3a393b, 0x3d3741, 0x3c3237, 0x382d33, 0x3a2c39,
            0x35302e, 0x362e32, 0x252b31, 0x37302e, 0x42433d, 0x37342b, 0x32342f,
            0x322b30, 0x352832, 0x342b34, 0x332f32, 0x383035, 0x2e2c2c, 0x39313f,
            0x2a2a2b, 0x32292a, 0x26262e, 0x322c31, 0x2b2e31, 0x2b2b29, 0x2c273e,
            0x35322b, 0x372c36, 0x342e37, 0x302a3a, 0x363341, 0x332f33, 0x2b2b35,
            0x9a8593, 0x393226, 0x352b32, 0x2d2d31, 0x423234, 0x3a3438, 0x3a303f,
            0x3d3a41, 0x382f3d, 0x3d3329, 0x35322f, 0x4c4947, 0x2d2932, 0x282c3b,
            0x282832, 0x2f292f, 0x2a2e39, 0x272e35, 0x322e36, 0x2a2b31, 0x303045,
            0x605875, 0x2b262e, 0x252a34, 0x352a36, 0x2f324a, 0x30252c, 0x282828,
            0x222126, 0x282524, 0x262528, 0x3d2d31, 0x3e4545, 0x282e32, 0x272634,
            0xa2a6b4, 0x22262a, 0x23292f, 0x24253b, 0x2b262e, 0x2d2c2a, 0x202630,
            0x262834, 0x26292b, 0x282734, 0x333843, 0x2f2c34, 0x212a3c, 0x383d5f,
            0x252a35, 0x636264, 0x212835, 0x292c3c, 0x292d32, 0x282d37, 0x20313f,
            0x3e4451, 0x323343, 0x29323e, 0x212c43, 0x23384e, 0x2a3554, 0x33344c,
            0x26363f, 0x2d3040, 0x5b5985, 0x303756, 0x374655, 0x323f45, 0x253956,
            0x383f4d, 0x334d65, 0x314461, 0x364454, 0x3c3f50, 0x30395a, 0x3c456c,
            0xb19da1, 0x324360, 0x2f4654, 0x38425d, 0x324265, 0x373f5e, 0x31406d,
            0x373e5d, 0x313d55, 0x2f3d63, 0x35445e, 0x364d67, 0x3d475d, 0x3c3e56,
            0x383f57, 0x595c7e, 0x3c425f, 0x414357, 0x38435a, 0x3f4868, 0x3c4c4e,
            0x41475b, 0x43415d, 0x413857, 0x52405c, 0x454556, 0x3b3b61, 0x3b3a44,
            0x6a7697, 0x3d3e56, 0x444249, 0x523e3f, 0x3b4150, 0x4c4855, 0x3c405a,
            0x45414a, 0x3c4651, 0x363853, 0x8b9ac5, 0x575f65, 0x3b3a44, 0x4d526c,
            0x3d475b, 0x373b52, 0x303949, 0x353939, 0x3a3747, 0x3d3648, 0x443b3b,
            0x3c3230, 0x47414c, 0x4e5257, 0x433d3e, 0xab9593, 0x263a5d, 0x323349,
            0x3b3736, 0x383645, 0x3a3438, 0x353537, 0x3e2d38, 0x363138, 0x362d2c,
            0x3c383d, 0x413034, 0x32353e, 0x3f4034, 0x424343, 0x433f4d, 0x403b43,
            0x3c4359, 0xae8b86, 0x484a5c, 0x444362, 0x5a5965, 0x494442, 0x50424f,
            0x3a393e, 0x3f353e, 0x45403c, 0xcfbdcf, 0x4b3840, 0x3f3124, 0x423138,
            0x3a3233, 0x3a3535, 0x413631, 0x3d3133, 0x383539, 0x3d2f3b, 0x45332e,
            0x463229, 0x7f6c77, 0x3c352f, 0x39322a, 0x3d3732, 0x433729, 0x484136,
            0x41322f, 0x51372c, 0x5a3e1d, 0x3c2d22, 0x3f332f, 0x463224, 0x392e2e,
            0x272f31, 0x3e2f2a, 0x402d32, 0x3e2b1a, 0x3e2f1c, 0x482a27, 0x3d352d,
            0x39382d, 0x422d22, 0x43352f, 0x533f30, 0x473432, 0x49311e, 0x43393a,
            0x3e3022, 0x4e4950, 0x47382f, 0x44363a, 0x373a31, 0x3e3728, 0x462e2d,
            0x3c3226, 0x423525, 0x49341f, 0x48332b, 0x44312d, 0x463530, 0x857472,
            0x4b3724, 0x564737, 0x3d2b17, 0x433325, 0x3d2e21, 0x423530, 0x3f331f,
            0x3d414a, 0x4e332a, 0x42424a, 0x8780a8, 0x403e43, 0x3c342c, 0x2e3331,
            0x4d342c, 0x3e2d15, 0x463521, 0x4b321f, 0x3e312d, 0x3e2f1d, 0x69728e,
            0xa28088, 0xf1f0f5, 0x6e4a4a, 0x3e3231, 0x4a352f, 0x4d3620, 0x3d2f2a,
            0x3e2e22, 0x312735, 0x372b22, 0x3d3744, 0x5b524e, 0x8898ca, 0x704f47,
            0x34353f, 0x3b3a2f, 0x4d3830, 0x61423a, 0x403425, 0x412d29, 0x403327,
            0x3c3428, 0x3c382d, 0x3f3730, 0x443433, 0x413025, 0x383129, 0x363132,
            0x403030, 0x45312b, 0x43352a, 0x32352f, 0x353338, 0x3b3430, 0x4f3426,
            0x3f312b, 0x453117, 0x372f1e, 0x443020, 0x492a20, 0x44302e, 0x402f1b,
            0x492e25, 0x3d3028, 0x403526, 0x3e3223, 0x43331b, 0x3b2e20, 0x323137,
            0x43301b, 0x3a3841, 0x37333b, 0x402f33, 0x33342b, 0x3e3035, 0x3d3524,
            0x3a3630, 0x373321, 0x41322f, 0x483015, 0x453a37, 0x453336, 0x3e3043,
            0x775152, 0x3b3636, 0x4e383a, 0x4c3531, 0x4f3f39, 0x6a5034, 0x3c323f,
            0x494855, 0x3c3434, 0x4a3425, 0x56351a, 0x4a3b2e, 0x363e4a, 0x4b566e,
            0x443949, 0x5c423b, 0x4b331d, 0x3d352e, 0x45342e, 0x40362f, 0x3f3429,
            0x543632, 0x543244, 0x4a342b, 0x4e3932, 0x533f22, 0x433939, 0x3e362b,
            0x423725, 0x503832, 0x51320f, 0x493729, 0x533927, 0x493735, 0x4b3536,
            0x51363a, 0x413248, 0x564d4f, 0x483830, 0x503526, 0x4a3934, 0x464042,
            0x5e3c43, 0x4f3a2a, 0x4e403a, 0x533c43, 0x5f3754, 0x533f3e, 0x554c4b,
            0x6b5e5d, 0x59423e, 0x53463c, 0x4d4143, 0x46424f, 0x493b4b, 0x414346,
            0x553e36, 0x634a4f, 0x5b4843, 0x6c7797, 0x504443, 0x5b454c, 0x3e3a3a,
            0x49392b, 0x4f3d48, 0x513d3a, 0x594a46, 0x494454, 0x4d4544, 0x404443,
            0x4a4039, 0x4a3b4b, 0x503d56, 0x563f33, 0x524341, 0x54463f, 0x4f4c57,
            0x504653, 0x664a4c, 0x4b454d, 0x4e454c, 0x4f4751, 0x564550, 0x524c4a,
            0x4b4543, 0x4c4343, 0x403f4d, 0x48464b, 0x3d4345, 0x443f45, 0x483d3f,
            0x3e4048, 0x444144, 0x3f3d3f, 0x464146, 0x3c3f42, 0x635058, 0x443c45,
            0x5a4950, 0x7e6b8d, 0x383633, 0x2c2c30, 0x372d39, 0x31302b, 0x342b29,
            0x3a302b, 0x39313a, 0x382836, 0x362a33, 0x30262c, 0x42383e, 0x3a2e24,
            0xd8d3e8, 0x3c3938, 0x2b2522, 0x312429, 0x34313c, 0x39312f, 0x313240,
            0x37313e, 0x333f52, 0x4d628a, 0x7593b0, 0x3d557a, 0x355482, 0x473d57,
            0x3f424f, 0x333c4d, 0x30364d, 0x2c3036, 0x343134, 0x383b3e, 0x767b8c,
            0x7c6e81, 0x3a3a3f, 0x4a4b50, 0x444151, 0x3c3b54, 0x5e5349, 0x4b3c36,
            0x4c4146, 0x35374d, 0x504e61, 0x2b3c47, 0x4f5467, 0x2e323d, 0x2e3343,
            0x222a2e, 0x252e35, 0x2a3032, 0x2b3441, 0x272c40, 0x403934, 0x302e38,
            0x363844, 0x2f303a, 0x2a2935, 0x302f3e, 0x4b5066, 0x303339, 0x263346,
            0x3b383d, 0x273339, 0x26313c, 0x2f2c40, 0x424663, 0x676876, 0x6f5d5a,
            0x272b35, 0x1b1f24, 0x7d708b, 0x292627, 0x342e2d, 0x28303a, 0x333233,
            0x31383e, 0x382c2c, 0x2d2e2c, 0x27282a, 0x2b2a25, 0x2e2829, 0x2a2329,
            0x37343b, 0x362f3b, 0x23201e, 0x242423, 0x1e2729, 0x4f5373, 0x2e3539,
            0x1f2629, 0x1e2120, 0x242322, 0x1f1d1f, 0x1e1e20, 0x212227, 0x21221e,
            0x1c2425, 0x25261b, 0x272828, 0x20221d, 0x1c2222, 0x202327, 0x152434,
            0x22242d, 0x212a2a, 0x302932, 0x222522, 0x282927, 0x243030, 0x212627,
            0x2c343b, 0x2b315e, 0x5f5967, 0x563856, 0x48313e, 0x50363d, 0x583a45,
            0x4a3a4f, 0x3c2b33, 0x4a2e3c, 0x463c53, 0x3d354d, 0x8f7ba3, 0x4a344d,
            0x422b4f, 0x422c40, 0x7a4453, 0x5c2f46, 0x603743, 0x6d3e55, 0x7c3e4f,
            0x6e354f, 0x823f5b, 0x7b3b4f, 0x873f49, 0x773b4d, 0x6b404a, 0x6c365c,
            0x703c50, 0x77454e, 0x683949, 0x673b51, 0x62364a, 0x5c373c, 0x603950,
            0x52333c, 0x503140, 0x52333f, 0x62436c, 0x4a3749, 0x4e3142, 0x533d52,
            0x4e2e4b, 0x4b2c40, 0x56343e, 0x593547, 0x64566b, 0x694e67, 0x5f3c60,
            0x543745, 0x5c3246, 0x5b364b, 0x6e445d, 0x713c60, 0x623347, 0x644266,
            0x734a6d, 0x5f364f, 0x71486d, 0xdab2d5, 0x573660, 0x5d3b60, 0x55366d,
            0x50395c, 0x543c69, 0x544677, 0x53446a, 0x594f77, 0x4f4782, 0x4e4a7e,
            0x8577a2, 0x4c528f, 0x575f9c, 0x445fa4, 0x4960a3, 0x5d69a7, 0x56639e,
            0x5167b3, 0x5371b4, 0x5f77ab, 0x5b74ad, 0x5471af, 0x5866ae, 0x5b5ea0,
            0x9a8ba8, 0x524f8a, 0x5b568c, 0x53558c, 0x80679f, 0x5a5085, 0x6d5080,
            0x69679f, 0x764d7e, 0x624d7f, 0x744f84, 0x9c6981, 0x6a4774, 0x674c76,
            0x6f477b, 0x6a4778, 0x6c4675, 0xc0a9bb, 0x71426f, 0x694479, 0x90698b,
            0x754873, 0x793a6f, 0x905983, 0x674a7d, 0x844b70, 0x824871, 0x713e71,
            0x6c3d6a, 0x79416f, 0x78436c, 0x6c3d70, 0x734769, 0x7d4671, 0x784869,
            0x774872, 0x824073, 0x834676, 0x8b4c76, 0x8d4679, 0x884987, 0x9767a2,
            0x914b88, 0x8d4681, 0x995494, 0x974783, 0x94457d, 0x99457e, 0x974e7b,
            0x983f76, 0x94457d, 0x973d6f, 0x96417b, 0x96346a, 0x97406c, 0x882b5f,
            0x8e2e6f, 0x9d3d80, 0x963c71, 0x8f2f60, 0x912d5d, 0x99335f, 0x912f57,
            0x973867, 0x963a65, 0xa23f74, 0xaa3b6f, 0xa23d75, 0xa24368, 0xa14679,
            0xad5283, 0x9d3f74, 0x864872, 0x794b75, 0x89476b, 0x844166, 0x893a6f,
            0x923a64, 0x95417a, 0x93416e, 0x983d6d, 0x964768, 0x8b3d5c, 0xad6a7c,
            0x864761, 0x7f4757, 0x874263, 0x894255, 0x823d4d, 0x8e4c5f, 0x805064,
            0x8c516f, 0x865a49, 0x844655, 0x884160, 0x904a64, 0x785353, 0x7c4245,
            0x7f453a, 0x834d5a, 0x704859, 0x7b495d, 0x724365, 0x67454e, 0x6c4642,
            0x643a5c, 0x7a495e, 0x6b4858, 0x7a495f, 0x7f4a5d, 0x68504b, 0x783d40,
            0x6c4461, 0x6a4538, 0x754e30, 0x60495d, 0x6a4b4c, 0x6c4a4b, 0x64414f,
            0x6b3d46, 0x69455c, 0x78324b, 0x57464e, 0x664f51, 0x6c404b, 0x66453d,
            0x663952, 0x643b58, 0x5d4452, 0x5c3848, 0x5a3637, 0x5a334a, 0x57414f,
            0x4d4654, 0x5a3b5a, 0x50305c, 0x594346, 0x593853, 0x663c36, 0x5c4049,
            0x583859, 0x643c35, 0x6d4153, 0x51354f, 0x5a3c45, 0x524046, 0x694566,
            0x583d47, 0x5c3538, 0x6a5b48, 0x713963, 0x643c5e, 0x533047, 0x694945,
            0x6a3b49, 0x524a52, 0x6a363d, 0x5f4059, 0x714953, 0x593b5e, 0x5c4242,
            0x684359, 0x61432f, 0x5d4749, 0x5c4030, 0x694952, 0x604046, 0x564543,
            0x623e38, 0x643556, 0x684c57, 0x705441, 0x584136, 0x6a4762, 0x834349
        }));

        ImageLibrary il = new ImageLibrary("images");
        PaletteExtractor horiz = new LinePaletteExtractor(0.5);
        PaletteExtractor vert = new LinePaletteExtractor(0.5, 1, 0.5, 0);

        pl.put("astro1", horiz.getPalette(il.get("apod-ccbysa-171004-soul-herschel.jpg")));
        pl.put("astro2", horiz.getPalette(il.get("apod-ccbysa-171101-thors-helmet.jpg")));
        pl.put("astro3", horiz.getPalette(il.get("apod-ccbysa-171004-soul-herschel.jpg")));

        pl.put("coral1", horiz.getPalette(il.get("coral1.jpeg")));
        pl.put("coral2", horiz.getPalette(il.get("coral2.jpeg")));
        pl.put("coral3", horiz.getPalette(il.get("coral3.jpeg")));
        pl.put("coral4", horiz.getPalette(il.get("coral4.jpeg")));
        pl.put("coral5", horiz.getPalette(il.get("coral5.jpeg")));

        pl.put("jupiter1", horiz.getPalette(il.get("junocam-jupiter-blues-pia21972.jpg")));
        pl.put("jupiter2", horiz.getPalette(il.get("junocam-jupiter-pj09-90-001.jpg")));

        pl.put("ocean1", vert.getPalette(il.get("ocean1.jpeg")));
        pl.put("ocean2", vert.getPalette(il.get("ocean2.jpeg")));
        pl.put("ocean3", vert.getPalette(il.get("ocean3.jpeg")));
        pl.put("ocean4", vert.getPalette(il.get("ocean4.jpeg")));

        pl.put("sun1", vert.getPalette(il.get("sunset1.jpeg")));
        pl.put("sun2", vert.getPalette(il.get("sunset2.jpeg")));
        pl.put("sun3", vert.getPalette(il.get("sunset3.jpeg")));
        pl.put("sun4", vert.getPalette(il.get("sunset4.jpeg")));

        pl.put(
            "sky.pink",
            new LinePaletteExtractor(0, 0.7, 1, 0.3).getPalette(il.get("maxpixel-cc0-pink-beautiful-sunset-1858600.jpg"))
        );
        pl.put("sky.purple", vert.getPalette(il.get("pexels-cc0-flight-landscape-nature-sky-36717.jpg")));
        pl.put("sky.red", vert.getPalette(il.get("pexels-cc0-animals-birds-dawn-giraffe-417142.jpg")));
        pl.put("sky.cloudy", vert.getPalette(il.get("pixnio-cc0-sunset-sky-mountain-landscape-cloud.jpg")));
        pl.put("sky.green", vert.getPalette(il.get("pexels-cc0-nature-sky-sunset-the-mountains-66997.jpg")));
        pl.put(
            "sky.pastel",
            new LinePaletteExtractor(0.11, 0.41, 0.5, 0).getPalette(il.get(
                "maxpixel-cc0-Abendstimmung-Landscape-Mountain-Sky-Nature-Lake-1504197.jpg"))
        );
        pl.put(
            "sky.dark",
            new LinePaletteExtractor(0, 1, 0, 0).getPalette(il.get("pexels-cc0-sunset-sunrise-sea-horizon-11434.jpg"))
        );
        pl.put(
            "sky.yellow",
            new LinePaletteExtractor(0.5, 1, 0.5, 0).getPalette(il.get("pexels-cc0-sunset-sunrise-sea-horizon-11434.jpg"))
        );

        pl.put(
            "lake",
            new LinePaletteExtractor(0, 1, 1, 0).getPalette(il.get(
                "maxpixel-cc0-Sky-Lake-Luener-Lake-Water-Blue-Mirroring-Clouds-475819.jpg"))
        );
        pl.put(
            "land",
            new LinePaletteExtractor(1, 0.5, 1, 0).getPalette(il.get(
                "maxpixel-cc0-Abendstimmung-Landscape-Mountain-Sky-Nature-Lake-1504197.jpg"))
        );
        pl.put("neon", horiz.getPalette(il.get("pixabay-cc0-neon-art-color-colorful-light-1596205.jpg")));
        pl.put(
            "scotland",
            new LinePaletteExtractor(0, 1, 0.6, 0).getPalette(il.get(
                "maxpixel-cc0-Elgol-Coast-Scotland-Stones-Lake-Isle-Of-Skye-540123.jpg"))
        );
    }

    public void draw() {
        background(lx.ui.theme.getDarkBackgroundColor());
        DrawHelper.runAll();
        dispatcher.draw();
    }

    private Pixlite[] setupPixlites() {
        return new Pixlite[0]; // todo
    }

    public final static int CHAN_WIDTH = 200;
    public final static int CHAN_HEIGHT = 650;
    public final static int CHAN_Y = 20;
    public final static int PAD = 5;
}
