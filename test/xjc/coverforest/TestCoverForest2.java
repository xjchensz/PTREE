package xjc.coverforest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.google.common.base.Defaults;

import cern.colt.function.IntProcedure;
import common.data.distance.EuclideanDistanceMeasure;
import common.data.instance.IInstance;
import common.data.instance.numeric.sparse.SparseDoubleInstance;
import common.data.meta.MetaData;
import common.utils.RandomUtils;
import common.utils.collection.OrderedDoubleMap;
import common.utils.collection.OrderedIntMap;
import junit.framework.TestCase;
import sun.nio.cs.ext.DoubleByte.Decoder_EUC_SIM;
import test.dataGenerator.SparseDoubleInstanceGenerator;
import xjc.clustering.validation.NormalizedLogW;
import xjc.covertree.CoverTree;
import xjc.covertree.CoverTreeFactory;
import xjc.covertree.CoverTreeInstanceDataset;
import xjc.covertree.Centrality;
import xjc.data.PTree.PurTree.PurTreeClust.CenterMeasureType;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;
import xjc.data.PTree.PurTree.PurTreeDataSet;

public class TestCoverForest2 extends TestCase {

	public static void main(String[] args) throws Exception {

		int treeSize=10;
		
		int numCenters=14;
		//testKRandomCoverTree(degree,baseBegin,baseEnd,offset);
		
		double[] gammas={0,0.2,0.8,1,2,8,1000};
		double [] bases={2};

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
		System.out.println(nearest.getID());

		sdi0 = new SparseDoubleInstance(0, md);
		sdi0.setValue(1, 10);
		nearest = cd.getNearest(sdi0);
		//assertEquals(sdi1, nearest);
	}

}


