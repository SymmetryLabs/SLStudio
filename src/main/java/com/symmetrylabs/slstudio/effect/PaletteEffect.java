package com.symmetrylabs.slstudio.effect;

import com.google.gson.JsonObject;
import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.*;

import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;




public class PaletteEffect extends SLEffect {

    private static final double LIGHT_SHOW_MS = 1000 * 60 * 1; // 10min
    private static final double SOLID_COLOR_MS = 1000 * 60 * 1; // 30min
    private static final double FADE_MS = 1000 * 60 * 1;
    private boolean inLightShow = true;
    private double timeSinceLastXfade = 0;
    private long lastRunNanos;
    private double lastAmtValue = 0;
    private long amountStartNanos;

    private static ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);








    private static final String KEY_PALETTE_NAME = "paletteName";

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();

    static CompoundParameter amount = new CompoundParameter("Amount", 0, 0, 1);
    DiscreteParameter palette = new DiscreteParameter("Palette", paletteLibrary.getNames());
        // selected colour palette
    CompoundParameter bottom = new CompoundParameter("Bottom", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter top = new CompoundParameter("Top", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter bias = new CompoundParameter("Bias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter shift = new CompoundParameter("Shift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter cutoff = new CompoundParameter("Cutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)
    BooleanParameter alpha = new BooleanParameter("Alpha", false).setDescription("Preserve alpha channel");
    BooleanParameter sunset = new BooleanParameter("Sunset", false).setDescription("Trigger Sunset Timer");
    ZigzagPalette pal = new ZigzagPalette();

    public PaletteEffect(LX lx) {

        super(lx);
        addParameter(amount);
        addParameter(palette);
        addParameter(bottom);
        addParameter(top);
        addParameter(shift);
        addParameter(bias);
        addParameter(cutoff);
        addParameter(alpha);
        addParameter(sunset);

    }




    final Runnable checker = new Runnable() {


        public void run() {
            double getAmnt = amount.getValue();
            double finalAmt = getAmnt;
            final boolean isItSunset = SunsetTool.sunset();
            if (!isItSunset){
                System.out.println("it is not sunset time");
                double amtValue = finalAmt;
                double xfade = amtValue - .01;
                if (xfade < 0) {
                    xfade = 0;
                }
//
//                long runNanos = System.nanoTime();
//                timeSinceLastXfade += 1e-6 * (runNanos - lastRunNanos);
//                lastRunNanos = runNanos;
//
//                double amtValue = finalAmt;
//                if (Math.abs(amtValue - lastAmtValue) > 0.1) {
//                    timeSinceLastXfade = 0;
//                    inLightShow = amtValue < 0.5;
//                }
//                lastAmtValue = amtValue;
//
//                if (timeSinceLastXfade > (inLightShow ? LIGHT_SHOW_MS : SOLID_COLOR_MS)) {
//                    inLightShow = !inLightShow;
//                    amountStartNanos = runNanos;
//                    timeSinceLastXfade = 0;
//                }
//
//                double xfade = (1e-6 * (runNanos - (amountStartNanos * 1)) / FADE_MS) * -1;
//                if (xfade > 1) {
//                    xfade = 1;
//                }
//                if (inLightShow) {
//                    xfade = 0 - (xfade * 1);
//                }
                PaletteEffect.amount.setValue(xfade);
            }



            else {
                        System.out.println("it is currently sunset time");
                        double amtValue = finalAmt;
                        double xfade = amtValue + .01;
                        if (xfade > 1) {
                            xfade = 1;
                        }

//
//                long runNanos = System.nanoTime();
//                timeSinceLastXfade += 1e-6 * (runNanos - lastRunNanos);
//                lastRunNanos = runNanos;
//



//                if (Math.abs(amtValue - lastAmtValue) > 0.1) {
//                    timeSinceLastXfade = 0;
//                    inLightShow = amtValue < 0.5;
//                }
//                lastAmtValue = amtValue;
//
//                if (timeSinceLastXfade > (inLightShow ? LIGHT_SHOW_MS : SOLID_COLOR_MS)) {
//                    inLightShow = !inLightShow;
//                    amountStartNanos = runNanos;
//                    timeSinceLastXfade = 0;
//                }
//
//                double xfade = 1e-6 * (runNanos - amountStartNanos) / FADE_MS;
//                if (xfade > 1) {
//                    xfade = 1;
//                }
//                if (inLightShow) {
//                    xfade = 1 - xfade;
//                }
                        PaletteEffect.amount.setValue(xfade);
            }



        }
    };
    final ScheduledFuture<?> beeperHandle =
        scheduler.scheduleAtFixedRate(checker, 1, 5, SECONDS);







    @Override
    public void run(double deltaMs, double amount) {


        double amt = this.amount.getValue();


            if (palette.getOptions().length == 0) {
                palette.setOptions(paletteLibrary.getNames());
            }


            if (amt == 0) return;

            pal.setPalette(paletteLibrary.get(palette.getOption()));
            pal.setBottom(bottom.getValue());
            pal.setTop(top.getValue());
            pal.setBias(bias.getValue());
            pal.setShift(shift.getValue());
            pal.setCutoff(cutoff.getValue());



            for (int i = 0; i < colors.length; i++) {
                int c = colors[i];
                int a = c & 0xff000000;
                int r = (c >> 16) & 0xff;
                int g = (c >> 8) & 0xff;
                int b = c & 0xff;
                int target = pal.getColor(r * 0.2126 / 255 + g * 0.7152 / 255 + b * 0.0722 / 255);
                colors[i] = (amt == 1) ? target : LXColor.lerp(colors[i], target, amt);
                if (alpha.getValueb()) {
                    colors[i] = (colors[i] & 0x00ffffff) | a;

            }
        }
    }
    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        obj.addProperty(KEY_PALETTE_NAME, palette.getOption());
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);
        if (obj.has(KEY_PALETTE_NAME)) {
            String pname = obj.get(KEY_PALETTE_NAME).getAsString();
            String[] palettes = paletteLibrary.getNames();
            for (int i = 0; i < palettes.length; i++) {
                if (palettes[i].equals(pname)) {
                    palette.setValue(i);
                    return;
                }
            }
            System.err.println("couldn't find palette '" + pname + "'");
        }
    }




}
