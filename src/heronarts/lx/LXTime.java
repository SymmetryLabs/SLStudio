/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx;

import java.util.Calendar;

/**
 * Utilities for working with time
 */
public class LXTime {

    public static int day() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public static int hour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static int minute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

}
