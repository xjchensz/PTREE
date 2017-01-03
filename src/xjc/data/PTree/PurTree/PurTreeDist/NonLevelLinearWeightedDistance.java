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
public class NonLevelLinearWeightedDistance implements ILevelWeightedDistance {

	/**
	 * 
	 */
	private static final long serialVersionUID = -120052201947902180L;
	private double scale;
	private double m_Ratio = 2;
	private double m_Exponent = 0.5;
	private double[] m_Weights;
	private double[] m_Similarity;
	private int[] m_NumNodesInTree;
	private int[] m_NumNodes;

	public NonLevelLinearWeightedDistance() {
	}

	/**
	 * 
	 */
	public NonLevelLinearWeightedDistance(double ratio, double exponent) {
		if (ratio > 1) {
			m_Ratio = ratio;
		}
		if (exponent > 0 && exponent <= 1) {
			m_Exponent = exponent;
		}
	}

	@Override
	public void setData(PurTreeDataSet data) {
		int numLevels = data.getProductTree().numLevels();
		scale = (double) (m_Ratio - 1) / (Math.pow(m_Ratio, numLevels) - 1);
		m_NumNodesInTree = data.getProductTree().numNodes();
		m_Weights = new double[numLevels];
		m_Similarity = new double[numLevels];
		m_NumNodes = new int[numLevels];
		for (int i = 0; i < numLevels; i++) {
			m_Weights[i] = scale * Math.pow(m_Ratio, i);
		}
	}

	@Override
	public void reset() {
		for (int i = 0; i < m_Similarity.length; i++) {
			m_Similarity[i] = 0;
			m_NumNodes[i] = 0;
		}
	}

	@Override
	public void addLevelSimilarity(int level, double similarity) {
		m_Similarity[level] += similarity;
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
	public void setZero() {
		for (int i = 0; i < m_Similarity.length; i++) {
			m_Similarity[i] = 1;
		}
	}

	@Override
	public double getDistance() {
		double distance = 0;
		for (int i = 0; i < m_Similarity.length; i++) {
			if (m_NumNodes[i] > 0) {
				distance += m_Weights[i] * ((1 - (double) m_Similarity[i] / (double) m_NumNodes[i]))
						* Math.pow(((double) m_NumNodes[i] / (double) m_NumNodesInTree[i]), m_Exponent);
			}
		}
		return distance;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(m_Exponent);
		out.writeDouble(m_Ratio);

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_Exponent = in.readDouble();
		m_Ratio = in.readDouble();
	}

	public NonLevelLinearWeightedDistance clone() {
		return new NonLevelLinearWeightedDistance();
	}

	@Override
	public void destroy() {
		m_NumNodes = null;
		m_NumNodesInTree = null;
		m_Similarity = null;
		m_Weights = null;
	}

}
