/**
 * 
 */
package xjc.covertree;

import common.data.distance.InstanceDistanceMeasure;
import common.data.instance.numeric.INumericInstance;

/**
 * @author xiaojun chen
 *
 */
public class CoverTreeInstanceDataset extends NumericInstanceDistanceHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -33125012558577264L;
	private CoverTree ct;
	private INumericInstance tmp;

	public CoverTreeInstanceDataset(InstanceDistanceMeasure distance) {
		super(distance);
		ct = CoverTreeFactory.getDefault().create(this);
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

	public CoverTree getCoverTree() {
		return ct;
	}

}
