package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.slstudio.ApplicationState;

public class OpcSysExMsg extends OpcMessage {
    byte channel;
    byte command;
    byte length_h;
    byte length_l;
    byte system_id_h;
    byte system_id_l;
    byte command_h;
    byte command_l;
    byte payload[];

    public OpcSysExMsg(int channel, int systemId, int sysexCode, byte[] sysexContent) {
        super(channel, systemId, sysexCode, sysexContent);
    }

    public OpcSysExMsg(byte[] data, int length) {
        super(data, length);
        channel = data[0];
        command = data[1];
        length_h = data[2];
        length_l = data[3];
        system_id_h = data[4];
        system_id_l = data[5];
        command_h = data[6];
        command_l = data[7];
        payload   = new byte[getLength()];
        System.arraycopy(data, 8, payload, 0, getLength());
    }

    private int getLength() {
        return (length_h << 8 | length_l & 0xff) - 4; // additional 4 bytes preallocated for systemID and command
    }


    public class MetaSample {
        // how to initialize two dimensional array in Java
        // using for loop
        int NUM_CHANNEL = 8;
        int UINT16_PER_CHANNEL = 4;
        int[][] allSample = new int[NUM_CHANNEL][UINT16_PER_CHANNEL];
        int[] analogSampleArray = new int[NUM_CHANNEL];

        int powerOnStateMask = 0;

        MetaSample(){
            for (int i = 0; i < NUM_CHANNEL; i++) {
                for (int j = 0; j < UINT16_PER_CHANNEL; j++) {
                    allSample[i][j] = 0;
                }
            }
        }
    }

    public MetaSample allChData = new MetaSample();

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public void deserializeSysEx_0x7(){
        String deserializeResult = "";
        for (int i = 0; i < getLength(); i += 2){
            // first 8 half words are the analog channel samples
            int chIndex = i/2;
            if (chIndex < 8){
                allChData.analogSampleArray[chIndex] = (unsignedToBytes(payload[i+1]) << 8) + (unsignedToBytes(payload[i]) & 0xff);
                deserializeResult += (allChData.analogSampleArray[chIndex]);
                deserializeResult += ("\t");
            }
            if (chIndex == 8){ // the fault mask
                allChData.powerOnStateMask = (unsignedToBytes(payload[i]));
                for (int j = 0; j < 8; j ++){
                    if ( ((allChData.powerOnStateMask >> j) & 1) == 1 ){
                        deserializeResult += ("1");
                    }
                    else {
                        deserializeResult += ("0");
                    }
                }
                deserializeResult += ("\t");
            }


            // The old with all samples
//            int structIndex = (i%8) / 2;
//            allChData.allSample[chIndex][structIndex] = (unsignedToBytes(payload[i+1]) << 8) + (unsignedToBytes(payload[i]) & 0xff);
//            if (structIndex == 0){
//                System.out.print(allChData.allSample[chIndex][structIndex]);
//                System.out.print("\t");
//            }
        }
//        System.out.println(deserializeResult);
        ApplicationState.setWarning("powerSample: ", deserializeResult);
    }
}
