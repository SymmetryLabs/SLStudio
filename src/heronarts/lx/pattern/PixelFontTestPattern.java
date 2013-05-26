/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.pattern;

import heronarts.lx.HeronLX;
import heronarts.lx.font.PixelFont;
import heronarts.lx.modulator.SawLFO;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Example pattern to render a text string using PixelFont.
 */
public class PixelFontTestPattern extends LXPattern {

    final private SawLFO hMod = new SawLFO(0, 360, 10000);
    final private SawLFO pMod = new SawLFO(0, 0, 10000);
    final private PImage image;
    
    public PixelFontTestPattern(HeronLX lx) {
        this(lx, "The quick brown fox jumped over the lazy dog.");
    }
    
    public PixelFontTestPattern(HeronLX lx, String s) {
        super(lx);
        this.image = (new PixelFont(lx)).getImage(s);
        this.addModulator(this.hMod.trigger());
        this.addModulator(
                this.pMod.setRange(-lx.width, this.image.width,
                        this.image.width * 250)).trigger();
    }

    public void run(int deltaMs) {    
        for (int i = 0; i < this.colors.length; ++i) {
            double col = this.lx.column(i) + this.pMod.getValue();
            int floor = (int) Math.floor(col);
            int ceil = (int) Math.ceil(col);
            float b1 = this.lx.applet.brightness(this.image.get(floor, this.lx.row(i)));
            float b2 = this.lx.applet.brightness(this.image.get(ceil, this.lx.row(i)));
            
            this.colors[i] = this.lx.colord(
                    this.hMod.getValue(),
                    100.,
                    PApplet.lerp(b1, b2, (float) (col - floor))
                    );
        }
    }
}

