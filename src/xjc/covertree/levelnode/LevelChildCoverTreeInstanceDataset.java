/**
 * 
 */
package xjc.covertree.levelnode;

import common.data.distance.InstanceDistanceMeasure;
import common.data.instance.numeric.INumericInstance;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeFactory;
import xjc.covertree.NumericInstanceDistanceHolder;

/**
 * @author xiaojun chen
 *
 */
public class LevelChildCoverTreeInstanceDataset extends NumericInstanceDistanceHolder {

	private LevelChildCoverTree ct;
	private INumericInstance tmp;

	public LevelChildCoverTreeInstanceDataset(InstanceDistanceMeasure distance) {
		super(distance);
		ct = LevelChildCoverTreeFactory.getDefault().create(this);
	}

	public void addInstance(INumericInstance ins) {
		super.addInstance(ins);
		ct.insert(size() - 1);
	}

	public INumericInstance get(int index) {
		if (index >= 0 && index < size()) {
			return super.get(index);
		}
		return tmp;
	}

	public INumericInstance getNearest(INumericInstance ins) {
		tmp = ins;
		int index = ct.getNearest(-1);
		if (index >= 0 && index < size()) {
			return super.get(index);
		}
		return null;
	}
	
	public LevelChildCoverTree getCoverTree(){
		return ct;
	}

}
