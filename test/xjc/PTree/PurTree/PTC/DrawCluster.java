/**
 * 
 */
package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.IOException;

import common.utils.collection.OrderedIntMap;
import common.utils.collection.STATUS;
import xjc.PTree.PurTree.distance.DistanceMap;

/**
 * @author Xiaojun Chen
 * @date 2016/6/27
 */
public class DrawCluster {

	public static void draw(int[][] partition, double[][] distances, File file) throws IOException {
		int[] rearrange = new int[distances.length];
		int ptr = 0;

		OrderedIntMap map = new OrderedIntMap(STATUS.REPEATABLE);
		for (int i = 0; i < partition.length; i++) {
			map.put(partition[i].length, i);
		}

		int index;
		for (int i = 0, j; i < partition.length; i++) {
			index = map.getValueAt(map.size() - i - 1);
			for (j = 0; j < partition[index].length; j++) {
				rearrange[ptr++] = partition[index][j];
			}
		}
		double[][] clusterResults = new double[distances.length][distances.length];
		for (int i = 0, j; i < clusterResults.length; i++) {
			for (j = 0; j < clusterResults[i].length; j++) {
				clusterResults[i][j] = distances[rearrange[i]][rearrange[j]];
			}
		}
		// make graph
		DistanceMap.draw(clusterResults, file);
	}
}
