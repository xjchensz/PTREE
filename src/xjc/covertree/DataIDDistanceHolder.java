/**
 * 
 */
package xjc.covertree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import common.data.distance.InstanceDistanceMeasure;
import common.data.instance.numeric.INumericInstance;

/**
 * @author xiaojun chen
 *
 */
public class DataIDDistanceHolder implements IDistanceHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8956884156113595574L;

	protected ArrayList<INumericInstance> m_Instances = new ArrayList<INumericInstance>();

	private InstanceDistanceMeasure m_Distance;

	private DataIDDistanceHolder() {
	}

	/**
	 * 
	 */
	public DataIDDistanceHolder(InstanceDistanceMeasure distance) {
		m_Distance = distance;
	}

	public void addInstance(INumericInstance ins) {
		m_Instances.add(ins);
	}

	public INumericInstance get(int index) {
		return m_Instances.get(index);
	}

	public int size() {
		return m_Instances.size();
	}

	@Override
	public double distance(int ins1, int ins2) {
		return m_Distance.distance(get(ins1), get(ins2));
	}

	@Override
	public double[] distances(int ins1) {
		double[] dis = new double[size()];
		INumericInstance is1 = get(ins1);
		for (int i = 0; i < dis.length; i++) {
			dis[i] = m_Distance.distance(is1, m_Instances.get(i));
		}
		return dis;
	}

	public void clear() {
		m_Instances.clear();
		m_Distance = null;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		m_Distance.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {

	}

	@Override
	public double[][] distances() throws Exception {
		return null;
	}

	public DataIDDistanceHolder clone() {
		DataIDDistanceHolder did = new DataIDDistanceHolder();
		did.m_Instances = (ArrayList<INumericInstance>) m_Instances.clone();
		did.m_Distance = m_Distance.clone();
		return did;
	}

}
