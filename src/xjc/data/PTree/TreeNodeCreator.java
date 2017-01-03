/**
 * This class provides a Java version of the cover tree nearest neighbor algorithm.
 * It is based on Thomas Kollar's version of "Cover Trees for Nearest Neighbor" by 
 * Langford, Kakade, Beygelzimer (2007). 
 * 
 * Date of creation: 2013-02-08
 * Copyright (c) 2015, Xiaojun Chen
 * 
 * The software is provided 'as-is', without any express or implied
 * warranty. In no event will the author be held liable for any damages
 * arising from the use of this software. Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it freely.
 * 
 * @author Xiaojun Chen
 *
 */
package xjc.data.PTree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import common.utils.collection.ArrayMap;

/**
 * @author xiaojun chen
 *
 */
public class TreeNodeCreator implements ITreeNodeCreator<TreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4049514405853955517L;
	private int SID;
	private String[] m_LevelNames;

	public TreeNodeCreator() {
	}

	public TreeNodeCreator(String[] levelNames) {
		m_LevelNames = levelNames;
	}

	public TreeNode createRootNode() {
		return new TreeNode("Root", null, -1, "", null);
	}

	@Override
	public TreeNode createChildNode(TreeNode parent, String key, ArrayMap<String, String> prop) {
		int levelIndex = parent.getLevel() - 1;
		String lname = null;
		if (m_LevelNames != null && m_LevelNames.length > levelIndex) {
			lname = m_LevelNames[levelIndex];
		}
		if (lname == null) {
			lname = "noname";
		}

		TreeNode child = new TreeNode(lname, parent, SID++, key,
				prop == null ? null : new ArrayMap<String, String>(prop));
		parent.addChild(child);
		return child;
	}

	@Override
	public TreeNode copyCreateNode(TreeNode parent, TreeNode node, boolean recursive) {
		TreeNode nn = new TreeNode(parent, node);
		if (recursive) {
			int nc = node.numChildren();
			if (nc > 0) {
				TreeNode child;
				for (int i = 0; i < nc; i++) {
					child = (TreeNode) node.getChild(i);
					// recursively create children
					nn.addChild(copyCreateNode(nn, child, true));
				}
			}
		}

		return nn;
	}

	@Override
	public TreeNode loadNode(DataInput input) throws IOException {
		return TreeNode.readNode(input, null);
	}

	@Override
	public int numLevels() {
		return m_LevelNames.length;
	}

	@Override
	public void destroy() {
		m_LevelNames = null;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(SID);
		out.writeInt(m_LevelNames.length);
		for (int i = 0; i < m_LevelNames.length; i++) {
			out.writeUTF(m_LevelNames[i]);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		SID = in.readInt();
		int size = in.readInt();
		m_LevelNames = new String[size];
		for (int i = 0; i < size; i++) {
			m_LevelNames[i] = in.readUTF();
		}
	}

	@Override
	public Class<TreeNode> getNodeClass() {
		return TreeNode.class;
	}

}
