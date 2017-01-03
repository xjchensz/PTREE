/**
 * 
 */
package xjc.PTree.PurTree.distance;

import java.io.File;
import java.io.IOException;

import xjc.data.WriteUtils;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

/**
 * @author xiaojun chen
 *
 */
public class ComputePTDistance {

	public static double[] gamma = new double[] { 0, 0.2, 0.8, 1, 2, 1000 };

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {

	}

	public static void computeDistance(File dir, double[] gamma) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		for (int i = 0; i < gamma.length; i++) {
			PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma[i]));
			dis.setData(sd);
			WriteUtils.write(dis.distances(), new File(dir, "distance_" + gamma[i] + ".csv"));
		}
		System.out.println("Finished computing " + dir);
	}

}
