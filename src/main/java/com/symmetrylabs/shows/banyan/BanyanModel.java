package com.symmetrylabs.shows.banyan;

import com.symmetrylabs.shows.tree;


public class BanyanModel extends TreeModel {

	public Star star;

	public BanyanModel(String showName, StarConfig starConfig, TreeConfig treeConfig) {
		super(showName, treeConfig);
	}

	public static class Star extends SLModel {

		public List<Panel> panels;

		public Star() {
			//...
		}
	
		public static class Panel extends SLModel {

			public Panel() {
				//...
			}
		}

	}
}