/**
 * 
 */
package xjc.clustering.validation;

import java.io.Serializable;

/**
 * @author xjchensz
 *
 */
public class UnSupervisedClusteringEvaluationResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6458377666761706200L;

	public long time;
	public int numClusters;
	public double m_DunnIndex;
	public double m_Moduality;
	public double m_SilhouetteIndex;
	public double m_LogWK;
	public double m_LogAWK;
	public double m_NLW;

	/**
	 * 
	 */
	public UnSupervisedClusteringEvaluationResult(int[][] partition, double[][] distances) {
		numClusters = partition.length;
		m_DunnIndex = DunnIndex.di(partition, distances);
		m_Moduality = Moduality.Q(partition, distances);
		m_SilhouetteIndex = SilhouetteIndex.si(partition, distances);
		m_NLW = NormalizedLogW.NLW(partition, distances);
		m_LogWK = NormalizedLogW.logwk(partition, distances);
		m_LogAWK = NormalizedLogW.logawk(partition, distances);
	}

	public UnSupervisedClusteringEvaluationResult(int[][] partition, double[][] distances, long time) {
		this(partition, distances);
		this.time = time;
	}

	public static UnSupervisedClusteringEvaluationResult[] merge(UnSupervisedClusteringEvaluationResult[] e1,
			UnSupervisedClusteringEvaluationResult[] e2) {
		int size1 = e1 == null ? 0 : e1.length;
		int size2 = e2 == null ? 0 : e2.length;

		UnSupervisedClusteringEvaluationResult[] results = new UnSupervisedClusteringEvaluationResult[size1 + size2];

		int ptr = 0;
		for (int i = 0; i < size1; i++) {
			results[ptr++] = e1[i];
		}

		for (int i = 0; i < size2; i++) {
			results[ptr++] = e2[i];
		}

		return results;
	}

	public static UnSupervisedClusteringEvaluationResult[] expand(UnSupervisedClusteringEvaluationResult[] evaluations,
			int times) {
		UnSupervisedClusteringEvaluationResult[] results = new UnSupervisedClusteringEvaluationResult[evaluations.length
				* times];

		for (int i = 0; i < results.length; i++) {
			results[i] = evaluations[i / times];
		}

		return results;
	}

	public static String getHead() {
		return "NU,DI,Q,SI,LogWK,LogAWK,NLW,Time\n";
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(numClusters).append(',').append(m_DunnIndex).append(',').append(m_Moduality).append(',')
				.append(m_SilhouetteIndex).append(',').append(m_LogWK).append(',').append(m_LogAWK).append(',')
				.append(m_NLW).append(',').append(time).append('\n');

		return sb.toString();
	}
}
