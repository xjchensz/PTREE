/**
 * 
 */
package xjc.PTree.PurTree.distance;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * @author xiaojun chen
 *
 */
public class DrawDistance {

	public static void main(String[] args) throws IOException {

		final JFrame f = new JFrame("ProgressMonitor Sample");
		f.getContentPane().setLayout(new FlowLayout());

		JButton b = new JButton("Draw Distance");
		f.getContentPane().add(b);
		f.pack();

		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				new Thread() {
					public void run() {
						try {

							JFileChooser fc = new JFileChooser();
							fc.showDialog(f, "Select file");
							File file = fc.getSelectedFile();
							if (file.exists()) {
								DistanceMap.drawDistance(file,
										new File(file.getParentFile(), file.getName().replace(".csv", ".jpg")));
							}

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
