/*
 * May 26, 2016
 * 	- add linkExchangeFalsePos(), linkExchangeFixedM(), extractFilterBlind(), extractFilterWithEdgeSet()
 * Jun 6
 * 	- count true/false/dup links in linkExchangeFalsePos()
 * Jun 7
 * 	- replace BloomFilter<Long> with BloomFilter
 * 	- add extractAllFiltersWithEdgeSet()
 */

package dsn;

import hist.Int2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bf.BloomFilter;
import bf.BloomFilterLong;
import dp.PathMetric;
import dp.UtilityMeasure;
import algs4.EdgeInt;
import algs4.EdgeIntGraph;
import algs4.UnweightedGraph;


public class LinkExchangeBloomFilter {

	////
	public static void printEdges(List<Int2> list){
		for(Int2 e:list)
			System.out.print("(" + e.val0 + "," + e.val1 + ") ");
		System.out.println();
	}
	
	////
	public static void writeDegreeSeq(EdgeIntGraph G, String deg_file) throws IOException{
		int n = G.V();
		BufferedWriter bw = new BufferedWriter(new FileWriter(deg_file));
		for (int u = 0; u < n; u++){
			bw.write(G.degree(u) + "\n");
		}
		bw.close();
		System.out.println("Written to deg_file.");
	}
	
	////
	public static void graphMetric(String graph_file, int n_nodes) throws IOException{
		UnweightedGraph aG = UnweightedGraph.readEdgeListWithNodes(graph_file, "\t", n_nodes);
		System.out.println("#nodes = " + aG.V());
		System.out.println("#edges = " + aG.E());
		
		PathMetric path = new PathMetric();
		double[] distance_dist;
		
//		distance_dist = UtilityMeasure.getDistanceDistr(aG, path);
//		
//		System.out.println("diameter = " + path.s_Diam);
		
		int max_deg = 0;
		int min_deg = aG.V();
		for (int u = 0; u < aG.V(); u++){
			if (max_deg < aG.degree(u))
				max_deg = aG.degree(u);
			if (min_deg > aG.degree(u))
				min_deg = aG.degree(u);
		}
		System.out.println("max_deg = " + max_deg);
		System.out.println("min_deg = " + min_deg);
		
		
	}
	
	////
	public static List<Int2> createFalseLink(EdgeIntGraph G, int u, double beta){
		List<Int2> ret = new ArrayList<Int2>();
		
		int n = G.V();
		//
		Random random = new Random();
		for (int i = 0; i < beta*G.degree(u); i++){
			int w = random.nextInt(n);
			while (G.areEdgesAdjacent(u, w))
				w = random.nextInt(n);
			
			ret.add(new Int2(u, w));
			
		}
		
		
		//
		return ret;
	}
	
	////
	public static List<Int2> sampleLink(List<Int2> srcList, double alpha){
		List<Int2> ret = new ArrayList<Int2>();
		
		Random random = new Random();
		for (int i = 0; i < alpha*srcList.size(); i++){
			int k = random.nextInt(srcList.size());
			
			ret.add(srcList.get(k));
			
		}
		
		//
		return ret;
		
	}
	
	////
	public static List<Int2> sampleLinkNoDup(List<Int2> srcList, double alpha){
		List<Int2> ret = new ArrayList<Int2>();
		
		Map<Integer, Integer> dup = new HashMap<Integer, Integer>();
		
		Random random = new Random();
		for (int i = 0; i < alpha*srcList.size(); i++){
			int k = random.nextInt(srcList.size());
			while(dup.containsKey(k) == true)
				k = random.nextInt(srcList.size());
			
			dup.put(k, 1);
			ret.add(srcList.get(k));
			
		}
		
		//
		return ret;
	}
	
	////
	public static void normalizeEdge(Int2 e){
		if (e.val0 > e.val1){
			int temp = e.val0;
			e.val0 = e.val1;
			e.val1 = temp;
		}
	}
	
	//// extract links from a bloomfiter
	public static List<Int2> extractFilterBlind(BloomFilterLong bf, int n){
		List<Int2> ret = new ArrayList<Int2>();
		
		for (long eid = 0; eid < n*n; eid++)
			if (bf.contains(eid)){
				int u = (int)(eid / n);
				int v = (int)(eid % n);
				ret.add(new Int2(u, v));
			}
		
		//
		return ret;
	}
	
	////extract links from a bloomfiter
	public static List<Int2> extractFilterWithEdgeSet(BloomFilterLong bf, int n, List<Int2> edges){
		List<Int2> ret = new ArrayList<Int2>();
		
		for (Int2 e : edges){
			normalizeEdge(e);
			long eid = e.val0 * n + e.val1;
			if (bf.contains(eid)){
				ret.add(e);
			}
		}
		//
		return ret;
	}
	
	////
	public static List<List<Int2>> extractAllFiltersWithEdgeSet(List<BloomFilterLong> filters, int n, List<Int2> edges){
		List<List<Int2>> ret = new ArrayList<List<Int2>>();
		for (int i = 0; i < n; i++)
			ret.add(new ArrayList<Int2>());
		
		for (Int2 e : edges){
			normalizeEdge(e);
			long eid = e.val0 * n + e.val1;
			
			boolean[] check = BloomFilterLong.containsListBF(eid, filters);
			for (int i = 0; i < n; i++)
				if (check[i] == true)
					ret.get(i).add(e);
		}
		//
		return ret;
	}
	
	//// alpha <= 1.0
	public static void linkExchangeFalsePos(EdgeIntGraph G, int round, double falsePositive, double alpha, double beta, String count_file) throws IOException{
		int n = G.V();
		int m = G.E();
		System.out.println("round = " + round);
		System.out.println("falsePositive = " + falsePositive);
		System.out.println("alpha = " + alpha);
		System.out.println("beta = " + beta);
		
		//
		BloomFilterLong bf = new BloomFilterLong(falsePositive, m);
		
		System.out.println("k is " + bf.getK());
        System.out.println("bitSetSize is " + bf.getBitSetSize());

		// Bloom filters at nodes
		List<BloomFilterLong> filters = new ArrayList<BloomFilterLong>();
		for (int u = 0; u < n; u++){
			filters.add(new BloomFilterLong(falsePositive, m));
		}
		
		
        // 
		long start = System.currentTimeMillis();
		List<Int2> allEdges = new ArrayList<Int2>();	// used in extractFilterWithEdgeSet() below
		allEdges.addAll(G.allEdges());
		
		// 1 - initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
			
			List<Int2> newLinks = createFalseLink(G, u, beta);
			temp.addAll(newLinks);
			
			allEdges.addAll(newLinks);		
			
			// hash links[u] to filters[u]
			for (Int2 e : temp){
				normalizeEdge(e);
				long eid = e.val0 * n + e.val1;
				filters.get(u).add(eid);
			}
		}
		System.out.println("allEdges.size = " + allEdges.size());
		
		// 2 - loop
		Random random = new Random();
		for(int t = 1; t < round+1; t++){
			List<BloomFilterLong> newFilters = new ArrayList<BloomFilterLong>();		// new links received at each node
			
			// for each node u
			for (int u = 0; u < n; u++){
				bf = new BloomFilterLong(falsePositive, m);
				bf.union(filters.get(u));			// set to u
				
				for (int v : G.adj(u).keySet())
//					bf.union(filters.get(v);
					bf.union(filters.get(v).removeBits(alpha, random));		// take union with v in N(u)
				
				//
				newFilters.add(bf);
			}
			
			for (int u = 0; u < n; u++)
				filters.set(u, newFilters.get(u));
		}
		
		System.out.println("linkExchangeFalsePos - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// 3 - extract (recover) edges from filters		
		start = System.currentTimeMillis();
		
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		// WAY-1
//		for (int u = 0; u < n; u++){
////			List<Int2> ret = extractFilterBlind(filters.get(u), n);
////			List<Int2> ret = extractFilterWithEdgeSet(filters.get(u), n, G.allEdges());
//			
//			List<Int2> ret = extractFilterWithEdgeSet(filters.get(u), n, allEdges);		// ret MAY contain duplicate falseLinks
//			
//			links.add(ret);
//		}
//		System.out.println("extractFilterWithEdgeSet - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// WAY-2
		links = extractAllFiltersWithEdgeSet(filters, n, allEdges);
		System.out.println("extractAllFiltersWithEdgeSet - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// 4 - count true/false/duplicate links and write to file
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		
		LinkExchange.countTrueFalseDupLinks(G, links, trueLinks, falseLinks, dupLinks);
		
		//
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
	}
	
	
	////alpha = 1.0
	public static void linkExchangeFixedM(EdgeIntGraph G, int round, double falsePositive, double beta, String count_file) throws IOException{
		int n = G.V();
		int m = G.E();
		System.out.println("round = " + round);
		System.out.println("falsePositive = " + falsePositive);
		System.out.println("beta = " + beta);
		
		int k = 4;	// num of hash functions
		int c = 10;	// bits per element	
		m = n/10; //(int)Math.ceil(Math.sqrt(m));
		
		//
		BloomFilterLong bf = new BloomFilterLong(c, m, k);
		
		System.out.println("k is " + bf.getK());
       System.out.println("bitSetSize is " + bf.getBitSetSize());

		// Bloom filters at nodes
		List<BloomFilterLong> filters = new ArrayList<BloomFilterLong>();
		for (int u = 0; u < n; u++){
			filters.add(new BloomFilterLong(c, m, k));
			
		}
		
		
       // 
       List<List<Int2>> links = new ArrayList<List<Int2>>();
		
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
			
			List<Int2> newLinks = createFalseLink(G, u, beta);
			temp.addAll(newLinks);
			
			links.add(temp);
			
			// hash links[u] to filters[u]
			for (Int2 e : temp){
				normalizeEdge(e);
				long eid = e.val0 * n + e.val1;
				filters.get(u).add(eid);
			}
				
		}
		
		// loop
		for(int t = 1; t < round+1; t++){
			List<BloomFilterLong> newFilters = new ArrayList<BloomFilterLong>();		// new links received at each node
			
			// for each node u
			for (int u = 0; u < n; u++){
				bf = new BloomFilterLong(c, m, k);
				bf.union(filters.get(u));			// set to u
				
				for (int v : G.adj(u).keySet())
					bf.union(filters.get(v));		// take union with v in N(u)
				
				//
				newFilters.add(bf);
			}
			
			for (int u = 0; u < n; u++)
				filters.set(u, newFilters.get(u));
		}
		
		System.out.println("linkExchangeFixedM - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		//
		for (int u = 0; u < 20; u++){
//			List<Int2> ret = extractFilterBlind(filters.get(u), n);
			List<Int2> ret = extractFilterWithEdgeSet(filters.get(u), n, G.allEdges());
			
			System.out.println(G.degree(u) + " " + ret.size());
		}
		
	}
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "";

		
//		String dataname = "pl_1000_5_01";		// diameter = 5
		String dataname = "pl_10000_5_01";		// diameter = 6, round=2  extractFilterWithEdgeSet(656s Acer, 527s PC)	extractAllFiltersWithEdgeSet (84s)
												//				 round=3  extractAllFiltersWithEdgeSet(209s PC, 8.8GB)
//		String dataname = "pl_100000_5_01";		// diameter = 6,
		
//		String dataname = "ba_1000_5";			// diameter = 5
//		String dataname = "ba_10000_5";			// diameter = 6, 
		
//		String dataname = "er_1000_001";		// diameter = 5
//		String dataname = "er_10000_0001";		// diameter = 7, round=2  extractFilterWithEdgeSet(519s PC, BloomFilterLong 401s), extractAllFiltersWithEdgeSet (60s PC)
												// 				 round=3  extractAllFiltersWithEdgeSet (124s PC)
		
//		String dataname = "sm_1000_005_11";		// diameter = 9
//		String dataname = "sm_10000_005_11";	// diameter = 12,
												// 				
		//
//		String dataname = "example";			// 	diameter = 5, 
//		String dataname = "karate";				// (34, 78)	diameter = 5
//		String dataname = "polbooks";			// (105, 441)			
//		String dataname = "polblogs";			// (1224,16715) 		
//		String dataname = "as20graph";			// (6474,12572)			
//		String dataname = "wiki-Vote";			// (7115,100762)		
//		String dataname = "ca-HepPh";			// (12006,118489) 		
//		String dataname = "ca-AstroPh";			// (18771,198050) 			
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 
		
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		
		//
		System.out.println("filename = " + filename);
		long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());

		// compute diameter
//		graphMetric(filename, G.V());
		
		
		//
		int round = 3; 		// flood
//		int round = 10; 	// gossip
		int step = 100000;	// gossip-async
		double alpha = 0.75;
		double discount = 1.0;
		double beta = 1.0;
		double falsePositive = 0.1;
		
		//
//		String deg_file = prefix + "_out/" + dataname + ".deg";
//		writeDegreeSeq(G, deg_file);
		
		// TEST BitSet
//		BitSet a = new BitSet(10);
//		System.out.println("a.length = " + a.length());
//		System.out.println("a.size = " + a.size());
//		System.out.println("a.cardinality = " + a.cardinality());
//		a.set(1);	
//		a.set(5);
//		System.out.println("a.length = " + a.length());
//		System.out.println("a.size = " + a.size());
//		System.out.println("a.cardinality = " + a.cardinality());
		
		// TEST linkExchange()
//		String count_file = prefix + "_out/" + dataname + "-bf-" + round + "_" + String.format("%.1f",beta) + ".cnt"; // no alpha
		String count_file = prefix + "_out/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + String.format("%.1f",beta) + ".cnt";
		
		System.out.println("count_file = " + count_file);
		
		// alpha = 1.0
		linkExchangeFalsePos(G, round, falsePositive, alpha, beta, count_file);
		
		
		
	}

}
