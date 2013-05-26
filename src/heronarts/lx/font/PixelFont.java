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

package heronarts.lx.font;

import heronarts.lx.HeronLX;

import processing.core.PConstants;
import processing.core.PImage;

public class PixelFont {

    final private PImage alphabet;

    final private int[] offsets = {
     // A  B  C   D   E   F   G   H   I   J   K   L   M   N   O   P   Q   R   S   T   U   V    W    X    Y    Z    .    :    -    ,    '         <END>
        0, 5, 10, 15, 20, 25, 30, 35, 40, 42, 46, 51, 56, 62, 68, 73, 78, 83, 88, 93, 99, 104, 110, 116, 122, 128, 134, 136, 138, 142, 144, 146, 149
    };

    public PixelFont(HeronLX lx) {
        this.alphabet = lx.applet.loadImage("PixelFont.png");
        this.alphabet.loadPixels();
    }

    private boolean isValidCharacter(char c) {
        return (this.validCharacter(c) > 0);
    }

    private char validCharacter(char c) {
        if ((c >= 'a') && (c <= 'z')) {
            return (char) ('A' + (c - 'a'));
        } else if ((c >= 'A') && (c <= 'Z')) {
            return c;
        }
        switch (c) {
        case '.':
        case ',':
        case '-':
        case ':':
        case '\'':
        case ' ':
            return c;
        }
        return 0;
    }

    private int characterIndex(char c) {
        char valid = this.validCharacter(c);
        if (valid <= 0) {
            return -1;
        }
        switch (valid) {
        case '.':
            return 26;
        case ':':
            return 27;
        case '-':
            return 28;
        case ',':
            return 29;
        case '\'':
            return 30;
        case ' ':
            return 31;
        default:
            return valid - 'A';
        }
    }

    private int characterOffset(char c) {
        int index = this.characterIndex(c);
        if (index >= 0) {
            return this.offsets[index];
        }
        return -1;
    }

    private int characterWidth(char c) {
        int index = this.characterIndex(c);
        if (index < 0) {
            return -1;
        }
        return this.offsets[index+1] - this.offsets[index] - 1;
    }

    public PImage getImage(String s) {
        int width=0,height=5;
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (this.isValidCharacter(chars[i])) {
                if (i > 0) {
                    ++width;
                }
                width += this.characterWidth(chars[i]);
            }
        }

        PImage image = new PImage(width, height, PConstants.RGB);
        image.loadPixels();
        
        int xPos = 0;
        for (int i = 0; i < chars.length; ++i) {
            if (this.isValidCharacter(chars[i])) {
                if (i > 0) {
                    for (int y = 0; y < image.height; ++y) {
                        image.pixels[xPos + y*image.width] = 0;
                    }
                    ++xPos;
                }
                int offset = this.characterOffset(chars[i]);
                int characterWidth = this.characterWidth(chars[i]);
                for (int j = 0; j < characterWidth; ++j) {
                    for (int y = 0; y < image.height; ++y) {
                        image.pixels[xPos + j + y*image.width] = this.alphabet.pixels[offset + j + y*this.alphabet.width];
                    }
                }
                xPos += characterWidth;
            }
        }
        
        image.updatePixels();
        return image;
    }
}
