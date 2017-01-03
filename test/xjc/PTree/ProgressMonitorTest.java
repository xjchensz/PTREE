/**
 * 
 */
package xjc.PTree;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ProgressMonitorInputStream;

/**
 * @author Admin
 *
 */
public class ProgressMonitorTest {
	public static void main(String[] args) {
		final JFrame f = new JFrame("ProgressMonitor Sample");
		f.getContentPane().setLayout(new FlowLayout());
		JButton b = new JButton("Click me");
		f.getContentPane().add(b);
		f.pack();

		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				new Thread() {
					public void run() {
						try {
							InputStream in = new FileInputStream("bigfile.dat");
							ProgressMonitorInputStream pm = new ProgressMonitorInputStream(
									f, "Reading a big file", in);
							int c;
							while ((c = pm.read()) != -1) {
							}
							pm.close();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

				}.start();
			}
		});

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
