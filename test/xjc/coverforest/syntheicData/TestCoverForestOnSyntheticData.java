package xjc.coverforest.syntheicData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.numeric.DenseDoubleInstance;
import common.data.meta.MetaData;
import common.utils.collection.OrderedIntMap;
import test.dataGenerator.DoubleInstanceGenerator;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.clustering.validation.DunnIndex;
import xjc.clustering.validation.NormalizedLogW;
import xjc.coverforest.BuildResultStore;
import xjc.coverforest.CoverForest;
import xjc.coverforest.CoverForestFactory;
import xjc.covertree.Centrality;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.IDistanceHolder;
import xjc.data.PTree.PurTree.PurTreeClust.AbstractDataset;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;

public class TestCoverForestOnSyntheticData {
	
	public static int radiusK=5; 

	public static void main(String[] args) throws Exception {
		int dataSize = 1000;
		String syntheticDataPath = BuildSuperStoreData.dataDir.getAbsolutePath()
				+ "/syntheticData/syntheticData_[gaussianDataSize=" + dataSize + "]/gaussian/g0.csv";
		BufferedReader br = new BufferedReader(new FileReader(new File(syntheticDataPath)));
		String ts = "";

		int dimenNum = 2;

		double[][] synData = new double[dataSize][dimenNum];
		
		int index = 0;
		while ((ts = br.readLine()) != null) {

			String[] dimenValueStr = ts.split(",");
			for (int i = 0; i < dimenNum; i++) {
				// System.out.print(i+":"+dimenValueStr[i]+" ");
				synData[index][i] = Double.parseDouble(dimenValueStr[i]);
			}
			// System.out.println();
			index++;
		}
		System.out.println("dataSize:" + index);
		dataSize = index;
		DoubleInstanceGenerator sg = new DoubleInstanceGenerator();
		MetaData md = sg.generateMetaData("a", "a", 100, 1000, new Random(), true);
		CoverTreeInstanceDataset cd = new CoverTreeInstanceDataset(EuclideanDistanceMeasure.getInstance());

		for (int i = 0; i < dataSize; i++) {
			DenseDoubleInstance sdi = new DenseDoubleInstance(i, md);
			for (int j = 0; j < dimenNum; j++) {
				sdi.setValue(j, synData[i][j]);
			}
			//sdi.setLabel(labels[i]);
			cd.addInstance(sdi);
		}
		double[][] distances = cd.distances();
		System.out.println("finished compute distance");

		// saveDistance(distances, null)
		// int[] radiusKs={5,6,7,8,9,10};
		int[] radiusKs = { 6 };
		for (int i = 0; i < radiusKs.length; i++) {
			computeCentralityInTree(radiusKs[i], dataSize, cd, distances, synData);
		}

	}

	public static void computeCentralityInTree(int radiusK, int dataSize, CoverTreeInstanceDataset cd,
			double[][] distances, double[][] synData) throws IOException {

		System.out.println("kradius-K:" + radiusK);
		int[] node = new int[dataSize];
		for (int i = 0; i < dataSize; i++)
			node[i] = i;
		Centrality[] nodesCentrality = cd.getCoverTree().centrality(node, radiusK);

		File dir = new File(BuildSuperStoreData.dataDir, "Synthetic-data-Centrality");
		if (!dir.exists())
			dir.mkdir();
		dir = new File(dir, "[dataSize=" + dataSize + ",radiusK=" + radiusK + "]");
		if (!dir.exists())
			dir.mkdir();
		// testKCentersByCentrality.saveCentrality(dir, nodesCentrality);
		// testKCentersByCentrality.saveCentralityWithoutHeader(dir,
		// nodesCentrality);
		// DistanceMap.draw(distances, new File(dir, "distance.jpg"));

		// BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir,
		// "CentralityInTreesWithPositionValue.csv")));
		BufferedWriter abw1 = new BufferedWriter(
				new FileWriter(new File(dir, "CentralityInTreesWithPositionValueWithoutHeader.csv")));
		// abw.write("id,x,y,centrality\n");
		for (int i = 0; i < nodesCentrality.length; i++) {
			// format centrality output
			// abw.write(nodesCentrality[i].getID()+","+synData[nodesCentrality[i].getID()][0]+","+synData[nodesCentrality[i].getID()][1]+","+nodesCentrality[i].getCentrality()+"\n");
			abw1.write(nodesCentrality[i].getID() + "," + synData[nodesCentrality[i].getID()][0] + ","
					+ synData[nodesCentrality[i].getID()][1] + "," + nodesCentrality[i].getCentrality() + "\n");
		}
		// abw.close();
		abw1.close();
		// System.out.println("saved each node's Centrality in
		// "+"CentralityInTreesWithPositionValue.csv");
		System.out.println("saved each node's Centrality in " + "CentralityInTreesWithPositionValueWithoutHeader.csv");

		int numCenters = 50;
		if (numCenters > dataSize)
			numCenters = dataSize;
		System.out.println("center-num:" + numCenters);

		Centrality[] centralities = new Centrality[numCenters];
		for (int i = 0; i < numCenters; i++)
			centralities[i] = nodesCentrality[i]; // get k candidate centrality
													// node for whole node
		for (int i = 0; i < centralities.length; i++) {
			System.out.print(centralities[i].id + ",");
		}
		System.out.println();

		int treeSize = 2;
		int treeSizeMax = 300;// define test range
		// computeCoverForestMatchedResult(cd, treeSize, treeSizeMax,
		// numCenters, centralities);

		 //coverForestIncreasedMatchedNum(cd, numCenters, treeSize, treeSizeMax,
		 //centralities);

		int centerBegin = 2;
		int centerEnd = 500;
		int offset = 0;
		int delta = 1;
		// compute cluster indice on whole tree directly
		computeCentralityDirectClusterIndice(cd, distances, nodesCentrality, centerBegin, centerEnd, offset, delta);

		// compute cluster indice on tree
		// computeCentralityClusterIndice(cd, distances, centerBegin, centerEnd,
		// offset, delta);

		// compute cluster indice on forest
		/*
		 * double base =2.0; int treeSize =10; CoverForest cf =
		 * CoverForestFactory.getDefault().create(cd, base, treeSize);
		 * cf.setTreeInsSize(cd.size()); cf.buildCoverForest(cd, base,
		 * treeSize); computeCentralityClusterOnForestIndice(cf, distances,
		 * centralities, centerBegin, centerEnd, offset, delta);
		 */
	}

	public static void computeCentralityDirectClusterIndice(CoverTreeInstanceDataset cd, double[][] distances,
			Centrality[] centralities, int centerBegin, int centerEnd, int offset, int delta) throws IOException {
		int[] numCenters = new int[(centerEnd - centerBegin) / delta + 1];
		while (centerBegin + offset <= centerEnd) {
			numCenters[offset] = centerBegin + offset;
			offset += delta;
		}

		File pathDir = new File(BuildSuperStoreData.dataDir, "CentralityDirectClusterIndice");
		if (!pathDir.exists())
			pathDir.mkdirs();

		File resultDir = new File(pathDir,
				"CentralityDirectClusterIndice-[" + centerBegin + "," + centerEnd + "," + delta + "].csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(resultDir));

		double[] logwks = new double[numCenters.length];
		for (int i = 0; i < numCenters.length; i++) {
			// System.out.println("#################################################################");
			System.out.print(numCenters[i] + ",");
			int[] centers = new int[numCenters[i]];
			for (int j = 0; j < numCenters[i]; j++) {
				centers[j] = centralities[j].id;
			}
			long ctime = System.currentTimeMillis();
			int[][] partition = AbstractDataset.getPartition(cd.getCoverTree().clustering(centers));

			logwks[i] = NormalizedLogW.logwk(partition, distances);
			// System.out.println(pr.printPartitionResult());
			bWriter.write(numCenters[i] + "," + logwks[i] + "\n");
			System.out.println(System.currentTimeMillis() - ctime);
			/*
			 * int[][] bound={ {0,39}, {40,439}, {440,489}, {490,989},
			 * {990,999},};
			 * 
			 * PartitionResult pResult=new
			 * PartitionResult(numCenters[i],partition,centers,bound);
			 * pResult.initDistributionMap(); pResult.updateDistribution();
			 * //logwks[i]=pr.indices.get("logwk");
			 * System.out.println(pResult.printDistribution());
			 */
		}
		bWriter.close();
		System.out.println("save CentralityDirectClusterIndice in " + resultDir.getPath());

	}

	public static void computeCentralityClusterOnForestIndice(CoverForest cf, double[][] distances,
			Centrality[] centralities, int centerBegin, int centerEnd, int offset, int delta) throws IOException {
		int[] numCenters = new int[(centerEnd - centerBegin) / delta + 1];
		while (centerBegin + offset <= centerEnd) {
			numCenters[offset] = centerBegin + offset;
			offset += delta;
		}

		File pathDir = new File(BuildSuperStoreData.dataDir, "CentralityClusterOnForestIndice");
		if (!pathDir.exists())
			pathDir.mkdirs();

		File resultDir = new File(pathDir,
				"CentralityClusterOnForestIndice-[" + centerBegin + "," + centerEnd + "," + delta + "].csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(resultDir));

		double[] logwks = new double[numCenters.length];
		for (int i = 0; i < numCenters.length; i++) {
			int[] centers = new int[numCenters[i]];
			int radiusK=5;
			centers = cf.getKCentralityCenters(numCenters[i],radiusK);
			int[][] partition = AbstractDataset.getPartition(cf.getCoverTree(0).clustering(centers));

			logwks[i] = NormalizedLogW.logwk(partition, distances);
			// System.out.println(pr.printPartitionResult());
			bWriter.write(numCenters[i] + "," + logwks[i] + "\n");
			if (numCenters[i] % 10 == 0) {
				System.out.println((numCenters[i] - 10) + "-" + numCenters[i]);
			}
		}
		bWriter.close();
		System.out.println("save CentralityClusterOnForestIndice in " + resultDir.getPath());

	}

	public static void computeCentralityClusterIndice(CoverTreeInstanceDataset cd, double[][] distances,
			int centerBegin, int centerEnd, int offset, int delta) throws IOException {
		int[] numCenters = new int[(centerEnd - centerBegin) / delta + 1];
		while (centerBegin + offset <= centerEnd) {
			numCenters[offset] = centerBegin + offset;
			offset += delta;
		}

		File pathDir = new File(BuildResultStore.resultDir, "CentralityClusterIndice");
		if (!pathDir.exists())
			pathDir.mkdirs();

		File resultDir = new File(pathDir,
				"CentralityClusterIndice-[" + centerBegin + "," + centerEnd + "," + delta + "].csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(resultDir));

		double[] logwks = new double[numCenters.length];
		for (int i = 0; i < numCenters.length; i++) {
			// System.out.println("#################################################################");

			PartitionResult pr = measure(cd, CenterMeasureType.CENTRALITY, distances, numCenters[i]);
			logwks[i] = pr.indices.get("logwk");
			//pr.printDistribution();
			// System.out.println(pr.printPartitionResult());
			bWriter.write(numCenters[i] + "," + logwks[i] + "\n");
		}
		bWriter.close();
		System.out.println("save CentralityClusterIndice in " + resultDir.getPath());

	}
	
	public static void computeCoverForestMatchedResult(IDistanceHolder cd, int treeSize, int treeMax, int numCenters,
			Centrality[] centralities) throws IOException {
		System.out.println("numCenter:" + numCenters);
		OrderedIntMap matchNumMap = new OrderedIntMap();
		for (int i = treeSize; i < treeMax; i++) {

			int matchedNum = CoverForestMatchedNum(cd, numCenters, i, centralities);
			System.out.println("treeSize-" + i + ":" + matchedNum);
			matchNumMap.put(i, matchedNum);
		}
		File dir2 = new File(BuildSuperStoreData.dataDir, "CoverForest_MatchedOnCentrality");
		if (!dir2.exists())
			dir2.mkdir();

		File saveDir = new File(dir2, "CoverForestMatchNum_Statisitic.csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(saveDir));
		int[] keys = matchNumMap.keys();
		for (int i = 0; i < keys.length; i++) {
			bWriter.write(keys[i] + "," + matchNumMap.get(keys[i]) + "\n");
		}
		bWriter.close();
		System.out.println("saved match result in " + saveDir.getPath());
	}

	public static int CoverForestMatchedNum(IDistanceHolder cd, int numCenters, int treeSize,
			Centrality[] centralities) {
		HashMap<Integer, Centrality> centralityMap = new HashMap<Integer, Centrality>();
		for (int i = 0; i < numCenters; i++) {
			centralityMap.put(centralities[i].id, centralities[i]);
		}

		double base = 2.0;

		CoverForest cf = CoverForestFactory.getDefault().create(cd, base, treeSize);
		cf.setTreeInsSize(cd.size());
		cf.buildCoverForest(cd, base, treeSize);
		CoverTree[] cts = cf.getCoverTrees();

		Centrality[][] treeNodeCentrality = new Centrality[treeSize][numCenters];// centrality
																					// in
																					// a
																					// tree
		for (int i = 0; i < treeSize; i++) {
			treeNodeCentrality[i] = cts[i].centrality(cts[i].getKCentralityCenters(radiusK, numCenters), radiusK);// get
																							// k
																							// candidate
																							// centrality
																							// node
																							// for
																							// each
																							// tree
		}

		ArrayList<ArrayList<Centrality>> matchNodes = new ArrayList<ArrayList<Centrality>>();
		HashMap<Integer, Centrality> centralityMapInForest = new HashMap<Integer, Centrality>();
		for (int i = 0; i < treeSize; i++) {
			ArrayList<Centrality> matchedNoseInTree = new ArrayList<Centrality>();
			for (int j = 0; j < numCenters; j++) {
				// System.out.println(treeNodeCentrality[i][j].id+",");
				if (centralityMap.containsKey(treeNodeCentrality[i][j].id)) {
					matchedNoseInTree.add(treeNodeCentrality[i][j]);
					if (!centralityMapInForest.containsKey(treeNodeCentrality[i][j].id)) {
						centralityMapInForest.put(treeNodeCentrality[i][j].id, treeNodeCentrality[i][j]);
					}
				}
			}
			matchNodes.add(matchedNoseInTree);
		}

		return centralityMapInForest.size();
	}

	public static int[] coverForestIncreasedMatchedNum(IDistanceHolder cd, int numCenters, int treeSize,
			int treeSizeMax, Centrality[] centralities) throws IOException {
		HashMap<Integer, Centrality> centralityMap = new HashMap<Integer, Centrality>();
		for (int i = 0; i < numCenters; i++) {
			centralityMap.put(centralities[i].id, centralities[i]);
		}

		double base = 2.0;

		CoverForest cf = CoverForestFactory.getDefault().create(cd, base, treeSizeMax);
		cf.setTreeInsSize(cd.size());
		cf.buildCoverForest();
		CoverTree[] cts = cf.getCoverTrees();

		Centrality[][] treeNodeCentrality = new Centrality[treeSizeMax][numCenters];// centrality
																					// in
																					// a
																					// tree
		for (int i = 0; i < treeSizeMax; i++) {
			treeNodeCentrality[i] = cts[i].centrality(cts[i].getKCentralityCenters(radiusK, numCenters), radiusK);// get
																							// k
																							// candidate
																							// centrality
																							// node
																							// for
																							// each
																							// tree
		}

		ArrayList<ArrayList<Centrality>> matchNodes = new ArrayList<ArrayList<Centrality>>();
		HashMap<Integer, Centrality> centralityMapInForest = new HashMap<Integer, Centrality>();
		HashMap<Integer, Integer> increasedMatchedNumInForest = new HashMap<Integer, Integer>();

		for (int i = 0; i < treeSizeMax; i++) {
			System.out.println("tree-size:" + (i + 1));
			ArrayList<Centrality> matchedNodesInTree = new ArrayList<Centrality>();
			for (int j = 0; j < numCenters; j++) {
				// System.out.println(treeNodeCentrality[i][j].id+",");
				if (centralityMap.containsKey(treeNodeCentrality[i][j].id)) {
					matchedNodesInTree.add(treeNodeCentrality[i][j]);
					if (!centralityMapInForest.containsKey(treeNodeCentrality[i][j].id)) {
						centralityMapInForest.put(treeNodeCentrality[i][j].id, treeNodeCentrality[i][j]);
					}
				}
			}
			matchNodes.add(matchedNodesInTree);
			increasedMatchedNumInForest.put(i, centralityMapInForest.size());
		}

		int[] matchedNodeNum = new int[treeSizeMax - treeSize];
		for (int i = treeSize; i < treeSizeMax; i++) {
			matchedNodeNum[i - treeSize] = increasedMatchedNumInForest.get(i);
		}

		File dir2 = new File(BuildSuperStoreData.dataDir, "CoverForest_MatchedOnCentrality");
		if (!dir2.exists())
			dir2.mkdir();

		File saveDir = new File(dir2, "CoverForestIncreasedMatchNum_Statisitic.csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(saveDir));
		for (int i = 0; i < matchedNodeNum.length; i++) {
			bWriter.write((treeSize + i) + "," + (matchedNodeNum[i] / (double) numCenters) + "\n");
		}
		bWriter.close();
		System.out.println("saved match result in " + saveDir.getPath());

		return matchedNodeNum;
	}

	public static PartitionResult measure(CoverTreeInstanceDataset cd, CenterMeasureType cmt, double[][] distances,
			int numCenter) {
		int[] centers = new int[numCenter];
		switch (cmt) {
		case LEVEL_DENSITY: {
			centers = cd.getCoverTree().getKLevelDensityCenters(numCenter);

		}
			;
			break;
		case CENTRALITY: {
			Centrality[] centralityCenters = cd.getCoverTree().centrality(cd.getCoverTree().getKCentralityCenters(radiusK, numCenter), radiusK);
			for (int i = 0; i < numCenter; i++) {
				centers[i] = centralityCenters[i].getID();
			}
		}
			;
			break;
		}

		/*
		 * for(int i=0;i<centers.length;i++){ double[]
		 * centerPos=synData[centers[i]];
		 * 
		 * System.out.print(i+"-"+(centers[i])+":"); for(int j=0;j<dimenNum;j++)
		 * System.out.print((centerPos[j])+","); System.out.println(); }
		 */
		int[][] partition = AbstractDataset.getPartition(cd.getCoverTree().clustering(centers));

		int[][] bound = { { 0, 39 }, { 40, 439 }, { 440, 489 }, { 490, 989 }, { 990, 999 }, };

		PartitionResult pResult = new PartitionResult(numCenter, partition, centers, bound);
		// System.out.println("logwk:"+NormalizedLogW.logwk(partition,
		// distances));
		// System.out.println("dunnIndex:"+DunnIndex.di(partition, distances));
		pResult.indices.put("logwk", NormalizedLogW.logwk(partition, distances));
		pResult.indices.put("dunnIndex:", DunnIndex.di(partition, distances));
		return pResult;
	}

	public static void saveDistance(CoverTreeInstanceDataset cd, String resultPath) throws Exception {
		double[][] dis = cd.distances();
		int dataSize = cd.size();
		File syntheticDataResult = new File(BuildSuperStoreData.dataDir, "SyntheticData-TestResult");
		if (!syntheticDataResult.exists())
			syntheticDataResult.mkdirs();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(syntheticDataResult, "distance.csv")));

		for (int i = 0; i < dataSize; i++) {
			for (int j = 0; j < dataSize; j++) {
				bw.write(dis[i][j] + ",");
			}
			bw.write('\n');
		}
		System.out.println("saved synthetic distance");
	}

	public static void saveDistance(double[][] dis, String resultPath) throws Exception {
		int dataSize = dis.length;
		File syntheticDataResult = new File(BuildSuperStoreData.dataDir, "SyntheticData-TestResult");
		if (!syntheticDataResult.exists())
			syntheticDataResult.mkdirs();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(syntheticDataResult, "distance.csv")));

		for (int i = 0; i < dataSize; i++) {
			for (int j = 0; j < dataSize; j++) {
				bw.write(dis[i][j] + ",");
			}
			bw.write('\n');
		}
		System.out.println("saved synthetic distance");
	}

	public static class PartitionResult {
		public int[][] bound;
		public int numCenter;
		public int[][] partition = new int[numCenter][];
		public int[] center = new int[numCenter];
		public HashMap<String, Double> indices = new HashMap<String, Double>();
		public HashMap<Integer, ArrayList<Integer>> centerDistributionMap;
		public HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> partitionDistributionMap;

		public PartitionResult() {
			// TODO Auto-generated constructor stub
		}

		public PartitionResult(int numCenter, int[][] partition, int[] center, int[][] bound) {
			this.numCenter = numCenter;
			this.partition = partition;
			this.center = center;
			this.bound = bound;
		}

		public void initDistributionMap() {
			// initial distribution;
			centerDistributionMap = new HashMap<Integer, ArrayList<Integer>>();
			partitionDistributionMap = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();

			for (int i = 0; i < bound.length; i++) {
				centerDistributionMap.put(i, new ArrayList<Integer>());
			}
			for (int i = 0; i < numCenter; i++) {

				HashMap<Integer, ArrayList<Integer>> th = new HashMap<Integer, ArrayList<Integer>>();
				for (int j = 0; j < bound.length; j++) {
					th.put(j, new ArrayList<Integer>());
				}
				partitionDistributionMap.put(i, th);
			}
		}

		public String printPartitionIndices() {
			StringBuilder s = new StringBuilder();
			s.append("numCenter:" + numCenter + "\n");
			s.append("center:\n");
			for (int i = 0; i < numCenter; i++) {
				s.append(i + "-" + center[i] + "\n");
			}

			java.util.Iterator<String> it = indices.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				s.append(key + ":" + indices.get(key) + "\n");
			}
			return s.toString();
		}

		public int partitionIndex(int index) {
			int partIndex = -1;
			int temp = 0;
			for (int i = 0; i < bound.length; i++) {
				temp = bound[i][0];
				if (index >= bound[i][0] && index <= bound[i][1])
					partIndex = i;
			}
			return partIndex;
		}

		public void updateDistribution() {
			// get center distribution
			{
				for (int i = 0; i < numCenter; i++) {
					centerDistributionMap.get(partitionIndex(center[i])).add(center[i]);
				}
			}

			// get partition distribution
			{
				for (int i = 0; i < numCenter; i++) {
					HashMap<Integer, ArrayList<Integer>> tHashMap = partitionDistributionMap.get(i);
					for (int j = 0; j < partition[i].length; j++) {
						int partIndex = partitionIndex(partition[i][j]);
						tHashMap.get(partIndex).add(partition[i][j]);
					}
					partitionDistributionMap.put(i, tHashMap);
				}
			}
		}

		public String printDistribution() {
			StringBuilder s = new StringBuilder();
			s.append("numCenter:" + numCenter + "\n");
			{
				for (int i = 0; i < centerDistributionMap.size(); i++) {
					s.append("p" + i + ":");
					ArrayList<Integer> centerDistribution = centerDistributionMap.get(i);
					for (int j = 0; j < centerDistribution.size(); j++) {
						s.append(centerDistribution.get(j) + ",");
					}
					s.append('\n');
				}
			}

			s.append("\nClusterPartitionDistribution\n");
			// get partition distribution
			{
				for (int i = 0; i < numCenter; i++) {
					HashMap<Integer, ArrayList<Integer>> tHashMap = partitionDistributionMap.get(i);
					s.append("center<" + i + ">:" + center[i] + ",p" + partitionIndex(center[i]) + "\n");
					for (int j = 0; j < tHashMap.size(); j++) {
						s.append("p" + j + ":");
						ArrayList<Integer> tIntegers = tHashMap.get(j);
						for (int p = 0; p < tIntegers.size(); p++) {
							s.append(tIntegers.get(p) + ",");
						}
						s.append('\n');
					}
				}
			}

			return s.toString();

		}
	}
}
