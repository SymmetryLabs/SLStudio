package com.symmetrylabs.util;

import java.util.function.Supplier;

public class DoubleBuffer_withCleanFlags<T>{

    private class BufferWithFlag<T>{
        public T buffer;
        private boolean clean;

        protected BufferWithFlag(T buffer_in){
            this.buffer = buffer_in;
            this.clean = false;
        }

        protected void setClean(){
            clean = true;
        }

        protected void dirty(){
            clean = false;
        }
    }
    public boolean initialized = false;
    private BufferWithFlag<T> frontBuffer;
    private BufferWithFlag<T> backBuffer;

    private Supplier<BufferWithFlag> supply;

    public DoubleBuffer_withCleanFlags(Supplier<BufferWithFlag> supplier){
        this.supply = supplier;
    }

    public void initialize(){
        frontBuffer = supply.get();
        backBuffer = supply.get();
        initialized = true;
    }


    public void flip(){
        BufferWithFlag tmp = this.backBuffer;
        this.backBuffer = this.frontBuffer;
        this.frontBuffer = tmp;
    }

    // We're done with the back buffer. Make it the front buffer and mark it setClean.
    public void post(){
        BufferWithFlag tmp = this.backBuffer;
        this.backBuffer = this.frontBuffer;
        this.frontBuffer = tmp;
        this.frontBuffer.setClean();
    }

    public void dispose () {
        backBuffer = null;
        frontBuffer = null;
        initialized = false;
    }

    public BufferWithFlag getFront() {
        return frontBuffer;
    }

    public BufferWithFlag getBack() {
        return backBuffer;
    }

    public void setBack(BufferWithFlag in){
        backBuffer = in;
    }

    public void supplyBack(){
        assert !backBuffer.clean : "your back buffer hasn't been used it should not be supplied";
        BufferWithFlag t = supply.get();
        if (t != null) {
            backBuffer = t;
            backBuffer.setClean();
            return;
        }
        System.err.println("no supply needed");
    }

    public void supplyFront() {
        assert !frontBuffer.clean : "front buffer has not been written out yet. It should not be refreshed right now.";
        BufferWithFlag t = supply.get();
        if (t != null) {
            frontBuffer = t;
            frontBuffer.setClean();
            return;
        }
        System.err.println("no supply needed");
    }
}

