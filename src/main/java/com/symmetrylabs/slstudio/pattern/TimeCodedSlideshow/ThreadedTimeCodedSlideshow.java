package com.symmetrylabs.slstudio.pattern.TimeCodedSlideshow;

import heronarts.lx.LX;
import heronarts.lx.parameter.MutableParameter;

public class ThreadedTimeCodedSlideshow extends TimeCodedSlideshow {

    MutableParameter thread_idx = new MutableParameter("thread number", 2);

    // a pool of threads which will "roll" through frames buffering and then yeilding
    Thread circular_thread_pool[];

    public ThreadedTimeCodedSlideshow(LX lx){
        super(lx);

        // create N threads to service frames in round robin fashion
        circular_thread_pool = new Thread[thread_idx.getValuei()];

        //
        // thread_idx safe to increment decrement?
        // what would happen if I went from 1000 service threads to 2?
        // 2 to 1000?
        // In the middle of this loop?
        for (int i = 0; i < thread_idx.getValuei(); i++){

        }
    }
}
