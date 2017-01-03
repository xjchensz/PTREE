/**
 * 
 */
package xjc.coverforest;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Admin
 *
 */
public class DynamicIndex {

	private static class Index implements Comparable<Index> {
		int id;
		int level = -Integer.MAX_VALUE;

		public Index(int id) {
			this.id = id;
		}

		@Override
		public int compareTo(Index index) {
			return level - index.level;
		}

	}

	private Index[] m_Index;
	private HashMap<Integer, Index> map;

	public DynamicIndex(int size) {
		m_Index = new Index[size];
		map = new HashMap<Integer, Index>();
		for (int i = 0; i < size; i++) {
			m_Index[i] = new Index(i);
			map.put(i, m_Index[i]);
		}
	}
	
	public DynamicIndex(int[] insertIndex){
		int size=insertIndex.length;
		m_Index = new Index[size];
		map = new HashMap<Integer, Index>();
		for (int i = 0; i < size; i++) {
			m_Index[i] = new Index(insertIndex[i]);
			map.put(insertIndex[i], m_Index[i]);
		}
	}

	public void setLevel(int id, int level) {
		Index ind = map.get(id);
		if (level > ind.level) {
			ind.level = level;
		}

	}

	public void sort() {
		Arrays.sort(m_Index);
	}

	public int getID(int index) {
		return m_Index[index].id;
	}

	public void clear() {
		m_Index = null;
	}
}
