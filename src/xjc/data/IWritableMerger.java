/**
 * 
 */
package xjc.data;

import common.IWritable;
import common.utils.collection.IMerger;

/**
 * @author xiaojun chen
 *
 */
public interface IWritableMerger<T> extends IMerger<T>, IWritable {
	public boolean equals(IWritableMerger merger);
}
