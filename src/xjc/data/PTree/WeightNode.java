/**
 * 
 */
package xjc.data.PTree;

import java.util.ArrayList;

/**
 * @author Xiaojun Chen 2016Äê3ÔÂ8ÈÕ
 */
public class WeightNode {
	int index;
	int nodeID;
	WeightNode nextNode;
	ArrayList<WeightNode> m_Children;

	public WeightNode(int index, int nodeID, WeightNode nextNode,
			ArrayList<WeightNode> m_Children) {
		this.index = index;
		this.nodeID = nodeID;
		this.nextNode = nextNode;
		this.m_Children = m_Children;
	}

}
