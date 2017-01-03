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
package xjc.covertree;

/**
 * @author xiaojun chen
 *
 */
public class CoverTreeFactory {

	public static CoverTreeFactory ins;

	private CoverTreeFactory() {
	}

	public static CoverTreeFactory getDefault() {
		if (ins == null) {
			ins = new CoverTreeFactory();
		}
		return ins;
	}

	public CoverTree create(IDistanceHolder distanceHolder) {
		return create(new NodeCreator(), distanceHolder,
				CoverTree.DEFAULT_ALPHA, 500, -500);
	}

	public CoverTree create(IDistanceHolder distanceHolder, double base) {
		return create(new NodeCreator(), distanceHolder, base, 500, -500);
	}

	public CoverTree create(INodeCreator creator,
			IDistanceHolder distanceHolder, double base, int maxNumLevels,
			int minNumLevels) {
		return new CoverTree(creator, distanceHolder, base, maxNumLevels,
				minNumLevels);
	}

}
