/**
 * 
 */
package xjc.data.PTree.PurTree.PurTreeClust;

import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import common.IWritable;
import common.utils.collection.OrderedComplexEmptyValueMap;
import common.utils.collection.OrderedIntArraySet;
import xjc.covertree.IDistanceHolder;
import xjc.data.PTree.ProductTree;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.SubPurTreeBuilder;
import xjc.data.PTree.PurTree.distance.IDataDistance;

/**
 * @author Admin
 *
 */
public abstract class AbstractDataset implements IDistanceHolder, IWritable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2597422039723494019L;
	protected IDataDistance<PurTreeDataSet> m_Distance;

	protected long buildTime;

	protected AbstractDataset() {
	}

	public AbstractDataset(IDataDistance<PurTreeDataSet> distance) {
		m_Distance = distance;
	}

	public PurTreeDataSet getData() {
		return m_Distance.getData();
	}

	public IDataDistance<PurTreeDataSet> getDistance() {
		return m_Distance;
	}

	@Override
	public double distance(int ins1, int ins2) {
		return m_Distance.distance(ins1, ins2);
	}

	@Override
	public double[] distances(int ins1) {
		double[] dis = new double[size()];
		for (int i = 0; i < dis.length; i++) {
			dis[i] = m_Distance.distance(ins1, i);
		}
		return dis;
	}

	@Override
	public double[][] distances() throws Exception {
		return m_Distance.distances();
	}

	@Override
	public int size() {
		return m_Distance.size();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(m_Distance.getClass().getName());
		m_Distance.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		try {
			Class<IDataDistance<PurTreeDataSet>> classz = (Class<IDataDistance<PurTreeDataSet>>) Class
					.forName(in.readUTF());
			try {
				m_Distance = classz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		m_Distance.readFields(in);
	}

	public long buildTime() {
		return buildTime;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractDataset)) {
			return false;
		}
		AbstractDataset csd = (AbstractDataset) obj;
		return m_Distance.equals(csd.m_Distance);
	}

	public static int[][] getPartition(int[] clustering) {

		OrderedIntArraySet os = new OrderedIntArraySet();
		for (int i = 0; i < clustering.length; i++) {
			os.add(clustering[i]);
		}
		int numClusters = os.size();

		OrderedIntArraySet[] tos = new OrderedIntArraySet[numClusters];
		for (int i = 0; i < tos.length; i++) {
			tos[i] = new OrderedIntArraySet();
		}
		for (int i = 0; i < clustering.length; i++) {
			tos[os.indexOf(clustering[i])].add(i);
		}
		int[][] partition = new int[numClusters][];
		for (int i = 0; i < partition.length; i++) {
			partition[i] = tos[i].values();
			tos[i].destroy();
		}
		os.destroy();
		return partition;
	}

	public int[][] partition(int numClusters, CenterMeasureType cmt, Random random) {
		int[] assignments = clustering(numClusters, cmt, random);
		return getPartition(assignments);
	}

	public int[][] partition(int numClusters, CenterMeasureType cmt, File file, Random random) throws IOException {
		int[] assignments = clustering(numClusters, cmt, file, random);
		return getPartition(assignments);
	}

	public double[][] sparsityOfCenters(int[] centers) {
		double[][] sparsity = new double[centers.length][];
		for (int i = 0; i < sparsity.length; i++) {
			sparsity[i] = m_Distance.getData().sparsity(centers[i]);
		}
		return sparsity;
	}

	public double[][] sparsityOfClusterTree(int numClusters, CenterMeasureType cmt, Random random) throws IOException {
		int[] nns = m_Distance.getData().getProductTree().numNodes();
		ProductTree[] trees = getClusterTree(numClusters, cmt, random);
		double[][] sparsity = new double[numClusters][m_Distance.getData().numLevels()];
		int[] numNodes;
		for (int i = 0, j; i < sparsity.length; i++) {
			numNodes = trees[i].numNodes();
			for (j = 0; j < nns.length; j++) {
				sparsity[i][j] = (double) numNodes[j] / nns[j];
			}
		}

		return sparsity;
	}

	public double[][] avgSparsityOfClusters(int numClusters, CenterMeasureType cmt, Random random) {
		int[] clustering = clustering(numClusters, cmt, random);
		return avgSparsityOfClusters(clustering);
	}

	public double[][] avgSparsityOfClusters(int[] clustering) {
		OrderedIntArraySet os = new OrderedIntArraySet();
		for (int i = 0; i < clustering.length; i++) {
			os.add(clustering[i]);
		}
		int numClusters = os.size();
		os.destroy();
		double[][] sparsity = new double[numClusters][m_Distance.getData().numLevels()];
		double[] tmp;
		int[] numObjects = new int[numClusters];
		for (int i = 0, j; i < clustering.length; i++) {
			tmp = m_Distance.getData().sparsity(i);
			for (j = 0; j < tmp.length; j++) {
				sparsity[clustering[i]][j] += tmp[j];
			}
			numObjects[clustering[i]]++;
		}
		for (int i = 0, j; i < sparsity.length; i++) {
			for (j = 0; j < sparsity[i].length; j++) {
				sparsity[i][j] = (double) sparsity[i][j] / numObjects[i];
			}
		}
		return sparsity;
	}

	public ProductTree[] getClusterTree(int numClusters, CenterMeasureType cmt, Random random) throws IOException {
		int[] assignments = clustering(numClusters, cmt, random);
		int[][] partitions = getPartition(assignments);

		int numLevels = m_Distance.getData().numLevels();
		OrderedComplexEmptyValueMap values = m_Distance.getData().m_Values[numLevels];

		SubPurTreeBuilder builder = new SubPurTreeBuilder(m_Distance.getData().getProductTree());
		builder.getSubTree().destroy();
		int[] indices;
		int[] fullKeys, keys = new int[numLevels];
		ProductTree[] trees = new ProductTree[numClusters];
		for (int l = 0, i, j, t; l < numClusters; l++) {
			builder.reset();
			for (i = 0; i < partitions[l].length; i++) {
				indices = values.findAllIndicesForKey(partitions[l][i]);
				for (j = 0; j < indices.length; j++) {
					fullKeys = values.getKeyAt(indices[j]);
					for (t = 1; t < fullKeys.length; t++) {
						keys[t - 1] = fullKeys[t];
					}
					builder.keyAdd(keys);
				}
			}
			trees[l] = builder.getSubTree();
		}
		return trees;
	}

	public void saveClusterTree(int numClusters, CenterMeasureType cmt, File dir, Random random) throws IOException {

		ProductTree[] trees = getClusterTree(numClusters, cmt, random);

		dir.mkdirs();
		for (int i = 0; i < numClusters; i++) {
			trees[i].saveXML(new File(dir, "Tree " + (i + 1) + ".xml"));
		}
	}

	public abstract int[] clustering(int numClusters, CenterMeasureType cmt, Random random);

	public int[] clustering(int numClusters, CenterMeasureType cmt, File file, Random random) throws IOException {
		PurTreeDataSet data = m_Distance.getData();
		int[] assignments = clustering(numClusters, cmt, random);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.append("cluster,Data\n");
		for (int i = 0; i < assignments.length; i++) {
			bw.append(String.valueOf(assignments[i])).append(",").append(data.getDataByID(i)).append('\n');
		}
		bw.close();
		return assignments;
	}

	public abstract AbstractDataset clone() throws CloneNotSupportedException;

	public void destroy() {

	}

	public void save(File file) throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		write(dos);
		dos.close();
	}

}
