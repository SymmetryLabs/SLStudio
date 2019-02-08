package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.SLStudio;

import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public class DoubleBuffer<T>{

    private boolean initialized = false;
    private T frontBuffer;
    private T backBuffer;

    private Supplier<T> supply;

    public DoubleBuffer(Supplier<T> supplier){
        this.supply = supplier;
        frontBuffer = supply.get();
        backBuffer = supply.get();
        initialized = true;
    }


    public void flip(){
        T tmp = this.backBuffer;
        this.backBuffer = this.frontBuffer;
        this.frontBuffer = tmp;
    }

    public void dispose () {
        backBuffer = null;
        frontBuffer = null;
        initialized = false;
    }

    public T getClean() {
        return frontBuffer;
    }

    public T getDirty() {
        return backBuffer;
    }

    public void setBack(T in){
        backBuffer = in;
    }

    public void supplyBack(){
        T t = supply.get();
        if (t != null) {
            backBuffer = t;
        }
        System.err.println("no supply needed");
    }
}

