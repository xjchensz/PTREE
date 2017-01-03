package xjc.data.PTree.PurTree.distance;

import java.util.Arrays;

/**
 * 
 * @author Administrator
 * @function queue of int arry
 */
public class SingleDimensionArrayQueue {
	private int defaultRange = 1000;
	private final int MAX = 10000000;
	private int[] values;
	private int elenum;
	private int front, rear;

	public static void main(String[] args) throws Exception {
		SingleDimensionArrayQueue sd = new SingleDimensionArrayQueue();
		sd.add(new int[] { 1, 2, 3, 4, 5, 6, 6 });

		sd.add(new int[] { 9, 5, 2, 7 });
		System.out.println(Arrays.toString(sd.outqueue()));
		System.out.println(Arrays.toString(sd.outqueue()));
		sd.add(new int[] { 3, 5, 6, 9 });
		sd.add(new int[] { 3, 5, 123, 42 });
		System.out.println(Arrays.toString(sd.outqueue()));
		sd.add(new int[] { 13, 5 });
		sd.add(new int[] { 3, 5, 6, 9 });
		while (sd.size() > 0) {
			int[] d = sd.outqueue();
			System.out.println(Arrays.toString(d));
		}
	}

	public SingleDimensionArrayQueue() {
		values = new int[defaultRange];
		front = rear = 0;
	}

	public void add(int[] d) throws Exception {
		int length = values.length;
		if (length - 1 - (rear - front + length) % length < d.length + 1) {

			if (length + d.length + 1 > MAX) {
				throw new Exception("beyond size limit");
			}
			int[] newvalues = new int[(int) (values.length
					+ (values.length * 0.3 < d.length + 1 ? (d.length + 1) * 2 : values.length * 0.3))];

			int index = 0, c = 0;
			int[] tmp;

			while (this.size() > 0) {
				tmp = this.outqueue();
				newvalues[++index] = tmp.length;
				for (int e : tmp) {
					newvalues[++index] = e;
				}
				c++;
			}
			values = newvalues;
			front = 0;
			rear = index;
			elenum = c;
			length = values.length;
		}

		rear = (rear + 1) % length;
		values[rear] = d.length;
		for (int e : d) {
			rear = (rear + 1) % length;
			values[rear] = e;
		}

		this.elenum++;
	}

	public int front() throws Exception {

		if (isempty()) {
			throw new Exception("empty queue can't outqueue");
		}
		return values[(front + 1) % values.length];

	}

	public int[] outqueue() throws Exception {
		if (isempty()) {
			throw new Exception("empty queue can't outqueue");
		}

		front = (front + 1) % values.length;
		int size = values[front];
		int[] data = new int[size];
		while (size > 0) {
			front = (front + 1) % values.length;
			data[data.length - size--] = values[front];
		}
		this.elenum--;
		return data;
	}

	public boolean isempty() throws Exception {
		return this.elenum == 0;
	}

	public int size() {
		return this.elenum;
	}
}
