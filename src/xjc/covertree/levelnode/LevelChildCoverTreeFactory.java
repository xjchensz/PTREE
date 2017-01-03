/**
 * This class provides a Java version of the cover tree nearest neighbor algorithm.
 * It is based on Thomas Kollar's version of "Cover Trees for Nearest Neighbor" by 
 * Langford, Kakade, Beygelzimer (2007). 
 * 
 * Date of creation: 2013-02-08
 * Copyright (c) 2015, Xiaojun Chen
 * 
 * The software is provided 'as-is', without any express or implied
 * warranty. In no event will the author be held liable for any damages
 * arising from the use of this software. Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it freely.
 * 
 * @author Xiaojun Chen
 *
 */
package xjc.covertree.levelnode;

import xjc.covertree.levelnode.LevelChildCoverTree;
import xjc.covertree.IDistanceHolder;
import xjc.covertree.levelnode.ILevelNodeCreator;
import xjc.covertree.levelnode.LevelNodeCreator;

/**
 * @author xiaojun chen
 *
 */
public class LevelChildCoverTreeFactory {

	public static LevelChildCoverTreeFactory ins;

	private LevelChildCoverTreeFactory() {
	}

	public static LevelChildCoverTreeFactory getDefault() {
		if (ins == null) {
			ins = new LevelChildCoverTreeFactory();
		}
		return ins;
	}

	public LevelChildCoverTree create(IDistanceHolder distanceHolder) {
		return create(new LevelNodeCreator(), distanceHolder,
				LevelChildCoverTree.DEFAULT_ALPHA, 500, -500);
	}

	public LevelChildCoverTree create(IDistanceHolder distanceHolder, double base) {
		return create(new LevelNodeCreator(), distanceHolder, base, 500, -500);
	}

	public LevelChildCoverTree create(ILevelNodeCreator creator,
			IDistanceHolder distanceHolder, double base, int maxNumLevels,
			int minNumLevels) {
		return new LevelChildCoverTree(creator, distanceHolder, base, maxNumLevels,
				minNumLevels);
	}

}
