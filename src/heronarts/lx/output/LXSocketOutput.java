/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Mark C. Slee <mark@heronarts.com>
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
