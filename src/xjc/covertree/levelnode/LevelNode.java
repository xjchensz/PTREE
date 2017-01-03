package xjc.covertree.levelnode;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.EqualsUtils;
import common.utils.collection.OrderedIntArrayList;

public class LevelNode implements ILevelNode {


		private static final long serialVersionUID = 2668816567355553585L;
		
		private int m_level=500;

		static int SID = 0;

		private int m_ID;

		private int m_Instance;

		private ArrayList<ILevelNode> m_Children;

		private int m_AllChildren;

		private int numDistinctChildren;

		protected LevelNode() {
		}

/*		public LevelNode(int instance) {
			m_ID = SID++;
			m_Instance = instance;
		}*/
		
		public LevelNode(int instance, int level) {
			m_ID = SID++;
			m_Instance = instance;
			m_level=level;
		}

		public LevelNode(ILevelNode node, boolean recursive) {
			m_ID = node.getID();
			m_Instance = node.getInstance();

			if (recursive) {
				if (node.numChildren() > 0) {
					m_Children = new ArrayList<ILevelNode>(node.numChildren());
					List<ILevelNode> children = node.getChildren();
					for (int i = 0; i < node.numChildren(); i++) {
						m_Children.add(new LevelNode(children.get(i), recursive));
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
		
		@Override
		public int getLevel() {
			// TODO Auto-generated method stub
			return m_level;
		}
		
		@Override
		public void setLevel(int level) {
			m_level=level;
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
		public void addChild(ILevelNode child) {
			if (m_Children == null) {
				m_Children = new ArrayList<ILevelNode>();
			}
			m_Children.add(child);
		}

		@Override
		public List<ILevelNode> getChildren() {
			return m_Children;
		}

		@Override
		public void removeChild(ILevelNode child) {
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
			out.writeInt(m_level);
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
			m_level=in.readInt();
			int size = in.readInt();
			m_Children = new ArrayList<ILevelNode>(size);
			LevelNode node;
			for (int i = 0; i < size; i++) {
				node = new LevelNode();
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
				ILevelNode node;
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
		public int compareTo(ILevelNode o) {
			return numDistinctChildren() - o.numDistinctChildren();
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof LevelNode)) {
				return false;
			}
			LevelNode node = (LevelNode) obj;

			return m_ID == node.m_ID && m_Instance == node.m_Instance && m_AllChildren == node.m_AllChildren
					&& EqualsUtils.equalsInOrder(m_Children, node.m_Children);
		}



	

	}

