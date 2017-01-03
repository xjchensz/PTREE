package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import common.utils.RandomUtils;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeDataSet;

public class SplitData {
	public static int numDatasets = 10;

	public static void main(String[] args) throws IOException {
		split(BuildSuperStoreData.dataDir, numDatasets, 2, new Random(10));
	}

	public static void split(File dir, int[] sampleTrees, Random random) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		split(dir, sd, sampleTrees, random);
	}

	public static void split(File dir, int numDatasets, double step, Random random) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		int size = sd.size();

		int[] sampleTrees = new int[numDatasets];
		int sampleSize = size;
		for (int i = 1; i < numDatasets; i++) {
			sampleTrees[i] = (int) (sampleSize / step);
		}

		split(dir, sd, sampleTrees, random);
	}

	public static void split(File dir, PurTreeDataSet sd, int[] sampleTrees, Random random) throws IOException {
		int size = sd.size();

		int[] selected = null;

		PurTreeDataSet sd1;
		for (int i = 1; i < sampleTrees.length; i++) {
			if (sampleTrees[i] == size) {
				FileUtils.copyDirectory(dir, new File(dir.getParentFile(), "data" + (i + 1)), true);
			} else {
				selected = RandomUtils.nonRepeatSample(0, size, 1, sampleTrees[i], random);
				sd1 = sd.sample(selected);
				sd1.save(new File(dir.getParentFile(), "data" + (i + 1)));
				sd1.destroy();
				System.gc();
			}
			System.out.println("Finished data" + i + ".");
		}
	}
}
