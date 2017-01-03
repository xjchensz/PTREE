package xjc.PTree.PurTree.PTC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

public class BuildClusterTime {

	public static void main(String[] args) throws IOException {

		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(BuildSuperStoreData.dataDir, "time_ptc.csv")));
		bw1.append("Size,time\n");
		PurTreeDataSet sd;
		Random random = new Random();
		double[] gamma = { 0.2 };
		int[] k = { 14 };

		for (int i = 0; i < SplitData.numDatasets; i++) {
			sd = new PurTreeDataSet(new File(BuildSuperStoreData.dataDir, "data" + (i + 1)));
			bw1.append(String.valueOf(sd.size()));
			bw1.append(',');

			bw1.append(String.valueOf(buildCoverTree(sd, gamma[0], k[0], CenterMeasureType.LEVEL_DENSITY, random)));
			bw1.append('\n');
			bw1.flush();
			System.out.println("Finished " + "data" + (i + 1));
		}
		bw1.close();

	}

	public static void buildCoverTree(File[] files, double[] gamma, int[] k, CenterMeasureType[] cmt, Random random,
			BufferedWriter writer) throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append("Name,Size,gamma,type,k,time\n");

		for (int i = 0, j, l, p; i < files.length; i++) {
			PurTreeDataSet sd = new PurTreeDataSet(files[i]);
			for (j = 0; j < gamma.length; j++) {
				for (l = 0; l < k.length; l++) {
					for (p = 0; p < cmt.length; p++) {
						sb.setLength(0);
						sb.append(files[i].getName()).append(',').append(sd.size()).append(',').append(gamma[j])
								.append(',').append(cmt[p].name()).append(',').append(k[l]).append(',')
								.append(buildCoverTree(sd, gamma[j], k[l], cmt[p], random)).append('\n');
						writer.append(sb);
						writer.flush();
					}
				}
			}
			System.out.println("Finished " + files[i]);
		}
	}

	public static long buildCoverTree(PurTreeDataSet sd, double gamma, int k, CenterMeasureType cmt, Random random)
			throws IOException {

		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);
		PurTreeDataCoverTree csd = new PurTreeDataCoverTree(dis, random);
		long time = csd.buildTime();
		csd.clustering(k, cmt, random);
		time += csd.buildTime();
		return time;
	}

}
