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
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor16;

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
    protected void onSend(PolyBuffer src) {
        connect();
        if (isConnected()) {
            try {
                this.output.write(getPacketData(src));
            } catch (IOException iox) {
                dispose(iox);
            }
        }
    }

    /**
     * Old-style subclasses override this method to produce 8-bit color data.
     * New-style subclasses should override getPacketData(PolyBuffer) instead.
     * @param colors 8-bit color values
     */
    protected /* abstract */ byte[] getPacketData(int[] colors) {
        throw new RuntimeException("getPacketData() not implemented");
    }

    /**
     * Assembles a packet of data to send.  Subclasses should override this method.
     * @param src The color data to send.
     */
    protected /* abstract */ byte[] getPacketData(PolyBuffer src) {
        // For compatibility, this invokes the method that previous subclasses
        // were supposed to implement.  Implementations of getPacketData(int[])
        // know only how to send 8-bit color data, so that's what we pass to them.
        return getPacketData((int[]) src.getArray(PolyBuffer.Space.RGB8));

        // New subclasses should override and replace this method with one that
        // obtains a color array in the desired space using src.getArray(space),
        // and generates the packet data from that array.
    }
}
