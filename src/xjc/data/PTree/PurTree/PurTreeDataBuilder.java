/**
 * 
 */
package xjc.data.PTree.PurTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import common.utils.StringUtils;
import xjc.data.PTree.ProductTree;
import xjc.data.PTree.TreeNode;
import xjc.data.PTree.TreeNodeCreator;
import xjc.data.PTree.PurTree.PurTreeDist.ILineDataParser;
import xjc.data.PTree.PurTree.PurTreeDist.ISimpleLineDataKeyParser;

/**
 * @author xiaojun chen
 *
 */
public class PurTreeDataBuilder {

	private int notifyRows;

	private TreeNodeCreator m_TreeNodeCreator;

	public PurTreeDataBuilder(String[] levelNames) {
		this(levelNames, 100000);
	}

	public PurTreeDataBuilder(String[] levelNames, int notifyRows) {
		m_TreeNodeCreator = new TreeNodeCreator(levelNames);
		this.notifyRows = notifyRows;
	}

	public PurTreeDataSet build(File file, String delimiter, ILineDataParser parser, int skipLines) throws IOException {
		return build(new FileInputStream(file), delimiter, parser, skipLines);
	}

	public PurTreeDataSet build(InputStream is, String delimiter, ILineDataParser parser, int skipLines)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		try {
			PurTreeDataSet data = new PurTreeDataSet(m_TreeNodeCreator);
			data.init();
			ProductTree<TreeNode> tree = data.getProductTree();

			String line;
			String dataID;
			String[] keys;
			for (int i = 0; i < skipLines; i++) {
				br.readLine();
			}
			String[] arrays = new String[parser.maxColumns()];
			int i = skipLines;
			while ((line = br.readLine()) != null) {
				try {
					if (parser.parse(StringUtils.split2ArrayDirect(line, delimiter, arrays))) {
						dataID = parser.getDataID();
						keys = parser.keys();
						if (dataID != null && dataID.length() > 0 && keys != null && keys.length > 0) {
							tree.add(parser.getProperties(), keys);
							data.addValue(dataID, keys[keys.length - 1]);
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (++i % notifyRows == 0) {
					System.out.println("Processed " + i + " rows.");
				}
			}

			br.close();
			data.update();
			return data;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public PurTreeDataSet build(ProductTree tree, InputStream is, String delimiter, ISimpleLineDataKeyParser parser,
			int skipLines) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		try {
			PurTreeDataSet data = new PurTreeDataSet(tree);
			data.init();

			String line;
			String dataID;
			String key;
			for (int i = 0; i < skipLines; i++) {
				br.readLine();
			}
			String[] arrays = new String[parser.maxColumns()];
			int i = skipLines;
			while ((line = br.readLine()) != null) {
				try {
					if (parser.parse(StringUtils.split2ArrayDirect(line, delimiter, arrays))) {
						dataID = parser.getDataID();
						key = parser.getKey();
						if (dataID != null && dataID.length() > 0 && key != null && key.length() > 0) {
							data.addValue(dataID, key);
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (++i % notifyRows == 0) {
					System.out.println("Processed " + i + " rows.");
				}
			}

			br.close();
			data.update();
			return data;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}
}
