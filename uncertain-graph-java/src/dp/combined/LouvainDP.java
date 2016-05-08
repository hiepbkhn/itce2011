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
 * - randomEqualCommunity(), genEqualPrivate()	
 * - COMMAND-LINE
 * Oct 16
 * - fix genEqualPrivate() using full Filter technique of Cormode (ICDT'12)	
 * Mar 10, 2016
 * - add genEqualPrivateLaplace()
 * May 8
 * - rename louvainAfterFirstPass() -> louvainGivenPartition()
 */

package dp.combined;

import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jdk.management.resource.internal.TotalResourceContext;
import dp.DPUtil;
import toools.set.IntHashSet;
import toools.set.IntSet;
import algs4.Edge;
import algs4.EdgeWeightedGraph;



public class LouvainDP {
	
	
	////return: map of <node to community>
	// k: number of supernodes
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
	
	// k: size of supernode, not the number of supernodes !
	public static Map<Integer, Integer> randomEqualCommunity(int n, int k){
		// random permutation
		List<Integer> id = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
			id.add(i);
		Collections.shuffle(id);	
		
		//
		Map<Integer, Integer> part_init = new HashMap<Integer, Integer>();
		
		for (int j = 0; j < n/k; j++)
			for (int i = 0; i < k; i++){
				part_init.put(id.get(j*k + i), j);
			}
		// last partition
		if (n > (n/k)*k)
			for (int i = (n/k)*k; i < n; i++){
				part_init.put(id.get(i), n/k);
			}
		
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
	
	////
	public static void writeNodeMap(Map<Integer, Integer> part, String filename) throws IOException{
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		int num_part = 0;
		for (Map.Entry<Integer, Integer> entry : part.entrySet()){
			int u = entry.getKey();
			int k = entry.getValue();
			if (!map.containsKey(k))
				map.put(k, new ArrayList<Integer>());
				
			map.get(k).add(u);
			
			if (num_part < k)
				num_part = k;
		}
		num_part += 1;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
    	for (int k = 0; k < num_part; k++){
    		List<Integer> list = map.get(k);
    		for (int u : list)
    			bw.write(u + ",");
    		bw.write("\n");
    	}
    	
    	bw.close();
		
	}
	
	//// WRONG ! we must add noise to all edges (zero-cells, nonzero-cells) of graph_new 
//	public static void genEqualPrivate(EdgeWeightedGraph graph, int k, double eps, String sample_file, String nodemap_file) throws IOException{
//		int n = graph.V();
//		Map<Integer, Integer> part_init = randomEqualCommunity(n, k);
//		
//		EdgeWeightedGraph graph_new = getGraphByPartition(graph, part_init);
//		
//		// add Laplace/geometric noise to graph_new edge weights
//		double alpha = Math.exp(-eps);
//		for (Edge e : graph_new.edges()){
//			double value = e.weight() + DPUtil.geometricMechanism(alpha);
//			if (value < 0)
//				value = 0;
//			e.setWeight(value);
//		}
//
//		System.out.println("graph_new.V = " + graph_new.V() + " graph_new.E = " + graph_new.E());
//		System.out.println("graph_new.totalWeight = " + graph_new.totalWeight());
//		
//		// write to sample_file (G1)
//		EdgeWeightedGraph.writeGraph(graph_new, sample_file);
//		
//		// write to nodemap_file
//		writeNodeMap(part_init, nodemap_file);
//		
//		
//	}
	
	//// FIXED Oct 16, 2015
	public static void genEqualPrivate(EdgeWeightedGraph graph, int k, double eps, String sample_file, String nodemap_file) throws IOException{
		int n = graph.V();
		Map<Integer, Integer> part_init = randomEqualCommunity(n, k);
		
		EdgeWeightedGraph graph_new = getGraphByPartition(graph, part_init);
		int n_new = graph_new.V();
		System.out.println("graph_new.V = " + graph_new.V() + " graph_new.E = " + graph_new.E());
		
		// compute theta
		int m_new = graph_new.E();
		double m = (double)n_new * (n_new + 1.0)/2;		// n + 1: consider all diagonal edges
		System.out.println("m = " + m);
		double s = m_new;			// fix s
		System.out.println("s before = " + s);
		double alpha = Math.exp(-eps);
		double theta = Math.ceil( (Math.log(m-m_new) - Math.log((1+alpha)*s)) / eps); // 1+alpha: ONE-SIDED, round up theta
		s = (m - m_new)*Math.pow(alpha, theta) /(1+alpha);	// recompute s
		System.out.println("s after = " + s);
		
		System.out.println("alpha = " + alpha);
		System.out.println("theta = " + theta);
		
		// add geometric noise to graph_new edge weights
		EdgeWeightedGraph graph_final = new EdgeWeightedGraph(n_new);		// FIXED May 8,2016: EdgeWeightedGraph(n) -> n_new
		System.out.println("graph_final.V = " + graph_final.V());
		
		for (Edge e : graph_new.edges()){
			double value = e.weight() + DPUtil.geometricMechanism(alpha);		// Geometric mechanism
			if (value >= theta){
				e.setWeight(value);
				graph_final.addEdge(e);
			}
		}
		System.out.println("BEFORE: graph_final.E = " + graph_final.E());
		
		// sample s zero-cells
		Random random = new Random();
		double EPS = 0.000001;
		for (int i = 0; i < s; i++){
			int u = random.nextInt(n_new);
			int v = random.nextInt(n_new);
			if (graph_new.getEdge(u, v) == null & graph_final.getEdge(u, v) == null){
				// sample weight from Pr[X <= x] = 1 - alpha^(x-theta+1)	(ONE-SIDED)
				double r = random.nextDouble();	// r in (0,1)
				if (r > 1 - EPS)
					r = 1 - EPS;
				int weight = (int)(Math.log(1-r)/(-eps) + theta - 1);		// int
				if (weight > 0){
					Edge e = new Edge(u, v, weight);
					graph_final.addEdge(e);
				}
			}
		}
		

		System.out.println("AFTER: graph_final.E = " + graph_final.E());
		System.out.println("graph_final.totalWeight = " + graph_final.totalWeight());
		
		// write to sample_file (G1)
		EdgeWeightedGraph.writeGraph(graph_final, sample_file);
		
		// write to nodemap_file
		writeNodeMap(part_init, nodemap_file);
		
		
	}	
	
	////
	public static void genEqualPrivateLaplace(EdgeWeightedGraph graph, int k, double eps, String sample_file, String nodemap_file) throws IOException{
		int n = graph.V();
		Map<Integer, Integer> part_init = randomEqualCommunity(n, k);
		
		EdgeWeightedGraph graph_new = getGraphByPartition(graph, part_init);
		int n_new = graph_new.V();
		System.out.println("graph_new.V = " + graph_new.V() + " graph_new.E = " + graph_new.E());
		
		// compute theta
		int m_new = graph_new.E();
		double m = (double)n_new * (n_new + 1.0)/2;		// n + 1: consider all diagonal edges
		System.out.println("m = " + m);
		double s = m_new;			// fix s
		System.out.println("s = " + s);
		double theta = Math.log((m-m_new)/(2*s)) / eps; // 1+alpha: ONE-SIDED, round up theta
		
		System.out.println("theta = " + theta);
		
		// add geometric noise to graph_new edge weights
		EdgeWeightedGraph graph_final = new EdgeWeightedGraph(n_new);		// FIXED May 8,2016: EdgeWeightedGraph(n) -> n_new
		System.out.println("graph_final.V = " + graph_final.V());
		
		for (Edge e : graph_new.edges()){
			double value = e.weight() + DPUtil.laplaceMechanism(eps);		// Laplace mechanism
			if (value >= theta){
				e.setWeight(value);
				graph_final.addEdge(e);
			}
		}
		System.out.println("BEFORE: graph_final.E = " + graph_final.E());
		
		// sample s zero-cells
		Random random = new Random();
		double EPS = 0.000001;
		for (int i = 0; i < s; i++){
			int u = random.nextInt(n_new);
			int v = random.nextInt(n_new);
			if (graph_new.getEdge(u, v) == null & graph_final.getEdge(u, v) == null){
				// sample weight from Pr[X <= x] = 1 - alpha^(x-theta+1)	(ONE-SIDED)
				double r = random.nextDouble();	// r in (0,1)
				if (r > 1 - EPS)
					r = 1 - EPS;
				double weight = Math.log(1-r)/(-eps) + theta;
				if (weight > 0){
					Edge e = new Edge(u, v, weight);
					graph_final.addEdge(e);
				}
			}
		}
		

		System.out.println("AFTER: graph_final.E = " + graph_final.E());
		System.out.println("graph_final.totalWeight = " + graph_final.totalWeight());
		
		// write to sample_file (G1)
		EdgeWeightedGraph.writeGraph(graph_final, sample_file);
		
		// write to nodemap_file
		writeNodeMap(part_init, nodemap_file);
		
		
	}	
	
	////////////////// Sep 28 --> fixed like genEqualPrivate()
	// PRIVATE
	public static void louvainGivenPartition(EdgeWeightedGraph graph, String part_file, double eps) throws IOException{
		Map<Integer, Integer> part_init = new HashMap<Integer, Integer>();
		int n = graph.V();
		
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
		
		// 3. high-pass filter (Cormode et al. 2012)
		int n_new = graph_new.V();
		// compute theta
		int m_new = graph_new.E();
		double m = (double)n_new * (n_new + 1.0)/2;		// n + 1: consider all diagonal edges
		System.out.println("m = " + m);
		double s = m_new;			// fix s
		System.out.println("s before = " + s);
		double alpha = Math.exp(-eps);
		double theta = Math.ceil( (Math.log(m-m_new) - Math.log((1+alpha)*s)) / eps); // 1+alpha: ONE-SIDED, round up theta
		s = (m - m_new)*Math.pow(alpha, theta) /(1+alpha);	// recompute s
		System.out.println("s after = " + s);
		
		System.out.println("alpha = " + alpha);
		System.out.println("theta = " + theta);
		
		// add geometric noise to graph_new edge weights
		EdgeWeightedGraph graph_final = new EdgeWeightedGraph(n_new);		// FIXED May 8,2016: EdgeWeightedGraph(n) -> n_new
		System.out.println("graph_final.V = " + graph_final.V());
		
		for (Edge e : graph_new.edges()){
			double value = e.weight() + DPUtil.geometricMechanism(alpha);		// Geometric mechanism
			if (value >= theta){
				e.setWeight(value);
				graph_final.addEdge(e);
			}
		}
		System.out.println("BEFORE: graph_final.E = " + graph_final.E());
		
		// sample s zero-cells
		Random random = new Random();
		double EPS = 0.000001;
		for (int i = 0; i < s; i++){
			int u = random.nextInt(n_new);
			int v = random.nextInt(n_new);
			if (graph_new.getEdge(u, v) == null & graph_final.getEdge(u, v) == null){
				// sample weight from Pr[X <= x] = 1 - alpha^(x-theta+1)	(ONE-SIDED)
				double r = random.nextDouble();	// r in (0,1)
				if (r > 1 - EPS)
					r = 1 - EPS;
				int weight = (int)(Math.log(1-r)/(-eps) + theta - 1);		// int
				if (weight > 0){
					Edge e = new Edge(u, v, weight);
					graph_final.addEdge(e);
				}
			}
		}
		

		System.out.println("AFTER: graph_final.E = " + graph_final.E());
		System.out.println("graph_final.totalWeight = " + graph_final.totalWeight());

		// run exact Louvain on graph_final
		Louvain lv = new Louvain();
		
		Map<Integer, Integer> part = lv.best_partition(graph_final, null);
		
		// remap graph_final.part --> graph.int_part using part_init
		int remap_part[] = new int[n];
		for (Map.Entry<Integer, Integer> entry : part_init.entrySet()){
			remap_part[entry.getKey()] = part.get(entry.getValue());
		}
		
		double true_mod = CommunityMeasure.modularity(graph, remap_part); 
		System.out.println("true_mod = " + true_mod);
	}
	
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		System.out.println("LouvainDP");
		
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
//	    int n_samples = 1;
//	    double eps = 7.0;
//	    int k = 4;
//	    int type = 0;	// 0: Geometric, 1: Laplace
//	    
//	    if(args.length >= 5){
//			prefix = args[0];
//			dataname = args[1];
//			n_samples = Integer.parseInt(args[2]);
//			eps = Double.parseDouble(args[3]);
//			k = Integer.parseInt(args[4]);
//		}
//	    if(args.length >= 6){
//	    	type = Integer.parseInt(args[5]);
//	    }
//	    
//	    
//	    System.out.println("dataname = " + dataname);
//		System.out.println("n_samples = " + n_samples);
//		System.out.println("eps = " + eps);
//		System.out.println("k = " + k);
//		System.out.println("type = " + type);
//	    
//		String filename = prefix + "_data/" + dataname + ".gr";
//		String sample_file = prefix + "_sample/" + dataname + "_ldp_" + String.format("%.1f", eps) + "_" + k;
//		String nodemap_file = prefix + "_sample/" + dataname + "_ldp_" + String.format("%.1f", eps) + "_" + k + "_nodemap";
//		if (type == 1){
//			sample_file = prefix + "_sample/" + dataname + "_ldp_laplace_" + String.format("%.1f", eps) + "_" + k;
//			nodemap_file = prefix + "_sample/" + dataname + "_ldp_laplace_" + String.format("%.1f", eps) + "_" + k + "_nodemap";
//		}
//		
//		long start = System.currentTimeMillis();
//		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
//		
//		// 1 - TEST partitionEqual()
//		for (int i = 0; i < n_samples; i++){
//			System.out.println("sample i = " + i);
//				
//			start = System.currentTimeMillis();
//			if (type == 0){
//				LouvainDP.genEqualPrivate(G, k, eps, sample_file + "." + i, nodemap_file + "." + i);
//				System.out.println("genEqualPrivate - DONE, elapsed " + (System.currentTimeMillis() - start));
//			}
//			
//			if (type == 1){
//				LouvainDP.genEqualPrivateLaplace(G, k, eps, sample_file + "." + i, nodemap_file + "." + i);
//				System.out.println("genEqualPrivateLaplace - DONE, elapsed " + (System.currentTimeMillis() - start));
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
		
		// TEST louvainGivenPartition()
		double eps = 4.0;	// 0.25, 0.5, 1.0, 2.0
		System.out.println("dataname = " + dataname);
		System.out.println("eps = " + eps);
	    
		String filename = "_data/" + dataname + ".gr";
//		String part_file = "_out/" + dataname + ".f1";	// f1: full first round
		String part_file = "_out/" + dataname + ".s1";	// s1: only first step
		System.out.println("part_file = " + part_file);
		
		long start = System.currentTimeMillis();
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		start = System.currentTimeMillis();
		louvainGivenPartition(G, part_file, eps);
		System.out.println("louvainGivenPartition - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		
	    
	}

}
