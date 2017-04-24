public class TestScene extends Scene {
    public TestScene(LX lx) {
        super(lx, "TestScene");

        // Actions are blocks of code that you can trigger to run on devices
        registerAction(new Action("turnGreen", ActionMode.RUNS_ONCE) {
            protected void trigger(LX lx, double deltaMs) {
                lx.palette.hueMode.setValue(LXPalette.Mode.FIXED);
                lx.palette.clr.hue.setValue(100);
                lx.palette.clr.saturation.setValue(100);
                lx.palette.clr.brightness.setValue(100);
            }
        });
    }

    public void loopScene(double deltaMs) {
        // you can call triggerAction to trigger action of specific device
        for (Device device : devices)
            triggerAction(device, "turnGreen");

        // for you can call any osc route and pass a value (see Argument types in Device.pde)
        // device.dispatch("lx/channel/pattern1/parameter1", new FloatArgument(4.5));
    }
}