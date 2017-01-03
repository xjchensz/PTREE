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
import test.dataGenerator.DoubleInstanceGenerator;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.coverforest.CoverForest;
import xjc.coverforest.CoverForestFactory;
import xjc.covertree.Centrality;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.IDistanceHolder;

public class ComputeCoverForestMatchedCentrality {
	public static void main(String[] args) throws Exception {
		int dataSize = 1000;
		String syntheticDataPath = "sd1/gaussian/g0.csv";
		BufferedReader br = new BufferedReader(new FileReader(new File(syntheticDataPath)));
		String ts = "";

		int dimenNum = 2;
		int numCenters = 50;

		double[][] synData = new double[dataSize][dimenNum];

		int index = 0;
		while ((ts = br.readLine()) != null && index < dataSize) {

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
		MetaData md = sg.generateMetaData("a", "a", dataSize, dimenNum, new Random(), true);
		CoverTreeInstanceDataset cd = new CoverTreeInstanceDataset(EuclideanDistanceMeasure.getInstance());

		for (int i = 0; i < dataSize; i++) {
			DenseDoubleInstance sdi = new DenseDoubleInstance(i, md);
			for (int j = 0; j < dimenNum; j++) {
				sdi.setValue(j, synData[i][j]);
			}
			// sdi.setLabel(labels[i]);
			cd.addInstance(sdi);
		}
		double[][] distances = cd.distances();
		System.out.println("finished compute distance");

		// saveDistance(distances, null)
		// int[] radiusKs={5,6,7,8,9,10};
		int[] k = { 10 };
		for (int i = 0; i < k.length; i++) {
			computeCentralityInTree(k[i], dataSize, numCenters, cd, distances, synData, "sd1/k=" + k[i]);
		}

	}

	public static void computeCentralityInTree(int k, int dataSize, int numCenters, CoverTreeInstanceDataset cd,
			double[][] distances, double[][] synData, String saveDir) throws IOException {

		System.out.println("k:" + k);
		int[] node = new int[dataSize];
		for (int i = 0; i < dataSize; i++)
			node[i] = i;
		Centrality[] nodesCentrality = cd.getCoverTree().centrality(node, k);

		File dir = new File(BuildSuperStoreData.dataDir, "Synthetic-data-Centrality");
		if (!dir.exists())
			dir.mkdir();
		dir = new File(dir, "[dataSize=" + dataSize + ",k=" + k + "]");
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

		Centrality[] centralities = new Centrality[numCenters];
		for (int i = 0; i < numCenters; i++)
			centralities[i] = nodesCentrality[i]; // get k candidate centrality
													// node for whole node
		for (int i = 0; i < centralities.length; i++) {
			System.out.print(centralities[i].id + ",");
		}
		System.out.println();

		int treeSize = 2;
		int treeSizeMax = 500;// define test range

		coverForestIncreasedMatchedNum(cd, numCenters, treeSize, treeSizeMax, centralities, k ,saveDir);
	}

	public static int[] coverForestIncreasedMatchedNum(IDistanceHolder cd, int numCenters, int treeSize,
			int treeSizeMax, Centrality[] centralities, int k,String saveDir) throws IOException {
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
			treeNodeCentrality[i] = cts[i].getKCentralityCenterNodePairs(numCenters,k);// get
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

		File file = new File(saveDir + "_CoverForestIncreasedMatchNum_Statisitic.csv");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < matchedNodeNum.length; i++) {
			bWriter.write((treeSize + i) + "," + (matchedNodeNum[i] / (double) numCenters) + "\n");
		}
		bWriter.close();
		System.out.println("saved match result in " + file.getPath());

		return matchedNodeNum;
	}
}
