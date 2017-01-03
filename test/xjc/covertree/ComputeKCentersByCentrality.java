package xjc.covertree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import common.utils.io.BufferedReader;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.clustering.validation.DunnIndex;
import xjc.clustering.validation.NormalizedLogW;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.AbstractDataset;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

public class ComputeKCentersByCentrality {
	public static void main(String[] args) throws IOException {
		File dir = new File("SuperStore", "data");
		double gamma = 0.2;
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);
		PurTreeDataCoverTree ctd = new PurTreeDataCoverTree(dis, new Random());
		double [][] distances=dis.distances();
		System.out.println("finish distance matrix");
		CoverTree ct = ctd.getCoverTree();

		//int numCenters = 14;
		int numCenters = 30;
		int k=5;
		int size=dis.size();//data size;
		int[] nodes=new int[size];
		for(int i=0;i<size;i++){
			nodes[i]=i;
		}
		//saveCentrality(BuildSuperStoreData.dataDir, ct.centrality(nodes, k));
		int[] candidateCenters={
				225,523,78,289,45,
				605,728,19,550,134,
				426,656,65,	180,406,
				494,361,336,363,103,
				286,615,37,338,135,
				488,544,718,15,124,
				};
		
		
		double[] logwkAry=new double[candidateCenters.length];double[] dunnIndexAry=new double[candidateCenters.length];
		
		for(int i=2;i<numCenters;i++){
			System.out.println("<k>:"+i);
			
			int[] centers=new int[i];
			System.out.print("centers:");
			for(int j=0;j<i;j++){
				centers[j]=candidateCenters[j];
				System.out.print(""+centers[j]+",");
			}
			System.out.println();
			
			
			int[][] partitions2=AbstractDataset.getPartition(ct.clustering(centers));
			logwkAry[i] = NormalizedLogW.logwk(partitions2, distances);
			dunnIndexAry[i] = DunnIndex.di(partitions2, distances);
			
			System.out.println("logwk:"+logwkAry[i]);
			System.out.println("dunnIndex:"+dunnIndexAry[i]);
		}

		

	}
	
	public static void savePartition(){};
	
	public static void  saveCentrality(File dir,Centrality[] cts) throws IOException{
			//DoubleNode[][] cts=getKCentralityCenterNodePairsInTrees(numCenters);
			BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir, "CentralityInTrees.csv")));
			
			abw.write("id,cdist,knnradius,centrality\n");
			for(int i=0;i<cts.length;i++){
				// format centrality output  
				abw.write(cts[i].getID()+","+cts[i].getCdist()+","+cts[i].getKnnradius()+","+cts[i].getCentrality()+"\n");		
			}		
			abw.close();
			System.out.println("saved each node's Centrality in "+"CentralityInTrees.csv");
			
	}
	
	public static void loadCentrality(File dir,Centrality[] cts) throws IOException{
		BufferedReader br=new BufferedReader(new FileReader(dir));
		String tString=br.readLine();
		//while(br.)
	}

}
