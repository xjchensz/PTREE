/**
 * 
 */
package xjc.data.PTree.PurTree;

import java.io.UnsupportedEncodingException;
import java.util.List;

import common.IWritable;

/**
 * @author xiaojun chen
 *
 */
public interface IPTreeNode extends IWritable {

	public int getID();

	public IPTreeNode getParent();

	public List<? extends IPTreeNode> getChildren();

	public IPTreeNode getChild(int id);

	public int numChildren();

	public int getLevel();

	public void clear();

	public void destroy();

	public void update();

	public void toXML(StringBuilder sb) throws UnsupportedEncodingException;
}
