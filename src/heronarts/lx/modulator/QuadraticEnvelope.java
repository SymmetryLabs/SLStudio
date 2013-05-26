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

package heronarts.lx.modulator;

public class QuadraticEnvelope extends LXModulator {

    public enum Ease {
        IN,
        OUT,
        BOTH
    };

    private Ease ease;
    private double startVal;
    private double endVal;
    final LinearEnvelope basis;
    
    public QuadraticEnvelope(double startVal, double endVal, double duration) {
        this.ease = Ease.IN;
        this.startVal = startVal;
        this.endVal = endVal;
        this.basis = new LinearEnvelope(0, 1, duration);
    }
    
    public QuadraticEnvelope setEase(Ease ease) {
        this.ease = ease;
        return this;
    }
    
    public QuadraticEnvelope start() {
        super.start();
        this.basis.start();
        return this;
    }
    
    public QuadraticEnvelope stop() {
        super.stop();
        this.basis.stop();
        return this;
    }
    
    public QuadraticEnvelope trigger() {
        this.basis.trigger();
        this.start();
        return this;
    }

    public QuadraticEnvelope setRange(double startVal, double endVal, double durationMs) {
        this.setRange(startVal, endVal);
        this.basis.setDuration(durationMs);
        return this;
    }
    
    public QuadraticEnvelope setRange(double startVal, double endVal) {
        this.startVal = startVal;
        this.endVal = endVal;
        return this;
    }
    
    public LXModulator setDuration(double durationMs) {
        this.basis.setDuration(durationMs);
        return this;
    }

    protected void computeRun(int deltaMs) {
        this.basis.run(deltaMs);
        this.running = this.basis.isRunning();
        double bv = this.basis.getValue();
        switch (this.ease) {
        case IN:
            this.value = this.computeQuad(bv*bv, this.startVal, this.endVal);
            break;
        case OUT:
            this.value = this.computeQuad(1 - (1-bv)*(1-bv), this.startVal, this.endVal);
            break;
        case BOTH:
            if (bv < 0.5) {
                this.value = this.computeQuad((bv*2)*(bv*2), this.startVal, (this.startVal + this.endVal) / 2.);
            } else {
                bv = (bv-0.5) * 2.;
                this.value = this.computeQuad(1 - (1-bv)*(1-bv), (this.startVal + this.endVal) / 2., this.endVal);
            }
            break;
        }
    }
    
    private double computeQuad(double coeff, double startVal, double endVal) {
        return startVal + coeff * (endVal - startVal);
    }
        
}
