package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.transform.LXVector;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ThreadingTimeCodedSlideshow extends TimeCodedSlideshow {
    private static final String TAG = "TCSS threaded";

    private final MutableParameter numThreads = new MutableParameter("threadPoolSize", 2);

    List<WorkerThread> circular_thread_pool = new ArrayList<>();
    Semaphore colorBufferLock = new Semaphore(0);

    private class WorkerThread extends Thread {
        private static final String TAG = "worker-";

        private final int index;
        private boolean running = true;

        private final Semaphore timeToRender = new Semaphore(0);
        private final Semaphore waitRender = new Semaphore(0);

        public WorkerThread(int index) {
            this.index = index;
        }

//        public void startRender() {
//            timeToRender.release();
//        }

        public void waitFinished() {
            try {
                waitRender.acquire();
            } catch (InterruptedException e) { /* pass */ }
        }

        public void shutdown() {
            running = false;
            waitRender.release();
            interrupt();
        }

        @Override
        public void run() {
            long lastTime = System.currentTimeMillis();
            while (running) {

                // do my stuff
                loadPendingHunk();

                try {
                    timeToRender.acquire();
                } catch (InterruptedException e) {
                    continue;
                }

                int numThreads = circular_thread_pool.size();

                long t = System.currentTimeMillis();
                double deltaMs = t - lastTime;




                lastTime = t;

                // activate the callback that we've loaded hunk
                waitRender.release();
            }
        }

        void loadPendingHunk() {
            SLStudio.setWarning(TAG +  index, " loading hunk");
            String directoryPath = directory.getString();
            String hunkPath = directoryPath + (tc_modulo() + index) + ".png";

            if (hunkPath == null) {
                // throw FileNotFoundException
                return;
            }
            File hunkFile = new File(hunkPath);

            if (hunkFile.isFile() && hunkFile.getName().endsWith(".png")) {
                baked = true;
                try {
                    bakedImage = ImageIO.read(hunkFile);
                    nFrames = bakedImage.getHeight();
                } catch (IOException e) {
                    SLStudio.setWarning(TAG, "couldn't load baked slideshow");
                    System.out.println("could not load baked slideshow:");
                    e.printStackTrace();
                    bakedImage = null;
                }
                return;
            }

            if (!hunkFile.isDirectory()) {
                SLStudio.setWarning(TAG, "couldn't find *.png hunk");
                return;
            }
        }
    }

    // gets the index into the hunks based on the current timecode.
    // this is calculated as follows:
    // 1) What song are we in?  Simply what range does timecode fall into.. a switch statement.
    // 2) offset into the song <integer division> chunk length (time)
    private int tc_modulo() {
        return 1;
    }


    public ThreadingTimeCodedSlideshow(LX lx){
        super(lx);
        addParam(numThreads);

        // spawn our two threads..
        for (int i = circular_thread_pool.size(); i < numThreads.getValuei(); ++i) {
            WorkerThread rt = new WorkerThread(i);
            circular_thread_pool.add(rt);
            rt.start();
        }

        // watch MTC trigger our ThreadPool when to do stuff..
        // happens in overrided method
    }

    @Override
    private void goToFrame(int frame) {

    }



}
