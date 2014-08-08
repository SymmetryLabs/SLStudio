/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.output;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

import heronarts.lx.LX;

public abstract class LXSocketOutput extends LXOutput {

    public final String host;
    public final int port;

    protected Socket socket;
    protected OutputStream output;

    LXSocketOutput(LX lx, String host, int port) {
        super(lx);
        this.host = host;
        this.port = port;
    }

    public boolean isConnected() {
        return (this.socket != null);
    }

    private void connect() {
        if (this.socket == null) {
            try {
                this.socket = new Socket(this.host, this.port);
                this.socket.setTcpNoDelay(true);
                this.output = this.socket.getOutputStream();
                didConnect();
            } catch (ConnectException cx) {
                dispose(cx);
            } catch (IOException iox) {
                dispose(iox);
            }
        }
    }

    protected void didConnect() {

    }

    protected void dispose(Exception x) {
        this.socket = null;
        this.output = null;
        didDispose(x);
    }

    protected void didDispose(Exception x) {
    }

    @Override
    protected void onSend(int[] colors) {
        connect();
        if (isConnected()) {
            try {
                this.output.write(getPacketData(colors));
            } catch (IOException iox) {
                dispose(iox);
            }
        }
    }

    protected abstract byte[] getPacketData(int[] colors);

}
