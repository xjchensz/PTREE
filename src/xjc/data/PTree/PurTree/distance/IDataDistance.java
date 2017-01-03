/**
 * 
 */
package xjc.data.PTree.PurTree.distance;

import common.IWritable;
import xjc.covertree.IDistanceHolder;
import xjc.data.PTree.AbstractPTreeDataSet;

/**
 * @author xiaojun chen
 *
 */
public interface IDataDistance<T extends AbstractPTreeDataSet> extends IDistanceHolder, IWritable {

	public void setData(T data);

	public T getData();

	public double[][] distances();

	public IDataDistance<T> clone();

	public T newDataSet();

	public double distance(int ins1, T data2, int ins2);

	public void destroy();

}
