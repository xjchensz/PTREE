/**
 * 
 */
package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import xjc.PTree.PurTree.distance.ComputeDistance;
import xjc.data.PTree.PurTree.PurTreeClust.ClusterNumberChooser;
import xjc.data.PTree.PurTree.PurTreeClust.ITester;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

/**
 * @author xiaojun chen
 *
 */
public class GapTester {

	static int numCopies = 10;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
	}

	public static int testGap(File dir, PurTreeDataCoverTree csd, int numCopies, int[] k, ITester[] tester,
			String suffix) throws IOException {
		ClusterNumberChooser chooser = new ClusterNumberChooser(csd, numCopies);
		int nc = chooser.bestPartition(k, tester, new Random(10));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tester.length; i++) {
			sb.append(tester[i].getName()).append(',');
		}
		sb.setLength(sb.length() - 1);
		sb.append('\n');
		String head = sb.toString();
		ComputeDistance.write(head, chooser.logEWk(), new File(dir, "logEwk_" + suffix + ".csv"));
		ComputeDistance.write(head, chooser.logWk(), new File(dir, "logwk_" + suffix + ".csv"));
		ComputeDistance.write(head, chooser.getGap(), new File(dir, "gap_" + suffix + ".csv"));
		ComputeDistance.write(head, chooser.sk(), new File(dir, "sk_" + suffix + ".csv"));
		return nc;
	}
}
