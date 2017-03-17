/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class Edge)
 */

package geom_util;

public class Edge {

	public int edge_id;
    public int start_node_id;
    public int end_node_id;
    public int edge_class;
    
    public static double length(Node node_1, Node node_2){
        return Math.sqrt((node_1.x - node_2.x)*(node_1.x - node_2.x) + (node_1.y - node_2.y)*(node_1.y - node_2.y));
    }
}
