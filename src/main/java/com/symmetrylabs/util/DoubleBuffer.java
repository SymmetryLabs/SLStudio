package com.symmetrylabs.util;

import com.symmetrylabs.slstudio.SLStudio;

import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public class DoubleBuffer<T>{

    public boolean initialized = false;
    private T frontBuffer;
    private T backBuffer;

    private boolean backBufferClean = false;

    private Supplier<T> supply;

    public DoubleBuffer(Supplier<T> supplier){
        this.supply = supplier;
    }

    public void initialize(){
        frontBuffer = supply.get();
        backBuffer = supply.get();
        initialized = true;
    }


    public void flip(){
        T tmp = this.backBuffer;
        this.backBuffer = this.frontBuffer;
        this.frontBuffer = tmp;
        backBufferClean = false;
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
        backBufferClean = true;
    }

    public void supplyBack(){
        assert !backBufferClean : "your back buffer hasn't been used it should not be supplied";
        T t = supply.get();
        if (t != null) {
            backBuffer = t;
            backBufferClean = true;
            return;
        }
        System.err.println("no supply needed");
    }
}

