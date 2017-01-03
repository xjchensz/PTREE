package xjc.covertree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DB.HTreeMapMaker;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import test.dataGenerator.SparseDoubleInstanceGenerator;

import common.data.instance.numeric.sparse.SparseDoubleInstance;
import common.data.meta.MetaData;

public class InstanceMapTester extends TestCase {

	public static class InstanceSerializer extends
			Serializer<SparseDoubleInstance> implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = -8638286687142039561L;
		private transient MetaData m_Meta;

		public InstanceSerializer(MetaData meta) {
			m_Meta = meta;
		}

		@Override
		public void serialize(DataOutput out, SparseDoubleInstance value)
				throws IOException {
			if (value == null) {
				System.err.println("Custom serializer called with 'null'");
			} else {
				value.write(out);
			}
		}

		@Override
		public SparseDoubleInstance deserialize(DataInput in, int available)
				throws IOException {
			return (SparseDoubleInstance) m_Meta.readInstance(in, null);
		}
	}

	@Test
	public void test() {
		SparseDoubleInstanceGenerator sg = new SparseDoubleInstanceGenerator();
		MetaData md = sg.generateMetaData("a", "a", 100, 1000, new Random(),
				true);

		SparseDoubleInstance sdi1 = new SparseDoubleInstance(1, md);
		sdi1.setValue(1, 20);

		InstanceSerializer is = new InstanceSerializer(md);

		DB db = DBMaker.tempFileDB().deleteFilesAfterClose()
				.closeOnJvmShutdown().transactionDisable()
				.fileMmapEnableIfSupported().make();
		HTreeMapMaker maker = db.hashMapCreate("temp").valueSerializer(is);
		final HTreeMap<String, SparseDoubleInstance> map = maker.make();
		map.put("a", sdi1);
		assertEquals(sdi1, map.get("a"));
	}
}
