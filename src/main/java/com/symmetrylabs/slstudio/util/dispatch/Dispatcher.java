package com.symmetrylabs.slstudio.util.dispatch;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;


public class Dispatcher implements LXLoopTask {

    LX lx;

    private final DispatchQueue engineQueue = new DispatchQueue();
    private final DispatchQueue uiQueue = new DispatchQueue();

    public Dispatcher(LX lx) {
        this.lx = lx;

        uiQueue.setThreadName(Thread.currentThread().getName());
        setEngineThreaded(true);
    }

    void setEngineThreaded(boolean threaded) {
        engineQueue.setThreadName(threaded ? "LX Engine Thread" : Thread.currentThread().getName());
    }

    public void start() {
        lx.engine.addLoopTask(this);
        SLStudio.applet.registerMethod("draw", this);
    }

    public void loop(double deltaMs) {
        engineQueue.executeAll();
    }

    public void draw() {
        uiQueue.executeAll();
    }

    public void dispatchEngine(Runnable runnable) {
        engineQueue.queue(runnable);
    }

    void dispatchUi(Runnable runnable) {
        uiQueue.queue(runnable);
    }

    DispatchQueue getEngineQueue() {
        return engineQueue;
    }

    DispatchQueue getUiQueue() {
        return uiQueue;
    }

}


// // Used to get a handle on the eq parameters for pattern ui
// public class ProxyParameter extends LXVirtualParameter {
//   LXListenableParameter parameter;

//   public ProxyParameter(LXParameter parameter) {
//     this.parameter = (LXListenableParameter)parameter;
//   }

//   public LXParameter getRealParameter() {
//     return parameter;
//   }

//   public LXParameter.Polarity getPolarity() {
//     return parameter.getPolarity();
//   }

//   public LXListenableParameter setPolarity(LXParameter.Polarity polarity) {
//     return parameter;
//   }

//   @Override
//   public LXParameter setComponent(LXComponent component, String path) {
//     return parameter;
//   }

//   @Override
//   public LXComponent getComponent() {
//     return parameter.getComponent();
//   }

//   @Override
//   public String getPath() {
//     return parameter.getPath();
//   }

//   @Override
//   public String getDescription() {
//     return parameter.getDescription();
//   }

//   public LXParameter.Units getUnits() {
//     return parameter.getUnits();
//   }

//   @Override
//   public void dispose() {}
// }