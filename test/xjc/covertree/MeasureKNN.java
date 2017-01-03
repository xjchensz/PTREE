package xjc.covertree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

//test for full Nearest neighbor (beside itself) for every object in DataSet 
public class MeasureKNN {
	public static void main(String[] args) throws IOException {
		// testFullNNSearchTime();

		File dir = new File("SuperStore", "data");
		double gamma = 0.2;
		double base = 2;
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);
		PurTreeDataCoverTree ctd = new PurTreeDataCoverTree(dis, base, null, new Random());

		CoverTree ct = ctd.getCoverTree();

		int instance = 1;
		int k = 10;
		int knns[] = test2KNN(ct, instance, k);
		for (int i = 0; i < k; i++) {
			System.out.println(knns[i] + ":" + dis.distance(instance, knns[i]));
		}

		System.out.println("\n" + ctd.getCoverTree().getKNNRadius(instance, k));
		return;
	}

	// test2 for cover tree , insert data twice
	public static int[] test2KNN(CoverTree ct, int instance, int k) throws IOException {
		// int[] instances={0,1,2,3,4,5};
		int[] instances = new int[ct.size()];
		for (int i = 0; i < ct.size(); i++)
			instances[i] = i;

		for (int i = 0; i < instances.length; i++)
			ct.insert(instances[i]);

		return ct.getKNearestNeighbor(instance, k);
	}

	// test1 for cover tree , insert data once
	public static int[] testKNN(CoverTree ct, int instance, int k) throws IOException {
		return ct.getKNearestNeighbor(instance, k);
	}

	public static void testFullNNSearchTime() throws IOException {
		File dir = new File("SuperStore", "data");
		// File dir=new File("SuperStore");
		BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir, "fullNNSearchTime.csv")));
		double gamma = 0.2;
		double base = 2;
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);
		PurTreeDataCoverTree ctd = new PurTreeDataCoverTree(dis, base, null, new Random());

		// long nnSearchTime[]=getFullNNSearchTime(dir,gamma);
		int size = ctd.size();
		long totalTime = 0;

		// return all NNsearchTime in total CoverTree and avgTime in the end
		abw.append("degree,nnInt,searchTime\n");
		for (int i = 0; i < size; i++) {
			// abw.append(i+","+getNNSearchIns(ctd,i)+","+nnSearchTime[i]+"\n");
			int nnIns = getNNSearchIns(ctd, i);
			long nnSearchTime = getNNSearchTime(ctd, i);
			double minDis = ctd.distance(i, nnIns);
			abw.append(i + "," + nnIns + "," + nnSearchTime + "," + minDis + "\n");
			totalTime += nnSearchTime;
		}
		abw.append("totalTime,," + totalTime + "\n");
		abw.append("avgTime,," + totalTime / (double) size + "\n");

		abw.close();
	}

	// get all NN-search time(beside itself) for every object in DataSet
	public static long[] getFullNNSearcTime(PurTreeDataCoverTree ctd) throws IOException {

		int size = ctd.size();
		long[] knnTimes = new long[size];
		int[] nns = new int[size];

		long start = System.currentTimeMillis();

		System.out.println("test full KNN start time:");
		for (int i = 0; i < size; i++) {
			// System.out.println("Search nn_"+i);
			start = System.currentTimeMillis();
			int nn = (ctd.getCoverTree().getKNearestNeighbor(i, 2))[1];
			knnTimes[i] = System.currentTimeMillis() - start;
			// System.out.println(i+" nn: "+nn);
		}

		System.out.println("test full KNN end time:");
		return knnTimes;
	}

	public static int getNNSearchIns(PurTreeDataCoverTree ctd, int instance) throws IOException {
		int nn = (ctd.getCoverTree().getKNearestNeighbor(instance, 2))[1];
		return nn;
	}

	/*
	 * public static int getNNSearchIns(File dir,double gamma,double base,int
	 * instance) throws IOException{ PurTreeDataSet sd = new
	 * PurTreeDataSet(dir); PurTreeDistance dis = new PurTreeDistance(new
	 * LevelWeightedDistance(gamma)); dis.setData(sd);
	 * CoverTreeSetSemanticDataset ctd= new
	 * CoverTreeSetSemanticDataset(dis,base,null); int size=dis.size(); int
	 * nn=(ctd.getCoverTree().getKNearest(instance, 2))[1]; return nn; }
	 */

	public static long getNNSearchTime(PurTreeDataCoverTree ctd, int instance) {
		long start = System.currentTimeMillis();
		int nn = (ctd.getCoverTree().getKNearestNeighbor(instance, 2))[1];
		return System.currentTimeMillis() - start;
	}

}
