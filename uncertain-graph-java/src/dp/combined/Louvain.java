/*
 * Sep 14, 2015
 * 	- copy from LouvainGrph.java, but use algs4.EdgeWeightedGraph instead
 * Sep 16
 *	- add new variable status.sizes, status.com2com
 *	- checkTrueEdges (TOP-DOWN) + findCom(), checkNumEdges()
 *	- logLK() BOTTOM-UP --> wrong!
 * Sep 22
 * 	- (degree/(2.0*links))*(degree /(2.0*links)); 	
 * Sep 28
 * 	- PASS_MAX = 20 (nb_pass_done < PASS_MAX in true graphs), used to reduce runtime of Louvain on TmF, EdgeFlip
 */

package dp.combined;

import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import toools.set.IntHashSet;
import toools.set.IntSet;
import algs4.Edge;
import algs4.EdgeWeightedGraph;




////
class Status{
	public Map<Integer, Integer> node2com = new HashMap<Integer, Integer>();// 	[node]
    double total_weight = 0;
    public Map<Integer, Double> internals = new HashMap<Integer, Double>();	// 	[com]
    public Map<Integer, Integer> degrees = new HashMap<Integer, Integer>();	//	[com]
    public Map<Integer, Integer> gdegrees = new HashMap<Integer, Integer>();//	[node]
    public Map<Integer, Integer> loops = new HashMap<Integer, Integer>();	//	[node]
    public Map<Integer, Integer> sizes = new HashMap<Integer, Integer>();	// sizes[com] = #nodes within that 'com', see renumber()
    public Map<Integer, Integer> com2com = new HashMap<Integer, Integer>();	//	created in renumber()
    
    //
    public static Map<Integer, Integer> dictCopyInteger(Map<Integer, Integer> source){
    	Map<Integer, Integer> result = new HashMap<Integer, Integer>();
    	for (Map.Entry<Integer, Integer> entry: source.entrySet())
    		result.put(entry.getKey(), entry.getValue());
    	
    	return result;
    }
    
    //
    public static Map<Integer, Double> dictCopyDouble(Map<Integer, Double> source){
    	Map<Integer, Double> result = new HashMap<Integer, Double>();
    	for (Map.Entry<Integer, Double> entry: source.entrySet())
    		result.put(entry.getKey(), entry.getValue());
    	
    	return result;
    }
    
    public Status copy(){
    	Status new_status = new Status();
        new_status.node2com = dictCopyInteger(this.node2com);
        new_status.internals = dictCopyDouble(this.internals);
        new_status.degrees = dictCopyInteger(this.degrees);
        new_status.gdegrees = dictCopyInteger(this.gdegrees);
        new_status.sizes = dictCopyInteger(this.sizes);
        new_status.total_weight = this.total_weight;
        new_status.com2com = dictCopyInteger(this.com2com);
        //
        return new_status;
    }
    
    public void print(){
    	System.out.println("STATUS");
    	System.out.println("total_weight = " + total_weight + " modularity = " + Louvain.modularity(this));
    	
//    	System.out.println("node2com");
//    	for (int key : this.node2com.keySet())
//    		System.out.print(key + ":" + this.node2com.get(key) + ", ");
//    	System.out.println();
//    	System.out.println("com2com");
//    	for (int key : this.com2com.keySet())
//    		System.out.print(key + ":" + this.com2com.get(key) + ", ");
//    	System.out.println();
//    	System.out.println("internals");
//    	for (int key : this.internals.keySet())
//    		System.out.print(key + ":" + this.internals.get(key) + ", ");
//    	System.out.println();
//    	System.out.println("degrees");
//    	for (int key : this.degrees.keySet())
//    		System.out.print(key + ":" + this.degrees.get(key) + ", ");
//    	System.out.println();
//    	System.out.println("sizes");
//    	for (int key : this.sizes.keySet())
//    		System.out.print(key + ":" + this.sizes.get(key) + ", ");
//    	System.out.println();
    	//
    	int totalSize = 0;
    	for (int size : this.sizes.values())
    		totalSize += size;
    	System.out.println("totalSize = " + totalSize);
    	int totalLoop = 0;
    	for (int loop : this.loops.values())
    		totalLoop += loop;
    	System.out.println("totalLoop = " + totalLoop);
    	System.out.println("------------");
    }
    
    public void init(EdgeWeightedGraph graph, Map<Integer, Integer> part, Status status){
    	int V = graph.V();
    	
    	// Initialize the status of a graph with every node in one community
        int count = 0;
        
        this.node2com = new HashMap<Integer, Integer>();
        int total_weight = 0;
        this.internals = new HashMap<Integer, Double>();
        this.degrees = new HashMap<Integer, Integer>();
        this.gdegrees = new HashMap<Integer, Integer>();
        this.loops = new HashMap<Integer, Integer>();
        this.total_weight = graph.totalWeight();
        
        if (part == null){
        	for (int node = 0; node < V; node++){
        		
        		this.node2com.put(node, count);
//        		int deg = graph.degree(node);	// WRONG
        		double deg = graph.adjWeight(node);
                this.degrees.put(count, (int)deg);
                this.gdegrees.put(node, (int)deg);
                Edge e = graph.getEdge(node, node);
                if (e != null)
                	this.loops.put(node, (int)e.weight());
                else
                	this.loops.put(node, 0);
                this.internals.put(count, (double)this.loops.get(node));
                if (status == null)
                	this.sizes.put(node, 1);	// init sizes at level 0
                	
                count = count + 1;
        	}
        	
        }else{
        	for (int node = 0; node < V; node++){
        		
        		int com = part.get(node);
        		this.node2com.put(node, com);
//        		int deg = graph.degree(node);	// WRONG
        		double deg = graph.adjWeight(node);
                this.degrees.put(count, (int)deg);
        		if (this.degrees.containsKey(com))
        			this.degrees.put(com, this.degrees.get(com) + (int)deg);
        		else
        			this.degrees.put(com, (int)deg);
        		this.gdegrees.put(node, (int)deg);
        		// missing in python (package 'community')
        		Edge e1 = graph.getEdge(node, node);
                if (e1 != null)
                	this.loops.put(node, (int)e1.weight());
                else
                	this.loops.put(node, 0);
        		
                double inc = 0.0;
                for (Edge e : graph.adj(node).values()){
                	int neighbor = e.other(node);
                    double weight = e.weight();
                    if (part.get(neighbor) == com){
                        if (neighbor == node)
                            inc += weight;
                        else
                            inc += weight / 2.;
                    }
	                if (this.internals.containsKey(com))
	                	this.internals.put(com, this.internals.get(com) + inc);
	                else
	                	this.internals.put(com, inc);
                }
                
                // init sizes at level 0
                if (this.sizes.containsKey(com))
                	this.sizes.put(com, this.sizes.get(com) + 1);
                else
                	this.sizes.put(com, 1);
        	}
        }
        // copy sizes
        if (status != null)	// init sizes at level > 0, see renumber()
        	this.sizes = dictCopyInteger(status.sizes);
        	
    }
	
}

public class Louvain {
	
	static final int PASS_MAX = 20;		// -1
	static final double MIN = 0.0000001;

	
	
	////
	private Map<Integer, Integer> neighcom(int node, EdgeWeightedGraph graph, Status status){
	    // Compute the communities in the neighborood of node in the graph given
	    // with the decomposition node2com
		Map<Integer, Integer> weights = new HashMap<Integer, Integer>();
	    for (Edge e : graph.adj(node).values()){
        	int neighbor = e.other(node);
	        if (neighbor != node){
	        	int weight = (int)e.weight();
	            int neighborcom = status.node2com.get(neighbor);
	            if (weights.containsKey(neighborcom))
	            	weights.put(neighborcom, weights.get(neighborcom) + weight);
	            else
	            	weights.put(neighborcom, weight);
	        }
	
	    }
	    //
	    return weights;
	}
	
	////
	private void remove(int node, int com, int weight, Status status){
	    // Remove node from community com and modify status"""
	    status.degrees.put(com, status.degrees.get(com) - status.gdegrees.get(node) );
	    status.internals.put(com, status.internals.get(com) - weight - status.loops.get(node) );
	    status.node2com.put(node, -1);
	}
	    
	////
	private void insert(int node, int com, int weight, Status status){
	    // Insert node into community and modify status"""
	    status.node2com.put(node, com);
	    status.degrees.put(com, status.degrees.get(com) + status.gdegrees.get(node) );
	    status.internals.put(com, status.internals.get(com) + weight + status.loops.get(node) );
	}
	                        
	////
	public static double modularity(Status status){
		// Compute the modularity of the partition of the graph fastly using status precomputed
	    double links = status.total_weight;
	    double result = 0.0;
	    
	    Set<Integer> set = new HashSet<Integer>(status.node2com.values());
	    for (int community : set){
	        double in_degree = status.internals.get(community);
	        int degree = status.degrees.get(community);
	        if (links > 0)
	            result = result + in_degree/links - (degree/(2.0*links))*(degree /(2.0*links));
	    }
	    //
	    return result;
	}
	
	//// call neighcom(), remove(), insert()
	private void one_level(EdgeWeightedGraph graph, Status status){
		int V = graph.V();
		
		// Compute one level of communities
	    boolean modif = true;
	    int nb_pass_done = 0;
	    double cur_mod = modularity(status);
	    double new_mod = cur_mod;

	    while (modif && nb_pass_done != PASS_MAX){
	    	cur_mod = new_mod;
	        modif = false;
	        nb_pass_done += 1;

	        for (int node = 0; node < V; node++){
	        	
	            int com_node = status.node2com.get(node);
	            double degc_totw = status.gdegrees.get(node) / (status.total_weight*2.);
	            
	            Map<Integer, Integer> neigh_communities = neighcom(node, graph, status);
	            
	            // remove() 'node' from current 'com_node'
	            int weight = 0;
	            if (neigh_communities.containsKey(com_node))
	            	weight = neigh_communities.get(com_node);
	            remove(node, com_node, weight, status);
	            
	            // compute 'best_com'
	            int best_com = com_node;
	            double best_increase = 0;
	            for (Map.Entry<Integer, Integer> entry : neigh_communities.entrySet()){
	            	int com = entry.getKey();
	            	int dnc = entry.getValue();
	                double incr =  dnc  - status.degrees.get(com) * degc_totw;
	                if (incr > best_increase){
	                    best_increase = incr;
	                    best_com = com;
	                }
	            }
	            
	            // insert() 'node' into 'best_com'
	            weight = 0;
	            if (neigh_communities.containsKey(best_com))
	            	weight = neigh_communities.get(best_com);
	            insert(node, best_com, weight, status);
	            
	            if (best_com != com_node)
	                modif = true;
			}
	        new_mod = modularity(status);
	        
	        if (new_mod - cur_mod < MIN)
	            break;
	    }
	    
	    System.out.println("?PASS_MAX = " + (nb_pass_done < PASS_MAX));
	}
	
	//// replace param 'dictionary' by 'status' (dictionary = status.node2com)
	// 	
	private Map<Integer, Integer> renumber(Status status){
		
		// Renumber the values of the dictionary from 0 to n
		Map<Integer, Integer> dictionary = status.node2com;
		
	    int count = 0;
	    Map<Integer, Integer> ret = Status.dictCopyInteger(dictionary);
	    Map<Integer, Integer> new_values = new HashMap<Integer, Integer>();
	    status.com2com = new HashMap<Integer, Integer>();

	    for (Integer key : dictionary.keySet()){
	        int value = dictionary.get(key);
	        int new_value = -1;
	        if (! new_values.containsKey(value)){
	            new_values.put(value, count);
	            new_value = count;
	            count = count + 1;
	        }else
	        	new_value = new_values.get(value);
	        ret.put(key, new_value);
	        status.com2com.put(value, new_value);
	    }
	    
	    // update status.sizes
	    Map<Integer, Integer> new_sizes = new HashMap<Integer, Integer>();
	    for (Integer key : status.sizes.keySet()){
	    	int com = ret.get(key);
	    	if (new_sizes.containsKey(com))
	    		new_sizes.put(com, new_sizes.get(com) + status.sizes.get(key));
	    	else
	    		new_sizes.put(com, status.sizes.get(key));
	    }
	    status.sizes = new_sizes;
	    
	    

	    return ret;
	}
	
	////
	private EdgeWeightedGraph induced_graph(Map<Integer, Integer> partition, EdgeWeightedGraph graph){
		// Produce the graph where nodes are the communities
	    // there is a link of weight w between communities if the sum of the weights of the links between their elements is w
		
		// debug
//		System.out.println("induced_graph");
//		for (int key : partition.keySet())
//			System.out.println(key + " " + partition.get(key));
		
		IntSet nodeSet = new IntHashSet();
		for (int value : partition.values())
			nodeSet.add(value);
		EdgeWeightedGraph ret = new EdgeWeightedGraph(nodeSet.size());	

	    for (Edge e : graph.edges()){
	    	int node1 = e.either();
	    	int node2 = e.other(node1);
	        int weight = (int)e.weight();
	        int com1 = partition.get(node1);
	        int com2 = partition.get(node2);
	        
	        Edge r_e = ret.getEdge(com1, com2);
	        if (r_e != null){
	        	r_e.setWeight(r_e.weight() + weight);
	        }else
	        	ret.addEdge(new Edge(com1, com2, weight));
	    }

	    return ret;
	}
	
	////
	public List<Map<Integer, Integer>> generate_dendrogram(EdgeWeightedGraph graph, Map<Integer, Integer> part_init){
		int V = graph.V();
		
		// Find communities in the graph and return the associated dendrogram
		//
	    // A dendrogram is a tree and each level is a partition of the graph nodes.  
	    // Level 0 is the first partition, which contains the smallest communities, and the best is len(dendrogram) - 1. 
	    // The higher the level is, the bigger are the communities
	    
		Map<Integer, Integer> part = new HashMap<Integer, Integer>();
		
		// special case, when there is no link
	    // the best partition is everyone in its community
	    if (graph.E() == 0){
	        for (int node = 0; node < V; node++)
	            part.put(node, node);
	        
	        List<Map<Integer, Integer>> list = new ArrayList<Map<Integer,Integer>>();
	        list.add(part);
	        return list;
	    }
	    
	    //
	    EdgeWeightedGraph current_graph = graph.clone();
	    Status status = new Status();
	    status.init(current_graph, part_init, null);
	    double mod = modularity(status);
	    //debug
//	    status.print();
//    	System.out.println(current_graph);
	    System.out.println("current_graph: #nodes = " + current_graph.V() + " #edges = " + current_graph.E() + " mod = " + mod);

	    List<Status> status_all = new ArrayList<Status>();			// for logLK()
	    List<EdgeWeightedGraph> graph_all = new ArrayList<EdgeWeightedGraph>();			// for logLK()
	    
	    List<Map<Integer, Integer>> status_list = new ArrayList<Map<Integer,Integer>>();
	    
	    one_level(current_graph, status);
	    System.out.println("current_graph: #nodes = " + current_graph.V() + " #edges = " + current_graph.E() + " mod = " + mod);
	    
	    double new_mod = modularity(status);
	    
	    
	    //debug
//	    status.print();
//	    System.out.println(current_graph);
	    
	    Map<Integer, Integer> partition = renumber(status);
	    status_list.add(partition);
	    status_all.add(status.copy());
	    
	    mod = new_mod;
	    
	    
	    current_graph = induced_graph(partition, current_graph);
	    status.init(current_graph, null, status);
	    graph_all.add(current_graph.clone());
	    
	    
	    
	    while (true){
	    	// debug
//	    	status.print();
//	    	System.out.println(current_graph);
		    
	    	one_level(current_graph, status);
	    	//debug
	    	System.out.println("current_graph: #nodes = " + current_graph.V() + " #edges = " + current_graph.E() + " mod = " + mod);
	    	
	        new_mod = modularity(status);
	        
	        if (new_mod - mod < MIN)
	            break;
	        partition = renumber(status);
	        status_list.add(partition);
	        status_all.add(status.copy());
	        
	        
	        
	        mod = new_mod;
	        
	        current_graph = induced_graph(partition, current_graph);
	        status.init(current_graph, null, status);
	        graph_all.add(current_graph.clone());
	        
	        
	    }
	    
	  	//debug
	    System.out.println("modularity = " + new_mod);
	    System.out.println("#communitites = " + current_graph.V());
	    
	    // debug
//	    logLK(graph_all, status_all);
////	    System.out.println("logLK = " + logLK(graph, status_all));
//	    
////	    checkNumEdges(graph_all, status_all, graph);
////	    System.out.println("checkNumEdges - DONE.");
//	    
//	    double logLK2 = checkTrueEdges(status_all, graph);
//	    System.out.println("logLK2 = " + logLK2);
//	    System.out.println("checkTrueEdges - DONE.");
	    
	    //
	    return status_list;
	}
	
	////
	public Map<Integer, Integer> partition_at_level(List<Map<Integer, Integer>> dendrogram, int level){
		Map<Integer, Integer> partition = Status.dictCopyInteger(dendrogram.get(0));
	    for (int index = 1; index < level + 1; index++)
	        for (Map.Entry<Integer, Integer> entry : partition.entrySet()){
	        	int node = entry.getKey();
	        	int community = entry.getValue();
	            partition.put(node, dendrogram.get(index).get(community));
	        }
	    return partition;
	}
	
	////
	public Map<Integer, Integer> best_partition(EdgeWeightedGraph graph, Map<Integer, Integer> partition){
		List<Map<Integer, Integer>> dendo = generate_dendrogram(graph, partition);
	    return partition_at_level(dendo, dendo.size() -1 );
		
	}
	
	
	////
	public void checkNumEdges(List<EdgeWeightedGraph> graph_all, List<Status> status_all, EdgeWeightedGraph graph_org){
		
		// check that each 'com' at the current level has weight equal to the sum of weights
		for (int l = 1; l < status_all.size(); l++){	
			Status status = status_all.get(l);
			EdgeWeightedGraph graph = graph_all.get(l);
			
			EdgeWeightedGraph graph_sub = graph_all.get(l-1);
			
			for (int com : status.com2com.keySet()){
				int recom = status.com2com.get(com);
				double internal = graph.getEdge(recom, recom).weight();
				
				double internal_sub = 0.0;
				for (int i = 0; i < graph_sub.V(); i++)
					for (int j = i; j < graph_sub.V(); j++)
						if (status.node2com.get(i) == com && status.node2com.get(j) == com)
							if (graph_sub.getEdge(i, j) != null)
								internal_sub += graph_sub.getEdge(i, j).weight();
				
				//
//				System.out.println("recom = " + recom + " : internal = " + internal + " internal_sub = " + internal_sub);
				if (internal != internal_sub)
					System.err.println("error");
			}
		}
		
		// check level 0 (graph vs. graph_org)
		System.out.println("check level 0");
		
		Status status = status_all.get(0);
		EdgeWeightedGraph graph = graph_all.get(0);
		for (int com : status.com2com.keySet()){
			int recom = status.com2com.get(com);
			double internal = graph.getEdge(recom, recom).weight();
			
			List<Integer> nodeSet = new ArrayList<Integer>();
			for (int i = 0; i < graph_org.V(); i++)
				if (status.node2com.get(i) == com)
					nodeSet.add(i);
			
			double internal_sub = 0.0;
			for (int i : nodeSet)
				for (int j : nodeSet)
					if (i <= j)
						if (graph_org.getEdge(i, j) != null)
							internal_sub += graph_org.getEdge(i, j).weight();
			
			//
//			System.out.println("recom = " + recom + " : internal = " + internal + " internal_sub = " + internal_sub);
			if (internal != internal_sub)
				System.err.println("error");
		}
		
		
	}

	
	//
	private int findCom(List<Status> status_all, int u, int level){
		int com = status_all.get(0).node2com.get(u);
		int recom = status_all.get(0).com2com.get(com);
		
		for (int l = 1; l < level; l++){
			com = status_all.get(l).node2com.get(recom);
			recom = status_all.get(l).com2com.get(com);
		}
		
		//
		return com;
	}
	
	//
	private int findReCom(List<Status> status_all, int u, int level){
		int com = status_all.get(0).node2com.get(u);
		int recom = status_all.get(0).com2com.get(com);
		
		for (int l = 1; l < level; l++){
			com = status_all.get(l).node2com.get(recom);
			recom = status_all.get(l).com2com.get(com);
		}
		
		//
		return recom;
	}
	
	//// graph: original graph
	public double checkTrueEdges(List<Status> status_all, EdgeWeightedGraph graph){
		double logLK = 0.0;
		
		List<Int2> edgeList = new ArrayList<Int2>();
		for (Edge e : graph.edges()){
			int u = e.either();
			int v = e.other(u);
			edgeList.add(new Int2(u, v));
		}
		
		//
		for (int l = status_all.size()-1; l >= 0; l--){		// MUST BE TOP-DOWN !
			Status status = status_all.get(l);
//			System.out.println("status.sizes");
//	    	for (int key : status.sizes.keySet())
//	    		System.out.print(key + ":" + status.sizes.get(key) + ", ");
//	    	System.out.println();
			
			Map<Int2, Integer> comDict = new HashMap<Int2, Integer>();		// (u_com, v_com) -> num edges
			
			int i = edgeList.size() - 1;
			while (i >= 0){
				Int2 e = edgeList.get(i);
				int u = e.val0;
				int v = e.val1;
				// find communities of u and v
				int u_com = findReCom(status_all, u, l+1);		// findReCom, not findCom
				int v_com = findReCom(status_all, v, l+1);
				if (u_com != v_com){
					if (u_com > v_com){		// swap for comDict
						int temp = u_com;
						u_com = v_com;
						v_com = temp;
					}
					Int2 key = new Int2(u_com, v_com);
					if (comDict.containsKey(key))
						comDict.put(key, comDict.get(key) + 1);
					else
						comDict.put(key,  1);
					
					// delete e
					edgeList.remove(i);
				}
				i = i - 1;
				
			}
			// debug
//			for (Int2 com : comDict.keySet())
//				System.out.print("(" + com.val0 + "," + com.val1 + "):" + comDict.get(com) + ",");
//			System.out.println();
			
			// compute logLK
			for (Int2 com : comDict.keySet()){
				int ni = status.sizes.get(com.val0);
				int nj = status.sizes.get(com.val1);
				double e_ij = comDict.get(com);
				double p_ij = e_ij/(ni*nj);
				if (p_ij > 0.0 && p_ij < 1.0)
					logLK += ni*nj* (p_ij*Math.log(p_ij) + (1-p_ij)*Math.log(1-p_ij)); 
			}
			
		}
		
		// at level 0
		System.out.println("level 0*");
		Status status = status_all.get(0);
//		System.out.println("status.sizes");
//    	for (int key : status.sizes.keySet())
//    		System.out.print(key + ":" + status.sizes.get(key) + ", ");
//    	System.out.println();
    	
		Map<Integer, Integer> comDict = new HashMap<Integer, Integer>();
		for (Int2 e : edgeList){
			int u = e.val0;
			int v = e.val1;
			int u_com = status.com2com.get(status.node2com.get(u));
			int v_com = status.com2com.get(status.node2com.get(v));
			if (u_com == v_com){
				if (comDict.containsKey(u_com))
					comDict.put(u_com, comDict.get(u_com) + 1);
				else
					comDict.put(u_com, 1);
			}
		}
		// debug
//		for (int com : comDict.keySet())
//			System.out.print("(" + com + "):" + comDict.get(com) + ",");
//		System.out.println();
		for (int com : comDict.keySet()){
			int ni = status.sizes.get(com);
			double e_ij = comDict.get(com);
			double p_ij = e_ij/(ni*(ni-1)/2);
			if (p_ij > 0.0 && p_ij < 1.0)
				logLK += ni*(ni-1)/2 * (p_ij*Math.log(p_ij) + (1-p_ij)*Math.log(1-p_ij)); 
					
		}
		
		
		//// OK
//		double edges_counted_old = 0.0;
//		double total_nedges = 0.0;
//		
//		for (int l = status_all.size(); l > 0; l--){		// MUST BE TOP-DOWN !
//			System.out.println("level " + l);
//			double edges_counted = 0.0;		// inter-cluster edges
//			
//			//
//			for (Edge e : graph.edges()){
//				int u = e.either();
//				int v = e.other(u);
//				// find communities of u and v
//				int u_com = findCom(status_all, u, l);
//				int v_com = findCom(status_all, v, l);
//				if (u_com != v_com)
//					edges_counted += 1;
//				
//			}
//			
//			double nedges = edges_counted;
//			if (l > 0)
//				nedges = edges_counted - edges_counted_old;
//			System.out.println("edges counted = " + nedges);
//			total_nedges += nedges;
//			
//			edges_counted_old = edges_counted;
//			
//		}
//		
//		// at level 0
//		System.out.println("level 0*");
//		Status status = status_all.get(0);
//		double edges_counted = 0.0;
//		for (Edge e : graph.edges()){
//			int u = e.either();
//			int v = e.other(u);
//			int u_com = status.node2com.get(u);
//			int v_com = status.node2com.get(v);
//			if (u_com == v_com)
//				edges_counted += 1;
//		}
//		double nedges = edges_counted;
//		System.out.println("edges counted = " + nedges);
//		total_nedges += nedges;
//		
//		//
//		System.out.println("total_nedges = " + total_nedges);
		
		
		//
		return logLK;
	}
	
	////
	public double logLK(List<EdgeWeightedGraph> graph_all, List<Status> status_all){
		double ret = 0.0;
		
		// print
		for (int i = 0; i < status_all.size(); i++){	// at each level
			Status status = status_all.get(i);
			EdgeWeightedGraph graph = graph_all.get(i);
			
			status.print();
			System.out.println("graph : #nodes = " + graph.V() + " #edges = " + graph.E() + " totalWeight = " + graph.totalWeight());
//			for (Edge e : graph.edges())
//				System.out.print(e.weight() + " ");
			double totalInternal = 0;
			for (int u = 0; u < graph.V(); u++){
				System.out.print(graph.getEdge(u, u).weight() + " ");
				totalInternal += graph.getEdge(u, u).weight();
			}
			System.out.println();
			System.out.println("totalInternal = " + totalInternal);
			System.out.println("======");
		}

		// level 0
		double total_e = 0;
		Status status = status_all.get(0);
		EdgeWeightedGraph graph = graph_all.get(0);
		System.out.println("level 0 : #nodes = " + graph.V());
		
		for (int i = 0; i < graph.V(); i++){
			double e_ij = graph.getEdge(i, i).weight();
			int ni = status.sizes.get(i);
			total_e += e_ij;
			
			double p_ij = e_ij/(ni*(ni-1)/2);
			if (p_ij > 0.0 && p_ij < 1.0)
				ret += ni*(ni-1)/2 * (p_ij*Math.log(p_ij) + (1-p_ij)*Math.log(1-p_ij)); 
		}
		System.out.println("edges counted = " + total_e);			
		
		// use <status.node2com> of the next level with <status.sizes> and <graph> of the current level !
		for (int l = 0; l < status_all.size()-1; l++){	
			status = status_all.get(l);
			graph = graph_all.get(l);
			Map<Integer, Integer> node2com = status_all.get(l+1).node2com;
			
			System.out.println("level " + (l+1) + " : #nodes = " + graph.V());
			double edges_counted = 0.0;
			
			for (int i = 0; i < graph.V(); i++)
				for (int j = i+1; j < graph.V(); j++)			// each pair (i < j) is considered once
					if (node2com.get(j) == node2com.get(i)){	// com i and com j are merged in the next level
						Edge e = graph.getEdge(i, j);
						double e_ij = 0.0;
						if (e != null)
							e_ij = graph.getEdge(i, j).weight();
//						else
//							System.err.println("error at level " + l + " pair(i,j) = " + i + "," + j );
						
						int ni = status.sizes.get(i);
						int nj = status.sizes.get(j);
						total_e += e_ij;
						edges_counted += e_ij;
						
						double p_ij = e_ij/(ni*nj);
						if (p_ij > 0.0 && p_ij < 1.0)
							ret += ni*nj* (p_ij*Math.log(p_ij) + (1-p_ij)*Math.log(1-p_ij)); 
					}
			System.out.println("edges counted = " + edges_counted);			
			
		}
		// don't forget the top level !
		status = status_all.get(status_all.size()-1);
		graph = graph_all.get(status_all.size()-1);
		System.out.println("level top : #nodes = " + graph.V());
		double edges_counted = 0.0;
		
		for (int i = 0; i < graph.V(); i++)
			for (int j = i+1; j < graph.V(); j++){
				Edge e = graph.getEdge(i, j);
				double e_ij = 0.0;
				if (e != null)
					e_ij = graph.getEdge(i, j).weight();
				int ni = status.sizes.get(i);
				int nj = status.sizes.get(j);
				total_e += e_ij;
				edges_counted += e_ij;
				
				double p_ij = e_ij/(ni*nj);
				if (p_ij > 0.0 && p_ij < 1.0)
					ret += ni*nj* (p_ij*Math.log(p_ij) + (1-p_ij)*Math.log(1-p_ij)); 
			}
		System.out.println("edges counted = " + edges_counted);	
		
		System.out.println("======");
		System.out.println("total_e = " + total_e);
		System.out.println("logLK = " + ret);
		
		
		return ret;
	}
	
	////
	public static void writePart(Map<Integer, Integer> part, String part_file) throws IOException{

		List<List<Integer>> com = new ArrayList<List<Integer>>();
		int k = 0;
		for (int val : part.values())
			if (k < val)
				k = val;
		k += 1;
		for (int i = 0; i < k; i++)
			com.add(new ArrayList<Integer>());
		
		for (Map.Entry<Integer, Integer> entry : part.entrySet())
			com.get(entry.getValue()).add(entry.getKey());
		
		//
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));
		for (List<Integer> list:com){
			for (int u : list)
				bw.write(u + ",");
			bw.write("\n");
		}
		
		bw.close();
	}
	
	////
	public static Map<Integer, Integer> readNodeMap(String nodemap_file)  throws IOException{
		Map<Integer, Integer> nodemap = new HashMap<Integer, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(nodemap_file));
		//
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(",");
        	for (String item : items)
        		nodemap.put(Integer.parseInt(item), count);
        	
        	count += 1;
        	
		}
		
		//
		return nodemap;
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		// load graph
//		String dataname = "example";			// 
//		String dataname = "karate";			// (34, 78) 		ok
//		String dataname = "polbooks";		// (105, 441)		ok
//		String dataname = "polblogs";		// (1224,16715)		ok 	
//		String dataname = "as20graph";		// (6474,12572)		ok
//		String dataname = "wiki-Vote";		// (7115,100762)	ok
//		String dataname = "ca-HepPh";		// (12006,118489) 	ok
//		String dataname = "ca-AstroPh";		// (18771,198050) 	ok		1.56s
		// SYNTHETIC
//		String dataname = "network10k";		// (10000,34810) 	ok
//		String dataname = "network100k";	// (100000,232732) 	ok
//		String dataname = "network1m";		// (1000000,7817896) 	ok		(407s, Mem 4.3GB)
//		String dataname = "network100k2";	// (100000,501834) 	ok			(7s)
//		String dataname = "network300k";	// (300000,1519558) ok			(26s)
//		String dataname = "network300k2";	// (300000,1522207) ok			(26s)
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	17.8s
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	27.2s 			(new : 20s, Mem 1.5GB)
		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 670s, 2.2GB)	(new : 42s, Mem 2.7GB)
													//						
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
		String prefix = "";
	    int n_samples = 1;
	    int type = 1;	// 1:EdgeFlip, TmF, 2:LouvainDP
	    String sample_file = "";
	    
	    if(args.length >= 4){
			prefix = args[0];
			n_samples = Integer.parseInt(args[1]);
			type = Integer.parseInt(args[2]);
			sample_file = args[3];
	    }
	    
		System.out.println("n_samples = " + n_samples);
		System.out.println("type = " + type);
		System.out.println("sample_file = " + sample_file);
		
		int n = 0;
		int k = 0;
		if (sample_file.indexOf("polbooks") != -1)
			n = 105;
		if (sample_file.indexOf("as20graph") != -1)
			n = 6474;
		if (sample_file.indexOf("ca-AstroPh-wcc") != -1)
			n = 17903;
		if (sample_file.indexOf("amazon") != -1)
			n = 334863;
		if (sample_file.indexOf("dblp") != -1)
			n = 317080;
		if (sample_file.indexOf("youtube") != -1)
			n = 1134890;
		
		if (type == 1)
			for (int i = 0; i < n_samples; i++){
		    	System.out.println("sample i = " + i);
		    	
		    	String part_file = prefix + "_louvain/" + sample_file + "." + i + ".part";
		    	
				long start = System.currentTimeMillis();
				EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(prefix + "_sample/" + sample_file + "." + i);
				System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
				
				System.out.println("#nodes = " + G.V());
				System.out.println("#edges = " + G.E());
				
				// 
				Louvain lv = new Louvain();
				start = System.currentTimeMillis();
				Map<Integer, Integer> part = lv.best_partition(G, null);
				System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));
		
				Louvain.writePart(part, part_file);
				System.out.println("writePart - DONE");
		    	
			}
		else{	// LouvainDP
			k = Integer.parseInt(sample_file.substring(sample_file.lastIndexOf("_") + 1) );
			System.out.println("n = " + n + " k = " + k);

			for (int i = 0; i < n_samples; i++){
		    	System.out.println("sample i = " + i);
		    	
		    	String part_file = prefix + "_louvain/" + sample_file + "." + i + ".part";
		    	
				long start = System.currentTimeMillis();
				EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeListAndWeight(prefix + "_sample/" + sample_file + "." + i, n/k + 1);	// #nodes = n/k+1 (LouvainDP.randomEqualCommunity)
				System.out.println("readEdgeListAndWeight - DONE, elapsed " + (System.currentTimeMillis() - start));
				
				System.out.println("#nodes = " + G.V());
				System.out.println("#edges = " + G.E());
				System.out.println("totalWeight = " + G.totalWeight());
				
				// 
				Louvain lv = new Louvain();
				start = System.currentTimeMillis();
				Map<Integer, Integer> part = lv.best_partition(G, null);
				System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));
		
				// remap
				String nodemap_file = prefix + "_sample/" + sample_file + "_nodemap." + i;
				Map<Integer, Integer> nodemap = readNodeMap(nodemap_file);
				
				for (Map.Entry<Integer, Integer> entry : nodemap.entrySet()){
					nodemap.put(entry.getKey(), part.get(entry.getValue()));
				}
				
				Louvain.writePart(nodemap, part_file);
				System.out.println("writePart - DONE");
			}
		}
	    
	    
	    // Louvain on true graphs
//	    System.out.println("dataname = " + dataname);
//	    
//		String filename = prefix + "_data/" + dataname + ".gr";
//		String part_file = prefix + "_out/" + dataname + ".louvain";
//		
//		long start = System.currentTimeMillis();
//		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
//		
//		// TEST best_partition()
//		Louvain lv = new Louvain();
//		start = System.currentTimeMillis();
//		Map<Integer, Integer> part = lv.best_partition(G, null);
//		System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));
//
//		Louvain.writePart(part, part_file);
//		System.out.println("writePart - DONE");
		
	    
		// example.gr
////		int[] part1 = new int[]{0,0,0,1,1,2,2,3,3,3,3,2,2};
//		int[] part1 = new int[]{0,0,0,2,2,2,2,3,3,3,3,2,2};
//		System.out.println(CommunityMeasure.modularity(G, part1));
	}

}
