package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

public class TestScalability {

	public static void main(String[] args) throws IOException {
		double[] gamma = new double[] { 0.2 };
		int[] k = new int[] { 26 };
		CenterMeasureType[] cmt = new CenterMeasureType[] { CenterMeasureType.LEVEL_DENSITY,
				CenterMeasureType.CENTRALITY };
		Random random = new Random();

		FileWriter writer = new FileWriter(new File(BuildSuperStoreData.dataDir, "times_clustering.csv"));
		writer.append("Size,time\n");

		buildClusters(BuildSuperStoreData.dataDir, gamma, k, cmt, writer, random);
		writer.flush();

		writer.close();
	}

	public static void buildClusters(File dir, double[] gamma, int[] k, CenterMeasureType[] cmt, Writer writer,
			Random random) throws IOException {
		long time = 0;
		int size = 0;
		for (int i = 0, j, l; i < gamma.length; i++) {
			PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma[i] + ".ctr"));
			size = csd.getData().size();
			for (j = 0; j < k.length; j++) {
				for (l = 0; l < cmt.length; l++) {
					csd.clustering(k[j], cmt[l], random);
					time += csd.buildTime();
				}
			}
		}
		writer.append(String.valueOf(size)).append(',')
				.append(String.valueOf(time / (gamma.length * k.length * cmt.length)));
		System.out.println("Finished " + dir);
	}
}
