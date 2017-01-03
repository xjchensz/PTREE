package xjc.PTree.PurTree.PTC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.PTree.PurTree.distance.ComputeDistance;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

public class BuildCoverTreeDataset {

	public static void main(String[] args) throws IOException {
		double[] gamma = ComputeDistance.gamma;
		Random random = new Random();
		buildCoverTrees(BuildSuperStoreData.dataDir, gamma, random);
	}

	public static void buildCoverTrees(File dir, double[] gamma, Random random) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "time_ct.csv")));

		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf(gamma[0]));
		for (int i = 1; i < gamma.length; i++) {
			sb.append(',').append(String.valueOf(gamma[i]));
		}
		sb.append('\n');

		long[] times = new long[gamma.length];
		double[] alpha = new double[gamma.length];

		buildCoverTree(dir, gamma, times, alpha, random);
		bw.append(sb);
		bw.append(String.valueOf(times[0]));
		for (int j = 1; j < times.length; j++) {
			bw.append(',').append(String.valueOf(times[j]));
		}
		bw.append('\n');
		bw.close();

		BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir, "alpha_ct.csv")));
		abw.append(sb);
		abw.append(String.valueOf(alpha[0]));
		for (int j = 1; j < alpha.length; j++) {
			abw.append(',').append(String.valueOf(alpha[j]));
		}
		abw.append('\n');
		abw.close();
	}

	public static void buildCoverTree(File dir, double[] gamma, long[] times, double[] alpha, Random random)
			throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		for (int i = 0; i < gamma.length; i++) {
			buildCoverTree(dir, sd, gamma[i], times, alpha, i, random);
		}
		System.out.println("Finished " + dir);
		sd.close();
	}

	public static void buildCoverTree(File dir, PurTreeDataSet sd, double gamma, long[] times, double[] alpha,
			int index, Random random) throws IOException {
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);
		PurTreeDataCoverTree csd = new PurTreeDataCoverTree(dis, random);

		csd.save(new File(dir, "data_" + gamma + ".ctr"));
		times[index] = csd.buildTime();
		alpha[index] = csd.getCoverTree().getBase();
	}

}
