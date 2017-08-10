public class UdpOutput extends LXOutput {

    private int port;
    private DatagramSocket socket;
    private InetAddress address;
    private DatagramPacket packet;
    private byte[] buffer;

    public UdpOutput(LX lx, InetAddress address, int port)
            throws SocketException {

        super(lx);

        this.port = port;
        this.socket = new DatagramSocket();
        this.address = address;

        buffer = new byte[8192];
        packet = new DatagramPacket(buffer, buffer.length, address, port);
    }

    @Override
    protected void onSend(int[] colors) {
        int l = Math.min(colors.length, buffer.length / 4);
        for (int i = 0; i < l; ++i) {
            buffer[i * 4 + 0] = (byte)(colors[i]);
            buffer[i * 4 + 1] = (byte)(colors[i] >> 8);
            buffer[i * 4 + 2] = (byte)(colors[i] >> 16);
            buffer[i * 4 + 3] = (byte)(colors[i] >> 24);
        }

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
}
