/**
 * 
 */
package xjc.data.PTree;

import common.utils.MathUtils;
import common.utils.collection.ArrayMap;

/**
 * @author xiaojun chen
 *
 */
public abstract class SimpleLineKeyParser implements ILineKeyParser {

	private int[] m_KeyIndices;

	protected String[] keys;
	private ArrayMap<String, String>[] map;

	private int maxIndex;
	private boolean hasValue = false;

	public SimpleLineKeyParser(int[] keyIndices, int maxColumn) {
		m_KeyIndices = keyIndices;
		map = new ArrayMap[m_KeyIndices.length];
		for (int i = 0; i < map.length; i++) {
			map[i] = new ArrayMap<String, String>();
		}

		maxIndex = MathUtils.max(keyIndices);
		keys = new String[keyIndices.length];

		if (maxIndex < maxColumn - 1) {
			maxIndex = maxColumn - 1;
		}
	}

	@Override
	public boolean parse(String[] arrays) throws NumberFormatException {
		hasValue = false;
		if (arrays == null || arrays.length <= maxIndex) {
			return hasValue;
		}
		for (int i = 0; i < m_KeyIndices.length; i++) {
			keys[i] = arrays[m_KeyIndices[i]];
		}
		for (int i = 0; i < map.length; i++) {
			map[i].clear();
		}
		parseProperty(arrays, map);
		return (hasValue = hasValue());
	}

	protected boolean hasValue() {
		return keys != null;
	}

	protected abstract void parseProperty(String[] values,
			ArrayMap<String, String>[] map);

	@Override
	public String[] keys() {
		return hasValue == true ? keys : null;
	}

	@Override
	public int numKeys() {
		return m_KeyIndices == null ? 0 : m_KeyIndices.length;
	}

	@Override
	public int maxColumns() {
		return maxIndex + 1;
	}

	@Override
	public ArrayMap<String, String>[] getProperties() {
		return map;
	}
}
