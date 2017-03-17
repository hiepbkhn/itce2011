/*
 * Mar 17, 2017
 * 	- translated from mmb_network/map_loader.py
 */

package map_loader;

public class SegItem {
	public double x;
	public double y;
	public double end_x;
	public double end_y;
	public double length;
	public int cur_edge_id;	// = -1 if is_node = true
	public boolean is_node;
	
	//
	public SegItem(double x, double y, double end_x, double end_y, double length, int cur_edge_id, boolean is_node) {
		super();
		this.x = x;
		this.y = y;
		this.end_x = end_x;
		this.end_y = end_y;
		this.length = length;
		this.cur_edge_id = cur_edge_id;	
		this.is_node = is_node;
	}
	
	
}


