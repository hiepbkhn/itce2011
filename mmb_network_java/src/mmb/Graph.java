/*
 * Mar 17, 2017
 * 	- translated from mmb_network/graph_naiveclique_network.py (class Graph)
 */

package mmb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geom_util.EdgeSegment;
import geom_util.Query;
import map_loader.MMBMap;
import map_loader.PairInt;
import query_loader.QueryLog;

public class Graph {

	public int num_user;
	public MMBMap map_data;
	public QueryLog query_log;
	
	public Map<Integer, Integer> user_mc_set = new HashMap<Integer, Integer>();  //dict of clique_id, e.g. user_mc_set[1] = 2 (clique_id = 2)
    public List<List<Integer>> mc_set;        //maximal clique set, list of sets
    public Map<PairInt, Integer> graph_edges;   //dict of pair (u,w) 
    public Map<Integer, Query>last_query;    //dict of (user, last query)
    public Map<Integer, List<EdgeSegment>> user_mesh;      //last cloaked (published) region
    //
    public List<List<Integer>> positive_mc_set;   //list of sets
    public List<List<Integer>> old_user_mc_set;
    public Map<Integer, List<EdgeSegment>> old_user_mesh; 
    //
    public List<List<Integer>> cover_set;     // list of sets
    public List<List<EdgeSegment>> cover_mesh;   // list of meshes, 
                            // checked agaist option.S_GLOBAL at the end of solve_new_queries() 
    public cover_mesh_mmb = []
    //
    public new_cover_set = []     // list of sets
    public new_cover_mesh = []    // list of meshes, 
    public new_cover_mesh_mmb = []
	
	
	//////////
	public static void main(String[] args) {

	}

}
