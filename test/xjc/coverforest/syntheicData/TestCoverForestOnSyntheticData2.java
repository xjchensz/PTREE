package xjc.coverforest.syntheicData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.numeric.DenseDoubleInstance;
import common.data.meta.MetaData;
import test.dataGenerator.DoubleInstanceGenerator;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.coverforest.BuildResultStore;
import xjc.coverforest.CoverForest;
import xjc.coverforest.CoverForestFactory;
import xjc.coverforest.CoverForestInstanceDataset;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;


public class TestCoverForestOnSyntheticData2 {
	static boolean hasLabel=true;
	static  int labelDimenOffset=0;
	public static void main(String[] args) throws Exception{
		int gaussianDataSize=1000;
		int dataSize=gaussianDataSize*3/2;
		//
		String syntheticDataPath=BuildResultStore.dataDir.getAbsolutePath()+"/syntheticData/syntheticData_[gaussianDataSize="+gaussianDataSize+"]/gaussian/g0-haslabel.csv";
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
		CoverTreeInstanceDataset cd = new CoverTreeInstanceDataset(EuclideanDistanceMeasure.getInstance());
		
		double base=2;
		int treeSize=2; int treeSizeMax=30;//define test range
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
	
		CoverForest cf=CoverForestFactory.getDefault().create(cd, base, treeSizeMax);
		cf.buildCoverForest();
		
		cfd.buildCoverForest();
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
		
 		
		File  savePathDir=new File(BuildResultStore.resultDir,"TreeCenter-PartitionOccupedResult/coverforest/partitionOccupiedRate");
		
		int[] numCenters=new int[(int)(Math.ceil((double)(numCenterMax-numCenter))/numCentetOffset)];
		for(int i=0;numCenter<numCenterMax;numCenter+=numCentetOffset,i++){
			numCenters[i]=numCenter;
		}
		
		
		ComputeDistribution.computeCoverForestTreesPartitionOccupiedCountMap(savePathDir,cfd, treeSizes, numCenters, cmt, 3);


		//ComputeDistribution.computeCoverForestTreesPartitionOccupiedCountMap(savePathDir,cf, treeSizes, numCenters, cmt, bound);

	}
	

	public static void saveDistance(CoverTreeInstanceDataset cd,String resultPath) throws Exception{
		double[][] dis=cd.distances();
		int dataSize=cd.size();
		File syntheticDataResult=new File(BuildSuperStoreData.dataDir,"SyntheticData-TestResult");
		if(!syntheticDataResult.exists()) syntheticDataResult.mkdirs();
		
		BufferedWriter bw =new BufferedWriter(new FileWriter(new File(syntheticDataResult,"distance.csv")));
		
		for(int i=0;i<dataSize;i++){
			for(int j=0;j<dataSize;j++){
				bw.write(dis[i][j]+",");
			}
			bw.write('\n');
		}
		System.out.println("saved synthetic distance");
	}	
	
	
	public static void saveDistance(double[][] dis,String resultPath) throws Exception{
		int dataSize=dis.length;
		File syntheticDataResult=new File(BuildSuperStoreData.dataDir,"SyntheticData-TestResult");
		if(!syntheticDataResult.exists()) syntheticDataResult.mkdirs();
		
		BufferedWriter bw =new BufferedWriter(new FileWriter(new File(syntheticDataResult,"distance.csv")));
		
		for(int i=0;i<dataSize;i++){
			for(int j=0;j<dataSize;j++){
				bw.write(dis[i][j]+",");
			}
			bw.write('\n');
		}
		System.out.println("saved synthetic distance");
	}
	
	
}
