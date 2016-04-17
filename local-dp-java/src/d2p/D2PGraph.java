/* paper "D2P: Distance-Based Differential Privacy in Recommenders"
 * applied to graph anonymization
 * 
 * Apr 17, 2016
 * 	- add computeGroupByCosine(), computeGroupByDistance()
 * 
 */

package d2p;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import algs4.EdgeInt;
import algs4.EdgeIntGraph;

public class D2PGraph {

	
	////
	public static double cosine(EdgeIntGraph G, int u, int v){
		double ret = 0;
		
		int deg_u = G.degree(u);
		int deg_v = G.degree(v);
		
		int count = 0;
		if (deg_u < deg_v){
			for (int w : G.adj(u).keySet())
				if (G.areEdgesAdjacent(w, v))
					count ++;
		}else{
			for (int w : G.adj(v).keySet())
				if (G.areEdgesAdjacent(w, u))
					count ++;
		}
		
		ret = count/Math.sqrt(deg_u*deg_v);
		
		//
		return ret;
	}
	
	//// e-DP sample from 
	public static EdgeIntGraph d2pSample(EdgeIntGraph G, List<List<Integer>> group, int min_size, double eps){
		int n = G.V();
		EdgeIntGraph ret = new EdgeIntGraph(n);
		
		//
		double p = n/(n + (Math.exp(eps) - 1)*min_size);
		System.out.println("p = " + p);
		//
		Random random = new Random();
		for (int u = 0; u < n; u++){
			for (int v : G.adj(u).keySet()){
				
				int w = random.nextInt(n);	// replace v by any node
				
				double val = random.nextDouble();
				if (val > p){	// replace v by nodes in group[u]
					int u_size = group.get(u).size();
					w = group.get(u).get(random.nextInt(u_size));
					ret.addEdge(new EdgeInt(u, w, 0));
				}
				
				val = random.nextDouble();
				if (val < 0.5)
					ret.addEdge(new EdgeInt(u, w, 0));
				
			}
			
		}
		
		//
		return ret;
	}
	
	//// lambda in [0,1]
	public static void computeGroupByCosine(EdgeIntGraph G, double lambda){
		int n = G.V();
		List<List<Integer>> group = new ArrayList<List<Integer>>();
		
		for (int u = 0; u < n; u++){
			group.add(new ArrayList<Integer>());
			group.get(u).add(u);	// u always in group[u] 
			
			Map<Integer, Integer> checked = new HashMap<Integer, Integer>();
			
			for (int v : G.adj(u).keySet()){
				checked.put(v, 1);
				double val = cosine(G,u,v);
//				if (val >= lambda)
				if (1/val - 1 <= lambda)
					group.get(u).add(v);
				
				for (int w : G.adj(v).keySet())
					if (w != u && !G.areEdgesAdjacent(u, w) && !checked.containsKey(w)){
						checked.put(w, 1);
						val = cosine(G,u,w);
//						if (val >= lambda)
						if (1/val - 1 <= lambda)
							group.get(u).add(w);
					}
			}
		}
		
		// size of group
		int total_size = 0;
		int max_size = 0;
		int min_size = n;
		for (int u = 0; u < n; u++){
			int u_size = group.get(u).size();
			total_size += u_size;
			if (min_size > u_size)
				min_size = u_size;
			if (max_size < u_size)
				max_size = u_size;
		}
		
		System.out.println("total_size = " + total_size);
		System.out.println("max_size = " + max_size);
		System.out.println("min_size = " + min_size);
		
	}
	
	
	//// distance 2 (friend-of-friend)
	public static void computeGroupByDistance(EdgeIntGraph G, double eps, String sample_file) throws IOException{
		int n = G.V();
		List<List<Integer>> group = new ArrayList<List<Integer>>();
		
		for (int u = 0; u < n; u++){
			group.add(new ArrayList<Integer>());
			
			Map<Integer, Integer> checked = new HashMap<Integer, Integer>();
			
			for (int v : G.adj(u).keySet()){
				checked.put(v, 1);
				group.get(u).add(v);
				
				for (int w : G.adj(v).keySet())
					if (w != u && !G.areEdgesAdjacent(u, w) && !checked.containsKey(w)){
						checked.put(w, 1);
						group.get(u).add(w);
					}
			}
		}
		
		// size of group
		int total_size = 0;
		int max_size = 0;
		int min_size = n;
		for (int u = 0; u < n; u++){
			int u_size = group.get(u).size();
			total_size += u_size;
			if (min_size > u_size)
				min_size = u_size;
			if (max_size < u_size)
				max_size = u_size;
		}
		
		System.out.println("total_size = " + total_size);
		System.out.println("max_size = " + max_size);
		System.out.println("min_size = " + min_size);
		
		// call d2pSample()
//		EdgeIntGraph aG = d2pSample(G, group, min_size, eps);
//		EdgeIntGraph.writeGraph(aG, "_sample/" + sample_file);
		
	}
	
	
	
	// //////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		String prefix = "";
//		String dataname = "example";
//		String dataname = "pl_1000_5_01"; // diameter = 5
		String dataname = "pl_10000_5_01"; // diameter = 6, Dup: round=3
		////
//		String dataname = "com_dblp_ungraph"; 	// max.deg=343computeGroup (42s, 1.4GB),  lambda-0.01 (total_size = 32M, min_size = 1)
//		String dataname = "com_amazon_ungraph";
//		String dataname = "com_youtube_ungraph";	// OutOfMem
		
		String filename = prefix + "_data/" + dataname + ".gr";

		//
		System.out.println("dataname = " + dataname);
		long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t"); // "\t" or
																	// " "
		System.out.println("readGraph - DONE, elapsed "	+ (System.currentTimeMillis() - start));

		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		int max_deg = 0;
		for (int u = 0; u < G.V(); u++)
			if (max_deg < G.degree(u))
				max_deg = G.degree(u);
		System.out.println("max_deg = " + max_deg);

		// TEST cosine()
//		System.out.println(cosine(G,0,10));
		
		// TEST computeGroupByCosine(), computeGroupByDistance()
//		double lambda = 0.01;
		double lambda = 100;
		double eps = 2.0;
		start = System.currentTimeMillis();
//		computeGroupByCosine(G, lambda);
//		System.out.println("computeGroupByCosine - DONE, elapsed "	+ (System.currentTimeMillis() - start));
		
		String sample_file = dataname + "_d2pdist_" + String.format("%.1f", eps);
		computeGroupByDistance(G, eps, sample_file);
		System.out.println("computeGroupByDistance - DONE, elapsed "	+ (System.currentTimeMillis() - start));
		
	}
}
