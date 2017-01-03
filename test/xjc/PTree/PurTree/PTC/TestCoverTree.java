package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.IOException;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

public class TestCoverTree {

	public static void main(String[] args) throws IOException {
		PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(BuildSuperStoreData.dataDir, "data.ctr"));
		System.out.println(csd.print());
		int id = csd.getNearest(2765);
		System.out.println(csd.distance(2765, id));
	}

}
