package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.slstudio.effect.SLEffect;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioInput; 
import java.util.LinkedList;
import java.util.Queue; 
import heronarts.lx.LXChannel;

public class VineWallManager extends SLEffect {
    DiscreteParameter look = new DiscreteParameter("look", 0, 5);
    BooleanParameter audioMode = new BooleanParameter("audio", false);
    CompoundParameter fadeDuration = new CompoundParameter("fade", 1, 0.1, 5);


    CompoundParameter bassLevel = new CompoundParameter("bass");
    CompoundParameter midLevel = new CompoundParameter("mid");
    CompoundParameter highLevel = new CompoundParameter("high");
    CompoundParameter allLevel = new CompoundParameter("all");
    DiscreteParameter bassSamples = new DiscreteParameter("bassSamples", 17, 1, 50);
    DiscreteParameter midSamples = new DiscreteParameter("midSamples", 17, 1, 50);
    DiscreteParameter highSamples = new DiscreteParameter("highSamples", 17, 1, 50);

    LXAudioInput audioInput = lx.engine.audio.getInput();
    GraphicMeter eq = new GraphicMeter(audioInput);

    Queue<Float> bassFifo = new LinkedList<Float>();
    Queue<Float> midFifo = new LinkedList<Float>();
    Queue<Float> highFifo = new LinkedList<Float>();

    int nBass = 4;
    int nMid = 8;
    int nHigh = 4;

    CompoundParameter bassGain = new CompoundParameter("bassGain", 1, 0.1, 10);
    CompoundParameter midGain = new CompoundParameter("midGain", 1, 0.1, 10);
    CompoundParameter highGain = new CompoundParameter("highGain", 1, 0.1, 10);
    CompoundParameter allGain = new CompoundParameter("allGain", 1, 0.1, 10);

    CompoundParameter bassSub = new CompoundParameter("bassSub", 0);
    CompoundParameter midSub = new CompoundParameter("midSub", 0);
    CompoundParameter highSub = new CompoundParameter("highSub", 0);

    boolean lastAudio;
    boolean lastAudioSet = false;
    boolean fading = false;
    float timeFading = 0;



    public VineWallManager(LX lx) {
        super(lx);

        addParameter(look);
        addParameter(audioMode);

        addParameter(bassLevel);
        addParameter(bassSamples);
         addParameter(midLevel);
        addParameter(midSamples);
         addParameter(highLevel);
        addParameter(highSamples);

        addParameter(allLevel);
        addParameter(allGain);

        addParameter(bassGain);
        addParameter(midGain);
        addParameter(highGain);

        addParameter(bassSub);
        addParameter(midSub);
        addParameter(highSub);

        addParameter(fadeDuration);



        addModulator(eq).start();



    }

    public String getLabel() {
        return "VineWallManager";
    }

    public float getAvg(Queue<Float> queue, int nSamples, float inp) {
        queue.add(inp);
        if (queue.size() > nSamples) {
            queue.remove();
        }
        float sum = 0;
        for (float v : queue) {
            sum += v;
        }
        sum /= queue.size();
        return sum;
    } 

    public void run(double deltaMs, double enabledAmount) {
        if (!lx.engine.audio.enabled.isOn()) {
            lx.engine.audio.enabled.setValue(true);
        }
        // System.out.println(eq.getBandf(0));

        float bassNow = 0;
        float midNow = 0;
        float highNow = 0;

        int i = 0;
        for (int j = i; j < nBass; j++) {
            bassNow += eq.getBandf(j);
        }
        bassNow /= nBass;
        i+= nBass;

        for (int j = i; j < i + nMid; j++) {
            midNow += eq.getBandf(j);
        }
        midNow /= nMid;
        i+= nMid;

        for (int j = i; j < i + nHigh; j++) {
            highNow += eq.getBandf(j);
        }
        highNow /= nHigh;

        float bassAvg = getAvg(bassFifo, bassSamples.getValuei(), bassNow);
        float midAvg = getAvg(midFifo, midSamples.getValuei(), midNow);
        float highAvg = getAvg(highFifo, highSamples.getValuei(), highNow);

        float bassV = (bassAvg - bassSub.getValuef()) * bassGain.getValuef();
        float midV = (midAvg - midSub.getValuef()) * midGain.getValuef();
        float highV = (highAvg - highSub.getValuef()) * highGain.getValuef();

        bassLevel.setValue(bassV);
        midLevel.setValue(midV);
        highLevel.setValue(highV);

        float avgV = (bassV + midV + highV) / 3.0f;
        allLevel.setValue(avgV * allGain.getValuef());

        if (!lastAudioSet) {
            lastAudio = audioMode.isOn();
            lx.engine.getFocusedLook().crossfader.setValue(lastAudio ? 1.0f : 0.0f);
            lastAudioSet = true;
        }

        if (lastAudio != audioMode.isOn()) {
            fading = true;
            timeFading = 0;

            if (audioMode.isOn()) {
                String prefix = String.format("look%d", look.getValuei());
                for (LXChannel c : lx.engine.getChannels()) {
                    String name = c.label.getString();
                    boolean en = name.contains(prefix) || name.contains("ambient"); 
                    c.enabled.setValue(en);
                }
            } else {

            }
        }

        lastAudio = audioMode.isOn();

        float fd = fadeDuration.getValuef();
        int WAIT = 200;
        float tf = timeFading - WAIT;
        if (fading && tf > 0) {

            float progress = tf / (1000 * fd);
            System.out.println(progress);
            if (progress > 1) {
                fading = false;
            } else {
                float val = audioMode.isOn() ? progress : (1.0f - progress);
                lx.engine.getFocusedLook().crossfader.setValue(val);
                           
            }
        }

        timeFading += (float)deltaMs;   



        // kernel.colors = colors;
        // kernel.shiftValue = shift.getValuef();
        // kernel.executeForSize(model.points.length);
    }
}