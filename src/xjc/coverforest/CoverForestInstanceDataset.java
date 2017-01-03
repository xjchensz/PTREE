/**
 * 
 */
package xjc.coverforest;

import java.util.Random;

import common.data.distance.InstanceDistanceMeasure;
import common.data.instance.numeric.INumericInstance;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeFactory;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.NumericInstanceDistanceHolder;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

/**
 * @author xiaojun chen
 *
 */
public class CoverForestInstanceDataset extends NumericInstanceDistanceHolder {

	private CoverForest cf;
	
	private INumericInstance tmp;
	
	public CoverForestInstanceDataset(InstanceDistanceMeasure distance,double base,int treeSize) {
		super(distance);
		cf = CoverForestFactory.getDefault().create(this,base,treeSize);
	}
	
	public void addInstance(INumericInstance ins) {
		super.addInstance(ins);
		cf.incInstance();
		//ct.insert(size() - 1);
	}
	
	public INumericInstance getInstance(int index) {
		if (index >= 0 && index < size()) {
			return super.get(index);
		}
		return tmp;
	}
	
	public void buildCoverForest(){
		cf.buildCoverForest();
	}
	
	public void buildCoverForest(int[] insertIndex){
		cf.buildCoverForest(insertIndex);
	}
	
	public void buildCoverForest(Random random){
		cf.buildCoverForest(random);
	}
	
	public CoverForest getCoverForest(){return cf;}
	
	public void setCoverForest(CoverForest cf){
		this.cf=cf;
	}
	
	public double getBase(){
		return cf.getBase();
	}
	
	public double getTreeSize(){
		return cf.getTreeSize();
	}
	
	public CoverForestInstanceDataset cfdClone() throws CloneNotSupportedException{
		
		CoverForestInstanceDataset pct = new CoverForestInstanceDataset(this.m_Distance,cf.getBase(),cf.getTreeSize());
		pct.setCoverForest(cf);
		
		pct.getCoverForest().distanceHolder = cf.distanceHolder.clone();
	
		return pct;
	}
	
	public void destroy(){
		this.cf.destroy();
	}
	
}
