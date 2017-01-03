package xjc.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteUtils {
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
