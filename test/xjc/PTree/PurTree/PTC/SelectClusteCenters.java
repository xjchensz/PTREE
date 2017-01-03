package xjc.PTree.PurTree.PTC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import common.utils.collection.OrderedIntMap;
import xjc.covertree.Centrality;
import xjc.covertree.SeparateDensity;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

public class SelectClusteCenters {

	public static void selectKCentrailityCenters(File dir, double gamma, int numClusters, int k, Random random)
			throws IOException {
		PurTreeDataCoverTree pct = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma + ".ctr"));
		Centrality[] kc = pct.getCoverTree().getKCentralityCenterNodePairs(numClusters, k);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "centralities.csv")));
		bw.append("id,kradius,cdist,centrality\n");
		Centrality centrality;
		for (int i = 0; i < kc.length; i++) {
			centrality = kc[i];
			bw.append(String.valueOf(centrality.getID())).append(',').append(String.valueOf(centrality.getKnnradius()))
					.append(',').append(String.valueOf(centrality.getCdist())).append(',')
					.append(String.valueOf(centrality.getCentrality())).append('\n');
		}
		bw.close();
	}

	public static void selectLDensities(File dir, double gamma, int k, Random random) throws IOException {
		PurTreeDataCoverTree pct = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma + ".ctr"));
		OrderedIntMap densities = pct.getCoverTree().levelDensity(k);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "ldensities.csv")));
		bw.append("id,ld\n");
		int size = densities.size();
		for (int i = 0; i < size; i++) {
			bw.append(String.valueOf(densities.getKeyAt(i))).append(',').append(String.valueOf(densities.getValueAt(i)))
					.append('\n');
		}
		bw.close();
	}

	public static void selectSeparateDensities(File dir, double gamma, int k, Random random) throws IOException {
		PurTreeDataCoverTree pct = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma + ".ctr"));
		SeparateDensity[] sden = pct.getCoverTree().getSepearateDensity(k);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, "separate_densities.csv")));
		bw.append("id,ld,sdis,sden\n");
		int size = sden.length;
		SeparateDensity sd;
		for (int i = 0; i < size; i++) {
			sd = sden[i];
			bw.append(String.valueOf(sd.getID())).append(',').append(String.valueOf(sd.getLevelDensity())).append(',')
					.append(String.valueOf(sd.getSeprateDistance())).append(',')
					.append(String.valueOf(sd.getSeprateDensity())).append('\n');
		}
		bw.close();
	}
}
