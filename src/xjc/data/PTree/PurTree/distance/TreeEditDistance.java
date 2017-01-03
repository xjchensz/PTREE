package xjc.data.PTree.PurTree.distance;

import distance.RTED_InfoTree_Opt;
import util.LblTree;

public class TreeEditDistance extends DistanceCreator {

	private static final long serialVersionUID = -5001111414110703228L;

	public void addTreePath(int[] data) {
		if (data != null) {
			LblTree parent = data[0] == lt2.getTreeID() ? lt2 : lt1;
			StringBuffer sbf = new StringBuffer(parent.getTreeID());
			for (int i = 1; i < data.length; i++) {
				sbf.append(data[i]).append(",");
				LblTree c = new LblTree(sbf.toString(), data[i]);
				int index = parent.getIndex(c);
				if (index < 0) {
					parent.add(c);
				}
				parent = c;
			}
		}
	}

	private double maxDis = 0;
	private double minDis = Double.MAX_VALUE;

	public double[][] distances() {
		int size = getData().size();
		double[][] distances = new double[size][size];
		for (int i = 0, j; i < size; i++) {
			for (j = 0; j < i; j++) {
				distances[i][j] = distance(i, j);
				if (distances[i][j] > maxDis)
					maxDis = distances[i][j];
				if (distances[i][j] < minDis)
					minDis = distances[i][j];
				distances[j][i] = distances[i][j];
			}
		}
		return distances;
	}

	private void getNormalizedDistance(double[][] distances) throws Exception {
		int size = getData().size();
		double width = maxDis - minDis;
		if (width == 0) {
			throw new Exception("invalid distance data width");
		}
		for (int i = 0, j; i < size; i++) {
			for (j = 0; j < i; j++) {
				distances[i][j] = (distances[i][j] - minDis) / width;
				distances[j][i] = distances[i][j];
			}
		}
	}

	public double[][] distances(int splitSize, int splitIndex) throws Exception {
		int size = getData().size();
		int sampleNum = size % splitSize == 0 ? size / splitSize : size / splitSize + 1;
		if (sampleNum < splitIndex) {
			throw new Exception("splitIndex is bigger than sampleNUM !");
		}
		double[][] distances = new double[splitSize][splitSize];

		for (int i = 0, j; i < splitSize; i++) {
			for (j = 0; j < i; j++) {
				distances[i][j] = distance((splitIndex - 1) * splitSize + i, j);
				distances[j][i] = distances[i][j];
			}
		}
		return distances;
	}

	public TreeEditDistance clone() {
		return null;
	}

	private LblTree lt1, lt2;

	@Override
	public void processSourceData(int i1, int i2) {

		try {
			getData().getItemTrees(i1, i2, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public double compute(int i1, int i2) {
		RTED_InfoTree_Opt rted;
		rted = new RTED_InfoTree_Opt(1, 1, 1);
		rted.init(lt1, lt2);
		rted.computeOptimalStrategy();
		double ted = rted.nonNormalizedTreeDist();
		return ted;
	}

	@Override
	public void start(int i1, int i2) {
		lt1 = new LblTree("root", i1);
		lt2 = new LblTree("root", i2);
	}

	int union;

	@Override
	public void end(int i1, int i2) {
		// System.out.println("tree edit end..");
		// System.out.println("edit distance .." + getCurrentDistance());
		union = 0;
		getData().handleKeyPairs(i1, i2, this);

		setCurrentDistance(getCurrentDistance() / union);
		// lt1.prettyPrint();
		// lt2.prettyPrint();
	}

	public void handleMatched(int[] subsequentKey) {
		union++;
	}

	public void handleUnMatched1(int[] subsequentKey) {
		union++;
	}

	public void handleUnMatched2(int[] subsequentKey) {
		union++;
	}

}
