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

import heronarts.lx.LX;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An output stage that functions by sending datagram packets.
 */
public class LXDatagramOutput extends LXOutput {

    private final DatagramSocket socket;

    private final Map<InetAddress, LXDatagramDestination> destinations = new HashMap<InetAddress, LXDatagramDestination>();
    private final List<LXDatagram> datagrams = new ArrayList<LXDatagram>();

    private final SimpleDateFormat date = new SimpleDateFormat("[HH:mm:ss]");

    public LXDatagramOutput(LX lx) throws SocketException {
        this(lx, new DatagramSocket());
    }

    public LXDatagramOutput(LX lx, DatagramSocket socket) {
        super(lx);
        this.socket = socket;
    }

    public LXDatagramOutput addDatagram(LXDatagram datagram) {
        this.datagrams.add(datagram);
        LXDatagramDestination destination = this.destinations.get(datagram.getAddress());
        if (destination == null) {
            destination = new LXDatagramDestination();
            this.destinations.put(datagram.getAddress(), destination);
        }
        datagram.destination = destination;
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
            if (datagram.enabled.isOn() && (now > datagram.destination.sendAfter)) {
                datagram.onSend(colors);
                try {
                    this.socket.send(datagram.packet);
                    if (datagram.destination.failureCount > 0) {
                        System.out.println(this.date.format(now) + " Recovered connectivity to " + datagram.packet.getAddress());
                    }
                    datagram.destination.error.setValue(false);
                    datagram.destination.failureCount = 0;
                    datagram.destination.sendAfter = 0;
                } catch (IOException iox) {
                    if (datagram.destination.failureCount == 0) {
                        System.out.println(this.date.format(now) + " IOException sending to "
                                + datagram.packet.getAddress() + " (" + iox.getLocalizedMessage()
                                + "), will initiate backoff after 3 consecutive failures");
                    }
                    ++datagram.destination.failureCount;
                    if (datagram.destination.failureCount >= 3) {
                        int pow = Math.min(5, datagram.destination.failureCount - 3);
                        long waitFor = (long) (50 * Math.pow(2, pow));
                        System.out.println(this.date.format(now) + " Retrying " + datagram.packet.getAddress()
                                + " in " + waitFor + "ms" + " (" + datagram.destination.failureCount
                                + " consecutive failures)");
                        datagram.destination.sendAfter = now + waitFor;
                        datagram.destination.error.setValue(true);
                    }
                }
            }
        }
        afterSend(colors);
    }
}
