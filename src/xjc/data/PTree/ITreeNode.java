/**
 * 
 */
package xjc.data.PTree;

import java.io.UnsupportedEncodingException;
import java.util.List;

import common.IWritable;
import common.utils.collection.ArrayMap;

/**
 * @author xiaojun chen
 *
 */
public interface ITreeNode extends IWritable {

	public int getID();

	public void mark();

	public boolean isMarked();

	public void clearMark();

	public void clearAllChildrenMark();

	public String getKey();

	public String getProperty(String property);

	public ArrayMap<String, String> getproperties();

	public ITreeNode getParent();

	public List<? extends ITreeNode> getChildren();

	public ITreeNode getChild(int index);

	public int indexOfChild(String key);

	public ITreeNode getChild(String key);

	public ITreeNode getChildWithID(int nodeID);

	public int numChildren();

	public int getLevel();

	public void clear();

	public void destroy();

	public void update();

	public void toXML(StringBuilder sb) throws UnsupportedEncodingException;

	public void toXML(StringBuilder sb, IChildFilter filter, int... key) throws UnsupportedEncodingException;
}
