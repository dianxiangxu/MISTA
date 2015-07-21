/* 	
	Author Dianxiang Xu
*/
package testgeneration;

import java.util.LinkedList;


import kernel.CancellationException;
import kernel.SystemOptions;

import mid.MID;


public class TransitionTreeForDepthCoverageBFS extends TransitionTreeForStructureCoverage {
	private static final long serialVersionUID = 1L;
	
	public TransitionTreeForDepthCoverageBFS(MID mid, SystemOptions systemOptions) {
		super(mid, systemOptions, TransitionTreeForStructureCoverage.SINK_EVENTS_DISABLED);
	}

	public void expand() throws CancellationException {
		int searchDepth = systemOptions.getSearchDepth();
		LinkedList<TransitionTreeNode> queue = new LinkedList<TransitionTreeNode>();
		for (TransitionTreeNode initNode: root.children())
			queue.addLast(initNode);
		while (!queue.isEmpty()) {
			checkForCancellation();
			TransitionTreeNode node = queue.poll();
			if (node.getLevel()<= searchDepth) {
				createChildren(node);
				for (TransitionTreeNode child: node.children())
					if (!child.isNegative())
						queue.addLast(child);
			}
		}
	}
	

}
