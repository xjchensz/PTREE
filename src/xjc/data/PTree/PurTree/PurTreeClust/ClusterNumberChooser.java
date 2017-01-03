/**
 * 
 */
package xjc.data.PTree.PurTree.PurTreeClust;

import java.util.Random;

import xjc.clustering.validation.NormalizedLogW;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.distance.IDataDistance;

/**
 * @author xiaojun chen
 *
 */
public class ClusterNumberChooser {

	private PurTreeDataCoverTree m_Data;
	private int m_NumCopies;

	private double[][] logEWk;
	private double[][] logWk;
	private double[][] sk;
	private double[][] gap;

	public ClusterNumberChooser(PurTreeDataCoverTree data, int numCopies) {
		m_Data = data;
		m_NumCopies = numCopies;
	}

	public int bestPartition(final int[] numClusters, final ITester[] tester, Random random) {
		IDataDistance<PurTreeDataSet> distance = m_Data.getDistance().clone();
		double[] nnodes = m_Data.getData().avgNodes(random);

		int[] nn = new int[nnodes.length];
		for (int i = 0; i < nn.length; i++) {
			nn[i] = (int) nnodes[i];
		}

		PurTreeDataSet sd;
		double[][][] logNullWk = new double[m_NumCopies][numClusters.length][tester.length];
		int[][][] results;
		for (int i = 0, l, j; i < m_NumCopies; i++) {
			sd = m_Data.getData().sampleInTree(nn, random);
			distance.setData(sd);
			final PurTreeDataCoverTree pct = new PurTreeDataCoverTree(distance, random);
			long start = System.currentTimeMillis();
			final double[][] dis = pct.getDistance().distances();
			long distanceTime = System.currentTimeMillis() - start;
			for (l = 0; l < tester.length; l++) {
				results = tester[l].test(numClusters, pct, dis, distanceTime, random);
				for (j = 0; j < numClusters.length; j++) {
					logNullWk[i][j][l] = NormalizedLogW.logwk(results[j], dis);
				}
			}
			pct.destroy();
		}
		System.out.println("Finished clustering samples!");

		logEWk = new double[numClusters.length][tester.length];
		logWk = new double[numClusters.length][tester.length];
		sk = new double[numClusters.length][tester.length];
		gap = new double[numClusters.length][tester.length];

		long start = System.currentTimeMillis();
		final double[][] distances = m_Data.getDistance().distances();
		long distanceTime = System.currentTimeMillis() - start;

		final int[] minCluster = new int[] { -1 };
		final double[] minGap = new double[] { Double.MAX_VALUE };

		double tmp;

		for (int l = 0, i, j; l < tester.length; l++) {
			results = tester[l].test(numClusters, m_Data, distances, distanceTime, random);
			for (j = 0; j < numClusters.length; j++) {
				logWk[j][l] = NormalizedLogW.logwk(results[j], distances);
				tmp = 0;
				for (i = 0; i < m_NumCopies; i++) {
					tmp += logNullWk[i][j][l];
				}
				logEWk[j][l] = tmp /= (double) m_NumCopies;
				sk[j][l] = 0;
				for (i = 0; i < m_NumCopies; i++) {
					sk[j][l] += Math.pow(logNullWk[i][j][l] - tmp, 2);
				}

				sk[j][l] = Math.sqrt(sk[j][l] / (double) m_NumCopies) * Math.sqrt(1 + 1.0 / (double) m_NumCopies);
				gap[j][l] = tmp - logWk[j][l];
				if (gap[j][l] < minGap[0]) {
					minGap[0] = gap[j][l];
					minCluster[0] = numClusters[j];
				}
			}
		}

		System.out.println("Finished clustering original data!");

		int[] bestCluster = new int[tester.length];
		double[] ggp = new double[tester.length];
		for (int j = 0, l; j < numClusters.length - 1; j++) {
			for (l = 0; l < tester.length; l++) {
				if (gap[j][l] >= gap[j + 1][l] - sk[j + 1][l]) {
					bestCluster[l] = numClusters[j];
					ggp[l] = gap[j][l];
					break;
				}
			}
		}

		// select max gap in bestCluster
		int bestNum = -1;
		double maxGp = -Double.MAX_VALUE;
		for (int l = 0; l < bestCluster.length; l++) {
			if (ggp[l] > maxGp) {
				maxGp = ggp[l];
				bestNum = bestCluster[l];
			}
		}
		return bestNum;
	}

	public double[][] logEWk() {
		return logEWk;
	}

	public double[][] logWk() {
		return logWk;
	}

	public double[][] getGap() {
		return gap;
	}

	public double[][] sk() {
		return sk;
	}

	public static double compactIndex(int[][] partition, double[][] distances) {
		double asd = 0, tmp;
		for (int i = 0, j, k; i < partition.length; i++) {
			tmp = 0;
			for (j = 0; j < partition[i].length; j++) {
				for (k = j; k < partition[i].length; k++) {
					tmp += distances[j][k];
				}
			}
			if (partition[i].length > 1) {
				tmp = tmp * 2 / Math.pow(partition[i].length, 2);
				asd += tmp * (double) partition[i].length / distances.length;
			}
		}
		return asd;
	}
}
