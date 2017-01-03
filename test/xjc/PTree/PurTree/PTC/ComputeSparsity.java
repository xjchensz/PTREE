package xjc.PTree.PurTree.PTC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeDataSet;

public class ComputeSparsity {

	public static void main(String[] args) throws IOException {
		Random random = new Random(10);
		File[] files = new File[] { BuildSuperStoreData.dataDir };
		computeSparsity(files, random,
				new BufferedWriter(new FileWriter(new File(BuildSuperStoreData.dataDir, "sparsity.tex"))));
	}

	public static void computeSparsity(File[] files, Random random, BufferedWriter writer) throws IOException {
		double[] sparsity;
		StringBuilder sb = new StringBuilder();
		NumberFormat format = new DecimalFormat("00.000%");
		int maxLevel = 0;
		for (int i = 0, j; i < files.length; i++) {
			PurTreeDataSet sd = new PurTreeDataSet(files[i]);
			sb.append(files[i].getName()).append('&');
			sb.append(sd.size()).append('&').append(sd.getProductTree().numLeafNodes()).append('&');
			sparsity = sd.avgSparsity(random);
			if (maxLevel < sparsity.length) {
				maxLevel = sparsity.length;
			}

			for (j = 0; j < sparsity.length; j++) {
				sb.append('&').append(format.format(sparsity[j]));
			}
			sb.append("\\\n");
		}

		writer.append("Data&Size&No. of products&");
		for (int i = 0; i < maxLevel; i++) {
			writer.append('&').append("SP^{").append(String.valueOf(i + 1)).append("}_{\\Psi}");
		}
		writer.append('\n');
		writer.append(sb);
		writer.close();
	}

}
