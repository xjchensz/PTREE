/**
 * 
 */
package xjc.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.mapdb.DB;
import org.mapdb.DB.HTreeMapMaker;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import common.data.IDataIterator;
import common.data.instance.numeric.sparse.SparseDoubleInstance;
import common.data.io.SerializedDataWriter;
import common.data.meta.DataType;
import common.data.meta.IAttribute;
import common.data.meta.MetaData;
import common.data.meta.NumericAttribute;
import common.utils.StringUtils;

/**
 * @author xiaojun chen
 *
 */
public class DataParser {

	public static void transformSemanticTreeData(File file, int[] indices,
			String[] names, File resultFile) throws Exception {

	}

	public static void transformData(File file, int base, String baseName,
			int variable, String variableName, int amount, File resultFile)
			throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(file));
		// first scan, build metadata
		final HashSet<String> variables = new HashSet<String>();
		final HashSet<String> cards = new HashSet<String>();
		MetaData md = null;

		DB db = DBMaker.tempFileDB().deleteFilesAfterClose()
				.closeOnJvmShutdown().transactionDisable().fileMmapEnable()
				.make();
		HTreeMapMaker maker = db.hashMapCreate("temp");
		final HTreeMap<String, Double> map = maker.make();

		try {
			String line, cardStr, key;
			String[] array;
			Double oldAmt;
			double amt;
			int numRows = 0;
			int numNotify = 1000000;
			final StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				numRows++;
				if (numRows % numNotify == 0) {
					System.out.println("Processed " + numRows + " rows!");
				}
				array = StringUtils.split2Array(line, ',');
				cardStr = array[base];
				if (cardStr.startsWith("'")) {
					cardStr = cardStr.substring(1, cardStr.length());
				}
				if (cardStr.endsWith("'")) {
					cardStr = cardStr.substring(0, cardStr.length() - 1);
				}

				if (cardStr != null && cardStr.length() > 0) {
					cards.add(cardStr);
					variables.add(array[variable]);
					sb.setLength(0);
					sb.append(cardStr).append('_').append(array[variable]);
					key = sb.toString();
					try {
						amt = Double.parseDouble(array[amount]);
						oldAmt = map.get(key);
						if (oldAmt != null) {
							amt += oldAmt;
						}
						map.put(key, amt);
					} catch (NullPointerException | NumberFormatException e) {
						// do nothing
					}
				}
			}

			// build metadata
			IAttribute[] attrs = new IAttribute[1 + variables.size()];
			attrs[0] = new NumericAttribute("cardNo", "cardNo");
			Iterator<String> itr = variables.iterator();
			String value;
			for (int i = 0; i < attrs.length - 1; i++) {
				value = itr.next();
				attrs[i + 1] = new NumericAttribute(value, value);
			}
			md = new MetaData("tianhong", "tianhong", attrs, -1, cards.size(),
					DataType.SPARSE_DOUBLE);
			System.out.println(attrs.length + " items.");
			System.out.println("Total " + numRows + " rows.");

			System.out.println(cards.size() + " cards.");
			md.setNumInstances(cards.size());
			final MetaData metaData = md;
			final int numCards = cards.size();
			SerializedDataWriter sdw = new SerializedDataWriter(
					new IDataIterator<SparseDoubleInstance>() {

						private int index;
						private Iterator<String> itr = cards.iterator();

						@Override
						public boolean hasNext() {
							return index < numCards;
						}

						@Override
						public SparseDoubleInstance next() {

							SparseDoubleInstance ins = new SparseDoubleInstance(
									index, metaData);

							Double value;
							String cardStr = itr.next();
							Iterator<String> tr = variables.iterator();
							for (int i = variables.size() - 1; i >= 0; i--) {
								sb.setLength(0);
								sb.append(cardStr).append('_')
										.append(tr.next());
								value = map.get(sb.toString());
								if (value != null) {
									ins.setValue(i, value);
								}
							}

							index++;
							return ins;

						}

						@Override
						public void remove() {

						}

						@Override
						public void close() throws Exception {
							map.clear();
						}

						@Override
						public boolean isClosed() {
							return false;
						}

						@Override
						public void reset() throws Exception {
							index = 0;
						}

						@Override
						public MetaData getMetaData() {
							return metaData;
						}
					});

			sdw.writeToDirectory(resultFile);
			sdw.close(true);
		} finally {
			br.close();
			db.close();
		}

	}

	public static void readhead(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			System.out.println(br.readLine());
		} finally {
			br.close();
		}
	}

	public static void readLines(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} finally {
			br.close();
		}
	}

	public static void transform(File dataFile, File resultDir)
			throws Exception {
		transformData(dataFile, 7, "Card", 3, "item_skey", 5, new File(
				resultDir, "items"));
		transformData(dataFile, 7, "Card", 8, "item_inclass", 5, new File(
				resultDir, "items"));
		transformData(dataFile, 7, "Card", 10, "item_categ", 5, new File(
				resultDir, "items"));
		transformData(dataFile, 7, "Card", 12, "business_small", 5, new File(
				resultDir, "items"));
		transformData(dataFile, 7, "Card", 14, "brand_id", 5, new File(
				resultDir, "items"));
	}

	public static void main(String[] args) throws Exception {
		File dataFile = new File("D:/工作/projects/天虹/pos_sample.txt");
		File resultDir = new File("D:/工作/projects/天虹/");
		readhead(dataFile);
	}
}
