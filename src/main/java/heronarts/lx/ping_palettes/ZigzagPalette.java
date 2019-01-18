package heronarts.lx.ping_palettes;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Ops8;
import heronarts.lx.color.LXColor;

/**
 * A palette that repeats the colors from a subrange of another palette in a zigzag
 * pattern over the number line: in forward order from 0 to 1, then reverse order
 * from 1 to 2, then forward order from 2 to 3, etc.  Provides several mutable
 * parameters: bottom, top, bias, shift, and cutoff.
 */
public class ZigzagPalette implements ColorPalette {
    ColorPalette palette;
    double bottom;
    double top;
    double bias;
    double exponent;
    double shift;
    double cutoff;

    ZigzagPalette(
        ColorPalette palette,
        double bottom, double top, double bias, double cutoff
    ) {
        this.palette = palette;
        setBottom(bottom);
        setTop(top);
        setBias(bias);
        setShift(shift);
        setCutoff(cutoff);
    }

    public ZigzagPalette() {
        this(new ConstantPalette(0));
    }

    ZigzagPalette(ColorPalette palette) {
        this(palette, 0, 1, 0, 0);
    }

    public ZigzagPalette(int[] colors) {
        this(new ArrayPalette(colors));
    }

    public ZigzagPalette copy() {
        return new ZigzagPalette(palette, bottom, top, bias, cutoff);
    }

    public void setPalette(ColorPalette palette) {
        this.palette = palette;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public void setTop(double top) {
        this.top = top;
    }

    /**
     * The bias factor shifts the output more toward the top end or the bottom end
     * of the palette, while keeping the top and bottom ends in place. A bias of -5
     * causes the output to spend most of its time (from p = 0 to about p = 0.95)
     * near the bottom end, then shoot quickly up to the top. A bias of +5 causes
     * the output to shoot quickly up to 0.95 of the way toward the top end and
     * then spend most of its time near the top end.
     */
    public void setBias(double bias) {
        exponent = Math.exp(bias);
    }

    public void setShift(double shift) {
        this.shift = shift;
    }

    public void setCutoff(double cutoff) {
        this.cutoff = cutoff;
    }

    public int getColor(double p) {
        p += shift;
        int floor = (int) p;
        p -= floor;
        if (floor % 2 != 0) {
            p = 1 - p;
        }
        if (exponent != 1) p = Math.pow(p, exponent);
        int c = palette.getColor(bottom + (top - bottom) * p);
        if (cutoff != 0) {
            double value = Ops8.mean(c);
            if (value < cutoff) return 0;
            c = LXColor.lerp(0, c, Math.pow(
                (value - cutoff) / (1.0 - cutoff), 0.5 * cutoff));
        }
        return c;
    }

    public long getColor16(double p) {
        p += shift;
        int floor = (int) p;
        p -= floor;
        if (floor % 2 != 0) {
            p = 1 - p;
        }
        if (exponent != 1) p = Math.pow(p, exponent);
        long c = palette.getColor16(bottom + (top - bottom) * p);
        if (cutoff != 0) {
            double value = Ops16.mean(c);
            if (value < cutoff) return 0;
            c = Ops16.blend(0L, c, Math.pow((value - cutoff) / (1.0 - cutoff), 0.5 * cutoff));
        }
        return c;
    }
}
