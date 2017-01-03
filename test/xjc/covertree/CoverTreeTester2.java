package xjc.covertree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;

public class CoverTreeTester2 extends TestCase {

	@Test
	public void test() {
		final int size = 100;
		CoverTree ct = CoverTreeFactory.getDefault().create(new IDistanceHolder() {

			/**
			* 
			*/
			private static final long serialVersionUID = 1L;

			@Override
			public void write(DataOutput out) throws IOException {

			}

			@Override
			public void readFields(DataInput in) throws IOException {

			}

			@Override
			public double distance(int ins1, int ins2) {
				return Math.abs(ins1 - ins2);
			}

			@Override
			public int size() {
				return size;
			}

			@Override
			public double[] distances(int ins1) throws Exception {
				double[] results = new double[size()];
				for (int i = 0; i < results.length; i++) {
					results[i] = distance(ins1, i);
				}
				return results;
			}

			@Override
			public double[][] distances() throws Exception {
				double[][] results = new double[size()][size()];
				for (int i = 0, j; i < results.length; i++) {
					for (j = i + 1; j < results[i].length; j++) {
						results[i][j] = results[j][i] = distance(i, j);
					}
				}
				return results;
			}

			public IDistanceHolder clone() throws CloneNotSupportedException {
				return (IDistanceHolder) super.clone();
			}
		});

		for (int i = 0; i < size; i++) {
			ct.insert(i);
		}
		ct.update();

		for (int i = 0; i < size - 1; i++) {
			assertEquals(i, ct.getNearest(i));
		}

	}
}
