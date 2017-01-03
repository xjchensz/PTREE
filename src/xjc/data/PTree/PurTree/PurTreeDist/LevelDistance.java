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
public class LevelDistance implements ILevelWeightedDistance {

	/**
	 * 
	 */
	private static final long serialVersionUID = -120052201947902180L;
	private double[] m_Similarity;

	private int m_Level;

	public LevelDistance(int level) {
		m_Level = level;
	}

	public int getLevel() {
		return m_Level;
	}

	public void setLevel(int m_Level) {
		this.m_Level = m_Level;
	}

	@Override
	public void setData(PurTreeDataSet data) {
		int numLevels = data.getProductTree().numLevels();
		m_Similarity = new double[numLevels];
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
		return 1 - m_Similarity[m_Level];
	}

	@Override
	public void write(DataOutput out) throws IOException {

	}

	@Override
	public void readFields(DataInput in) throws IOException {

	}

	public boolean equals(Object obj) {
		return obj instanceof LevelDistance;
	}

	public LevelDistance clone() {
		return new LevelDistance(m_Level);
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
	}

}
