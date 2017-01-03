/**
 * 
 */
package xjc.PTree;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ProgressMonitorInputStream;

import common.utils.collection.ArrayMap;
import xjc.data.PTree.ProductTree;
import xjc.data.PTree.ProductTreeBuilder;
import xjc.data.PTree.SimpleLineKeyParser;
import xjc.data.PTree.TreeNode;

/**
 * @author xiaojun chen
 *
 */
public class BuildProductTree {

	// static File dir = new File("G:/projects/tianhong/");
	// static File dir = new File("D:/¹¤×÷/projects/Ììºç/");
	static File dir = new File("sample3");
	static final File file = new File(dir, "pos_data.txt");
	public static final File treeDir = new File("tree").getAbsoluteFile();

	public static void main(String[] args) throws IOException {

		treeDir.mkdirs();

		final SimpleLineKeyParser keyParser = new SimpleLineKeyParser(new int[] { 13, 11, 9, 15, 3 }, 20) {

			@Override
			protected void parseProperty(String[] values, ArrayMap<String, String>[] map) {
				map[0].put("name", process(values[14]));
				map[1].put("name", process(values[12]));
				map[2].put("name", process(values[10]));
				map[3].put("name", process(values[16]));
				map[4].put("name", process(values[4]));
			}

			private String process(String value) {
				return value.replaceAll("\'", "");
			}
		};

		final ProductTreeBuilder builder = new ProductTreeBuilder(
				new String[] { "business_small", "item_categ", "item_inclass", "brand_id", "item_skey" });

		build(new IInputStreamProvider() {

			@Override
			public InputStream getInputStream() throws FileNotFoundException {
				return new FileInputStream(file);
			}

			@Override
			public void start(InputStream is) throws IOException {
				ProductTree<TreeNode> tree = builder.build(is, ",", keyParser, 2);
				tree.save(treeDir);

				ProductTree<TreeNode> tree1 = new ProductTree<TreeNode>(treeDir);
				if (tree.equals(tree1)) {
					System.out.println("Succeed!");
				} else {
					System.out.println("Read failed!");
				}
			}
		});
	}

	public static void build(final IInputStreamProvider provider) {

		final JFrame f = new JFrame("ProgressMonitor Sample");
		f.getContentPane().setLayout(new FlowLayout());
		JButton b = new JButton("Build data");
		f.getContentPane().add(b);
		f.pack();

		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				new Thread() {
					public void run() {
						try {
							ProgressMonitorInputStream pm = new ProgressMonitorInputStream(f, "Processing file: ",
									provider.getInputStream());
							provider.start(pm);
							pm.close();
							f.dispose();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

				}.start();
			}
		});

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}
