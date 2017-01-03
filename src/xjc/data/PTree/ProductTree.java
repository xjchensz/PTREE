/**
 * 
 */
package xjc.data.PTree;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import common.IWritable;
import common.utils.ArrayUtils;
import common.utils.collection.ArrayMap;
import common.utils.collection.ArrayQueue;
import common.utils.collection.ORDER;
import common.utils.collection.OrderedIntArraySet;
import common.utils.collection.OrderedIntMap;
import common.utils.collection.STATUS;

/**
 * @author xiaojun chen
 *
 */
public class ProductTree<M extends ITreeNode> implements IWritable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4474692954082716439L;
	private M m_Root;
	private ITreeNodeCreator<M> m_Creator;
	private int[] m_NumNodes;
	private HashMap<String, M> leafNodes;

	private int m_NumLevels;

	public ProductTree(File dir) throws IOException {
		// load tree
		DataInputStream dis = new DataInputStream(new FileInputStream(new File(dir, "stree.bin")));
		readFields(dis);
		dis.close();
	}

	public ProductTree(DataInput dis) throws IOException {
		readFields(dis);
	}

	public ProductTree(ProductTree<? extends M> tree) throws IOException {
		m_Creator = (ITreeNodeCreator<M>) tree.m_Creator;
		m_Root = m_Creator.copyCreateNode(null, (M) tree.m_Root, true);
		m_NumNodes = tree.m_NumNodes.clone();
		m_NumLevels = tree.numLevels();
		mapLeafNodes();
	}

	public ProductTree(ITreeNodeCreator<M> creator) {
		this(creator.createRootNode(), creator);
	}

	public ProductTree(M root, ITreeNodeCreator<M> creator) {
		m_Creator = creator;
		m_Root = root;
		leafNodes = new HashMap<String, M>();
	}

	public void add(ArrayMap<String, String>[] props, String... keys) {

		M parent = m_Root, child;
		int index;
		for (int i = 0; i < keys.length; i++) {
			index = parent.indexOfChild(keys[i]);
			if (index < 0) {
				child = m_Creator.createChildNode(parent, keys[i], props == null ? null : props[i]);
				index = parent.indexOfChild(keys[i]);
			} else {
				child = (M) parent.getChild(index);
			}
			parent = child;
		}

		leafNodes.put(keys[keys.length - 1], parent);

		if (m_NumLevels < keys.length) {
			m_NumLevels = keys.length;
		}
	}

	public M getNode(int... index) {
		M parent = m_Root, child;
		for (int i = 0; i < index.length; i++) {
			child = (M) parent.getChild(index[i]);
			parent = child;
		}
		return parent;
	}

	public M getNodeWithID(int... keys) {
		M parent = m_Root, child;
		for (int i = 0; i < keys.length; i++) {
			child = (M) parent.getChildWithID(keys[i]);
			parent = child;
		}
		return parent;
	}

	public String[] getKeys(int... index) {
		String[] keys = new String[index.length];
		M parent = m_Root, child;
		for (int i = 0; i < index.length; i++) {
			child = (M) parent.getChild(index[i]);
			keys[i] = child.getKey();
			parent = child;
		}
		return keys;
	}

	public void prune(OrderedIntArraySet leafNodeIndices) {

		Iterator<M> itr = leafNodes.values().iterator();
		M node;
		LinkedList<M> parents = new LinkedList();
		while (itr.hasNext()) {
			node = itr.next();
			if (!leafNodeIndices.containsValue(node.getID())) {
				if (!parents.contains(node.getParent())) {
					parents.add((M) node.getParent());
				}
				// remove
				node.destroy();
			}
		}

		// recursively process parents without children
		while (parents.size() > 0) {
			node = (M) parents.removeFirst();
			if (node.numChildren() == 0) {
				if (!parents.contains(node.getParent())) {
					parents.add((M) node.getParent());
				}
				// remove this node
				node.destroy();
			}
		}

		update();
	}

	public int[] getFullKey(String leafKey) {
		M leafNode = leafNodes.get(leafKey);

		int[] keys = new int[m_NumLevels];
		M parent, child = leafNode;
		for (int i = keys.length - 1; i >= 0; i--) {
			parent = (M) child.getParent();
			keys[i] = child.getID();
			child = parent;
		}

		return keys;
	}

	public int numNodes(int... keys) {
		if (keys.length == 0) {
			return 0;
		}

		M child = m_Root;

		for (int i = 0; i < keys.length; i++) {
			child = (M) child.getChild(keys[i]);
		}
		return child.numChildren();
	}

	public double childrenPercentInlevel(int... keys) {
		if (keys.length == 0) {
			return 1;
		}
		int numNodes = numNodes(keys);
		return (double) numNodes / m_NumNodes[keys.length];
	}

	public void save(OutputStream out) throws IOException {
		DataOutput dos = null;
		if (out instanceof DataOutput) {
			dos = (DataOutput) out;
		} else {
			dos = new DataOutputStream(out);
		}

		write(dos);
	}

	public ITreeNodeCreator<M> getTreeNodeCreator() {
		return m_Creator;
	}

	public Class<ITreeNodeCreator<M>> getCreatorClass() {
		return (Class<ITreeNodeCreator<M>>) m_Creator.getClass();
	}

	public M getRoot() {
		return m_Root;
	}

	public int numLevels() {
		return m_NumLevels;
	}

	public int[] numNodes() {
		return m_NumNodes;
	}

	public int numLeafNodes() {
		return m_NumNodes[m_NumNodes.length - 1];
	}

	public int numTotalNodes() {
		int nn = 0;
		for (int i = 0; i < m_NumNodes.length; i++) {
			nn += m_NumNodes[i];
		}
		return nn;
	}

	private NodeIndex m_NodeIndex;

	public NodeIndex getNodeIndex() {
		if (m_NodeIndex == null) {
			int[][] ids = new int[numLevels()][];
			int size = 1;
			ids[0] = new int[1];
			for (int i = 0; i < ids.length - 1; i++) {
				size += m_NumNodes[i];
				ids[i + 1] = new int[m_NumNodes[i]];
			}
			OrderedIntMap childrenIDS = new OrderedIntMap(STATUS.REPEATABLE, size);
			OrderedIntMap nodeMap = new OrderedIntMap(ORDER.ASC, STATUS.DISTINCT, size);

			ArrayQueue<ITreeNode> m_Nodes = new ArrayQueue<ITreeNode>(10, ITreeNode.class);
			m_Nodes.add(m_Root);
			ITreeNode tn;
			List<? extends ITreeNode> children;
			int index = 0;
			int[][] key = new int[size][];
			for (int i = 0, j; i < ids.length; i++) {
				size = m_Nodes.size();
				for (j = 0; j < size; j++) {
					tn = m_Nodes.remove();
					ids[i][j] = index;
					nodeMap.put(tn.getID(), index);
					if (i > 0) {
						childrenIDS.put(nodeMap.get(tn.getParent().getID()), index);
					}

					children = tn.getChildren();
					if (i < ids.length - 1) {
						for (ITreeNode child : children) {
							m_Nodes.add(child);
						}
					}

					if (tn.getParent() == null) {
						key[index] = new int[0];
					} else {
						key[index] = ArrayUtils.combine(key[nodeMap.get(tn.getParent().getID())], tn.getID());
					}

					index++;
				}
			}
			m_NodeIndex = new NodeIndex(ids, childrenIDS, nodeMap, key);
		}

		return m_NodeIndex;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(m_NumLevels);
		for (int i = 0; i < m_NumLevels; i++) {
			out.writeInt(m_NumNodes[i]);
		}
		out.writeUTF(getCreatorClass().getName());
		m_Root.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_NumLevels = in.readInt();
		m_NumNodes = new int[m_NumLevels];
		for (int i = 0; i < m_NumLevels; i++) {
			m_NumNodes[i] = in.readInt();
		}
		Class<ITreeNodeCreator<M>> classz;
		try {
			classz = (Class<ITreeNodeCreator<M>>) Class.forName(in.readUTF());
			m_Creator = classz.newInstance();
			m_Root = m_Creator.loadNode(in);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IOException(e);
		}

		mapLeafNodes();
	}

	private void mapLeafNodes() {
		// map leaf nodes
		leafNodes = new HashMap<String, M>();

		Stack<M> stack = new Stack();
		stack.add(m_Root);

		M node;
		while (!stack.empty()) {
			node = stack.pop();
			if (node.numChildren() == 0) {
				leafNodes.put(node.getKey(), node);
			} else {
				// push
				for (int i = node.numChildren() - 1; i >= 0; i--) {
					stack.push((M) node.getChild(i));
				}
			}
		}
		stack = null;
	}

	public String toXML() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		m_Root.toXML(sb);
		return sb.toString();
	}

	public void saveXML(Writer writer) throws IOException {
		writer.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
		writer.append(toXML());
	}

	public void saveXML(File file) throws IOException {
		// save tree
		FileWriter writer = new FileWriter(file);
		saveXML(writer);
		writer.close();
	}

	public void save(File dir) throws IOException {
		// save tree
		FileWriter writer = new FileWriter(new File(dir, "ptree.xml"));
		saveXML(writer);
		writer.close();

		// save creator
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(dir, "stree.bin")));
		write(dos);
		dos.close();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ProductTree)) {
			return false;
		}
		ProductTree<M> st = (ProductTree<M>) obj;

		return st.m_NumLevels == m_NumLevels && m_Root.equals(st.m_Root);
	}

	public void update() {
		m_Root.update();

		ArrayList<ITreeNode> last = new ArrayList<ITreeNode>();
		last.add(m_Root);
		ArrayList<ITreeNode> current = new ArrayList<ITreeNode>();
		ArrayList<ITreeNode> tmp;

		m_NumNodes = new int[m_NumLevels];
		int size;
		List<? extends ITreeNode> children;
		for (int i = 0, j, k; i < m_NumLevels; i++) {
			size = last.size();
			for (j = 0; j < size; j++) {
				children = last.get(j).getChildren();
				if (children != null && children.size() > 0) {
					for (k = 0; k < children.size(); k++) {
						current.add(children.get(k));
					}
				}
			}
			m_NumNodes[i] = current.size();
			tmp = last;
			last = current;
			current = tmp;
			current.clear();
		}
		last.clear();
		current.clear();
		last = null;
		current = null;
	}

	public ProductTree<M> createTree() {
		return new ProductTree<M>(m_Creator);
	}

	public ProductTree<M> clone() {
		try {
			return new ProductTree<M>(this);
		} catch (IOException e) {
			return null;
		}
	}

	public void destroy() {
		m_Root.destroy();
		m_Root = null;
		m_Creator.destroy();
	}

	public String statisInfor() {
		StringBuilder sb = new StringBuilder();

		sb.append(m_NumLevels).append(" levels.\n");

		ArrayList<ITreeNode> last = new ArrayList<ITreeNode>();
		last.add(m_Root);
		ArrayList<ITreeNode> current = new ArrayList<ITreeNode>();
		ArrayList<ITreeNode> tmp;

		int size;
		List<? extends ITreeNode> children;
		for (int i = 0, j, k; i < m_NumLevels; i++) {
			size = last.size();
			for (j = 0; j < size; j++) {
				children = last.get(j).getChildren();
				if (children != null && children.size() > 0) {
					for (k = 0; k < children.size(); k++) {
						current.add(children.get(k));
					}
				}
			}
			sb.append(1 + 1).append(" layer with ").append(current.size()).append(" nodes.\n");
			tmp = last;
			last = current;
			current = tmp;
			current.clear();
		}

		return sb.toString();
	}
}
