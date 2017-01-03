/**
 * @author Xiaojun Chen
 *
 */
package xjc.data.PTree.PurTree;

import java.io.DataInput;
import java.io.IOException;

import common.IWritable;

/**
 * @author xiaojun chen
 *
 */
public interface IPTreeNodeCreator<M extends IPTreeNode> extends IWritable {

	public void reset();

	public M getRootNode();

	public void createPath(int frequency, String... key);

	public M loadNode(DataInput input) throws IOException;

	public void destroy();

}
