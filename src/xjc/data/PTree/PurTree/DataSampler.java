/**
 * 
 */
package xjc.data.PTree.PurTree;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import xjc.data.PTree.ProductTree;
import xjc.data.PTree.TreeNode;

/**
 * @author Xiaojun Chen
 *
 */
public class DataSampler {

	public static void sampleUniformData(File dir, Random random, File outDir, int numSamples) throws IOException {
		PurTreeDataSet csd = new PurTreeDataSet(dir);
		double[] sparsity = csd.avgSparsity(random);
		ProductTree<TreeNode> tree = csd.getProductTree();

		int numLevels = csd.numLevels();
		int[] numNodes = tree.numNodes();
		int[] numSlectedNodes = new int[numLevels];
		for (int j = 0; j < numLevels; j++) {
			numSlectedNodes[j] = (int) (numNodes[j] * sparsity[j]);
		}

		File oDir;
		for (int n = 0; n < numSamples; n++) {
			PurTreeDataSet nsd = csd.sampleInTree(numSlectedNodes, random);
			nsd.numNodes(new int[] { 0 });
			oDir = new File(outDir, "sample" + (n + 1));
			oDir.mkdirs();
			nsd.save(oDir);
		}
	}
}
