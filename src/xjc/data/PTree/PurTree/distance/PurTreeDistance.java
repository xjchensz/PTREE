/**
 * 
 */
package xjc.data.PTree.PurTree.distance;

import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import common.utils.ArrayUtils;
import common.utils.collection.OrderedComplexDoubleMap;
import common.utils.collection.STATUS;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeDist.IPairKeyDistanceHandler;

/**
 * @author xiaojun chen
 *
 */
public class PurTreeDistance implements IDataDistance<PurTreeDataSet>, IPairKeyDistanceHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5001111414110703228L;

	private PurTreeDataSet m_Data;

	private ILevelWeightedDistance m_WeightedDistance;

	private double[] cacheDistance;

	public PurTreeDistance() {
	}

	public PurTreeDistance(ILevelWeightedDistance weightedDistance) {
		m_WeightedDistance = weightedDistance;
	}

	@Override
	public void setData(PurTreeDataSet data) {
		m_Data = data;
		m_WeightedDistance.setData(data);
		int size = data.size();
		cacheDistance = new double[size * (size - 1) / 2 + size - 1];
		for (int i = 0; i < cacheDistance.length; i++) {
			cacheDistance[i] = Double.NaN;
		}
	}

	@Override
	public PurTreeDataSet getData() {
		return m_Data;
	}

	public double distance(String ins1, String ins2) {
		int id1 = getData().getDataID(ins1);
		int id2 = getData().getDataID(ins2);
		return distance(id1, id2);
	}

	@Override
	public double distance(int ins1, int ins2) {
		if (ins1 == ins2) {
			m_WeightedDistance.setZero();
			return 0;
		} else {
			double value = get(ins1, ins2);
			if (!Double.isNaN(value)) {
				return value;
			} else {
				intersection = union = 0;
				m_WeightedDistance.reset();
				m_Data.handleKeyPairs(ins1, ins2, this);
				value = m_WeightedDistance.getDistance();
				put(ins1, ins2, value);
				return value;
			}
		}

	}

	@Override
	public double[] distances(int ins1) {
		double[] dis = new double[size()];
		for (int i = 0; i < dis.length; i++) {
			dis[i] = distance(ins1, i);
		}
		return dis;
	}

	protected void put(int ins1, int ins2, double distance) {
		if (ins1 > ins2) {
			int tmp = ins2;
			ins2 = ins1;
			ins1 = tmp;
		}

		cacheDistance[ins2 * (ins2 - 1) / 2 + ins1] = distance;
	}

	protected double get(int ins1, int ins2) {
		if (ins1 > ins2) {
			int tmp = ins2;
			ins2 = ins1;
			ins1 = tmp;
		}
		return cacheDistance[ins2 * (ins2 - 1) / 2 + ins1];
	}

	public void clearCache() {
		for (int i = 0; i < cacheDistance.length; i++) {
			cacheDistance[i] = Double.NaN;
		}
	}

	@Override
	public double distance(int ins1, PurTreeDataSet data2, int ins2) {
		intersection = union = 0;
		m_WeightedDistance.reset();
		m_Data.handleKeyPairs(ins1, data2, ins2, this);
		return m_WeightedDistance.getDistance();

	}

	private int[] m_CurrentCommonKey;
	private OrderedComplexDoubleMap currentLevelWeights;
	private OrderedComplexDoubleMap nextLevelWeights;
	private double similarity;

	@Override
	public boolean accept(int i1, int i2, int[] common) {
		m_CurrentCommonKey = common;
		intersection = union = 0;
		return true;
	}

	@Override
	public void end() {
		// actually, similarity
		double weight = 1;
		if (level > 0) {

			weight = currentLevelWeights.get(m_CurrentCommonKey);

			if (intersection > 0 && union > 0) {
				similarity += weight * ((double) intersection / union);
				int[] indices = nextLevelWeights.findAllIndicesForKey(m_CurrentCommonKey);
				weight = weight / (double) union;
				for (int i = 0; i < indices.length; i++) {
					nextLevelWeights.setValueAt(indices[i], weight);
				}
			}
		} else {
			if (union > 0) {
				similarity += weight * ((double) intersection / union);
				weight = weight / (double) union;
				for (int i = 0; i < nextLevelWeights.size(); i++) {
					nextLevelWeights.setValueAt(i, weight);
				}
			}
		}
	}

	private int level;

	@Override
	public void startLevel(int level) {
		this.level = level;
		if (currentLevelWeights != null) {
			currentLevelWeights.destroy();
		}
		currentLevelWeights = nextLevelWeights;
		nextLevelWeights = new OrderedComplexDoubleMap(level + 1, STATUS.DISTINCT);
		similarity = 0;
	}

	@Override
	public void endLevel() {
		m_WeightedDistance.addLevelSimilarity(level, similarity);
	}

	public ILevelWeightedDistance getLevelWeightedDistance() {
		return m_WeightedDistance;
	}

	private int intersection;
	private int union;

	@Override
	public void handleMatched(int[] subsequentKey) {
		intersection++;
		union++;
		nextLevelWeights.put(0, ArrayUtils.combine(m_CurrentCommonKey, subsequentKey));
	}

	@Override
	public void handleUnMatched1(int[] subsequentKey) {
		union++;
	}

	@Override
	public void handleUnMatched2(int[] subsequentKey) {
		union++;
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

	public void distances(double[][] distances) {
		int size = m_Data.size();
		for (int i = 0, j; i < size; i++) {
			for (j = 0; j < i; j++) {
				distances[i][j] = distance(i, j);
				distances[j][i] = distances[i][j];
			}
		}
	}

	@Override
	public int size() {
		return m_Data.size();
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
			System.out.println(i);
			sb.setLength(0);
		}

		bw.close();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		m_Data.write(out);
		out.writeUTF(m_WeightedDistance.getClass().getName());
		m_WeightedDistance.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_Data = PurTreeDataSet.read(in);
		try {
			m_WeightedDistance = (ILevelWeightedDistance) Class.forName(in.readUTF()).newInstance();
			m_WeightedDistance.readFields(in);
			m_WeightedDistance.setData(m_Data);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PurTreeDistance)) {
			return false;
		}

		PurTreeDistance sd = (PurTreeDistance) obj;
		return m_Data.equals(sd.m_Data) && m_WeightedDistance.equals(sd.m_WeightedDistance);
	}

	public PurTreeDistance clone() {
		PurTreeDistance ptd = new PurTreeDistance(m_WeightedDistance.clone());
		ptd.setData(getData());
		return ptd;
	}

	@Override
	public PurTreeDataSet newDataSet() {
		return new PurTreeDataSet(m_Data.getProductTree());
	}

	@Override
	public void destroy() {
		m_Data = null;
		m_WeightedDistance.destroy();
		cacheDistance = null;
	}

}
