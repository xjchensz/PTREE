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
import java.io.IOException;

import common.IWritable;
import common.utils.collection.ArrayMap;

/**
 * @author xiaojun chen
 *
 */
public interface ITreeNodeCreator<M extends ITreeNode> extends IWritable {

	public int numLevels();

	public M createRootNode();

	public M createChildNode(M parent, String key, ArrayMap<String, String> prop);

	public M copyCreateNode(M parent, M node, boolean recursive);

	public M loadNode(DataInput input) throws IOException;

	public Class<M> getNodeClass();

	public void destroy();

}
