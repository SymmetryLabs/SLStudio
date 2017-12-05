import java.util.concurrent.ConcurrentLinkedQueue;

public class Dispatcher implements LXLoopTask {

  LX lx;

  private final DispatchQueue engineQueue = new DispatchQueue();
  private final DispatchQueue uiQueue = new DispatchQueue();

  Dispatcher(LX lx) {
    this.lx = lx;

    uiQueue.setThreadName(Thread.currentThread().getName());
    setEngineThreaded(true);
  }

  void setEngineThreaded(boolean threaded) {
    engineQueue.setThreadName(threaded ? "LX Engine Thread" : Thread.currentThread().getName());
  }

  void start() {
    lx.engine.addLoopTask(this);
    registerMethod("draw", this);
  }

  void loop(double deltaMs) {
    engineQueue.executeAll();
  }

  void draw() {
    uiQueue.executeAll();
  }

  void dispatchEngine(Runnable runnable) {
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

class DispatchQueue {

  private volatile String threadName;
  private final ConcurrentLinkedQueue<Runnable> queuedRunnables = new ConcurrentLinkedQueue<Runnable>();

  DispatchQueue() {
    this(null);
  }

  DispatchQueue(String threadName) {
    this.threadName = threadName;
  }

  synchronized void setThreadName(String threadName) {
    this.threadName = threadName;
  }

  String getThreadName() {
    return threadName;
  }

  void executeAll() {
    Runnable runnable;
    while ((runnable = queuedRunnables.poll()) != null) {
      runnable.run();
    }
  }

  void queue(Runnable runnable) {
    boolean shouldRunNow = false;
    synchronized (this) {
      if (threadName != null && Thread.currentThread().getName().equals(threadName)) {
        shouldRunNow = true;
      }
    }
    if (shouldRunNow) {
      runnable.run();
    } else {
      queuedRunnables.add(runnable);
    }
  }
}

// Used to get a handle on the eq parameters for pattern ui 
public class ProxyParameter extends LXVirtualParameter {
  LXListenableParameter parameter;

  public ProxyParameter(LXParameter parameter) {
    this.parameter = (LXListenableParameter)parameter;
  }

  public LXParameter getRealParameter() {
    return parameter;
  }

  public LXParameter.Polarity getPolarity() {
    return parameter.getPolarity();
  }

  public LXListenableParameter setPolarity(LXParameter.Polarity polarity) {
    return parameter;
  }

  @Override
  public LXParameter setComponent(LXComponent component, String path) {
    return parameter;
  }

  @Override
  public LXComponent getComponent() {
    return parameter.getComponent();
  }

  @Override
  public String getPath() {
    return parameter.getPath();
  }

  @Override
  public String getDescription() {
    return parameter.getDescription();
  }

  public LXParameter.Units getUnits() {
    return parameter.getUnits();
  }

  @Override
  public Formatter getFormatter() {
    return parameter.getFormatter();
  }

  @Override
  public void dispose() {}
}