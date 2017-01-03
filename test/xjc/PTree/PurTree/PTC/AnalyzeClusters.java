package xjc.PTree.PurTree.PTC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import common.utils.FileUtils;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.distance.ILevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

public class AnalyzeClusters {

	public static void main(String[] args) throws IOException {
		double[] gamma = new double[] { 0.2 };
		int[] numClsters = new int[] { 26 };
		int k = 10;
		double[] minDistance = new double[] { 0.0, 0.0, 0.0, 0.0, 0.9 };
		double[] maxDistance = new double[] { 0.5, 0.6, 0.8, 0.9, 1 };
		double minSparsity = 0.1;

		for (int i = 3; i < 4; i++) {
			analyze(new File(BuildSuperStoreData.dataDir, "data" + (i + 1)), gamma, numClsters, k, minSparsity,
					minDistance, maxDistance);
		}
	}

	public static void analyze(File dir, double[] gamma, int[] numClsters, int k, double minSparsity,
			double[] minDistance, double[] maxDistance) throws IOException {
		for (int i = 0, j; i < gamma.length; i++) {
			PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma[i] + ".ctr"));
			for (j = 0; j < numClsters.length; j++) {
				analyze(dir, csd, gamma[i], numClsters[j], k, minSparsity, minDistance, maxDistance);
			}
		}

		System.out.println("Finished " + dir);
	}

	public static void analyze(File dir, PurTreeDataCoverTree csd, double gamma, int numClsters, int k,
			double minSparsity, double[] minDistance, double[] maxDistance) throws IOException {
		int[] centers = csd.getCoverTree().getKCentralityCenters(numClsters, k);
		int[] clustering = csd.clustering(centers);
		int[][] partition = PurTreeDataCoverTree.getPartition(clustering);

		double[] ld;
		PurTreeDistance distance = (PurTreeDistance) csd.getDistance();
		ILevelWeightedDistance ldis = distance.getLevelWeightedDistance();
		double dis;

		boolean satis;
		File results = new File(dir, "results");
		FileUtils.deleteFile(results, true);
		File clusterDir;
		BufferedWriter bw;
		StringBuilder sb = new StringBuilder();
		double[] sparsity;
		for (int i = 0, l, m; i < centers.length; i++) {
			sparsity = csd.getData().sparsity(centers[i]);
			if (sparsity[0] < minSparsity) {
				continue;
			}
			clusterDir = new File(results, "c" + (i + 1));
			clusterDir.mkdirs();
			csd.getData().saveObject(centers[i], clusterDir);

			sb.setLength(0);

			for (l = 0; l < partition[i].length; l++) {
				if (centers[i] != partition[i][l]) {
					dis = distance.distance(centers[i], partition[i][l]);
					ld = ldis.getLevelDistance();
					satis = true;
					for (m = 0; m < ld.length; m++) {
						if (ld[m] < minDistance[m] || ld[m] > maxDistance[m]) {
							satis = false;
							break;
						}
					}
					if (satis) {
						csd.getData().saveObject(partition[i][l], clusterDir);
						sb.append(csd.getData().getDataByID(centers[i])).append(',')
								.append(csd.getData().getDataByID(partition[i][l]));
						for (m = 0; m < ld.length; m++) {
							sb.append(',').append(ld[m]);
						}
						sb.append(',').append(dis).append('\n');
					}
				}
			}
			if (sb.length() > 0) {
				bw = new BufferedWriter(new FileWriter(new File(clusterDir, "distance.csv")));
				bw.append("ins1,ins2,d1,d2,d3,d4,d5,d\n");
				bw.append(sb);
				sb.setLength(0);
				bw.close();
			} else {
				FileUtils.deleteFile(clusterDir, true);
			}
		}
	}
}
