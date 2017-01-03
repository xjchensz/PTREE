/**
 * 
 */
package xjc.PTree.PurTree.build;

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
import xjc.PTree.IInputStreamProvider;
import xjc.data.PTree.PurTree.PurTreeDataBuilder;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.SimpleLineDataParser;

/**
 * @author xiaojun chen
 *
 */
public class BuildSuperStoreData {

	public static File dir = new File("SuperStore");
	static final File file = new File(dir, "Sample - Superstore Sales.csv");
	public static final File dataDir = new File(dir, "data");

	public static void main(String[] args) throws IOException {

		dataDir.mkdirs();

		final SimpleLineDataParser dataParser = new SimpleLineDataParser(11, new int[] { 15, 16, 17 }, 21) {

			@Override
			protected void parseProperty(String[] values, ArrayMap<String, String>[] map) {
				map[0].put("name", process(values[15]));
				map[1].put("name", process(values[16]));
				map[2].put("name", process(values[17]));
			}

			private String process(String value) {
				return value.replaceAll("\'", "");
			}
		};

		final PurTreeDataBuilder stb = new PurTreeDataBuilder(
				new String[] { "Product Category", "Product Sub-Category", "Product Name" });

		build(new IInputStreamProvider() {

			@Override
			public InputStream getInputStream() throws FileNotFoundException {
				return new FileInputStream(file);
			}

			@Override
			public void start(InputStream is) throws IOException {
				PurTreeDataSet sd = stb.build(is, ",", dataParser, 1);
				System.out.println("Saving...");
				sd.save(dataDir);
				System.out.println("Saving finished!");

				PurTreeDataSet sd1 = new PurTreeDataSet(dataDir);
				if (sd.equals(sd1)) {
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
