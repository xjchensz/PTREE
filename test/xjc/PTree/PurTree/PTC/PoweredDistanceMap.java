/**
 * 
 */
package xjc.PTree.PurTree.PTC;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.tc33.jheatchart.HeatChart;

import xjc.PTree.PurTree.build.BuildSuperStoreData;
import xjc.PTree.PurTree.distance.ComputeDistance;
import xjc.data.PTree.PurTree.PurTreeClust.PurTreeDataCoverTree;

/**
 * @author xiaojun chen
 *
 */
public class PoweredDistanceMap {

	public static void main(String[] args) throws IOException {

		for (int i = 3; i < 4; i++) {
			drawDistance(new File(BuildSuperStoreData.dataDir, "data" + (i + 1)), ComputeDistance.gamma);
		}
	}

	public static void drawDistance(File dir, double[] gamma) throws IOException {
		for (int i = 0; i < gamma.length; i++) {
			PurTreeDataCoverTree csd = PurTreeDataCoverTree.readFile(new File(dir, "data_" + gamma[i] + ".ctr"));
			draw(csd.getDistance().distances(), new File(dir, "p_distance_" + gamma[i] + ".jpg"));
		}
		System.out.println("Finished drawing " + dir);
	}

	@SuppressWarnings("deprecation")
	public static void draw(double[][] data, File file) throws IOException {

		HeatChart map = new HeatChart(data);
		map.setCellHeight(10);
		map.setCellWidth(10);
		map.setAxisThickness(0);
		map.setShowXAxisValues(false);
		map.setShowYAxisValues(false);
		map.setHighValueColour(Color.WHITE);
		map.setLowValueColour(Color.BLACK);
		map.setColourScale(1.5);
		map.setChartMargin(0);
		map.saveToFile(file);
	}
}
