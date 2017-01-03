/**
 * 
 */
package xjc.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import common.IWritable;
import common.utils.collection.OrderedComplexDoubleCalculableMap;
import common.utils.collection.STATUS;

/**
 * @author xiaojun chen
 *
 */
public class HierarchicalTree implements IWritable {
	private ArrayList<String>[] m_IDs;
	private OrderedComplexDoubleCalculableMap[] values;

	public HierarchicalTree(int numHierchies) {
		values = new OrderedComplexDoubleCalculableMap[numHierchies];
		m_IDs = new ArrayList[numHierchies];
		for (int i = 0; i < numHierchies; i++) {
			m_IDs[i] = new ArrayList<String>();
			values[i] = new OrderedComplexDoubleCalculableMap(i + 1,
					STATUS.DISTINCT);
			values[i].setEmptyValue(0);
		}
	}

	public boolean insert(double value, String... id) {
		if (id.length != values.length) {
			return false;
		}
		int[] index = new int[m_IDs.length];
		for (int i = 0; i < m_IDs.length; i++) {
			index[i] = m_IDs[i].indexOf(id[i]);
			if (index[i] < 0) {
				index[i] = m_IDs[i].size();
				m_IDs[i].add(id[i]);
			}
		}
		values[m_IDs.length - 1].add(value, index);
		return true;
	}

	public void update() {

		for (int i = m_IDs.length - 2; i >= 0; i--) {
			values[i] = values[i + 1].aggregate(i + 1, values[i]);
		}
	}

	public double getValue(String... id) {
		int[] index = new int[id.length];
		for (int i = 0; i < id.length; i++) {
			index[i] = m_IDs[i].indexOf(id[i]);
		}

		return getValue(index);
	}

	public double getValue(int... idIndex) {
		return values[idIndex.length - 1].get(idIndex);
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		int numHierchies = input.readInt();
		values = new OrderedComplexDoubleCalculableMap[numHierchies];
		m_IDs = new ArrayList[numHierchies];
		int size;
		for (int i = 0, j; i < numHierchies; i++) {
			size = input.readInt();
			m_IDs[i] = new ArrayList<String>(size);
			for (j = 0; j < size; j++) {
				m_IDs[i].add(input.readUTF());
			}
		}
		for (int i = 0; i < numHierchies; i++) {
			values[i] = OrderedComplexDoubleCalculableMap.read(input);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		int numHierchies = m_IDs.length;
		out.writeInt(numHierchies);
		for (int i = 0, j; i < numHierchies; i++) {
			out.writeInt(m_IDs[i].size());
			for (j = 0; j < m_IDs[i].size(); j++) {
				out.writeUTF(m_IDs[i].get(j));
			}
		}
		for (int i = 0; i < numHierchies; i++) {
			values[i].write(out);
		}
	}
}
