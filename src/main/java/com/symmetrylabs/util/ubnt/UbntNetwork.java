package com.symmetrylabs.util.ubnt;

public class UbntNetwork {
    private static int NUM_SWITCHES = 14;
    UbntSwitch ubntSwitches[] = new UbntSwitch[NUM_SWITCHES];

    // metaparams for specifying ips 10.200.1.240, 10.200.1.241, ... 10.200.1.253
    // The UBNT switches are given these IPs using the /private/etc/bootptab DHCP static configs file
    int NUM_UBNT_IPS = 14;
    int BASE_IP = 240;
    String[] ips = new String[NUM_UBNT_IPS];


    public void fmtIPs_and_constructUbnt(){
        // IP range
        for (int i = BASE_IP; i < BASE_IP + NUM_UBNT_IPS; i++){
            String thisIP = String.format("10.200.1.%d", i);
            ips[i - BASE_IP] = thisIP;
            ubntSwitches[i - BASE_IP] = new UbntSwitch(thisIP);
        }
    }


    public UbntNetwork(){
        // instantiate all the connections on the structure.

        fmtIPs_and_constructUbnt();
        int i = 0;
        for( String ip : ips ) {
        }


    }
}
