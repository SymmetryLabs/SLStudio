package com.symmetrylabs.util.dispatch;

import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;

public class Dispatcher {

    private LX lx;

    private final DispatchQueue engineQueue = new DispatchQueue();
    private final DispatchQueue uiQueue = new DispatchQueue();
    private final DispatchQueue networkQueue = new DispatchQueue();

    private static Map<LX, WeakReference<Dispatcher>> instanceByLX = new WeakHashMap<>();

    private final LXLoopTask engineLoopTask = deltaMs -> engineQueue.executeAll();
    private final Runnable networkTask = () -> networkQueue.executeAll();

    public static synchronized Dispatcher getInstance(LX lx) {
        WeakReference<Dispatcher> weakRef = instanceByLX.get(lx);
        Dispatcher ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new Dispatcher(lx)));
        }
        return ref;
    }

    private Dispatcher(LX lx) {
        this.lx = lx;

        uiQueue.setThreadName(Thread.currentThread().getName());
        setEngineThreaded(true);

        lx.engine.addLoopTask(engineLoopTask);
        lx.engine.addNetworkTask(networkTask);
    }

    void setEngineThreaded(boolean threaded) {
        engineQueue.setThreadName(threaded ? "LX Engine Thread" : Thread.currentThread().getName());
    }

    public void draw() {
        uiQueue.executeAll();
    }

    public void dispatchEngine(Runnable runnable) {
        engineQueue.queue(runnable);
    }

    public void dispatchUi(Runnable runnable) {
        uiQueue.queue(runnable);
    }

    public void dispatchNetwork(Runnable runnable) {
        networkQueue.queue(runnable);
    }

    DispatchQueue getEngineQueue() {
        return engineQueue;
    }

    DispatchQueue getUiQueue() {
        return uiQueue;
    }

    DispatchQueue getNetworkQueue() {
        return networkQueue;
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
