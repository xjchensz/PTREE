/**
 * 
 */
package xjc.PTree.PurTree.distance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.DistanceCreator;
import xjc.data.PTree.PurTree.distance.ItemsHierarchichyDistance;
import xjc.data.PTree.PurTree.distance.LeafNodesJaccardDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;
import xjc.data.PTree.PurTree.distance.TreeEditDistance;

/**
 * @author xiaojun chen
 *
 */
public class ComputeDistanceDistribution {

	public static void main(String[] args) throws IOException {

	}

	public static void computeDistribution(File dir, int numHistograms)
			throws IOException, InstantiationException, IllegalAccessException {
		Class[] classz = new Class[] { ItemsHierarchichyDistance.class, LeafNodesJaccardDistance.class,
				TreeEditDistance.class };
		computeDistribution(classz, dir, numHistograms);
	}

	public static void computeDistribution(Class[] classz, File dir, int numHistograms)
			throws IOException, InstantiationException, IllegalAccessException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		int size = sd.size();
		StringBuilder sb = new StringBuilder();
		sb.append("Distances");
		for (int i = 0; i < numHistograms; i++) {
			sb.append(',').append((double) (i + 1) / numHistograms);
		}
		sb.append('\n');
		double[] distribution = new double[numHistograms];
		double interval = (double) 1 / numHistograms;
		int index;

		for (int i = 0, j, l; i < classz.length; i++) {
			Class<?> claz = classz[i];
			DistanceCreator dataDistance = (DistanceCreator) claz.newInstance();
			dataDistance.setData(sd);

			for (j = 0; j < distribution.length; j++) {
				distribution[j] = 0;
			}
			double[][] distances = dataDistance.distances();
			for (j = 0; j < size; j++) {
				for (l = 0; l < j; l++) {
					index = (int) (distances[j][l] / interval);
					distribution[index >= numHistograms ? numHistograms - 1 : index]++;
				}
			}
			sb.append(classz[i].getSimpleName());
			for (j = 0; j < distribution.length; j++) {
				distribution[j] = 2 * distribution[j] / (size * (size - 1));
				sb.append(',').append(distribution[j]);
			}
			sb.append('\n');
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "distribution_cjt.csv")));
		bw.append(sb);
		bw.close();
		System.out.println("Finished computing " + dir);
	}

	public static void computePTreeDistDistribution(File dir, double[] gamma, int numHistograms) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		int size = sd.size();
		StringBuilder sb = new StringBuilder();
		sb.append("Gamma");
		for (int i = 0; i < numHistograms; i++) {
			sb.append(',').append((double) (i + 1) / numHistograms);
		}
		sb.append('\n');
		double[] distribution = new double[numHistograms];
		double interval = (double) 1 / numHistograms;
		int index;

		for (int i = 0, j, l; i < gamma.length; i++) {
			PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma[i]));
			dis.setData(sd);
			for (j = 0; j < distribution.length; j++) {
				distribution[j] = 0;
			}
			for (j = 0; j < size; j++) {
				for (l = 0; l < j; l++) {
					index = (int) (dis.distance(j, l) / interval);
					distribution[index >= numHistograms ? numHistograms - 1 : index]++;
				}
			}
			sb.append(gamma[i]);
			for (j = 0; j < distribution.length; j++) {
				distribution[j] = 2 * distribution[j] / (size * (size - 1));
				sb.append(',').append(distribution[j]);
			}
			sb.append('\n');
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "distribution_ptd.csv")));
		bw.append(sb);
		bw.close();
		System.out.println("Finished computing " + dir);
	}

	public static void computePTreeDistDistributionGamma(File dir, double[] gamma) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		int size = sd.size();
		StringBuilder sb = new StringBuilder();
		sb.append("Gamma,Mean\n");

		int select = 10;
		LevelWeightedDistance ld;
		double sum;
		for (int i = 0, l; i < gamma.length; i++) {
			ld = new LevelWeightedDistance(gamma[i]);
			sum = 0;
			PurTreeDistance dis = new PurTreeDistance(ld);
			dis.setData(sd);
			for (l = 0; l < size; l++) {
				sum += dis.distance(select, l);
			}
			sb.append(gamma[i]).append(',').append(sum / (double) size).append('\n');
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "dis_mean_gamma_ptd.csv")));
		bw.append(sb);
		bw.close();
		System.out.println("Finished computing " + dir);
	}
}
