package xjc.PTree;

import java.io.File;
import java.io.IOException;

import xjc.data.PTree.ProductTree;
import xjc.data.PTree.TreeNode;

public class PrintSemanticTree {

	// static File dir = new File("G:/projects/tianhong/");
	// static File dir = new File("D:/¹¤×÷/projects/Ììºç/");
	static File dir = new File("sample3");
	static final File file = new File(dir, "pos_data.txt");
	static final File dataDir = new File(dir, "tree");

	public static void main(String[] args) throws IOException {

		ProductTree<TreeNode> tree = new ProductTree<TreeNode>(dataDir);
		System.out.println(tree.statisInfor());
	}
}
