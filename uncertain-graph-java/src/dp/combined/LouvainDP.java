/*
 * Sep 15, 2015
 * 	- use Louvain with differential privacy
 */

package dp.combined;

import hist.Int2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdk.management.resource.internal.TotalResourceContext;
import dp.DPUtil;
import toools.set.IntHashSet;
import toools.set.IntSet;
import algs4.Edge;
import algs4.EdgeWeightedGraph;



public class LouvainDP {
	
	
	////return: map of <node to community>
	public static Map<Integer, Integer> initEqualCommunity(int n, int k){
		Map<Integer, Integer> part_init = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < k-1; i++){
			for (int j = 0; j < n/k; j++)
				part_init.put(i*(n/k) + j, i);
		}
		// last partition
		int m = n - (k-1)*(n/k);
		for (int j = 0; j < m; j++)
			part_init.put((k-1)*(n/k) + j, k-1);
		
		//
		return part_init;
	}
	
	////
	public static EdgeWeightedGraph getGraphbyPartition(EdgeWeightedGraph graph, Map<Integer, Integer> part_init, int k){
		// create new weighted graph from part_init
		Map<Int2, Integer> comDict = new HashMap<Int2, Integer>();		// (u_com, v_com) -> num edges
		for (Edge e : graph.edges()){
			int u = e.either();
			int v = e.other(u);
			int u_com = part_init.get(u);
			int v_com = part_init.get(v);
			if (u_com > v_com){		// swap for comDict
				int temp = u_com;
				u_com = v_com;
				v_com = temp;
			}
			
			Int2 key = new Int2(u_com, v_com);
			int weight = 1;
			if (comDict.containsKey(key))
				comDict.put(key, comDict.get(key) + weight);
			else
				comDict.put(key,  weight);
		}
		//
		EdgeWeightedGraph graph_new = new EdgeWeightedGraph(k);
		for (Map.Entry<Int2, Integer> entry : comDict.entrySet()){
			Int2 key = entry.getKey();
			Integer value = entry.getValue();
//			Integer value = entry.getValue() + DPUtil.geometricMechanism(alpha);
			if (value < 0)
				value = 0;
			graph_new.addEdge(new Edge(key.val0, key.val1, value));
		}
		
		//
		return graph_new;
	}
	
	//// non-private
	public static Map<Integer, Integer> partitionEqual(EdgeWeightedGraph graph, int k){
		int n = graph.V();
		Map<Integer, Integer> part_init = initEqualCommunity(n, k);
		
		// 1. this formulation does not fix nodes at the beginning !
//		System.out.println("graph.V = " + graph.V() + " graph.E = " + graph.E());
//		System.out.println("graph.totalWeight = " + graph.totalWeight());
//		//
//		Louvain lv = new Louvain();
//		
//		//
//		return lv.best_partition(graph, part_init);
		
		// 2. fix nodes to partitions in part_init
		EdgeWeightedGraph graph_new = getGraphbyPartition(graph, part_init, k);
		Louvain lv = new Louvain();
		return lv.best_partition(graph_new, null);
	}
	
	//// private
	public static Map<Integer, Integer> partitionEqualPrivate(EdgeWeightedGraph graph, int k, double eps){
		int n = graph.V();
		Map<Integer, Integer> part_init = initEqualCommunity(n, k);
		
		EdgeWeightedGraph graph_new = getGraphbyPartition(graph, part_init, k);
		
		// add Laplace/geometric noise to graph_new edge weights
		double alpha = Math.exp(-eps);
		for (Edge e : graph_new.edges()){
			double value = e.weight() + DPUtil.geometricMechanism(alpha);
			if (value < 0)
				value = 0;
			e.setWeight(value);
		}

		System.out.println("graph_new.V = " + graph_new.V() + " graph_new.E = " + graph_new.E());
		System.out.println("graph_new.totalWeight = " + graph_new.totalWeight());
		//
		Louvain lv = new Louvain();
		
		//
		return lv.best_partition(graph_new, null);
		
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
//		String dataname = "ca-AstroPh";		// (18771,198050) 			
		// LARGE
		String dataname = "com_amazon_ungraph";		// (334863,925872)	
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 
													//						
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
		String prefix = "";
	    int n_samples = 1;
//	    double eps = 1.0;
	    
	    System.out.println("dataname = " + dataname);
	    
	    
		String filename = prefix + "_data/" + dataname + ".gr";
		
		long start = System.currentTimeMillis();
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		// TEST partitionEqual()
		int k = 0;
		
		for (double eps = 2.0; eps < 32.0; eps = eps*2){
			System.out.println("=========");
			System.out.println("eps = " + eps);
			for (k = 4; k < G.V()/10; k = k*2){
				System.out.println("k = " + k);
				
				start = System.currentTimeMillis();
//				Map<Integer, Integer> part = LouvainDP.partitionEqual(G, k);
//				System.out.println("partitionEqual - DONE, elapsed " + (System.currentTimeMillis() - start));
				
				Map<Integer, Integer> part = LouvainDP.partitionEqualPrivate(G, k, eps);
				System.out.println("partitionEqualPrivate - DONE, elapsed " + (System.currentTimeMillis() - start));
			}
		}
	}

}
