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
public class NumericInstanceDistanceHolder implements IDistanceHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7643652069289372579L;

	protected ArrayList<INumericInstance> m_Instances = new ArrayList<INumericInstance>();

	protected InstanceDistanceMeasure m_Distance;

	private NumericInstanceDistanceHolder() {
	}

	/**
	 * 
	 */
	public NumericInstanceDistanceHolder(InstanceDistanceMeasure distance) {
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
		out.writeUTF(getClass().getName());
		m_Distance.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		try {
			Class<InstanceDistanceMeasure> claszz = (Class<InstanceDistanceMeasure>) Class.forName(in.readUTF());
			m_Distance = claszz.newInstance();
			m_Distance.readFields(in);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IOException(e);
		}
	}

	@Override
	public double[][] distances() throws Exception {
		double[][] distances = new double[size()][size()];
		for (int i = 0, j; i < distances.length; i++) {
			for (j = 0; j < distances[i].length; j++) {
				distances[i][j] = distance(i, j);
			}
		}

		return distances;
	}

	public NumericInstanceDistanceHolder clone() {

		NumericInstanceDistanceHolder nid = new NumericInstanceDistanceHolder();
		nid.m_Instances = (ArrayList<INumericInstance>) m_Instances.clone();
		nid.m_Distance = m_Distance.clone();
		return nid;

	}
}
