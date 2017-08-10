public class OscOutput extends LXOutput {

    private String host;
    private int port;
    private LXOscEngine.Transmitter transmitter;

    public OscOutput(LX lx, String host, int port)
            throws SocketException, UnknownHostException {

        super(lx);

        this.host = host;
        this.port = port;

        transmitter = lx.engine.osc.transmitter(host, port);
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
