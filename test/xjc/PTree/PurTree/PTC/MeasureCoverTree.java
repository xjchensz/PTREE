/**
 * 
 */
package xjc.PTree.PurTree.PTC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.PTree.PurTree.distance.ComputeDistance;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

/**
 * @author Great
 *
 */
public class MeasureCoverTree {

	public static void main(String[] args) throws IOException {

		double[] numNodes = new double[10];
		for (int i = 3; i < 4; i++) {
			analyzeCoverTree(new File(BuildSuperStoreData.dataDir, "data" + (i + 1)), ComputeDistance.gamma, numNodes);
		}
	}

	public static void analyzeCoverTree(File dir, double[] gamma, double[] numNodes) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "coverrates.csv")));
		bw.append("Gamma");
		int startLevel = 0;
		for (int j = 0; j < numNodes.length; j++) {
			bw.append(',').append(String.valueOf(startLevel - j));
		}
		bw.append('\n');

		for (int i = 0, j; i < gamma.length; i++) {
			numNodes(dir, gamma[i], numNodes, startLevel);
			bw.append(String.valueOf(gamma[i]));
			for (j = 0; j < numNodes.length; j++) {
				bw.append(',').append(String.valueOf(numNodes[j]));
			}
			bw.append('\n');
		}
		bw.close();
		System.out.println("Finished " + dir);
	}

	public static void numNodes(File dir, double gamma, double[] numNodes, int startLevel) throws IOException {
		PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma + ".ctr"));
		csd.getCoverTree().numNodes(startLevel, numNodes);
	}

}
