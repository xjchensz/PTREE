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
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

/**
 * @author xiaojun chen
 *
 */
public class ComputeJaccardDistance {

	public static void main(String[] args) {

	}

	public static void computeJaccrdDistance(File data, File resultDir) throws IOException {

		PurTreeDataSet dataset = new PurTreeDataSet(data);
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(Double.NEGATIVE_INFINITY));
		dis.setData(dataset);
		write(dis.distances(), new File(resultDir, "dis_jaccard.csv"));
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

	public static void write(double[][] data, File file) throws IOException {
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
