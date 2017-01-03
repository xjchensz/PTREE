package xjc.coverforest.syntheicData;

import java.util.ArrayList;
import java.util.HashMap;

import com.sun.org.apache.bcel.internal.generic.IFGE;

import common.utils.collection.OrderedDoubleMap;
import xjc.covertree.NumericInstanceDistanceHolder;

public class PartitionResult{
	public boolean haslabel=false;
	public NumericInstanceDistanceHolder niDistanceHolder;
	public HashMap<Double, Integer> labelMap;
	
	public int[][] bound; 
	public int numCenter;
	public int [][]partition= new int[numCenter][];
	public int[] center=new int[numCenter];
	public HashMap<String,Double> indices=new HashMap<String,Double>();
	public HashMap<Integer,ArrayList<Integer>> centerDistributionMap;
	public HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> partitionDistributionMap;
	public PartitionResult() {
		// TODO Auto-generated constructor stub
	}
	public PartitionResult(int numCenter,int[][] partition,int[] center,int[][] bound){
		this.numCenter=numCenter;
		this.partition=partition;
		this.center=center;
		this.bound=bound;
	}
	
	public PartitionResult(int numCenter, int[][] partition, int[] center, NumericInstanceDistanceHolder nid) {
		// TODO Auto-generated constructor stub
		this.haslabel=true;
		this.labelMap=new HashMap<Double,Integer>();
		this.numCenter=numCenter;
		this.partition=partition;
		this.center=center;
		this.niDistanceHolder=nid;
	}
	public void initCenterDistributionMap(){
		centerDistributionMap=new HashMap<Integer,ArrayList<Integer>>() ;
		if(haslabel){
			for(int i=0;i<niDistanceHolder.size();i++){
				double label=niDistanceHolder.get(i).getLabel();
				if(!labelMap.containsKey(label)){
					labelMap.put(label,labelMap.size());
					centerDistributionMap.put(labelMap.size()-1, new ArrayList<Integer>());
				}
					
			}
			return ; 
		}else{
			for(int i=0;i<bound.length;i++){
				centerDistributionMap.put(i, new ArrayList<Integer>());
			}
		}
		partitionDistributionMap=new HashMap<Integer,HashMap<Integer,ArrayList<Integer>>>();
	}
	
	public void initPartitionDistributionMap(){
		for(int i=0;i<numCenter;i++){
			HashMap<Integer,ArrayList<Integer>> th=new  HashMap<Integer,ArrayList<Integer>>();
			int length=haslabel?labelMap.size():bound.length;
			for(int j=0;j<length;j++){
				th.put(j, new ArrayList<Integer>());
			}
			partitionDistributionMap.put(i, th);
		}
	}
	
	public void initDistributionMap(){
		//initial Center distribution;
		initCenterDistributionMap();
		initPartitionDistributionMap();
	}
	
	public String printPartitionIndices(){
		StringBuilder s=new StringBuilder();
		s.append("numCenter:"+numCenter+"\n");
		s.append("center:\n");
		for(int i=0;i<numCenter;i++){
			s.append(i+"-"+center[i]+"\n");
		}
		
		java.util.Iterator<String> it=indices.keySet().iterator();
		while(it.hasNext()){
			String key=it.next();
			s.append(key+":"+indices.get(key)+"\n");
		}
		return s.toString();
	}
	
	public int partitionIndex(int index){
		
		int partIndex=-1;
		int temp=0;
		if(haslabel){
			double inslabel=niDistanceHolder.get(index).getLabel();
			partIndex=labelMap.get(inslabel);
			return partIndex;
		}

		for(int i=0;i<bound.length;i++){
			temp=bound[i][0];
			if(index>=bound[i][0]&&index<=bound[i][1])
				partIndex=i;
		}
		return partIndex;
	}

	
	public boolean updateCenterDistribution(){
		if(centerDistributionMap==null)return false;
		for(int i=0;i<numCenter;i++){
			int pIndex=partitionIndex(center[i]);
			centerDistributionMap.get(pIndex).add(center[i]);
		}
		return true;
	}
	public boolean updatePartitionDistribution(){
		if(partitionDistributionMap==null) return false;
		for(int i=0;i<numCenter;i++){
			 HashMap<Integer, ArrayList<Integer>> tHashMap=partitionDistributionMap.get(i);
			 for(int j=0;j<partition[i].length;j++){
				 int partIndex=partitionIndex(partition[i][j]);
				 tHashMap.get(partIndex).add(partition[i][j]);
			 }
			 partitionDistributionMap.put(i, tHashMap);
		}
		return true;
	}		
	public void updateDistribution(){
		//get center distribution
		updateCenterDistribution();
		
		//get partition distribution
		updatePartitionDistribution();
	}
	
	public String printCenterDistribution(){
		StringBuilder s=new StringBuilder();
		for(int i=0;i<centerDistributionMap.size();i++){
			s.append("p"+i+":");
			ArrayList<Integer> centerDistribution= centerDistributionMap.get(i);
			for(int j=0;j<centerDistribution.size();j++){
				s.append(centerDistribution.get(j)+",");
			}
			s.append('\n');
		}
		return s.toString();
	}
	public String printPartitionDistribution(){
		StringBuilder s=new StringBuilder();
		for(int i=0;i<numCenter;i++){
			 HashMap<Integer, ArrayList<Integer>> tHashMap=partitionDistributionMap.get(i);
			 s.append("center<"+i+">:"+center[i]+",p"+partitionIndex(center[i])+"\n");
			 for(int j=0;j<tHashMap.size();j++){
				 s.append("p"+j+":");
				 ArrayList<Integer> tIntegers=tHashMap.get(j);
				 for(int p=0;p<tIntegers.size();p++){
					 s.append(tIntegers.get(p)+",");
				 }
				 s.append('\n');
			 }
		}
		return s.toString();
	}
	public String printDistribution(){
		StringBuilder s=new StringBuilder();
		s.append("numCenter:"+numCenter+"\n");
		s.append("\nCenterDistribution\n");
		s.append(printCenterDistribution());//get center distribution
		
		s.append("\nClusterPartitionDistribution\n");
		s.append(printPartitionDistribution());//get partition distribution
	
		return s.toString();			
	}
}