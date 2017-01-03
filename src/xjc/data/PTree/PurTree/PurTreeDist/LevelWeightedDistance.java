/**
 * 
 */
package xjc.data.PTree.PurTree.PurTreeDist;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.distance.ILevelWeightedDistance;

/**
 * @author xiaojun chen
 *
 */
public class LevelWeightedDistance implements ILevelWeightedDistance {

	/**
	 * 
	 */
	private static final long serialVersionUID = -120052201947902180L;
	private double scale;
	private double m_Ratio = 2;
	private double[] m_Weights;
	private double[] m_Similarity;

	public LevelWeightedDistance() {
	}

	/**
	 * 
	 */
	public LevelWeightedDistance(double ratio) {
		m_Ratio = ratio;
		if (m_Ratio < 0 || m_Ratio == Double.NEGATIVE_INFINITY || m_Ratio == Double.NaN) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void setData(PurTreeDataSet data) {
		int numLevels = data.getProductTree().numLevels();
		m_Weights = new double[numLevels];
		m_Similarity = new double[numLevels];
		if (m_Ratio == 0) {
			m_Weights[0] = 1;
		} else if (m_Ratio == Double.POSITIVE_INFINITY) {
			m_Weights[numLevels - 1] = 1;
		} else {
			if (m_Ratio == 1) {
				scale = (double) 1 / numLevels;
			} else {
				scale = (double) (m_Ratio - 1) / (Math.pow(m_Ratio, numLevels) - 1);
			}
			for (int i = 0; i < numLevels; i++) {
				m_Weights[i] = scale * Math.pow(m_Ratio, i);
			}
		}
	}

	@Override
	public void reset() {
		for (int i = 0; i < m_Similarity.length; i++) {
			m_Similarity[i] = 0;
		}
	}

	@Override
	public void setZero() {
		for (int i = 0; i < m_Similarity.length; i++) {
			m_Similarity[i] = 1;
		}
	}

	@Override
	public void addLevelSimilarity(int level, double similarity) {
		m_Similarity[level] += similarity;
	}

	@Override
	public double getDistance() {
		double distance = 0;
		for (int i = 0; i < m_Similarity.length; i++) {
			distance += m_Weights[i] * m_Similarity[i];
		}
		return 1 - distance;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(m_Ratio);

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_Ratio = in.readDouble();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof LevelWeightedDistance)) {
			return false;
		}

		LevelWeightedDistance wd = (LevelWeightedDistance) obj;
		return m_Ratio == wd.m_Ratio;
	}

	public LevelWeightedDistance clone() {
		return new LevelWeightedDistance(m_Ratio);
	}

	@Override
	public double[] getLevelDistance() {
		double[] ld = new double[m_Similarity.length];
		for (int i = 0; i < m_Similarity.length; i++) {
			ld[i] = 1 - m_Similarity[i];
		}
		return ld;
	}

	@Override
	public void destroy() {
		m_Similarity = null;
		m_Weights = null;
	}

}
