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

import javax.swing.text.html.CSS;

//import org.junit.experimental.theories.FromDataPoints;

import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.numeric.DenseDoubleInstance;
import common.data.meta.MetaData;
import common.utils.collection.OrderedDoubleMap;
import common.utils.collection.OrderedIntArraySet;
import common.utils.collection.OrderedIntMap;
import test.dataGenerator.DoubleInstanceGenerator;
import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.coverforest.CoverForest;
import xjc.coverforest.CoverForestFactory;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.IDistanceHolder;
import xjc.covertree.INode;;

public class ComputeDensity {
	public static void main(String[] args) throws NumberFormatException, IOException{
		System.out.println(ComputeCentrality.class);
		int dataSize=1000;
		String syntheticDataPath=BuildSuperStoreData.dataDir.getAbsolutePath()+"/SyntheticData/syntheticData_[gaussianDataSize="+dataSize+"]/gaussian/g0-haslabel.csv";
		BufferedReader br=new BufferedReader(new FileReader(new File(syntheticDataPath)));
		String ts="";
		
		int dimenNum=2;
		
		double[][] synData=new double[1600][dimenNum]; 
		double[] labels=new double[1600];
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
		
		CoverTree cTree=cd.getCoverTree();
		
		File file1,file2;
		BufferedWriter bWriter1 ,bWriter2=null;
		StringBuilder s1=new StringBuilder();
		
		StringBuilder s4=new StringBuilder();
		String filePath4=BuildSuperStoreData.dir+"/result/"+"CoverTreeDensityByInterFunction_child_ map.txt";
		File file4=new File(BuildSuperStoreData.dir+"/result/"+"CoverTreeDensityByInterFunction_child_ map.txt");
		BufferedWriter bWriter4=new BufferedWriter(new FileWriter(file4));
		
		OrderedIntMap map=new OrderedIntMap();
		map.clear();
		for(int level=cTree.maxLevel();level>cTree.minLevel();level--){
			map.clear();
			cTree.levelDensity2(s4,level, map);
			s1.append("level-"+level+"\n");
			s1.append("levelSize-"+cTree.size(level)+"\n");
			int[] key=map.keys();
			for(int j=0;j<map.size();j++){
				s1.append("("+key[j]+","+map.get(key[j])+","+cd.get(key[j]).getLabel()+")");
			}
			s1.append('\n');
		}		
		file1=new File(BuildSuperStoreData.dir+"/result/"+"CoverTreeDensityByInterFunction.txt");	
		bWriter1=new BufferedWriter(new FileWriter(file1));
		bWriter1.write(s1.toString());
		bWriter1.close();
		bWriter4.write(s4.toString());
		bWriter4.close();
		System.out.println("#######################################");
		
		
		StringBuilder s3=new StringBuilder();
		String filePath=BuildSuperStoreData.dir+"/result/"+"CoverTreeDensityByOuterFunction_child_ map.txt";
		File file3=new File(BuildSuperStoreData.dir+"/result/"+"CoverTreeDensityByOuterFunction_child_ map.txt");
		BufferedWriter bWriter3=new BufferedWriter(new FileWriter(file3));
		
		
		map.clear();
		StringBuilder s2=new StringBuilder();
		for(int level=cTree.maxLevel();level>cTree.minLevel();level--){
			map.clear();
			s2.append("level-"+level+"\n");
			
			computeLevelDensity(s3,level, map, cd);
			
			int[] key=map.keys();
			for(int j=0;j<map.size();j++){
				s2.append("("+key[j]+","+map.get(key[j])+","+cd.get(key[j]).getLabel()+")");
			}
			s2.append("\n");
		}
		file2=new File(BuildSuperStoreData.dir+"/result/"+"CoverTreeDensityByOuterFunction.txt");
		bWriter2=new BufferedWriter(new FileWriter(file2));
		bWriter2.write(s2.toString());
		bWriter2.close();
		System.out.println("#######################################");
		
		bWriter3.write(s3.toString());
		bWriter3.close();
		
/*		int treeSizeMax=2;
		int treeSize=2; ;//define test range		
		double base=2;
		CoverForest cf=CoverForestFactory.getDefault().create(cd, base, treeSizeMax);
		cf.buildCoverForest();
	    try {
	    	System.out.println("finish building "+treeSizeMax+" trees");
	    	System.out.println("sleep 2000");
            Thread.sleep(2000);
            System.out.println("continue");
        } catch (InterruptedException e) {
            e.printStackTrace(); 
        }
		//computeCoverForestLevelDensity(cd,cf,treeSizeMax);

		int gaussianDataSize=cd.size();
	    File levelDensityFile=new File(BuildSuperStoreData.dir+"/result/SyntheticData/[gaussianDataSize="+gaussianDataSize+",treeSize="+treeSizeMax+"]/CoverForestLevelDensity");
	    if(!levelDensityFile.exists()) levelDensityFile.mkdirs();
	    String ctInsertSeqPath=levelDensityFile.getPath();
	    saveCoveTreeInsertSequence(ctInsertSeqPath, cf);
	    
		String coverForestChildMapsDir=BuildSuperStoreData.dir+"/result/SyntheticData/syntheticData_[gaussianDataSize="+dataSize+",treeSize="+treeSizeMax+"]/";
		saveCoverForestChildMap(coverForestChildMapsDir, cf, treeSizeMax, synData);
		saveCoveTreeInsertSequence(coverForestChildMapsDir, cf);
		
		computeCoverForestLevelDensity2(cd, cf, treeSizeMax);//compute levelDenstiy of cover tree in forest 
*/
	}
	
	public static void saveCoverForestChildMap(String resultDir,CoverForest cf,int treeSizeMax,double[][] synData) throws IOException{
		CoverTree cTree;
		for(int i=0;i<cf.getTreeSize();i++){
			cTree=cf.getCoverTree(i);
			int maxLevel=cTree.maxLevel();
			int minLevel=cTree.minLevel();
			ArrayList<HashMap<Integer, OrderedIntArraySet>> childLevelMap=getCoverTreeChildMap(BuildSuperStoreData.dir+"/result/", cTree, synData);
		    int targetLevel=minLevel+1;
		    
		    int pInsCount=0;  int cInsCount=0;
		    for(targetLevel=maxLevel-1;targetLevel>=minLevel;targetLevel--){
		    	System.out.println("level-"+targetLevel);
			    int levelMapIndex=maxLevel-1-targetLevel;
				Set<Integer> pInstances=childLevelMap.get(levelMapIndex).keySet();//get level parent instance
				System.out.print("p:");
				Iterator<Integer> itrInteger=pInstances.iterator();
				while(itrInteger.hasNext()){
					System.out.print(itrInteger.next().intValue()+",");
				}
				System.out.println();
				
				//Set<Integer> cInstances=childLevelMap.get(levelMapIndex+1).keySet();//get level child instance
				Collection<OrderedIntArraySet> tmOIArySetCollection=childLevelMap.get(levelMapIndex).values();
				Iterator<OrderedIntArraySet> itrOIAry=tmOIArySetCollection.iterator();
				System.out.print("c:");
				while(itrOIAry.hasNext()){
					OrderedIntArraySet tmIntArraySet=itrOIAry.next();
					for(int index2=0;index2<tmIntArraySet.size();index2++){
						System.out.print(tmIntArraySet.getValueAt(index2)+",");
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
				Set<Integer> pInstances=childLevelMap.get(levelMapIndex).keySet();//get level parent instance
				HashMap<Integer, OrderedIntArraySet> tHashMap=childLevelMap.get(levelMapIndex);
				Iterator<Integer> itrInteger=pInstances.iterator();
				while(itrInteger.hasNext()){
					int pIns=itrInteger.next().intValue();
					System.out.print(pIns+",");
					OrderedIntArraySet tIntArraySet=tHashMap.get(pIns);
					for(int indice=0;indice<tIntArraySet.size();indice++){
						childListStr.append(pIns+","+(targetLevel+1)+","+tIntArraySet.getValueAt(indice)+","+targetLevel+",\n");
					}
				}
				//System.out.println();
				
		    }
		    System.out.println(childListStr.toString());
		    String childListFilePath=resultDir+"/tree-"+i;//BuildSuperStoreData.dir+"/result";
		    File childListFile=new File(childListFilePath);
		    if(!childListFile.exists()) childListFile.mkdirs();
		    childListFile=new File(childListFilePath+"/childList.csv");
		    BufferedWriter bWriter=new BufferedWriter(new FileWriter(childListFile));
		    bWriter.write(childListStr.toString());
		    bWriter.flush();
		    bWriter.close();
		    System.out.println("save childMapList in "+childListFile.getAbsolutePath());
		}
		
	}
	
	public static void computeCoverForestLevelDensity(CoverTreeInstanceDataset cd,CoverForest cf,int treeSizeMax) throws IOException{
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
	    
	    File levelDensityFile=new File(BuildSuperStoreData.dir+"/result/CoverForestLevelDensity/"+"[gaussianDataSize="+gaussianDataSize+",treeSize="+treeSizeMax+"]");
	    if(!levelDensityFile.exists()) levelDensityFile.mkdirs();
	    
	    BufferedWriter bw;//=new BufferedWriter(new FileWriter(levelDensityFile));
	    
	    StringBuilder tStringBuilder=new StringBuilder();
	    for(int level=max_maxLevelInForest;level>min_minlevelInForest;level--){
	    	System.out.println("cover forest level_"+level);
		    for(int i=0;i<cts.length;i++){
		    	System.out.println("cover tree_"+i);
		    	levelDensityMsgStr.append("tree-"+i+"\t");
				levelDensityMsgStr.append("level-"+level+"\n");
				//levelDensityMsgStr.append("levelSize-"+cts[i].size(level)+"\n");
				levelDensityMsgStr.append("\t\t");
				map.clear();
				if(level<=cts[i].maxLevel()&&level>cts[i].minLevel())
					cts[i].levelDensity2(tStringBuilder, level, map);
				
				int[] key=map.keys();
				for(int j=0;j<map.size();j++){
					levelDensityMsgStr.append("("+key[j]+","+map.get(key[j])+","+cd.get(key[j]).getLabel()+")");
				}
				levelDensityMsgStr.append('\n');
		    }
		    bw=new BufferedWriter(new FileWriter(new File(levelDensityFile.getPath()+"/CoverForestLevelDensityByInterFunction.txt."+level)));
		    bw.write(levelDensityMsgStr.toString());
		    bw.close();
		    levelDensityMsgStr.delete(0, levelDensityMsgStr.length());
		    //levelDensityMsgStr=new StringBuilder();
		    System.out.println("save cover forest-<"+level+">level-density result in "+levelDensityFile.getAbsolutePath());
	    }
	}
	
	public static ArrayList<HashMap<Integer, OrderedIntArraySet>> getCoverTreeChildMap(String path,CoverTree cTree,double[][] pos) throws IOException{
		//BufferedWriter bWriter=new BufferedWriter(new FileWriter(new File(path+"/coverTreeChildMap.txt")));
		
		ArrayList<HashMap<Integer, OrderedIntArraySet>> childMap=new ArrayList<HashMap<Integer,OrderedIntArraySet>>();
		//CoverTree cTree=ctd.getCoverTree();
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
	
	
	public static void saveCoveTreeInsertSequence(String path,CoverForest cf) throws IOException{
		BufferedWriter bWriter=new BufferedWriter(new FileWriter(new File(path+"/coverTreeInsertSequence.txt")));
		StringBuilder stringBuilder=new StringBuilder();
		for(int i=0;i<cf.getTreeSize();i++){
			stringBuilder.append("tree_"+i+",");
			int[] coverTreeIndexs=cf.getCoverTreeInsertIndex(i);
			for(int j=0;j<coverTreeIndexs.length;j++){
				stringBuilder.append(coverTreeIndexs[j]+",");
			}
			stringBuilder.append("\n");
			
			bWriter.append(stringBuilder.toString());
			stringBuilder.delete(0, stringBuilder.length());
		}
		bWriter.flush();
		bWriter.close();
	}
	
	public static void saveCoveTree(String path,CoverForest cf) throws IOException{
		BufferedWriter bWriter=new BufferedWriter(new FileWriter(new File(path+"coverTreeInsertSequence.txt")));
		StringBuilder stringBuilder=new StringBuilder();
		for(int i=0;i<cf.getTreeSize();i++){
			stringBuilder.append("tree_"+i);
			int[] coverTreeIndexs=cf.getCoverTreeInsertIndex(i);
			for(int j=0;j<coverTreeIndexs.length;j++){
				stringBuilder.append(coverTreeIndexs[j]+",");
			}
			stringBuilder.append("\n");
			
			bWriter.append(stringBuilder.toString());
			stringBuilder.delete(0, stringBuilder.length());
		}
		bWriter.flush();
		bWriter.close();
	}
	
	public static void computeLevelDensityInTree(CoverTree ct,int level,StringBuilder s ,OrderedIntMap map){
		ct.levelDensity2(s, level, map);

	}
	public static double[] estimateDensity(File dir, IDistanceHolder dis, double gamma, int level,
			OrderedIntMap map) throws IOException{
		return null;
	}
	
	public static void computeLevelDensity(StringBuilder s,int level, OrderedIntMap map,CoverTreeInstanceDataset ctd) throws IOException{
		
		CoverTree cTree=ctd.getCoverTree();
		double base=cTree.getBase();
		int tsize=cTree.size();
		INode rootNode=cTree.getRootNode();
		int maxLevel=cTree.maxLevel();
		int minLevel=cTree.minLevel();
		//int size=cTree.size();
		CoverTreeInstanceDataset m_DistanceHolder=ctd;//cTree.getDistanceHolder();
		
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
		
		s.append("level-"+level+":");s.append("\n");

		for (int l = maxLevel - 1, j, k; l >= level; l--) {
			tmpNodes2.clear();
			for (j = tmpNodes1.size() - 1; j >= 0; j--) {
				children = tmpNodes1.get(j).getChildren();
				if (children != null) {
					for (k = children.size() - 1; k >= 0; k--) {
						child = children.get(k);
						s.append(l+"-ins>"+tmpNodes1.get(j).getInstance()+" childs "+ child.getInstance());s.append("\n");
						s.append(l+"-id>"+tmpNodes1.get(j).getID()+" childs "+ child.getID());s.append("\n");
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

		
		for (int i = 0; i < tmpNodes3.size(); i++) {
			os.add(tmpNodes3.get(i).getInstance());
			s.append(tmpNodes3.get(i).getInstance()+",");s.append("\n");
		}
		//System.out.println();

		int instance, tmpIns; 
		double distance;
		int count;
		if (level - minLevel < 2 || tmpNodes3.size() >= 0.5 * m_DistanceHolder.size()) {
			//
			tmpNodes3.clear();
			int size = m_DistanceHolder.size();
			double distanceThreshHold = Math.pow(base, level + 1);
			for (int i = 0, j; i < os.size(); i++) {
				instance = os.getValueAt(i);
				count = 0;
				for (j = 0; j < size; j++) {
					distance = m_DistanceHolder.distance(instance, j);
					//System.out.println("distance<"+instance+","+j+">="+distance);
					if (distance <= distanceThreshHold) {
						count++;
					}
				}
				map.put(instance, count);
			}
			return;
		}

		tmpNodes4.addAll(tmpNodes1);

		double distanceThreshHold = Math.pow(base, level), tmpDistanceThreshHold;
		System.out.println("level-"+level);
		for (int i = 0, l, j, k; i < os.size(); i++) {
			instance = os.getValueAt(i);
			count = 0;
			tmpMap.clear();

			neighbors.clear();
			for (j = tmpNodes3.size() - 1; j >= 0; j--) {
				tmpIns = tmpNodes3.get(j).getInstance();
				distance = m_DistanceHolder.distance(instance, tmpIns);
				tmpMap.put(tmpIns, distance);
				if (distance <= distanceThreshHold) {
					neighbors.add(tmpNodes3.get(j).getInstance());
				}
			}

			tmpNodes1.clear();
			tmpNodes1.addAll(tmpNodes4);
			for (l = level - 1; l >= minLevel && tmpNodes1.size() > 0; l--) {
				tmpDistanceThreshHold = Math.pow(base, l + 1);
				tmpNodes2.clear();
				for (j = tmpNodes1.size() - 1; j >= 0; j--) {
					children = tmpNodes1.get(j).getChildren();
					if (children != null) {
						for (k = children.size() - 1; k >= 0; k--) {
							child = children.get(k);
							tmpIns = child.getInstance();
							if ((distance = tmpMap.get(tmpIns)) < 0) {
								distance = m_DistanceHolder.distance(instance, tmpIns);
							}

							if (distance <= distanceThreshHold - tmpDistanceThreshHold) {
								neighbors.removeValue(tmpIns);
								if (instance == tmpIns) {
									count += child.numDistinctChildren();
								} else {
									count += child.numDistinctChildren() + 1;
								}
							} else if (distance <= distanceThreshHold + tmpDistanceThreshHold) {
								if (distance <= distanceThreshHold) {
									neighbors.add(tmpIns);
								}
								tmpNodes2.add(child);
							}
						}
					}
				}

				tmp = tmpNodes1;
				tmpNodes1 = tmpNodes2;
				tmpNodes2 = tmp;
			}

			neighbors.removeValue(instance);
			map.put(instance, count + neighbors.size());
			System.out.println("child numDistinctChildren count:"+count);
			System.out.println("neighbor size:"+neighbors.size());
			System.out.println("instance:"+instance);
		}
		tmpNodes1.clear();
		tmpNodes2.clear();
		tmpNodes3.clear();
		tmpNodes4.clear();
		tmpMap.clear();
		os.clear();
		
		

	}

}
