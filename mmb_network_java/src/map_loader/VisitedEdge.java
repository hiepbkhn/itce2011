/*
 * Mar 17, 2017
 * 	- translated from mmb_network/map_loader.py
 */

package map_loader;

public class VisitedEdge {
	public double start_x;
	public double start_y;
	public int cur_edge_id;
	
	//
	public VisitedEdge(int cur_edge_id, double start_x, double start_y) {
		super();
		this.start_x = start_x;
		this.start_y = start_y;
		this.cur_edge_id = cur_edge_id;
	}
	
	
	
}
