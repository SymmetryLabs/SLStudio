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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

public class UIFileNameBox extends UITextBox {
    public UIFileNameBox() {
        this(0, 0, 0, 0);
    }

    public UIFileNameBox(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    private static final String VALID_CHARACTERS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.-";

    public static boolean isValidTextCharacter(char keyChar) {
        return VALID_CHARACTERS.indexOf(keyChar) >= 0;
    }

    @Override
    protected boolean isValidCharacter(char keyChar) {
        return isValidTextCharacter(keyChar);
    }
}
