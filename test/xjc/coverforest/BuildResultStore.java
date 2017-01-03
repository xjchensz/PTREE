package xjc.coverforest;

import java.io.File;

public class BuildResultStore {
	public static File dir = new File("SuperStore");
	static final File file = new File(dir, "Sample - Superstore Sales.csv");
	public static final File dataDir = new File(dir, "data");
	public static final File resultDir=new File(dir,"result");
	public static final File resultStoreDir=new File(dir,"resultStore");
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(BuildResultStore.class);

	}

}
