/**
 * 
 */
package xjc.PTree.PurTree.distance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

/**
 * @author xiaojun chen
 *
 */
public class ComputeLevelDistances {

	public static void main(String[] args) throws IOException {
		File resultDir=new File(BuildSuperStoreData.dataDir,"levelDis");
		if(!resultDir.exists())
			resultDir.mkdirs();
		computeDistance(BuildSuperStoreData.dataDir,resultDir);
	}

	public static void computeDistance(File data, File resultDir) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(data);
		int size = sd.size();
		File result = new File(resultDir, "dis_" + size);
		result.mkdirs();

		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(1));
		dis.setData(sd);
		int level = sd.numLevels();
		double[] ld;

		BufferedWriter[] bw = new BufferedWriter[level];
		for (int l = 0; l < level; l++) {
			bw[l] = new BufferedWriter(new FileWriter(new File(result, "l" + (l + 1) + ".csv")));
		}

		int notifyCount = 100;
		for (int i = 0, j, l; i < size; i++) {
			for (j = 0; j < size; j++) {
				dis.distance(i, j);
				ld = dis.getLevelWeightedDistance().getLevelDistance();
				if (j < size - 1) {
					for (l = 0; l < level; l++) {
						bw[l].append(String.valueOf(ld[l])).append(',');
					}
				} else {
					for (l = 0; l < level; l++) {
						bw[l].append(String.valueOf(ld[l])).append('\n');
					}
				}
			}
			if (i % notifyCount == 0) {
				System.out.println((100 * (i + 1)) / size + "%");
			}
		}

		for (int l = 0; l < level; l++) {
			bw[l].close();
		}
		System.out.println("Finished!");
	}

	public static void computeSpecificDistance(File data, int[][] pairs, File resultDir) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(data);

		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(1));
		dis.setData(sd);
		int level = sd.numLevels();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(resultDir, "ldis.csv")));

		double[][] dist = new double[pairs.length][];

		for (int i = 0; i < dist.length; i++) {
			dis.distance(pairs[i][0], pairs[i][1]);
			dist[i] = dis.getLevelWeightedDistance().getLevelDistance();
		}

		for (int i = 0, j; i < level; i++) {
			bw.append("L").append(String.valueOf(i + 1));
			for (j = 0; j < dist.length; j++) {
				bw.append(',').append(String.valueOf(dist[j][i]));
			}
			bw.append('\n');
		}
		bw.close();
		System.out.println("Finished!");
	}

	public static void computeSampleDistance(File data, int first, int numSamples, int numBins, Random random,
			File resultDir) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(data);

		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(1));
		dis.setData(sd);
		int level = sd.numLevels();
		int size = sd.size();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(resultDir, "ldis.csv")));

		numSamples = numSamples > size ? size : numSamples;

		double[][] dist = new double[numSamples][];

		LinkedList<Integer> indices = new LinkedList<Integer>();
		for (int i = 0; i < first; i++) {
			indices.add(i);
		}
		for (int i = first; i < size; i++) {
			indices.add(i);
		}

		int[] counts = new int[numBins];
		int maxNumPerBin = numSamples / numBins;
		if (numSamples % numBins > 0) {
			maxNumPerBin++;
		}
		double start = 0, end = 1.01;
		double step = (end - start) / (double) numBins;
		int indexBin;
		int i = 0, ptr = 0;
		double[] ld;
		for (; i < size && ptr < dist.length; i++) {
			dis.distance(first, indices.remove(random.nextInt(indices.size())));
			ld = dis.getLevelWeightedDistance().getLevelDistance();
			indexBin = (int) ((ld[0] - start) / step);
			if (counts[indexBin] > maxNumPerBin) {
				continue;
			}
			dist[ptr++] = ld;
			counts[indexBin]++;
		}

		i = 0;
		for (int j; i < level; i++) {
			bw.append("L").append(String.valueOf(i + 1));
			for (j = 0; j < ptr; j++) {
				bw.append(',').append(String.valueOf(dist[j][i]));
			}
			bw.append('\n');
		}
		bw.close();
		System.out.println("Finished!");
	}
}
