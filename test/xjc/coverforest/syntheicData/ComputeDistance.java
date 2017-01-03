package xjc.coverforest.syntheicData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.numeric.DenseDoubleInstance;
import common.data.meta.MetaData;
import test.dataGenerator.DoubleInstanceGenerator;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.covertree.CoverTreeInstanceDataset;

public class ComputeDistance {
	public static void main(String[] args) throws NumberFormatException, IOException{
		System.out.println(ComputeCentrality.class);
		int dataSize=1000;
		String syntheticDataPath=BuildSuperStoreData.dataDir.getAbsolutePath()+"/SyntheticData/syntheticData_[gaussianDataSize="+dataSize+"]/gaussian/g0-haslabel.csv";
		BufferedReader br=new BufferedReader(new FileReader(new File(syntheticDataPath)));
		String ts="";
		
		int dimenNum=2;
		
		double[][] synData=new double[dataSize][dimenNum]; 
		double[] labels=new double[dataSize];
		int index=0;
		while((ts=br.readLine())!=null){

			String[] dimenValueStr=ts.split(",");
			for(int i=0;i<dimenNum;i++){
				//System.out.print(i+":"+dimenValueStr[i]+" ");
				synData[index][i]=Double.parseDouble(dimenValueStr[i+1]);
				
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
		for(int i=0;i<dataSize;i++){
			DenseDoubleInstance sdi =new DenseDoubleInstance(i, md);
			for(int j=0;j<dimenNum;j++){
				sdi.setValue(j, synData[i][j]);
			}
			sdi.setLabel(labels[i]);
			cd.addInstance(sdi);
		}
		
		System.out.println(cd.distance(730,682));
	}
}
