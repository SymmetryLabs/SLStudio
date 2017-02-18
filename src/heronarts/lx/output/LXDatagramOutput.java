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

import heronarts.lx.LX;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * An output stage that functions by sending datagram packets.
 */
public class LXDatagramOutput extends LXOutput {

    private final DatagramSocket socket;

    private final List<LXDatagram> datagrams = new ArrayList<LXDatagram>();

    public LXDatagramOutput(LX lx) throws SocketException {
        this(lx, new DatagramSocket());
    }

    public LXDatagramOutput(LX lx, DatagramSocket socket) {
        super(lx);
        this.socket = socket;
    }

    public LXDatagramOutput addDatagram(LXDatagram datagram) {
        this.datagrams.add(datagram);
        return this;
    }

    public LXDatagramOutput addDatagrams(LXDatagram[] datagrams) {
        for (LXDatagram datagram : datagrams) {
            addDatagram(datagram);
        }
        return this;
    }

    /**
     * Subclasses may override. Invoked before datagrams are sent.
     *
     * @param colors Color values
     */
    protected /* abstract */ void beforeSend(int[] colors) {}

    /**
     * Subclasses may override. Invoked after datagrams are sent.
     *
     * @param colors Color values
     */
    protected /* abstract */ void afterSend(int[] colors) {}

    /**
     * Core method which sends the datagrams.
     */
    @Override
    protected final void onSend(int[] colors) {
        long now = System.currentTimeMillis();
        beforeSend(colors);
        for (LXDatagram datagram : this.datagrams) {
            if (datagram.enabled.isOn() && (now > datagram.sendAfter)) {
                datagram.onSend(colors);
                try {
                    this.socket.send(datagram.packet);
                    if (datagram.failureCount > 0) {
                        System.out.println("Recovered connectivity to "
                                + datagram.packet.getAddress());
                    }
                    datagram.failureCount = 0;
                    datagram.sendAfter = 0;
                } catch (IOException iox) {
                    if (datagram.failureCount == 0) {
                        System.out.println("IOException sending to "
                                + datagram.packet.getAddress() + " (" + iox.getMessage()
                                + "), " + "will initiate backoff after 3 consecutive failures");
                    }
                    ++datagram.failureCount;
                    if (datagram.failureCount >= 3) {
                        int pow = Math.min(5, datagram.failureCount - 3);
                        long waitFor = (long) (50 * Math.pow(2, pow));
                        System.out.println("Retrying " + datagram.packet.getAddress()
                                + " in " + waitFor + "ms" + " (" + datagram.failureCount
                                + " consecutive failures)");
                        datagram.sendAfter = now + waitFor;
                    }
                }
            }
        }
        afterSend(colors);
    }
}
