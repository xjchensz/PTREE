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

import java.util.List;

import common.IWritable;


public interface ILevelNode extends IWritable, Comparable<ILevelNode> {
	public void setLevel(int level);
	
	public int getLevel();
	
	public int getID();

	public int getInstance();

	public void update();

	public int numAllChildren();

	public int numDistinctChildren();

	public List<ILevelNode> getChildren();

	public void attach(int ins);

	public int[] getAttached();

	public boolean hasAttach();

	public void addChild(ILevelNode child);

	public void removeChild(ILevelNode child);

	public void removeChildren();

	public int numChildren();

	public void destroy();

	public void destroyAll();
}
