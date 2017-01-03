package xjc.data.PTree.PurTree.distance;

public class ItemsHierarchichyDistance extends DistanceCreator {

	private static final long serialVersionUID = -5001111414110703228L;

	private double a = 1.0;
	private double c = 5.0;
	private double b;
	private double sl;
	private double sh;
	private int[][] transRecord;
	private int defaultTotalRecordSize = 100; // for two data object

	public ItemsHierarchichyDistance(double a, double c, double b) {
		this.a = a;
		this.c = c;
		this.b = (a + c) / 2;
		transRecord = new int[defaultTotalRecordSize + 1][];
		transRecord[0] = new int[] { 0, 0, -1, -1 };
	}

	public ItemsHierarchichyDistance() {
		this.b = (a + c) / 2;
		transRecord = new int[defaultTotalRecordSize + 1][];
		transRecord[0] = new int[] { 0, 0, -1, -1 };
	}

	private double getTwoItemDistance(int[] source, int[] target) {
		double result = 0.0;
		double l, h;
		l = 0;
		h = source.length - 1;
		for (int i = 1; i < source.length; i++) {
			if (source[i] != target[i]) {
				l = (source.length - i) * 2;
				h = i - 1;
				if (i == source.length - 1)
					l = 0.5;
			}

			if (l != 0 && h != source.length - 1) {
				break;
			}
		}

		if (l < this.a) {
			sl = 1;
		} else if (l <= this.b) {
			sl = 1 - 2 * Math.pow((l - a) / (c - a), 2);
		}

		else if (l <= this.c) {
			sl = 2 * Math.pow((l - c) / (c - a), 2);
		} else {
			sl = 0;
		}

		// ** sh
		if (h < this.a) {
			sh = 0;
		} else if (h <= this.b) {
			sh = Math.pow(h - a, 2) / ((b - a) * (c - a));
		} else if (h <= this.c) {
			sh = 1 - Math.pow(h - c, 2) / ((c - b) * (c - a));
		} else {
			sh = 1;
		}
		result = 1 - sl * sh;
		return result;
	}

	/**
	 * @param data
	 */
	public void addTreePath(int[] data) {

		if (data != null) {

			int rindex = transRecord[0][0];
			if (rindex + 1 >= transRecord.length) {
				int[][] newTransRecord = new int[transRecord.length + rindex][];
				for (int i = 0; i < transRecord.length; i++) {
					newTransRecord[i] = transRecord[i];
				}
				transRecord = newTransRecord;
			}
			transRecord[transRecord[0][0] = ++rindex] = data;

			if (transRecord[0][2] == -1) {
				transRecord[0][2] = data[0];
			} else if (transRecord[0][2] != data[0] && transRecord[0][3] == -1) {
				transRecord[0][3] = data[0];
				transRecord[0][1] = rindex;
			}
		}
	}

	public double getDistance() {
		return getCurrentDistance();
	}

	public ItemsHierarchichyDistance clone() {
		return null;
	}

	@Override
	public double compute(int i1, int i2) {

		int index = transRecord[0][0];
		int secondIndex = transRecord[0][1];
		double[] dt = new double[secondIndex];
		for (int i = 1; i < secondIndex; i++) {
			double tmp = Double.MAX_VALUE;
			double tmpij = 0.0;
			for (int j = secondIndex; j <= index; j++) {
				tmpij = getTwoItemDistance(transRecord[i], transRecord[j]);
				if (tmpij < tmp) {
					tmp = tmpij;
				}

			}
			dt[i] = tmp;
		}

		double sum = 0.0;
		for (int i = 0; i < dt.length; i++) {
			sum += dt[i];
		}
		if (index - secondIndex > secondIndex) {
			if (secondIndex == 0) {
				System.out.println("0 devided " + secondIndex);
			}
			return sum / secondIndex;
		} else if (index == 0) {

			System.out.println("0 devided " + index);
		}
		return sum / index;
	}

	@Override
	public void start(int i1, int i2) {

		for (int i = 1; i < transRecord.length; i++) {
			transRecord[i] = null;
		}
		transRecord[0][0] = 0;
		transRecord[0][1] = 0;
		transRecord[0][2] = -1;
		transRecord[0][3] = -1;
	}

	@Override
	public void end(int i1, int i2) {
	}

	@Override
	public void processSourceData(int i1, int i2) {
		try {
			getData().getItemTrees(i1, i2, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
