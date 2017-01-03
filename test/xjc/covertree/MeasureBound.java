package xjc.covertree;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

public class MeasureBound {
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		long build = start;

		File dir = new File("SuperStore", "data");
		double gamma = 0.2;
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma));
		dis.setData(sd);
		build = System.currentTimeMillis() - start;
		System.out.println("Load-Time:" + build);

		// calculate build distance-map time
		start = System.currentTimeMillis();
		double[][] distances = dis.distances();// get distance-map

		System.out.println("Build-Time:" + build);

		int length = dis.size();// get distance-dimension-num
		double minDistance = -1;
		double maxDistance = -1;

		for (int i = 0; i < 1; i++) {
			System.out.println("Calculate:" + i);
			start = System.currentTimeMillis();
			// System.out.println(topBound(dis));
			maxDistance = topBound(distances, length);
			System.out.println(maxDistance);
			build = System.currentTimeMillis() - start;
			System.out.println(":" + build);

			start = System.currentTimeMillis();
			// System.out.println(downBound(dis));
			minDistance = downBound(distances, length);
			System.out.println(minDistance);
			build = System.currentTimeMillis() - start;
			System.out.println(":" + build);
		}

		double alpha;
		int height = (int) (Math.log(length) / Math.log(2));// balance-tree
															// height

		System.out.println("");
		System.out.println("");
		System.out.println("balance-tree height:" + height);
		double tt = maxDistance / minDistance;

		System.out.println("Max/Min Rates:" + tt);

		alpha = Math.exp(Math.log(length) / (height - 2));
		System.out.println("alpha max:" + alpha);
		alpha = Math.exp(Math.log(length) / (height));
		System.out.println("alpha min:" + alpha);

		System.out.println();
		System.out.println();
		PurTreeDataCoverTree ctd = new PurTreeDataCoverTree(dis, 5, null, new Random());
		System.out.println(":" + ctd.getCoverTree().minLevel());
	}

	// calculate base
	public static double getBase(double[][] distances, int size) {
		double maxDistance = topBound(distances, size);
		double minDistance = downBound(distances, size);
		int height = (int) (Math.log(size) / Math.log(2));
		double alpha = Math.exp(Math.log(size) / (height));
		return alpha;
	}

	public static double downBound(double[][] dis, int dimen) {
		double minDistance = 1000000;
		double tempDistance = 1000000;
		int first = -1;
		int last = -1;
		for (int i = 0; i < dimen; i++) {
			for (int j = 0; j < dimen; j++) {
				if (i == j)
					continue;
				tempDistance = dis[i][j];
				if (tempDistance < minDistance) {
					minDistance = tempDistance;
					first = i;
					last = j;
				}
				;
			}
		}
		return minDistance;
	}

	public static double topBound(double[][] dis, int dimen) {
		double maxDistance = -1000000;
		double tempDistance = -1000000;
		int first = -1;
		int last = -1;
		for (int i = 0; i < dimen; i++) {
			for (int j = 0; j < dimen; j++) {
				tempDistance = dis[i][j];
				if (tempDistance > maxDistance) {
					maxDistance = tempDistance;
					first = i;
					last = j;
				}
				;
			}
		}
		return maxDistance;
	}

	public static double downBound(PurTreeDistance dis) {
		double minDistance = 1000000;
		double tempDistance = 1000000;
		int first = -1;
		int last = -1;
		for (int i = 0; i < dis.size(); i++) {
			for (int j = 0; j < dis.size(); j++) {
				if (i == j)
					continue;
				tempDistance = dis.distance(i, j);
				if (tempDistance < minDistance) {
					minDistance = tempDistance;
					first = i;
					last = j;
				}
				;
			}
		}
		return minDistance;
	}

	public static double topBound(PurTreeDistance dis) {
		double maxDistance = -1000000;
		double tempDistance = -1000000;
		int first = -1;
		int last = -1;
		for (int i = 0; i < dis.size(); i++) {
			for (int j = 0; j < dis.size(); j++) {
				tempDistance = dis.distance(i, j);
				if (tempDistance > maxDistance) {
					maxDistance = tempDistance;
					first = i;
					last = j;
				}
				;
			}
		}
		return maxDistance;
	}
}
