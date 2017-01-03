/**
 * 
 */
package xjc.clustering.validation;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TTest;

/**
 * @author xjchen
 * 
 */
public class StatisticResult {
	public double mean;
	public double variance;
	public double max;
	public boolean significant;

	private static NumberFormat nf = new DecimalFormat("0.00");

	public StatisticResult(double[] values, double[] base, double alpha) {
		if (base == null || base.length < 1) {
			if (values != null && values.length > 0) {
				this.mean = StatUtils.mean(values);
				this.variance = StatUtils.variance(values);
				this.max = StatUtils.max(values);
				significant = false;
			} else {
				this.mean = Double.NaN;
				this.variance = Double.NaN;
				this.max = Double.NaN;
				significant = false;
			}
		} else {
			if (values != null && values.length > 1) {
				this.mean = StatUtils.mean(values);
				this.variance = StatUtils.variance(values);
				this.max = StatUtils.max(values);
				TTest ttest = new TTest();
				significant = ttest.tTest(values, base, alpha);
			} else if (values != null && values.length == 1) {
				this.mean = values[0];
				this.variance = values[0];
				this.max = values[0];
				significant = false;
			} else {
				this.mean = Double.NaN;
				this.variance = Double.NaN;
				this.max = Double.NaN;
				significant = false;
			}
		}
	}

	public String toMeanMaxText(boolean isMaxMean, boolean isMaxMax) {
		StringBuilder sb = new StringBuilder();
		if (isMaxMean) {
			sb.append("\\textbf{").append(Double.isNaN(mean) ? '-' : nf.format(mean)).append('}');
		} else {
			sb.append(Double.isNaN(mean) ? '-' : nf.format(mean));
		}

		if (isMaxMax) {
			sb.append('(').append("\\textbf{").append(Double.isNaN(max) ? '-' : nf.format(max)).append("})");
		} else {
			sb.append('(').append(Double.isNaN(max) ? '-' : nf.format(max)).append(')');
		}

		if (significant) {
			sb.append('*');
		}
		return sb.toString();
	}

	public String toMeanVarianceText(boolean isMaxMean) {
		StringBuilder sb = new StringBuilder();
		if (isMaxMean) {
			sb.append("\\textbf{").append(Double.isNaN(mean) ? '-' : nf.format(mean)).append('}');
		} else {
			sb.append(Double.isNaN(mean) ? '-' : nf.format(mean));
		}

		sb.append('(').append(Double.isNaN(variance) ? '-' : nf.format(variance)).append(')');

		if (significant) {
			sb.append('*');
		}
		return sb.toString();
	}
}
