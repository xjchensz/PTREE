/**
 * 
 */
package xjc.clustering.validation;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import xjc.data.PTree.PurTree.PurTreeClust.AbstractDataset;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

/**
 * @author xiaojun chen
 *
 */
public class Moduality {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
	}

	public static double[][] computeModuality(File file, int[] k, CenterMeasureType[] cmt, Random random)
			throws IOException {
		PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(file);

		double[][] moduality = computeModuality(csd, k, cmt, random);
		csd.destroy();
		return moduality;
	}

	public static double[][] normalize(double[][] distances, double[] d) {

		double[][] nd = new double[distances.length][distances.length];
		double m = 0;

		for (int j = 0, l; j < distances.length; j++) {
			for (l = 0; l < distances.length; l++) {
				d[j] += distances[j][l];
			}
			m += d[j];
		}

		for (int j = 0, l; j < distances.length; j++) {
			for (l = 0; l < distances.length; l++) {
				nd[j][l] = distances[j][l] / m;
			}
			d[j] /= m;
		}

		return nd;
	}

	public static double[][] computeModuality(PurTreeDataCoverTree csd, int[] k, CenterMeasureType[] cmt, Random random)
			throws IOException {

		double[][] distances = csd.getDistance().distances();

		double[][] moduality = new double[k.length][cmt.length];

		for (int j = 0, l; j < k.length; j++) {
			if (distances.length > k[j] / 2) {
				for (l = 0; l < cmt.length; l++) {
					moduality[j][l] = computeModuality(csd, k[j], cmt[l], distances, random);
				}
			}
		}
		return moduality;
	}

	public static double computeModuality(PurTreeDataCoverTree csd, int k, CenterMeasureType cmt, double[][] distances,
			Random random) throws IOException {

		int[][] partition = AbstractDataset.getPartition(csd.clustering(k, cmt, random));
		return Q(partition, distances);
	}

	public static double Q(int[][] partition, double[][] distances) {
		double moduality = 0;

		double[] s = new double[distances.length];
		double m = 0;

		for (int j = 0, l; j < distances.length; j++) {
			for (l = 0; l < distances.length; l++) {
				s[j] += 1 - distances[j][l];
			}
			m += s[j];
		}

		for (int i = 0, j, l; i < partition.length; i++) {
			for (j = 0; j < partition[i].length; j++) {
				for (l = 0; l < partition[i].length; l++) {
					if (partition[i][j] != partition[i][l]) {
						moduality += 1 - distances[partition[i][j]][partition[i][l]]
								- (s[partition[i][j]] / m) * s[partition[i][l]];
					}
				}
			}
		}
		moduality /= m;

		return moduality;
	}
}
