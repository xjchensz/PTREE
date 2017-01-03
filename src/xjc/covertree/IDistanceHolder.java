/**
 * 
 */
package xjc.covertree;

import common.IWritable;

/**
 * @author xiaojun chen
 *
 */
public interface IDistanceHolder extends IWritable {

	public double[][] distances() throws Exception;

	public double[] distances(int ins1) throws Exception;

	public double distance(int ins1, int ins2);

	public int size();

	public IDistanceHolder clone() throws CloneNotSupportedException;
}
