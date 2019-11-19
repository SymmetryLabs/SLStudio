package com.symmetrylabs.shows.treeV2;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.transform.LXMatrix;


public class BranchConfig {

	public static int DEFAULT_NUMBER_TWIGS = 8;

	private String controllerId;
	private final String modelId;
	private final LXMatrix matrix;
	private final int[] twigOrdering;

	private static final LXMatrix[] twigMatrices = new LXMatrix[] {
		LXMatrix.createFromColumnMajor(new double[] {0.312858952866808, 0.932739457068392, 0.179210437304431, 0, 0.177496233208369, -0.242774547179387, 0.95370624745709, 0, 0.933067180222263, -0.266566360348797, -0.241511930807715, 0, -0.345872303242758 , 0.278090985607215 , 0.0841452941451376, 1}),
		LXMatrix.createFromColumnMajor(new double[] {0.645377229946167, 0.759070381293509, -0.0854423039830945, 0, -0.0962678299918081, 0.191788359198585, 0.976703501675193, 0, 0.757773538714358, -0.622116855195904, 0.196849898413684, 0, -0.360004119522088 , 0.502570634077144 , 0.0302521561964262, 1}),
		LXMatrix.createFromColumnMajor(new double[] {0.655851851030912, 0.744181414217205, 0.126697956704153, 0, 0.276346760090695, -0.392871752261492, 0.87708851005036, 0, 0.702489016061249, -0.54022755298917, -0.463317788677094, 0, -0.262339176572296 , 0.730791328937601 , 0.239818621465373, 1}),
		LXMatrix.createFromColumnMajor(new double[] {0.85612003986588, 0.514605437044072, 0.047325696030006, 0, -0.105109032342927, 0.0837331431593486, 0.990929287112148, 0, 0.505974869593321, -0.853328778901389, 0.125775301385788, 0,-0.149902407924507 , 0.890031590319137 , 0.178716226506837, 1}),
		LXMatrix.createFromColumnMajor(new double[] {0.988217367698891, -0.121280145594757, 0.0933678770391082, 0, -0.120160538236217, -0.23689863289833, 0.964074936289542, 0,-0.0948044262102676, -0.963934730163519, -0.248680430986287, 0,0.0367582517810132 , 0.958816088030177 , 0.247291840224837, 1}),
		LXMatrix.createFromColumnMajor(new double[] {0.846386588017118, -0.521687874727954, 0.107105111860133, 0,-0.0530134578658471, 0.11758105358356, 0.991647250348271, 0,-0.529923878410709, -0.844994945073644, 0.0718625485920336, 0, 0.194222169156843 , 0.766856942704011 , 0.115024693377668, 1}),
		LXMatrix.createFromColumnMajor(new double[] {-0.606044393414896, 0.784201272814428, 0.133186173931835, 0,0.276968809938327, 0.365006116996857, -0.888852525943547, 0,-0.745653050373776, -0.501795673826616, -0.43840920405162, 0, 0.272443830205276 , 0.545982212035309 , 0.181912225750186, 1}),
		LXMatrix.createFromColumnMajor(new double[] {-0.283494778033851, 0.958959651092142, 0.00520561281465909, 0, -0.116587339317409, -0.0290773498620167, -0.992754702852567, 0, -0.951860338042523, -0.282047682674828, 0.120045831078529, 0, 0.32974258054308 , 0.333610868657911 , 0.0315428663599697, 1})
	};

	public BranchConfig(LXMatrix matrix) {
		this("branch", "0.0.0.0", matrix);
	}

	public BranchConfig(String modelId, String controllerId, LXMatrix matrix) {
		this(modelId, controllerId, matrix, new int[DEFAULT_NUMBER_TWIGS]);
	}

	public BranchConfig(String modelId, String controllerId, LXMatrix matrix, int[] twigOrdering) {
		this.modelId = modelId;
		this.controllerId = controllerId;
		this.matrix = matrix;
		this.twigOrdering = twigOrdering;
	}

	public String getControllerId() {
		return controllerId;
	}

	public String setControllerId(String id) {
		return this.controllerId = controllerId;
	}

	public String getModelId() {
		return modelId;
	}

	public LXMatrix getMatrix() {
		return matrix;
	}

	public int[] getTwigOrdering() {
		return twigOrdering;
	}

	public int getIndexOfTwig(int i) {
		return twigOrdering[i];
	}

	public static LXMatrix getTwigMatrix(int i) {
		return twigMatrices[i];
	}
}