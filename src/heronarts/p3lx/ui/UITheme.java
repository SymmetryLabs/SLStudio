/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui;

import heronarts.lx.LX;
import processing.core.PApplet;
import processing.core.PFont;

public class UITheme {

    private PFont labelFont;
    private int labelColor = 0xffcccccc;

    private PFont windowTitleFont;
    private int windowTitleColor = 0xffcccccc;
    private int windowBackgroundColor = 0xff404040;
    private int windowFocusedBackgroundColor = 0xff4c4c4c;
    private int windowBorderColor = 0xff292929;

    private int paneBackgroundColor = 0xff040404;
    private int paneInsetColor = 0xff242424;

    private int focusColor = 0xff669966;
    private int primaryColor = 0xff669966;
    private int secondaryColor = 0xff666699;
    private int attentionColor = 0xff996666;

    private PFont controlFont;
    private int controlBackgroundColor = 0xff222222;
    private int controlBorderColor = 0xff494949;
    private int controlTextColor = 0xffcccccc;
    private int controlDisabledColor = 0xff666666;

    private int midiMappingColor = 0x33ff0000;
    private int modulationTargetMappingColor = 0x3300ff00;

    UITheme(PApplet applet) {
        // this.controlFont = applet.createFont("Arial", 10);
        this.controlFont = applet.loadFont("ArialUnicodeMS-10.vlw");
        LX.initTimer.log("P3LX: UI: Theme: controlFont");
        // this.setLabelFont(this.windowTitleFont = applet.createFont("Arial-Black", 9));
        this.setLabelFont(this.windowTitleFont = applet.loadFont("Arial-Black-9.vlw"));
        LX.initTimer.log("P3LX: UI: Theme: windowTitleFont");
    }

    /**
     * Gets the default item font
     *
     * @return The default item font
     */
    public PFont getControlFont() {
        return this.controlFont;
    }

    /**
     * Sets the default item font
     *
     * @param font Font to use
     * @return this
     */
    public UITheme setControlFont(PFont font) {
        this.controlFont = font;
        return this;
    }

    /**
     * Gets the default title font
     *
     * @return default title font
     */
    public PFont getWindowTitleFont() {
        return this.windowTitleFont;
    }

    /**
     * Sets the default title font
     *
     * @param font Default title font
     * @return this
     */
    public UITheme setWindowTitleFont(PFont font) {
        this.windowTitleFont = font;
        return this;
    }

    /**
     * Gets the default text color
     *
     * @return default text color
     */
    public int getWindowTitleColor() {
        return this.windowTitleColor;
    }

    /**
     * Sets the default text color
     *
     * @param color Color
     * @return this UI
     */
    public UITheme setWindowTitleColor(int color) {
        this.windowTitleColor = color;
        return this;
    }

    /**
     * Gets background color
     *
     * @return background color
     */
    public int getWindowBackgroundColor() {
        return this.windowBackgroundColor;
    }

    /**
     * Sets default background color
     *
     * @param color color
     * @return this UI
     */
    public UITheme setWindowBackgroundColor(int color) {
        this.windowBackgroundColor = color;
        return this;
    }

    /**
     * Gets background color
     *
     * @return background color
     */
    public int getWindowFocusedBackgroundColor() {
        return this.windowFocusedBackgroundColor;
    }

    /**
     * Sets default background color
     *
     * @param color color
     * @return this UI
     */
    public UITheme setWindowFocusedBackgroundColor(int color) {
        this.windowFocusedBackgroundColor = color;
        return this;
    }

    /**
     * Gets border color
     *
     * @return bordercolor
     */
    public int getWindowBorderColor() {
        return this.windowBorderColor;
    }

    /**
     * Sets default border color
     *
     * @param color color
     * @return this UI
     */
    public UITheme setWindowBorderColor(int color) {
        this.windowBorderColor = color;
        return this;
    }

    /**
     * Gets border color
     *
     * @return bordercolor
     */
    public int getPaneBackgroundColor() {
        return this.paneBackgroundColor;
    }

    /**
     * Sets default border color
     *
     * @param color color
     * @return this UI
     */
    public UITheme setPaneBackgroundColor(int color) {
        this.paneBackgroundColor = color;
        return this;
    }

    /**
     * Gets border color
     *
     * @return bordercolor
     */
    public int getPaneInsetColor() {
        return this.paneInsetColor;
    }

    /**
     * Sets default border color
     *
     * @param color color
     * @return this UI
     */
    public UITheme setPaneInsetColor(int color) {
        this.paneInsetColor = color;
        return this;
    }

    /**
     * Gets highlight color
     *
     * @return Highlight color
     */
    public int getPrimaryColor() {
        return this.primaryColor;
    }

    /**
     * Sets highlight color
     *
     * @param color Color
     * @return this
     */
    public UITheme setPrimaryColor(int color) {
        this.primaryColor = color;
        return this;
    }

    /**
     * Gets highlight color
     *
     * @return Highlight color
     */
    public int getAttentionColor() {
        return this.attentionColor;
    }

    /**
     * Sets highlight color
     *
     * @param color Color
     * @return this
     */
    public UITheme setAttentionColor(int color) {
        this.attentionColor = color;
        return this;
    }

    /**
     * Gets focus color
     *
     * @return focus color
     */
    public int getFocusColor() {
        return this.focusColor;
    }

    /**
     * Sets highlight color
     *
     * @param color Color
     * @return this
     */
    public UITheme setFocusColor(int color) {
        this.focusColor = color;
        return this;
    }

    /**
     * Get active color
     *
     * @return Selection color
     */
    public int getSecondaryColor() {
        return this.secondaryColor;
    }

    /**
     * Set active color
     *
     * @param color Color
     * @return this
     */
    public UITheme setSecondaryColor(int color) {
        this.secondaryColor = color;
        return this;
    }

    /**
     * Get disabled color
     *
     * @return Disabled color
     */
    public int getControlDisabledColor() {
        return this.controlDisabledColor;
    }

    /**
     * Set disabled color
     *
     * @param color Color
     * @return this
     */
    public UITheme setControlDisabldColor(int color) {
        this.controlDisabledColor = color;
        return this;
    }

    /**
     * Get control background color
     *
     * @return color
     */
    public int getControlBackgroundColor() {
        return controlBackgroundColor;
    }

    /**
     * Set control background color
     *
     * @param controlBackgroundColor Color to set
     * @return this
     */
    public UITheme setControlBackgroundColor(int controlBackgroundColor ) {
        this.controlBackgroundColor = controlBackgroundColor;
        return this;
    }

    /**
     * Get control border color
     *
     * @return color
     */
    public int getControlBorderColor() {
        return controlBorderColor;
    }

    /**
     * Set control border color
     *
     * @param controlBorderColor color
     * @return this
     */
    public UITheme setControlBorderColor(int controlBorderColor) {
        this.controlBorderColor = controlBorderColor;
        return this;
    }

    /**
     * Control text color
     *
     * @return the controlTextColor
     */
    public int getControlTextColor() {
        return controlTextColor;
    }

    /**
     * Set control text color
     *
     * @param controlTextColor color
     * @return this
     */
    public UITheme setControlTextColor(int controlTextColor) {
        this.controlTextColor = controlTextColor;
        return this;
    }

    /**
     * Label font
     *
     * @return font
     */
    public PFont getLabelFont() {
        return labelFont;
    }

    /**
     * Set label font
     *
     * @param labelFont font
     * @return this
     */
    public UITheme setLabelFont(PFont labelFont) {
        this.labelFont = labelFont;
        return this;
    }

    /**
     * Default text color
     *
     * @return color
     */
    public int getLabelColor() {
        return labelColor;
    }

    /**
     * Set default text color
     *
     * @param labelColor color
     * @return this
     */
    public UITheme setLabelColor(int labelColor) {
        this.labelColor = labelColor;
        return this;
    }

    public int getMidiMappingColor() {
        return this.midiMappingColor;
    }

    public UITheme setMidiMappingColor(int midiMappingColor) {
        this.midiMappingColor = midiMappingColor;
        return this;
    }

    public int getModulationTargetMappingColor() {
        return this.modulationTargetMappingColor;
    }

    public UITheme setModulationTargetMappingColor(int modulationTargetMappingColor) {
        this.modulationTargetMappingColor = modulationTargetMappingColor;
        return this;
    }

}
