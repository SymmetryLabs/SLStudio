package com.symmetrylabs.shows.treeV2;

import heronarts.lx.transform.LXMatrix;


public class TwigConfig {
	
	private static final LXMatrix[] leafMatrices = new LXMatrix[] {
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 5, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 10, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 15, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 20, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})
	};

	public static LXMatrix getLeafMatrix(int i) {
		return leafMatrices[i];
	}
}