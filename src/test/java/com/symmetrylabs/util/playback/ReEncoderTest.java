package com.symmetrylabs.util.playback;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReEncoderTest {
    ReEncoder rere;

    @Test
    public void testReEncode(){
        System.out.println("reEncodeTest");
        rere = new ReEncoder();

        rere.transcode();
    }

}
