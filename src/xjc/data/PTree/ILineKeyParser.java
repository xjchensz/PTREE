/**
 * 
 */
package xjc.data.PTree;

import common.utils.collection.ArrayMap;

/**
 * @author xiaojun chen
 *
 */
public interface ILineKeyParser {

	public int numKeys();

	public int maxColumns();

	public boolean parse(String[] arrays);

	public String[] keys();

	public ArrayMap<String, String>[] getProperties();

}
