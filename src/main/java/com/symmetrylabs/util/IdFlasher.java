package com.symmetrylabs.util;

/**
 * Generates a sequence of flashes that allows you to read off an ID number
 * associated with a given pixel or part of the model.  The digits of the ID
 * are flashed in sequence, using the colors red, green, blue, white for the
 * different digits.  To help you prepare to count flashes, there is a brief
 * pause before the flash sequence during which the pixels are lit in grey.
 * Zero digits are shown as one short flash.
 *
 * For example, if maxDigits is 4, an ID of 326 will generate the sequence:
 * grey pause, short red flash (representing zero), brief pause, 3 green
 * flashes, brief pause, 2 blue flashes, brief pause, 6 white flashes.
 */
public class IdFlasher {
    final int flashOnMs; // duration of the on part of one flash
    final int flashOffMs; // duration of the off part of one flash
    final int flashPeriodMs; // total on + off duration of one flash
    final int pauseMs; // length of time to pause after each entire sequence
    final int cyclePeriodMs; // total duration of one entire cycle
    final int maxDigits;

    final int offColor = 0xff000000;
    final int pauseColor = 0xff606060; // "get ready" color shown before sequence
    final int[] colors = new int[] {0xffff0000, 0xff00ff00, 0xff0000ff, 0xffffffff};

    int elapsedMs = 0;
    int flashIndex = 0; // how many flashes into the sequence we're at

    enum FlashState { SHORT_ON, ON, OFF, PAUSE };
    FlashState state = FlashState.OFF;

    public IdFlasher(double flashOnSec, double flashOffSec, double pauseSec, int maxDigits) {
        flashOnMs = (int) (flashOnSec * 1000);
        flashOffMs = (int) (flashOffSec * 1000);
        flashPeriodMs = flashOnMs + flashOffMs;
        pauseMs = (int) (pauseSec * 1000);
        cyclePeriodMs = flashPeriodMs * maxDigits * 11 + pauseMs;
        this.maxDigits = maxDigits;
    }

    public void restart() {
        elapsedMs = cyclePeriodMs - pauseMs;
    }

    public void advance(double deltaSec) {
        elapsedMs += (int) (deltaSec * 1000);
        int cycleMs = elapsedMs % cyclePeriodMs;
        flashIndex = cycleMs / flashPeriodMs;
        if (cycleMs > cyclePeriodMs - pauseMs) {
            state = FlashState.PAUSE;
        } else if (cycleMs % flashPeriodMs < flashOnMs / 4) {
            state = FlashState.SHORT_ON;
        } else if (cycleMs % flashPeriodMs < flashOnMs) {
            state = FlashState.ON;
        } else {
            state = FlashState.OFF;
        }
    }

    public int getColor(int id) {
        if (state == FlashState.OFF) return offColor;
        if (state == FlashState.PAUSE) return pauseColor;

        int[] digits = new int[maxDigits];
        for (int i = maxDigits - 1; i >= 0; i--) {
            digits[i] = id % 10;
            id /= 10;
        }
        int fi = flashIndex;
        for (int i = 0; i < maxDigits; i++) {
            int onColor = colors[i % colors.length];
            if (digits[i] == 0 && fi == 0) {
                // Display a zero as one quick flash.
                return (state == FlashState.SHORT_ON) ? onColor : offColor;
            } else if (fi >= 0 && fi < digits[i]) {
                return onColor;
            } else {
                fi -= (digits[i] == 0 ? 1 : digits[i]);
                 fi -= 1;  // insert a space between digits
            }
        }
        return 0;
    }
}
