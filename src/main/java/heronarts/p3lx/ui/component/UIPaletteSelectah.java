package heronarts.p3lx.ui.component;

import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.PaletteParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class UIPaletteSelectah extends UICompoundParameterControl implements UIFocus {

	public final static int KNOB_MARGIN = 6;
	public final static int KNOB_SIZE = 28;
	public final static int WIDTH = KNOB_SIZE + 2*KNOB_MARGIN;
	public final static int HEIGHT = KNOB_SIZE + LABEL_MARGIN + LABEL_HEIGHT;

	private final static float KNOB_INDENT = .4f;

	private LX lx;

	PaletteParameter param;

	public UIPaletteSelectah(LX lx, PaletteParameter param, float x, float y) {
		this(lx, param, x, y, WIDTH, KNOB_SIZE);
	}

	public UIPaletteSelectah(LX lx, PaletteParameter setparam, float x, float y, float w, float h) {
		super(x, y, w, h);
		this.keyEditable = true;
		enableImmediateEdit(true);
		try {
			this.param = setparam;
		}
		catch (ClassCastException e){
			System.out.println("This should only be used for Palette Parameter.  Found:" + e);
		}
		this.lx = lx;
	}


	@Override
	public void onDraw(UI ui, PGraphics pg) {
		// draw sections for each of our palettes
		int section_height = (int)((this.height-2)/ LX.NUM_PALLETS); // -2 to account for ui border?
		for (int p = 0; p < LX.NUM_PALLETS; p++){
//				for (int i = p*section_height; i < (p+1)*section_height; ++i) {
//					pg.stroke(LX.hsb(i * 360.f / (this.height-1), 100, 50));
//					pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
//				}
			for (int i = 0; i < this.width; ++i) {
				// get palette color for a given x-val, and reference to given palette.
//					pg.stroke(LX.hsb(i * 360.f / (this.width), 100, (p+1)*25));
				int p_color = lx.palettes.get(p).getColorByRange(i, this.width);

				if (p != param.getValuei()){
					p_color = Ops8.multiply(p_color, LXColor.BLACK, 0.7);
				}

				pg.stroke(p_color);
				pg.line(i, section_height*p, i, section_height*(p+1));
			}
		}
//		drawValue(ui, pg);
	}

	private double dragValue;

	@Override
	public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
		super.onMousePressed(mouseEvent, mx, my);
		this.dragValue = getNormalized();
//		if ((this.parameter != null) && (mouseEvent.getCount() > 1)) {
//			LXCompoundModulation modulation = getModulation(mouseEvent.isShiftDown());
//			if (modulation != null && (mouseEvent.isControlDown() || mouseEvent.isMetaDown())) {
//				modulation.range.reset();
//			} else {
//				this.parameter.reset();
//			}
//		}
	}


	@Override
	public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
		if (!isEnabled()) {
			return;
		}

		float delta = dy / 100.f;
		if (mouseEvent.isShiftDown()) {
			delta /= 10;
		}
		this.dragValue = LXUtils.constrain(this.dragValue - delta, 0, 1);
		setNormalized(1-this.dragValue);
	}
}
