/*
 * Mar 17, 2017
 * 	- translated from mmb_network/geom_util.py (class Point)
 */

package geom_util;

public class Point {

	public double x;
	public double y;
	public int cur_edge_id;
	
	//
	public Point(Query query){
		this.x = query.x;
        this.y = query.y;
        this.cur_edge_id = query.cur_edge_id;
	}
}
