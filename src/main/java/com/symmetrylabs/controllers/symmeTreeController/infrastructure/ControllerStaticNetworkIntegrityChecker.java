package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import com.symmetrylabs.slstudio.network.NetworkDevice;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Maintain a map from IP address to controller name.  If there is ever a collision warn about it (there should never be two)
 */
public class ControllerStaticNetworkIntegrityChecker {
    PersistentControllerByHumanIdMap controllerByHumanIdMap = PersistentControllerByHumanIdMap.loadFromDisk();

    HashMap<InetAddress, String> controllerByInetAddressMap = new HashMap<>();

    boolean noProblemsFound = true;

    // for given dictionary of controllers ensure that there are no duplicate IPs
    public boolean staticNetworkAllocationIsValid(){
        for (String key : controllerByHumanIdMap.slControllerIndex.keySet()){
            NetworkDevice deviceToCheck = controllerByHumanIdMap.slControllerIndex.get(key);
            if (controllerByInetAddressMap.containsKey(deviceToCheck.ipAddress)){
                System.out.println("Conflict! " + deviceToCheck.ipAddress  +  "  Controller " + key + " matches IP address with " + controllerByInetAddressMap.get(deviceToCheck.ipAddress));
                noProblemsFound = false;
                return false;
            }
            controllerByInetAddressMap.put(deviceToCheck.ipAddress, key);
        }
        if (noProblemsFound){
            System.out.println("SUCCESS - no problems found");
        }

        return true; // guilty until proven innocent
    }
}
