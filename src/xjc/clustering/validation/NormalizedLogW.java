package xjc.clustering.validation;

public class NormalizedLogW {

	public static double NLW(int[][] partition, double[][] distances) {

		double norm = 0;
		for (int i = 0, j; i < distances.length; i++) {
			for (j = 0; j < distances[i].length; j++) {
				norm += distances[i][j];
			}
		}

		return logwk(partition, distances) - Math.log(norm);
	}

	public static double logwk(int[][] partition, double[][] distances) {
		return Math.log(wk(partition, distances));
	}

	public static double wk(int[][] partition, double[][] distances) {
		if (partition == null) {
			return Double.NaN;
		}

		double wk = 0, tmp;
		for (int i = 0, j, k; i < partition.length; i++) {
			tmp = 0;
			for (j = 0; j < partition[i].length; j++) {
				for (k = j + 1; k < partition[i].length; k++) {
					tmp += distances[partition[i][j]][partition[i][k]];
				}
			}
			if (partition[i].length > 1) {
				wk += (double) tmp / partition[i].length;
			}
		}
		return wk;
	}

	public static double logawk(int[][] partition, double[][] distances) {
		return Math.log(awk(partition, distances));
	}

	public static double awk(int[][] partition, double[][] distances) {

		double wk = 0, tmp;
		int count = 0;
		for (int i = 0, j, k; i < partition.length; i++) {
			tmp = 0;
			for (j = 0; j < partition[i].length; j++) {
				for (k = j + 1; k < partition[i].length; k++) {
					tmp += distances[partition[i][j]][partition[i][k]];
				}
			}
			if (partition[i].length > 1) {
				wk += (double) tmp / partition[i].length;
				count++;
			}
		}
		return wk / (double) count;
	}
}
