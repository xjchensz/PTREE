/**
 * 
 */
package xjc.PTree.PurTree.PTC;

import java.io.IOException;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.data.PTree.PurTree.PurTreeDataSet;

/**
 * @author xiaojun chen
 *
 */
public class SaveObjectTree {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(BuildSuperStoreData.dataDir);
		sd.saveObject("59902", BuildSuperStoreData.dataDir);
		sd.saveObject("55200", BuildSuperStoreData.dataDir);
	}

}
