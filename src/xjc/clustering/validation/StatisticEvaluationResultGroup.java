package xjc.clustering.validation;

import java.lang.reflect.Field;

public class StatisticEvaluationResultGroup {

	private StatisticEvaluationResult[] m_Results;

	public StatisticEvaluationResultGroup(StatisticEvaluationResult[] results) {
		m_Results = results;
	}

	public StatisticEvaluationResultGroup(UnSupervisedClusteringEvaluationResult[][] results, int base, double alpha)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		UnSupervisedClusteringEvaluationResult[] baseResult = null;
		if (base >= 0) {
			baseResult = results[base];
		}
		m_Results = new StatisticEvaluationResult[results.length];
		for (int i = 0; i < m_Results.length; i++) {
			m_Results[i] = new StatisticEvaluationResult(results[i], baseResult, alpha);
		}
	}

	public static String[] getMethodNames() {
		return StatisticEvaluationResult.getMethodNames();
	}

	public String getMeanMaxResult(int index) throws IllegalArgumentException, IllegalAccessException {
		Field field = StatisticEvaluationResult.class.getFields()[index];

		double maxMean = -Double.MAX_VALUE;
		double maxMax = -Double.MAX_VALUE;
		int maxMeanIndex = -1, maxMaxIndex = -1;

		StatisticResult sr;
		for (int i = 0; i < m_Results.length; i++) {

			sr = (StatisticResult) field.get(m_Results[i]);
			if (sr.mean >= maxMean) {
				maxMean = sr.mean;
				maxMeanIndex = i;
			}
			if (sr.max >= maxMax) {
				maxMax = sr.max;
				maxMaxIndex = i;
			}
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < m_Results.length; i++) {
			sr = (StatisticResult) field.get(m_Results[i]);
			sb.append(sr.toMeanMaxText(i == maxMeanIndex, i == maxMaxIndex)).append('&');
		}
		if (m_Results.length > 0) {
			sb.setLength(sb.length() - 1);
		}

		return sb.toString();
	}

	public String getMeanVarianceResult(int index) throws IllegalArgumentException, IllegalAccessException {
		Field field = StatisticEvaluationResult.class.getFields()[index];

		double maxMean = -Double.MAX_VALUE;
		int maxMeanIndex = -1;

		StatisticResult sr;
		for (int i = 0; i < m_Results.length; i++) {

			sr = (StatisticResult) field.get(m_Results[i]);
			if (sr.mean >= maxMean) {
				maxMean = sr.mean;
				maxMeanIndex = i;
			}
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < m_Results.length; i++) {
			sr = (StatisticResult) field.get(m_Results[i]);
			sb.append(sr.toMeanVarianceText(i == maxMeanIndex)).append('&');
		}
		if (m_Results.length > 0) {
			sb.setLength(sb.length() - 1);
		}

		return sb.toString();
	}

	public void clear() {
		m_Results = null;
	}

	public String toMeanMaxResult(String dataName) throws IllegalArgumentException, IllegalAccessException {
		StringBuilder sb = new StringBuilder();
		String[] methodNames = getMethodNames();

		sb.append("\\multirow{").append(methodNames.length).append("}*{").append(dataName).append("}");

		for (int i = 0; i < methodNames.length; i++) {
			sb.append("& ").append(methodNames[i]).append(" & ").append(getMeanMaxResult(i)).append("\\\\")
					.append('\n');
		}

		return sb.toString();
	}

	public String toMeanVarianceResult(String dataName) throws IllegalArgumentException, IllegalAccessException {
		StringBuilder sb = new StringBuilder();
		String[] methodNames = getMethodNames();

		sb.append("\\multirow{").append(methodNames.length).append("}*{").append(dataName).append("}");

		for (int i = 0; i < methodNames.length; i++) {
			sb.append("& ").append(methodNames[i]).append(" & ").append(getMeanVarianceResult(i)).append("\\\\")
					.append('\n');
		}

		return sb.toString();
	}
}
