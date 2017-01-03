package xjc.coverforest.syntheicData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import sun.security.util.Length;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.coverforest.CoverForest;
import xjc.coverforest.CoverForestFactory;
import xjc.coverforest.CoverForestInstanceDataset;
import xjc.coverforest.levelnode.LevelChildCoverForest;
import xjc.coverforest.levelnode.LevelChildCoverForestInstanceDataset;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.IDistanceHolder;
import xjc.covertree.NumericInstanceDistanceHolder;
import xjc.covertree.levelnode.LevelChildCoverTree;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;

public class ComputeDistribution {
	public static void main(String[] args) {
		System.out.println(ComputeDistribution.class);
		}
	
	// get centers of cover trees in cover forest,and compute center distribution ,return centerDistributionMap
/*	tree-1
	p0:
	p1:241,227,157,349,69,
	p2:487,448,
	p3:673,745,709,		
	p4:*/
	public static PartitionResult[]  computeCoverForestCentersDistribution(IDistanceHolder cd,int treeSize,int numCenter,CenterMeasureType cmt,int numLabel) throws IOException, ClassNotFoundException{
		if(cd.getClass()!=LevelChildCoverForestInstanceDataset.class){
			CoverForest cf;
			NumericInstanceDistanceHolder nid=(NumericInstanceDistanceHolder)cd;
			if(cd.getClass()==CoverForestInstanceDataset.class)
				cf=((CoverForestInstanceDataset)cd).getCoverForest();
			else if(cd.getClass()==CoverTreeInstanceDataset.class){
				double base=2.0;
				cf = CoverForestFactory.getDefault().create(cd, base, treeSize);
				cf.setTreeInsSize(cd.size());
				cf.buildCoverForest(cd, base, treeSize);
				System.out.println("cover forest build finished, tree size="+treeSize);
			}else {
				cf=new CoverForest();
				return null;
			}
				
			CoverTree[] cts = cf.getCoverTrees();
			PartitionResult[] centerDistributionResults=new PartitionResult[treeSize];
			for(int i=0;i<treeSize;i++){
				System.out.println("tree-"+i);
				int[] centers=cts[i].getKcenters(numCenter, cmt);
				PartitionResult tpResult=new PartitionResult(numCenter, null, centers, nid);
				tpResult.initCenterDistributionMap();
				tpResult.updateCenterDistribution();
				centerDistributionResults[i]=tpResult;
				System.out.println(tpResult.printCenterDistribution());
			}
			return centerDistributionResults;
		}
		else{
			LevelChildCoverForest cf=((LevelChildCoverForestInstanceDataset)cd).getCoverForest();
			LevelChildCoverTree[] cts =cf.getCoverTrees();
			PartitionResult[] centerDistributionResults=new PartitionResult[cts.length];
			NumericInstanceDistanceHolder nid=(NumericInstanceDistanceHolder)cd;
			for(int i=0;i<treeSize;i++){
				System.out.println("tree-"+i);
				int[] centers=cts[i].getKcenters(numCenter, cmt);
				PartitionResult tpResult=new PartitionResult(numCenter, null, centers, nid);
				tpResult.initCenterDistributionMap();
				tpResult.updateCenterDistribution();
				centerDistributionResults[i]=tpResult;
				//System.out.println(tpResult.printCenterDistribution());
			}
			return centerDistributionResults;
		}
	}
	public static PartitionResult[]  computeCoverForestCentersDistribution(CoverForest cf,int treeSize,int numCenter,CenterMeasureType cmt,int[][] bound) throws IOException, ClassNotFoundException{
		
		CoverTree[] cts = cf.getCoverTrees();
		PartitionResult[] centerDistributionResults=new PartitionResult[cts.length];
		for(int i=0;i<cts.length;i++){
			//System.out.println("tree-"+i);
			int[] centers=cts[i].getKcenters(numCenter, cmt);
			PartitionResult tpResult=new PartitionResult(numCenter, null, centers, bound);
			tpResult.initCenterDistributionMap();
			tpResult.updateCenterDistribution();
			centerDistributionResults[i]=tpResult;
			//System.out.println(tpResult.printCenterDistribution());
		}
		return centerDistributionResults;
	}
	
	// get centers of cover trees in cover forest;compute center distribution ,count distribution occupied;return PartitionOccupiedMap
/*	1-matchCount:0
	1-matchMap:[]
	2-matchCount:0
	2-matchMap:[]
	3-matchCount:1
	3-matchMap:[19,]
	4-matchCount:11
	4-matchMap:[0,4,6,8,9,10,11,12,13,14,17,]
	5-matchCount:8
	5-matchMap:[1,2,3,5,7,15,16,18,]   trees' distribution of occupation to original partition ,referring to candidate centers' distribution
	*/
	public static HashMap<Integer, ArrayList<Integer>>  computeCoverForestTreeCentersPartitionOccupied(IDistanceHolder cd,int treeSize,int treeMax,int numCenter,CenterMeasureType cmt,int numLabel) throws IOException, ClassNotFoundException{
		
		PartitionResult[] centerDistributionResults=computeCoverForestCentersDistribution(cd,treeSize,numCenter,cmt,numLabel);		
		
		HashMap<Integer, ArrayList<Integer>> partitionOccupiedMap=new HashMap<Integer, ArrayList<Integer>>();
		int clusterSize=numLabel;
		for(int i=1;i<=clusterSize;i++){
			partitionOccupiedMap.put(i, new ArrayList<Integer>());
		}
		for(int i=0;i<treeSize;i++){
			PartitionResult tpResult=centerDistributionResults[i];			
			int patitionCount=0;
			for(int j=0;j<clusterSize;j++){
				if(tpResult.centerDistributionMap.get(j).size()!=0)
					patitionCount++;
			}
			partitionOccupiedMap.get(patitionCount).add(i);			
		}
		return partitionOccupiedMap;
	}
	public static HashMap<Integer, ArrayList<Integer>>  computeCoverForestTreeCentersPartitionOccupied(CoverForest cf,int treeSize,int numCenter,CenterMeasureType cmt,int numLabel) throws IOException, ClassNotFoundException{
		
		PartitionResult[] centerDistributionResults=computeCoverForestCentersDistribution(cf,treeSize,numCenter,cmt,numLabel);		
		
		HashMap<Integer, ArrayList<Integer>> partitionOccupiedMap=new HashMap<Integer, ArrayList<Integer>>();
		int clusterSize=numLabel;
		for(int i=1;i<=clusterSize;i++){
			partitionOccupiedMap.put(i, new ArrayList<Integer>());
		}
		for(int i=0;i<treeSize;i++){
			PartitionResult tpResult=centerDistributionResults[i];			
			int patitionCount=0;
			for(int j=0;j<clusterSize;j++){
				if(tpResult.centerDistributionMap.get(j).size()!=0)
					patitionCount++;
			}
			partitionOccupiedMap.get(patitionCount).add(i);			
		}
		return partitionOccupiedMap;
	}

	
	private static PartitionResult[] computeCoverForestCentersDistribution(CoverForest cf, int treeSize, int numCenter,
			CenterMeasureType cmt, int numLabel) {
		// TODO Auto-generated method stub
		return null;
	}

	// get centers of cover trees in cover forest,and compute center distribution rate ; return treeCenterDistributionRateMap;
	/*	1-matchRate:0.0
		2-matchRate:0.0
		3-matchRate:0.05
		4-matchRate:0.55
		5-matchRate:0.4     trees' distribution rate of occupation to original partition ,referring to candidate centers' distribution
		*/
	public static HashMap<Integer, Double>   computeCoverForestTreesPartitionOccupiedCountMap(File  pathDir,IDistanceHolder cd,int treeSize,int treeMax,int numCenter,CenterMeasureType cmt,int numLabel) throws IOException, ClassNotFoundException{
		//File  pathDir=new File(BuildSuperStoreData.dataDir,"TreeCenterPartitionOccupedResult");
		if(!pathDir.exists()) pathDir.mkdir();
		
		{
			File filePath= new File(pathDir,"partitionOccupiedRate-{treeSize=["+treeSize+","+treeMax+"],numCenter="+numCenter+"}");
			BufferedWriter bw=new BufferedWriter(new FileWriter(filePath));
		}
		
		HashMap<Integer, ArrayList<Integer>> partitionOccupiedMap=computeCoverForestTreeCentersPartitionOccupied(cd,treeSize,treeMax,numCenter,cmt,numLabel);
		
		int clusterSize=numLabel;
		HashMap<Integer, Double> centerDistributionRate=new HashMap<Integer,Double>();
		for(int i=1;i<=clusterSize;i++){
			ArrayList<Integer> th=partitionOccupiedMap.get(i);
			
			//print cluster partition result
/*			System.out.println(i+"-matchCount:"+th.size());
			System.out.print(i+"-matchMap:[");
			for(int j=0;j<th.size();j++){
				System.out.print(th.get(j)+",");
			}
			System.out.println("]");
			System.out.println("\t"+i+"-matchRate:"+((double)th.size())/treeSize);*/
			centerDistributionRate.put(i, ((double)th.size())/treeSize);
		}
		
		return centerDistributionRate;
	}
	
	public static HashMap<Integer, Double>   computeCoverForestTreesPartitionOccupiedCountMap(File  pathDir,CoverForest cf,int[] treeSizes,int[] numCenters,CenterMeasureType cmt,int numLabel) throws IOException, ClassNotFoundException{
		
		if(!pathDir.exists()) pathDir.mkdirs();
		StringBuilder stringBuilder=new StringBuilder();

		if(treeSizes.length>15)
			stringBuilder.append(treeSizes[0]+","+treeSizes[1]+","+treeSizes[2]+"~~~"+treeSizes[treeSizes.length-2]+","+treeSizes[treeSizes.length-1]+",");
		else
			for(int i=0;i<treeSizes.length;i++){
				stringBuilder.append(treeSizes[i]+",");
			}
		
		StringBuilder stringBuilder2=new StringBuilder();
		if(numCenters.length>15)
			stringBuilder2.append(numCenters[0]+","+numCenters[1]+","+numCenters[2]+"~~~"+numCenters[numCenters.length-2]+","+numCenters[numCenters.length-1]+",");
		else
			for(int i=0;i<numCenters.length;i++){
				stringBuilder2.append(numCenters[i]+",");
			}
		
		File filePath= new File(pathDir,"{treeSize=["+stringBuilder.toString()+"],numCenter="+stringBuilder2.toString()+"}.csv");
		BufferedWriter bw=new BufferedWriter(new FileWriter(filePath));
	
		int treeSize=0;
		int numCenter=0;
		long oldTime=System.currentTimeMillis();
		long curTime=oldTime;
		
		for(int ni=0;ni<numCenters.length;ni++){
			numCenter=numCenters[ni];
			bw.write(numCenter+",");

			for(int ti=0;ti<treeSizes.length;ti++){
				treeSize=treeSizes[ti];
				bw.write(treeSize+",");
				HashMap<Integer, ArrayList<Integer>> partitionOccupiedMap=computeCoverForestTreeCentersPartitionOccupied(cf,treeSize,numCenter,cmt,numLabel);
				
				int clusterSize=numLabel;
				HashMap<Integer, Double> centerDistributionRate=new HashMap<Integer,Double>();
				for(int i=1;i<=clusterSize;i++){
					ArrayList<Integer> th=partitionOccupiedMap.get(i);
	/*				System.out.println(i+"-matchCount:"+th.size());
					System.out.print(i+"-matchMap:[");
					for(int j=0;j<th.size();j++){
						System.out.print(th.get(j)+",");
					}
					System.out.println("]");
					System.out.println("\t"+i+"-matchRate:"+((double)th.size())/treeSize);*/
					centerDistributionRate.put(i, ((double)th.size())/treeSize);
					
					bw.write(((double)th.size())/treeSize+",");
				}
				bw.newLine();
			}
			
			curTime=System.currentTimeMillis();
			System.out.println("numCenter-"+numCenter+":"+(curTime-oldTime));
			oldTime=curTime;
		}
		
		bw.close();
		System.out.println("save TreeCentersPartitionOccupied Map in "+filePath.getAbsolutePath());
			
		return null;
	}

	public static HashMap<Integer, Double>   computeCoverForestTreesPartitionOccupiedCountMap(File  pathDir,LevelChildCoverForestInstanceDataset cfd,int[] treeSizes,int[] numCenters,CenterMeasureType cmt,int numLabel) throws IOException, ClassNotFoundException{
		
		if(!pathDir.exists()) pathDir.mkdirs();
		StringBuilder stringBuilder=new StringBuilder();

		if(treeSizes.length>3)
			stringBuilder.append(treeSizes[0]+","+treeSizes[1]+","+treeSizes[2]+"~"+treeSizes[treeSizes.length-2]+","+treeSizes[treeSizes.length-1]+",");
		else
			for(int i=0;i<treeSizes.length;i++){
				stringBuilder.append(treeSizes[i]+",");
			}
		
		StringBuilder stringBuilder2=new StringBuilder();
		if(numCenters.length>3)
			stringBuilder2.append(numCenters[0]+","+numCenters[1]+","+numCenters[2]+"~"+numCenters[numCenters.length-2]+","+numCenters[numCenters.length-1]+",");
		else
			for(int i=0;i<numCenters.length;i++){
				stringBuilder2.append(numCenters[i]+",");
			}
		
		File filePath= new File(pathDir,"{treeSize=["+stringBuilder.toString()+"],numCenter="+stringBuilder2.toString()+"}.csv");
		BufferedWriter bw=new BufferedWriter(new FileWriter(filePath));
	
		int treeSize=0;
		int numCenter=0;
		long oldTime=System.currentTimeMillis();
		long curTime=oldTime;
		
		for(int ni=0;ni<numCenters.length;ni++){
			numCenter=numCenters[ni];

			for(int ti=0;ti<treeSizes.length;ti++){
				System.out.println("numCenter-"+numCenter+"-treeSize-"+treeSizes[ti]);
				bw.write(numCenter+",");
				treeSize=treeSizes[ti];
				bw.write(treeSize+",");
				HashMap<Integer, ArrayList<Integer>> partitionOccupiedMap=computeCoverForestTreeCentersPartitionOccupied(cfd,treeSize,treeSize,numCenter,cmt,numLabel);
				
				int clusterSize=numLabel;
				HashMap<Integer, Double> centerDistributionRate=new HashMap<Integer,Double>();
				for(int i=1;i<=clusterSize;i++){
					ArrayList<Integer> th=partitionOccupiedMap.get(i);
	/*				System.out.println(i+"-matchCount:"+th.size());
					System.out.print(i+"-matchMap:[");
					for(int j=0;j<th.size();j++){
						System.out.print(th.get(j)+",");
					}
					System.out.println("]");
					System.out.println("\t"+i+"-matchRate:"+((double)th.size())/treeSize);*/
					centerDistributionRate.put(i, ((double)th.size())/treeSize);
					
					bw.write(((double)th.size())/treeSize+",");
				}
				bw.newLine();
			}
			
			curTime=System.currentTimeMillis();
			System.out.println("numCenter-"+numCenter+"-calculateTime:"+(curTime-oldTime));
			oldTime=curTime;
		}
		
		bw.close();
		System.out.println("save TreeCentersPartitionOccupied Map in "+filePath.getAbsolutePath());
			
		return null;
	}
	
public static HashMap<Integer, Double>   computeCoverForestTreesPartitionOccupiedCountMap(File  pathDir,CoverForestInstanceDataset cfd,int[] treeSizes,int[] numCenters,CenterMeasureType cmt,int numLabel) throws IOException, ClassNotFoundException{
		
		if(!pathDir.exists()) pathDir.mkdirs();
		StringBuilder stringBuilder=new StringBuilder();

		if(treeSizes.length>3)
			stringBuilder.append(treeSizes[0]+","+treeSizes[1]+","+treeSizes[2]+"~"+treeSizes[treeSizes.length-2]+","+treeSizes[treeSizes.length-1]+",");
		else
			for(int i=0;i<treeSizes.length;i++){
				stringBuilder.append(treeSizes[i]+",");
			}
		
		StringBuilder stringBuilder2=new StringBuilder();
		if(numCenters.length>3)
			stringBuilder2.append(numCenters[0]+","+numCenters[1]+","+numCenters[2]+"~"+numCenters[numCenters.length-2]+","+numCenters[numCenters.length-1]+",");
		else
			for(int i=0;i<numCenters.length;i++){
				stringBuilder2.append(numCenters[i]+",");
			}
		
		File filePath= new File(pathDir,"{treeSize=["+stringBuilder.toString()+"],numCenter="+stringBuilder2.toString()+"}.csv");
		BufferedWriter bw=new BufferedWriter(new FileWriter(filePath));
	
		int treeSize=0;
		int numCenter=0;
		long oldTime=System.currentTimeMillis();
		long curTime=oldTime;
		
		for(int ni=0;ni<numCenters.length;ni++){
			numCenter=numCenters[ni];

			for(int ti=0;ti<treeSizes.length;ti++){
				bw.write(numCenter+",");
				treeSize=treeSizes[ti];
				bw.write(treeSize+",");
				HashMap<Integer, ArrayList<Integer>> partitionOccupiedMap=computeCoverForestTreeCentersPartitionOccupied(cfd,treeSize,treeSize,numCenter,cmt,numLabel);
				
				int clusterSize=numLabel;
				HashMap<Integer, Double> centerDistributionRate=new HashMap<Integer,Double>();
				for(int i=1;i<=clusterSize;i++){
					ArrayList<Integer> th=partitionOccupiedMap.get(i);
	/*				System.out.println(i+"-matchCount:"+th.size());
					System.out.print(i+"-matchMap:[");
					for(int j=0;j<th.size();j++){
						System.out.print(th.get(j)+",");
					}
					System.out.println("]");
					System.out.println("\t"+i+"-matchRate:"+((double)th.size())/treeSize);*/
					centerDistributionRate.put(i, ((double)th.size())/treeSize);
					
					bw.write(((double)th.size())/treeSize+",");
				}
				bw.newLine();
			}
			
			curTime=System.currentTimeMillis();
			System.out.println("numCenter-"+numCenter+"-calculateTime:"+(curTime-oldTime));
			oldTime=curTime;
		}
		
		bw.close();
		System.out.println("save TreeCentersPartitionOccupied Map in "+filePath.getAbsolutePath());
			
		return null;
	}
}
