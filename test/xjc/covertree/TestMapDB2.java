package xjc.covertree;

import java.io.File;
import java.util.Map;

import junit.framework.TestCase;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public class TestMapDB2 extends TestCase {

	public void testHashMap(File path) {
		DB db = DBMaker.memoryDirectDB().transactionDisable()
				.closeOnJvmShutdown().make();
		// DB db = DBMaker.fileDB(path).fileMmapEnable().transactionDisable()
		// .closeOnJvmShutdown().make();
		if (db.exists("temp")) {
			db.delete("temp");
		}
		Map<String, Double> map = db.hashMapCreate("temp").make();
		int size = 2000000;
		for (int i = 0; i < size; i++) {
			map.put("key" + i, 1.0);
		}

		double value;
		for (int i = 0; i < size; i++) {
			value = map.get("key" + i);
			value += i;
			map.put("key" + i, value);
		}

		for (int i = 0; i < size; i++) {
			value = map.get("key" + i);
			assertEquals(value, i + 1.0, 0);
		}
	}

	public void test() {
		File dir = new File("D:/tmp");
		dir.mkdirs();
		try {
			testHashMap(new File(dir, "hashtest"));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
