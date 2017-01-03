/**
 * 
 */
package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import common.utils.collection.OrderedIntMap;
import common.utils.collection.STATUS;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.PTree.PurTree.distance.ComputeDistance;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

/**
 * @author xiaojun chen
 *
 */
public class DensityEstimation {

	public static void main(String[] args) throws IOException {
		int level = -2;
		for (int i = 3; i < 4; i++) {
			estimateDensity(new File(BuildSuperStoreData.dataDir, "data" + (i + 1)), ComputeDistance.gamma, level);
		}
	}

	public static void estimateDensity(File dir, double[] gamma, int level) throws IOException {
		OrderedIntMap map = new OrderedIntMap(STATUS.REPEATABLE);
		double[][] densities = new double[gamma.length][];
		for (int i = 0; i < gamma.length; i++) {
			PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma[i] + ".ctr"));
			densities[i] = estimateDensity(dir, csd, gamma[i], level, map);
			map.clear();
		}

		ComputeDistance.write(null, densities, new File(dir, "densities.csv"));

		System.out.println("Finished " + dir);
	}

	public static double[] estimateDensity(File dir, PurTreeDataCoverTree csd, double gamma, int level,
			OrderedIntMap map) throws IOException {
		csd.getCoverTree().levelDensity(level, map);
		double[] density = new double[map.size()];
		int size = csd.getData().size();
		for (int i = density.length - 1; i >= 0; i--) {
			density[i] = (double) map.getValueAt(i) / size;
		}
		Arrays.sort(density);

		return density;
	}
}
