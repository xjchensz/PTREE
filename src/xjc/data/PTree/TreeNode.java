/**
 * 
 */
package xjc.data.PTree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.utils.ArrayUtils;
import common.utils.collection.ArrayMap;

/**
 * @author xjchen
 *
 */
public class TreeNode implements ITreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1083222005453668386L;
	private String m_NodeName;
	private int m_ID;
	private int m_Level = 1;
	private TreeNode m_Parent;
	private String m_Key;
	private ArrayMap<String, String> m_Properties;

	private ArrayList<TreeNode> m_Children;

	private HashMap<String, Integer> map = new HashMap<String, Integer>();

	protected TreeNode() {
	}

	public TreeNode(TreeNode parent, TreeNode node) {
		m_NodeName = node.m_NodeName;
		m_ID = node.m_ID;
		m_Level = node.m_Level;
		m_Parent = parent;
		m_Key = node.m_Key;
		m_Properties = node.m_Properties;
	}

	public TreeNode(String nodeName, TreeNode parent, int id, String key, ArrayMap<String, String> roperties) {
		m_NodeName = nodeName;
		m_Key = key;
		m_Parent = parent;
		if (m_Parent != null) {
			m_Level = m_Parent.getLevel() + 1;
		}
		m_ID = id;
		m_Properties = roperties;
	}

	@Override
	public int getID() {
		return m_ID;
	}

	@Override
	public String getKey() {
		return m_Key;
	}

	@Override
	public String getProperty(String property) {
		return m_Properties == null ? null : m_Properties.get(property);
	}

	@Override
	public ArrayMap<String, String> getproperties() {
		return m_Properties;
	}

	void addChild(TreeNode child) {
		if (m_Children == null) {
			m_Children = new ArrayList<TreeNode>();
		}
		map.put(child.getKey(), m_Children.size());
		m_Children.add(child);
	}

	void removeChild(TreeNode child) {
		if (m_Children != null) {
			int index = map.get(child.getKey());
			if (m_Children.get(index) == child) {
				map.remove(child.getKey());
				m_Children.remove(index);
			}
			// update map
			for (int i = index; i < m_Children.size(); i++) {
				map.put(m_Children.get(i).getKey(), i);
			}
		}
	}

	@Override
	public ITreeNode getParent() {
		return m_Parent;
	}

	@Override
	public List<? extends ITreeNode> getChildren() {
		return m_Children;
	}

	@Override
	public ITreeNode getChild(int index) {
		if (m_Children == null || m_Children.size() <= index) {
			return null;
		}
		return m_Children.get(index);
	}

	@Override
	public int indexOfChild(String key) {
		Integer index = map.get(key);
		if (index != null) {
			return index;
		} else {
			return -1;
		}
	}

	@Override
	public TreeNode getChild(String key) {
		int index = indexOfChild(key);
		if (index < 0) {
			return null;
		} else {
			if (m_Children == null || m_Children.size() <= index) {
				return null;
			} else {
				return m_Children.get(index);
			}
		}
	}

	@Override
	public ITreeNode getChildWithID(int nodeID) {
		if (m_Children == null || m_Children.size() == 0) {
			return null;
		} else {
			for (TreeNode node : m_Children) {
				if (node.getID() == nodeID) {
					return node;
				}
			}
		}
		return null;
	}

	@Override
	public int numChildren() {
		return m_Children == null ? 0 : m_Children.size();
	}

	public int getLevel() {
		return m_Level;
	}

	@Override
	public void clear() {
		if (m_Children != null) {
			m_Children = null;
		}
	}

	@Override
	public void destroy() {
		if (m_Children != null) {
			for (ITreeNode child : m_Children) {
				child.destroy();
			}
			m_Children = null;
		}
		if (m_Parent != null) {
			m_Parent.removeChild(this);
		}
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		m_ID = input.readInt();
		m_NodeName = input.readUTF();
		m_Key = input.readUTF();
		m_Level = input.readInt();

		int propSize = input.readInt();
		if (propSize > 0) {
			m_Properties = new ArrayMap<String, String>(propSize);
			for (int i = 0; i < propSize; i++) {
				m_Properties.put(input.readUTF(), input.readUTF());
			}
		}

		int nc = input.readInt();
		m_Children = new ArrayList<TreeNode>(nc);
		TreeNode node;
		for (int i = 0; i < nc; i++) {
			node = readNode(input, this);
			m_Children.add(node);
			map.put(node.getKey(), i);
		}
	}

	@Override
	public void write(DataOutput output) throws IOException {

		output.writeInt(m_ID);
		output.writeUTF(m_NodeName);
		output.writeUTF(m_Key);
		output.writeInt(m_Level);

		int propSize = m_Properties == null ? 0 : m_Properties.size();
		output.writeInt(propSize);

		String key, value;
		for (int i = 0; i < propSize; i++) {
			key = m_Properties.getKeyAt(i);
			value = m_Properties.getValueAt(i);
			output.writeUTF(key);
			output.writeUTF(value);
		}

		int nc = numChildren();
		output.writeInt(nc);
		if (nc > 0) {
			for (int i = 0; i < nc; i++) {
				m_Children.get(i).write(output);
			}
		}
	}

	@Override
	public void update() {
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof TreeNode)) {
			return false;
		}
		TreeNode tn = (TreeNode) obj;
		if (m_ID != tn.m_ID || m_Level != tn.m_Level || !m_Key.equals(tn.m_Key) || !m_NodeName.equals(tn.m_NodeName)) {
			return false;
		}

		return ArrayUtils.equals(m_Children, tn.m_Children);
	}

	@Override
	public void toXML(StringBuilder sb) throws UnsupportedEncodingException {
		if (m_Children == null || m_Children.size() == 0) {
			sb.append("<").append(m_NodeName).append(" ID=\"").append(m_ID).append('\"');
			writeProertrXML(sb).append("/>\n");
		} else {
			sb.append("<").append(m_NodeName).append(" ID=\"").append(m_ID).append('\"');
			writeProertrXML(sb).append(">\n");
			for (int i = 0; i < m_Children.size(); i++) {
				m_Children.get(i).toXML(sb);
			}
			sb.append("</").append(m_NodeName).append(">\n");
		}
	}

	@Override
	public void toXML(StringBuilder sb, IChildFilter filter, int... key) throws UnsupportedEncodingException {
		if (m_Children == null || m_Children.size() == 0) {
			sb.append("<").append(m_NodeName).append(" ID=\"").append(m_ID).append('\"');
			writeProertrXML(sb).append("/>\n");
		} else {
			int[] accepted = filter.getAccepted(key);

			if (accepted == null || accepted.length == 0) {
				sb.append("<").append(m_NodeName).append(" ID=\"").append(m_ID).append('\"');
				writeProertrXML(sb).append("/>\n");
			} else {
				sb.append("<").append(m_NodeName).append(" ID=\"").append(m_ID).append('\"');
				writeProertrXML(sb).append(">\n");
				for (int i = 0; i < accepted.length; i++) {
					m_Children.get(accepted[i]).toXML(sb, filter, ArrayUtils.combine(key, accepted[i]));
				}
				sb.append("</").append(m_NodeName).append(">\n");
			}
		}
	}

	private StringBuilder writeProertrXML(StringBuilder sb) throws UnsupportedEncodingException {
		sb.append(" key=\"").append(m_Key).append('\"');
		if (m_Properties == null) {
			return sb;
		}
		String key, value;

		int propSize = m_Properties.size();

		for (int i = 0; i < propSize; i++) {
			key = m_Properties.getKeyAt(i);
			value = m_Properties.getValueAt(i);
			sb.append(' ').append(checkXmlChar(key)).append("=\"").append(checkXmlChar(value)).append('\"');
		}
		return sb;
	}

	public static TreeNode readNode(DataInput input, TreeNode parent) throws IOException {
		TreeNode node = new TreeNode();
		node.readFields(input);
		node.m_Parent = parent;
		return node;
	}

	private static StringBuilder tmp = new StringBuilder();

	/**
	 * @see http://www.w3.org/TR/2004/REC-xml-20040204/#charsets All supported
	 *      characters
	 * @param data
	 *            content in each field
	 * @return regular content is filtered from illegal XML char
	 * @throws UnsupportedEncodingException
	 */
	public static String checkXmlChar(String data) throws UnsupportedEncodingException {
		tmp.setLength(0);
		if (data != null && data.length() > 0) {

			for (int i = 0; i < data.length(); i++) {
				char ch = data.charAt(i);
				if ((ch == 0x9) || (ch == 0xA) || (ch == 0xD) || ((ch >= 0x20) && (ch <= 0xD7FF))
						|| ((ch >= 0xE000) && (ch <= 0xFFFD)) || ((ch >= 0x10000) && (ch <= 0x10FFFF)))
					tmp.append(ch);
			}
		}

		String result = tmp.toString();
		return result.replaceAll("]]>", "");
	}

	private boolean mark;

	@Override
	public void mark() {
		mark = true;
	}

	@Override
	public boolean isMarked() {
		return mark;
	}

	@Override
	public void clearMark() {
		mark = false;
	}

	@Override
	public void clearAllChildrenMark() {
		mark = false;
		if (m_Children != null && m_Children.size() > 0) {
			for (int i = m_Children.size() - 1; i >= 0; i--) {
				m_Children.get(i).clearAllChildrenMark();
			}
		}
	}

}
