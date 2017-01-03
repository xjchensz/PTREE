/**
 * 
 */
package xjc.data.PTree.PurTree;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Random;

import common.utils.ArrayUtils;
import common.utils.RandomUtils;
import common.utils.collection.IPairKeyHandler;
import common.utils.collection.IntArrayQueue;
import common.utils.collection.OrderedComplexEmptyValueMap;
import common.utils.collection.OrderedIntArraySet;
import xjc.data.PTree.AbstractPTreeDataSet;
import xjc.data.PTree.IChildFilter;
import xjc.data.PTree.ITreeNodeCreator;
import xjc.data.PTree.ProductTree;
import xjc.data.PTree.TreeNode;
import xjc.data.PTree.PurTree.PurTreeDist.IPairKeyDistanceHandler;
import xjc.data.PTree.PurTree.distance.DistanceCreator;
import xjc.data.PTree.PurTree.distance.SingleDimensionArrayQueue;

/**
 * @author xiaojun chen
 *
 */
public class PurTreeDataSet extends AbstractPTreeDataSet implements IChildFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OrderedComplexEmptyValueMap[] m_Values;

	protected PurTreeDataSet() {
	}

	public PurTreeDataSet(File dir) throws IOException {
		DataInputStream pdis = new DataInputStream(new FileInputStream(new File(dir, "data")));
		readFields(pdis);
		pdis.close();
	}

	public PurTreeDataSet(ITreeNodeCreator<TreeNode> treeNodeCreator) {
		super(treeNodeCreator);
	}

	public PurTreeDataSet(ProductTree<TreeNode> tree) {
		super(tree);
	}

	public PurTreeDataSet(PurTreeDataSet set, int[] selected) {
		super(set, selected);
		m_Values = new OrderedComplexEmptyValueMap[m_NumLevels + 1];
		for (int i = 0; i < m_Values.length; i++) {
			m_Values[i] = new OrderedComplexEmptyValueMap(i + 1);
		}

		for (int i = 0; i < m_Values.length; i++) {
			set.m_Values[i].copy(m_Values[i], selected);
		}

		pruneProductTree();
	}

	public PurTreeDataSet(PurTreeDataSet set, int[] numSelectedNodes, Random random) {
		super(set);
		m_Values = new OrderedComplexEmptyValueMap[m_NumLevels + 1];

		m_Values[0] = set.m_Values[0].clone();

		for (int i = 1; i < m_Values.length; i++) {
			m_Values[i] = new OrderedComplexEmptyValueMap(i + 1);
		}

		int size = m_Values[0].size();

		OrderedIntArraySet os = new OrderedIntArraySet();
		OrderedIntArraySet tp = new OrderedIntArraySet();
		OrderedIntArraySet tmp;
		OrderedIntArraySet base = new OrderedIntArraySet();
		int[] indices;

		for (int n = 0, i, j, l, selectBase; n < size; n++) {
			// for each customer
			tp.clear();
			tp.add(n);
			for (i = 1; i < m_Values.length; i++) {
				tmp = os;
				os = tp;
				tp = tmp;
				tp.clear();
				base.clear();
				for (j = 0; j < os.size(); j++) {
					indices = set.m_Values[i].findAllIndicesForKey(set.m_Values[i - 1].getKeyAt(os.getValueAt(j)));
					if (indices.length > 0) {
						selectBase = random.nextInt(indices.length);
						base.add(indices[selectBase]);
						tp.addAll(indices);
						tp.removeValue(indices[selectBase]);
					}
				}
				// sample
				tp.retain(numSelectedNodes[i - 1] - base.size(), random);
				tp.addAll(base);

				for (l = 0; l < tp.size(); l++) {
					m_Values[i].put(set.m_Values[i].getKeyAt(tp.getValueAt(l)));
				}
			}
		}
		pruneProductTree();
	}

	private void pruneProductTree() {
		OrderedIntArraySet leafNodes = new OrderedIntArraySet();
		int valueIndex = m_Values.length - 1;
		for (int j = m_Values[valueIndex].size() - 1; j >= 0; j--) {
			leafNodes.add(m_Values[valueIndex].getKeyAt(j)[valueIndex]);
		}
		// prune product trees
		m_Tree.prune(leafNodes);
	}

	@Override
	public void init() {
		m_Values = new OrderedComplexEmptyValueMap[m_NumLevels + 1];
		for (int i = 0; i < m_Values.length; i++) {
			m_Values[i] = new OrderedComplexEmptyValueMap(i + 1);
		}
	}

	public void addValue(String dataID, String lastKey) {
		m_Values[m_Values.length - 1].put(addKey(dataID, lastKey));
	}

	public void addValue(int... keys) {
		m_Values[m_Values.length - 1].put(keys);
	}

	@Override
	protected void _save(File dir) throws IOException {
		// save nothing

	}

	public void getItemTrees(int ins1, int ins2, DistanceCreator handler) throws Exception {

		// if (result == null)
		// return;
		SingleDimensionArrayQueue queue = new SingleDimensionArrayQueue();
		// final IntArrayQueue queue = new IntArrayQueue(m_Values.length);
		queue.add(new int[] { ins1 });
		queue.add(new int[] { ins2 });

		int[] tmp;

		while (queue.size() != 0) {
			tmp = queue.outqueue();
			if (tmp == null)
				continue;
			if (tmp.length == m_Values.length) {

				// System.out.println(Arrays.toString(tmp));
				handler.addTreePath(tmp);
			} else {
				int[][] fullkeys = m_Values[tmp.length].getFullKey(tmp);

				for (int[] key : fullkeys) {
					if (key.length == 6) {
						handler.addTreePath(key);
						continue;
						// System.out.println("full " + Arrays.toString(key));
					}
					int[] t = Arrays.copyOf(key, key.length);
					queue.add(t);
				}
			}
		}
	}

	public void handleKeyPairs(int ins1, PurTreeDataSet aData, int ins2, final IPairKeyDistanceHandler handler) {

		IntArrayQueue tmp = null;
		for (int i = 1; i < m_Values.length; i++) {
			final IntArrayQueue last = tmp;
			final IntArrayQueue queue = new IntArrayQueue(i);
			handler.startLevel(i - 1);
			do {
				final int[] key = last == null || last.isEmpty() ? new int[0] : last.remove();
				if (handler.accept(ins1, ins2, key)) {
					m_Values[i].handleMatchedKeys(ins1, aData.m_Values[i], ins2, key, new IPairKeyHandler() {

						@Override
						public void handleMatched(int[] subsequentKey) {
							queue(subsequentKey);
							handler.handleMatched(subsequentKey);
						}

						@Override
						public void handleUnMatched1(int[] subsequentKey) {
							handler.handleUnMatched1(subsequentKey);
						}

						@Override
						public void handleUnMatched2(int[] subsequentKey) {
							handler.handleUnMatched2(subsequentKey);
						}

						private void queue(int[] subsequentKey) {
							queue.add(Arrays.copyOf(ArrayUtils.combine(key, subsequentKey),
									key == null ? 0 + subsequentKey.length : key.length + subsequentKey.length));
						}
					});
					handler.end();
				}
			} while (last != null && !last.isEmpty());

			handler.endLevel();
			if (last != null) {
				last.destroy();
			}
			tmp = null;
			tmp = queue;
			if (tmp.size() == 0) {
				break;
			}
		}
	}

	public void handleKeyPairs(int ins1, int ins2, final IPairKeyDistanceHandler handler) {

		IntArrayQueue tmp = null;
		for (int i = 1; i < m_Values.length; i++) {
			final IntArrayQueue last = tmp;
			final IntArrayQueue queue = new IntArrayQueue(i);
			handler.startLevel(i - 1);
			do {
				final int[] key = last == null || last.isEmpty() ? new int[0] : last.remove();
				if (handler.accept(ins1, ins2, key)) {
					m_Values[i].handleMatchedKeys(ins1, ins2, key, new IPairKeyHandler() {

						@Override
						public void handleMatched(int[] subsequentKey) {
							queue(subsequentKey);
							handler.handleMatched(subsequentKey);
						}

						@Override
						public void handleUnMatched1(int[] subsequentKey) {
							handler.handleUnMatched1(subsequentKey);
						}

						@Override
						public void handleUnMatched2(int[] subsequentKey) {
							handler.handleUnMatched2(subsequentKey);
						}

						private void queue(int[] subsequentKey) {
							queue.add(Arrays.copyOf(ArrayUtils.combine(key, subsequentKey),
									key == null ? 0 + subsequentKey.length : key.length + subsequentKey.length));
						}
					});
					handler.end();
				}
			} while (last != null && !last.isEmpty());

			handler.endLevel();
			if (last != null) {
				last.destroy();
			}
			tmp = null;
			tmp = queue;
			if (tmp.size() == 0) {
				break;
			}
		}
	}

	public void handleKeyPairs(int ins1, int ins2, final DistanceCreator handler) {

		IntArrayQueue tmp = null;
		for (int i = 1; i < m_Values.length; i++) {
			final IntArrayQueue last = tmp;
			final IntArrayQueue queue = new IntArrayQueue(i);
			do {
				final int[] key = last == null || last.isEmpty() ? new int[0] : last.remove();
				m_Values[i].handleMatchedKeys(ins1, ins2, key, new IPairKeyHandler() {

					@Override
					public void handleMatched(int[] subsequentKey) {
						queue(subsequentKey);
						handler.handleMatched(subsequentKey);
					}

					@Override
					public void handleUnMatched1(int[] subsequentKey) {
						handler.handleUnMatched1(subsequentKey);
					}

					@Override
					public void handleUnMatched2(int[] subsequentKey) {
						handler.handleUnMatched2(subsequentKey);
					}

					private void queue(int[] subsequentKey) {
						queue.add(Arrays.copyOf(ArrayUtils.combine(key, subsequentKey),
								key == null ? 0 + subsequentKey.length : key.length + subsequentKey.length));
					}
				});
			} while (last != null && !last.isEmpty());

			if (last != null) {
				last.destroy();
			}
			tmp = null;
			tmp = queue;
			if (tmp.size() == 0) {
				break;
			}
		}
	}

	public void handleKeyPairs(int ins1, int ins2, int[] key, final IPairKeyDistanceHandler handler) {
		handleKeyPairs(ins1, this, ins2, key, handler);
	}

	public void handleKeyPairs(int ins1, PurTreeDataSet aData, int ins2, int[] key,
			final IPairKeyDistanceHandler handler) {
		if (handler.accept(ins1, ins2, key)) {
			m_Values[key.length + 1].handleMatchedKeys(ins1, aData.m_Values[key.length + 1], ins2, key, handler);
			handler.end();
		}
	}

	@Override
	protected int innerUpdate() {
		int size = m_Values.length;
		for (int i = size - 1; i > 0; i--) {
			m_Values[i - 1] = m_Values[i].aggregate(i, m_Values[i - 1]);
		}
		return m_Values[0].size();
	}

	public void write(DataOutput out) throws IOException {
		super.write(out);
		for (int i = 0; i < m_Values.length; i++) {
			m_Values[i].write(out);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		m_Values = new OrderedComplexEmptyValueMap[m_NumLevels + 1];
		for (int i = 0; i < m_Values.length; i++) {
			m_Values[i] = new OrderedComplexEmptyValueMap(i + 1);
			m_Values[i].readFields(in);
		}
	}

	public static PurTreeDataSet read(DataInput in) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet();
		sd.readFields(in);
		return sd;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PurTreeDataSet) || !(super.equals(obj))) {
			return false;
		}

		PurTreeDataSet pd = (PurTreeDataSet) obj;

		for (int i = 0; i < m_Values.length; i++) {
			if (!m_Values[i].equals(pd.m_Values[i])) {
				return false;
			}
		}

		return true;
	}

	public double[] sparsity(String data) {
		return sparsity(getDataID(data));
	}

	public double[] avgSparsity(Random random) {
		if (random == null) {
			return avgSparsity();
		}
		return avgSparsity(size > 10000 ? 100 : size > 1000 ? 50 : size > 20 ? 20 : size, random);
	}

	public double[] avgSparsity(int numSelected, Random random) {

		double[] results = avgNodes(numSelected, random);

		int[] nnodes = getProductTree().numNodes();
		for (int j = 0; j < results.length; j++) {
			results[j] /= (double) (nnodes[j]);
		}
		return results;
	}

	public double[] avgSparsity() {

		double[] results = avgNodes();

		int[] nnodes = getProductTree().numNodes();
		for (int j = 0; j < results.length; j++) {
			results[j] /= (double) (nnodes[j]);
		}
		return results;
	}

	public double[] avgNodes(Random random) {
		return avgNodes(size > 10000 ? 100 : size > 1000 ? 50 : size > 20 ? 20 : size, random);
	}

	public double[] avgNodes() {
		double[] results = new double[m_NumLevels];
		int[][] nn = numNodes();
		for (int i = 0, j; i < nn.length; i++) {
			for (j = 0; j < nn[i].length; j++) {
				results[j] += nn[i][j];
			}
		}

		for (int j = 0; j < results.length; j++) {
			results[j] /= (double) size;
		}

		return results;
	}

	public double[] avgNodes(int numSelected, Random random) {

		int[] selected = RandomUtils.nonRepeatSample(0, size, 1, numSelected, random);
		double[] results = new double[m_NumLevels];
		int[][] nn = numNodes(selected);
		for (int i = 0, j; i < nn.length; i++) {
			for (j = 0; j < nn[i].length; j++) {
				results[j] += nn[i][j];
			}
		}

		for (int j = 0; j < results.length; j++) {
			results[j] /= (double) numSelected;
		}

		return results;
	}

	public double[] sparsity(int dataID) {
		int[] numNodes = m_Tree.numNodes();
		double[] sparsity = new double[numNodes.length];

		int[][] nn = numNodes(new int[] { dataID });

		for (int i = 0; i < sparsity.length; i++) {
			sparsity[i] = (double) nn[0][i] / numNodes[i];
		}
		return sparsity;
	}

	public void saveObject(String data, File dir) throws IOException {
		Writer writer = new FileWriter(new File(dir, data + ".xml"));
		saveObject(getDataID(data), writer);
		writer.close();
	}

	public void saveObject(String data, Writer writer) throws IOException {
		saveObject(getDataID(data), writer);
	}

	public void saveObject(int dataID, File dir) throws IOException {
		Writer writer = new FileWriter(new File(dir, getDataByID(dataID) + ".xml"));
		saveObject(dataID, writer);
		writer.close();
	}

	public void saveObject(int dataID, Writer writer) throws IOException {

		TreeNode root = (TreeNode) m_Tree.getRoot();

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
		root.toXML(sb, this, new int[] { dataID });
		writer.append(sb.toString());
	}

	public PurTreeDataSet sample(int[] selected) {
		return new PurTreeDataSet(this, selected);
	}

	public PurTreeDataSet sampleInTree(Random random) {
		double[] nnodes = avgNodes(random);

		int[] nn = new int[nnodes.length];
		for (int i = 0; i < nn.length; i++) {
			nn[i] = (int) nnodes[i];
		}

		return sampleInTree(nn, random);
	}

	public PurTreeDataSet sampleInTree(int[] numSelectedNodes, Random random) {
		return new PurTreeDataSet(this, numSelectedNodes, random);
	}

	public int[][] numNodes(int[] index) {
		if (index == null) {
			return numNodes();
		}
		int numLevels = numLevels();
		int[][] nn = new int[index.length][numLevels];

		OrderedIntArraySet os = new OrderedIntArraySet();
		OrderedIntArraySet tp = new OrderedIntArraySet();
		OrderedIntArraySet tmp;
		int[] indices;
		for (int n = 0, i, j; n < index.length; n++) {
			// for each customer
			tp.clear();
			tp.add(index[n]);
			for (i = 1; i < m_Values.length; i++) {
				tmp = os;
				os = tp;
				tp = tmp;
				tp.clear();
				for (j = 0; j < os.size(); j++) {
					indices = m_Values[i].findAllIndicesForKey(m_Values[i - 1].getKeyAt(os.getValueAt(j)));
					tp.addAll(indices);
				}
				nn[n][i - 1] = tp.size();
			}
		}

		return nn;
	}

	public int[][] numNodes() {
		int numLevels = numLevels();
		int[][] nn = new int[size][numLevels];

		OrderedIntArraySet os = new OrderedIntArraySet();
		OrderedIntArraySet tp = new OrderedIntArraySet();
		OrderedIntArraySet tmp;
		int[] indices;
		for (int n = 0, i, j; n < size; n++) {
			// for each customer
			tp.clear();
			tp.add(n);
			for (i = 1; i < m_Values.length; i++) {
				tmp = os;
				os = tp;
				tp = tmp;
				tp.clear();
				for (j = 0; j < os.size(); j++) {
					indices = m_Values[i].findAllIndicesForKey(m_Values[i - 1].getKeyAt(os.getValueAt(j)));
					tp.addAll(indices);
				}
				nn[n][i - 1] = tp.size();
			}
		}

		return nn;
	}

	@Override
	public int[] getAccepted(int... key) {
		return m_Values[key.length].getKeyUnder(key);
	}

	public ProductTree<TreeNode> getProductTree() {
		return (ProductTree<TreeNode>) m_Tree;
	}

	public PurTreeDataSet newDataSet() {
		return new PurTreeDataSet(getProductTree());
	}

	@Override
	protected boolean _checkData() {
		OrderedComplexEmptyValueMap map = m_Values[m_Values.length - 1];
		int size = map.size();

		int[] key;
		int[] realKey = new int[map.numKeys() - 1];
		for (int i = 0, j; i < size; i++) {
			key = map.getKeyAt(i);
			for (j = 1; j < key.length; j++) {
				realKey[j - 1] = key[j];
			}
			// check real key
			if (m_Tree.getNodeWithID(realKey) == null) {
				return false;
			}
		}
		return true;
	}

	public PurTreeDataSet clone() {
		PurTreeDataSet pd = new PurTreeDataSet();
		pd.copy(this);
		pd.m_Values = new OrderedComplexEmptyValueMap[m_Values.length];
		for (int i = 0; i < pd.m_Values.length; i++) {
			pd.m_Values[i] = m_Values[i].clone();
		}
		return pd;
	}

	public void close() {
		super.close();
		for (int i = 0; i < m_Values.length; i++) {
			m_Values[i].clear();
		}
	}

	public void destroy() {
		super.destroy();
		for (int i = 0; i < m_Values.length; i++) {
			m_Values[i].destroy();
		}
	}

}
