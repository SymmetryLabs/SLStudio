package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.Slice;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;

/**
 * @author Nate Argetsinger (narget@umich.edu)
 */

/**
 * Contains the current state of a lightning strike.
 */


// an instance of "SnowFlake" which caries some points to infect
class SnowFlake {
        private int lifetime; // how long this strike lives
        private long birthtime;
        private float fallspeed;
        public LXPoint nucleo;


        private String description = "a color blob that does stuff";

        public SnowFlake(float fallspeed, LXModulator radius_modulator, LXPoint nucleationPoint, int lifetime) {
                this.nucleo = nucleationPoint;
                this.lifetime = lifetime;
                this.birthtime = System.currentTimeMillis();
                this.fallspeed = fallspeed;
        }

        public boolean isDead() {
                return (this.birthtime + this.lifetime < System.currentTimeMillis()) || (this.nucleo.y < 20); // if lifespan is over.
        }


        public String describe() {
                return description;
        }

        public void migrate(LXPoint lxPoint) {
                this.nucleo = new LXPoint(lxPoint.x, lxPoint.y - fallspeed, lxPoint.z);
        }

}

public class SnowFlakes extends SunsPattern {

        public final CompoundParameter fallSpeed = new CompoundParameter("FallSpeed", .5, .1, 4);
        public final CompoundParameter kernelSize = new CompoundParameter("size", .5, .1, 2.5);

        LXModulator dotSize = new LXModulator("Strike Size") {
                @Override
                protected double computeValue(double v) {
                        return 0;
                }
        };

        private Random pointRandom = new Random();

        final DiscreteParameter dotLifetime = new DiscreteParameter("Life", 10000, 0, 50000);
        final BooleanParameter debugSpawn = new BooleanParameter("Spawn");


        final DiscreteParameter numDots = new DiscreteParameter("Number", 300);
        //    private List<LightningStrike> dots = new ArrayList<LightningStrike>;
        final SinLFO lfo = new SinLFO("Nate", 0, model.yRange, 2000);

        //    private LightningStrike strike = new LightningStrike(size, model.cx, model.cy, model.cz);
        private List<SnowFlake> dots = new ArrayList<SnowFlake>();


        private List<LXPoint> spawnPoints = new ArrayList<LXPoint>();

        // constructed
        public SnowFlakes(LX lx) {
                super(lx);
                startModulator(lfo);
                addParameter(numDots);
                addParameter(kernelSize);
                addParameter(dotLifetime);
                addParameter(fallSpeed);
                addParameter(debugSpawn);

                buildSpawnIndex();
        }


        // get the top pixels so that snow flakes spawn at the top
        private void buildSpawnIndex() {
                for (Sun sun : model.getSuns()) {
                        float hue = 0;

                        int slice_num = 0;
                        for (Slice slice : sun.getSlices()) {
                                if (slice_num++ < 2) {

                                        int strip_num = 0;
                                        for (Strip strip : slice.getStrips()) {
                                                strip_num++;
                                                int stripIndex = SLStudio.applet.selectedStrip.getValuei();

                                                int counter = 0;
                                                for (LXPoint p : strip.points) {

                                                        if (counter > strip.metrics.numPoints - 3) {
                                                                this.spawnPoints.add(p);
                                                        }
                                                        if (counter < 2) {
                                                                this.spawnPoints.add(p);
                                                        }
                                                        if (strip_num < 20) {
                                                                this.spawnPoints.add(p);
                                                        }
                                                        counter++;
                                                }
                                        }

                                }
                        }
                }
        }

        public void run(double deltaMs) {
//        try {


                        setColors(0xff000000);

                        if (debugSpawn.getValueb()) {
                                for (LXPoint p : this.spawnPoints) {
                                        colors[p.index] = 0xffffffff;
                                }
                        }

                        // create new SnowFlakes
                        while (this.dots.size() < numDots.getValuei()) {
                                LXPoint new_center_point = spawnPoints.get(pointRandom.nextInt(spawnPoints.size()));
                                int lifetime = dotLifetime.getValuei(); // lifetime in ms
                                this.dots.add(new SnowFlake(fallSpeed.getValuef(), dotSize, new_center_point, lifetime )/*snowflake*/);
                        }


                        this.dots.removeIf(SnowFlake::isDead);


                        // do something with living dotStrikes
                        for (SnowFlake dot : this.dots) {
                                List<LXPoint> nearbyPoints = model.getModelIndex().pointsWithin(dot.nucleo, kernelSize.getValuef());

                                // change the center
                                if (nearbyPoints.size() > 1){
                                        dot.migrate(nearbyPoints.get(pointRandom.nextInt(nearbyPoints.size())));
                                }

                                for (LXPoint p : nearbyPoints) {
                                        colors[p.index] = 0xff00ffff;
                                }
                        }

//        } catch (Exception e) {
//            println(e);
//        }

        }
}
