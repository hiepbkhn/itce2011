/*
 * Sep 15, 2015
 * 	- use Louvain with differential privacy
 * Sep 22
 * 	- add modularity()
 * 	- add initEqualCommunityFromFile()
 * Sep 28
 * 	- MCMC at the first level (use NodeSetLouvain.java)
 * 	- add louvainAfterFirstPass()
 * Oct 11
 * 	- COMMAND-LINE
 */

package dp.combined;

import hist.Int2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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



public class LouvainModDiv {
	
	
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
	
	////return: map of <node to community>
	public static Map<Integer, Integer> initEqualCommunityFromFile(int n, int size, String part_file) throws IOException{
		Map<Integer, Integer> part_init = new HashMap<Integer, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(part_file));
		
		int count = 0;
		int[] node_list = new int[n];
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split(",");
        	
        	for (int i = 0; i < items.length; i++){
        		int u = Integer.parseInt(items[i]);
        		node_list[count++] = u;
        	}
        		
		}
		
		br.close();
		//
		for (int u = 0; u < n; u++)
			part_init.put(u, u/size);
		//
		return part_init;
	}
	
	////
	public static double modularity(EdgeWeightedGraph graph, Map<Integer, Integer> part_init, int k){
		double mod = 0.0;
		double[] lc = new double[k];
		double[] dc = new double[k];
		
		//
		for (int u = 0; u < graph.V(); u++)
			dc[part_init.get(u)] += graph.degree(u);
		
		for (Edge e: graph.edges()){
			int u = e.either();
			int v = e.other(u);
			
			if (part_init.get(u) == part_init.get(v))
				lc[part_init.get(u)] += 1;
		}
		
		//
		int m = graph.E();
		for (int i = 0; i < k; i++)
			mod += lc[i]/m - (dc[i]/(2*m))*(dc[i]/(2*m));
		
		//
		return mod;
	}
				
	
	////
	public static EdgeWeightedGraph getGraphByPartition(EdgeWeightedGraph graph, Map<Integer, Integer> part_init){
		int k = 0;
		for (int val : part_init.values())
			if (k < val)
				k = val;
		k += 1;
		
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
			graph_new.addEdge(new Edge(key.val0, key.val1, value));
		}
		
		//
		return graph_new;
	}
	
	//// NON-PRIVATE
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
		EdgeWeightedGraph graph_new = getGraphByPartition(graph, part_init);
		Louvain lv = new Louvain();
		return lv.best_partition(graph_new, null);
	}
	
	//// PRIVATE
	public static Map<Integer, Integer> partitionEqualPrivate(EdgeWeightedGraph graph, int k, double eps){
		int n = graph.V();
		Map<Integer, Integer> part_init = initEqualCommunity(n, k);
		
		EdgeWeightedGraph graph_new = getGraphByPartition(graph, part_init);
		
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
	
	
	////////////////// Sep 28
	// PRIVATE
	public static void louvainAfterFirstPass(EdgeWeightedGraph graph, String part_file, double eps2) throws IOException{
		Map<Integer, Integer> part_init = new HashMap<Integer, Integer>();
		
		// 1. read part_file
		BufferedReader br = new BufferedReader(new FileReader(part_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	if (str.length() == 0)
        		continue;
        	
        	String[] items = str.split(",");
        	
        	for (int i = 0; i < items.length; i++){
        		int u = Integer.parseInt(items[i]);
        		part_init.put(u, count);
        	}
        		
        	//
        	count += 1;
		}
		
		br.close();
		
		// 2. build graph_new, add noise and run exact Louvain
		EdgeWeightedGraph graph_new = getGraphByPartition(graph, part_init);
		
		// add Laplace/geometric noise to graph_new edge weights
		double alpha = Math.exp(-eps2);
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
		
		lv.best_partition(graph_new, null);
		
	}
	
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		// load graph
//		String dataname = "karate";			// (34, 78)
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
//		String prefix = "";
//		int burn_factor = 20;
//	    int n_samples = 1;
//	    int num_part = 10;
//	    double eps = 30.0;
//	    
//	    System.out.println("dataname = " + dataname);
//		System.out.println("burn_factor = " + burn_factor + " n_samples = " + n_samples);
//		System.out.println("eps = " + eps);
//		System.out.println("num_part = " + num_part);
//	    
//		String filename = prefix + "_data/" + dataname + ".gr";
//		
//		long start = System.currentTimeMillis();
//		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
		
		// 1 - TEST partitionEqual()
//		int k = 0;
//		
//		for (double eps = 2.0; eps < 32.0; eps = eps*2){
//			System.out.println("=========");
//			System.out.println("eps = " + eps);
//			for (k = 4; k < G.V()/10; k = k*2){
//				System.out.println("k = " + k);
//				
//				start = System.currentTimeMillis();
////				Map<Integer, Integer> part = LouvainDP.partitionEqual(G, k);
////				System.out.println("partitionEqual - DONE, elapsed " + (System.currentTimeMillis() - start));
//				
//				Map<Integer, Integer> part = LouvainDP.partitionEqualPrivate(G, k, eps);
//				System.out.println("partitionEqualPrivate - DONE, elapsed " + (System.currentTimeMillis() - start));
//			}
//		}
		
		// TEST initEqualCommunityFromFile()
//		int size = 1;	// as20graph -> (size,mod) = 5:0.403, 2:0.471, 1:0.623
//		String part_file = "_out/as20graph.louvain";
//		Map<Integer, Integer> part = LouvainDP.initEqualCommunityFromFile(G.V(), size, part_file);
//		EdgeWeightedGraph graph_new = getGraphByPartition(G, part);
//		
//		Louvain lv = new Louvain();
//		lv.best_partition(graph_new, null);
		
		
		// 2 - TEST NodeSetLouvain + louvainAfterFirstPass()
//	    String[] dataname_list = new String[]{"com_amazon_ungraph", "com_dblp_ungraph", "com_youtube_ungraph"};
//	    double[][] eps_list = new double[][]{{5.0, 10.0, 20.0, 30.0, 50.0}, {5.0, 10.0, 20.0, 30.0, 50.0}, {10.0, 20.0, 30.0, 50.0, 80.0}};
//	    int[][] num_part_list = new int[][]{{5, 10, 20, 40, 80}, {5, 10, 20, 40, 80}, {5, 10, 20, 40, 80}};
//	    
//	    double[] eps2_list = new double[]{1.0, 2.0, 4.0};
//	    
//	    for (int i = 0; i < dataname_list.length; i++){
//	    	dataname = dataname_list[i];
//	    	
//	    	System.out.println("dataname = " + dataname);
//	    	
//	    	String filename = prefix + "_data/" + dataname + ".gr";
//			long start = System.currentTimeMillis();
//			EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
//			System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//			
//			System.out.println("#nodes = " + G.V());
//			System.out.println("#edges = " + G.E());	    	
//			
//	    	for (double eps : eps_list[i])
//	    		for (int num_part : num_part_list[i]){
//	    			
//	    			System.out.println("eps = " + eps);
//	    			System.out.println("num_part = " + num_part);	 
//	    			String part_file = prefix + "_out/" + dataname + "_nodesetlv_" + burn_factor + "_" + num_part + "_" 
//	    							+ String.format("%.1f", eps) + ".part";
//	    			
//	    			//
////					NodeSetLouvain R = new NodeSetLouvain(G, num_part);
////					
////					start = System.currentTimeMillis();
////					R.partitionMod(G, eps, burn_factor*G.V(), n_samples, 0);
////					System.out.println("recursiveMod - DONE, elapsed " + (System.currentTimeMillis() - start));
////					
////					System.out.println("modularity = " + R.modularity(G.E()));
////					
////					R.writePart(part_file);
//					
//					//
//	    			for (double eps2 : eps2_list){
//	    				
//	    				LouvainDP.louvainAfterFirstPass(G, part_file, eps2);
//	    			}
//	    			
//	    	}
//	    }
	    
	    
	    ///////////
	    // 3 - TEST NodeSetLouvain (new: k-ary tree)
		// as20graph 		:	eps=5  ratio=2						 (3,4,0.113), (2,7,0.111)
		//						eps=10 ratio=2						 (3,4,0.270), (2,7,0.245)
		//						eps=20 ratio=2						 (3,4,0.324), (2,7,0.313), (5,3,0.336)
		// ca-AstroPh		:	eps=10 ratio=1						 (3,3,0.)
		//						eps=10 ratio=2						 (3,3,0.498), (3,4,0.485)
		//						eps=15 ratio=2						 (3,6,0.496)
		//						eps=20 ratio=2						 (3,6,0.521)
		// com_amazon_ungraph:	eps=10 ratio=2 (k, max_level, mod) = (3,6,0.451), (4,5,0.461), (2,10,0.389), (10,3,0.)
		//						eps=20 ratio=2 (k, max_level, mod) = (6,3,0.588), (5,4,0.586), (4,5,0.), (3,6,0.587), (10,3,0.512), (20,2,0.366)
		//						eps=20 ratio=1						 (5,4,0.)
		//						eps=30 ratio=2						 (6,3,0.613), (3,7,0.655) 44s 
		// com_dblp_ungraph:	eps=10 ratio=2 (k, max_level, mod) = (3,6,0.437), (2,10,0.416)
		//						eps=20 ratio=2 (k, max_level, mod) = (5,4,0.546), (6,3,0.549), (4,5,0.), (3,6,0.542), (2,10,0.521)
		//						eps=20 ratio=1						 (5,4,0.437)
		//						eps=30 ratio=2						 (3,7,0.595) 40s 		
		// com_youtube_ungraph	eps=10 ratio=2						 (3,7,0.339)
		//						eps=20 ratio=2						 (3,7,0.415) 150s
		//						eps=30 ratio=2						 (3,7,0.434) LouvainAfter: 0.451 
		//						eps=40 ratio=2						 (3,7,0.462)
	    // COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
 		String prefix = "";
 		int n_samples = 1;
 		int burn_factor = 20;
 		int limit_size = 40;		// at least 4*lower_size
 		int lower_size = 10;		// at least 2
 		int max_level = 4;
 		double eps1 = 20.0;	// 1, 10, 50, 80, 100 for polbooks: interesting prob values and final results
 		double ratio = 2.0; // 1.26 = 2^(1/3)
 		int k = 5;
 		double eps_mod = 0.1;		// epsilon used in bestCut()
 		
 		if(args.length >= 4){
 			prefix = args[0];
 			dataname = args[1];
 			n_samples = Integer.parseInt(args[2]);
 			burn_factor = Integer.parseInt(args[3]);
 		}
 		if(args.length >= 5)
 			limit_size = Integer.parseInt(args[4]);
 		
 		System.out.println("dataname = " + dataname);
 		System.out.println("burn_factor = " + burn_factor + " n_samples = " + n_samples);
 		System.out.println("limit_size = " + limit_size);
 		System.out.println("lower_size = " + lower_size);
 		System.out.println("max_level = " + max_level);
 		System.out.println("eps1 = " + eps1);
 		System.out.println("ratio = " + ratio);
 		System.out.println("k = " + k);
 		
 		//
 		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
 		String part_file = prefix + "_out/" + dataname +"_nodesetlv2_" + burn_factor + "_" + limit_size + "_" + lower_size + "_" 
 						+ max_level + "_" + String.format("%.2f", ratio) + "_" + String.format("%.1f", eps1) + "_" + k + ".part";
 		String leaf_file = part_file.substring(0, part_file.length()-5) + ".leaf";
 		String best_file = part_file.substring(0, part_file.length()-5) + ".best";
 		
 		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
 		
 		System.out.println("#nodes = " + G.V());
 		System.out.println("#edges = " + G.E());
 		
 		//
 		NodeSetLouvain R = new NodeSetLouvain(G, k);
 		System.out.println("mod = " + R.modularity(G.E()));
 		
 		// TEST recursiveMod()
 		for (int i = 0; i < n_samples; i++){
 			System.out.println("sample i = " + i);
 			
 			long start = System.currentTimeMillis();
 			NodeSetLouvain root_set = NodeSetLouvain.recursiveMod(G, eps1, burn_factor, limit_size, lower_size, max_level, ratio, k);	
 			System.out.println("recursiveMod - DONE, elapsed " + (System.currentTimeMillis() - start));
 			
 			
 			NodeSetLouvain.printSetIds(root_set, G.E());
 			System.out.println("final modularity = " + root_set.modularityAll(G.E()));
 			
 			// test call of louvainAfterFirstPass() on leaf nodes
 			NodeSetLouvain.writeLeaf(root_set, leaf_file);
 			System.out.println("writeLeaf - DONE");
 			
 			LouvainModDiv.louvainAfterFirstPass(G, leaf_file, 2.0);
 			
 			//
 			List<NodeSetLouvain> best_cut = NodeSetLouvain.bestCut(root_set, G.E(), eps_mod);
 			System.out.println("best_cut.size = " + best_cut.size());
 			NodeSetLouvain.writeBestCut(best_cut, best_file);
 			
 			//
 			int level = 2;
 			NodeSetLouvain.writeLevel(root_set, part_file + level, level);
 			level = 3;
 			NodeSetLouvain.writeLevel(root_set, part_file + level, level);
 			System.out.println("writeLevel - DONE");
 			
 		}
		
		
	    
	}

}