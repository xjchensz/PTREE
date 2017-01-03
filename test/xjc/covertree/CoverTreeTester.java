package xjc.covertree;

import java.util.Random;

import org.junit.Test;

import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.IInstance;
import common.data.instance.numeric.sparse.SparseDoubleInstance;
import common.data.meta.MetaData;
import junit.framework.TestCase;
import test.dataGenerator.SparseDoubleInstanceGenerator;

public class CoverTreeTester extends TestCase {

	@Test
	public void test() {
		SparseDoubleInstanceGenerator sg = new SparseDoubleInstanceGenerator();
		MetaData md = sg.generateMetaData("a", "a", 100, 1000, new Random(), true);
		CoverTreeInstanceDataset cd = new CoverTreeInstanceDataset(EuclideanDistanceMeasure.getInstance());

		SparseDoubleInstance sdi1 = new SparseDoubleInstance(1, md);
		sdi1.setValue(1, 20);
		cd.addInstance(sdi1);
		SparseDoubleInstance sdi2 = new SparseDoubleInstance(2, md);
		sdi2.setValue(0, 20);
		sdi2.setValue(1, 20);
		cd.addInstance(sdi2);
		SparseDoubleInstance sdi3 = new SparseDoubleInstance(3, md);
		sdi3.setValue(0, 10);
		cd.addInstance(sdi3);
		SparseDoubleInstance sdi4 = new SparseDoubleInstance(4, md);
		sdi4.setValue(0, 10);
		cd.addInstance(sdi4);

		SparseDoubleInstance sdi0 = new SparseDoubleInstance(0, md);
		sdi0.setValue(5, 10);

		IInstance nearest = cd.getNearest(sdi0);
		assertEquals(sdi3, nearest);

		sdi0 = new SparseDoubleInstance(0, md);
		sdi0.setValue(1, 10);
		nearest = cd.getNearest(sdi0);
		assertEquals(sdi1, nearest);
	}
}
