/**
 * 
 */
package xjc.data.PTree;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import common.IWritable;
import common.utils.ArrayUtils;
import common.utils.StringUtils;

/**
 * @author xiaojun chen
 *
 */
public abstract class AbstractPTreeDataSet implements IWritable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3440963850623366600L;

	private HashMap<String, Integer> dataIDs;
	private HashMap<Integer, String> inverseDataIDs;
	protected ProductTree<TreeNode> m_Tree;

	protected int m_NumLevels;
	protected int size;

	protected AbstractPTreeDataSet() {
	}

	public AbstractPTreeDataSet(ITreeNodeCreator<TreeNode> treeNodeCreator) {
		this(new ProductTree<TreeNode>(treeNodeCreator));
		m_NumLevels = treeNodeCreator.numLevels();
	}

	public AbstractPTreeDataSet(ProductTree<TreeNode> tree) {
		m_Tree = tree;
		m_NumLevels = tree.numLevels();
		dataIDs = new HashMap<String, Integer>();
		inverseDataIDs = new HashMap<Integer, String>();
	}

	public AbstractPTreeDataSet(AbstractPTreeDataSet set, int[] selected) {
		m_Tree = set.m_Tree.clone();
		m_NumLevels = set.m_NumLevels;
		dataIDs = new HashMap<String, Integer>();
		inverseDataIDs = new HashMap<Integer, String>();

		Iterator<Entry<String, Integer>> itr = set.dataIDs.entrySet().iterator();
		Entry<String, Integer> entry;
		int i = 0, ptr = 0;
		Arrays.sort(selected);
		while (itr.hasNext() && ptr < selected.length) {
			entry = itr.next();
			if (selected[ptr] == i++) {
				dataIDs.put(entry.getKey(), ptr);
				inverseDataIDs.put(ptr, entry.getKey());
				ptr++;
			}
		}
		size = ptr;
	}

	public AbstractPTreeDataSet(AbstractPTreeDataSet set) {
		m_Tree = set.m_Tree.clone();
		m_NumLevels = set.m_NumLevels;
		dataIDs = new HashMap<String, Integer>(set.dataIDs);
		inverseDataIDs = new HashMap<Integer, String>(set.inverseDataIDs);
		size = set.size;
	}

	public abstract void init();

	public int numLevels() {
		return m_NumLevels;
	}

	public ProductTree<? extends ITreeNode> getProductTree() {
		return m_Tree;
	}

	public int getDataID(String dataID) {
		Integer id = dataIDs.get(dataID);
		return id != null ? id : -1;
	}

	public String getDataByID(int id) {
		return inverseDataIDs.get(id);
	}

	protected int[] addKey(String dataID, String productKey) {
		Integer id = dataIDs.get(dataID);
		if (id == null) {
			id = dataIDs.size();
			dataIDs.put(dataID, id);
			inverseDataIDs.put(id, dataID);
		}
		return ArrayUtils.combine(id, m_Tree.getFullKey(productKey));
	}

	public void update() {
		m_Tree.update();
		size = innerUpdate();
	}

	public int size() {
		return size;
	}

	protected abstract int innerUpdate();

	public void save(File dir) throws IOException {

		if (!dir.exists()) {
			dir.mkdirs();
		}

		// save tree
		FileWriter writer = new FileWriter(new File(dir, "stree.xml"));
		m_Tree.saveXML(writer);
		writer.close();

		// save dataIDs
		writer = new FileWriter(new File(dir, "dataID.csv"));
		writer.append("id,dataID\n");

		Iterator<Entry<String, Integer>> itr = dataIDs.entrySet().iterator();
		Entry<String, Integer> entry;

		while (itr.hasNext()) {
			entry = itr.next();
			writer.append(String.valueOf(entry.getValue())).append(',').append(entry.getKey()).append('\n');
		}

		writer.close();

		// save data
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(dir, "data")));
		write(dos);
		dos.close();

		_save(dir);
	}

	public void saveBinaryData(File file) throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(file, "data")));
		write(dos);
		dos.close();
	}

	public boolean checkData(File dir) throws IOException {
		HashMap<String, Integer> dids = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(new File(dir, "dataID.csv")));
		br.readLine();
		String line;
		String[] kv;
		while ((line = br.readLine()) != null) {
			kv = StringUtils.splitTwoPart(line, ',');
			dids.put(kv[1], Integer.parseInt(kv[0]));
		}
		br.close();

		if (!dids.equals(dataIDs)) {
			return false;
		}

		return _checkData();
	}

	protected abstract boolean _checkData();

	protected abstract void _save(File dir) throws IOException;

	public abstract AbstractPTreeDataSet sample(int[] selected) throws IOException;

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(m_Tree.getClass().getName());
		m_Tree.write(out);

		int size = dataIDs.size();
		out.writeInt(size);
		Iterator<Entry<String, Integer>> itr = dataIDs.entrySet().iterator();
		Entry<String, Integer> entry;
		while (itr.hasNext()) {
			entry = itr.next();
			out.writeUTF(entry.getKey());
			out.writeInt(entry.getValue());
		}

		out.writeInt(m_NumLevels);

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		try {
			Class<?> classz = Class.forName(in.readUTF());
			Constructor<?> cons = classz.getConstructor(DataInput.class);
			m_Tree = (ProductTree<TreeNode>) cons.newInstance(in);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IOException(e);
		}

		size = in.readInt(); //读取四个输入字节保存成整数的形式
		dataIDs = new HashMap<String, Integer>(size);
		inverseDataIDs = new HashMap<Integer, String>(size);
		String dataID;
		int id;
		for (int i = 0; i < size; i++) {
			dataID = in.readUTF();
			id = in.readInt();
			dataIDs.put(dataID, id);
			inverseDataIDs.put(id, dataID);
		}
		m_NumLevels = in.readInt();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractPTreeDataSet)) {
			return false;
		}
		AbstractPTreeDataSet ds = (AbstractPTreeDataSet) obj;
		return dataIDs.equals(ds.dataIDs) && m_Tree.equals(ds.m_Tree) && m_NumLevels == ds.m_NumLevels;
	}

	protected void copy(AbstractPTreeDataSet dataset) {
		dataIDs = (HashMap<String, Integer>) dataset.dataIDs.clone();
		inverseDataIDs = (HashMap<Integer, String>) dataset.inverseDataIDs.clone();
		m_Tree = dataset.m_Tree;
		m_NumLevels = dataset.m_NumLevels;
		size = dataset.size;
	}

	public void close() {
		m_Tree = null;
	}

	public void destroy() {
		m_Tree = null;
	}
}
