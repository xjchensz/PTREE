package xjc.coverforest.syntheicData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ujmp.core.util.Matlab;

import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.numeric.DenseDoubleInstance;
import common.data.meta.MetaData;
import common.utils.collection.OrderedDoubleMap;
import common.utils.collection.OrderedIntArrayList;
import common.utils.collection.OrderedIntArraySet;
import common.utils.collection.OrderedIntMap;
import test.dataGenerator.DoubleInstanceGenerator;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.coverforest.BuildResultStore;
import xjc.coverforest.CoverForest;
import xjc.coverforest.CoverForestFactory;
import xjc.coverforest.CoverForestInstanceDataset;
import xjc.coverforest.levelnode.LevelChildCoverForest;
import xjc.coverforest.levelnode.LevelChildCoverForestInstanceDataset;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.levelnode.ILevelNode;
import xjc.covertree.levelnode.LevelChildCoverTree;
import xjc.covertree.levelnode.LevelChildCoverTreeInstanceDataset;
import xjc.covertree.levelnode.LevelNode;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;

public class TestLevelChildCoverForest {
	static boolean hasLabel=true;
	static  int labelDimenOffset=0;
	public static void main(String[] args) throws Exception{
		int gaussianDataSize=1000;
		int dataSize=gaussianDataSize*3/2;
		//
		String syntheticDataPath=BuildResultStore.dataDir.getAbsolutePath()+"/syntheticData/syntheticData_[gaussianDataSize="+gaussianDataSize+"]/gaussian/g0-haslabel.csv";
		System.out.println("load data from "+syntheticDataPath);
		File syntheticDataFile=new File(syntheticDataPath);
		BufferedReader br=new BufferedReader(new FileReader(syntheticDataFile));
		String ts="";
		if(!syntheticDataPath.contains("label")) hasLabel=false;
		
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
		LevelChildCoverTreeInstanceDataset cd = new LevelChildCoverTreeInstanceDataset(EuclideanDistanceMeasure.getInstance());
		
		double base=2;
		int treeSize=2; int treeSizeMax=10;//define test range
		LevelChildCoverForestInstanceDataset cfd=new LevelChildCoverForestInstanceDataset(EuclideanDistanceMeasure.getInstance(), base,treeSizeMax);
		
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
		
		treeSize=2;int offset=2;treeSizeMax=10;int[]treeSizes=new int[(int)Math.ceil((double)(treeSizeMax-treeSize)/offset)];
		for(int i=0;treeSize<treeSizeMax;treeSize+=offset,i++){
			treeSizes[i]=treeSize;
		}
	
		/*CoverForest cf=CoverForestFactory.getDefault().create(cd, base, treeSizeMax);
		cf.buildCoverForest();*/
		
		cfd.buildCoverForest();
		for(int i=0;i<cfd.getTreeSize();i++){
			cfd.getCoverForest().getCoverTree(i).update();
			String resultDir=BuildResultStore.resultDir.getPath()+"/LevelChildCoverForestChildMap/syntheticData_[gaussianDataSize="+gaussianDataSize+",treeSize="+(int)cfd.getTreeSize()+"]";
			saveLevelChildCoverTreeChildMap(resultDir+"/tree-"+i, cfd.getCoverForest().getCoverTree(i), synData);
			TestLevelChildCoverTree.saveCoverTree(new File(resultDir+"/tree-"+i+"/covertree.ctr"), cfd.getCoverForest().getCoverTree(i));
		}
	    try {
	    	System.out.println("finish building "+treeSizeMax+" trees");
	    	System.out.println("sleep 2000");
            Thread.sleep(2000);
            System.out.println("continue");
        } catch (InterruptedException e) {
            e.printStackTrace(); 
        }
  
		int numCenter=5;
		int numCentetOffset=1; int numCenterMax=10;
		
 		
		File  savePathDir=new File(BuildResultStore.resultDir,"TreeCenter-PartitionOccupedResult/LevelChildCoverForest/partitionOccupiedRate");
		
		int[] numCenters=new int[(int)(Math.ceil((double)(numCenterMax-numCenter))/numCentetOffset)];
		for(int i=0;numCenter<numCenterMax;numCenter+=numCentetOffset,i++){
			numCenters[i]=numCenter;
		}
		
		//String resultDir=BuildResultStore.resultDir.getPath()+"/LevelChildCoverTreeChildMap/syntheticData_[gaussianDataSize="+gaussianDataSize+"]";
		//LevelChildCoverTree cTree=cd.getCoverTree();
		//saveLevelChildCoverTreeChildMap(resultDir, cTree, synData);
		//computeCoverForestLevelDensity(cd,null,1);
		//computeCoverForestRealLevelDensity(cd,null,1);
		
		String resultDir=BuildResultStore.resultDir.getPath()+"/LevelChildCoverForestChildMap/syntheticData_[gaussianDataSize="+gaussianDataSize+",treeSize="+(int)cfd.getTreeSize()+"]";
		//computeCoverForestLevelDensity(resultDir, cd, cfd.getCoverForest(), treeSizeMax);
		//ComputeDistribution.computeCoverForestTreesPartitionOccupiedCountMap(savePathDir,cfd, treeSizes, numCenters, cmt, bound);
		computeCoverForestRealLevelDensity(resultDir, cd, cfd.getCoverForest(), treeSizeMax);

		//ComputeDistribution.computeCoverForestTreesPartitionOccupiedCountMap(savePathDir,cf, treeSizes, numCenters, cmt, bound);

	}
	
	public static ArrayList<HashMap<Integer, OrderedIntArraySet>> getCoverTreeChildMap(String path,LevelChildCoverTree cTree,double[][] pos) throws IOException{
		//BufferedWriter bWriter=new BufferedWriter(new FileWriter(new File(path+"/coverTreeChildMap.txt")));
		
		ArrayList<HashMap<Integer, OrderedIntArraySet>> childMap=new ArrayList<HashMap<Integer,OrderedIntArraySet>>();
		//CoverTree cTree=ctd.getCoverTree();
		double base=cTree.getBase();
		int tsize=cTree.size();
		ILevelNode rootNode=cTree.getRootNode();
		int maxLevel=cTree.maxLevel();
		int minLevel=cTree.minLevel();
		
		//int size=cTree.size();
	//	CoverTreeInstanceDataset m_DistanceHolder=ctd;//cTree.getDistanceHolder();
		
		ArrayList<ILevelNode> tmpNodes1 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes2 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes3 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes4 = new ArrayList<ILevelNode>();
		OrderedDoubleMap tmpMap = new OrderedDoubleMap();
		OrderedIntArraySet os = new OrderedIntArraySet();
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpNodes1.add(rootNode);
		tmpNodes3.add(rootNode);

		List<ILevelNode> children;
		ILevelNode child;

		ArrayList<ILevelNode> tmp;
		OrderedIntArraySet neighbors = new OrderedIntArraySet();
		
	//	s.append("level-"+level+":");s.append("\n");

		for (int l = maxLevel - 1, j, k; l >= minLevel; l--) {
			int childMapLevel=maxLevel-1-l;
			
			HashMap<Integer, OrderedIntArraySet> instanceChildMap=new HashMap<Integer, OrderedIntArraySet>();
			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					OrderedIntArraySet instanceChildSet=new OrderedIntArraySet();
					
					for (k = children.size() - 1; k >= 0; k--) {
						child = children.get(k);
						instanceChildSet.add(child.getInstance());//;tmpNodes1.get(j).getInstance()+" childs "+ child.getInstance());s.append("\n");
						tmpNodes2.add(child);
					}
					instanceChildMap.put(tmpNodes1.get(j).getInstance(), instanceChildSet);
				}
				
			}
			childMap.add(instanceChildMap);
			
			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			tmpNodes3.addAll(tmpNodes1);
		}

		os.clear();
		
		return childMap;
	}
	
	public static ArrayList<HashMap<Integer, OrderedIntArraySet>> getLevelChildCoverTreeChildMap(String path,LevelChildCoverTree cTree,double[][] pos) throws IOException{
		//BufferedWriter bWriter=new BufferedWriter(new FileWriter(new File(path+"/coverTreeChildMap.txt")));
		
		ArrayList<HashMap<Integer, OrderedIntArraySet>> childMap=new ArrayList<HashMap<Integer,OrderedIntArraySet>>();
		//CoverTree cTree=ctd.getCoverTree();
		double base=cTree.getBase();
		int tsize=cTree.size();
		ILevelNode rootNode=cTree.getRootNode();
		int maxLevel=cTree.maxLevel();
		int minLevel=cTree.minLevel();
		
		 ArrayList<HashMap<Integer, OrderedIntArraySet>> childMapList=new ArrayList<HashMap<Integer, OrderedIntArraySet>>();
		 for(int i=0;i<maxLevel-minLevel;i++){
			 childMapList.add(new HashMap<Integer, OrderedIntArraySet>());
		 }

		int level = maxLevel;
		
		int minNumLevels=-500;
		
		ArrayList<ILevelNode> tmpNodes1 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes2 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes3 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes4 = new ArrayList<ILevelNode>();
		OrderedDoubleMap tmpMap = new OrderedDoubleMap();
		OrderedIntArraySet os = new OrderedIntArraySet();
		tmpNodes1.clear();
		
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes2.add(rootNode);
		List<ILevelNode> children;
		ArrayList<ILevelNode> tmp;
		ILevelNode child;
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
						int childLevel=child.getLevel();
						try{
							HashMap<Integer, OrderedIntArraySet> childLevelMap=childMapList.get(maxLevel-1-childLevel);
							OrderedIntArraySet childArry=childLevelMap.get(tmpNodes1.get(i).getInstance());
							if(childArry==null){childArry=new OrderedIntArraySet();}
							childArry.add(child.getInstance());
							childLevelMap.put(tmpNodes1.get(i).getInstance(), childArry);
							tmpNodes2.add(child);
						}
						catch(Exception exception){
							System.out.println(exception.getMessage());
						}

					}
				}
			}

			if (tmpNodes2.isEmpty()) {

				if (level == minNumLevels) {
					break;
				}
				break;
			}

			level--;
		}
		tmpMap.clear();
		tmpNodes1.clear();
		tmpNodes2.clear();
		return childMapList;
		
		
	}
	
	public static ArrayList<HashMap<ILevelNode, ArrayList<ILevelNode>>> getLevelChildCoverTreeChildMap(String path,LevelChildCoverTree cTree) throws IOException{
		//BufferedWriter bWriter=new BufferedWriter(new FileWriter(new File(path+"/coverTreeChildMap.txt")));
		
		ArrayList<HashMap<Integer, OrderedIntArraySet>> childMap=new ArrayList<HashMap<Integer,OrderedIntArraySet>>();
		//CoverTree cTree=ctd.getCoverTree();
		double base=cTree.getBase();
		int tsize=cTree.size();
		ILevelNode rootNode=cTree.getRootNode();
		int maxLevel=cTree.maxLevel();
		int minLevel=cTree.minLevel();
		
		ArrayList<HashMap<ILevelNode, ArrayList<ILevelNode>>> childMapList=new ArrayList<HashMap<ILevelNode, ArrayList<ILevelNode>>>();
		 for(int i=0;i<maxLevel-minLevel;i++){
			 childMapList.add(new HashMap<ILevelNode, ArrayList<ILevelNode>>());
		 }

		int level = maxLevel;
		
		int minNumLevels=-500;
		
		ArrayList<ILevelNode> tmpNodes1 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes2 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes3 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes4 = new ArrayList<ILevelNode>();
		OrderedDoubleMap tmpMap = new OrderedDoubleMap();
		OrderedIntArraySet os = new OrderedIntArraySet();
		tmpNodes1.clear();
		
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes2.add(rootNode);
		List<ILevelNode> children;
		ArrayList<ILevelNode> tmp;
		ILevelNode child;
		boolean zerodistance = false;
		int childCount=0;
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
					childCount+=children.size();
					
					for (j = children.size() - 1; j >= 0; j--) {
						child = children.get(j);
						int childLevel=child.getLevel();
						try{
							HashMap<ILevelNode, ArrayList<ILevelNode>> childLevelMap=childMapList.get(maxLevel-1-childLevel);
							ArrayList<ILevelNode> childArry=childLevelMap.get(tmpNodes1.get(i));
							if(childArry==null){childArry=new ArrayList<ILevelNode>();}
							childArry.add(child);
							childLevelMap.put(tmpNodes1.get(i), childArry);
							tmpNodes2.add(child);
						}
						catch(Exception exception){
							System.out.println(exception.getMessage());
						}

					}
				}
			}

			if (tmpNodes2.isEmpty()) {

				if (level == minNumLevels) {
					break;
				}
				break;
			}

			level--;
		}
		tmpMap.clear();
		tmpNodes1.clear();
		tmpNodes2.clear();
		System.out.println("child-count= "+childCount);
		return childMapList;
		
		
	}
	
	public static void saveLevelChildCoverTreeChildMap(String resultDir,LevelChildCoverTree cTree,double[][] synData) throws IOException{

			int maxLevel=cTree.maxLevel();
			int minLevel=cTree.minLevel();
			//ArrayList<HashMap<Integer, OrderedIntArraySet>> childLevelMap=getCoverTreeChildMap(BuildResultStore.resultDir.getPath(), cTree, synData);
		    
			ArrayList<HashMap<ILevelNode, ArrayList<ILevelNode>>> childLevelMap=getLevelChildCoverTreeChildMap(BuildResultStore.resultDir.getPath(), cTree);

			int targetLevel=minLevel+1;
		    
		    int pInsCount=0;  int cInsCount=0;
		    for(targetLevel=maxLevel-1;targetLevel>=minLevel;targetLevel--){
		    	System.out.println("level-"+targetLevel);
			    int levelMapIndex=maxLevel-1-targetLevel;
				Set<ILevelNode> pInstances=childLevelMap.get(levelMapIndex).keySet();//get level parent instance
				System.out.print("p:");
				Iterator<ILevelNode> itrInteger=pInstances.iterator();
				while(itrInteger.hasNext()){
					System.out.print(itrInteger.next().getInstance()+",");
				}
				System.out.println();
				
				//Set<Integer> cInstances=childLevelMap.get(levelMapIndex+1).keySet();//get level child instance
				Collection<ArrayList<ILevelNode>> tmOIArySetCollection=childLevelMap.get(levelMapIndex).values();
				Iterator<ArrayList<ILevelNode>> itrOIAry=tmOIArySetCollection.iterator();
				System.out.print("c:");
				while(itrOIAry.hasNext()){
					ArrayList<ILevelNode> tmIntArraySet=itrOIAry.next();
					for(int index2=0;index2<tmIntArraySet.size();index2++){
						System.out.print(tmIntArraySet.get(index2).getInstance()+",");
					}
					cInsCount+=tmIntArraySet.size();
				}
				System.out.println();
				pInsCount+=pInstances.size();
				
		    }

			System.out.println("finished at "+pInsCount+" "+cInsCount);;
			
			StringBuilder childListStr=new StringBuilder();
		    for(targetLevel=maxLevel-1;targetLevel>=minLevel;targetLevel--){
		    	System.out.println("level-"+targetLevel);
			    int levelMapIndex=maxLevel-1-targetLevel;
				Set<ILevelNode> pInstances=childLevelMap.get(levelMapIndex).keySet();//get level parent instance
				HashMap<ILevelNode, ArrayList<ILevelNode>> tHashMap=childLevelMap.get(levelMapIndex);
				Iterator<ILevelNode> itrInteger=pInstances.iterator();
				while(itrInteger.hasNext()){
					ILevelNode pIns=itrInteger.next();
					System.out.print(pIns+",");
					ArrayList<ILevelNode> tIntArraySet=tHashMap.get(pIns);
					for(int indice=0;indice<tIntArraySet.size();indice++){
						double cpdis=cTree.getDistanceHolder().distance(pIns.getInstance(), tIntArraySet.get(indice).getInstance());
						double clevel=Math.log(cpdis)/Math.log(2.0);
						//childListStr.append(pIns.getInstance()+","+pIns.getLevel()+","+tIntArraySet.get(indice).getInstance()+","+tIntArraySet.get(indice).getLevel()+",<"+Math.ceil(clevel)+","+Math.floor(clevel)+">\n");
						childListStr.append(pIns.getInstance()+","+pIns.getLevel()+","+tIntArraySet.get(indice).getInstance()+","+tIntArraySet.get(indice).getLevel()+"\n");

					}
				}
				//System.out.println();
				
		    }
		    System.out.println(childListStr.toString());
		    
		    String childListFilePath=resultDir;//BuildSuperStoreData.dir+"/result";
		    File childListFile=new File(childListFilePath);
		    if(!childListFile.exists()) childListFile.mkdirs();
		    childListFile=new File(childListFilePath+"/childList.csv");
		    BufferedWriter bWriter=new BufferedWriter(new FileWriter(childListFile));
		    bWriter.write(childListStr.toString());
		    bWriter.flush();
		    bWriter.close();
		    System.out.println("save childMapList in "+childListFile.getAbsolutePath());
	}
	
	
	
	
	
	
	public static void computeCoverForestLevelDensity2(CoverTreeInstanceDataset cd,CoverForest cf,int treeSizeMax) throws IOException{
		OrderedIntMap map=new OrderedIntMap();
	    
	    CoverTree[] cts=cf.getCoverTrees();
	    StringBuilder childMap=new StringBuilder();
	    StringBuilder levelDensityMsgStr=new StringBuilder();
	    int min_minlevelInForest=Integer.MAX_VALUE;
	    int max_maxLevelInForest=Integer.MIN_VALUE;
	    for(int i=0;i<cts.length;i++){
	    	if(min_minlevelInForest<cts[i].minLevel())
	    		min_minlevelInForest=cts[i].minLevel();
	    	if(max_maxLevelInForest>cts[i].maxLevel())
	    		max_maxLevelInForest=cts[i].maxLevel();
	    }
	    int gaussianDataSize=cd.size();
	    
	    File levelDensityFileDir=new File(BuildSuperStoreData.dir+"/result/SyntheticData/syntheticData_[gaussianDataSize="+gaussianDataSize+",treeSize="+treeSizeMax+"]");
	    if(!levelDensityFileDir.exists()) levelDensityFileDir.mkdirs();
	    
	    BufferedWriter bw;//=new BufferedWriter(new FileWriter(levelDensityFile));
	    
	    StringBuilder tStringBuilder=new StringBuilder();
	    for(int ti=0;ti<cts.length;ti++){
	    	System.out.println("cover tree_"+ti+" levelDensity");
	    	int maxLevel=cts[ti].maxLevel();int minLevel=cts[ti].minLevel();
		    for(int level=maxLevel;level>minLevel;level--){			    	
					map.clear();
					cts[ti].levelDensity(level, map);

					int[] key=map.keys();
					for(int j=0;j<map.size();j++){
						levelDensityMsgStr.append(level+","+key[j]+","+map.get(key[j])+","+cd.get(key[j]).getLabel()+"\n");
					}

		    }
		    File levelDensityFile=new File(levelDensityFileDir.getPath()+"/tree-"+ti+"/levelDensity.csv");
		    bw=new BufferedWriter(new FileWriter(levelDensityFile));
		    bw.write(levelDensityMsgStr.toString());
		    bw.close();
		    levelDensityMsgStr.delete(0, levelDensityMsgStr.length());
		    //levelDensityMsgStr=new StringBuilder();
		    System.out.println("save cover forest-<tree_"+ti+">level-density result in "+levelDensityFile.getAbsolutePath());
	    }
	}
	
	public static void computeCoverForestLevelDensity(String resultDir,LevelChildCoverTreeInstanceDataset cd,LevelChildCoverForest cf,int treeSizeMax) throws IOException{
		OrderedIntMap map=new OrderedIntMap();
		LevelChildCoverTree cts[];
		
	    if(cf!=null){
	    	cts=cf.getCoverTrees();
	    }
	    else {	cts=new LevelChildCoverTree[]{cd.getCoverTree()};}
	    
	    StringBuilder childMap=new StringBuilder();
	    StringBuilder levelDensityMsgStr=new StringBuilder();
	    int min_minlevelInForest=Integer.MAX_VALUE;
	    int max_maxLevelInForest=Integer.MIN_VALUE;
	    for(int i=0;i<cts.length;i++){
	    	if(min_minlevelInForest<cts[i].minLevel())
	    		min_minlevelInForest=cts[i].minLevel();
	    	if(max_maxLevelInForest>cts[i].maxLevel())
	    		max_maxLevelInForest=cts[i].maxLevel();
	    }
	    int gaussianDataSize=cd.size();
	    
	    //File levelDensityFileDir=new File(BuildResultStore.resultDir+"/SyntheticData/syntheticData_[gaussianDataSize="+gaussianDataSize+",treeSize="+treeSizeMax+"]");
	    File levelDensityFileDir=new File(resultDir);

	    if(!levelDensityFileDir.exists()) levelDensityFileDir.mkdirs();
	    
	    BufferedWriter bw;//=new BufferedWriter(new FileWriter(levelDensityFile));
	    
	    StringBuilder tStringBuilder=new StringBuilder();
	    for(int ti=0;ti<cts.length;ti++){
	    	cts[ti].update();
	    	cts[ti].update();
	    	System.out.println("cover tree_"+ti+" levelDensity");
	    	int maxLevel=cts[ti].maxLevel();int minLevel=cts[ti].minLevel();
		    for(int level=maxLevel;level>minLevel;level--){			    	
					map.clear();
					cts[ti].levelDensity2(tStringBuilder,level, map);

					int[] key=map.keys();
					for(int j=0;j<map.size();j++){
						levelDensityMsgStr.append(level+","+key[j]+","+map.get(key[j])+","+cd.get(key[j]).getLabel()+"\n");
					}

		    }
		    File treeDir=new File(levelDensityFileDir.getPath(),"/tree-"+ti);
		    if(!treeDir.exists())
		    	treeDir.mkdirs();
		    File levelDensityFile=new File(levelDensityFileDir.getPath()+"/tree-"+ti+"/levelDensity.csv");
		    bw=new BufferedWriter(new FileWriter(levelDensityFile));
		    bw.write(levelDensityMsgStr.toString());
		    bw.close();
		    levelDensityMsgStr.delete(0, levelDensityMsgStr.length());
		    //levelDensityMsgStr=new StringBuilder();
		    System.out.println("save cover forest-<tree_"+ti+">level-density result in "+levelDensityFile.getAbsolutePath());
	    }
	}
	
	public static void computeCoverForestRealLevelDensity(String resultDir,LevelChildCoverTreeInstanceDataset cd,LevelChildCoverForest cf,int treeSizeMax) throws IOException{
		OrderedIntMap map=new OrderedIntMap();
		LevelChildCoverTree cts[];
		
	    if(cf!=null){
	    	cts=cf.getCoverTrees();
	    }
	    else {	cts=new LevelChildCoverTree[]{cd.getCoverTree()};}
	    
	    StringBuilder childMap=new StringBuilder();
	    StringBuilder levelDensityMsgStr=new StringBuilder();
	    int min_minlevelInForest=Integer.MAX_VALUE;
	    int max_maxLevelInForest=Integer.MIN_VALUE;
	    for(int i=0;i<cts.length;i++){
	    	if(min_minlevelInForest<cts[i].minLevel())
	    		min_minlevelInForest=cts[i].minLevel();
	    	if(max_maxLevelInForest>cts[i].maxLevel())
	    		max_maxLevelInForest=cts[i].maxLevel();
	    }
	    int gaussianDataSize=cd.size();
	    
	    //File levelDensityFileDir=new File(BuildResultStore.resultDir+"/SyntheticData/syntheticData_[gaussianDataSize="+gaussianDataSize+",treeSize="+treeSizeMax+"]");
	    File levelDensityFileDir=new File(resultDir);

	    if(!levelDensityFileDir.exists()) levelDensityFileDir.mkdirs();
	    
	    BufferedWriter bw;//=new BufferedWriter(new FileWriter(levelDensityFile));
	    
	    StringBuilder tStringBuilder=new StringBuilder();
	    for(int ti=0;ti<cts.length;ti++){
	    	cts[ti].update();
	    	cts[ti].update();
	    	System.out.println("cover tree_"+ti+" levelDensity");
	    	int maxLevel=cts[ti].maxLevel();int minLevel=cts[ti].minLevel();minLevel=maxLevel-7;
		    for(int level=maxLevel;level>minLevel;level--){			    	
					map.clear();
					cts[ti].realLevelDensity(tStringBuilder,level, map);

					int[] key=map.keys();
					for(int j=0;j<map.size();j++){
						levelDensityMsgStr.append(level+","+key[j]+","+map.get(key[j])+","+cd.get(key[j]).getLabel()+"\n");
					}

		    }
		    File treeDir=new File(levelDensityFileDir.getPath(),"/tree-"+ti);
		    if(!treeDir.exists())
		    	treeDir.mkdirs();
		    File levelDensityFile=new File(levelDensityFileDir.getPath()+"/tree-"+ti+"/realLevelDensity.csv");
		    bw=new BufferedWriter(new FileWriter(levelDensityFile));
		    bw.write(levelDensityMsgStr.toString());
		    bw.close();
		    levelDensityMsgStr.delete(0, levelDensityMsgStr.length());
		    //levelDensityMsgStr=new StringBuilder();
		    System.out.println("save cover forest-<tree_"+ti+">level-density result in "+levelDensityFile.getAbsolutePath());
	    }
	}
	
	public static void realLevelDensity(StringBuilder s,LevelChildCoverTreeInstanceDataset ctd,int level, OrderedIntMap map) {
		s.append("level-"+level+":");s.append("\n");
		System.out.println("level-"+level+":");
		LevelChildCoverTree cTree=ctd.getCoverTree();
		double base=cTree.getBase();
		int tsize=cTree.size();
		ILevelNode rootNode=cTree.getRootNode();
		int maxLevel=cTree.maxLevel();
		int minLevel=cTree.minLevel();
		
		//int size=cTree.size();
	//	CoverTreeInstanceDataset m_DistanceHolder=ctd;//cTree.getDistanceHolder();
		
		ArrayList<ILevelNode> tmpNodes1 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes2 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes3 = new ArrayList<ILevelNode>();
		ArrayList<ILevelNode> tmpNodes4 = new ArrayList<ILevelNode>();
		OrderedDoubleMap tmpMap = new OrderedDoubleMap();
		OrderedIntArraySet os = new OrderedIntArraySet();
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpNodes1.add(rootNode);
		tmpNodes3.add(rootNode);

		List<ILevelNode> children;
		ILevelNode child;

		ArrayList<ILevelNode> tmp;
		OrderedIntArraySet neighbors = new OrderedIntArraySet();

		for (int l = maxLevel - 1, j, k; l >= level; l--) {
			tmpNodes2.clear();
			for (j = tmpNodes3.size() - 1; j >= 0; j--) {
				children = tmpNodes3.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						child = children.get(k);
						
						if(child.getLevel()==l)	tmpNodes2.add(child);
					}
				}
			}
			//tmpNodes2.add(tmpNodes3.get(j));

/*			tmp = tmpNodes1;
			tmpNodes1 = tmpNodes2;
			tmpNodes2 = tmp;
			tmpNodes2.clear();
			tmpNodes3.addAll(tmpNodes1);*/
			tmpNodes3.addAll(tmpNodes2);
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
	
}
