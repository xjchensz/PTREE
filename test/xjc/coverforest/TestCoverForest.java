package xjc.coverforest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.primitives.Doubles;

import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.numeric.DenseDoubleInstance;
import common.data.meta.MetaData;
import common.utils.collection.OrderedDoubleMap;
import common.utils.collection.OrderedIntArraySet;
import common.utils.collection.OrderedIntMap;
import test.dataGenerator.DoubleInstanceGenerator;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.clustering.validation.DunnIndex;
import xjc.clustering.validation.NormalizedLogW;
import xjc.clustering.validation.SilhouetteIndex;
import xjc.coverforest.syntheicData.ComputeDistribution;
import xjc.coverforest.syntheicData.TestCoverForestOnSyntheticData;
import xjc.coverforest.syntheicData.TestCoverForestOnSyntheticData2;
import xjc.covertree.Centrality;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.IDistanceHolder;
import xjc.covertree.INode;
import xjc.covertree.levelnode.ILevelNode;
import xjc.covertree.levelnode.LevelChildCoverTree;
import xjc.covertree.levelnode.LevelChildCoverTreeInstanceDataset;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.AbstractDataset;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

public class TestCoverForest {
	static int radiusK=5;
	
	static boolean hasLabel=false;
	static  int labelDimenOffset=0;
	public static void main(String[] args) throws Exception{
		int gaussianDataSize=1000;
		int dataSize=gaussianDataSize*3/2;
		//
		//String syntheticDataPath=BuildResultStore.dataDir.getAbsolutePath()+"/wine/wine.data";
		String syntheticDataPath="D:/Project/java_project/CoverForest2/PTree/trunk/PTree/sd1/gaussian/g0.csv";
		File syntheticDataFile=new File(syntheticDataPath);
		BufferedReader br=new BufferedReader(new FileReader(syntheticDataFile));
		String ts="";
		//if(!syntheticDataPath.contains("label")) hasLabel=false;
		
		int dimenNum=2;
		
		double[][] synData=new double[dataSize][dimenNum]; 
		double[] labels=new double[dataSize];
		if(hasLabel) labelDimenOffset=1;
		int index=0;
		while((ts=br.readLine())!=null){

			String[] dimenValueStr=ts.split(",");
			for(int i=0;i<dimenNum;i++){
				//System.out.print(i+":"+dimenValueStr[i]+" ");
				synData[index][i]=Double.parseDouble(dimenValueStr[i+labelDimenOffset]);
			}
			//System.out.println();
			labels[index]=Double.valueOf(dimenValueStr[0]);
			index++;
		}
		System.out.println("dataSize:"+index);
		dataSize=index;
		DoubleInstanceGenerator sg = new DoubleInstanceGenerator();
		MetaData md = sg.generateMetaData("a", "a", 100, 1000, new Random(), true);
		CoverTreeInstanceDataset cd = new CoverTreeInstanceDataset(EuclideanDistanceMeasure.getInstance());
		
		double base=2;
		int treeSize=2; int treeSizeMax=100;//define test range
		CoverForestInstanceDataset cfd=new CoverForestInstanceDataset(EuclideanDistanceMeasure.getInstance(), base,treeSizeMax);
		
		for(int i=0;i<dataSize;i++){
			DenseDoubleInstance sdi =new DenseDoubleInstance(i, md);
			for(int j=0;j<dimenNum;j++){
				sdi.setValue(j, synData[i][j]);			
			}
			
			sdi.setLabel(labels[i]);
			cd.addInstance(sdi);
			cfd.addInstance(sdi);
		} 
		//double[][] distances=cd.distances();
		//System.out.println("finished compute distance");
				
		CenterMeasureType cmt=CenterMeasureType.LEVEL_DENSITY;
		//CoverForestInstanceDataset cfd=new CoverForestInstanceDataset(EuclideanDistanceMeasure.getInstance(), base, treeSizeMax);
		//computeCoverForestTreesPartitionOccupiedCountMap(cd, treeSizeMax, treeSizeMax, numCenter, cmt, bound);
		
		treeSize=2;int offset=2;treeSizeMax=20;int[]treeSizes=new int[(int)Math.ceil((double)(treeSizeMax-treeSize)/offset)];
		for(int i=0;treeSize<treeSizeMax;treeSize+=offset,i++){
			treeSizes[i]=treeSize;
		}
	
		//CoverForest cf=CoverForestFactory.getDefault().create(cd, base, treeSizeMax);
		//cf.buildCoverForest();
		
		cfd.buildCoverForest(new Random());
	    try {
	    	System.out.println("finish building "+treeSizeMax+" trees");
	    	System.out.println("sleep 2000");
            Thread.sleep(2000);
            System.out.println("continue");
        } catch (InterruptedException e) {
            e.printStackTrace(); 
        }
  

		int numCenter=3;
		int numCenterOffset=0; int numCenterMax=10;
		 int delta = 1;
		 
		double[][] distances=cd.distances();
		//TestCoverForestOnSyntheticData.computeCentralityClusterIndice(cd, distances, numCenter, numCenterMax, numCenterOffset, delta);

		
		
		//test for wine 
		File  savePathDir=new File(BuildResultStore.resultDir,"TreeCenter-PartitionOccupedResult/coverforest/wine/partitionOccupiedRate");
		
		int[] numCenters=new int[(int)(Math.ceil((double)(numCenterMax-numCenter))/delta)];
		for(int i=0;numCenter+numCenterOffset<numCenterMax;numCenterOffset+=delta,i++){
			numCenters[i]=numCenter+numCenterOffset;
		}	
		int numLabel=3;
		//ComputeDistribution.computeCoverForestTreesPartitionOccupiedCountMap(savePathDir,cfd, treeSizes, numCenters, cmt, numLabel);

/*		// double base =2.0; int treeSize =10;
		// CoverForest cf = CoverForestFactory.getDefault().create(cd, base, treeSize);
		int radiusK=numCenter;
		System.out.println("kradius-K:" + radiusK);
		int[] node = new int[dataSize];
		for (int i = 0; i < dataSize; i++)
			node[i] = i;
		Centrality[] nodesCentrality = cd.getCoverTree().centrality(node, radiusK);

		File dir = new File(BuildResultStore.resultDir, "Synthetic-data-Centrality");
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

		int numCandidateCenter = 50;
		if (numCandidateCenter > dataSize)
			numCandidateCenter = dataSize;
		System.out.println("candidateCenter-num:" + numCandidateCenter);

		Centrality[] centralities = new Centrality[numCandidateCenter];
		for (int i = 0; i < numCandidateCenter; i++)
			centralities[i] = nodesCentrality[i]; // get k candidate centrality
													// node for whole node
		for (int i = 0; i < centralities.length; i++) {
			System.out.print(centralities[i].id + ",");
		}
		System.out.println();
		 
		cf.setTreeInsSize(cd.size()); cf.buildCoverForest(cd, base,treeSize);
		TestCoverForestOnSyntheticData.computeCentralityClusterOnForestIndice(cf, distances,centralities, numCenter, numCenterMax, numCentetOffset, delta);
		*/
		
		
		
		CoverForest cf=cfd.getCoverForest();
		/*		
		File measureResultRootDir=new File(syntheticDataFile.getParent(),"measureCluster");
		if(!measureResultRootDir.exists()) measureResultRootDir.mkdir(); 
		for(int i=0;i<numCenters.length;i++){
			File measureResultDir=new File(measureResultRootDir,"b_"+base+"_ts_"+treeSizeMax+"_numCenter_"+numCenters[i]);
			if(!measureResultDir.exists()) measureResultDir.mkdir(); 
			cf.saveKCentralityCenterNodePairsInTrees(measureResultDir,cf.getKCentralityCenterInTrees(radiusK,numCenters[i]));
			System.out.println();
			cf.saveKCentralityCenterNodePairsInForest(measureResultDir,cf.getKCentralityCenterNodePairs(radiusK,numCenters[i]));
		}
		 */
		cf.saveCoverTreesInForest(syntheticDataFile.getParentFile());
		
	}
	
	
	public static void realLevelDensity(StringBuilder s,CoverTreeInstanceDataset ctd,int level, OrderedIntMap map) {
		s.append("level-"+level+":");s.append("\n");
		System.out.println("level-"+level+":");
		CoverTree cTree=ctd.getCoverTree();
		double base=cTree.getBase();
		int tsize=cTree.size();
		INode rootNode=cTree.getRootNode();
		int maxLevel=cTree.maxLevel();
		int minLevel=cTree.minLevel();
		
		//int size=cTree.size();
	//	CoverTreeInstanceDataset m_DistanceHolder=ctd;//cTree.getDistanceHolder();
		
		ArrayList<INode> tmpNodes1 = new ArrayList<INode>();
		ArrayList<INode> tmpNodes2 = new ArrayList<INode>();
		ArrayList<INode> tmpNodes3 = new ArrayList<INode>();
		ArrayList<INode> tmpNodes4 = new ArrayList<INode>();
		OrderedDoubleMap tmpMap = new OrderedDoubleMap();
		OrderedIntArraySet os = new OrderedIntArraySet();
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
			for (j = tmpNodes3.size() - 1; j >= 0; j--) {
				children = tmpNodes3.get(j).getChildren();
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

		//System.out.println("level-"+level+":");
		for (int i = 0; i < tmpNodes3.size(); i++) {
			os.add(tmpNodes3.get(i).getInstance());
			s.append(tmpNodes3.get(i).getInstance()+",");s.append("\n");
			//System.out.print(tmpNodes3.get(i).getInstance()+",");
		}
		
		double threshold=Math.pow(base,level);
		//System.out.println();
		for(int i=0;i<os.size();i++){
			int count=0;
			int ins=os.getValueAt(i);
			for(int j=0;j<ctd.size();j++){
				if(ctd.distance(j, ins)<=threshold)count++;
			}
			map.put(ins, count);
		}

	
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpMap.clear();
		os.clear();
	}
	
/*	public static void main(String[] args) throws Exception {

		
		//int[] treeSizes={3};
		 int[] treeSizes={2,3,4,5};
		//int[] treeSizes = { 5, 6, 7, 8, 9, 10, 11, 12 };

		int[] numCenters = { 5 };
		// testKRandomCoverTree(degree,baseBegin,baseEnd,offset);

		double[] gammas = { 0, 0.2, 0.8, 1, 2, 8, 1000 };
		double[] bases = { 2 };

		String[] indiceKey={"logWk","Dunindex"};
		
		measureClusterInPurchaseData(treeSizes, numCenters, gammas[1], bases[0],indiceKey);

	}*/

	static void measureClusterInPurchaseData(int[] treeSizes, int[] numCenters, double gamma, double base,String[] indiceKey)
			throws IOException {
		// testBuildKRandomCoverTree(degree,gammas,bases);
		File dir = new File("SuperStore", "data");
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);

		double[][] distances = dis.distances();
		System.out.println("Finished compute distance Matrix : gamma=" + gamma);

		for (int i = 0; i < treeSizes.length; i++) {
			System.out.println("#############################################");
			System.out.println("Tree-size:" + treeSizes[i]);
			for (int j = 0; j < numCenters.length; j++) {
				System.out.println("NumCenter:" + numCenters[j]);
				measureCluster(dis, base, treeSizes[i], numCenters[j], distances,indiceKey);
			}

		}
	}

	static void measureCluster(IDistanceHolder dis, Double base, int treeSize, int numCenters, double[][] distances,String[] indiceskey)
			throws IOException {
		File measureResultDir=new File(BuildSuperStoreData.dataDir,"measureCluster");
		if(!measureResultDir.exists()) measureResultDir.mkdir(); 
		measureResultDir=new File(measureResultDir,"b_"+base+"_ts_"+treeSize+"_numCenter_"+numCenters);
		if(!measureResultDir.exists()) measureResultDir.mkdir(); 
		
		CoverForest cf = CoverForestFactory.getDefault().create(dis, base, treeSize);
		cf.setTreeInsSize(dis.size());
		cf.buildCoverForest(dis, base, treeSize);
		CoverTree[] cts = cf.getCoverTrees();

		int[] centers = cf.getKCentralityCenters(radiusK,numCenters);

		cf.saveKCentralityCenterNodePairsInTrees(measureResultDir,cf.getKCentralityCenterInTrees(radiusK,numCenters));
		System.out.println();
		cf.saveKCentralityCenterNodePairsInForest(measureResultDir,cf.getKCentralityCenterNodePairs(radiusK,numCenters));

		// cf.printClusterResult(numCenters, null, new Random());

		//bufferedWriter write measureIndice to file
		BufferedWriter bw=new BufferedWriter(new FileWriter(new File(measureResultDir,"measureIndice.csv"))); 
		int[][] partitions = cf.getPartition(numCenters,radiusK, CenterMeasureType.CENTRALITY, new Random());
		double logwk = NormalizedLogW.logwk(partitions, distances);
		double dunnIndex = DunnIndex.di(partitions, distances);
		//double normalizeLogw = NormalizedLogW.NLW(partitions, distances);
		//double silhouetteIndex = SilhouetteIndex.si(partitions, distances);

		System.out.println("logwk:" + logwk);
		//System.out.println("normalizeLogw:" + normalizeLogw);
		
		System.out.println("dunnIndex:" + dunnIndex);
		//System.out.println("silhouetteIndex:" + silhouetteIndex);
		System.out.println();
		bw.write("forest-id,logwk,dunnIndex\n");
		bw.write(0+","+logwk+","+dunnIndex+"\n\n");

		double[] logwkAry = new double[treeSize];
		//double[] normalizeLogwAry = new double[treeSize];
		double[] dunnIndexAry = new double[treeSize];
		//double[] silhouetteIndexAry = new double[treeSize];
		Random random = new Random();
		
		
		bw.write("tree-id,logwk,dunnIndex\n");
		for (int i = 0; i < cts.length; i++) {
			CoverTree ct=cf.getCoverTree(i);
			
			int[][] partitions2 =AbstractDataset.getPartition(ct.clustering(numCenters, CenterMeasureType.LEVEL_DENSITY, new Random()));

			// calculate cluster cris
			logwkAry[i] = NormalizedLogW.logwk(partitions2, distances);
			dunnIndexAry[i] = DunnIndex.di(partitions2, distances);
			bw.write(i+","+logwkAry[i]+","+dunnIndexAry[i]+"\n");
			//normalizeLogwAry[i] = NormalizedLogW.NLW(partitions2, distances);
			//silhouetteIndexAry[i] = SilhouetteIndex.si(partitions2, distances);
		}		
		
		bw.close();
		System.out.println("saved measure indice in measureIndice.csv");
		
		System.out.println("logWk:");
		for (int i = 0; i < treeSize; i++){
			System.out.println(i + ">" + logwkAry[i]);
		}			
		System.out.println();

		/*System.out.println("normalizeLogw:");
		for (int i = 0; i < treeSize; i++)
			System.out.println(i + ">" + normalizeLogwAry[i]);
		System.out.println();*/

		System.out.println("dunnIndex:");
		for (int i = 0; i < treeSize; i++)
			System.out.println(i + ">" + dunnIndexAry[i]);
		System.out.println();

		/*System.out.println("silhouetteIndex:");
		for (int i = 0; i < treeSize; i++)
			System.out.println(silhouetteIndexAry[i]);
		System.out.println();*/
	}
	
	public static void printKCentralityCenterNodePairs(Centrality[] doubleNodes,int treeId,BufferedWriter abw) throws IOException{
		
		StringBuilder s=new StringBuilder();
		//DoubleNode[] doubleNodes=getKCentralityCenterNodePairs(numCenters);
		
		
		s.append("Tree "+treeId+" :\n");
		
		for( int j=0;j<doubleNodes.length;j++){
			//abw.write(""+doubleNodes[j].id+"   /"+doubleNodes[j].centrality+",");
			s.append(j+">"+doubleNodes[j].id+":"+doubleNodes[j].centrality+"\n");
		}

		abw.write("Tree "+treeId+" :\n");
		// format centrality output  
		int[] ids=new int[doubleNodes.length];
		double[] cdists=new double[doubleNodes.length];
		double[] knnRs=new double[doubleNodes.length];
		double[] centralitys=new double[doubleNodes.length];
		for(int j=0;j<doubleNodes.length;j++){
			ids[j]=doubleNodes[j].getID();
			cdists[j]=doubleNodes[j].getCdist();
			knnRs[j]=doubleNodes[j].getKnnradius();
			centralitys[j]= doubleNodes[j].getCentrality();
		}
		abw.write("id,"); for(int j=0;j<doubleNodes.length;j++)	abw.write(ids[j]+",");	abw.newLine();
		abw.write("cdist,"); for(int j=0;j<doubleNodes.length;j++)	abw.write(cdists[j]+",");	abw.newLine();
		abw.write("knnradius,"); for(int j=0;j<doubleNodes.length;j++)	abw.write(knnRs[j]+",");	abw.newLine();
		abw.write("centrality,"); for(int j=0;j<doubleNodes.length;j++)	abw.write(centralitys[j]+",");	abw.newLine();
		abw.newLine();
		
		
		System.out.println(s);
	}
	
	
}
