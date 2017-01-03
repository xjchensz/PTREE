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
import xjc.data.PTree.PurTree.distance.IDataDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

/**
 * @author xiaojun chen
 *
 */
public class ComputeDistance {
	public static double[] gamma = new double[] { 0, 0.2, 0.8, 1, 2, 1000 };

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
	}

	public static void computeDistance(Class<?> distanceClass, File dir, double[] gamma)
			throws IOException, InstantiationException, IllegalAccessException {

		PurTreeDataSet sd = new PurTreeDataSet(dir);

		for (int i = 0; i < gamma.length; i++) {
			IDataDistance<PurTreeDataSet> dataDistance = (IDataDistance<PurTreeDataSet>) distanceClass.newInstance();
			dataDistance.setData(sd);
			write(null, dataDistance.distances(), new File(dir, "distance_" + gamma[i] + ".csv"));
		}
		System.out.println("Finished computing " + dir);
	}

	public static void computeDistance(File dir, double[] gamma) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		for (int i = 0; i < gamma.length; i++) {
			PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma[i]));
			dis.setData(sd);
			write(null, dis.distances(), new File(dir, "distance_" + gamma[i] + ".csv"));
		}
		System.out.println("Finished computing " + dir);
	}

	public static void write(long[][] data, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		StringBuilder sb = new StringBuilder();
		for (int i = 0, j; i < data.length; i++) {
			for (j = 0; j < data[i].length; j++) {
				sb.append(data[i][j]).append(',');
			}
			sb.setLength(sb.length() - 1);
			sb.append('\n');
		}
		bw.append(sb);

		bw.close();
	}

	public static void write(String head, double[][] data, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		StringBuilder sb = new StringBuilder();
		if (head != null) {
			sb.append(head).append('\n');
		}

		for (int i = 0, j; i < data.length; i++) {
			for (j = 0; j < data[i].length; j++) {
				sb.append(data[i][j]).append(',');
			}
			sb.setLength(sb.length() - 1);
			sb.append('\n');
		}
		bw.append(sb);

		bw.close();
	}

	public static void write(double[][] data, File file, String separator) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		StringBuilder sb = new StringBuilder();
		for (int i = 0, j; i < data.length; i++) {
			for (j = 0; j < data[i].length; j++) {
				sb.append(data[i][j]).append(separator);
			}
			sb.setLength(sb.length() - 1);
			sb.append('\n');
		}
		bw.append(sb);

		bw.close();
	}

	public static void write(int[][] data, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		StringBuilder sb = new StringBuilder();
		for (int i = 0, j; i < data.length; i++) {
			for (j = 0; j < data[i].length; j++) {
				sb.append(data[i][j]).append(',');
			}
			sb.setLength(sb.length() - 1);
			sb.append('\n');
		}
		bw.append(sb);

		bw.close();
	}

	public static void write(double[] data, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			sb.append(data[i]).append(',');
		}
		sb.setLength(sb.length() - 1);
		sb.append('\n');
		bw.append(sb);

		bw.close();
	}

	public static void write(int[] rows, double[] data, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			sb.append(rows[i]).append(',').append(data[i]).append('\n');
		}
		bw.append(sb);

		bw.close();
	}

	public static void write(String[] rows, double[] data, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			sb.append(rows[i]).append(',').append(data[i]).append('\n');
		}
		bw.append(sb);

		bw.close();
	}
}
