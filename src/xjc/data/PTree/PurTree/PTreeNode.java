/**
 * 
 */
package xjc.data.PTree.PurTree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import common.utils.ArrayUtils;

/**
 * @author xjchen
 *
 */
public class PTreeNode implements IPTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1083222005453668386L;
	private int m_ID;
	private int m_Level = -1;
	private IPTreeNode m_Parent;
	private String m_Key;

	private ArrayList<IPTreeNode> m_Children;

	protected PTreeNode() {
	}

	public PTreeNode(IPTreeNode parent, int id) {
		m_Parent = parent;
		if (m_Parent != null) {
			m_Level = m_Parent.getLevel() + 1;
		}
		m_ID = id;
	}

	@Override
	public int getID() {
		return m_ID;
	}

	public void addChild(IPTreeNode child) {
		if (m_Children == null) {
			m_Children = new ArrayList<IPTreeNode>();
		}
		if (!m_Children.contains(child)) {
			m_Children.add(child);
		}
	}

	@Override
	public IPTreeNode getParent() {
		return m_Parent;
	}

	@Override
	public List<IPTreeNode> getChildren() {
		return m_Children;
	}

	@Override
	public IPTreeNode getChild(int index) {
		if (m_Children == null || m_Children.size() <= index) {
			return null;
		}
		return m_Children.get(index);
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
			for (IPTreeNode child : m_Children) {
				child.destroy();
			}
			m_Children = null;
		}
	}

	@Override
	public void readFields(DataInput input) throws IOException {
		m_ID = input.readInt();
		m_Key = input.readUTF();
		m_Level = input.readInt();

		int nc = input.readInt();
		m_Children = new ArrayList<IPTreeNode>(nc);
		if (nc > 0) {
			try {
				Class<? extends IPTreeNode> classz = (Class<? extends IPTreeNode>) Class
						.forName(input.readUTF());
				IPTreeNode node;
				for (int i = 0; i < nc; i++) {
					node = classz.newInstance();
					node.readFields(input);
					m_Children.add(node);
				}
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

	}

	@Override
	public void write(DataOutput output) throws IOException {

		output.writeInt(m_ID);
		output.writeUTF(m_Key);
		output.writeInt(m_Level);

		int nc = numChildren();
		output.writeInt(nc);
		if (nc > 0) {
			output.writeUTF(m_Children.get(0).getClass().getName());
			for (int i = 0; i < nc; i++) {
				m_Children.get(i).write(output);
			}
		}
	}

	@Override
	public void update() {
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PTreeNode)) {
			return false;
		}
		PTreeNode tn = (PTreeNode) obj;
		if (m_ID != tn.m_ID || m_Level != tn.m_Level || !m_Key.equals(tn.m_Key)) {
			return false;
		}

		return ArrayUtils.equals(m_Children, tn.m_Children);
	}

	@Override
	public void toXML(StringBuilder sb) throws UnsupportedEncodingException {
		if (m_Children == null || m_Children.size() == 0) {
			sb.append("<node ID=\"").append(m_ID).append("\" ");
			writeContent(sb);
			sb.append(">\n");
		} else {
			sb.append("<node ID=\"").append(m_ID).append("\" ");
			writeContent(sb);
			sb.append("/>\n");
			for (int i = 0; i < m_Children.size(); i++) {
				m_Children.get(i).toXML(sb);
			}
			sb.append("</node>\n");
		}
	}

	protected void writeContent(StringBuilder sb) {

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
	public static String checkXmlChar(String data)
			throws UnsupportedEncodingException {
		tmp.setLength(0);
		if (data != null && data.length() > 0) {

			for (int i = 0; i < data.length(); i++) {
				char ch = data.charAt(i);
				if ((ch == 0x9) || (ch == 0xA) || (ch == 0xD)
						|| ((ch >= 0x20) && (ch <= 0xD7FF))
						|| ((ch >= 0xE000) && (ch <= 0xFFFD))
						|| ((ch >= 0x10000) && (ch <= 0x10FFFF)))
					tmp.append(ch);
			}
		}

		String result = tmp.toString();
		return result.replaceAll("]]>", "");
	}
}
