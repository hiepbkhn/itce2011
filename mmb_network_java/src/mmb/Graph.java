/*
 * Mar 17, 2017
 * 	- translated from mmb_network/graph_naiveclique_network.py (class Graph)
 */

package mmb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import geom_util.EdgeSegment;
import geom_util.EdgeSegmentSet;
import geom_util.Point;
import geom_util.Query;
import map_loader.MMBMap;
import query_loader.QueryLog;
import tuple.PairEdgeSegInt;
import tuple.PairInt;
import tuple.PairIntList;
import tuple.PairSetListInt;

public class Graph {

	public int num_user;
	public MMBMap map_data;
	public QueryLog query_log;
	
	public Map<Integer, Integer> user_mc_set = new HashMap<Integer, Integer>();  //dict of clique_id, e.g. user_mc_set[1] = 2 (clique_id = 2)
    public List<Set<Integer>> mc_set = new ArrayList<Set<Integer>>();        //maximal clique set, list of sets
    public Map<PairInt, Integer> graph_edges;   //dict of pair (u,w) 
    public Map<Integer, Query>last_query;    //dict of (user, last query)
    public Map<Integer, List<EdgeSegment>> user_mesh = new HashMap<Integer, List<EdgeSegment>>();      //last cloaked (published) region
    //
    public List<Set<Integer>> positive_mc_set;   //list of sets
    public Map<Integer, Integer> old_user_mc_set;
    public Map<Integer, List<EdgeSegment>> old_user_mesh; 
    //
    public List<Set<Integer>> cover_set;     // list of sets
    public List<List<EdgeSegment>> cover_mesh;   // list of meshes, 
                            // checked agaist Option.S_GLOBAL at the end of solve_new_queries() 
//    public cover_mesh_mmb;
//    //
    public List<Set<Integer>> new_cover_set;     // list of sets
    public List<List<EdgeSegment>> new_cover_mesh;   // list of meshes, 
//    public new_cover_mesh_mmb;
	
	
    //////////////////////////
    public Graph(int num_user, MMBMap map_data, QueryLog query_log){
    	this.num_user = num_user;
    	this.map_data = map_data;
        this.query_log = query_log;
    }
    
    
    //
    public void reset(){
        this.num_user = 0;
        this.user_mc_set = new HashMap<Integer, Integer>();   //dict of sets
        this.mc_set = new ArrayList<Set<Integer>>();        //maximal clique set, list of sets 
        //
        this.old_user_mc_set = this.user_mc_set;
        this.old_user_mesh = this.user_mesh;
        //
        this.user_mesh = new HashMap<Integer, List<EdgeSegment>>();
    }
    
    //
    // copied from EdgeSegmentSet.is_set_cover()  
    public List<Integer> find_partial_coverage(List<PairEdgeSegInt> part_edges, Query query){
        if (part_edges.size() == 0)
            return new ArrayList<Integer>();
        
        //binary search
        int lo = 0;
        int hi = part_edges.size() - 1;
        int mid = (lo + hi) / 2;
        boolean found = false;
        while (true){
            if (part_edges.get(mid).e.cur_edge_id == query.cur_edge_id){
                found = true;
                break;
            }
            if (part_edges.get(mid).e.cur_edge_id > query.cur_edge_id){
                hi = mid - 1;
                if (hi < lo)
                    break;
            }
            else{
                lo = mid + 1;
                if (lo > hi)
                    break;
            }
            mid = (lo + hi) / 2;    
        }
        
        if (found == false)
            return new ArrayList<Integer>();
        
        //
        lo = mid;
        while (lo-1 > 0 && part_edges.get(lo-1).e.cur_edge_id == query.cur_edge_id)
            lo = lo - 1;
        hi = mid;
        while (hi+1 < part_edges.size() && part_edges.get(hi+1).e.cur_edge_id == query.cur_edge_id)
            hi = hi + 1;
        
        List<Integer> result = new ArrayList<Integer>();
        for (int i = lo; i < hi+1; i++){
        	PairEdgeSegInt pair = part_edges.get(i);
            if (EdgeSegment.is_line_cover(new Point(query), pair.e) == true)
                result.add(pair.obj_id);
        }
        
        
        return result;
    }
     
    //
    public boolean is_reverse_existed(int u, int v, List<PairInt> directed_edges){     //check (v,u) in directed_edges
        //binary search
        int lo = 0;
        int hi = directed_edges.size() - 1;
        int mid = (lo + hi) / 2;
        boolean found = false;
        while (true){
            if (directed_edges.get(mid).x == v){
                found = true;
                break;
            }
            if (directed_edges.get(mid).x > v){
                hi = mid - 1;
                if (hi < lo) 
                    break;
            }
            else{
                lo = mid + 1;
                if (lo > hi)
                    break;
            }
            mid = (lo + hi) / 2;    
        }
        
        if (found == false)
            return false;
        
        //
        lo = mid;
        while (lo-1 > 0 && directed_edges.get(lo-1).x == v)
            lo = lo - 1;
        hi = mid;
        while (hi+1 < directed_edges.size() && directed_edges.get(hi+1).x == v)
            hi = hi + 1;
        
    	for (int i = lo; i < hi+1; i++){
        	PairInt pair = directed_edges.get(i);	
            if (pair.y == u) 
				return true;
    	}
        return false;
    }
    
    // build the Constraint Graph
    public PairIntList compute_edge_list(Map<Integer, List<EdgeSegment>> expanding_list, List<Query> query_list){
        int num_edges = 0;
        List<PairInt> list_edges = new ArrayList<PairInt>();
        
        List<PairInt> directed_edges = new ArrayList<PairInt>(); //(u,v) in directed_edges: mesh(v) contains u
        
        List<PairEdgeSegInt> part_edges = new ArrayList<PairEdgeSegInt>(); // list of partial edges (edge segment)
        Map<Integer, List<Integer>> full_edge_meshes = new HashMap<Integer, List<Integer>>();   // full_edge_meshes[e] = list of meshes containing e
        
        //1.
        for (Entry<Integer, List<EdgeSegment>> entry : expanding_list.entrySet()){
        	int obj_id = entry.getKey();
        	List<EdgeSegment> mesh = entry.getValue();
        	for (EdgeSegment e : mesh){
                if (this.map_data.is_full_edge(e) == true){   // FULL edge
                    if (! full_edge_meshes.containsKey(e.cur_edge_id))
                        full_edge_meshes.put(e.cur_edge_id, new ArrayList<Integer>());
                    full_edge_meshes.get(e.cur_edge_id).add(obj_id);
                }        
                else                                       // PARTIAL edge
                    part_edges.add(new PairEdgeSegInt(e, obj_id));          // "list" of only one point mesh(obj_id) covers e
        	}
        }
        
        //sort part_edges by e.cur_edge_id
        Collections.sort(part_edges);
                    
        //2.
        for (Query query : query_list){
            int u = query.obj_id;
            //
            if (full_edge_meshes.containsKey(query.cur_edge_id)){
                List<Integer> list_full = full_edge_meshes.get(query.cur_edge_id);
                for (int v : list_full)
                    directed_edges.add(new PairInt(u, v));    //NOTE: maybe v = u
            }                
            //
            List<Integer> list_part = this.find_partial_coverage(part_edges, query);		// sorted_part_edges -> part_edges
            for (int v : list_part)
                directed_edges.add(new PairInt(u, v));    //NOTE: maybe v = u    
        }
            
        System.out.println("directed_edges.len =" + directed_edges.size());                
        //sort
        Collections.sort(directed_edges);
        
        //3. find undirected edge: both (u,v) and (v,u) from directed_edges
        for (PairInt pair : directed_edges)
            if (pair.x < pair.y){
                int u = pair.x;
                int v = pair.y;
                if (this.is_reverse_existed(u, v, directed_edges)){
                    num_edges += 1;
                    list_edges.add(new PairInt(u,v));
                }
            }
                    
        return new PairIntList(num_edges, list_edges);    
    }
    
    //
    public void write_list_edges(List<PairInt> list_edges) throws IOException{
        //
        int max_edge_id = -1;
        for (PairInt e : list_edges){
        	if (max_edge_id < e.x)
        		max_edge_id = e.x;
        	if (max_edge_id < e.y)
        		max_edge_id = e.y;
        }
        
        List<List<Integer>> list_adj = new ArrayList<List<Integer>>();
        for (int i = 0; i < max_edge_id+1; i++)   //list of lists
        	list_adj.add(new ArrayList<Integer>());
        	
        for (PairInt e : list_edges)
            list_adj.get(e.x).add(e.y);
         
        //    
        BufferedWriter f = new BufferedWriter(new FileWriter(Option.MAXIMAL_CLIQUE_FILE_IN));
        for (int u = 0; u < max_edge_id+1; u++){
            if (list_adj.get(u).size() > 0){
                for (int v : list_adj.get(u))
                    f.write(v + ",");
            }
            f.write("\n");    
        }
        
        f.close();
    }
    
    //
    public void find_cloaking_sets(int timestamp, Map<Integer, List<EdgeSegment>> expanding_list){
        
        //1. find positive and negative cliques
        this.positive_mc_set = new ArrayList<Set<Integer>>();
        List<Set<Integer>> negative_mc_set = new ArrayList<Set<Integer>>();
        for (Set<Integer> clique : this.mc_set){
            if (clique.size() == 1)
                continue;
            //
            double max_min_length = 0;
            int max_k_anom = 0;
            List<Query> query_list = new ArrayList<Query>();
            for (int obj_id : clique){
                Query query = this.query_log.trajs.get(obj_id).get(timestamp);
                query_list.add(query);
                if (max_min_length < query.min_length)
                    max_min_length = query.min_length;
                if (max_k_anom < query.k_anom)
                    max_k_anom = query.k_anom;
            }
            //compute length of mesh
            List<EdgeSegment> mesh = new ArrayList<EdgeSegment>();
            for (int obj_id : clique){
//                mesh = EdgeSegmentSet.union(mesh, expanding_list[obj_id])
                // NEW (trial)
                mesh.addAll(expanding_list.get(obj_id));
            }
            // NEW (trial)
            mesh = EdgeSegmentSet.clean_fixed_expanding(mesh);    

            double clique_len = EdgeSegmentSet.length(mesh);
            
            //
            if (clique.size() >= max_k_anom &&
                    clique_len >= max_min_length * this.map_data.total_map_len)
                this.positive_mc_set.add(clique); 
            else if (clique.size() > 2)
                negative_mc_set.add(clique);
        }
        
        //2.convert negative cliques (heuristically)
        List<Set<Integer>> new_negative_mc_set = new ArrayList<Set<Integer>>();
        
        for (Set<Integer> clique : negative_mc_set){
            List<Query >query_list = new ArrayList<Query>(); 
            for (int obj_id : clique){
                Query query = this.query_log.trajs.get(obj_id).get(timestamp);
                query_list.add(query);
            }
            //sort
            Collections.sort(query_list);	// sort by .k_anom  
                
            while (true){
                query_list.remove(query_list.size() - 1);    //remove the last
                if (query_list.size() == 0)
                    break;
                double max_min_length = 0;
                for (Query query : query_list)
                	if (max_min_length < query.min_length)
                		max_min_length = query.min_length;
                //compute length of mesh
                List<EdgeSegment> mesh = new ArrayList<EdgeSegment>();
                for (Query query : query_list){
//                    mesh = EdgeSegmentSet.union(mesh, expanding_list[query.obj_id])
                    // NEW (trial)
                    mesh.addAll(expanding_list.get(query.obj_id));
                }
                // NEW (trial)
                mesh = EdgeSegmentSet.clean_fixed_expanding(mesh);    
                    
                    
                double clique_len = EdgeSegmentSet.length(mesh);
                
                //
                if (query_list.size() >= query_list.get(query_list.size()-1).k_anom &&
                        clique_len >= max_min_length * this.map_data.total_map_len)
                    break;
            }
            
            //
            if (query_list.size() > 1){
            	Set<Integer> set = new HashSet<Integer>();
            	for (Query query : query_list)
            		set.add(query.obj_id);
                new_negative_mc_set.add(set);
            }
        }        
        //3.
//        print "positive_mc_set =", this.positive_mc_set
//        print "new_negative_mc_set =", new_negative_mc_set 
        
        this.positive_mc_set.addAll(new_negative_mc_set);
    }
    
    //
    public void solve_new_queries(int timestamp) throws Exception{
        
        Map<Integer, List<EdgeSegment>> expanding_list = new HashMap<Integer, List<EdgeSegment>>();       //dict of lists
        List<Query> query_list = this.query_log.frames.get(timestamp);       // timestamp
        
        
        //0. reset
        this.reset();
        
        //1. compute expanding_list
        long start_time = System.currentTimeMillis(); 
        for (Query query : query_list){
        	List<EdgeSegment> seg_list = this.map_data.compute_fixed_expanding(query.x, query.y, 
                                            query.cur_edge_id, query.dist);  //old: Option.DISTANCE_CONSTRAINT
            seg_list = EdgeSegmentSet.clean_fixed_expanding(seg_list);
            
            expanding_list.put(query.obj_id, seg_list);
        }
        System.out.println("expanding_list - elapsed : " + (System.currentTimeMillis() - start_time));       
        
        //2. compute mc_set
//        start_time = System.currentTimeMillis();    
//        num_edges = 0 
//        list_edges = []
//        for i in range(len(query_list)):
//            for j in range(i+1,len(query_list)):
//                if get_distance(query_list[i].x, query_list[i].y, query_list[j].x, query_list[j].y) > \
//                    Option.INIT_GRAPH_DISTANCE:
//                    continue
//                
//                if EdgeSegmentSet.is_set_cover(Point(query_list[i]), expanding_list[query_list[j].obj_id]) and \
//                    EdgeSegmentSet.is_set_cover(Point(query_list[j]), expanding_list[query_list[i].obj_id]):
//                    num_edges += 1
//                    list_edges.append((query_list[i].obj_id, query_list[j].obj_id))
//                    
//        print "num_edges=", num_edges
//        print "list_edges OLD - elapsed : ", (time.clock() - start_time)  
        
        start_time = System.currentTimeMillis(); 
        PairIntList temp = this.compute_edge_list(expanding_list, query_list);
        int num_edges = temp.num_edges;
        List<PairInt> list_edges = temp.list_edges;
        
        System.out.println("num_edges= " + num_edges);
        System.out.println("list_edges NEW - elapsed : " + (System.currentTimeMillis() - start_time));       
        
        // write list_edges[] to file
        this.write_list_edges(list_edges);
        //
    
        start_time = System.currentTimeMillis();        
//        // (OLD)    
//        graph.add_to_mc_set(list_edges)
         
        // (NEW)
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(Option.MACE_EXECUTABLE + " M " + Option.MAXIMAL_CLIQUE_FILE_IN + " " + Option.MAXIMAL_CLIQUE_FILE_OUT);
        p.waitFor();
//        BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
        
        BufferedReader f = new BufferedReader(new FileReader(Option.MAXIMAL_CLIQUE_FILE_OUT));
        List<String> fstr = new ArrayList<String>();
        while (true){
        	String str = f.readLine();
        	if (str == null)
        		break;
        	fstr.add(str);
        }
        f.close();
        
        for (String line : fstr){
            String[] node_list = line.split(" ");
            if (node_list.length < 2)
                continue;
            Set<Integer> set = new HashSet<Integer>();
            for (String node : node_list)
            	set.add(Integer.parseInt(node));
            this.mc_set.add(set);
        }
            
        System.out.println(this.mc_set.size()); 
        
        System.out.println("add_to_mc_set - elapsed : " + (System.currentTimeMillis() - start_time)); 
//        print "mc_set =", this.mc_set
        
        //3.
        start_time = System.currentTimeMillis(); 
        this.find_cloaking_sets(timestamp, expanding_list);
        
        System.out.println("find_cloaking_sets - elapsed : " + (System.currentTimeMillis() - start_time)); 
        
        
        //4. 'Set Cover Problem' (from weighted_set_cover.py)
        start_time = System.currentTimeMillis(); 
        
        int num_element = -1;
        for (Query query: query_list) 
        	if (num_element < query.obj_id)
        		num_element = query.obj_id;
        num_element += 1;  // avoid out of range
        
        int num_cloaked_users = 0;
        if (timestamp == 0){
        	PairSetListInt _temp = WeightedSetCover.find_init_cover(this.positive_mc_set, num_element);
        	
            this.cover_set = _temp.set_list;
            num_cloaked_users = _temp.i;
            
            this.new_cover_set = this.cover_set;     // for compute CLOAKING MESH
        }
        else{
            PairSetListInt _temp = WeightedSetCover.find_next_cover(this.positive_mc_set, num_element, this.cover_set, Option.K_GLOBAL);
            this.new_cover_set = _temp.set_list;
            num_cloaked_users = _temp.i; 
        }
        
        System.out.println("Success rate =" + ((double)num_cloaked_users)/query_list.size());    
        System.out.println("compute cover_set - elapsed : " + (System.currentTimeMillis() - start_time)); 
        
        
        //5. compute CLOAKING MESH
        start_time = System.currentTimeMillis();   
        
        double total_mesh_length = 0;
        int total_query = 0;
        this.new_cover_mesh = new ArrayList<List<EdgeSegment>>();    // NEW
        for (int clique_id = 0; clique_id <this.new_cover_set.size(); clique_id++){
            Set<Integer> clique = this.new_cover_set.get(clique_id);
            //compute length of mesh
            List<EdgeSegment> mesh = new ArrayList<EdgeSegment>();
            for (int obj_id : clique)
                mesh = EdgeSegmentSet.union(mesh, expanding_list.get(obj_id));
            
            this.new_cover_mesh.add(mesh);    //NEW           
            
            total_mesh_length += EdgeSegmentSet.length(mesh);
            total_query += clique.size();    
        }
        
        double average_mesh_query = total_mesh_length/total_query;
        
        System.out.println("total_mesh_length =" + total_mesh_length);    
        System.out.println("average_mesh_query =" + average_mesh_query); 
        System.out.println("Compute CLOAKING MBR - elapsed : " + (System.currentTimeMillis() - start_time)); 
//        print "user_mesh = ", this.user_mesh
                
                
        //5.2 Check MMB/MAB
//        this.new_cover_mesh_mmb = this.compute_cover_mesh_mmb(this.new_cover_mesh, expanding_list)
//            
//        if timestamp > 0:
//            start_time = System.currentTimeMillis(); 
//            
//            this.check_MMB_MAB(checking_pairs, this.cover_mesh, this.cover_mesh_mmb, this.new_cover_mesh, this.new_cover_mesh_mmb)
//            
//            print "check_MMB_MAB() - elapsed : ", (time.clock() - start_time)
        
        // UPDATE
        this.cover_set = this.new_cover_set;
        this.cover_mesh = this.new_cover_mesh;
//        this.cover_mesh_mmb = this.new_cover_mesh_mmb        
        
        
        //6. compute user_mc_set (max clique for each obj_id), replace this.positive_mc_set by this.cover_set
        start_time = System.currentTimeMillis();   
        this.user_mc_set = new HashMap<Integer, Integer>();
        for (int clique_id = 0; clique_id < this.cover_set.size(); clique_id++){
        	Set<Integer> clique = this.cover_set.get(clique_id);
            
            for (int obj_id : clique)
                //
                if (! this.user_mc_set.containsKey(obj_id))
                    this.user_mc_set.put(obj_id, clique_id);            //use id
                else if (this.cover_set.get(this.user_mc_set.get(obj_id)).size() < clique.size())
                    this.user_mc_set.put(obj_id, clique_id);               //store the maximum
            //
            for (int obj_id : clique)
                if (this.user_mc_set.get(obj_id) == clique_id)  //clique id comparison
                    this.user_mesh.put(obj_id, this.cover_mesh.get(clique_id));        
        }
        System.out.println("Compute user_mc_set - elapsed : " + (System.currentTimeMillis() - start_time)); 
//        print "user_mc_set = ", this.user_mc_set
        
                
        //7. publish MBRs (write to file)   
        start_time = System.currentTimeMillis();   
        this.write_results_to_files(timestamp);
        
        System.out.println("write_results_to_files - elapsed : " + (System.currentTimeMillis() - start_time)); 
    }
    
    //
    public void write_results_to_files(int timestamp) throws IOException{
        
        String config_name = Option.QUERY_FILE.substring(Option.QUERY_FILE.length()-4) + "-" + 
        			Option.DISTANCE_CONSTRAINT + "-" + Option.MAX_SPEED;
        
//        //0. this.positive_mc_set
//        f = open(Option.RESULT_PATH + "/" + config_name + "_positive_mc_set" + "_" + str(timestamp) + ".out", "w")
//        for clique in this.positive_mc_set:
//            for obj_id in clique:
//                f.write("%d,"%obj_id)
//            f.write("\n")
//        f.close()    
//        
        //1. this.cover_set
        BufferedWriter f = new BufferedWriter(new FileWriter(Option.RESULT_PATH + config_name + "_" + Option.K_GLOBAL + "_" +
                  Option.INIT_COVER_KEEP_RATIO + "_cover_set" + "_" + timestamp + ".out"));
        for (Set<Integer> clique : this.cover_set){
            for (int obj_id : clique)
                f.write(obj_id + ",");
            f.write("---");
            for (int obj_id : clique)
                f.write(this.query_log.trajs.get(obj_id).get(timestamp).k_anom + ",");    
            f.write("\n");
        }
        f.close();    
            
        //2. this.user_mc_set
//        f = open(Option.RESULT_PATH + "/" + config_name + "_user_mc_set" + "_" + str(timestamp) + ".out", "w")
//        for (obj_id, clique_id) in this.user_mc_set.iteritems():
//            f.write("%d %d\n"%(obj_id,clique_id))
//        f.close()     
        
        //3. this.user_mesh
//        f = open(Option.RESULT_PATH + "/" + config_name + "_user_mesh" + "_" + str(timestamp) + ".out", "w")
//        for (obj_id, mesh) in this.user_mesh.iteritems():
//            f.write("%d\n"%obj_id)
//            for seg in mesh:
//                f.write("%.2f,%.2f,%.2f,%.2f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))
//            f.write("\n")            
//        f.close()     

        //4. this.cover_mesh
//        f = open(Option.RESULT_PATH + "/" + config_name + "_cover_mesh" + "_" + str(timestamp) + ".out", "w")
//        for mesh in this.cover_mesh:
//            mbr = EdgeSegmentSet.compute_mbr(mesh)
//            f.write("%.2f,%.2f,%.2f,%.2f,%.2f\n"%(mbr.area, mbr.min_x, mbr.min_y, mbr.max_x, mbr.max_y))
//            for seg in mesh:
//                f.write("%.2f,%.2f,%.2f,%.2f,%d:"%(seg.start_x, seg.start_y, seg.end_x, seg.end_y, seg.cur_edge_id))
//            f.write("\n")            
//        f.close()         
        
        //5. this.user_mesh (only print edge_id) for attacks (in trace_generator)
        f = new BufferedWriter(new FileWriter(Option.RESULT_PATH + "/" + config_name + "_edge_cloaking_" + timestamp + ".out"));
        for (Entry<Integer, List<EdgeSegment>> entry : this.user_mesh.entrySet()){
        	int obj_id = entry.getKey(); 
        	List<EdgeSegment> mesh = entry.getValue();
        	
            int cur_edge_id = this.query_log.trajs.get(obj_id).get(timestamp).cur_edge_id;
            f.write(obj_id + "-" + cur_edge_id + "\n");
            for (EdgeSegment seg : mesh)
                f.write(seg.cur_edge_id + ",");
            f.write("\n");
        }
        
        f.close();
    }
    
    //
    public void run_timestamps(int start_timestamp, int end_timestamp) throws Exception{
	    for (int timestamp = start_timestamp; timestamp < end_timestamp+1; timestamp++){
	        long start_time = System.currentTimeMillis();
	        System.out.println("--------->>");
	    	System.out.println("TIMESTAMP : " + timestamp);
	    	System.out.println("self.num_user = " + this.query_log.frames.get(timestamp).size());
	        
	        solve_new_queries(timestamp);
	        
	        System.out.println("Total time elapsed :" + (System.currentTimeMillis() - start_time));
	//            print "check_published_mcset_mbr", \
	//                this.check_published_mcset_mesh(this.user_mc_set, this.user_mesh, timestamp)
	            
	            //print "len(graph.mc_set) = ", len(graph.mc_set)
	        //print graph.mc_set
	    }
    }
    
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{

		// COMMAND-LINE <query_file> <timestep> <distance_constraint> <k_global><INIT_COVER_KEEP_RATIO><NEXT_COVER_KEEP_RATIO>
		int timestep = 3;
		//    timestep = 40       // for lbs_attack
		
		if(args.length >= 3){
	        Option.QUERY_FILE = args[0];
	        timestep = Integer.parseInt(args[1]);
	        Option.DISTANCE_CONSTRAINT = Integer.parseInt(args[2]);
		}
		
	    if (args.length >= 4)
	        Option.K_GLOBAL = Integer.parseInt(args[3]);    
	        
	    if (args.length >= 5)
	        Option.INIT_COVER_KEEP_RATIO = Double.parseDouble(args[4]);     
	    
	    if (args.length >= 6)
	        Option.NEXT_COVER_KEEP_RATIO = Double.parseDouble(args[5]);     

	    //
	    System.out.println("MAP_FILE = " + Option.MAP_NAME);
		System.out.println("timestep = " + timestep);
		System.out.println("QUERY_FILE = " + Option.QUERY_FILE);
		System.out.println("DISTANCE_CONSTRAINT = " + Option.DISTANCE_CONSTRAINT);
	    
	    //    
	    long start_time = System.currentTimeMillis();
	        
	    MMBMap map_data = new MMBMap();
	    map_data.read_map(Option.MAP_PATH, Option.MAP_NAME);
	    System.out.println("Load Map : DONE");
	    
	    QueryLog query_log = new QueryLog(map_data);
	    query_log.read_query(Option.QUERY_PATH, Option.QUERY_FILE, timestep, Option.QUERY_TYPE);   // default: max_time_stamp = 10 (40: only for attack) 
	    System.out.println("Load Query : DONE");
	    
	    System.out.println("max_speed = " + query_log.max_speed);
	    System.out.println("elapsed : " + (System.currentTimeMillis() - start_time));  
	    
	        
	    Graph graph = new Graph(0, map_data, query_log);
	    
	    //TEST
	    graph.run_timestamps(0, timestep);
	    
	    System.out.println("graph.run_timestamps - DONE");
		
	}

}
