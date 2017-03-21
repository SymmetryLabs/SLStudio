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

package heronarts.lx.transition;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class SlideTransition extends LXTransition {
    public enum Direction {
        RIGHT, LEFT, DOWN, UP
    };

    private final Direction direction;

    public SlideTransition(LX lx) {
        this(lx, Direction.RIGHT);
    }

    public SlideTransition(LX lx, Direction direction) {
        super(lx);
        this.direction = direction;
    }

    private int getColor(int[] c1, int[] c2, int row, int col) {
        int[] source = c1;
        if (col >= this.lx.width) {
            col -= this.lx.width;
            source = c2;
        } else if (col < 0) {
            col += this.lx.width;
            source = c2;
        } else if (row >= this.lx.height) {
            row -= this.lx.height;
            source = c2;
        } else if (row < 0) {
            row += this.lx.height;
            source = c2;
        }
        return source[col + this.lx.width * row];
    }

    @Override
    protected void computeBlend(int[] c1, int[] c2, double progress) {
        double blendPosition = 0;
        switch (this.direction) {
        case LEFT:
            blendPosition = this.lx.width * progress;
            break;
        case RIGHT:
            blendPosition = -this.lx.width * progress;
            break;
        case DOWN:
            blendPosition = -this.lx.height * progress;
            break;
        case UP:
            blendPosition = this.lx.height * progress;
            break;
        }
        for (int i = 0; i < this.colors.length; ++i) {
            int row = this.lx.row(i);
            int col = this.lx.column(i);
            int p1, p2;
            switch (this.direction) {
            case LEFT:
            case RIGHT:
                p1 = (int) Math.floor(col + blendPosition);
                p2 = (int) Math.ceil(col + blendPosition);
                this.colors[i] = LXColor.lerp(
                    this.getColor(c1, c2, row, p1),
                    this.getColor(c1, c2, row, p2),
                    col + blendPosition - p1
                );
                break;
            case DOWN:
            case UP:
                p1 = (int) Math.floor(row + blendPosition);
                p2 = (int) Math.ceil(row + blendPosition);
                this.colors[i] = LXColor.lerp(
                    this.getColor(c1, c2, p1, col),
                    this.getColor(c1, c2, p2, col),
                    row + blendPosition - p1
                );
                break;
            }
        }
    }
}
