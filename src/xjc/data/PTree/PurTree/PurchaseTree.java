/**
 * 
 */
package xjc.data.PTree.PurTree;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;

import common.IWritable;

/**
 * @author xiaojun chen
 *
 */
public class PurchaseTree<M extends IPTreeNode> implements IWritable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4474692954082716439L;
	private M m_Root;
	private IPTreeNodeCreator<M> m_Creator;
	private int[] m_NumNodes;
	private HashMap<String, M> leafNodes;

	private int m_NumLevels;

	public PurchaseTree(File dir) throws IOException {
		// load tree
		DataInputStream dis = new DataInputStream(new FileInputStream(new File(
				dir, "ptree.bin")));
		readFields(dis);
		dis.close();
	}

	public PurchaseTree(DataInput dis) throws IOException {
		readFields(dis);
	}

	public PurchaseTree(IPTreeNodeCreator<M> creator) {
		this(creator.getRootNode(), creator);
	}

	public PurchaseTree(M root, IPTreeNodeCreator<M> creator) {
		m_Creator = creator;
		m_Root = root;
		leafNodes = new HashMap<String, M>();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(m_NumLevels);
		for (int i = 0; i < m_NumLevels; i++) {
			out.writeInt(m_NumNodes[i]);
		}
		out.writeUTF(getCreatorClass().getName());
		m_Root.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		m_NumLevels = in.readInt();
		m_NumNodes = new int[m_NumLevels];
		for (int i = 0; i < m_NumLevels; i++) {
			m_NumNodes[i] = in.readInt();
		}

		Class<IPTreeNodeCreator<M>> classz;
		try {
			classz = (Class<IPTreeNodeCreator<M>>) Class.forName(in.readUTF());
			m_Creator = classz.newInstance();
			m_Root = m_Creator.loadNode(in);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			throw new IOException(e);
		}

	}

	public String toXML() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		m_Root.toXML(sb);
		return sb.toString();
	}

	public void saveXML(Writer writer) throws IOException {
		writer.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
		writer.append(toXML());
	}

	public void saveXML(File file) throws IOException {
		// save tree
		FileWriter writer = new FileWriter(file);
		saveXML(writer);
		writer.close();
	}

	public void save(File dir) throws IOException {
		// save tree
		FileWriter writer = new FileWriter(new File(dir, "ptree.xml"));
		saveXML(writer);
		writer.close();

		// save creator
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(
				new File(dir, "ptree.bin")));
		write(dos);
		dos.close();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof PurchaseTree)) {
			return false;
		}
		PurchaseTree<M> st = (PurchaseTree<M>) obj;

		return st.m_NumLevels == m_NumLevels && m_Root.equals(st.m_Root);
	}

	public PurchaseTree<M> createTree() {
		return new PurchaseTree<M>(m_Creator);
	}

	public Class<IPTreeNodeCreator<M>> getCreatorClass() {
		return (Class<IPTreeNodeCreator<M>>) m_Creator.getClass();
	}

	public void destroy() {
		m_Root.destroy();
		m_Root = null;
		m_Creator.destroy();
	}
}
