/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.Stack;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;


public class TransitionTreeForDepthCoverageDFS extends TransitionTreeForStructureCoverage {
	private static final long serialVersionUID = 1L;
	
	public TransitionTreeForDepthCoverageDFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions, TransitionTreeForStructureCoverage.SINK_EVENTS_DISABLED);
	}

	protected void expand() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		Stack<TransitionTreeNode> stack = new Stack<TransitionTreeNode>();
		for (int i=root.children().size()-1; i>=0; i--)
			stack.push(root.children().get(i));
		while (!stack.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = stack.pop();
			if (node.getLevel()<= searchDepth) {
				createChildren(node);
				for (int i=node.children().size()-1; i>=0; i--){
					TransitionTreeNode child = node.children().get(i);
					if (!child.isNegative())
						stack.push(child);
				}
			}
		}
	}
	
}
