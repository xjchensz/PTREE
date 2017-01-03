package xjc.data.PTree.PurTree;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class CheckPurTreeDataSet extends TestCase {

	public static boolean check(File dir) throws IOException {
		PurTreeDataSet pd = new PurTreeDataSet(dir);
		return pd.checkData(dir);
	}
}
