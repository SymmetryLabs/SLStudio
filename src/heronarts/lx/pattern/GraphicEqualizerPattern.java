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
import heronarts.lx.LXUtils;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.transition.WipeTransition;


import ddf.minim.analysis.FFT;

public class GraphicEqualizerPattern extends LXPattern {
    
    private final FFT fft; 
    private final LinearEnvelope[] bandVals;
    
    public GraphicEqualizerPattern(HeronLX lx) {
        super(lx);
        this.fft = new FFT(lx.audioInput().bufferSize(), lx.audioInput().sampleRate());
        this.fft.window(FFT.HAMMING);
        this.fft.logAverages(22, 3);
        this.bandVals = new LinearEnvelope[this.fft.avgSize()];
        for (int i = 0; i < this.bandVals.length; ++i) {
            this.addModulator(this.bandVals[i] = (new LinearEnvelope(0, 0, 900+i*4))).trigger();
        }
        this.transition = new WipeTransition(lx, WipeTransition.Direction.UP);
    }
    
    public void run(double deltaMs) {
        this.fft.forward(this.lx.audioInput().mix);
        int avgSize = this.fft.avgSize();
        for (int i = 0; i < avgSize; ++i) {
            double value = this.fft.getAvg(i);
            if (value > this.bandVals[i].getValue()) {
                this.bandVals[i].setRange(value, 0).trigger();
            }
        }
        
        for (int i = 0; i < this.lx.width; ++i) {
            int avgIndex = (int) (i / (double) this.lx.width * (avgSize-1));
            double value = this.bandVals[avgIndex].getValue();
            for (int j = 0; j < this.lx.height; ++j) {
                int jmin = this.lx.height - j;
                double b = LXUtils.constrain(25. * (value - jmin), 0, 100);
                this.setColor(i, j, this.lx.colord(this.lx.getBaseHue(), 100., b));
            }
        }
    }
    
}