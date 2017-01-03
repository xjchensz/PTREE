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
package xjc.covertree.levelnode;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author xiaojun chen
 *
 */
public interface ILevelNodeCreator {

	public ILevelNode createRootNode(int instance);
	
	public ILevelNode createRootNode(int instance,int level);

//	public ILevelNode createParentNode(ILevelNode child);
	
	public ILevelNode createParentNode(ILevelNode child,int level);

	//public ILevelNode createChildNode(ILevelNode parent, int instance);
	
	public ILevelNode createChildNode(ILevelNode parent, int instance,int level);

	public ILevelNode copyCreateNode(ILevelNode node, boolean recursive);

	public ILevelNode read(DataInput in) throws IOException;
}
