package xjc.covertree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class TestMapDB {
	public static class MyValue implements Serializable {
		private String string;

		public MyValue(String string) {
			this.string = string;
		}

		@Override
		public String toString() {
			return "MyValue{" + "string='" + string + '\'' + '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof MyValue))
				return false;

			MyValue myValue = (MyValue) o;
			if (!string.equals(myValue.string))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			return string.hashCode();
		}
	}

	public static class MyValueSerializer extends Serializer<MyValue> implements
			Serializable {

		@Override
		public void serialize(DataOutput out, MyValue value) throws IOException {
			if (value == null) {
				System.err.println("Custom serializer called with 'null'");
			} else {
				System.out.println("Custom serializer called with '" + value
						+ "'");
				out.writeUTF(value.string);
			}
		}

		@Override
		public MyValue deserialize(DataInput in, int available)
				throws IOException {
			String s = in.readUTF();
			return new MyValue(s);
		}
	}

	private static void printEntries(Map<Long, MyValue> map) {
		System.out.println("Reading back data");
		for (Map.Entry<Long, MyValue> entry : map.entrySet()) {
			System.out.println("Entry id = " + entry.getKey() + ", contents = "
					+ entry.getValue().toString());
		}
	}

	private static void testHashMap(File path) {
		System.out.println("--- Testing HashMap with custom serializer");

		DB db = DBMaker.tempFileDB().transactionDisable().closeOnJvmShutdown()
				.fileMmapEnable().make();
		Map<Long, MyValue> map = db.hashMapCreate("temp")
				.valueSerializer(new MyValueSerializer()).make();

		System.out.println("Putting and committing data");
		map.put(1L, new MyValue("one"));
		map.put(2L, new MyValue("two"));
		db.commit();
		System.out.println("Closing and reopening db");
		db.close();
		map = null;

		db = DBMaker.newFileDB(path).make();
		map = db.getHashMap("map");

		printEntries(map);
	}

	private static void testBTreeMap(File path) {
		System.out.println("--- Testing BTreeMap with custom serializer");

		DB db = DBMaker.tempFileDB().transactionDisable().closeOnJvmShutdown()
				.fileChannelEnable().make();
		Map<Long, MyValue> map = db.treeMapCreate("temp")
				.valueSerializer(new MyValueSerializer()).make();
		db.commit();

		System.out.println("Putting and committing data");
		map.put(1L, new MyValue("one"));
		map.put(2L, new MyValue("two"));
		db.commit();

		System.out.println("Closing and reopening db");
		db.close();
		map = null;

		db = DBMaker.newFileDB(path).make();
		map = db.getTreeMap("map");

		printEntries(map);
	}

	public static void main(String[] args) {
		File dir = new File("D:/tmp");
		dir.mkdirs();
		try {
			testHashMap(new File(dir, "hashtest"));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		try {
			testBTreeMap(new File(dir, "treetest"));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
