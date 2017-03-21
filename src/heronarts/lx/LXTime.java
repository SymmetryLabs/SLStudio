/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
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
