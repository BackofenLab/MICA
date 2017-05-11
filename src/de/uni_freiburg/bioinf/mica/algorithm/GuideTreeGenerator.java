package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.exception.NullArgumentException;


/**
 * Defines the interface to be implemented by a guide tree
 * generator for the progressive alignment.
 *
 */
public interface GuideTreeGenerator {

	
	/**
	 * Data structure that represents a node within the guide tree of the
	 * progressive alignment.
	 *
	 */
	public class GuideTreeNode implements Iterable<GuideTreeNode> {
		/**
		 * the curve ids of the cluster represented by this node
		 */
		public List<Integer> clusterIds = new ArrayList<Integer>();
		/**
		 * the link to the parent node or null if this is the root node
		 */
		public GuideTreeNode parent = null;
		/**
		 * list of children nodes, which is be empty if this is a leaf node 
		 */
		public List<GuideTreeNode> children = new ArrayList<GuideTreeNode>(2);
		

		/**
		 * Returns a post-order iterator for the tree rooted by this node
		 * @return the post-order iterator
		 */
		@Override
		public Iterator<GuideTreeNode> iterator() {
			return new PostOrderIterator(this);
		}
		
		@Override
		public String toString() {
			return ArrayUtils.toString(clusterIds.toArray());
		}
	
		/**
		 * A post-order iterator for the subtree rooted by a given node.
		 * 
		 * @author Mmann
		 *
		 */
		protected class PostOrderIterator implements Iterator<GuideTreeNode> {

			/**
			 * The post-ordered list of nodes from the tree to iterate
			 */
			List<GuideTreeNode> nodesPostOrdered = new LinkedList<>();
			
			/**
			 * the current position within nodesPostOrdered
			 */
			Iterator<GuideTreeNode> curPosition;
			
			/**
			 * Creates a new post-order iterator for the tree rooted by the given root node
			 * @param root the root node of the tree to iterate
			 * @throws NullArgumentException
			 */
			public PostOrderIterator( GuideTreeNode root ) throws NullArgumentException {
				if (root==null) throw new NullArgumentException();
				
				// temporary list of nodes to add to the ordered list 
				List<GuideTreeNode> toAdd = new LinkedList<>();
				toAdd.add(root);
				while(!toAdd.isEmpty()) {
					GuideTreeNode curNode = toAdd.get(0);
					toAdd.remove(0);
					// add children to todo list
					toAdd.addAll(curNode.children);
					// add this node to post-order list
					GuideTreeNode parent = curNode.parent;
					if (parent == null) {
						// add at the end
						nodesPostOrdered.add(curNode);
					} else {
						// add in front of parent
						nodesPostOrdered.add(nodesPostOrdered.indexOf(parent), curNode);
					}
				}
				// set iterator on list
				curPosition = nodesPostOrdered.iterator();
			}

			@Override
			public boolean hasNext() {
				return curPosition.hasNext();
			}

			@Override
			public GuideTreeNode next() {
				return curPosition.next();
			}
			
		}

	}

	
	/**
	 * Computes the guide tree for the given distance matrix
	 * @param distMatrix the distance matrix to be used for the guide tree computation
	 * @return the guide tree for the progressive alignment
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the matrix is not valid
	 */
	public GuideTreeNode compute( PICA.PicaData[][] distMatrix ) throws NullArgumentException, IllegalArgumentException;
	
	
}
