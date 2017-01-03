/**
 * 
 */
package xjc.clustering.validation;

import java.lang.reflect.Field;

/**
 * @author xjchen
 * 
 */
public class StatisticEvaluationResult {
	public StatisticResult m_DunnIndex;
	public StatisticResult m_Moduality;
	public StatisticResult m_SilhouetteIndex;
	public StatisticResult m_LogWK;
	public StatisticResult m_LogAWK;
	public StatisticResult m_NLW;

	public StatisticEvaluationResult(UnSupervisedClusteringEvaluationResult[] results,
			UnSupervisedClusteringEvaluationResult[] bases, double alpha)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		Field[] fields = UnSupervisedClusteringEvaluationResult.class.getFields();

		double[] rts = new double[results.length];
		double[] bts = bases == null ? null : new double[bases.length];

		for (int i = 0, j; i < fields.length; i++) {
			try {
				Field myField = getClass().getField(fields[i].getName());
				for (j = 0; j < results.length; j++) {
					rts[j] = fields[i].getDouble(results[j]);
				}
				if (bases != null) {
					for (j = 0; j < bases.length; j++) {
						bts[j] = fields[i].getDouble(bases[j]);
					}
				}
				myField.set(this, new StatisticResult(rts, bts, alpha));
			} catch (Exception e) {
			}
		}
	}

	public static String[] getMethodNames() {
		return new String[] { "DI", "Q", "SI", "LogWK", "LogAWK", "NLW" };
	}
}
