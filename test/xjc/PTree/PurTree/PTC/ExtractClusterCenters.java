package xjc.PTree.PurTree.PTC;

import java.io.File;
import java.io.IOException;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

public class ExtractClusterCenters {

	public static void main(String[] args) throws IOException {
		File dir = new File(BuildSuperStoreData.dataDir, "data4");
		PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_0.2.ctr"));
		CenterMeasureType cmt = CenterMeasureType.CENTRALITY;
		saveCenters(csd, 30, cmt, new File(dir, "centers"));
	}

	public static void saveCenters(PurTreeDataCoverTree dataset, int numClusters, CenterMeasureType cmt, File dir)
			throws IOException {
		int[] centers = dataset.getKcenters(numClusters, cmt);
		PurTreeDataSet data = dataset.getData();
		dir.mkdir();
		for (int i = 0; i < centers.length; i++) {
			data.saveObject(centers[i], dir);
		}

	}
}
