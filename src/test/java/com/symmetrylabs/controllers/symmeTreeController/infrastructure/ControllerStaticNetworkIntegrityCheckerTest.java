package com.symmetrylabs.controllers.symmeTreeController.infrastructure;

import org.junit.Test;

import static org.junit.Assert.*;

public class ControllerStaticNetworkIntegrityCheckerTest {

    @Test
    public void staticNetworkAllocationIsValid() {
        ControllerStaticNetworkIntegrityChecker checker = new ControllerStaticNetworkIntegrityChecker();
        checker.staticNetworkAllocationIsValid();
    }
}
