/**
 * 
 */
package xjc.covertree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.EqualsUtils;
import common.utils.collection.OrderedIntArrayList;

/**
 * @author xiaojun chen
 *
 */
public class Node implements INode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2668816567355553585L;

	static int SID = 0;

	private int m_ID;

	private int m_Instance;

	private ArrayList<INode> m_Children;

	private int m_AllChildren;

	private int numDistinctChildren;

	protected Node() {
	}

	public Node(int instance) {
		m_ID = SID++;
		m_Instance = instance;
	}

	public Node(INode node, boolean recursive) {
		m_ID = node.getID();
		m_Instance = node.getInstance();

		if (recursive) {
			if (node.numChildren() > 0) {
				m_Children = new ArrayList<INode>(node.numChildren());
				List<INode> children = node.getChildren();
				for (int i = 0; i < node.numChildren(); i++) {
					m_Children.add(new Node(children.get(i), recursive));
				}
			}
			m_AllChildren = node.numAllChildren();
			numDistinctChildren = node.numDistinctChildren();
		}
	}

	@Override
	public int getID() {
		return m_ID;
	}

	@Override
	public int getInstance() {
		return m_Instance;
	}

	private OrderedIntArrayList os;

	@Override
	public void attach(int ins) {
		if (os == null) {
			os = new OrderedIntArrayList();
		}
		os.add(ins);
	}

	@Override
	public int[] getAttached() {
		if (os != null) {
			return os.values();
		}
		return new int[0];
	}

	@Override
	public boolean hasAttach() {
		return os != null && os.size() > 0;
	}

	@Override
	public void addChild(INode child) {
		if (m_Children == null) {
			m_Children = new ArrayList<INode>();
		}
		m_Children.add(child);
	}

	@Override
	public List<INode> getChildren() {
		return m_Children;
	}

	@Override
	public void removeChild(INode child) {
		if (m_Children != null) {
			m_Children.remove(child);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(m_ID);
		out.writeInt(m_Instance);
		out.writeInt(m_AllChildren);
		out.writeInt(numDistinctChildren);
		int size = numChildren();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
			m_Children.get(i).write(out);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_ID = in.readInt();
		m_Instance = in.readInt();
		m_AllChildren = in.readInt();
		numDistinctChildren = in.readInt();
		int size = in.readInt();
		m_Children = new ArrayList<INode>(size);
		Node node;
		for (int i = 0; i < size; i++) {
			node = new Node();
			node.readFields(in);
			m_Children.add(node);
		}
	}

	@Override
	public int numChildren() {
		return m_Children == null ? 0 : m_Children.size();
	}

	@Override
	public void destroy() {
		if (m_Children != null) {
			m_Children.clear();
		}
		m_Children = null;
	}

	@Override
	public void destroyAll() {
		if (m_Children != null) {
			for (int i = m_Children.size() - 1; i >= 0; i--) {
				m_Children.get(i).destroyAll();
			}
			m_Children.clear();
			m_Children = null;
		}
	}

	@Override
	public void removeChildren() {
		if (m_Children != null) {
			for (int i = m_Children.size() - 1; i >= 0; i--) {
				m_Children.get(i).destroyAll();
			}
			m_Children.clear();
			m_Children = null;
		}
	}

	@Override
	public void update() {
		m_AllChildren = 0;
		numDistinctChildren = 0;
		if (m_Children != null) {
			INode node;
			int instance = getInstance();
			for (int i = m_Children.size() - 1; i >= 0; i--) {
				node = m_Children.get(i);
				node.update();
				m_AllChildren += node.numAllChildren() + 1;
				if (node.getInstance() == instance) {
					numDistinctChildren += node.numDistinctChildren();
				} else {
					numDistinctChildren += node.numDistinctChildren() + 1;
				}
			}
		}
	}

	@Override
	public int numAllChildren() {
		return m_AllChildren;
	}

	@Override
	public int numDistinctChildren() {
		return numDistinctChildren;
	}

	@Override
	public int compareTo(INode o) {
		return numDistinctChildren() - o.numDistinctChildren();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Node)) {
			return false;
		}
		Node node = (Node) obj;

		return m_ID == node.m_ID && m_Instance == node.m_Instance && m_AllChildren == node.m_AllChildren
				&& EqualsUtils.equalsInOrder(m_Children, node.m_Children);
	}

}
