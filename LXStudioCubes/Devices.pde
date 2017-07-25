class AgentOSCListener implements LXOscListener {
    public void oscMessage(OscMessage msg) {
        String[] splitMsg = msg.getAddressPattern().toString().split("/");
        //System.out.println(msg);

        if ("nebula".equals(splitMsg[1])) {
            String deviceName = splitMsg[2];
            InetAddress deviceAddress = msg.getSource();
            String param = splitMsg[3];

            if ("inputs".equals(param)) {
                List<String> inputs = new ArrayList<String>();
                for (OscArgument arg : msg) {
                    if (arg instanceof OscString) {
                        inputs.add(((OscString)arg).getValue());
                    }
                }
                setDeviceInputs(deviceName, deviceAddress, inputs);
            }
            else {
                float value = msg.getFloat(0);
                setDeviceParameter(deviceName, deviceAddress, param, value);
            }
        }
    }

    public void setDeviceParameter(String deviceName, InetAddress deviceAddress, String param, float value) {
        Device device = deviceController.getOrCreateDevice(deviceName, deviceAddress);
        if (device == null) {
            return;
        }

        LXParameter p = device.ensureParameter(param);
        p.setValue(value);
    }

    public void setDeviceInputs(String deviceName, InetAddress deviceAddress, List<String> inputs) {
        Device device = deviceController.getOrCreateDevice(deviceName, deviceAddress);
        if (device == null) {
            return;
        }

        device.ensureInputs(inputs);
    }
}

class Device extends LXComponent {
    String name;
    InetAddress address;
    List<NewParameterWatcher> watchers = new ArrayList<NewParameterWatcher>();

    private LXOscEngine.Transmitter transmitter;

    Device(LX lx, String name, InetAddress address) {
        super(lx);

        this.name = name;
        this.address = address;

        try {
            transmitter = lx.engine.osc.transmitter(address, 5005);
        }
        catch (java.net.SocketException e) { System.err.println(e); }

    }

    public LXParameter ensureParameter(String param) {
        LXParameter p = getParameter(param);
        if (p == null) {
            p = new BoundedParameter(param);
            addParameter(param, p);
            for (NewParameterWatcher w : watchers) {
                w.onParameterAdded(p);
            }
        }
        return p;
    }

    public void queryInputs() {
        if (transmitter == null)
            return;

        OscMessage m = new OscMessage("/list-inputs");
        try {
            transmitter.send(m);
        }
        catch (java.io.IOException e) {
            System.err.println(e);
        }
    }

    public void ensureInputs(List<String> inputs) {
        for (String input : inputs) {
            System.out.println("Device " + name + " input: " + input);
        }
    }

    public void addNewParameterWatcher(NewParameterWatcher w) {
        watchers.add(w);
    }
}

public interface NewParameterWatcher {
    public void onParameterAdded(LXParameter p);
}

class DeviceController extends LXComponent {
    ListenableList<Device> devices = new ListenableList<Device>();
    private Map<String, Device> deviceByName = new HashMap<String, Device>();

    DeviceController(LX lx) {
        super(lx);

        devices.addListener(new ListListener<Device>() {
            public void itemAdded(final int index, final Device c) {
                deviceByName.put(c.name, c);
                c.queryInputs();
            }
            public void itemRemoved(final int index, final Device c) {
                deviceByName.remove(c.name);
            }
        });
    }
    public Device getDeviceByName(String name) {
        return deviceByName.get(name);
    }
    public Device getOrCreateDevice(String name, InetAddress addr) {
        Device device = deviceByName.get(name);
        if (device == null) {
            device = new Device(lx, name, addr);
            devices.add(device);
        }
        return device;
    }
}

class DeviceListItem extends UIItemList.AbstractItem {
    Device device;
    DeviceListItem(Device device) {
        this.device = device;
    }

    String getLabel() {
        return device.name;
    }
}

class DeviceList extends UIItemList.ScrollList {
    public final UI2dScrollContext scrollContext;
    UI ui;
    float w;
    DeviceList(UI ui, float x, float y, float w, float h, UI2dScrollContext sc) {
        super(ui, x, y, w, h);
        scrollContext = sc;
        this.ui = ui;
        this.w = w;
    }

    @Override
    void onKeyPressed(KeyEvent keyEvent,
        char keyChar,
        int keyCode) {
        if (keyCode == ENTER) {
            Device selected = ((DeviceListItem) getFocusedItem()).device;
            new UIDeviceKnobs(lx, ui, x, y, w, selected).addToContainer(scrollContext);
        } else {
            super.onKeyPressed(keyEvent, keyChar, keyCode);
        }
    }
}

class DeviceSection extends UICollapsibleSection {

    DeviceSection(LX lx, UI ui, float x, float y, float w, UI2dScrollContext sc) {
        super(ui, x, y, w, 140);
        setTitle("Devices");

        int h = 78;

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        final DeviceList outputList = new DeviceList(ui, 0, 8, w-8, h, sc);

        for (Device device : deviceController.devices) {
            items.add(new DeviceListItem(device));
        }


        outputList.setItems(items).setSingleClickActivate(true);
        outputList.addToContainer(this);

        final Runnable update = new Runnable() {
            public void run() {
                final List<UIItemList.Item> localItems = new ArrayList<UIItemList.Item>();
                int i = 0;
                for (Device device : deviceController.devices) {
                    localItems.add(new DeviceListItem(device));
                }
                outputList.setItems(localItems);
                redraw();
            }
        };


        deviceController.devices.addListener(new ListListener<Device>() {
            public void itemAdded(final int index, final Device c) {
                dispatcher.dispatchUi(update);
            }
            public void itemRemoved(final int index, final Device c) {
                dispatcher.dispatchUi(update);
            }
        }
        );
    }
}

public class UIDeviceKnobs extends UICollapsibleSection {

    private final static int TOP_PADDING = 4;
    private final static int X_SPACING = UIKnob.WIDTH + 1;
    private final static int Y_SPACING = UIKnob.HEIGHT + TOP_PADDING;

    private int xMultiplier = 0;

    UIDeviceKnobs(LX lx, UI ui, float x, float y, float w, Device device) {
        super(ui, x, y, w, 140);
        setTitle(device.name);
        setChildMargin(0);

        Collection<LXParameter> deviceParams = device.getParameters();
        for (LXParameter param : deviceParams) {
            if (param.getLabel() == "Label") {
                continue;
            }
            System.out.println(param.getLabel());
            System.out.println(param.getValue());
            new UIKnob((BoundedParameter) param).setY(TOP_PADDING).setX(X_SPACING*xMultiplier).addToContainer(this);
            xMultiplier++;
        }

        device.addNewParameterWatcher(new NewParameterWatcher() {
            @Override
            public void onParameterAdded(LXParameter p) {
                new UIKnob((BoundedParameter) p).setY(TOP_PADDING).setX(X_SPACING*xMultiplier).addToContainer(UIDeviceKnobs.this);
                xMultiplier++;
            }
        });
    }
}
