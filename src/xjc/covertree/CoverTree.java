package xjc.covertree;

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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import common.IWritable;
import common.utils.collection.ORDER;
import common.utils.collection.OrderedDoubleMap;
import common.utils.collection.OrderedIntArraySet;
import common.utils.collection.OrderedIntMap;
import common.utils.collection.STATUS;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;

public class CoverTree implements IWritable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8478288273931250659L;

	private INodeCreator m_NodeCreator;
	private IDistanceHolder m_DistanceHolder;

	public static final double DEFAULT_ALPHA = 1.619;

	int maxLevel;
	int minLevel;
	double base = 2;
	INode rootNode;

	int maxNumLevels = 500;
	int minNumLevels = -500;

	private int[] numLevels;
	private double maxDist;

	private ArrayList<INode> tmpNodes1 = new ArrayList<INode>();
	private ArrayList<INode> tmpNodes2 = new ArrayList<INode>();
	private ArrayList<INode> tmpNodes3 = new ArrayList<INode>();
	private ArrayList<INode> tmpNodes4 = new ArrayList<INode>();
	private OrderedDoubleMap tmpMap = new OrderedDoubleMap();
	private OrderedIntArraySet os = new OrderedIntArraySet();

	public CoverTree(CoverTree ct, IDistanceHolder distanceHolder) {
		m_DistanceHolder = distanceHolder;
		tmpMap.setEmptyValue(-1);

		m_NodeCreator = ct.m_NodeCreator;
		base = ct.base;
		maxLevel = ct.maxLevel;
		minLevel = ct.minLevel;
		maxNumLevels = ct.maxNumLevels;
		minNumLevels = ct.minNumLevels;
		numLevels = ct.numLevels.clone();
		maxDist = ct.maxDist;

		rootNode = m_NodeCreator.copyCreateNode(ct.rootNode, true);
	}

	private CoverTree(IDistanceHolder distanceHolder) {
		m_DistanceHolder = distanceHolder;
		tmpMap.setEmptyValue(-1);
	}

	/**
	 * Create a cover tree at level zero which automatically expands above and
	 * below.
	 */
	CoverTree(INodeCreator creator, IDistanceHolder distanceHolder) {
		this(creator, distanceHolder, DEFAULT_ALPHA, 500, -500);
	}

	/**
	 * Create a cover tree which stops increasing the minimumLevel as soon as
	 * the given number of nodes is reached.
	 * 
	 * * @param maxNumLevels the minimum levels of the cover tree by defining
	 * the maximum exponent of the base (default = 500).
	 * 
	 * @param minNumLevels
	 *            * Set the minimum levels of the cover tree by defining the
	 *            minimum exponent of the base (default = -500).
	 */
	CoverTree(INodeCreator creator, IDistanceHolder distanceHolder, double base, int maxNumLevels, int minNumLevels) {
		m_NodeCreator = creator;
		m_DistanceHolder = distanceHolder;
		if (base >= 1.619) {
			this.base = base;
		}
		tmpMap.setEmptyValue(-1);
		this.maxNumLevels = maxNumLevels;
		this.minNumLevels = minNumLevels;
		initialize();
	}

	/**
	 * Returns the base of this tree.
	 * 
	 * @return
	 */
	public double getBase() {
		return base;
	}

	/**
	 * Returns the maximum level of this tree.
	 * 
	 * @return
	 */
	public int maxLevel() {
		return maxLevel;
	}

	/**
	 * Returns the minimum level of this tree.
	 * 
	 * @return
	 */
	public int minLevel() {
		return minLevel;
	}

	private void initialize() {
		maxDist = Math.pow(base, maxLevel);
		numLevels = new int[maxNumLevels - minNumLevels];
	}

	/**
	 * Returns the size of the cover tree up to the given level (inclusive)
	 * 
	 * @param level
	 * @return
	 */
	public int size(int level) {
		int sum = 0;
		for (int i = maxLevel; i >= level; i--)
			sum += numLevels[maxNumLevels - i];
		return sum;
	}

	public int size() {
		return size(minLevel);
	}

	private void decNodes(int level) {
		numLevels[maxNumLevels - level]--;
	}

	private void incNodes(int level) {
		if (level < minNumLevels) {
			System.out.println();
		}
		numLevels[maxNumLevels - level]++;
		if (minLevel > level) {
			minLevel = level;
		}
	}

	private double insertAtRoot(int instance, double distance) {
		// inserts the point above the root by successively increasing the
		// cover of the root node until it
		// contains the new point, the old root is added as child of the new
		// root
		INode newRoot;

		int tempLevel;
		if (rootNode.numChildren() == 0) {
			tempLevel = (int) (Math.log(distance) / Math.log(base));
			if (Math.pow(base, tempLevel) < distance) {
				tempLevel++;
			}
			if (tempLevel > maxNumLevels) {
				return Double.NaN;
			}
			decNodes(maxLevel);
			maxLevel = tempLevel;
			incNodes(maxLevel);
			maxDist = Math.pow(base, maxLevel);
		} else {

			tempLevel = (int) (Math.log(distance) / Math.log(base));
			if (Math.pow(base, tempLevel) < distance) {
				tempLevel++;
			}
			if (tempLevel > maxNumLevels) {
				return Double.NaN;
			}
			double disThreshold = Math.pow(base, maxLevel);
			while (distance > disThreshold) {
				newRoot = m_NodeCreator.createParentNode(rootNode);
				decNodes(maxLevel);
				rootNode = newRoot;
				incNodes(++maxLevel);
				disThreshold *= base;
			}
			maxDist = disThreshold;
		}

		m_NodeCreator.createChildNode(rootNode, instance);
		incNodes(maxLevel - 1);
		return maxLevel - 1;
	}

	/**
	 * Insert an instance into the tree.
	 * 
	 * @return the level the instance inserted. Return NAN if the inserion
	 *         failed.
	 * 
	 * @param instance
	 */
	public double insert(int instance) {

		// if this is the first node make it the root node
		if (rootNode == null) {
			rootNode = m_NodeCreator.createRootNode(instance);
			incNodes(maxLevel);
			minLevel = maxLevel;
			return maxLevel;
		}
		// do not add if the new node is identical to the root node
		if (instance == rootNode.getInstance()) {
			return Double.NaN;
		}
		double distance = m_DistanceHolder.distance(rootNode.getInstance(), instance);
		if (distance == 0) {
			rootNode.attach(instance);
			return maxLevel;
		}

		// if the node lies outside the cover of the root node and its
		// decendants then insert the node above the root node

		if (distance > maxDist) {
			return insertAtRoot(instance, distance);
		}
		// tmpMap.clear();
		// tmpMap.put(rootNode.getInstance(), distance);
		int level = maxLevel;
		double distanceThreshold = maxDist;

		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes2.add(rootNode);
		List<INode> children;
		ArrayList<INode> tmp;
		INode child;
		boolean zerodistance = false;
		while (true) {
			// exchange tmpNodes1 and tmpNodes2
			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			// cover set
			for (int i = tmpNodes1.size() - 1, j; i >= 0 && !zerodistance; i--) {
				children = tmpNodes1.get(i).getChildren();
				if (children != null) {
					for (j = children.size() - 1; j >= 0; j--) {
						child = children.get(j);
						distance = m_DistanceHolder.distance(child.getInstance(), instance);
						if (distance == 0) {
							child.attach(instance);
							zerodistance = true;
							break;
						}
						// tmpMap.put(child.getInstance(), distance);
						if (distance < distanceThreshold) {
							tmpNodes2.add(child);
						}
					}
				}
			}

			if (zerodistance) {
				break;
			}

			if (tmpNodes2.isEmpty()) {
				// insert in the current level
				// find parent

				if (level == minNumLevels) {
					return Double.NaN;
				}

				INode nearestNode = null;
				double minDist = Double.MAX_VALUE;
				double dist;
				for (int i = tmpNodes1.size() - 1; i >= 0; i--) {
					dist = m_DistanceHolder.distance(tmpNodes1.get(i).getInstance(), instance);
					if (dist < minDist) {
						nearestNode = tmpNodes1.get(i);
						minDist = dist;
					}
				}

				m_NodeCreator.createChildNode(nearestNode, instance);
				incNodes(--level);
				break;
			}

			level--;
			distanceThreshold /= base;
		}
		// tmpMap.clear();
		tmpNodes1.clear();
		tmpNodes2.clear();
		return level;
	}

	public void update() {
		rootNode.update();
	}

	/**
	 * Removes the the cover at the lowest level of the tree.
	 */
	void removeLowestCover() {
		tmpNodes1.clear();
		tmpNodes1.add(rootNode);
		tmpNodes2.clear();
		int k = maxLevel;
		ArrayList<INode> tmp;
		while (k-- > minLevel + 1) {
			for (INode n : tmpNodes1)
				tmpNodes2.addAll(n.getChildren());
			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
		}
		for (INode n : tmpNodes2)
			n.removeChildren();

		minLevel++;
	}

	/**
	 * Retrieve the element from the tree that is nearest to the given point
	 * 
	 * @param instance
	 * @return
	 */
	public int getNearest(int instance) {

		double distance = m_DistanceHolder.distance(rootNode.getInstance(), instance);
		if (distance == 0) {
			return rootNode.getInstance();
		}
		double minDist = distance;
		int minIns = rootNode.getInstance();

		int[] attach;
		if (rootNode.hasAttach()) {
			attach = rootNode.getAttached();
			for (int i = 0; i < attach.length; i++) {
				distance = m_DistanceHolder.distance(attach[i], instance);
				if (distance == 0) {
					return attach[i];
				} else if (distance < minDist) {
					minDist = distance;
					minIns = attach[i];
				}
			}
		}

		double distanceThreshhold;

		double globalMinDist = distance;

		int parent, ins;
		ArrayList<INode> tmp;
		tmpNodes1.clear();
		tmpNodes1.add(rootNode);
		// tmpMap.put(rootNode.getInstance(), distance);
		tmpNodes2.clear();
		List<INode> children;
		for (int level = maxLevel, j, k, l; level >= minLevel; level--) {
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				parent = tmpNodes1.get(j).getInstance();
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						ins = children.get(k).getInstance();
						if (parent == ins) {
							continue;
						}
						distance = m_DistanceHolder.distance(ins, instance);
						if (distance == 0) {
							tmpNodes1.clear();
							tmpNodes2.clear();
							// tmpMap.clear();
							return ins;
						}
						if (distance < minDist) {
							minDist = distance;
						}
						// tmpMap.put(ins, distance);
					}
				}
			}

			distanceThreshhold = minDist + Math.pow(base, level);
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						ins = children.get(k).getInstance();
						distance = m_DistanceHolder.distance(ins, instance);
						// distance = tmpMap.get(ins);
						if (distance < distanceThreshhold) {
							tmpNodes2.add(children.get(k));

							if (distance > 0 && distance < globalMinDist) {
								globalMinDist = distance;
								minIns = ins;
							}
						}

						if (children.get(k).hasAttach()) {
							attach = children.get(k).getAttached();
							for (l = 0; l < attach.length; l++) {
								distance = m_DistanceHolder.distance(attach[l], instance);
								if (distance == 0) {
									tmpNodes1.clear();
									tmpNodes2.clear();
									// tmpMap.clear();
									return attach[l];
								} else if (distance < globalMinDist) {
									globalMinDist = distance;
									minIns = ins;
								}
							}
						}
					}
				}
			}

			tmp = tmpNodes2;
			tmpNodes2 = tmpNodes1;
			tmpNodes1 = tmp;
			tmpNodes2.clear();

			if (tmpNodes1.size() == 0) {
				break;
			}
		}
		tmpNodes1.clear();
		tmpNodes2.clear();
		// tmpMap.clear();
		return minIns;
	}

	public int[] getKNearestNeighbor(int instance, int k) {
		INode[] knnNodes = getKNearestNode(instance, k);
		int[] kNNs = new int[k];
		int nodeLength = 0;
		int[] nodeAttached;
		for (int i = 0, j, p = 0; i < k; i++, p++) {
			kNNs[i] = knnNodes[p].getInstance();
			if (!knnNodes[p].hasAttach())
				continue;

			nodeAttached = knnNodes[p].getAttached();
			nodeLength = nodeAttached.length;
			for (j = 0; j < nodeLength; j++) {
				if (i >= k - 1)
					break;
				kNNs[++i] = nodeAttached[j];
			}
		}

		tmpNodes3.clear();

		return kNNs;
	}

	/**
	 * Retrieve the element from the tree that is nearest to the given point
	 * 
	 * @param instance
	 *            the given instance
	 * @param k
	 *            the number of nearest neighborhoods
	 * @return
	 */
	public INode[] getKNearestNode(int instance, int k) {
		if (k < 1)
			return null;

		if (size() < k) {
			k = size();
		}

		double distance = m_DistanceHolder.distance(rootNode.getInstance(), instance);
		double distanceThreshhold;

		double minKDist = distance;// 锟斤拷锟斤拷锟斤拷锟斤拷牡锟終锟斤拷锟斤拷木锟斤拷锟�

		int parent, ins;// parent为锟斤拷锟节碉拷锟絠d
		ArrayList<INode> tmp;
		tmpNodes1.clear();
		tmpNodes1.add(rootNode);// 锟斤拷询锟斤拷Q
		tmpMap.put(rootNode.getInstance(), distance);
		tmpNodes3.add(rootNode);// 锟斤拷锟饺讹拷锟叫ｏ拷锟斤拷锟斤拷为K锟斤拷锟斤拷前锟斤拷锟斤拷诘锟斤拷锟斤拷询锟斤拷木锟斤拷锟斤拷锟斤拷蔚锟斤拷锟�
		tmpNodes2.clear();

		List<INode> children;// children为锟接节碉拷锟絠d锟斤拷锟斤拷
		for (int level = maxLevel, j, m; level >= minLevel; level--) {

			for (j = tmpNodes1.size() - 1; j >= 0; j--) {// 锟节诧拷询锟斤拷锟叫诧拷询确锟斤拷K锟斤拷锟斤拷锟斤拷锟�
				parent = tmpNodes1.get(j).getInstance();
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (m = children.size() - 1; m >= 0; m--) {// m为锟节碉拷锟斤拷咏诘锟斤拷锟斤拷
						ins = children.get(m).getInstance();// ins为锟斤拷前锟接节碉拷锟絠d
						if (parent == ins) {
							continue;
						}
						distance = m_DistanceHolder.distance(ins, instance);
						tmpMap.put(ins, distance);

						if (tmpNodes3.size() < k) {
							if (minKDist < distance) {// 锟斤拷锟斤拷锟饺讹拷锟斤拷锟叫碉拷元锟截诧拷锟斤拷k时锟斤拷锟斤拷要锟斤拷minKDIst锟斤拷锟斤拷为锟斤拷前锟斤拷锟饺讹拷锟斤拷锟叫碉拷锟斤拷远锟斤拷木锟斤拷锟�
								minKDist = distance;// 锟斤拷k锟斤拷锟斤拷锟斤拷木锟斤拷锟斤拷锟斤拷锟�
								tmpNodes3.add(children.get(m));
							} else {
								int index = tmpNodes3.size() - 1;
								int offset = 0;
								for (; index >= 0; index--)// 锟接猴拷锟斤拷前锟斤拷锟斤拷Node3锟叫的节点，锟斤拷锟斤拷锟斤拷锟侥节碉拷锟斤拷锟斤拷锟绞碉拷位锟矫★拷
								{
									if (tmpMap.get(tmpNodes3.get(index).getInstance()) < distance) {
										break;
									}
									++offset;
								}
								tmpNodes3.add(tmpNodes3.size() - offset, children.get(m));
							}
						} else {
							if (distance < minKDist) {// tmpMap.size==k

								int index = k - 1;
								int offset = 0;
								for (; index >= 0; index--)// 锟接猴拷锟斤拷前锟斤拷锟斤拷Node3锟叫的节点，锟斤拷锟斤拷锟斤拷锟侥节碉拷锟斤拷锟斤拷锟绞碉拷位锟矫★拷
								{
									if (tmpMap.get(tmpNodes3.get(index).getInstance()) < distance) {
										break;
									}
									++offset;
								}
								tmpNodes3.add(k - offset, children.get(m));
								tmpNodes3.remove(k);
								minKDist = tmpMap.get(tmpNodes3.get(k - 1).getInstance());// 锟斤拷锟斤拷锟饺讹拷锟斤拷锟叫碉拷元锟斤拷为k时锟斤拷锟斤拷要锟斤拷minKDIst锟斤拷锟斤拷为锟斤拷前锟斤拷锟饺讹拷锟斤拷锟叫碉拷锟斤拷远锟斤拷木锟斤拷耄拷锟絠ndex为K-1锟斤拷元锟斤拷
							}
						}

					}
				}
			}

			distanceThreshhold = minKDist + Math.pow(base, level);
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {// 确锟斤拷锟斤拷一锟轿碉拷锟斤拷锟斤拷锟斤拷Q
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (m = children.size() - 1; m >= 0; m--) {
						ins = children.get(m).getInstance();
						distance = tmpMap.get(ins);
						if (distance < distanceThreshhold) {
							tmpNodes2.add(children.get(m));
						}
					}
				}
			}

			tmp = tmpNodes2;
			tmpNodes2 = tmpNodes1;
			tmpNodes1 = tmp;
			tmpNodes2.clear();

			if (tmpNodes1.size() == 0) {
				break;
			}
		}
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpMap.clear();

		// ArrayList<INode> knnNodes=new ArrayList<INode>();

		INode[] knnNodes = new INode[k];
		int[] kNNs = new int[k];
		for (int i = 0; i < k; i++) {
			kNNs[i] = tmpNodes3.get(i).getInstance();
			knnNodes[i] = tmpNodes3.get(i);
		}

		tmpNodes3.clear();

		return knnNodes;
	}

	/**
	 * Return the maximum distance between the given instance and its k nearest
	 * neighbors
	 * 
	 * @param instance
	 *            the given instance
	 * @param k
	 *            the number of nearest neighborhoods
	 * @return
	 */
	public double getKNNRadius(int instance, int k) {
		if (k < 1)
			return Double.NaN;

		/*
		 * if (size() < k) { k = size(); }
		 */
		int[] knn = getKNearestNeighbor(instance, k + 1);
		int maxKIns = knn[k];
		double maxKDist = m_DistanceHolder.distance(instance, maxKIns);
		return maxKDist;
	}

	public double[] getKNNsRadius(int instance, int k) {
		if (k < 1)
			return new double[] { Double.NaN };

		/*
		 * if (size() < k) { k = size(); }
		 */
		int[] knn = getKNearestNeighbor(instance, k + 1);
		// int maxKIns = knn[k];
		double[] maxKDists = new double[k];
		for (int i = 0; i < k; i++) {
			maxKDists[i] = m_DistanceHolder.distance(instance, knn[i]);
		}

		return maxKDists;
	}

	/**
	 * Gets k centers which are maximally apart from each other, from the bottom
	 * most level of the tree.
	 * 
	 * @param number
	 *            of centers
	 * @return
	 */
	public int[] getSimpleKcenters(int numCenters) {

		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes1.add(rootNode);
		os.clear();
		os.add(rootNode.getInstance());
		List<INode> children;
		INode child;

		ArrayList<INode> tmp;
		int left;
		for (int level = maxLevel, j, k; level > minLevel && os.size() < numCenters; level--) {

			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						child = children.get(k);
						tmpNodes2.add(child);
					}
				}
			}
			left = numCenters - os.size();

			if (tmpNodes2.size() <= left) {
				for (int i = tmpNodes2.size() - 1; i >= left; i--) {
					os.add(tmpNodes2.get(i).getInstance());
				}
				if (os.size() == numCenters) {
					int[] results = os.values();
					os.clear();
					tmpNodes1.clear();
					tmpNodes2.clear();
					return results;
				}

			} else {
				Collections.sort(tmpNodes2);
				int i = tmpNodes2.size() - 1;
				while (os.size() < numCenters && i >= 0) {
					os.add(tmpNodes2.get(i--).getInstance());
				}

				if (os.size() == numCenters) {
					int[] results = os.values();
					os.clear();
					tmpNodes1.clear();
					tmpNodes2.clear();
					return results;
				}
			}
			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
		}
		return new int[0];
	}

	/**
	 * Gets k centers which are maximally apart from each other, from the bottom
	 * most level of the tree.
	 * 
	 * @param number
	 *            of centers
	 * @return
	 */
	public int[] getKLevelDensityCenters(int numCenters) {

		OrderedIntMap densities = new OrderedIntMap();
		int level = maxLevel;
		for (; level >= minLevel && size(level) < numCenters; level--) {
		}
		levelDensity(level, densities);

		OrderedIntMap sort = new OrderedIntMap(ORDER.ASC, STATUS.REPEATABLE, densities.size());

		int size = densities.size();
		for (int i = 0; i < size; i++) {
			sort.put(densities.getValueAt(i), densities.getKeyAt(i));
		}

		int[] centers = new int[numCenters < densities.size() ? numCenters : densities.size()];

		for (int i = 0; i < centers.length; i++) {
			centers[i] = sort.getValueAt((size - 1 - i));
		}

		return centers;
	}

	public OrderedIntMap levelDensity(int numCenters) {

		OrderedIntMap densities = new OrderedIntMap();
		int level = maxLevel;
		for (; level >= minLevel && size(level) < numCenters; level--) {
		}
		levelDensity(level, densities);

		return densities;
	}

	/**
	 * Gets k centers which are maximally apart from each other, from the bottom
	 * most level of the tree.
	 * 
	 * @param number
	 *            of centers
	 * @return
	 */
	public int[] getKSeparateDensityCenters(int numCenters) {

		int level = maxLevel;
		for (; level >= minLevel && size(level) < numCenters; level--) {
		}
		SeparateDensity[] sd = sepearateDensity(level);
		int[] results = new int[numCenters < sd.length ? numCenters : sd.length];
		for (int i = 0; i < results.length; i++) {
			results[i] = sd[i].id;
		}
		return results;
	}

	/**
	 * Gets k centers which are maximally apart from each other, from the bottom
	 * most level of the tree.
	 * 
	 * @param number
	 *            of centers
	 * @return
	 */
	public int[] getKCentralityCenters(int numCenters, int radiusK) {

		int level = maxLevel;
		for (; level >= minLevel && size(level) < numCenters; level--) {
		}
		Centrality[] dn = centrality(level, radiusK);
		int[] results = new int[numCenters < dn.length ? numCenters : dn.length];
		for (int i = 0; i < results.length; i++) {
			results[i] = dn[i].id;
		}
		return results;
	}

	// compute centrality of n nodes.
	// k define knn-radius
	public Centrality[] centrality(int[] nodes, int k) {
		// compute and sort knnradius
		Centrality[] dn = new Centrality[nodes.length];
		for (int i = 0; i < dn.length; i++) {
			// node = candidateNodes.get(i);
			dn[i] = new Centrality(nodes[i]);
			dn[i].setKnnradius(getKNNRadius(nodes[i], k));
			dn[i].setCdist(Math.pow(dn[i].getKnnradius(), 2));
		}
		Arrays.sort(dn);

		// compute centrality
		int id, aid;
		double dis, cdis;
		for (int i = dn.length - 1, j; i >= 0; i--) {
			id = dn[i].id;
			cdis = Double.MAX_VALUE;
			for (j = 0; j <= dn.length - 1; j++) {
				if (i == j)
					continue;
				aid = dn[j].id;
				dis = m_DistanceHolder.distance(id, aid);
				if (cdis > dis) {
					cdis = dis;
				}
			}
			dn[i].setCdist(cdis);
		}
		// Arrays.sort(dn);
		Arrays.sort(dn, Collections.reverseOrder());// sort node pair in desc
													// order finally
		return dn;
	}

	public int[] nodes(int numCenters) {
		int level = maxLevel;
		for (; level >= minLevel && size(level) < numCenters; level--) {
		}

		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpNodes1.add(rootNode);
		// candidate centers
		tmpNodes3.add(rootNode);

		List<INode> children;
		INode child;

		ArrayList<INode> tmp;

		// collect candidate centers
		for (int l = maxLevel - 1, j, m; l >= level; l--) {
			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (m = children.size() - 1; m >= 0; m--) {
						child = children.get(m);
						tmpNodes2.add(child);
					}
				}
			}

			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			tmpNodes3.addAll(tmpNodes1);
		}

		os.clear();

		INode node;
		ArrayList<INode> candidateNodes = new ArrayList<INode>(tmpNodes3);
		// compute and sort knnradius

		int[] nodes = new int[candidateNodes.size()];
		for (int i = 0; i < nodes.length; i++) {
			node = candidateNodes.get(i);
			nodes[i] = node.getID();
		}

		return nodes;
	}

	public Centrality[] getKCentralityCenterNodePairs(int numCenters, int radiusK) {
		int level = maxLevel;
		for (; level >= minLevel && size(level) < numCenters; level--) {
		}
		Centrality[] dn = centrality(level, radiusK);// get all nodes'
														// centrality in level n
		// return dn;
		Centrality[] results = new Centrality[numCenters < dn.length ? numCenters : dn.length];
		for (int i = 0; i < results.length; i++) {
			results[i] = dn[i];
		}
		return results;
	}

	public void levelDensity(int level, OrderedIntMap map) {

		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpNodes1.add(rootNode);
		tmpNodes3.add(rootNode);

		List<INode> children;
		INode child;

		ArrayList<INode> tmp;
		OrderedIntArraySet neighbors = new OrderedIntArraySet();

		for (int l = maxLevel - 1, j, k; l >= level; l--) {
			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						child = children.get(k);
						tmpNodes2.add(child);
					}
				}
			}

			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			tmpNodes3.addAll(tmpNodes1);
		}

		os.clear();

		for (int i = 0; i < tmpNodes3.size(); i++) {
			os.add(tmpNodes3.get(i).getInstance());
		}

		int instance, tmpIns;
		double distance;
		int count;
		if (level - minLevel < 2 || tmpNodes3.size() >= 0.5 * m_DistanceHolder.size()) {
			//
			tmpNodes3.clear();
			int size = m_DistanceHolder.size();
			double distanceThreshHold = Math.pow(base, level + 1);
			for (int i = 0, j; i < os.size(); i++) {
				instance = os.getValueAt(i);
				count = 0;
				for (j = 0; j < size; j++) {
					distance = m_DistanceHolder.distance(instance, j);
					if (distance <= distanceThreshHold) {
						count++;
					}
				}
				map.put(instance, count);
			}
			return;
		}

		tmpNodes4.addAll(tmpNodes1);

		double distanceThreshHold = Math.pow(base, level), tmpDistanceThreshHold;

		for (int i = 0, l, j, k; i < os.size(); i++) {
			instance = os.getValueAt(i);
			count = 0;
			tmpMap.clear();

			neighbors.clear();
			for (j = tmpNodes3.size() - 1; j >= 0; j--) {
				tmpIns = tmpNodes3.get(j).getInstance();
				distance = m_DistanceHolder.distance(instance, tmpIns);
				tmpMap.put(tmpIns, distance);
				if (distance <= distanceThreshHold) {
					neighbors.add(tmpNodes3.get(j).getInstance());
				}
			}

			tmpNodes1.clear();
			tmpNodes1.addAll(tmpNodes4);
			for (l = level - 1; l >= minLevel && tmpNodes1.size() > 0; l--) {
				tmpDistanceThreshHold = Math.pow(base, l + 1);
				tmpNodes2.clear();
				for (j = tmpNodes1.size() - 1; j >= 0; j--) {
					children = tmpNodes1.get(j).getChildren();
					if (children != null) {
						for (k = children.size() - 1; k >= 0; k--) {
							child = children.get(k);
							tmpIns = child.getInstance();
							if ((distance = tmpMap.get(tmpIns)) < 0) {
								distance = m_DistanceHolder.distance(instance, tmpIns);
							}

							if (distance <= distanceThreshHold - tmpDistanceThreshHold) {
								neighbors.removeValue(tmpIns);
								if (instance == tmpIns) {
									count += child.numDistinctChildren();
								} else {
									count += child.numDistinctChildren() + 1;
								}
							} else if (distance <= distanceThreshHold + tmpDistanceThreshHold) {
								if (distance <= distanceThreshHold) {
									neighbors.add(tmpIns);
								}
								tmpNodes2.add(child);
							}
						}
					}
				}

				tmp = tmpNodes1;
				tmpNodes1 = tmpNodes2;
				tmpNodes2 = tmp;
			}
			neighbors.removeValue(instance);
			map.put(instance, count + neighbors.size());
		}
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpMap.clear();
		os.clear();
	}

	public SeparateDensity[] getSepearateDensity(int numCenters) {

		int level = maxLevel;
		for (; level >= minLevel && size(level) < numCenters; level--) {
		}
		return sepearateDensity(level);
	}

	public SeparateDensity[] sepearateDensity(int level) {

		OrderedIntMap densities = new OrderedIntMap();
		levelDensity(level, densities);

		SeparateDensity[] sd = new SeparateDensity[densities.size()];
		for (int i = 0; i < sd.length; i++) {
			sd[i] = new SeparateDensity(densities.getKeyAt(i));
			sd[i].setLevelDensity(densities.getValueAt(i));
			sd[i].setSeprateDistance(1);
		}
		Arrays.sort(sd);

		// compute separate density
		int id, aid;
		double dis, sdis;
		for (int i = sd.length - 1, j; i >= 0; i--) {
			id = sd[i].id;
			sdis = Double.MAX_VALUE;
			for (j = 0; j <= sd.length - 1; j++) {
				if (i == j)
					continue;
				aid = sd[j].id;
				dis = m_DistanceHolder.distance(id, aid);
				if (sdis > dis) {
					sdis = dis;
				}
			}
			sd[i].setSeprateDistance(sdis);
		}
		Arrays.sort(sd, Collections.reverseOrder());// sort node pair in desc
													// order finally
		return sd;
	}

	public Centrality[] centrality(int level, int k) {

		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpNodes1.add(rootNode);
		// candidate centers
		tmpNodes3.add(rootNode);

		List<INode> children;
		INode child;

		ArrayList<INode> tmp;

		// collect candidate centers
		for (int l = maxLevel - 1, j, m; l >= level; l--) {
			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (m = children.size() - 1; m >= 0; m--) {
						child = children.get(m);
						tmpNodes2.add(child);
					}
				}
			}

			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			tmpNodes3.addAll(tmpNodes1);
		}

		os.clear();

		INode node;
		ArrayList<INode> candidateNodes = new ArrayList<INode>(tmpNodes3);
		// compute and sort knnradius

		Centrality[] dn = new Centrality[candidateNodes.size()];
		for (int i = 0; i < dn.length; i++) {
			node = candidateNodes.get(i);
			dn[i] = new Centrality(node.getInstance());
			dn[i].setKnnradius(getKNNRadius(node.getInstance(), k));
			dn[i].setCdist(Math.pow(dn[i].getKnnradius(), 2));
		}
		Arrays.sort(dn);

		// compute centrality
		int id, aid;
		double dis, cdis;
		for (int i = dn.length - 1, j; i >= 0; i--) {
			id = dn[i].id;
			cdis = Double.MAX_VALUE;
			for (j = 0; j <= dn.length - 1; j++) {
				if (i == j)
					continue;
				aid = dn[j].id;
				dis = m_DistanceHolder.distance(id, aid);
				if (cdis > dis) {
					cdis = dis;
				}
			}
			dn[i].setCdist(cdis);
		}
		// Arrays.sort(dn);
		Arrays.sort(dn, Collections.reverseOrder());// sort node pair in desc
													// order finally
		return dn;
	}

	// this is temporary-used code for CoverTree Clustering
	public int[] clustering(int numClusters, CenterMeasureType cmt, Random random) {
		long start = System.currentTimeMillis();
		int[] centers = getKcenters(numClusters, cmt);
		int[] assignments = clustering(centers);
		// buildTime = System.currentTimeMillis() - start;
		return assignments;
	}

	// this is temporary-used code for CoverTree Clustering
	public int[] clustering(int[] centers) {
		int size = m_DistanceHolder.size();

		double dist, minDist;
		int[] assignments = new int[size];

		OrderedIntArraySet os = new OrderedIntArraySet();
		Random random = new Random();
		for (int i = 0, k; i < size; i++) {
			minDist = Double.MAX_VALUE;
			os.clear();
			for (k = 0; k < centers.length; k++) {
				if (i == centers[k]) {
					os.clear();
					os.add(k);
					break;
				} else {
					dist = m_DistanceHolder.distance(i, centers[k]);
					if (dist < minDist) {
						minDist = dist;
						os.clear();
						os.add(k);
					} else if (dist == minDist) {
						os.add(k);
					}
				}
			}
			if (os.size() == 1) {
				assignments[i] = os.getLastValue();
			} else {
				assignments[i] = os.getValueAt(random.nextInt(os.size()));
			}
		}
		return assignments;
	}

	// this is temporary-used code for CoverTree Clustering
	public int[] getKcenters(int numClusters, CenterMeasureType cmt) {
		switch (cmt) {
		case LEVEL_DENSITY:
			return getKLevelDensityCenters(numClusters);
		case SEPARATE_DENSITY:
			return getKSeparateDensityCenters(numClusters);
		default:
			return null;
		}
	}

	// this is temporary-used code for CoverTree root
	public INode getRootNode() {
		return rootNode;
	}

	public CoverTree clone() {
		CoverTree ct = new CoverTree(m_DistanceHolder);
		return ct;
	}

	public IDistanceHolder getDistanceHolder() {
		return m_DistanceHolder;
	}

	public void levelDensity2(StringBuilder s, int level, OrderedIntMap map) {
		s.append("level-" + level + ":");
		s.append("\n");
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpNodes1.add(rootNode);
		tmpNodes3.add(rootNode);

		List<INode> children;
		INode child;

		ArrayList<INode> tmp;
		OrderedIntArraySet neighbors = new OrderedIntArraySet();

		for (int l = maxLevel - 1, j, k; l >= level; l--) {
			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						child = children.get(k);
						tmpNodes2.add(child);
					}
				}
			}

			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			tmpNodes3.addAll(tmpNodes1);
		}

		os.clear();

		// System.out.println("level-"+level+":");
		for (int i = 0; i < tmpNodes3.size(); i++) {
			os.add(tmpNodes3.get(i).getInstance());
			s.append(tmpNodes3.get(i).getInstance() + ",");
			s.append("\n");
			// System.out.print(tmpNodes3.get(i).getInstance()+",");
		}
		// System.out.println();

		int instance, tmpIns;
		double distance;
		int count;
		if (level - minLevel < 2 || tmpNodes3.size() >= 0.5 * m_DistanceHolder.size()) {
			//
			tmpNodes3.clear();
			int size = m_DistanceHolder.size();
			double distanceThreshHold = Math.pow(base, level + 1);
			for (int i = 0, j; i < os.size(); i++) {
				instance = os.getValueAt(i);
				count = 0;
				for (j = 0; j < size; j++) {
					distance = m_DistanceHolder.distance(instance, j);
					if (distance <= distanceThreshHold) {
						count++;
					}
				}
				map.put(instance, count);
			}
			return;
		}

		tmpNodes4.addAll(tmpNodes1);

		double distanceThreshHold = Math.pow(base, level), tmpDistanceThreshHold;

		for (int i = 0, l, j, k; i < os.size(); i++) {
			instance = os.getValueAt(i);
			count = 0;
			tmpMap.clear();

			neighbors.clear();
			for (j = tmpNodes3.size() - 1; j >= 0; j--) {
				tmpIns = tmpNodes3.get(j).getInstance();
				distance = m_DistanceHolder.distance(instance, tmpIns);
				tmpMap.put(tmpIns, distance);
				if (distance <= distanceThreshHold) {
					neighbors.add(tmpNodes3.get(j).getInstance());
				}
			}

			tmpNodes1.clear();
			tmpNodes1.addAll(tmpNodes4);
			for (l = level - 1; l >= minLevel && tmpNodes1.size() > 0; l--) {
				tmpDistanceThreshHold = Math.pow(base, l + 1);
				tmpNodes2.clear();
				for (j = tmpNodes1.size() - 1; j >= 0; j--) {
					children = tmpNodes1.get(j).getChildren();
					if (children != null) {
						for (k = children.size() - 1; k >= 0; k--) {
							child = children.get(k);
							tmpIns = child.getInstance();
							if ((distance = tmpMap.get(tmpIns)) < 0) {
								distance = m_DistanceHolder.distance(instance, tmpIns);
							}

							if (distance <= distanceThreshHold - tmpDistanceThreshHold) {
								neighbors.removeValue(tmpIns);
								if (instance == tmpIns) {
									count += child.numDistinctChildren();
								} else {
									count += child.numDistinctChildren() + 1;
								}

							} else if (distance <= distanceThreshHold + tmpDistanceThreshHold) {
								if (distance <= distanceThreshHold) {
									neighbors.add(tmpIns);
								}
								tmpNodes2.add(child);
							}
						}
					}
				}

				tmp = tmpNodes1;
				tmpNodes1 = tmpNodes2;
				tmpNodes2 = tmp;
			}
			neighbors.removeValue(instance);
			// System.out.println("child numDistinctChildren count:"+count);
			// System.out.println("neighbor size:"+neighbors.size());
			map.put(instance, count + neighbors.size());
		}
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpMap.clear();
		os.clear();
	}

	public StringBuilder realLevelDensity(int level, OrderedIntMap map) {
		StringBuilder s = new StringBuilder();
		s.append("level-" + level + ":");
		s.append("\n");
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpNodes1.add(rootNode);
		tmpNodes3.add(rootNode);

		List<INode> children;
		INode child;

		ArrayList<INode> tmp;
		OrderedIntArraySet neighbors = new OrderedIntArraySet();

		for (int l = maxLevel - 1, j, k; l >= level; l--) {
			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						child = children.get(k);
						tmpNodes2.add(child);
					}
				}
			}

			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			tmpNodes3.addAll(tmpNodes1);
		}

		os.clear();

		// System.out.println("level-"+level+":");
		for (int i = 0; i < tmpNodes3.size(); i++) {
			os.add(tmpNodes3.get(i).getInstance());
			s.append(tmpNodes3.get(i).getInstance() + ",");
			s.append("\n");
			// System.out.print(tmpNodes3.get(i).getInstance()+",");
		}
		// System.out.println();

		int instance, tmpIns;
		double distance;
		int count;
		if (level - minLevel < 2 || tmpNodes3.size() >= 0.5 * m_DistanceHolder.size()) {
			//
			tmpNodes3.clear();
			int size = m_DistanceHolder.size();
			double distanceThreshHold = Math.pow(base, level + 1);
			for (int i = 0, j; i < os.size(); i++) {
				instance = os.getValueAt(i);
				count = 0;
				for (j = 0; j < size; j++) {
					distance = m_DistanceHolder.distance(instance, j);
					if (distance <= distanceThreshHold) {
						count++;
					}
				}
				map.put(instance, count);
			}
			return s;
		}

		tmpNodes4.addAll(tmpNodes1);

		double threshold = Math.pow(base, level);
		// System.out.println();
		for (int i = 0; i < os.size(); i++) {
			int count1 = 0;
			int ins = os.getValueAt(i);
			for (int j = 0; j < this.size(); j++) {
				if (m_DistanceHolder.distance(j, ins) <= threshold)
					count1++;
			}
			map.put(ins, count1);
		}

		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpMap.clear();
		os.clear();
		return s;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(maxLevel);
		out.writeInt(minLevel);
		out.writeDouble(base);
		out.writeInt(maxNumLevels);
		out.writeInt(minNumLevels);
		out.writeDouble(maxDist);

		for (int i = maxLevel; i >= minLevel; i--) {
			out.writeInt(numLevels[maxNumLevels - i]);
		}

		out.writeUTF(m_NodeCreator.getClass().getName());
		rootNode.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		maxLevel = in.readInt();
		minLevel = in.readInt();
		base = in.readDouble();
		maxNumLevels = in.readInt();
		minNumLevels = in.readInt();
		maxDist = in.readDouble();

		numLevels = new int[maxNumLevels - minNumLevels];
		for (int i = maxLevel; i >= minLevel; i--) {
			numLevels[maxNumLevels - i] = in.readInt();
		}

		try {
			m_NodeCreator = (INodeCreator) Class.forName(in.readUTF()).newInstance();
			rootNode = m_NodeCreator.read(in);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CoverTree)) {
			return false;
		}
		CoverTree ct = (CoverTree) obj;

		return base == ct.base && rootNode.equals(ct.rootNode);
	}

	public String printLevels() {
		StringBuilder sb = new StringBuilder();

		for (int i = maxLevel; i >= minLevel; i--) {
			sb.append(i).append(" level: ").append(size(i)).append('\n');
		}

		return sb.toString();
	}

	public void numNodes(int[] numNodes) {
		for (int i = 0; i < numNodes.length; i++) {
			numNodes[i] = size(maxLevel - i);
		}
	}

	public void numNodes(int startLevel, double[] numNodes) {
		int numTotal = size(minLevel);
		for (int i = startLevel; i < startLevel + numNodes.length; i++) {
			numNodes[i] = (double) size(maxLevel - i) / numTotal;
		}
	}

	public void destroy() {
		rootNode.destroyAll();
		rootNode = null;
		m_NodeCreator = null;
		m_DistanceHolder = null;
	}

	public static CoverTree read(DataInput in, IDistanceHolder distanceHolder) throws IOException {
		CoverTree ct = new CoverTree(distanceHolder);
		ct.readFields(in);
		return ct;
	}

}