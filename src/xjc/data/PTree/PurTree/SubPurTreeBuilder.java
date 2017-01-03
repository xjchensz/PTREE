/**
 * 
 */
package xjc.data.PTree.PurTree;

import common.utils.collection.ArrayMap;
import xjc.data.PTree.ITreeNode;
import xjc.data.PTree.ProductTree;

/**
 * @author xiaojun chen
 *
 */
public class SubPurTreeBuilder {

	private ProductTree m_Tree;

	private ProductTree m_SubTree;

	/**
	 * 
	 */
	public SubPurTreeBuilder(ProductTree tree) {
		m_Tree = tree;
		m_SubTree = m_Tree.createTree();
	}

	public void keyAdd(int... keys) {
		String[] strKeys = new String[keys.length];
		ITreeNode parent = m_Tree.getRoot(), child;
		ArrayMap<String, String>[] props = new ArrayMap[keys.length];
		for (int i = 0; i < keys.length; i++) {
			child = parent.getChildWithID(keys[i]);
			if (child == null) {
				System.out.println();
			}
			strKeys[i] = child.getKey();
			props[i] = child.getproperties();
			parent = child;
		}

		m_SubTree.add(props, strKeys);
	}

	public ProductTree getSubTree() {
		m_SubTree.update();
		return m_SubTree;
	}

	public void reset() {
		m_SubTree = null;
		m_SubTree = m_Tree.createTree();
	}
}
