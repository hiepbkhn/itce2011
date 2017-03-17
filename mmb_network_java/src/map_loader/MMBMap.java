/*
 * Mar 17, 2017
 * 	- translated from mmb_network/map_loader.py (class Stack)
 */

package map_loader;

public class Map {

	public  nodes = {}
    self.edges = {}
    self.adj = {}           //adjacent lists
    self.node_to_edges = {}
    self.xy_to_node_id = {}
    self.node_pair_to_edge = {}
    #
    self.interval_tree_x = None
    self.interval_tree_y = None
    
    self.min_x = 0
    self.min_y = 0
    self.max_x = 0
    self.max_y = 0
    self.dx = 0
    self.dy = 0
    self.area = 0   # = dx*dy
    self.total_map_len = 0
}
