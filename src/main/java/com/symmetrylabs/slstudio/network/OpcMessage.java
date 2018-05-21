package com.symmetrylabs.slstudio.network;

/** Serializer and deserializer for the OPC message format: http://openpixelcontrol.org */
public class OpcMessage {
    public final byte[] bytes;  // raw bytes as seen on the wire
    public final byte channel;  // channel number (0 for broadcast)
    public final byte command;  // command (see command codes below)
    public final byte[] payload;

    // Command codes
    public static final byte SET_PIXEL_COLORS = 0;
    public static final byte SET_16BIT_PIXEL_COLORS = 2;
    public static final byte SYSTEM_EXCLUSIVE = (byte) 0xff;

    // System IDs
    public static final int SYMMETRY_LABS = 2;

    // Sysex codes
    public static final int SYMMETRY_LABS_IDENTIFY = 0;
    public static final int SYMMETRY_LABS_IDENTIFY_REPLY = 1;
    public static final int SYMMETRY_LABS_RESET = 2;
    public static final int SYMMETRY_LABS_RESET_REPLY = 3;

    /** Constructs an OpcMessage from an array of raw bytes as seen on the wire. */
    public OpcMessage(byte[] bytes) {
        this.bytes = bytes;
        channel = bytes.length > 0 ? bytes[0] : 0;
        command = bytes.length > 1 ? bytes[1] : 0;
        if (bytes.length >= 4) {
            int length = toInt(bytes[2], bytes[3]);
            payload = new byte[Math.min(length, bytes.length - 4)];
            System.arraycopy(bytes, 4, payload, 0, payload.length);
        } else {
            payload = new byte[0];
        }
    }

    /** Constructs an OpcMessage with a given channel, command, and payload. */
    public OpcMessage(int channel, int command, byte[] payload) {
        if (channel > 0xff) {
            throw new IllegalArgumentException("OPC channel is invalid (" + channel + " > 255)");
        }
        if (command > 0xff) {
            throw new IllegalArgumentException("OPC command is invalid (" + command + " > 255)");
        }
        if (payload.length > 0xffff) {
            throw new IllegalArgumentException("OPC payload too large (" + payload.length + " > 65535)");
        }
        this.channel = (byte) channel;
        this.command = (byte) command;
        this.payload = payload;
        bytes = new byte[payload.length + 4];
        bytes[0] = this.channel;
        bytes[1] = this.command;
        bytes[2] = (byte) (payload.length >> 8);
        bytes[3] = (byte) (payload.length & 0xff);
        System.arraycopy(payload, 0, bytes, 4, payload.length);
    }

    /** Constructs an OpcMessage with a given channel and command, and no payload. */
    public OpcMessage(int channel, int command) {
        this(channel, command, new byte[0]);
    }

    /** Constructs a system-exclusive OpcMessage. */
    public OpcMessage(int channel, int systemId, int sysexCode, byte[] sysexContent) {
        this(channel, SYSTEM_EXCLUSIVE, createSysexPayload(systemId, sysexCode, sysexContent));
    }

    /** Constructs a system-exclusive OpcMessage with no inner content. */
    public OpcMessage(int channel, int systemId, int sysexCode) {
        this(channel, systemId, sysexCode, new byte[0]);
    }

    /** Returns true if this is a system-exclusive message. */
    public boolean isSysex() {
        return command == SYSTEM_EXCLUSIVE && payload.length >= 4;
    }

    /** Returns true if this is the specified kind of system-exclusive message. */
    public boolean isSysex(int systemId, int sysexCode) {
        return isSysex() && getSystemId() == systemId && getSysexCode() == sysexCode;
    }

        /** Returns the system ID, if this is a system-exclusive message. */
    public int getSystemId() {
        return isSysex() ? toInt(payload[0], payload[1]) : 0;
    }

    /** Returns the system-exclusive command code, if this is a system-exclusive message. */
    public int getSysexCode() {
        return isSysex() ? toInt(payload[2], payload[3]) : 0;
    }

    /** Returns the inner content of a system-exclusive message. */
    public byte[] getSysexContent() {
        if (!isSysex()) return null;
      byte[] content = new byte[payload.length - 4];
        System.arraycopy(payload, 4, content, 0, content.length);
        return content;
    }

    protected static int toInt(byte hi, byte lo) {
        return (Byte.toUnsignedInt(hi) << 8) | Byte.toUnsignedInt(lo);
    }

    protected static byte[] createSysexPayload(int systemId, int sysexCode, byte[] sysexPayload) {
        byte[] payload = new byte[sysexPayload.length + 4];
        payload[0] = (byte) (systemId >> 8);
        payload[1] = (byte) (systemId & 0xff);
        payload[2] = (byte) (sysexCode >> 8);
        payload[3] = (byte) (sysexCode & 0xff);
        System.arraycopy(sysexPayload, 0, payload, 4, sysexPayload.length);
        return payload;
    }
}
