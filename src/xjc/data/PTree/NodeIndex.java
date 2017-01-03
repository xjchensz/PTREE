package xjc.data.PTree;

import common.utils.collection.OrderedIntMap;

public class NodeIndex {

	/** the index of the node in the node weight array */
	private int[][] m_WeightIndex;
	/** the location of the children node in the m_WeightIndex */
	private OrderedIntMap m_ChildrenID;
	/** map the node id to the index in the m_WeightIndex */
	private OrderedIntMap m_NodeIDMap;
	private int[][] key;

	public NodeIndex(int[][] lid, OrderedIntMap childrenID,
			OrderedIntMap nodeIDMap, int[][] key) {
		this.m_WeightIndex = lid;
		this.m_ChildrenID = childrenID;
		this.m_NodeIDMap = nodeIDMap;
		this.key = key;
	}

	public int[][] getWeightIndex() {
		return m_WeightIndex;
	}

	public OrderedIntMap getChildrenID() {
		return m_ChildrenID;
	}

	public OrderedIntMap getNodeIDMap() {
		return m_NodeIDMap;
	}

	public int[][] getKey() {
		return key;
	}
}
