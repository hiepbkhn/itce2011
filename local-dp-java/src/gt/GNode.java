/*
 * Sep 6, 2016
 * 	- node used in GraphicalGame.java
 */

package gt;

public class GNode {

	public GNode child;
	public int k;	// number of parents
	public GNode[] parents;
	public int type;	// 0: opposite, 1: match
	
	////
	public GNode(int type, int k){
		this.type = type;
		this.k = k;
	}
	
}
