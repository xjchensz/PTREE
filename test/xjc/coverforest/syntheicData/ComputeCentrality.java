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
import xjc.clustering.validation.DunnIndex;
import xjc.clustering.validation.NormalizedLogW;
import xjc.coverforest.BuildResultStore;
import xjc.coverforest.CoverForest;
import xjc.coverforest.CoverForestFactory;
import xjc.covertree.Centrality;
import xjc.covertree.ComputeKCentersByCentrality;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.IDistanceHolder;
import xjc.data.PTree.PurTree.PurTreeClust.AbstractDataset;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;

public class ComputeCentrality {
	static boolean hasLabel = true;
	static int labelDimenOffset = 0;
	static int radiusK=5;

	public static void main(String[] args) throws Exception {
		System.out.println(ComputeCentrality.class);

		int gaussianDataSize = 1000;
		int dataSize = gaussianDataSize * 3 / 2;
		//
		String syntheticDataPath = BuildResultStore.dataDir.getAbsolutePath()
				+ "/syntheticData/syntheticData_[gaussianDataSize=" + gaussianDataSize + "]/gaussian/g0-haslabel.csv";
		File syntheticDataFile = new File(syntheticDataPath);
		BufferedReader br = new BufferedReader(new FileReader(syntheticDataFile));
		String ts = "";
		if (!syntheticDataPath.contains("label"))
			hasLabel = false;

		int dimenNum = 2;

		double[][] synData = new double[dataSize][dimenNum];
		if (hasLabel)
			labelDimenOffset = 1;
		int index = 0;
		while ((ts = br.readLine()) != null) {

			String[] dimenValueStr = ts.split(",");
			for (int i = 0; i < dimenNum; i++) {
				// System.out.print(i+":"+dimenValueStr[i]+" ");
				synData[index][i] = Double.parseDouble(dimenValueStr[i + labelDimenOffset]);
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
			cd.addInstance(sdi);
		}
		double[][] distances = cd.distances();
		System.out.println("finished compute distance");

		// saveDistance(distances, null)
		int[] k = { 5, 6, 7, 8, 9, 10 };
		// int[] radiusKs={6};
		for (int i = 0; i < k.length; i++) {
			ComputeCentrality.computeCentralityInTree(k[i], dataSize, cd, distances, synData);
		}
	}

	// compute Centrality in tree
	public static void computeCentralityInTree(int k, int dataSize, CoverTreeInstanceDataset cd, double[][] distances,
			double[][] synData) throws IOException {

		System.out.println("kradius-K:" + k);
		int[] node = new int[dataSize];
		for (int i = 0; i < dataSize; i++)
			node[i] = i;
		Centrality[] nodesCentrality = cd.getCoverTree().centrality(node, k);

		File dir = new File(BuildResultStore.resultDir, "Synthetic-data-Centrality");
		if (!dir.exists())
			dir.mkdir();
		dir = new File(dir, "[dataSize=" + dataSize + ",k=" + k + "]");
		if (!dir.exists())
			dir.mkdir();
		ComputeKCentersByCentrality.saveCentrality(dir, nodesCentrality);
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

		// coverForestIncreasedMatchedNum(cd, numCenters, treeSize, treeSizeMax,
		// centralities);

		int centerBegin = 2;
		int centerEnd = 500;
		int offset = 0;
		int delta = 1;
		// compute cluster indice on whole tree directly
		// computeCentralityDirectClusterIndice(cd, distances,
		// nodesCentrality,centerBegin, centerEnd, offset, delta);

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

		File pathDir = new File(BuildResultStore.resultDir, "CentralityDirectClusterIndice");
		if (!pathDir.exists())
			pathDir.mkdirs();

		File resultDir = new File(pathDir,
				"CentralityDirectClusterIndice-[" + centerBegin + "," + centerEnd + "," + delta + "].csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(resultDir));

		double[] logwks = new double[numCenters.length];
		for (int i = 0; i < numCenters.length; i++) {
			// System.out.println("#################################################################");
			int[] centers = new int[numCenters[i]];
			for (int j = 0; j < numCenters[i]; j++) {
				centers[j] = centralities[j].id;
			}
			int[][] partition = AbstractDataset.getPartition(cd.getCoverTree().clustering(centers));

			logwks[i] = NormalizedLogW.logwk(partition, distances);
			// System.out.println(pr.printPartitionResult());
			bWriter.write(numCenters[i] + "," + logwks[i] + "\n");

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

	// compute cluster index ,cluster in forest by centrality
	public static void computeCentralityClusterOnForestIndice(CoverForest cf, double[][] distances,
			Centrality[] centralities, int centerBegin, int centerEnd, int offset, int delta) throws IOException {
		int[] numCenters = new int[(centerEnd - centerBegin) / delta + 1];
		while (centerBegin + offset <= centerEnd) {
			numCenters[offset] = centerBegin + offset;
			offset += delta;
		}

		File pathDir = new File(BuildResultStore.resultDir, "CentralityClusterOnForestIndice");
		if (!pathDir.exists())
			pathDir.mkdirs();

		File resultDir = new File(pathDir,
				"CentralityClusterOnForestIndice-[" + centerBegin + "," + centerEnd + "," + delta + "].csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(resultDir));

		double[] logwks = new double[numCenters.length];
		for (int i = 0; i < numCenters.length; i++) {
			int[] centers = new int[numCenters[i]];
			centers = cf.getKCentralityCenters(5,numCenters[i]);
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

	// compute cluster index ,cluster by centrality in whole dataset
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
			pr.printDistribution();
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
		File dir2 = new File(BuildResultStore.resultDir, "CoverForest_MatchedOnCentrality");
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
			treeNodeCentrality[i] = cts[i].centrality(cts[i].getKCentralityCenters(radiusK,numCenters), radiusK);// get
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
			treeNodeCentrality[i] = cts[i].centrality(cts[i].getKCentralityCenters(numCenters, radiusK),radiusK);// get
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

		File dir2 = new File(BuildResultStore.resultDir, "CoverForest_MatchedOnCentrality");
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
		default:
			centers = cd.getCoverTree().getKLevelDensityCenters(numCenter);
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

}
