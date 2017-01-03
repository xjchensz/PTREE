/**
 * 
 */
package xjc.PTree.PurTree.distance;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.tc33.jheatchart.HeatChart;

import common.utils.StringUtils;
import xjc.data.PTree.PurTree.PurTreeDataSet;
import xjc.data.PTree.PurTree.PurTreeDist.LevelWeightedDistance;
import xjc.data.PTree.PurTree.distance.PurTreeDistance;

/**
 * @author xiaojun chen
 *
 */
public class DistanceMap {

	public static void main(String[] args) throws IOException {

	}

	public static void drawDistance(File dir, double[] gamma) throws IOException {
		PurTreeDataSet sd = new PurTreeDataSet(dir);
		for (int i = 0; i < gamma.length; i++) {
			PurTreeDistance dis = new PurTreeDistance(new LevelWeightedDistance(gamma[i]));
			dis.setData(sd);
			draw(dis.distances(), new File(dir, "distance_" + gamma[i] + ".jpg"));
		}
		System.out.println("Finished drawing " + dir);
	}

	public static void drawDistance(File distanceFile, File imageFile) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(distanceFile));
		String line = br.readLine();
		String[] array = StringUtils.split2Array(line, ',');

		double[][] data = new double[array.length][array.length];
		for (int i = 0; i < array.length; i++) {
			data[0][i] = Double.parseDouble(array[i]);
		}
		int index = 1;
		while ((line = br.readLine()) != null) {
			array = StringUtils.split2Array(line, ',');
			for (int i = 0; i < array.length; i++) {
				data[index][i] = Double.parseDouble(array[i]);
			}
			index++;
		}
		br.close();

		draw(data, imageFile);
	}

	@SuppressWarnings("deprecation")
	public static void draw(double[][] data, File file) throws IOException {

		HeatChart map = new HeatChart(data);
		map.setCellHeight(1);
		map.setCellWidth(1);
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
