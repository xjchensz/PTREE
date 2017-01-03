/**
 * 
 */
package xjc.data.PTree.PurTree.PurTreeDist;

/**
 * @author xiaojun chen
 *
 */
public interface ISimpleLineDataKeyParser {

	public int maxColumns();

	public boolean parse(String[] arrays);

	public String getDataID();

	public String getKey();
}
