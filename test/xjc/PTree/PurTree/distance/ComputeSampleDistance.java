/**
 * 
 */
package xjc.PTree.PurTree.distance;

import java.io.File;
import java.io.IOException;

import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

/**
 * @author xiaojun chen
 *
 */
public class ComputeSampleDistance {

	public static double[] gamma = new double[] { 0, 0.2, 0.8, 1, 2, 1000 };

	public void test() throws IOException {

	}

	public static void computeDistance(File dir, double[] gamma) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);

		PurTreeDistance[] dis = new PurTreeDistance[gamma.length];
		for (int i = 0; i < gamma.length; i++) {
			dis[i] = new PurTreeDistance(new LevelWeightedDistance(gamma[i]));
			dis[i].setData(sd);
		}

		int[][] pairs = new int[][] { new int[] { 1082, 1082 }, new int[] { 1256, 384 }, new int[] { 384, 691 },
				new int[] { 631, 968 }, new int[] { 709, 751 } };
		for (int i = 0, j; i < pairs.length; i++) {
			for (j = 0; j < dis.length; j++) {
				System.out.println(dis[j].distance(pairs[i][0], pairs[i][1]));
			}
		}
	}
}
