/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.output;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import heronarts.lx.LX;

public abstract class LXSocketOutput extends LXOutput {

    public final String host;
    public final int port;

    protected Socket socket;
    protected OutputStream output;

    protected LXSocketOutput(LX lx, String host, int port) {
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
                this.socket = new Socket();
                this.socket.connect(new InetSocketAddress(this.host, this.port), 100);
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
