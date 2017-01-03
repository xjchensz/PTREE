package xjc.clustering.validation;

public class DunnIndex {
	public static double di(int[][] partition, double[][] distances) {
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;

		for (int i = 0, j, l, m; i < partition.length; i++) {
			for (l = 0; l < partition[i].length; l++) {
				for (m = l + 1; m < partition[i].length; m++) {
					if (max < distances[partition[i][l]][partition[i][m]]) {
						max = distances[partition[i][l]][partition[i][m]];
					}
				}
			}
			for (j = i; j < partition.length; j++) {
				for (l = 0; l < partition[i].length; l++) {
					for (m = 0; m < partition[j].length; m++) {
						if (partition[i][l] != partition[j][m] && min > distances[partition[i][l]][partition[j][m]]) {
							min = distances[partition[i][l]][partition[j][m]];
						}

					}
				}
			}
		}

		return min / max;
	}
}
