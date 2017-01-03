package xjc.covertree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

//Measure base from baseBegin to Base End in the  setting ,save results to the file 
public class MeasureBase {
	public static void main(String[] args) throws IOException {
		File dir = new File("SuperStore", "data");
		double gamma = 0.2;
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);

		double baseBegin = 2.0;
		double baseEnd = 2.3;
		double offset = 0.01;
		dir=new File(dir,"measureBase");
		if(!dir.exists()) dir.mkdir(); 
		findBase(dir, dis, baseBegin, baseEnd, offset);

		// testBase(dir,dis,MeasureBound.getBase(dis.distances(), sd.si ze()));

	}

	public static void testBase(File dir, PurTreeDistance dis, double base) throws IOException {
		long start = System.currentTimeMillis();
		long build = start;
		long buildTime = build;
		long loadTime = build;

		BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir, "testBase_ct_(" + base + ").csv")));
		abw.append("iteration,base,buildTime\n");
		for (int j = 0; j < 3; j++) {

			System.out.println("TestBase Start:" + j);

			start = System.currentTimeMillis();
			PurTreeDataCoverTree ctd = new PurTreeDataCoverTree(dis, base, null, new Random());
			build = System.currentTimeMillis() - start;
			abw.append("" + j + ',');
			abw.append(Double.toString(base)).append(',');
			abw.append(Double.toString(build)).append('\n');

			System.out.println("TestBase end");
		}
		abw.close();
	}

	public static void findBase(File dir, PurTreeDistance dis, double baseBegin, double baseEnd, double offset)
			throws IOException {
		long start = System.currentTimeMillis();
		long build = start;
		long buildTime = build;
		long loadTime = build;

		for (int j = 0; j < 3; j++) {
			BufferedWriter abw = new BufferedWriter(new FileWriter(new File(dir,
					"findBase_ct_(" + baseBegin + "_" + baseEnd + "_" + offset + ")" + (j + 1) + ".csv")));
			System.out.println("findBase Start:" + j);
			start = System.currentTimeMillis();
			long startRecur = System.currentTimeMillis();

			double[] base;
			int i = 0;
			for (double baseCur = baseBegin; baseCur <= baseEnd; baseCur += offset, i++) {
				abw.append(Double.toString(baseCur)).append(',');
				// startRecur=System.currentTimeMillis();
				PurTreeDataCoverTree ctd = new PurTreeDataCoverTree(dis, baseCur, null, new Random());
				abw.append(Double.toString(ctd.getCoverTree().minLevel())).append(',');
				abw.append(Double.toString(ctd.buildTime()) + "\n");
			}
			build = System.currentTimeMillis() - start;
			abw.append("Build Time:," + build + '\n');
			abw.close();
			System.out.println("findBase End");
		}
	}
}
