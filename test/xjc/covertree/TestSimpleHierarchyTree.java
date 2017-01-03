/**
 * 
 */
package xjc.covertree;

import junit.framework.TestCase;
import xjc.data.HierarchicalTree;

/**
 * @author xiaojun chen
 *
 */
public class TestSimpleHierarchyTree extends TestCase {
	public void test() {
		HierarchicalTree ht = new HierarchicalTree(4);
		ht.insert(2, "0", "0", "0", "0");
		ht.insert(3, "0", "0", "0", "1");
		ht.insert(4, "0", "0", "1", "2");
		ht.insert(5, "0", "0", "1", "3");
		ht.insert(6, "0", "1", "0", "0");
		ht.insert(7, "0", "1", "0", "1");
		ht.insert(8, "0", "1", "1", "2");
		ht.insert(9, "0", "1", "1", "3");
		ht.insert(10, "1", "0", "0", "0");
		ht.insert(11, "1", "0", "0", "1");
		ht.insert(12, "1", "0", "1", "2");
		ht.insert(-2, "1", "0", "1", "3");
		ht.insert(-10, "1", "1", "0", "0");
		ht.insert(3, "1", "1", "0", "1");
		ht.insert(6, "1", "1", "1", "2");
		ht.insert(9, "1", "1", "1", "3");
		ht.update();
		assertEquals(14, ht.getValue("0", "0"), 0);
		assertEquals(5, ht.getValue("0", "0", "0"), 0);
		assertEquals(30, ht.getValue("0", "1"), 0);
		assertEquals(44, ht.getValue("0"), 0);
	}
}
