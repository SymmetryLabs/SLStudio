package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.component.GammaExpander;
import heronarts.lx.LX;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.model.LXPoint;

import java.net.UnknownHostException;

public class ArtNetDmxDatagram extends LXDatagram {
    private static final int ARTNET_DMX_HEADER_LENGTH = 6;
    private static final short ARTNET_DMX_OPCODE = 0x5000;
    private static final int SEQUENCE_INDEX = 12;

    private static final int DEFAULT_UNIVERSE = 0;
    private static final long FLASH_NANOS = 100_000_000;

    private int[] pointIndices;
    private boolean sequenceEnabled = false;
    private byte sequence = 1;

    private int unmappedPointColor = 0x000000;
    private boolean flashUnmapped = false;
    private boolean flashInOn = true;
    private long lastFlashNanos = System.nanoTime();

    private GammaExpander gammaExpander;
    private byte[] datagram;

    public ArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int universeNumber) {
        this(lx, ipAddress, indices, 3 * indices.length, universeNumber);
    }

    public ArtNetDmxDatagram(LX lx, String ipAddress, int[] indices, int dataLength, int universeNumber) {
        super(ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH + dataLength + (dataLength % 2));

        this.pointIndices = indices;
        this.gammaExpander = GammaExpander.getInstance(lx);
        this.datagram = new byte[buffer.length];
        System.arraycopy(buffer, 0, datagram, 0, buffer.length);

        try {
            setAddress(ipAddress);
            setPort(ArtNetDatagramUtil.ARTNET_PORT);
        } catch (UnknownHostException e) {
            // System.out.println("MappingPixlite with ip address (" + ipAddress + ") is not on the network.");
        }

        ArtNetDatagramUtil.fillHeader(datagram, ARTNET_DMX_OPCODE);
        datagram[12] = 0; // Sequence
        datagram[13] = 0; // Physical
        setUniverse(universeNumber);
        setDataLength(dataLength);
    }

    public ArtNetDmxDatagram setUnmappedPointColor(int c, boolean flash) {
        unmappedPointColor = c;
        flashUnmapped = flash;
        return this;
    }

    public ArtNetDmxDatagram setSequenceEnabled(boolean sequenceEnabled) {
        this.sequenceEnabled = sequenceEnabled;
        return this;
    }

    public void setUniverse(int universe) {
        datagram[14] = (byte) (universe & 0xff); // Universe LSB
        datagram[15] = (byte) ((universe >>> 8) & 0xff); // Universe MSB
    }

    public void setDataLength(int dataLength) {
        datagram[16] = (byte) ((dataLength >>> 8) & 0xff);
        datagram[17] = (byte) (dataLength & 0xff);
    }

    public void updatePoints(LXPoint[] points) {
        int[] indices = new int[points.length];
        int i = 0;
        for (LXPoint p : points) {
            indices[i++] = p.index;
        }
        this.pointIndices = indices;
        this.datagram = new byte[ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH + 3 * indices.length];
        System.arraycopy(buffer, 0, datagram, 0, ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH);
        ArtNetDatagramUtil.fillHeader(datagram, ARTNET_DMX_OPCODE);
        setUniverse(DEFAULT_UNIVERSE);
        setDataLength(3 * indices.length);
    }

    public void setIndices(int[] indices) {
        this.pointIndices = indices;
        int dataLength = 3 * indices.length;
        this.datagram = new byte[ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH + dataLength];
        System.arraycopy(buffer, 0, datagram, 0, ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH);
        ArtNetDatagramUtil.fillHeader(datagram, ARTNET_DMX_OPCODE);
        setUniverse(DEFAULT_UNIVERSE);
        setDataLength(dataLength);
    }

    @Override
    public void onSend(int[] colors) {
        copyPointsGamma(colors, this.pointIndices, ArtNetDatagramUtil.HEADER_LENGTH + ARTNET_DMX_HEADER_LENGTH, datagram);

        if (this.sequenceEnabled) {
            if (++this.sequence == 0) {
                ++this.sequence;
            }
            datagram[SEQUENCE_INDEX] = this.sequence;
        }

        // Send the datagram
        send(datagram, getAddress(), getPort());

        // We need to slow down the speed at which we send the packets so that we don't overload our switches. 3us seems to
        // be about right - Yona
        busySleep(3000);
    }

    @Override
    public void send(byte[] datagram, java.net.InetAddress address, int port) {
        super.send(datagram, address, port);
    }

    LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset, byte[] datagram) {
        int i = offset;
        int[] byteOffset = BYTE_ORDERING[this.byteOrder.ordinal()];
        int unmappedC = flashUnmapped && !flashInOn ? 0 : unmappedPointColor;
        if (System.nanoTime() - lastFlashNanos > FLASH_NANOS) {
            lastFlashNanos = System.nanoTime();
            flashInOn = !flashInOn;
        }
        for (int index : pointIndices) {
            int colorValue = (index >= 0) ? colors[index] : unmappedC;

            int gammaExpanded = gammaExpander.getExpandedColor(colorValue);
            datagram[i + byteOffset[0]] = (byte) Ops8.red(gammaExpanded);
            datagram[i + byteOffset[1]] = (byte) Ops8.green(gammaExpanded);
            datagram[i + byteOffset[2]] = (byte) Ops8.blue(gammaExpanded);

            i += 3;
        }
        return this;
    }

    public void busySleep(long nanos) {
        long elapsed;
        final long startTime = System.nanoTime();
        do {
            elapsed = System.nanoTime() - startTime;
        } while (elapsed < nanos);
    }
}