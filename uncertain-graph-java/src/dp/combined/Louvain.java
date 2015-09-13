/*
 * Sep 13, 2015
 * 	- convert from python (http://perso.crans.org/aynaud/communities/)
 * 
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

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;


//// weighted graph
class WGraph{
	
	
}

////
class Status{
	public Map<Integer, Integer> node2com = new HashMap<Integer, Integer>();
    int total_weight = 0;
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
    
    public void init(Grph graph, Map<Integer, Integer> part){
    	// Initialize the status of a graph with every node in one community
        int count = 0;
        
        this.node2com = new HashMap<Integer, Integer>();
        int total_weight = 0;
        this.internals = new HashMap<Integer, Double>();
        this.degrees = new HashMap<Integer, Integer>();
        this.gdegrees = new HashMap<Integer, Integer>();
        this.loops = new HashMap<Integer, Integer>();
        this.total_weight = graph.getNumberOfEdges();
        
        if (part == null){
        	for (IntCursor t : graph.getVertices()){
        		int node = t.value;
        		
        		this.node2com.put(node, count);
        		int deg = graph.getVertexDegree(node);
                this.degrees.put(count, deg);
                this.gdegrees.put(node, deg);
                if (graph.areEdgesAdjacent(node, node))
                	this.loops.put(node, 1);
                else
                	this.loops.put(node, 0);
                this.internals.put(count, (double)this.loops.get(node));
                count = count + 1;
        	}
        	
        }else{
        	for (IntCursor t : graph.getVertices()){
        		int node = t.value;
        		
        		int com = part.get(node);
        		this.node2com.put(node, com);
        		int deg = graph.getVertexDegree(node);
        		if (this.degrees.containsKey(com))
        			this.degrees.put(com, this.degrees.get(com) + deg);
        		else
        			this.degrees.put(com, deg);
        		this.gdegrees.put(node, deg);
                double inc = 0.0;
                for (IntCursor u : graph.getNeighbours(node)){
                	int neighbor = u.value;
                    int weight = graph.getEdgesConnecting(node, neighbor).size();
                    if (part.get(neighbor) == com)
                        if (neighbor == node)
                            inc += weight;
                        else
                            inc += weight / 2.;
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
	private Map<Integer, Integer> neighcom(int node, Grph graph, Status status){
	    // Compute the communities in the neighborood of node in the graph given
	    // with the decomposition node2com
		Map<Integer, Integer> weights = new HashMap<Integer, Integer>();
	    for (IntCursor u : graph.getNeighbours(node)){
        	int neighbor = u.value;
	        if (neighbor != node){
	        	int weight = graph.getEdgesConnecting(node, neighbor).size();
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
	    status.degrees.put(com, status.degrees.get(com)
	                                    - status.gdegrees.get(node) );
	    status.internals.put(com, status.internals.get(com) -
	                weight - status.loops.get(node) );
	    status.node2com.put(node, -1);
	}
	    
	////
	private void insert(int node, int com, int weight, Status status){
	    // Insert node into community and modify status"""
	    status.node2com.put(node, com);
	    status.degrees.put(com, status.degrees.get(com) +
	                                status.gdegrees.get(node) );
	    status.internals.put(com, status.internals.get(com) +
	                        weight + status.loops.get(node) );
	}
	                        
	////
	private double modularity(Status status){
		// Compute the modularity of the partition of the graph faslty using status precomputed
	    double links = status.total_weight;
	    double result = 0.0;
	    
	    Set<Integer> set = new HashSet<Integer>(status.node2com.values());
	    for (int community : set){
	        double in_degree = status.internals.get(community);
	        int degree = status.degrees.get(community);
	        if (links > 0)
	            result = result + in_degree / links - ((degree / (2.*links)) * (degree / (2.*links)));
	    }
	    //
	    return result;
	}
	
	//// call neighcom(), remove(), insert()
	private void one_level(Grph graph, Status status){
		// Compute one level of communities
	    boolean modif = true;
	    int nb_pass_done = 0;
	    double cur_mod = modularity(status);
	    double new_mod = cur_mod;

	    while (modif && nb_pass_done != PASS_MAX){
	    	cur_mod = new_mod;
	        modif = false;
	        nb_pass_done += 1;

	        for (IntCursor u : graph.getVertices()){
	        	int node = u.value;
	            int com_node = status.node2com.get(node);
	            double degc_totw = status.gdegrees.get(node) / (status.total_weight*2.);
	            
	            Map<Integer, Integer> neigh_communities = neighcom(node, graph, status);
	            
	            remove(node, com_node, neigh_communities.get(com_node), status);
	            
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
	            insert(node, best_com, neigh_communities.get(best_com), status);
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
	        if (! new_values.containsKey(key)){
	            new_values.put(value, count);
	            new_value = count;
	            count = count + 1;
	        }
	        ret.put(key, new_value);
	    }

	    return ret;
	}
	
	////
	private Grph induced_graph(Map<Integer, Integer> partition, Grph graph){
		Grph ret = new InMemoryGrph();
		
		IntSet nodeSet = new IntHashSet();
		for (int value:partition.values())
			nodeSet.add(value);
	    ret.addVertices(nodeSet);

	    for (VertexPair p : graph.getEdgePairs()){
	    	int node1 = p.first;
	    	int node2 = p.second;
	        int weight = graph.getEdgesConnecting(node1, node2).size();
	        int com1 = partition.get(node1);
	        int com2 = partition.get(node2);
	        int w_prec = ret.getEdgesConnecting(com1, com2).size();
	        		
	        for (int i = 0; i < w_prec + weight; i++)		// simulate WEIGHTED edges
	        	ret.addSimpleEdge(com1, com2, false);
	    }

	    return ret;
	}
	
	////
	public List<Map<Integer, Integer>> generate_dendrogram(Grph graph, Map<Integer, Integer> part_init){
		// Find communities in the graph and return the associated dendrogram
		//
	    // A dendrogram is a tree and each level is a partition of the graph nodes.  
	    // Level 0 is the first partition, which contains the smallest communities, and the best is len(dendrogram) - 1. 
	    // The higher the level is, the bigger are the communities
	    
		Map<Integer, Integer> part = new HashMap<Integer, Integer>();
		
		// special case, when there is no link
	    // the best partition is everyone in its community
	    if (graph.getNumberOfEdges() == 0){
	        for (IntCursor u : graph.getVertices())
	            part.put(u.value, u.value);
	        
	        List<Map<Integer, Integer>> list = new ArrayList<Map<Integer,Integer>>();
	        list.add(part);
	        return list;
	    }
	    
	    //
	    Grph current_graph = graph.clone();
	    Status status = new Status();
	    status.init(current_graph, part_init);
	    double mod = modularity(status);
	    
	    List<Map<Integer, Integer>> status_list = new ArrayList<Map<Integer,Integer>>();
	    
	    one_level(current_graph, status);
	    double new_mod = modularity(status);
	    Map<Integer, Integer> partition = renumber(status.node2com);
	    status_list.add(partition);
	    mod = new_mod;
	    
	    current_graph = induced_graph(partition, current_graph);
	    status.init(current_graph, null);
	    
	    while (true){
	    	one_level(current_graph, status);
	        new_mod = modularity(status);
	        if (new_mod - mod < MIN)
	            break;
	        partition = renumber(status.node2com);
	        status_list.add(partition);
	        mod = new_mod;
	        current_graph = induced_graph(partition, current_graph);
	        status.init(current_graph, null);
	    }
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
	public Map<Integer, Integer> best_partition(Grph graph, Map<Integer, Integer> partition){
		List<Map<Integer, Integer>> dendo = generate_dendrogram(graph, partition);
	    return partition_at_level(dendo, dendo.size() - 1 );
		
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) {
		// weighted Grph ?? YES, see toy example below
		Grph G = new InMemoryGrph();
		G.addNVertices(4);
		G.addSimpleEdge(0, 1, false);
		G.addSimpleEdge(1, 2, false);
		G.addSimpleEdge(1, 3, false);
		G.addSimpleEdge(2, 3, false);
//		G.addSimpleEdge(2, 3, false);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
//		System.out.println(G.getEdgesConnecting(2, 3).size()); // weight
		
		Louvain lv = new Louvain();
		Map<Integer, Integer> part = lv.best_partition(G, null);
		

	}

}
