package xjc.clustering.validation;

public class SilhouetteIndex {
	public static double si(int[][] partition, double[][] distances) {

		int index;
		double temp;
		double ax = 0, bx;
		double si = 0;
		for (int i = 0, j, l, m; i < partition.length; i++) {
			for (j = 0; j < partition[i].length; j++) {
				index = partition[i][j];

				bx = Double.MAX_VALUE;

				// ax

				if (partition[i].length > 1) {
					temp = 0;
					for (m = 0; m < partition[i].length; m++) {
						temp += distances[index][partition[i][m]];
					}
					ax = (double) temp / (partition[i].length - 1);
				} else {
					ax = 0;
				}

				for (l = 0; l < partition.length; l++) {
					if (l != i) {
						temp = 0;
						for (m = 0; m < partition[l].length; m++) {
							temp += distances[index][partition[l][m]];
						}
						temp = (double) temp / partition[l].length;
						if (bx > temp) {
							bx = temp;
						}
					}
				}
				si += (double) (bx - ax) / (Math.max(ax, bx) * partition.length);
			}
		}

		return si;
	}

}
