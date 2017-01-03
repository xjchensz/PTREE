package xjc.data.PTree.PurTree.distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeafNodesJaccardDistance extends DistanceCreator {

	private static final long serialVersionUID = -5001111414110703228L;

	private int[][] transRecord;
	private int defaultTotalRecordSize = 100; // for two data object

	public LeafNodesJaccardDistance() {

		transRecord = new int[defaultTotalRecordSize + 1][];
		transRecord[0] = new int[] { 0, 0, -1, -1 };
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

	public static void main(String[] args) {
		LeafNodesJaccardDistance ln = new LeafNodesJaccardDistance();
		ln.transRecord = new int[][] { { 7, 3, -1, -1 }, { 10, 0, 0, 0, 0, 1 }, { 10, 0, 0, 0, 1, 0 },
				{ 10, 1, 1, 1, 1, 0 }, { 9, 2, 0, 0, 0, 0 }, { 9, 0, 2, 0, 1, 0 }, { 9, 0, 0, 0, 0, 1 },
				{ 9, 0, 0, 0, 0, 2 } };
		System.out.println(ln.compute(1, 1));
	}

	@Override
	public double compute(int i1, int i2) {

		int index = transRecord[0][0];
		List<String> pathdata = new ArrayList<String>();
		for (int i = 1; i <= index; i++) {
			pathdata.add(Arrays.toString(Arrays.copyOfRange(transRecord[i], 1, transRecord[i].length)));
		}

		Map<String, String> pathmap = new HashMap<String, String>();
		int intersection = 0, union = 0;

		for (String s : pathdata) {
			if (pathmap.containsKey(s)) {
				intersection++;
			} else {
				union++;
			}
			pathmap.put(s, "");
		}
		pathmap.clear();
		double r = 1.0 - (double) intersection / union;
		return r;
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
