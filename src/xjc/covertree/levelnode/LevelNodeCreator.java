/**
 * 
 */
package xjc.covertree.levelnode;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author xiaojun chen
 *
 */
public class LevelNodeCreator implements ILevelNodeCreator {

	public LevelNodeCreator() {
	}

	@Override
	public LevelNode createRootNode(int instance) {
		return new LevelNode(instance,500);
	}
	
	@Override
	public LevelNode createRootNode(int instance,int level) {
		return new LevelNode(instance,level);
	}

/*	@Override
	public LevelNode createChildNode(ILevelNode parent, int instance) {
		LevelNode child = new LevelNode(instance);
		parent.addChild(child);
		return child;
	}*/
	
	@Override
	public LevelNode createChildNode(ILevelNode parent, int instance,int level) {
		LevelNode child = new LevelNode(instance,level);
		parent.addChild(child);
		return child;
	}

/*	@Override
	public ILevelNode createParentNode(ILevelNode child) {

		LevelNode parent = new LevelNode(child.getInstance());
		parent.addChild(child);
		return parent;

	}*/
	
	@Override
	public ILevelNode createParentNode(ILevelNode child,int level) {

		LevelNode parent = new LevelNode(child.getInstance(),level);
		parent.addChild(child);
		return parent;

	}

	@Override
	public ILevelNode read(DataInput in) throws IOException {
		LevelNode node = new LevelNode();
		node.readFields(in);
		return node;
	}

	@Override
	public ILevelNode copyCreateNode(ILevelNode node, boolean recursive) {
		return new LevelNode(node, recursive);
	}
}
