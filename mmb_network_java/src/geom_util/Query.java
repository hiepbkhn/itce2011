/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class Query)
 */

package geom_util;

public class Query {
	public int obj_id;
    public double x;
    public double y;
    public int timestamp;
    public double next_node_x;
    public double next_node_y;
    public int next_node_id;
    public int cur_edge_id;
    //
    public int k_anom;
    public double min_length;
    public double dist;    // distance constraint
    
    //
	public Query(int obj_id, double x, double y, int timestamp, double next_node_x, double next_node_y,
			int next_node_id, int cur_edge_id, int k_anom, double min_length, double dist) {
		super();
		this.obj_id = obj_id;
		this.x = x;
		this.y = y;
		this.timestamp = timestamp;
		this.next_node_x = next_node_x;
		this.next_node_y = next_node_y;
		this.next_node_id = next_node_id;
		this.cur_edge_id = cur_edge_id;
		this.k_anom = k_anom;
		this.min_length = min_length;
		this.dist = dist;
	}
    
    
}
