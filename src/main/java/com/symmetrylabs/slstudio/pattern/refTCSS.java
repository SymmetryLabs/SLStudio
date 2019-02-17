//package com.symmetrylabs.slstudio.pattern;
//
//import heronarts.lx.LX;
//import heronarts.lx.parameter.MutableParameter;
//
//public class ThreadingTimeCodedSlideshow extends TimeCodedSlideshow {
//    private final MutableParameter numThreads = new MutableParameter("numThreads", 2);
//    public ThreadingTimeCodedSlideshow(LX lx){
//        super(lx);
//        addParam(numThreads);
//    }
//
//    Thread circular_thread_pool[];
//
//    @Override
//    public void onActive() {
//        super.onActive();
//        stopping = false;
//        loadDirectory();
//
//        if (!baked) {
//            loaderThread = new Thread(() -> {
//                while (!stopping) {
//                    try {
//                        loaderSemaphore.acquire();
//                    } catch (InterruptedException e) {
//                        return;
//                    }
//                    lastLoadLoop = System.nanoTime();
//                    /* Find the first frame after/including the current frame that
//                     * hasn't been loaded, and load it. */
//                    for (int i = currentFrame < 0 ? 0 : currentFrame; i < nFrames; i++) {
//                        if (!allFrames[i].loaded()) {
//                            allFrames[i].load();
//                            break;
//                        }
//                    }
//                }
//            });
//            try {
//                loaderThread.start();
//            } catch (IllegalThreadStateException e) {
//                e.printStackTrace();
//            }
//        } else {
//            loaderThread = null;
//        }
//    }
//}
