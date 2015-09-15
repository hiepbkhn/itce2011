/*
 * Sep 14, 2015
 * 	- copy from LouvainGrph.java, but use algs4.EdgeWeightedGraph instead
 */

package dp.combined;

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
	public Map<Integer, Integer> node2com = new HashMap<Integer, Integer>();
    double total_weight = 0;
    public Map<Integer, Double> internals = new HashMap<Integer, Double>();
    public Map<Integer, Integer> degrees = new HashMap<Integer, Integer>();
    public Map<Integer, Integer> gdegrees = new HashMap<Integer, Integer>();
    public Map<Integer, Integer> loops = new HashMap<Integer, Integer>();
    
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
        new_status.total_weight = this.total_weight;
        //
        return new_status;
    }
    
    public void print(){
    	System.out.println("STATUS");
    	System.out.println("total_weight = " + total_weight + " modularity = " + Louvain.modularity(this));
    	System.out.println("node2com");
    	for (int key : this.node2com.keySet())
    		System.out.print(key + ":" + this.node2com.get(key) + ", ");
    	System.out.println();
    	System.out.println("internals");
    	for (int key : this.internals.keySet())
    		System.out.print(key + ":" + this.internals.get(key) + ", ");
    	System.out.println();
    	System.out.println("degrees");
    	for (int key : this.degrees.keySet())
    		System.out.print(key + ":" + this.degrees.get(key) + ", ");
    	System.out.println();
    	System.out.println("------------");
    }
    
    public void init(EdgeWeightedGraph graph, Map<Integer, Integer> part){
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
        	}
        	
        }
    }
	
}

public class Louvain {
	
	
	static final int PASS_MAX = -1;
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
	            result = result + in_degree / links - degree*degree / (4.*links *links);
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
	}
	
	////
	private Map<Integer, Integer> renumber(Map<Integer, Integer> dictionary){
		
		// Renumber the values of the dictionary from 0 to n
	    int count = 0;
	    Map<Integer, Integer> ret = Status.dictCopyInteger(dictionary);
	    Map<Integer, Integer> new_values = new HashMap<Integer, Integer>();

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
	    }

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
	    status.init(current_graph, part_init);
	    double mod = modularity(status);
	    //debug
//	    status.print();
//    	System.out.println(current_graph);
	    
	    List<Map<Integer, Integer>> status_list = new ArrayList<Map<Integer,Integer>>();
	    List<Status> status_all = new ArrayList<Status>();			// for logLK()
	    
	    one_level(current_graph, status);
	    double new_mod = modularity(status);
	    status_all.add(status.copy());
	    
	    //debug
//	    status.print();
//	    System.out.println(current_graph);
	    
	    Map<Integer, Integer> partition = renumber(status.node2com);
	    status_list.add(partition);
	    mod = new_mod;
	    
	    current_graph = induced_graph(partition, current_graph);
	    status.init(current_graph, null);
	    
	    
	    while (true){
	    	// debug
//	    	status.print();
//	    	System.out.println(current_graph);
	    	
	    	one_level(current_graph, status);
		    
	        new_mod = modularity(status);
	        status_all.add(status.copy());
	        
	        if (new_mod - mod < MIN)
	            break;
	        partition = renumber(status.node2com);
	        status_list.add(partition);
	        mod = new_mod;
	        current_graph = induced_graph(partition, current_graph);
	        status.init(current_graph, null);
	        
	    }
	    
	  	//debug
	    System.out.println("modularity = " + new_mod);
	    System.out.println("#communitites = " + current_graph.V());
	    
	    //
	    System.out.println("logLK = " + logLK(graph, status_all));
	    
	    
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
	public double logLK(EdgeWeightedGraph graph, List<Status> status_all){
		double ret = 0.0;
		
		for (Status status : status_all){	// at each level
			//count e_ij, ni, nj for all pairs (ci < cj)
		}
		return ret;
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		// load graph
//		String dataname = "karate";			// (105, 441)
//		String dataname = "polbooks";		// (105, 441)		
//		String dataname = "polblogs";		// (1224,16715) 	
//		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 			1.56s
		// LARGE
		String dataname = "com_amazon_ungraph";		// (334863,925872)	17.8s
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	27.2s 			(new : 20s, Mem 1.5GB)
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 670s, 2.2GB)	(new : 42s, Mem 2.7GB)
													//						
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
		String prefix = "";
	    int n_samples = 1;
	    
	    System.out.println("dataname = " + dataname);
	    
		String filename = prefix + "_data/" + dataname + ".gr";
		
		long start = System.currentTimeMillis();
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		// TEST best_partition()
		Louvain lv = new Louvain();
		start = System.currentTimeMillis();
		Map<Integer, Integer> part = lv.best_partition(G, null);
		System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));

	}

}
