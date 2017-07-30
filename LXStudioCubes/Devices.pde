
class DeviceOSCListener implements LXOscListener {
    private OscServer.Connection connection;

    public DeviceOSCListener(OscServer.Connection connection) {
        this.connection = connection;
    }

    public void oscMessage(OscMessage msg) {
        String[] splitMsg = msg.getAddressPattern().toString().split("/");
        //System.out.println(msg);

        if ("nebula".equals(splitMsg[1])) {
            String deviceName = splitMsg[2];
            InetAddress deviceAddress = msg.getSource();
            String param = splitMsg[3];

            Device device = deviceController.getOrCreateDevice(deviceName, deviceAddress, connection);
            if (device == null)
                return;

            if ("inputs".equals(param)) {
                List<String> inputs = new ArrayList<String>();
                for (OscArgument arg : msg) {
                    if (arg instanceof OscString) {
                        inputs.add(((OscString)arg).getValue());
                    }
                }
                device.ensureInputs(inputs);
            }
            else {
                float value = msg.getFloat(0);
                device.setParameterValue(param, value);
            }
        }
    }
}

public interface DeviceParameterWatcher {
    public void onParameterAdded(BoundedParameter p);
}

public interface DeviceWatcher {
    public void onDeviceAdded(Device d);
    public void onDeviceRemoved(Device d);
}

class Device extends LXComponent {
    String name;
    InetAddress address;

    private Map<String, BoundedParameter> deviceParams = new HashMap<String, BoundedParameter>();
    private Map<String, BoundedParameter> inputParams = new HashMap<String, BoundedParameter>();

    private List<DeviceParameterWatcher> paramWatchers = new ArrayList<DeviceParameterWatcher>();
    private List<DeviceParameterWatcher> inputWatchers = new ArrayList<DeviceParameterWatcher>();

    private OscServer.Connection connection;

    Device(LX lx, String name, InetAddress address, OscServer.Connection connection) {
        super(lx);

        this.name = name;
        this.address = address;
        this.connection = connection;
    }

    public Device setConnection(OscServer.Connection connection) {
        this.connection = connection;
        return this;
    }

    public Collection<BoundedParameter> getDeviceParameters() {
        return Collections.unmodifiableCollection(deviceParams.values());
    }

    public Collection<BoundedParameter> getInputParameters() {
        return Collections.unmodifiableCollection(inputParams.values());
    }

    public Device setParameterValue(String param, float value) {
        BoundedParameter p = ensureParameter(param);
        p.setValue(value);

        return this;
    }

    public BoundedParameter ensureParameter(String paramName) {
        BoundedParameter p = deviceParams.get(paramName);
        if (p == null) {
            p = new CompoundParameter(paramName);
            deviceParams.put(paramName, p);
            addParameter(paramName, p);

            for (DeviceParameterWatcher w : paramWatchers) {
                w.onParameterAdded(p);
            }
        }
        return p;
    }

    public void ensureInputs(List<String> inputs) {
        for (String inputName : inputs) {
            System.out.println("Device " + name + " input: " + inputName);

            BoundedParameter p = inputParams.get(inputName);
            if (p == null) {
                p = new CompoundParameter(inputName);
                inputParams.put(inputName, p);
                addParameter("input-" + inputName, p);

                p.addListener(new LXParameterListener() {
                    @Override
                    public void onParameterChanged(LXParameter p) {
                        sendInput(p.getLabel(), (float)p.getValue());
                    }
                });

                for (DeviceParameterWatcher w : inputWatchers) {
                    w.onParameterAdded(p);
                }
            }
        }
    }

    public void queryInputs() {
        if (connection == null)
            return;

        System.out.println("Sending list-inputs to: " + name);
        OscMessage m = new OscMessage("/list-inputs");
        try {
            connection.send(m);
        }
        catch (java.io.IOException e) {
            System.err.println(e);
        }
    }

    public void sendInput(String name, float value) {
        if (connection == null)
            return;

        OscMessage m = new OscMessage("/input/" + name);
        m.add(value);
        try {
            connection.send(m);
        }
        catch (java.io.IOException e) {
            System.err.println(e);
        }
    }

    public void addParameterWatcher(DeviceParameterWatcher w) {
        paramWatchers.add(w);
    }

    public void addInputWatcher(DeviceParameterWatcher w) {
        inputWatchers.add(w);
    }
}

class DeviceController extends LXComponent implements LXLoopTask {

    private Map<String, Device> deviceByName = new HashMap<String, Device>();
    private Map<String, List<BoundedParameter>> allDeviceInputs = new HashMap<String, List<BoundedParameter>>();
    private Map<String, BoundedParameter> inputProxyParams = new HashMap<String, BoundedParameter>();
    private List<DeviceParameterWatcher> inputWatchers = new ArrayList<DeviceParameterWatcher>();
    private List<DeviceWatcher> deviceWatchers = new ArrayList<DeviceWatcher>();

    DeviceController(LX lx) {
        super(lx);

        lx.engine.addLoopTask(this);
    }

    @Override
    public void loop(double deltaMs) {
        for (BoundedParameter proxyParam : inputProxyParams.values()) {
            String inputName = proxyParam.getLabel();
            //System.err.println(inputName + ": " + proxyParam.getValue());
            List<BoundedParameter> deviceInputParams = allDeviceInputs.get(inputName);
            if (deviceInputParams == null)
                continue;

            for (BoundedParameter p : deviceInputParams) {
                p.setValue(proxyParam.getValue());
            }
        }
    }

    public Device getDeviceByName(String name) {
        return deviceByName.get(name);
    }

    public Device getOrCreateDevice(String name, InetAddress addr, OscServer.Connection c) {
        Device device = deviceByName.get(name);
        if (device == null) {
            System.out.println("New device: " + name + " (" + addr.getHostAddress() + ")");
            device = new Device(lx, name, addr, c);
            deviceByName.put(device.name, device);

            final Device d = device;
            device.addInputWatcher(new DeviceParameterWatcher() {
                @Override
                public void onParameterAdded(BoundedParameter p) {
                    String inputName = p.getLabel();
                    System.out.println("New input for device " + d.name + ": " + inputName);
                    List<BoundedParameter> params = allDeviceInputs.get(inputName);
                    if (params == null) {
                        params = new ArrayList<BoundedParameter>();
                        allDeviceInputs.put(inputName, params);

                        BoundedParameter proxyParam = new CompoundParameter(inputName);
                        inputProxyParams.put(inputName, proxyParam);
                        addParameter(proxyParam);

                        for (DeviceParameterWatcher w : inputWatchers) {
                            w.onParameterAdded(proxyParam);
                        }
                    }
                    params.add(p);
                }
            });
            device.queryInputs();

            for (DeviceWatcher w : deviceWatchers) {
                w.onDeviceAdded(device);
            }
        }
        else {
            device.setConnection(c);
        }
        return device;
    }

    public Collection<BoundedParameter> getInputParameters() {
        return Collections.unmodifiableCollection(inputProxyParams.values());
    }

    public BoundedParameter getInputParameter(String inputName) {
        return inputProxyParams.get(inputName);
    }

    public void addInputWatcher(DeviceParameterWatcher w) {
        inputWatchers.add(w);
    }

    public Collection<Device> getDevices() {
        return Collections.unmodifiableCollection(deviceByName.values());
    }

    public void addDeviceWatcher(DeviceWatcher w) {
        deviceWatchers.add(w);
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
        }
        if (keyCode == 68) {
            Device selected = ((DeviceListItem) getFocusedItem()).device;
            // deviceController.removeDevice(selected);
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

        final DeviceList outputList = new DeviceList(ui, 0, 8, w-8, h, sc);

        for (Device d : deviceController.getDevices()) {
            outputList.addItem(new DeviceListItem(d));
        }

        outputList.setSingleClickActivate(true);
        outputList.addToContainer(this);

        deviceController.addDeviceWatcher(new DeviceWatcher() {
            @Override
            public void onDeviceAdded(Device d) {
                outputList.addItem(new DeviceListItem(d));
            }

            @Override
            public void onDeviceRemoved(Device d) {

            }
        });
    }
}

public class UIInputKnobs extends UICollapsibleSection {

    private final static int TOP_PADDING = 4;
    private final static int X_SPACING = UIKnob.WIDTH + 1;
    private final static int Y_SPACING = UIKnob.HEIGHT + TOP_PADDING;
    private final static int TOTAL_WIDTH = (UIKnob.WIDTH + 1) * 6;
    private final static int TOP_MARGIN = 20;
    private final static int RIGHT_MARGIN = 10;

    private int knobCount = 0;
    private int numColumns;

    UIInputKnobs(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w + RIGHT_MARGIN, (UIKnob.HEIGHT + TOP_PADDING)*2 + TOP_MARGIN);
        setTitle("Inputs");
        setChildMargin(0);

        numColumns = (int)(w / UIKnob.WIDTH);

        for (BoundedParameter p : deviceController.getInputParameters()) {
            int row = knobCount / numColumns;
            int column = knobCount % numColumns;

            new UIKnob(X_SPACING * column, Y_SPACING * row)
                .setParameter(p)
                .addToContainer(UIInputKnobs.this);

            ++knobCount;
        }

        deviceController.addInputWatcher(new DeviceParameterWatcher() {
            @Override
            public void onParameterAdded(BoundedParameter p) {
                int row = knobCount / numColumns;
                int column = knobCount % numColumns;

                new UIKnob(X_SPACING * column, Y_SPACING * row)
                    .setParameter(p)
                    .addToContainer(UIInputKnobs.this);

                ++knobCount;
            }
        });
    }
}

public class UIDeviceKnobs extends UICollapsibleSection {

    private final static int TOP_PADDING = 4;
    private final static int X_SPACING = UIKnob.WIDTH + 1;
    private final static int Y_SPACING = UIKnob.HEIGHT + TOP_PADDING;
    private final static int TOTAL_WIDTH = (UIKnob.WIDTH + 1) * 6;
    private final static int TOP_MARGIN = 20;
    private final static int RIGHT_MARGIN = 10;

    private int knobCount = 0;
    private int numColumns;

    UIDeviceKnobs(LX lx, UI ui, float x, float y, float w, Device device) {
        super(ui, x, y, w + RIGHT_MARGIN, (UIKnob.HEIGHT + TOP_PADDING)*2 + TOP_MARGIN);
        setTitle(device.name);
        setChildMargin(0);

        numColumns = (int)(w / UIKnob.WIDTH);
        Collection<BoundedParameter> deviceParams = device.getDeviceParameters();

        for (BoundedParameter p : deviceParams) {
            int row = knobCount / numColumns;
            int column = knobCount % numColumns;

            new UIKnob(X_SPACING * column, Y_SPACING * row)
                .setParameter(p)
                .addToContainer(UIDeviceKnobs.this);

            ++knobCount;
        }

        device.addParameterWatcher(new DeviceParameterWatcher() {
            @Override
            public void onParameterAdded(BoundedParameter p) {
                int row = knobCount / numColumns;
                int column = knobCount % numColumns;

                new UIKnob(X_SPACING * column, Y_SPACING * row)
                    .setParameter(p)
                    .addToContainer(UIDeviceKnobs.this);

                ++knobCount;
            }
        });
    }
}
