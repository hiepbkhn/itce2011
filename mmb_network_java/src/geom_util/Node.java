/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class Node)
 */

package geom_util;

public class Node {
	public int node_id;
    public int x;
    public int y;
    
    //
    public Node(){
    	
    }
    
    //
	public Node(int node_id, int x, int y) {
		super();
		this.node_id = node_id;
		this.x = x;
		this.y = y;
	}
	
}
