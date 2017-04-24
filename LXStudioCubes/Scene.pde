public static enum ActionMode {RUNS, RUNS_ONCE};

public abstract class Scene extends LXRunnable implements LXOscComponent {
    private final HashMap<String, Action> actions = new HashMap<String, Action>();
    private final LX lx;

    public Scene(final LX lx, String label) {
        super(lx, label);
        this.lx = lx;
    }

    public void registerAction(Action action) {
        actions.put(action.label.getString(), action);
        addSubcomponent(action);
    }

    public Action getAction(String label) {
        return actions.get(label);
    }

    public void triggerAction(Device device, String actionLabel) {
        Action action = actions.get(actionLabel);
        String oscPath = action.getOscAddress() + "/command/trigger";
        device.dispatch(oscPath);
    }

    public void stopAction(Device device, String actionLabel) {
        Action action = actions.get(actionLabel);
        String oscPath = action.getOscAddress() + "/command/stop";
        device.dispatch(oscPath);
    }

    public void resetAction(Device device, String actionLabel) {
        Action action = actions.get(actionLabel);
        String oscPath = action.getOscAddress() + "/command/reset";
        device.dispatch(oscPath);
    }

    public void triggerActionBroadcast(String actionLabel) {
        for (Device device : devices) triggerAction(device, actionLabel);
    }

    public void stopActionBroadcast(String actionLabel) {
        for (Device device : devices) stopAction(device, actionLabel);
    }

    public void resetActionBroadcast(String actionLabel) {
        for (Device device : devices) resetAction(device, actionLabel);
    }

    public void onReset() {
        for (Action action : actions.values())
            resetActionBroadcast(action.label.getString());
    }

    public String getOscAddress() {
        String parentPath = ((SceneController)getParent()).getOscAddress();
        return parentPath + "/" + label.getString();
    }

    // Override this!
    public void loopScene(double deltaMs) {}

    protected void run(double deltaMs) {
        if (!this.running.isOn()) return;

        for (Action action : actions.values())
            action.run(deltaMs);
        loopScene(deltaMs);
    }

    public class Action extends LXRunnable implements LXOscComponent {
        private final EnumParameter<ActionMode> mode;

        public Action(String label, ActionMode mode) {
            super(label);
            this.mode = new EnumParameter<ActionMode>("mode", mode);
        }

        // Override this!
        protected void trigger(LX lx, double deltaMs) {}

        protected void run(double deltaMs) {
            trigger(lx, deltaMs);
            if (mode.getEnum() == ActionMode.RUNS_ONCE) stop();
        }

        public String getOscAddress() {
            String parentPath = ((Scene)getParent()).getOscAddress();
            return parentPath + "/" + label.getString();
        }
    }
}

public class SceneActionListener implements LXOscListener {
  public void oscMessage(OscMessage message) {
    String[] parts = message.getAddressPattern().getValue().split("/");

    if (parts[2].equals("scenes")) {
        Scene scene = sceneController.getScene(parts[3]);
        Scene.Action action = scene.getAction(parts[4]);

        if (parts[5].equals("command")) {
            String command = parts[6];

            if (command.equals("trigger")) {
                action.start(); //println("trigger");
            } else if (command.equals("stop")) {
                action.stop();  //println("stop");
            } else if (command.equals("reset")) {
                action.reset(); //println("reset");
            }
        }
    }
  }
}

public class SceneController extends LXRunnable implements LXOscComponent {
    private HashMap<String, Scene> scenes = new HashMap<String, Scene>();
    private DiscreteParameter selectedScene = null;

    public SceneController(LX lx, Scene[] paramScenes) {
        super(lx, "SceneController");

        for (Scene scene : paramScenes) {
            scenes.put(scene.label.getString(), scene);
            addSubcomponent(scene);
        }

        this.selectedScene = new DiscreteParameter("selectedScene", (Object[])paramScenes);
        this.selectedScene.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                for (Scene scene : scenes.values())
                    if (scene.isRunning()) scene.reset();
                getActiveScene().start();
            }
        });

        addParameter(selectedScene);
        start();
        ((Scene)selectedScene.getObject()).start();
    }

    protected void run(double deltaMs) {
         getActiveScene().run(deltaMs);
    }

    public Scene getActiveScene() {
        return (Scene)selectedScene.getObject();
    }

    public Scene getScene(String label) {
        return scenes.get(label);
    }

    public String getOscAddress() {
        return "/lx/scenes";
    }
}