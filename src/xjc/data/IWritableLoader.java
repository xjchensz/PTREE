/**
 * 
 */
package xjc.data;

import common.ILoader;
import common.IWritable;

/**
 * @author xiaojun chen
 *
 */
public interface IWritableLoader<T extends IWritable> extends ILoader<T>,
		IWritable {
}
