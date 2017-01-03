package xjc.PTree.PurTree.PTC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import common.utils.StringUtils;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

public class BuildCluster {

	public static void main(String[] args) throws IOException {
		double[] gamma = new double[] { 0.2 };
		int[] k = new int[] { 14 };

		CenterMeasureType[] cmt = CenterMeasureType.values();
		Random random = new Random();

		for (int i = 3; i < 4; i++) {
			buildClusters(new File(BuildSuperStoreData.dataDir, "data" + (i + 1)), gamma, k, cmt, random);
		}
	}

	public static void buildClusters(File dir, double[] gamma, int[] k, CenterMeasureType[] cmt, Random random)
			throws IOException {
		for (int i = 0, j, l; i < gamma.length; i++) {
			PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma[i] + ".ctr"));
			double[][] distances = read(new File(dir, "distance_" + gamma[i] + ".csv"));
			for (j = 0; j < k.length; j++) {
				for (l = 0; l < cmt.length; l++) {
					buildCluster(dir, csd, distances, gamma[i], k[j], cmt[l], random);
				}
			}
		}

		System.out.println("Finished " + dir);
	}

	public static void buildCluster(File dir, PurTreeDataCoverTree csd, double[][] distances, double gamma, int k,
			CenterMeasureType cmt, Random random) throws IOException {
		int[][] partition = csd.partition(k, cmt,
				new File(dir, "clustering_" + cmt.name() + "_" + gamma + "_" + k + ".csv"), random);

		// draw
		DrawCluster.draw(partition, distances,
				new File(dir, "clustering_" + cmt.name() + "_" + gamma + "_" + k + ".jpg"));
	}

	public static double[][] read(File distanceFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(distanceFile));
		try {
			String line = br.readLine();
			String[] array = StringUtils.split2Array(line, ',');
			int size = array.length;
			double[][] results = new double[size][size];
			for (int i = 0; i < size; i++) {
				results[0][i] = Double.parseDouble(array[i]);
			}
			for (int i = 1, j; i < size; i++) {
				line = br.readLine();
				StringUtils.split2ArrayDirect(line, ',', array);
				for (j = 0; j < size; j++) {
					results[i][j] = Double.parseDouble(array[j]);
				}
			}
			return results;
		} finally {
			br.close();
		}
	}

}
