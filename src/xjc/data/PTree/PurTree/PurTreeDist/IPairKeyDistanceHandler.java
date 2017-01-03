/**
 * 
 */
package xjc.data.PTree.PurTree.PurTreeDist;

import common.utils.collection.IPairKeyHandler;

/**
 * @author xiaojun chen
 *
 */
public interface IPairKeyDistanceHandler extends IPairKeyHandler {

	public void startLevel(int level);

	public boolean accept(int i1, int i2, int[] common);

	public void end();

	public void endLevel();
}
