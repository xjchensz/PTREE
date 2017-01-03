package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import xjc.PTree.PurTree.distance.ComputeDistance;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

public class TestBuildClusterTime {

	public static void buildClusters(File dir, double[] gamma, int[] k, CenterMeasureType[] cmt, Random random)
			throws IOException {
		long[][] times = new long[gamma.length][k.length];
		for (int i = 0, j, l; i < gamma.length; i++) {
			PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma[i] + ".ctr"));
			for (j = 0; j < k.length; j++) {
				for (l = 0; l < cmt.length; l++) {
					times[i][j] = buildCluster(dir, csd, k[j], cmt[l], random);
				}
			}
		}

		ComputeDistance.write(times, new File(dir, "times_clustering.csv"));

		System.out.println("Finished " + dir);
	}

	public static long buildCluster(File dir, PurTreeDataCoverTree csd, int k, CenterMeasureType cmt, Random random)
			throws IOException {
		csd.clustering(k, cmt, random);
		return csd.buildTime();
	}

}
