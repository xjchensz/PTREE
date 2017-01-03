/**
 * 
 */
package xjc.data.PTree.PurTree.PurTreeClust;

import java.util.Random;

/**
 * @author xjchensz
 *
 */
public interface ITester {

	public String getName();

	public int[][][] test(int[] numClusters, PurTreeDataCoverTree tree, double[][] distances, long distanceTime,
			Random random);
}
