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
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 35, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 70, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 105, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 140, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 175, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 210, 0, 0, 1}),
		LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 245, 0, 0, 1})
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