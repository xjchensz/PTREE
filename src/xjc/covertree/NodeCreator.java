/**
 * 
 */
package xjc.covertree;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author xiaojun chen
 *
 */
public class NodeCreator implements INodeCreator {

	public NodeCreator() {
	}

	@Override
	public Node createRootNode(int instance) {
		return new Node(instance);
	}

	@Override
	public Node createChildNode(INode parent, int instance) {
		Node child = new Node(instance);
		parent.addChild(child);
		return child;
	}

	@Override
	public INode createParentNode(INode child) {

		Node parent = new Node(child.getInstance());
		parent.addChild(child);
		return parent;

	}

	@Override
	public INode read(DataInput in) throws IOException {
		Node node = new Node();
		node.readFields(in);
		return node;
	}

	@Override
	public INode copyCreateNode(INode node, boolean recursive) {
		return new Node(node, recursive);
	}
}
