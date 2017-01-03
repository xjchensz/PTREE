/**
 * 
 */
package xjc.data.PTree.PurTree.PurTreeClust;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import common.utils.RandomUtils;
import common.utils.collection.OrderedIntArraySet;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeFactory;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.distance.IDataDistance;

/**
 * @author xiaojun chen
 *
 */
public class PurTreeDataCoverTree extends AbstractDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2597422039723494019L;
	private CoverTree ct;

	protected PurTreeDataCoverTree() {
	}

	/**
	 * 
	 */
	public PurTreeDataCoverTree(IDataDistance<PurTreeDataSet> distance, Random random) {
		this(distance, 2, null, random);
	}

	public PurTreeDataCoverTree(IDataDistance<PurTreeDataSet> distance, int[] selected, int[] sampled, Random random) {
		this(distance, estimateAlpha(distance, sampled), selected, random);
	}

	public PurTreeDataCoverTree(IDataDistance<PurTreeDataSet> distance, int[] selected, Random random) {
		this(distance, 2, selected, random);
	}

	public PurTreeDataCoverTree(IDataDistance<PurTreeDataSet> distance, double alpha, int[] selected, Random random) {
		super(distance);
		ct = CoverTreeFactory.getDefault().create(this, alpha);

		PurTreeDataSet data = distance.getData();
		long start = System.currentTimeMillis();
		int size = 0;

		LinkedList<Integer> sample = new LinkedList<Integer>();
		if (selected == null) {
			size = data.size();
			for (int i = 0; i < size; i++) {
				sample.add(i);
			}
		} else {
			size = selected.length;
			for (int i = 0; i < size; i++) {
				sample.add(selected[i]);
			}
		}

		for (int i = 0; i < size; i++) {
			ct.insert(sample.remove(random.nextInt(sample.size())));
		}
		ct.update();
		buildTime = System.currentTimeMillis() - start;
	}

	public static double estimateAlpha(IDataDistance<PurTreeDataSet> distance, int[] sampled) {
		PurTreeDataSet data = distance.getData();
		int size = data.size();

		if (sampled == null) {
			sampled = RandomUtils.nonRepeatSample(0, size, 1, size > 10 ? 10 : size, new Random());
		}

		double minDist = Double.MAX_VALUE, dist;

		for (int i = 0, j; i < sampled.length; i++) {
			for (j = 0; j < size; j++) {
				if (sampled[i] != j) {
					dist = distance.distance(sampled[i], j);
					if (dist > 0 && minDist > dist) {
						minDist = dist;
					}
				}
			}
		}

		double alpha = 1.0 / Math.pow(minDist, Math.log(2) / Math.log(size));
		return alpha > 2 ? alpha : 2;
	}

	public int getNearest(int id) {
		return ct.getNearest(id);
	}

	public String getNearest(String dataID) {
		int id = m_Distance.getData().getDataID(dataID);
		return m_Distance.getData().getDataByID(ct.getNearest(id));
	}

	public int[] getNearest(int id, int k) {
		return ct.getKNearestNeighbor(id, k);
	}

	public String[] getNearest(String dataID, int k) {
		int id = m_Distance.getData().getDataID(dataID);
		int[] knn = ct.getKNearestNeighbor(id, k);
		String[] results = new String[knn.length];
		for (int i = 0; i < results.length; i++) {
			results[i] = m_Distance.getData().getDataByID(knn[i]);
		}
		return results;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		ct.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		ct = CoverTree.read(in, this);
	}

	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof PurTreeDataCoverTree)) {
			return false;
		}
		PurTreeDataCoverTree csd = (PurTreeDataCoverTree) obj;
		return ct.equals(csd.ct);
	}

	public int[] getKcenters(int numClusters, CenterMeasureType cmt) {
		switch (cmt) {
		case LEVEL_DENSITY:
			return ct.getKLevelDensityCenters(numClusters);
		case SEPARATE_DENSITY:
			return ct.getKSeparateDensityCenters(numClusters);
		case CENTRALITY:
		default:
			return ct.getKCentralityCenters(5,numClusters);
		}
	}

	public double[][] sparsityOfCenters(int numClusters, CenterMeasureType cmt) {
		int[] centers;
		switch (cmt) {
		case LEVEL_DENSITY:
			centers = ct.getKLevelDensityCenters(numClusters);
			break;
		case CENTRALITY:
		default:
			centers = ct.getKCentralityCenters(5,numClusters);
			break;
		}

		double[][] sparsity = new double[centers.length][];
		for (int i = 0; i < sparsity.length; i++) {
			sparsity[i] = m_Distance.getData().sparsity(centers[i]);
		}
		return sparsity;
	}

	public int[] clustering(int numClusters, CenterMeasureType cmt, Random random) {
		long start = System.currentTimeMillis();
		int[] centers = getKcenters(numClusters, cmt);
		int[] assignments = clustering(centers);
		buildTime = System.currentTimeMillis() - start;
		return assignments;
	}

	public int[] clustering(int[] centers) {
		PurTreeDataSet data = m_Distance.getData();
		int size = data.size();

		double dist, minDist;
		int[] assignments = new int[size];

		OrderedIntArraySet os = new OrderedIntArraySet();
		Random random = new Random();
		for (int i = 0, k; i < size; i++) {
			minDist = Double.MAX_VALUE;
			os.clear();
			for (k = 0; k < centers.length; k++) {
				if (i == centers[k]) {
					os.clear();
					os.add(k);
					break;
				} else {
					dist = m_Distance.distance(i, centers[k]);
					if (dist < minDist) {
						minDist = dist;
						os.clear();
						os.add(k);
					} else if (dist == minDist) {
						os.add(k);
					}
				}
			}
			if (os.size() == 1) {
				assignments[i] = os.getLastValue();
			} else {
				assignments[i] = os.getValueAt(random.nextInt(os.size()));
			}
		}
		return assignments;
	}

	public String print() {
		return ct.printLevels();
	}

	public CoverTree getCoverTree() {
		return ct;
	}

	public PurTreeDataCoverTree clone() throws CloneNotSupportedException {
		PurTreeDataCoverTree pct = new PurTreeDataCoverTree();
		pct.m_Distance = m_Distance.clone();
		pct.buildTime = buildTime;
		pct.ct = new CoverTree(ct, pct);
		return pct;
	}

	public void destroy() {
		super.destroy();
		ct.destroy();
		ct = null;
	}

	public static PurTreeDataCoverTree readFile(File file) throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		PurTreeDataCoverTree ctd = read(dis);
		dis.close();
		return ctd;
	}

	public static PurTreeDataCoverTree read(DataInput in) throws IOException {
		PurTreeDataCoverTree ctd = new PurTreeDataCoverTree();
		ctd.readFields(in);
		return ctd;
	}

}
