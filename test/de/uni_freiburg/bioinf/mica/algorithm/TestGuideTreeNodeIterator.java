package de.uni_freiburg.bioinf.mica.algorithm;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.ExpectedException;

/**
 * 
 */

/**
 * @author Mmann
 *
 */
public class TestGuideTreeNodeIterator {
	
	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	final double precisionDelta = 0.0001;
	
	
	
	@Test
	public final void testIterator() {
		
		GuideTreeGenerator.GuideTreeNode root = new GuideTreeGenerator.GuideTreeNode();
		root.clusterIds.add(1);
		
		GuideTreeGenerator.GuideTreeNode cur = new GuideTreeGenerator.GuideTreeNode();
		cur.clusterIds.add(2);
		root.children.add(cur);
		cur.parent = root;
		
		cur = new GuideTreeGenerator.GuideTreeNode();
		cur.clusterIds.add(3);
		root.children.add(cur);
		cur.parent = root;
		
		cur = new GuideTreeGenerator.GuideTreeNode();
		cur.clusterIds.add(4);
		root.children.add(cur);
		cur.parent = root;
		
		GuideTreeGenerator.GuideTreeNode subroot = cur;
		
		cur = new GuideTreeGenerator.GuideTreeNode();
		cur.clusterIds.add(5);
		subroot.children.add(cur);
		cur.parent = subroot;
		
		cur = new GuideTreeGenerator.GuideTreeNode();
		cur.clusterIds.add(6);
		subroot.children.add(cur);
		cur.parent = subroot;
		
		
		// should be post-order ( 2, 3, (5,6) 4) 1
		
		String postOrder = "";
		for (GuideTreeGenerator.GuideTreeNode curNode : root ) {
			postOrder += curNode.toString();
		}
		Assert.assertTrue(postOrder.equals("{2}{3}{5}{6}{4}{1}"));
		
	}

}
