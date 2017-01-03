/**
 * 
 */
package xjc.data.PTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import common.utils.StringUtils;

public class ProductTreeBuilder {

	private TreeNodeCreator m_TreeNodeCreator;

	public ProductTreeBuilder(String[] levelNames) {
		m_TreeNodeCreator = new TreeNodeCreator(levelNames);
	}

	public ProductTree<TreeNode> build(File file, String delimiter, ILineKeyParser parser, int skipLines)
			throws IOException {
		return build(new FileInputStream(file), delimiter, parser, skipLines);
	}

	public ProductTree<TreeNode> build(InputStream is, String delimiter, ILineKeyParser parser, int skipLines)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		try {
			ProductTree<TreeNode> tree = new ProductTree<TreeNode>(m_TreeNodeCreator);

			String line;
			String[] keys;
			for (int i = 0; i < skipLines; i++) {
				br.readLine();
			}
			String[] arrays = new String[parser.maxColumns()];
			while ((line = br.readLine()) != null) {
				try {
					if (parser.parse(StringUtils.split2ArrayDirect(line, delimiter, arrays))) {
						keys = parser.keys();
						if (keys != null && keys.length > 0) {
							tree.add(parser.getProperties(), keys);
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			br.close();
			tree.update();
			return tree;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

}
