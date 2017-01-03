package xjc.PTree.PurTree.PTC;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Test;

import junit.framework.TestCase;
import xjc.data.PTree.TreeNodeCreator;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

public class TestPurTreeDataSet extends TestCase {

	@Test
	public void test() throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(new TreeNodeCreator(new String[] { "v1", "v2" }));
		sd.init();
		sd.addValue("0", "b1");
		sd.addValue("0", "b2");
		sd.addValue("0", "c2");

		// sd.addValue("1", null, "a1", "b1");
		// sd.addValue("1", null, "a1", "b3");
		// sd.addValue("1", null, "b1", "b2");
		// sd.update();
		//
		// sd.addValue("2", null, "a1", "b1");
		// sd.addValue("2", null, "a1", "b3");
		// sd.addValue("2", null, "b1", "b2");
		sd.update();

		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		sd.saveObject("0", writer);
		writer.close();

		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(0.5));
		dis.setData(sd);

		assertEquals(0.222, dis.distance("0", "1"), 0.001);
		assertEquals(0, dis.distance("1", "2"), 0.001);

	}
}
