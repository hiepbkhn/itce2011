/*
 * Mar 20, 2017
 * 	- translated from trace_generator/lbs_synthetic.py 
 */

package trace;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import geom_util.Edge;
import geom_util.GeomUtil;
import geom_util.Node;
import graph.EdgeWeightedGraph;
import graph.WeightedEdge;
import map_loader.MMBMap;
import map_loader.PairInt;
import mmb.Option;
import tuple.PairDouble;
import tuple.TupleTrace;

public class TraceGenerator {
	
	//
	// return a list of random traces based on trans_prob, access_prob (NODE-BASED)    
	public static List<Trace> generate_random_traces(MMBMap map_data, Map<Integer, Map<Integer, Double>> trans_prob, 
			Map<Integer, Double> access_prob, int n_random_traces, double deviation_prob){
	    
		List<Trace> random_traces = new ArrayList<Trace>();
	    
	    // adjacent sets
	    Map<Integer, Set<Integer>> adj_trans_sets = new HashMap<Integer, Set<Integer>>();   // dict of sets
	    for (Entry<Integer, Map<Integer, Double>> entry : trans_prob.entrySet()){
	    	int node1 = entry.getKey();
	    	Map<Integer, Double> adj_dict = entry.getValue();

	    	adj_trans_sets.put(node1, adj_dict.keySet());
	    }
	        
	    
	    //  
	    for (int i = 0; i < n_random_traces; i++){
	        Trace trace = new Trace();
	        
	        ////// PER NODE
	        // random start node (using access_prob)
	        int start_node_id = 0;
	        Random random = new Random();
	        double rand_val = random.nextDouble();
	        double s = 0;
	        for (Entry<Integer, Double> entry : access_prob.entrySet()){
	        	int node = entry.getKey();
	        	double prob = entry.getValue();
	            s += prob;
	            if (s >= rand_val)
	                start_node_id = node;
	                break;
	        }
	    
	        // generate edges by Markov chain (using trans_prob)
	        trace.node_list.add(start_node_id);
	        int cur_node = start_node_id;
	        int next_node = -1;
	        while (trace.node_list.size() < Option.MAX_TRACE_LEN){
	//            if cur_node not in trans_prob:  // if from this node, cannot go further
	//                break
	            if (!trans_prob.containsKey(cur_node)){
	                // uniformly choose next_node from adjacent nodes
	            	rand_val = random.nextDouble();
	                s = 0.0;
	                int n_adj = map_data.adj.get(cur_node).size();
	                for (int node : map_data.adj.get(cur_node)){
	                    s += 1.0/n_adj;
	                    if (s >= rand_val){
	                        next_node = node;
	                        break;
	                    }
	                }
	            }
	            else{ // cur_node exists in access_prob
	                double deviation_val = random.nextDouble();
	                
	                boolean found = false;
	                if (deviation_val < deviation_prob){  // uniformly choose next_node from adjacent nodes that NOT in trans_prob
	                    Set<Integer> adj_set = new HashSet<Integer>(map_data.adj.get(cur_node));
	                    adj_set.removeAll(adj_trans_sets.get(cur_node));
	                    if (adj_set.size() > 0){
	                        found = true;
	                        rand_val = random.nextDouble();
	                        s = 0.0;
	                        int n_adj = adj_set.size();
	                        for (int node : adj_set){
	                            s += 1.0/n_adj;
	                            if (s >= rand_val){
	                                next_node = node;
	                                break;
	                            }
	                        }
	                    }
	                }
	                
	                // no adjacent node NOT in trans_prob[cur_node]     
	                if (! found){   
	                    // next node (choose from trans_prob)   
	                    next_node = 0;
	                    rand_val = random.nextDouble();
	                    s = 0.0;
	                    for (Entry<Integer, Double> entry : trans_prob.get(cur_node).entrySet()){
	                    	int node = entry.getKey(); 
	                    	double prob = entry.getValue();

	                    	s += prob;
	                        if (s >= rand_val){
	                            next_node = node;
	                            break;
	                        }
	                    }
	                }
	            }
	            
	            //
	            trace.node_list.add(next_node);
	            trace.edge_list.add(map_data.node_pair_to_edge.get(new PairInt(cur_node, next_node)));
	            
	            cur_node = next_node;
	        }
	        
	        //
	        random_traces.add(trace); 
	    }
	         
	    // result
	    return random_traces;
	}
	
	//
	// compute list of exposed events
	public static List<Event> compute_exposed_events(MMBMap map_data, Trace trace, int user_id, double user_speed){
	    
	    // DEBUG
	//    print "user_speed =", user_speed
	//    trace_len = 0.0
	//    for i in range(len(trace.node_list)-1):
	//        trace_len += map_data.edges[map_data.node_pair_to_edge[(trace.node_list[i], trace.node_list[i+1])]].edge_length
	//    print "trace_len =", trace_len
	    
	    //
		List<Event> exposed_events = new ArrayList<Event>();
	    
	    // TODO: choose random start/end locations ON start/end edges
	    Event cur_evt = new Event();
	    cur_evt.user_id = user_id;
	    cur_evt.edge_id = trace.edge_list.get(0);
	//    cur_evt.x = map_data.nodes[trace.node_list[0]].x
	//    cur_evt.y = map_data.nodes[trace.node_list[0]].y
	    
	    Random random = new Random();
	    double ratio = random.nextDouble() * 0.9;	// random.uniform(0.0, 0.9)
	    PairDouble _temp = GeomUtil.get_point_between(map_data.nodes.get(trace.node_list.get(0)).x, map_data.nodes.get(trace.node_list.get(0)).y, 
	                    map_data.nodes.get(trace.node_list.get(1)).x, map_data.nodes.get(trace.node_list.get(1)).y, ratio);
	    cur_evt.x = _temp.x;
	    cur_evt.y = _temp.y;
	    
	    cur_evt.ts = 0;
	    cur_evt.speed = user_speed * Option.SPEED_CLASSES[map_data.edges.get(cur_evt.edge_id).edge_class];
	    
	    Node node = map_data.nodes.get(trace.node_list.get(1));
	    cur_evt.next_x = node.x;
	    cur_evt.next_y = node.y;
	    
	    cur_evt.k_anom = Option.K_MIN + random.nextInt(Option.K_MAX - Option.K_MIN);
	    cur_evt.min_length = Option.MIN_LENGTH_LOW + random.nextDouble() * (Option.MIN_LENGTH_HIGH - Option.MIN_LENGTH_LOW);
	    
	    //
	    int next_node_id = 1;
	    int time_step = 0;
	    exposed_events.add(cur_evt);
	    Event evt;
	    Node prev_node = new Node();
	    
	    while (next_node_id < trace.node_list.size()-1){
	        
	        node = map_data.nodes.get(trace.node_list.get(next_node_id));
	        double remain_len = GeomUtil.get_distance(cur_evt.x, cur_evt.y, node.x, node.y);     // distance from cur_evt.xy to next_node_id
	        
	        Edge cur_edge = map_data.edges.get(cur_evt.edge_id);
	        double cur_speed_class = Option.SPEED_CLASSES[cur_edge.edge_class];
	        
	        if (remain_len >= user_speed*cur_speed_class){  // next event on the current edge
	            evt = new Event();
	            evt.user_id = user_id;
	            evt.edge_id = trace.edge_list.get(next_node_id-1);
	            time_step += 1;
	            evt.ts = time_step;
	            _temp = GeomUtil.get_point_on_line(cur_evt.x, cur_evt.y, node.x, node.y, 
	                                             user_speed*cur_speed_class);
	            evt.x = _temp.x;
	            evt.y = _temp.y; 
	            evt.speed = user_speed * cur_speed_class;
	            evt.next_x = node.x;
	            evt.next_y = node.y;
	            // evt.k_anom, evt.min_length = generate_privacy_params()
	            evt.k_anom = Option.K_MIN + random.nextInt(Option.K_MAX - Option.K_MIN);
	    	    evt.min_length = Option.MIN_LENGTH_LOW + random.nextDouble() * (Option.MIN_LENGTH_HIGH - Option.MIN_LENGTH_LOW);
	        }
	        else{   // move to next edge(s)
	            double remain_time = 1.0;
	            boolean found = false;
	            while (next_node_id < trace.node_list.size()-1){
	                remain_time = remain_time - remain_len/(user_speed*cur_speed_class);
	                // to next node
	                prev_node = map_data.nodes.get(trace.node_list.get(next_node_id));
	                next_node_id += 1;
	                node = map_data.nodes.get(trace.node_list.get(next_node_id));
	                //
	                cur_edge = map_data.edges.get(map_data.node_pair_to_edge.get(new PairInt(prev_node.node_id, node.node_id)));
	                cur_speed_class = Option.SPEED_CLASSES[cur_edge.edge_class];
	                
	                remain_len = GeomUtil.get_distance(prev_node.x, prev_node.y, node.x, node.y);
	                if (remain_len >= remain_time*user_speed*cur_speed_class){
	                    found = true;
	                    break;
	                }
	            }
	            
	            //
	            if (found == true){
	                evt = new Event();
	                evt.user_id = user_id;
	                evt.edge_id = map_data.node_pair_to_edge.get(new PairInt(prev_node.node_id, node.node_id));
	                time_step += 1;
	                evt.ts = time_step;
	                _temp = GeomUtil.get_point_on_line(prev_node.x, prev_node.y, node.x, node.y, 
	                                                 remain_time*user_speed*cur_speed_class);
	                evt.x = _temp.x;
	                evt.y = _temp.y;
	                evt.speed = user_speed*cur_speed_class;
	                evt.next_x = node.x;
	                evt.next_y = node.y;
	                evt.k_anom = Option.K_MIN + random.nextInt(Option.K_MAX - Option.K_MIN);
		    	    evt.min_length = Option.MIN_LENGTH_LOW + random.nextDouble() * (Option.MIN_LENGTH_HIGH - Option.MIN_LENGTH_LOW);
	            }
	            else
	                break;
	        }
	        //
	        exposed_events.add(evt);
	        cur_evt = evt;
	    }
	    //
	    return exposed_events;
	}
	
	
	// select trace from p.random_traces and compute exposed events
	public static List<Event> select_events(MMBMap map_data, Profile p){
		List<Trace> long_traces = new ArrayList<Trace>(); 
	    for (Trace trace : p.random_traces)
	        if (trace.edge_list.size() >= Option.MIN_SELECTED_TRACE_LEN)
	            long_traces.add(trace);
	    

	    if (long_traces.size() == 0)
	        return new ArrayList<Event>(); 
	    
	    Random random = new Random();   
	    Trace selected_trace = long_traces.get(random.nextInt(long_traces.size()));
	    
	    List<Event> exposed_events = compute_exposed_events(map_data, selected_trace, p.user_id, p.user_speed);
	    
	    return exposed_events;
	}
	            
	
	//
	public static void generate_random_traces_and_events(MMBMap map_data, List<Profile> profile_list){
		int count = 0;
	    for (Profile p : profile_list){
	        if (count % 500 == 0)
	            System.out.println("random_traces_and_events - count =" + count);
	        count += 1;
	        
	        // 4 - generate random traces
	        Random random = new Random();
	        int n_random_traces = Option.MIN_N_RANDOM_TRACE + random.nextInt(Option.MAX_N_RANDOM_TRACE - Option.MIN_N_RANDOM_TRACE);    
	        p.random_traces = generate_random_traces(map_data, p.trans_prob, p.access_prob, n_random_traces, p.deviation);
	    
	        // 5 - select trace and compute exposed events
	        p.exposed_events = select_events(map_data, p); 
	        
	        int trial = 0;
	        while (p.exposed_events.size() == 0){   // regenerate random_traces if not long enough
	            p.random_traces = generate_random_traces(map_data, p.trans_prob, p.access_prob, n_random_traces, p.deviation);
	            p.exposed_events = select_events(map_data, p);
	            trial += 1;
	            if (trial == 5)
	                break;
	        }
	    }
		
	}
	
	//
	// return a list of training Paths    
	public static List<Path> generate_paths(MMBMap map_data, EdgeWeightedGraph G, int n_train_paths, List<Integer> list_node_ids){    
	    
	    
	    // number of train groups (sum = n_train_paths)
		Random random = new Random();
	    int n_group = Option.MIN_TRAIN_GROUP + random.nextInt(Option.MAX_TRAIN_GROUP - Option.MIN_TRAIN_GROUP);
	    
	    double[] freq_list = new double[n_group];
	    double sum_freq = 0.0;
	    for (int i = 0; i < n_group; i++){
	    	double val = 0.1 + random.nextDouble() * 0.9;
	        freq_list[i] = val;
	        sum_freq += val;
	    }
	    
	    for (int i = 0; i < n_group; i++)
	        freq_list[i] = freq_list[i] / sum_freq;
	    double s = 0;        
	    for (int i = 0; i < n_group-1; i++){              
	        freq_list[i] = (int)(Math.floor(freq_list[i] * n_train_paths));
	        if (freq_list[i] == 0) 
	            freq_list[i] = 1;
	        s += freq_list[i];
	    }
	    freq_list[n_group-1] = n_train_paths - s;    // last path
	    
	    //
	    int n_nodes = map_data.nodes.size();
	    
	    List<Path> train_paths = new ArrayList<Path>();
	    for (int k = 0; k < n_group; k++){
	        Path path = new Path();
	        
	        // source/target node ids
	        int source_node_id = list_node_ids.get(random.nextInt(n_nodes));
	        int target_node_id = list_node_ids.get(random.nextInt(n_nodes));
	        while (target_node_id == source_node_id)
	            target_node_id = list_node_ids.get(random.nextInt(n_nodes));
	            
	        //
	        List<Integer> node_list = EdgeWeightedGraph.shortest_path(G, source_node_id, target_node_id);
	        
	        path.source_node_id = source_node_id;
	        path.target_node_id = target_node_id;
	        path.node_list = node_list;
	        path.freq = freq_list[k];
	        // compute path.edge_list
	        path.edge_list = new ArrayList<Integer>();
	        for (int i = 0; i < path.node_list.size() - 1; i++){
	            int node1 = path.node_list.get(i);
	            int node2 = path.node_list.get(i+1);
	            int edge_id = map_data.node_pair_to_edge.get(new PairInt(node1, node2));
	            path.edge_list.add(edge_id);
	        }
	            
	        //
	        train_paths.add(path); 
	    }
	    //
	    return train_paths;
	}
	
	//
	public static List<Profile> generate_profiles(MMBMap map_data){
	    
	    double[] speed_classes = Option.SPEED_CLASSES;
	    
	    List<Integer> list_node_ids = new ArrayList<Integer>();
	    for (Node node : map_data.nodes.values())
	    	list_node_ids.add(node.node_id);
	    
	    // 2 - prepare weighted graph G for shortest paths in generate_profiles()
	    System.out.println("num of map_data.edges :" + map_data.edges.size());
	    int V = list_node_ids.size();
	    EdgeWeightedGraph G = new EdgeWeightedGraph(V);
	    for (Edge edge : map_data.edges.values())
	        G.addEdge(new WeightedEdge(edge.start_node_id, edge.end_node_id, edge.edge_length/speed_classes[edge.edge_class]));
	    // DEBUG
	    System.out.println("graph G:");
	    System.out.println("num of nodes :" + G.V());
	    System.out.println("num of edges :" + G.E());
	    
	        
	    // 3 - generate transition matrix (trans_prob)
	    ////// TEST
	//    p = Profile()
	//    p.n_train_paths = 30
	//    p.train_paths = generate_paths(map_data, G, p.n_train_paths)
	//    
	//    print "p.n_groups =", len(p.train_paths)
	//    for path in p.train_paths:
	//        print path.source_node_id, path.target_node_id, " freq =", path.freq
	////        print path.node_list
	//        print path.edge_list
	        
	    
	    List<Profile> profile_list = new ArrayList<Profile>();   // list of user Profiles
	    Random random = new Random();
	    for (int i = 0; i < Option.N_USERS; i++){
	    	Profile p = new Profile();
	        p.user_id = i;
	        // assign a nominal speed
	        
	        p.user_speed = Option.USER_NOMINAL_SPEEDS[random.nextInt(Option.NUM_NOMINAL_SPEEDS)] * Option.SPEED_PROFILE;
	        
	        p.n_train_paths = Option.MIN_N_TRAIN_PATH + random.nextInt(Option.MAX_N_TRAIN_PATH - Option.MIN_N_TRAIN_PATH);
	        p.train_paths = generate_paths(map_data, G, p.n_train_paths, list_node_ids);
	//        print "finish generate_paths"
	        
	        TupleTrace temp = compute_trans_prob_and_access_prob(map_data, p.train_paths, p.user_id, p.user_speed);
	        p.trans_prob = temp.trans_prob;
	        p.access_prob = temp.access_prob;
	        p.trans_prob_e = temp.trans_prob_e;
	        p.access_prob_e = temp.access_prob_e;
	        p.move_cdf  = temp.move_cdf;
	        
	//        print "finish compute_trans_prob_and_access_prob"
	        //
	        profile_list.add(p);
	        
	        if (i % 500 == 0)
	        	System.out.println("finished i = " + i);
	    }
	    
	    // RETURN profile_list
	    return profile_list;
	}

	//
	// return trans_prob, computed from train_paths
	public static TupleTrace compute_trans_prob_and_access_prob(MMBMap map_data, List<Path> train_paths, int user_id, double user_speed){
		Map<Integer, Map<Integer, Double>> trans_prob = new HashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> access_prob = new HashMap<Integer, Double>();
	    
	    ////// PER NODE
	    for (Path path : train_paths){
	        // for trans_prob{}
	        for (int i = 0; i < path.node_list.size() - 1; i++){
	            int node1 = path.node_list.get(i);
	            int node2 = path.node_list.get(i+1);
	            if (!trans_prob.containsKey(node1))
	                trans_prob.put(node1, new HashMap<Integer, Double>());      // dict: (node2, num of occurences)
	            if (!trans_prob.get(node1).containsKey(node2))
	                trans_prob.get(node1).put(node2, path.freq);
	            else        
	            	trans_prob.get(node1).put(node2, trans_prob.get(node1).get(node2) + path.freq);
	        }
	        
	        // for access_prob{}
	        for (int node : path.node_list)
	            if (! access_prob.containsKey(node))
	                access_prob.put(node, path.freq);
	            else
	                access_prob.put(node, access_prob.get(node) + path.freq);       
	    }
	//    print "finish PER NODE"

	                
	    // normalize to get prob. sum = 1
	    // for trans_prob{}
	    for (Entry<Integer, Map<Integer, Double>> entry : trans_prob.entrySet()){
	    	int node1 = entry.getKey();
	    	Map<Integer, Double> adj_dict = entry.getValue();
	    	
	        int total_occur = 0;
	        for (Double n_occur : adj_dict.values())
	            total_occur += n_occur;
	        	
	        for (int node2 : adj_dict.keySet())
	            trans_prob.get(node1).put(node2, trans_prob.get(node1).get(node2)/total_occur);
	    }
	    
	//    print "finish trans_prob"
	            
	    // for access_prob{}
	    double total_occur = 0;
	    for (Double n_occur : access_prob.values())
	        total_occur += n_occur;
	    
	    for (int node : access_prob.keySet())
	        access_prob.put(node, access_prob.get(node)/total_occur);         
	//    print "finish access_prob"    
	    
	    ////// PER EDGE
	    Map<Integer, Map<Integer, Double>> trans_prob_e = new HashMap<Integer, Map<Integer, Double>>();
	    Map<Integer, Double> access_prob_e = new HashMap<Integer, Double>(); 
	    
	    for (Path path : train_paths){
	        // for trans_prob_e{}
	        for (int i = 0; i < path.edge_list.size() - 1; i++){
	            int edge1 = path.edge_list.get(i);
	            int edge2 = path.edge_list.get(i+1);
	            if (! trans_prob_e.containsKey(edge1))
	                trans_prob_e.put(edge1, new HashMap<Integer, Double>());      // new dict: (edge2, num of occurences)
	            if (! trans_prob_e.get(edge1).containsKey(edge2))
	                trans_prob_e.get(edge1).put(edge2, path.freq);
	            else        
	            	trans_prob_e.get(edge1).put(edge2, trans_prob_e.get(edge1).get(edge2) + path.freq);
	        }
	        
	        // for access_prob_e{}
	        for (int edge : path.edge_list)
	            if (!access_prob_e.containsKey(edge))
	                access_prob_e.put(edge, path.freq);
	            else
	                access_prob_e.put(edge, access_prob_e.get(edge) + path.freq);
	    }              
	                
	    // normalize to get prob. sum = 1
	    // for trans_prob_e{}
	    for (Entry<Integer, Map<Integer, Double>> entry : trans_prob_e.entrySet()){
	    	int edge1 = entry.getKey();
	    	Map<Integer, Double> adj_dict = entry.getValue();
	        total_occur = 0;
	        for (double n_occur : adj_dict.values())
	            total_occur += n_occur;
	        for (int edge2 : adj_dict.keySet())
	        	trans_prob.get(edge1).put(edge2, trans_prob.get(edge1).get(edge2)/total_occur);
	    }
	//    print "finish trans_prob_e"  
	    
	    // for access_prob_e{}
	    total_occur = 0;
	    for (double n_occur : access_prob_e.values())
	        total_occur += n_occur;
	    for (int edge : access_prob_e.keySet())
	        access_prob_e.put(edge, access_prob_e.get(edge)/total_occur);    
	//    print "finish access_prob_e" 
	            
	    
	    // CHECK normalization condition (sum = 1)
	    
	    ////// MOVE_CDF
	    Map<Integer, Map<Integer, List<Double>>> move_cdf = new HashMap<Integer, Map<Integer, List<Double>>>();
	    
	    for (Path path : train_paths){
	        List<Event> exposed_events = compute_exposed_events(map_data, new Trace(path), user_id, user_speed);
	        int n_events = exposed_events.size();
	        for (int i = 0; i < n_events-1; i++){
	            Event evt_i = exposed_events.get(i);
	            if (! move_cdf.containsKey(evt_i.edge_id))   // initialize if not yet
	                move_cdf.put(evt_i.edge_id, new HashMap<Integer, List<Double>>());
	            for (int j = i+1; j < n_events; j++){
	                if (j - i >= Option.N_TIMESTEPS)      // restrict length of k in S_ij(k)
	                    continue;
	                
	                Event evt_j = exposed_events.get(j);
	                if (!move_cdf.get(evt_i.edge_id).containsKey(evt_j.edge_id)){
	                    List<Double> temp = new ArrayList<Double>();
	                    for (int _t = 0; _t < Option.N_TIMESTEPS; _t++)
	                    	temp.add(0.0);
	                	move_cdf.get(evt_i.edge_id).put(evt_j.edge_id, temp);   // initialize if not yet
	                    move_cdf.get(evt_i.edge_id).get(evt_j.edge_id).set(j-i, path.freq);
	                }
	                else{
	                    double val = move_cdf.get(evt_i.edge_id).get(evt_j.edge_id).get(j-i) + path.freq;
	                    move_cdf.get(evt_i.edge_id).get(evt_j.edge_id).set(j-i, val);
	                }
	            }
	        }
	    }

	    //    print "finish move_cdf"
	    // compute CDF
	    for (Map<Integer, List<Double>> adj_dict : move_cdf.values())
	        for (List<Double> freq_list : adj_dict.values()){
	            double s = 0.0;
	            for (double val : freq_list)
	            	s += val;
	            for (int i = 1; i < freq_list.size(); i++)
	                freq_list.set(i, freq_list.get(i) + freq_list.get(i-1));
	            for (int i = 0; i < freq_list.size(); i++)
	                freq_list.set(i, freq_list.get(i)/s);
	        }
	//    print "finish compute cdf"             
	    
	    //
	    return new TupleTrace(trans_prob, access_prob, trans_prob_e, access_prob_e, move_cdf);  
	}
	
	//
	public static void save_profiles(List<Profile> profile_list) throws IOException{
	    
		BufferedWriter f = new BufferedWriter(new FileWriter("../out/" + Option.getProfileName() + "_profiles.txt"));
	    // save p.trans_prob + p.access_prob
	    for (Profile p : profile_list){
	        f.write(p.user_id +"-" + p.user_speed + "\n");
	        ////// NODE
	        // trans_prob
	        for (Entry<Integer, Map<Integer, Double>> entry : p.trans_prob.entrySet()){
	        	int node1 = entry.getKey();
	        	Map<Integer, Double> adj_dict = entry.getValue();
	            
	        	f.write(node1 + "-");
	            for (Entry<Integer, Double> entry2 : adj_dict.entrySet()){
	            	int node2 = entry2.getKey();
	            	double freq = entry2.getValue();
	                f.write(node2 + ":" + freq + ",");
	            }
	            f.write(";");
	        }
	        f.write("\n");
	        
	        // access_prob
	        for (Entry<Integer, Double> entry2 : p.access_prob.entrySet()){ 
	        	int node = entry2.getKey();
            	double freq = entry2.getValue();
	            f.write(node + ":" + freq + ",");
	        }
	            
	        f.write("\n");
	        
	        ////// EDGE
	        // trans_prob_e
	        for (Entry<Integer, Map<Integer, Double>> entry : p.trans_prob_e.entrySet()){
	        	int edge1 = entry.getKey();
	        	Map<Integer, Double> adj_dict = entry.getValue();
	            
	        	f.write(edge1 + "-");
	            for (Entry<Integer, Double> entry2 : adj_dict.entrySet()){
	            	int edge2 = entry2.getKey();
	            	double freq = entry2.getValue();
	                f.write(edge2 + ":" + freq + ",");
	            }
	            f.write(";");
	        }
	        f.write("\n");
	        
	        // access_prob_e
	        for (Entry<Integer, Double> entry2 : p.access_prob_e.entrySet()){ 
	        	int edge = entry2.getKey();
            	double freq = entry2.getValue();
	            f.write(edge + ":" + freq + ",");
	        }
	        f.write("\n");
	        
	        ////// MOVE_CDF
	        // move_cdf
	        for (Entry<Integer, Map<Integer, List<Double>>> entry : p.move_cdf.entrySet()){
	        	int edge1 = entry.getKey();
	        	Map<Integer, List<Double>> adj_dict = entry.getValue();
	            
	        	f.write(edge1 + "-");
	            for (Entry<Integer, List<Double>> entry2 : adj_dict.entrySet()){
	            	int edge2 = entry2.getKey();
	            	List<Double> freq_list = entry2.getValue();
	                f.write(edge2 + ":");
	                
	                for (double freq : freq_list)
	                    f.write(freq + ",");
	                f.write("|");
	            }
	            f.write(";");
	        }
	        
	        f.write("\n");
	    }
	        
	        
	    f.close();
	}
	    
	    
	//////////////////////////////////////////////
	public static void main(String[] args) throws IOException {
	
		MMBMap map_data = new MMBMap();
		map_data.read_map(Option.MAP_PATH, Option.MAP_NAME);
		
		//
		// TEST generate_profiles()
	    long start = System.currentTimeMillis();
	    
	    List<Profile> profile_list = generate_profiles(map_data);
	    System.out.println("generate_profiles - DONE !");
	    System.out.println("Elapsed " + (System.currentTimeMillis() - start));
	    
	//    p = profile_list[0]
	//    print p.move_cdf
	    
	    
	    // TEST save_profiles()
	    start = System.currentTimeMillis();
	    
	    save_profiles(profile_list);
	    System.out.println("save_profiles - DONE !");
	    System.out.println("Elapsed " + (System.currentTimeMillis() - start));
	    
	    
	    // TEST load_profiles()
	//    start = time.clock()
	//    
	//    filename = "../out/" + Option.PROFILE_NAME + "_profiles.txt"
	//    profile_list = load_profiles(filename, Option.N_USERS, False)   // turn off/on loading move_cdf
	//    print "load_profiles - DONE !"
	//    print "Elapsed ", (time.clock() - start)
	////    p = profile_list[0]
	////    print p.move_cdf
	//    
	//    // assign deviation probabilitites
	//    assign_deviation_probs(profile_list)
	//    
	//    //
	//    start = time.clock()
	//    
	//    profile_list = generate_random_traces_and_events(map_data, profile_list)
	//    print "generate_random_traces_and_events - DONE !"
	//    print "Elapsed ", (time.clock() - start)
	//
	//    // write events (inputs for MeshCloak, ICliqueCloak)
	//    start = time.clock()
	//    
	//    filename = "../out/" + Option.PROFILE_NAME + "_events.txt"
	//    write_events(profile_list, filename)
	//    print "write_events - DONE !"
	//    print "Elapsed ", (time.clock() - start)
		
	}
}
