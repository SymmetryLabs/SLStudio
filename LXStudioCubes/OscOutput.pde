public class OscOutput extends LXOutput {

    private InetAddress address;
    private int port;
    private LXOscEngine.Transmitter transmitter;

    public OscOutput(LX lx, InetAddress address, int port) throws SocketException {

        super(lx);

        this.address = address;
        this.port = port;

        transmitter = lx.engine.osc.transmitter(address, port);
    }

    @Override
    protected void onSend(int[] colors) {
        try {
            OscMessage m = new OscMessage();
            for (int i = 0; i < colors.length; ++i) {
                m.add(colors[i]);
            }
            transmitter.send(m);
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
}
