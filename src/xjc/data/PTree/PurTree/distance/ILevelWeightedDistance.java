/**
 * 
 */
package xjc.data.PTree.PurTree.distance;

import common.IWritable;
import xjc.data.PTree.PurTree.PurTreeDataSet;

/**
 * @author xiaojun chen
 *
 */
public interface ILevelWeightedDistance extends IWritable {

	public void setData(PurTreeDataSet data);

	public void reset();

	public void setZero();

	public void addLevelSimilarity(int level, double similarity);

	public double getDistance();

	public ILevelWeightedDistance clone();

	public double[] getLevelDistance();

	public void destroy();

}
