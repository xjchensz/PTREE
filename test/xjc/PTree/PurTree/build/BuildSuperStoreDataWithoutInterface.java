package xjc.PTree.PurTree.build;

import java.io.FileInputStream;
import java.io.IOException;

import common.utils.collection.ArrayMap;
import xjc.data.PTree.ProductTree;
import xjc.data.PTree.ProductTreeBuilder;
import xjc.data.PTree.PurTree.SimpleLineDataParser;

public class BuildSuperStoreDataWithoutInterface {

	public static void main(String[] args) throws IOException {

		BuildSuperStoreData.dataDir.mkdirs();

		final SimpleLineDataParser dataParser = new SimpleLineDataParser(11, new int[] { 15, 16, 17 }, 21) {

			@Override
			protected void parseProperty(String[] values, ArrayMap<String, String>[] map) {
				map[0].put("name", process(values[15]));
				map[1].put("name", process(values[16]));
				map[2].put("name", process(values[17]));
			}

			private String process(String value) {
				return value.replaceAll("\'", "");
			}
		};

		final ProductTreeBuilder stb = new ProductTreeBuilder(
				new String[] { "Product Category", "Product Sub-Category", "Product Name" });

		System.out.println("Starting...");
		ProductTree sd = stb.build(new FileInputStream(BuildSuperStoreData.file), ",", dataParser, 2);
		System.out.println("Saving...");
		sd.save(BuildSuperStoreData.dataDir);
		System.out.println("Saving finished!");

		ProductTree sd1 = new ProductTree(BuildSuperStoreData.dataDir);
		if (sd.equals(sd1)) {
			System.out.println("Succeed!");
		} else {
			System.out.println("Read failed!");
		}
	}

}
