package xjc.coverforest;

import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.ujmp.gui.actions.RandAction;

import common.IWritable;
import common.utils.collection.OrderedIntArraySet;
import common.utils.collection.OrderedIntMap;
import xjc.covertree.Centrality;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeFactory;
import xjc.covertree.IDistanceHolder;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;

public class CoverForest implements IWritable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1686912827712497577L;
	private int treeSize;
	private int treeInsSize;// tree instance Size
	private double base;
	ArrayList<CoverTree> cf;
	IDistanceHolder distanceHolder;
	ArrayList<int[]> CoverTreeInsertIndex ;//cover Tree Insert sequence 
	long[] coverTreesBuildTime;//each cover Tree build time
	
	private DynamicIndex m_DynamicIndex;

	public CoverForest() {
		cf = new ArrayList<CoverTree>();
	}

	public CoverForest(CoverTree[] cts) {
		cf = new ArrayList<CoverTree>();
		for (int i = 0; i < cts.length; i++)
			cf.add(cts[i]);
		treeSize = cts.length;
	}

	public CoverForest(IDistanceHolder dis, double base, int treeSize) {
		cf = new ArrayList<CoverTree>();
		m_DynamicIndex = new DynamicIndex(dis.size());
		distanceHolder = dis;
		this.base = base;
		this.treeSize = treeSize;
		this.treeInsSize = dis.size();// ####################################
		CoverTreeInsertIndex=new ArrayList<int[]>();
		coverTreesBuildTime=new long[treeSize];
	}

	public double getBase() {
		return base;
	}

	public void setBase(double base) {
		this.base = base;
	}

	public void incInstance() {
		treeInsSize++;
	}

	public void decInstance() {
		if (treeInsSize > 0)
			treeInsSize--;
	}

	public void setTreeInsSize(int size) {
		treeInsSize = size;
	}

	public int getTreeInsSize() {
		return treeInsSize;
	}

	public int getTreeSize() {
		return treeSize;
	}

	public void buildCoverForest() {
		buildCoverForest(distanceHolder, base, treeSize);// make sure fields are
															// valid;
	}
	
	public void buildCoverForest(Random random) {
		buildCoverForest(distanceHolder, base, treeSize,random);// make sure fields are
															// valid;
	} 
	
	public void buildCoverForest(int[] originInsertIndex) {
		buildCoverForest(distanceHolder, base, treeSize,originInsertIndex);// make sure fields are
															// valid;
	}

	public void buildCoverForest(IDistanceHolder dis, double base, int treeSize,Random random) {
		int[] originInsertIndex=createNonRepeatableRandomList(dis.size());
		buildCoverForest(dis,base,treeSize,originInsertIndex);
	}
	
	public void buildCoverForest(IDistanceHolder dis, double base, int treeSize,int[] originInsertIndex) {
		m_DynamicIndex = new DynamicIndex(originInsertIndex);
		long start=System.currentTimeMillis();
		long end=start;
		int id;
		for (int i = 0; i < treeSize; i++) {
			int[] insertIndex=new int[dis.size()];
			 start=System.currentTimeMillis();
			CoverTree cTree = CoverTreeFactory.getDefault().create(dis, base);
			for (int j = 0; j < treeInsSize; j++) {
				id = m_DynamicIndex.getID(j);
				insertIndex[j]=id;
				m_DynamicIndex.setLevel(id, (int) cTree.insert(id));
			}
			cTree.update();
			 end=System.currentTimeMillis();
			coverTreesBuildTime[i]=end-start;
			cf.add(cTree);
			CoverTreeInsertIndex.add(insertIndex);
			m_DynamicIndex.sort();
		}
	}
	
	
	public void buildCoverForest(IDistanceHolder dis, double base, int treeSize) {
		m_DynamicIndex = new DynamicIndex(dis.size());
		long start=System.currentTimeMillis();
		long end=start;
		int id;
		for (int i = 0; i < treeSize; i++) {
			int[] insertIndex=new int[dis.size()];
			 start=System.currentTimeMillis();
			CoverTree cTree = CoverTreeFactory.getDefault().create(dis, base);
			for (int j = 0; j < treeInsSize; j++) {
				id = m_DynamicIndex.getID(j);
				insertIndex[j]=id;
				m_DynamicIndex.setLevel(id, (int) cTree.insert(id));
			}
			cTree.update();
			 end=System.currentTimeMillis();
			coverTreesBuildTime[i]=end-start;
			cf.add(cTree);
			CoverTreeInsertIndex.add(insertIndex);
			m_DynamicIndex.sort();
		}
	}
	
	public long getCoverTreeBuildTime(int index){
		return coverTreesBuildTime[index];
	}
	


	public CoverTree getCoverTree(int index) {
		return cf.get(index);
	}

	public CoverTree[] getCoverTrees() {
		CoverTree[] coverTrees = new CoverTree[cf.size()];
		for (int i = 0; i < cf.size(); i++) {
			coverTrees[i] = cf.get(i);
		}
		return coverTrees;
	}

	ArrayList<ArrayList<HashMap<Integer, OrderedIntArraySet>>> coverTreesChildLevelMap;

	// ArrayList<HashMap<Integer, OrderedIntArraySet>> is childLevelMap;
	public void initCoverTreesChildLevelMap() {
		coverTreesChildLevelMap = new ArrayList<ArrayList<HashMap<Integer, OrderedIntArraySet>>>();
		for (int i = 0; i < treeSize; i++) {
			coverTreesChildLevelMap.add(i, null);
		}
	}

	public boolean updateCoverTreeChildLevelMap(int treeIndex,
			ArrayList<HashMap<Integer, OrderedIntArraySet>> newChildlevelMap) {
		coverTreesChildLevelMap.set(treeIndex, newChildlevelMap);
		return true;
	}

	public ArrayList<HashMap<Integer, OrderedIntArraySet>> getCoverTreeChildLevelMap(int index) {
		return coverTreesChildLevelMap.get(index);
	}

	ArrayList<ArrayList<OrderedIntMap>> coverTreesLevelDensity;

	// ArrayList<OrderedInMap> is levelDensity
	public void initCoverTreesLevelDensity() {
		coverTreesLevelDensity = new ArrayList<ArrayList<OrderedIntMap>>();
		for (int i = 0; i < treeSize; i++) {
			coverTreesLevelDensity.add(i, null);
		}
	}

	public boolean updateCoverTreesLevelDensity(int treeIndex, ArrayList<OrderedIntMap> newChildlevelMap) {
		coverTreesLevelDensity.set(treeIndex, newChildlevelMap);
		return true;
	}

	public ArrayList<OrderedIntMap> getCoverTreesLevelDensity(int index) {
		return coverTreesLevelDensity.get(index);
	}

	/**
	 * Gets k centers which are maximally apart from each other, from the bottom
	 * most level of the tree.
	 * 
	 * @param number
	 *            of centers
	 * @return
	 */
	/*
	 * public int[] getKLevelDensityCenters(int numCenters) {
	 * 
	 * OrderedIntMap densities = new OrderedIntMap(); int level = maxLevel; for
	 * (; level >= minLevel && size(level) < numCenters; level--) { }
	 * density(level, numCenters * 2, densities);
	 * 
	 * OrderedIntMap sort = new OrderedIntMap(ORDER.ASC, STATUS.REPEATABLE,
	 * densities.size());
	 * 
	 * int size = densities.size(); for (int i = 0; i < size; i++) {
	 * sort.put(densities.getValueAt(i), densities.getKeyAt(i)); }
	 * 
	 * int[] centers = new int[numCenters < densities.size() ? numCenters :
	 * densities.size()];
	 * 
	 * for (int i = 0; i < centers.length; i++) { centers[i] =
	 * sort.getValueAt((size - 1 - i)); }
	 * 
	 * return centers; }
	 */

	/**
	 * Gets k centers which are maximally apart from each other, from the bottom
	 * most level of the tree.
	 * 
	 * @param number
	 *            of centers
	 * @return
	 */
	public int[] getKCentralityCenters(int k, int numCenters) {

		int[] centers = new int[numCenters];
		Centrality[] centerNodes = getKCentralityCenterNodePairs(k, numCenters);
		for (int i = 0; i < centerNodes.length; i++) {
			centers[i] = centerNodes[i].id;
		}
		return centers;
	}

	public Centrality[] getKCentralityCenterNodePairs(int k, int numCenters) {

		OrderedIntArraySet os = new OrderedIntArraySet();
		int[] nodes;
		for (int i = 0; i < treeSize; i++) {
			nodes = cf.get(i).nodes(numCenters);
			os.addAll(nodes);
		}
		Centrality[] cn = cf.get(0).centrality(os.values(), k);

		Arrays.sort(cn);
		Centrality[] results = new Centrality[numCenters];
		for (int i = 0; i < numCenters; i++) {
			results[i] = cn[cn.length - 1 - i];
		}

		return results;
	}

	public void printKCentralityCenterNodePairsInTrees(Centrality[][] doubleNodes) throws IOException {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < doubleNodes.length; i++) {
			s.append("Tree_" + i + ":\n");
			for (int j = 0; j < doubleNodes[i].length; j++) {
				s.append(j + ">" + doubleNodes[i][j].id + ":" + doubleNodes[i][j].centrality + "\n");
			}
			s.append("\n");
		}
		System.out.println(s);
	}

	public Centrality[][]  getKCentralityCenterInTrees(int radiusK,int numCenters){
		Centrality[][] results = new Centrality[treeSize][numCenters];
		for (int i = 0; i < cf.size(); i++) {
			results[i] = cf.get(i).getKCentralityCenterNodePairs(numCenters,radiusK);
		}
		return results;
	}
	
	public Centrality[] getKCentralityCenterNodePairs(int numCenters) {
			ArrayList<Integer> candidateCentersInForest=new ArrayList<Integer>();
			ArrayList< ArrayList<Integer> > centersInTrees=new ArrayList< ArrayList<Integer> >();
			
			for (int i = 0; i < cf.size(); i++) {
				//Centrality[] dn = cf.get(i).getKCentralityCenterNodePairs(numCenters);
				ArrayList<Integer> centersInTree=new ArrayList<Integer>();
				int[] centers=cf.get(i).getKLevelDensityCenters(numCenters);
				for(int j=0;j<centers.length;j++){
					centersInTree.add(centers[j]);
					if(!candidateCentersInForest.contains(centers[j])) candidateCentersInForest.add(centers[j]); 
					
				}
				centersInTrees.add(centersInTree);
			}
			
			int[] candidateCentersIdInForest=new int[candidateCentersInForest.size()];
			for(int i=0;i<candidateCentersIdInForest.length;i++)
				candidateCentersIdInForest[i]=candidateCentersInForest.get(i);
			
			Centrality[] candidateCenterCentrality=cf.get(0).centrality(candidateCentersIdInForest, 5);
			
			Centrality[] results = new Centrality[numCenters];
			//Arrays.sort(totalResults, Collections.reverseOrder());

			for (int i = 0; i < results.length; i++) {
				results[i]=candidateCenterCentrality[i];
			}
			return results;
			
			
		}
	
	
	public void saveKCentralityCenterNodePairsInTrees(File dir, Centrality[][] doubleNodes) throws IOException {
		// DoubleNode[][]
		// doubleNodes=getKCentralityCenterNodePairsInTrees(numCenters);
		BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir, "KCentralityCenterInTrees.csv")));
		StringBuilder s = new StringBuilder();

		for (int i = 0; i < doubleNodes.length; i++) {
			// format centrality output
			int[] ids = new int[doubleNodes[i].length];
			double[] cdists = new double[doubleNodes[i].length];
			double[] knnRs = new double[doubleNodes[i].length];
			double[] centralitys = new double[doubleNodes[i].length];
			for (int j = 0; j < doubleNodes[i].length; j++) {
				ids[j] = doubleNodes[i][j].getID();
				cdists[j] = doubleNodes[i][j].getCdist();
				knnRs[j] = doubleNodes[i][j].getKnnradius();
				centralitys[j] = doubleNodes[i][j].getCentrality();
			}
			abw.write("tree_" + i + ":\n");
			abw.write("id,");
			for (int j = 0; j < doubleNodes[i].length; j++)
				abw.write(ids[j] + ",");
			abw.newLine();
			abw.write("cdist,");
			for (int j = 0; j < doubleNodes[i].length; j++)
				abw.write(cdists[j] + ",");
			abw.newLine();
			abw.write("knnradius,");
			for (int j = 0; j < doubleNodes[i].length; j++)
				abw.write(knnRs[j] + ",");
			abw.newLine();
			abw.write("centrality,");
			for (int j = 0; j < doubleNodes[i].length; j++)
				abw.write(centralitys[j] + ",");
			abw.newLine();
			abw.newLine();

		}
		abw.close();
		System.out.println("saved each tree's kCentralityCenter in " + "KCentralityCenterInTrees.csv");
	}

	public void printKCentralityCenterNodePairs(Centrality[] doubleNodes) throws IOException {
		StringBuilder s = new StringBuilder();
		s.append("Forest:\n");

		for (int j = 0; j < doubleNodes.length; j++) {
			s.append(j + ">" + doubleNodes[j].id + ":" + doubleNodes[j].centrality + "\n");
		}
		System.out.println(s);
	}

	public void saveKCentralityCenterNodePairsInForest(File dir, Centrality[] doubleNodes) throws IOException {
		BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir, "KCentralityCenterInForest.csv")));
		abw.write("Forest:\n");
		// format centrality output
		int[] ids = new int[doubleNodes.length];
		double[] cdists = new double[doubleNodes.length];
		double[] knnRs = new double[doubleNodes.length];
		double[] centralitys = new double[doubleNodes.length];
		for (int j = 0; j < doubleNodes.length; j++) {
			ids[j] = doubleNodes[j].getID();
			cdists[j] = doubleNodes[j].getCdist();
			knnRs[j] = doubleNodes[j].getKnnradius();
			centralitys[j] = doubleNodes[j].getCentrality();
		}
		abw.write("id,");
		for (int j = 0; j < doubleNodes.length; j++)
			abw.write(ids[j] + ",");
		abw.newLine();
		abw.write("cdist,");
		for (int j = 0; j < doubleNodes.length; j++)
			abw.write(cdists[j] + ",");
		abw.newLine();
		abw.write("knnradius,");
		for (int j = 0; j < doubleNodes.length; j++)
			abw.write(knnRs[j] + ",");
		abw.newLine();
		abw.write("centrality,");
		for (int j = 0; j < doubleNodes.length; j++)
			abw.write(centralitys[j] + ",");
		abw.newLine();
		abw.newLine();

		abw.close();
		System.out.println("saved forest's kCentralityCenter in " + "KCentralityCenterInForest.csv");
	}

	/*
	 * public Centrality[][] getKCentralityCenterNodePairsInTrees(int
	 * numCenters){ Centrality[][] results = new
	 * Centrality[treeSize][numCenters]; for (int i = 0; i < cf.size(); i++) {
	 * results[i] = cf.get(i).getKCentralityCenterNodePairs(numCenters); }
	 * return results; }
	 */

	@Override
	public void write(DataOutput out) throws IOException {
		
	}

	@Override
	public void readFields(DataInput in) throws IOException {

	}
	
	public void saveCoverTreesInForest(File dir) throws IOException{
		dir=new File(dir,"coverForest");
		if(!dir.exists()) dir.mkdirs();
		for(int i=0;i<treeSize;i++){
			DataOutputStream out=new DataOutputStream(new FileOutputStream(new File(dir,"covertree-"+i+".ctr")));
			cf.get(i).write(out);
		}
		BufferedWriter bWriter=new BufferedWriter(new FileWriter(new File(dir,"insert-sequence.csv")));
		StringBuilder s=new StringBuilder();
		for(int i=0;i<treeSize;i++){
			int[] ctreeInsertIndex=CoverTreeInsertIndex.get(i);
			for(int j=0;j<treeInsSize-1;j++){
				s.append(""+ctreeInsertIndex[j]+',');				
			}
			s.append(""+ctreeInsertIndex[treeInsSize-1]+"\r\n");			
			bWriter.append(s.toString());
			s.delete(0, s.length()-1);
		}
		bWriter.close();
	}

	public void destroy() {
		cf.clear();
		m_DynamicIndex.clear();
		treeSize = 0;
		treeInsSize = 0;
	}

	public static int[] createNonRepeatableRandomList(int length) {
		Random r = new Random();
		r.nextInt();
		OrderedIntMap oIntMap = new OrderedIntMap();
		for (int i = 0; i <= length - 1; i++) {
			int randomKey = r.nextInt();
			while (oIntMap.containsKey(randomKey))
				randomKey = r.nextInt();
			oIntMap.put(randomKey, i);// create key for every integer in
										// [1,length]
		}

		int[] fullRandomIndexs = new int[length];
		for (int i = 0; i < length; i++)
			fullRandomIndexs[i] = oIntMap.get(oIntMap.getKeyAt(i));

		return fullRandomIndexs;
	}

	// determine num of center and assign instance to center ,return partion
	// result with center
	public HashMap<Integer, ArrayList<Integer>> clustering(int numCenters, int k, CenterMeasureType cmt,
			Random random) {
		int[] centers = getKCentralityCenters(k, numCenters);
		int[] assignment = assignment(centers, cmt, random);

		HashMap<Integer, ArrayList<Integer>> clusterResult = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < numCenters; i++) {
			clusterResult.put(centers[i], new ArrayList<Integer>());
		}

		for (int i = 0; i < assignment.length; i++) {
			clusterResult.get(centers[assignment[i]]).add(i);
		}
		return clusterResult;
	}

	// determine num of center and assign instance to center ,return partition
	// result
	public int[][] getPartition(int numCenters, int k, CenterMeasureType cmt, Random random) {
		int[] centers = getKCentralityCenters(k, numCenters);
		int[] clustering = assignment(centers, cmt, random);

		OrderedIntArraySet os = new OrderedIntArraySet();
		for (int i = 0; i < clustering.length; i++) {
			os.add(clustering[i]);
		}
		int numClusters = os.size();

		OrderedIntArraySet[] tos = new OrderedIntArraySet[numClusters];
		for (int i = 0; i < tos.length; i++) {
			tos[i] = new OrderedIntArraySet();
		}
		for (int i = 0; i < clustering.length; i++) {
			tos[os.indexOf(clustering[i])].add(i);
		}
		int[][] partition = new int[numClusters][];
		for (int i = 0; i < partition.length; i++) {
			partition[i] = tos[i].values();
			tos[i].destroy();
		}
		os.destroy();
		return partition;
	}
	
	public CoverTree buildCoverTree(){
		CoverTree cTree = CoverTreeFactory.getDefault().create(this.distanceHolder, base);
		for (int j = 0; j < treeInsSize; j++) {
			 cTree.insert(j);
		}
		cTree.update();
		return cTree;
	}
	

	public void printClusterResult(int numCenters, int k, CenterMeasureType cmt, Random random) {
		Map<Integer, ArrayList<Integer>> clusterResult = clustering(numCenters, k, cmt, random);
		Iterator<Integer> iterator = clusterResult.keySet().iterator();

		int index = 0;
		while (iterator.hasNext()) {
			int centeInt = iterator.next();
			ArrayList<Integer> clusterInts = clusterResult.get(centeInt);

			System.out.println("Cluster_" + (index++) + ": center:" + centeInt);
			System.out.println("  clusterSize:" + clusterInts.size());
			StringBuilder s = new StringBuilder();
			s.append('\t');
			for (int i = 0; i < clusterInts.size(); i++) {
				s.append(clusterInts.get(i) + ",");
			}
			s.append('\n');
			System.out.println(s);
		}
	}

	// determine centers and assign instance to nearest node
	public int[] assignment(int[] centers, CenterMeasureType cmt, Random random) {
		int size = treeInsSize;
		double dist, minDist;
		int[] assignments = new int[size];

		OrderedIntArraySet os = new OrderedIntArraySet();

		for (int i = 0, k; i < size; i++) {
			minDist = Double.MAX_VALUE;
			os.clear();
			for (k = 0; k < centers.length; k++) {
				if (i == centers[k]) {
					os.clear();
					os.add(k);
					break;
				} else {
					dist = distanceHolder.distance(i, centers[k]);
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
			os.clear();
		}
		return assignments;
	}

	public int[] getCoverTreeInsertIndex(int i) {
		// TODO Auto-generated method stub
		return CoverTreeInsertIndex.get(i);
	}

	public void setTreeSize(int i) {
		// TODO Auto-generated method stub
		treeSize=i;
	}
	

}
