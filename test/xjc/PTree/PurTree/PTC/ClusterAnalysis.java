package xjc.PTree.PurTree.PTC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

public class ClusterAnalysis {

	public static void main(String[] args) throws IOException {

	}

	public static void analyzeClsuter(File dir, int k, double gamma, CenterMeasureType cmt, File resultsDir)
			throws IOException {
		PurTreeDataCoverTree pct = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma + ".ctr"));
		Random random = new Random();
		ComputeClusterCentersSparsity(pct, k, cmt, resultsDir);
		ComputeClusterSparsity(pct, k, cmt, random, resultsDir);
		ComputeClusterTreeSparsity(pct, k, cmt, random, resultsDir);
		pct.destroy();

	}

	public static void ComputeClusterCentersSparsity(PurTreeDataCoverTree pct, int k, CenterMeasureType cmt,
			File resultsDir) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(resultsDir, "sparsity_Centers_" + k + ".csv")));
		StringBuilder sb = new StringBuilder();
		int levels = pct.getData().numLevels();
		sb.append("Centers");
		for (int i = 0; i < levels; i++) {
			sb.append(',').append("L").append(i + 1);
		}
		sb.append('\n');

		NumberFormat format = new DecimalFormat("0.00000");
		double[][] sparsity = pct.sparsityOfCenters(k, cmt);
		for (int i = 0, j; i < sparsity.length; i++) {
			sb.append(i + 1);
			for (j = 0; j < sparsity[i].length; j++) {
				sb.append(',').append(format.format(sparsity[i][j]));
			}
			sb.append('\n');
		}
		bw.append(sb);
		bw.close();
	}

	public static void ComputeClusterSparsity(PurTreeDataCoverTree pct, int k, CenterMeasureType cmt, Random random,
			File resultsDir) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(resultsDir, "sparsity_Clusters_" + k + ".csv")));
		StringBuilder sb = new StringBuilder();
		int levels = pct.getData().numLevels();
		sb.append("Clusters");
		for (int i = 0; i < levels; i++) {
			sb.append(',').append("L").append(i + 1);
		}
		sb.append('\n');

		NumberFormat format = new DecimalFormat("0.00000");
		double[][] sparsity = pct.avgSparsityOfClusters(k, cmt, random);
		for (int i = 0, j; i < sparsity.length; i++) {
			sb.append(i + 1);
			for (j = 0; j < sparsity[i].length; j++) {
				sb.append(',').append(format.format(sparsity[i][j]));
			}
			sb.append('\n');
		}
		bw.append(sb);
		bw.close();
	}

	public static void ComputeClusterTreeSparsity(PurTreeDataCoverTree pct, int k, CenterMeasureType cmt, Random random,
			File resultsDir) throws IOException {
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(new File(resultsDir, "sparsity_ClusterTrees_" + k + ".csv")));
		StringBuilder sb = new StringBuilder();
		int levels = pct.getData().numLevels();
		sb.append("CTree");
		for (int i = 0; i < levels; i++) {
			sb.append(',').append("L").append(i + 1);
		}
		sb.append('\n');

		NumberFormat format = new DecimalFormat("0.00000");
		double[][] sparsity = pct.sparsityOfClusterTree(k, cmt, random);
		for (int i = 0, j; i < sparsity.length; i++) {
			sb.append(i + 1);
			for (j = 0; j < sparsity[i].length; j++) {
				sb.append(',').append(format.format(sparsity[i][j]));
			}
			sb.append('\n');
		}
		bw.append(sb);
		bw.close();
	}
}
