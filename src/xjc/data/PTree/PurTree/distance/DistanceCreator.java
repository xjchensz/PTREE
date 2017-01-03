package xjc.data.PTree.PurTree.distance;

import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import common.IWritable;
import xjc.data.PTree.PurTree.PurTreeDataSet;

public abstract class DistanceCreator implements IWritable {

	private static final long serialVersionUID = 1L;
	private PurTreeDataSet m_Data;
	private double currentDistance;

	public double getCurrentDistance() {
		return currentDistance;
	}

	public int size() {
		return m_Data.size();
	}

	public void write(DataOutput out) throws IOException {
		m_Data.write(out);
	}

	public void readFields(DataInput in) {

		try {
			m_Data = PurTreeDataSet.read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double[][] distances() {
		int size = m_Data.size();
		double[][] distances = new double[size][size];
		for (int i = 0, j; i < size; i++) {
			for (j = 0; j < i; j++) {
				distances[i][j] = distance(i, j);
				distances[j][i] = distances[i][j];
			}
		}
		return distances;
	}

	public double distance(int i, int j) {
		this.start(i, j);
		this.processSourceData(i, j);
		this.setCurrentDistance(this.compute(i, j));
		this.end(i, j);
		return this.getCurrentDistance();
	}

	public double setCurrentDistance(double d) {
		return currentDistance = d;
	}

	public abstract DistanceCreator clone();

	public abstract void processSourceData(int i1, int i2);

	public abstract double compute(int i1, int i2);

	public abstract void start(int i1, int i2);

	public abstract void end(int i1, int i2);

	public void startLevel(int level) {

	}

	public void endLevel() {

	}

	public void start(int i1, int i2, int[] common) {

	}

	public void handleMatched(int[] subsequentKey) {

	}

	public void handleUnMatched1(int[] subsequentKey) {
	}

	public void handleUnMatched2(int[] subsequentKey) {
	}

	public double distance(String ins1, String ins2) {
		int id1 = getData().getDataID(ins1);
		int id2 = getData().getDataID(ins2);
		return distance(id1, id2);
	}

	public void setData(PurTreeDataSet data) {
		this.m_Data = data;
	}

	public PurTreeDataSet getData() {
		return this.m_Data;
	}

	public boolean equals(Object obj) {
		DistanceCreator dc = (DistanceCreator) obj;
		return m_Data.equals(dc.m_Data);
	}

	static DecimalFormat defaultFormat = new DecimalFormat("0.000");

	public void saveDistance(File file) throws IOException {
		saveDistance(file, defaultFormat, m_Data.size());
	}

	public void saveDistance(File file, int size) throws IOException {
		saveDistance(file, defaultFormat, size);
	}

	public void saveDistance(File file, DecimalFormat format, int size) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		StringBuilder sb = new StringBuilder();
		int length = m_Data.size();
		length = length < size ? length : size;
		for (int i = 0; i < length; i++) {
			sb.append(',').append(m_Data.getDataByID(i));
		}
		sb.append('\n');
		bw.append(sb);
		sb.setLength(0);
		for (int i = 0, j; i < length; i++) {
			sb.append(m_Data.getDataByID(i)).append(',');
			for (j = 0; j <= i; j++) {
				sb.append(',');
			}
			for (j = i + 1; j < length; j++) {
				sb.append(format.format(distance(i, j))).append(',');
			}
			sb.setLength(sb.length() - 1);
			sb.append('\n');
			bw.append(sb);
			bw.flush();
			sb.setLength(0);
		}

		bw.close();
	}

	public abstract void addTreePath(int[] tmp);

}
