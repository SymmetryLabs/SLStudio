// List of all devices on network (including itself)
public static ListenableList<Device> devices = new ListenableList<Device>();

public class Device {
    private InetAddress address = null;
    private LXOscEngine.Transmitter transmitter = null;

    public Device(LX lx, String ip) {
        try {
            this.address = InetAddress.getByName(ip);
            this.transmitter = lx.engine.osc.transmitter(address, 2121);
        } catch (UnknownHostException e) {
            println(e);
        } catch (SocketException e) {
            println("Could not create osc transmitter on device (" + address.getHostName() + ")." + e.getMessage());
        }
    }

    public void dispatch(String route, MessageArgument... arguments) {
        OscMessage message = new OscMessage();
        message.setAddressPattern(route);

        for (MessageArgument argument : arguments) {
            String type = argument.getType();

            switch(type) {
                case "float":
                    message.add(new OscFloat(((FloatArgument)argument).value));
                    break;
                case "double":
                    message.add(new OscDouble(((DoubleArgument)argument).value));
                    break;
                case "int":
                    message.add(new OscInt(((IntArgument)argument).value));
                    break;
                case "boolean":
                    if (((BooleanArgument)argument).value)
                        message.add(new OscTrue());
                    else message.add(new OscFalse());
            }
        }

        try {
            transmitter.send(message);
        } catch (Exception e) {
            println("Could not transmit message (" + message + ") to device (" + address.getHostName() + "). " + e.getMessage());
        }
    }
}

// This is mostly because we want a flexible dispatch method on device and lx's osc types don't extend a common type
public abstract class MessageArgument {
    String type = null;

    public String getType() {
        return type;
    }
}

public class FloatArgument extends MessageArgument {
    final public float value;

    public FloatArgument(float value) {
        this.value = value;
        this.type = "float";
    }
}

public class DoubleArgument extends MessageArgument {
    final public double value;

    public DoubleArgument(double value) {
        this.value = value;
        this.type = "double";
    }
}

public class IntArgument extends MessageArgument {
    final public int value;

    public IntArgument(int value) {
        this.value = value;
        this.type = "int";
    }
}

public class BooleanArgument extends MessageArgument {
    final public boolean value;

    public BooleanArgument(boolean value) {
        this.value = value;
        this.type = "boolean";
    }
}
