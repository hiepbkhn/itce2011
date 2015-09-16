/*
 * Sep 15, 2015
 * 	- use Louvain with differential privacy
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



public class LouvainDP {
	
	
	static final int PASS_MAX = -1;
	static final double MIN = 0.0000001;

	
	
	////return: map of <node to community>
	public static Map<Integer, Integer> initEqualCommunity(int n, int k){
		Map<Integer, Integer> part_init = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < k-1; i++){
			for (int j = 0; j < n/k; j++)
				part_init.put(i*(n/k) + j, i);
		}
		// last partition
		int m = n-(k-1)*(n/k);
		for (int j = 0; j < m; j++)
			part_init.put((k-1)*(n/k) + j, k-1);
		
		//
		return part_init;
	}
	
	////
	public static Map<Integer, Integer> partitionEqual(EdgeWeightedGraph graph, int k){
		int n = graph.V();
		Map<Integer, Integer> part_init = initEqualCommunity(n, k);
		
		//
		Louvain lv = new Louvain();
		
		//
		return lv.best_partition(graph, part_init);
		
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
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	17.8s
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	27.2s 			(new : 20s, Mem 1.5GB)
		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 670s, 2.2GB)	(new : 42s, Mem 2.7GB)
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
		
		// TEST partitionEqual()
		int k = 0;
		
		for (k = 4; k < G.V()/10; k = k*2){
			System.out.println("k = " + k);
			start = System.currentTimeMillis();
			Map<Integer, Integer> part = LouvainDP.partitionEqual(G, k);
			System.out.println("partitionEqual - DONE, elapsed " + (System.currentTimeMillis() - start));
		}

	}

}
