/*
 * Apr 2
 * 	- add LS, RS for new implementation of Dendrogram.generateSanitizedSample()
 */

package dp.mcmc;

import toools.set.IntSet;

public class Node {
public static final int ROOT_NODE = 100000000;
	
	public int id;
	public Node parent, left, right;
	public double value;
	public double noisy_value;
	public int nL, nR;
	public int nEdge;
	public double noisy_nEdge;
	public int level = -1;
	public int toplevel = 0;	// top-down level for lowestCommonAncestor()
	//
	public IntSet LS, RS;		// set of leaf nodes in left-right (for generateSanitizedSample)
	
	////
	public Node(int id, Node parent, double value){
		this.id = id;
		this.parent = parent;
		this.value = value;
		this.nL = 0;
		this.nR = 0;
		this.nEdge = 0;
	}
	////
	public Node copy(){
		Node aNode = new Node(0,null,0.0);
		        
        // do not copy parent, left, right
        aNode.id = this.id;
        aNode.value = this.value;
        aNode.noisy_value = this.noisy_value;
        aNode.nL = this.nL;
        aNode.nR = this.nR;
        aNode.nEdge = this.nEdge;
        aNode.noisy_nEdge = this.noisy_nEdge;
        if (aNode.id >= 0){   // leaf nodes
            aNode.left = null;
            aNode.right = null;
            
        }
        //
        return aNode;
	}
}
