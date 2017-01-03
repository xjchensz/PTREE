/**
 * 
 */
package xjc.coverforest.levelnode;

import common.data.distance.InstanceDistanceMeasure;
import common.data.instance.numeric.INumericInstance;
import xjc.covertree.CoverTreeFactory;
import xjc.covertree.NumericInstanceDistanceHolder;

/**
 * @author xiaojun chen
 *
 */
public class LevelChildCoverForestInstanceDataset extends NumericInstanceDistanceHolder {

	private LevelChildCoverForest cf;
	
	private INumericInstance tmp;

	public LevelChildCoverForestInstanceDataset(InstanceDistanceMeasure distance,double base,int treeSize) {
		super(distance);
		cf = LevelChildCoverForestFactory.getDefault().create(this,base,treeSize);
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
	
	public LevelChildCoverForest getCoverForest(){return cf;}
	
	public double getBase(){
		return cf.getBase();
	}
	
	public double getTreeSize(){
		return cf.getTreeSize();
	}
	
}
