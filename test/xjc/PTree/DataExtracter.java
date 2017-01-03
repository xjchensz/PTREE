/**
 * 
 */
package xjc.PTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import common.utils.StringUtils;

/**
 * @author Admin
 *
 */
public class DataExtracter {

	public static void extract(File file, String[] conditions, File desFile,
			int count, int c) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file),
				1024 * 1024);
		BufferedWriter bw = new BufferedWriter(new FileWriter(desFile),
				1024 * 1024);
		String line;
		boolean satisfied;
		while ((line = br.readLine()) != null) {
			if (StringUtils.numDelimeters(line, c) < count) {
				continue;
			}

			if (conditions == null || conditions.length == 0) {
				satisfied = true;
			} else {
				satisfied = false;
				for (int i = 0; i < conditions.length; i++) {
					if (line.startsWith(conditions[i])) {
						satisfied = true;
					}
				}
			}

			if (satisfied) {
				bw.write(line);
				bw.write('\n');
			}
		}
		br.close();
		bw.close();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("G:/projects/tianhong/");
		// File dir = new File("D:/¹¤×÷/projects/Ììºç/");
		final File file = new File(dir, "www.txt");
		extract(file, new String[] { "2015" }, new File(dir, "pos_2015.txt"),
				19, ',');
		extract(file, null, new File(dir, "pos_data.txt"), 19, ',');
	}

}
