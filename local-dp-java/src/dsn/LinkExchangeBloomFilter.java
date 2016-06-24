/*
 * May 26, 2016
 * 	- add linkExchangeFalsePos(), linkExchangeFixedM(), extractFilterBlind(), extractFilterWithEdgeSet()
 * Jun 6
 * 	- count true/false/dup links in linkExchangeFalsePos()
 * Jun 7
 * 	- replace BloomFilter<Long> with BloomFilter
 * 	- add extractAllFiltersWithEdgeSet()
 * 	- call removeBits() in LinkExchangeFalsePos() (for alpha < 1)
 * Jun 8
 * 	- add testArithmeticCoding()
 * Jun 12
 * 	- copy countTrueFalseDupLinks() from LinkExchange.java, use Int2
 * Jun 15
 * 	- update extractAllFiltersWithEdgeSet(), countTrueFalseDupLinks(), saveLocalGraph(), use Integer (eid)
 * Jun 24
 * 	- update testArithmeticCoding(): adaptive method runs slower but not better
 * 	- linkExchangeCompression(): test compression ratio of Bloom filters
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

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

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
	public static void saveLocalGraph(Int2[] elist, int n_edges, List<BloomFilterLong> filters, int n_nodes, int[] selectedNodes, String sample_file) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(sample_file));
		
		// number of selected nodes
		bw.write(selectedNodes.length + "\n");
		for (int u : selectedNodes){
			List<Integer> edgeIDs = new ArrayList<Integer>();
			
			for(int i = 0; i < n_edges; i++){
				Int2 e = elist[i];
				long eid = e.val0 * n_nodes + e.val1;
				
				if (filters.get(u).contains(eid))
					edgeIDs.add(i);
			}
			
			// node : #edges
			bw.write(u + "," + edgeIDs.size() + "\n");
			for (Integer eid : edgeIDs){
				Int2 e = elist[eid];
				bw.write(e.val0 + "\t" + e.val1 + "\n");
			}
			
		}
		bw.close();
		System.out.println("Written to sample_file.");
		
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
	
	//// return totalLink (NO dupLinks !)
	public static long countTrueFalseDupLinks(EdgeIntGraph G, Int2[] elist, int n_edges, List<BloomFilterLong> filters, int[] trueLinks, int[] falseLinks, int[] dupLinks){
		int n = G.V();
		
		long totalLink = 0;
		
		
		for(int i = 0; i < n_edges; i++){
			Int2 e = elist[i];
//			normalizeEdge(e);
			long eid = e.val0 * n + e.val1;
			
			boolean[] check = BloomFilterLong.containsListBF(eid, filters);
			for (int u = 0; u < n; u++)
				if (check[u] == true){
					if (G.areEdgesAdjacent(e.val0, e.val1))
						trueLinks[u] += 1;
					else
						falseLinks[u] += 1;
				}
			
		}
		for (int u = 0; u < n; u++)
			totalLink += trueLinks[u] + falseLinks[u];
		//
		return totalLink;
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
	public static List<List<Integer>> extractAllFiltersWithEdgeSet(List<BloomFilterLong> filters, int n, List<Int2> edges){
		List<List<Integer>> ret = new ArrayList<List<Integer>>();
		
		for (int u = 0; u < n; u++)
			ret.add(new ArrayList<Integer>());
		
		int n_edges = edges.size();
		System.out.println("n_edges = " + n_edges);
		
		for (int i = 0; i < n_edges; i++){
			Int2 e = edges.get(i);
			normalizeEdge(e);
			long eid = e.val0 * n + e.val1;
			
			boolean[] check = BloomFilterLong.containsListBF(eid, filters);
			for (int u = 0; u < n; u++)
				if (check[u] == true)
					ret.get(u).add(i);
		}
		//
		return ret;
	}
	
	//// alpha <= 1.0
	public static void linkExchangeFalsePos(EdgeIntGraph G, int round, double falsePositive, double alpha, double beta, 
			int nSample, String count_file, String sample_file, int iRun) throws IOException{
		int n = G.V();
		// choice of m 
		int m = G.E();
//		int m = (int)((1+beta)*G.E());
//		int m = G.E() / 2;
		
		System.out.println("round = " + round);
		System.out.println("falsePositive = " + falsePositive);
		System.out.println("alpha = " + alpha);
		System.out.println("beta = " + beta);
		
		//
		BloomFilterLong bf = new BloomFilterLong(falsePositive, m);
		
		System.out.println("k is " + bf.getK());
        System.out.println("bitSetSize is " + bf.getBitSetSize());
        int d = (int)( bf.getBitSet().cardinality() * (1 - Math.pow(alpha, 1.0/bf.getK())) );
        System.out.println("d = " + d);

		// Bloom filters at nodes
		List<BloomFilterLong> filters = new ArrayList<BloomFilterLong>();
		for (int u = 0; u < n; u++){
			filters.add(new BloomFilterLong(falsePositive, m));
		}
		
		
        // 
		long start = System.currentTimeMillis();
		int n_edges = (int)Math.ceil((2+2*beta)*G.E());
		Int2[] elist = new Int2[n_edges];
		int i = 0;
		for (Int2 e : G.allEdges())
			elist[i++] = e;
		
		// 1 - initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
			
			List<Int2> newLinks = createFalseLink(G, u, beta);
			temp.addAll(newLinks);
			
			for (Int2 e : newLinks)
				elist[i++] = e;		
			
			// hash links[u] to filters[u]
			for (Int2 e : temp){
				normalizeEdge(e);
				long eid = e.val0 * n + e.val1;
				filters.get(u).add(eid);
			}
		}
		n_edges = i;
		System.out.println("elist.length = " + elist.length);
		System.out.println("Initialization - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// 2 - loop
		Random random = new Random();
		for(int t = 1; t < round+1; t++){
			start = System.currentTimeMillis();
			System.out.println("round = " + t);
			
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
			
			System.out.println("exchangeNoDup - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			//
			start = System.currentTimeMillis();
			// 4 - count true/false/duplicate links and write to file
			int[] trueLinks = new int[n];
			int[] falseLinks = new int[n];
			int[] dupLinks = new int[n];
			
			long totalLink = countTrueFalseDupLinks(G, elist, n_edges, filters, trueLinks, falseLinks, dupLinks);
			System.out.println("countTrueFalseDupLinks - DONE, elapsed " + (System.currentTimeMillis() - start));
			System.out.println("totalLink = " + totalLink);
			
			//
			BufferedWriter bw = new BufferedWriter(new FileWriter(count_file + "-" + t + ".cnt" + "." + iRun));
			for (int u = 0; u < n; u++){
				bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
			}
			bw.close();
			System.out.println("Written to count_file.");
			
			// 5 - save local graphs
			start = System.currentTimeMillis();
			int[] deg = new int[n];
			
			for (int u = 0; u < n; u++)
				deg[u] = G.degree(u);
			int[] selectedNodes = LinkExchangeInt2.sampleNodeByDegree(deg, nSample);
			
			saveLocalGraph(elist, n_edges, filters, n, selectedNodes, sample_file + "-" + t + ".out" + "." + iRun);
			System.out.println("saveLocalGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		}
		
		System.out.println("linkExchangeFalsePos - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// 3 - extract (recover) edges from filters		
//		start = System.currentTimeMillis();
//		
//		List<List<Integer>> links = new ArrayList<List<Integer>>();
//		// WAY-1
////		for (int u = 0; u < n; u++){
//////			List<Int2> ret = extractFilterBlind(filters.get(u), n);
//////			List<Int2> ret = extractFilterWithEdgeSet(filters.get(u), n, G.allEdges());
////			
////			List<Int2> ret = extractFilterWithEdgeSet(filters.get(u), n, allEdges);		// ret MAY contain duplicate falseLinks
////			
////			links.add(ret);
////		}
////		System.out.println("extractFilterWithEdgeSet - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		// WAY-2
//		links = extractAllFiltersWithEdgeSet(filters, n, allEdges);
//		System.out.println("extractAllFiltersWithEdgeSet - DONE, elapsed " + (System.currentTimeMillis() - start));
		
	}
	
	//// alpha <= 1.0
	public static void linkExchangeCompression(EdgeIntGraph G, String dataname, String prefix, int round, double falsePositive, double alpha, double beta, int iRun) throws IOException{
		int n = G.V();
		// choice of m 
		int m = G.E();
//		int m = (int)((1+beta)*G.E());
//		int m = G.E() / 2;
		
		System.out.println("round = " + round);
		System.out.println("falsePositive = " + falsePositive);
		System.out.println("alpha = " + alpha);
		System.out.println("beta = " + beta);
		
		// for MATLAB
		int[] fullArr = new int[n * round];			// full sizes of Bloom filters
		int[] compressArr = new int[n * round];		// compressed sizes
		
		//
		BloomFilterLong bf = new BloomFilterLong(falsePositive, m);
		
		System.out.println("k is " + bf.getK());
		System.out.println("bitSetSize is " + bf.getBitSetSize());
		int d = (int)( bf.getBitSet().cardinality() * (1 - Math.pow(alpha, 1.0/bf.getK())) );
		System.out.println("d = " + d);

		// Bloom filters at nodes
		List<BloomFilterLong> filters = new ArrayList<BloomFilterLong>();
		for (int u = 0; u < n; u++){
			filters.add(new BloomFilterLong(falsePositive, m));
		}
		
		
		// 
		long start = System.currentTimeMillis();
		int n_edges = (int)Math.ceil((2+2*beta)*G.E());
		Int2[] elist = new Int2[n_edges];
		int i = 0;
		for (Int2 e : G.allEdges())
			elist[i++] = e;
		
		// 1 - initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
			
			List<Int2> newLinks = createFalseLink(G, u, beta);
			temp.addAll(newLinks);
			
			for (Int2 e : newLinks)
				elist[i++] = e;		
			
			// hash links[u] to filters[u]
			for (Int2 e : temp){
				normalizeEdge(e);
				long eid = e.val0 * n + e.val1;
				filters.get(u).add(eid);
			}
			
			// MATLAB
			fullArr[u] = filters.get(u).getBitSet().toByteArray().length;
			byte[] data = BloomFilterLong.compressBF(filters.get(u));
			compressArr[u] = data.length;
		}
		n_edges = i;
		System.out.println("elist.length = " + elist.length);
		System.out.println("Initialization - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// 2 - loop
		Random random = new Random();
		for(int t = 1; t < round+1; t++){
			start = System.currentTimeMillis();
			System.out.println("round = " + t);
			
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
			
			for (int u = 0; u < n; u++){
				filters.set(u, newFilters.get(u));
				
				if (t < round){
					fullArr[u + t*n] = filters.get(u).getBitSet().toByteArray().length;
					byte[] data = BloomFilterLong.compressBF(filters.get(u));
					compressArr[u + t*n] = data.length;
				}
			}
			
			System.out.println("exchangeNoDup - DONE, elapsed " + (System.currentTimeMillis() - start));
			
		}
		System.out.println("linkExchangeFalsePos - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// 3 - write to MATLAB
    	String matlab_file = prefix + "_compress/" + dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + 
				String.format("%.2f",beta) + "_" + falsePositive + "_compress." + iRun + ".mat";
		
    	MLInt32 fullA = new MLInt32("fullArr", fullArr, n);				// round columns
    	MLInt32 compressA = new MLInt32("compressArr", compressArr, n);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(fullA);
        towrite.add(compressA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
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
	
	////
	public static void testArithmeticCoding(double falsePositive, int n, int m) throws IOException{
		
		BloomFilterLong bf = new BloomFilterLong(falsePositive, m);
		System.out.println("k is " + bf.getK());
        System.out.println("bitSetSize is " + bf.getBitSetSize());
        
		// insert some edge ids
		Random random = new Random();
		int numE = m/10;
		System.out.println("numE = " + numE);
		long[] edges = new long[numE];
		for (int i = 0; i < numE; i++){
			edges[i] = random.nextLong() % (n*n);
			bf.add(edges[i]);
		}
		
		
		System.out.println("bf.bitSet: #bytes = " + bf.getBitSet().toByteArray().length);
		
		//// Fixed Frequency
		// compress
		byte[] data = BloomFilterLong.compressBF(bf);
		
		System.out.println("Fixed - compressed data: #bytes = " + data.length);
		
		// decompress
		bf = BloomFilterLong.decompressBF(data, falsePositive, m, numE);
		
		// test membership of edges
		for (long eid : edges){
			if (! bf.contains(eid))
				System.err.println("error ! " + eid);
		}
		System.out.println("decompress - DONE.");
		System.out.println("bf.bitSet: #bytes = " + bf.getBitSet().toByteArray().length);
		
		//// Adaptive
		// compress
		data = BloomFilterLong.compressBFAdaptive(bf);
		
		System.out.println("Adaptive - compressed data: #bytes = " + data.length);
		
		// decompress
		bf = BloomFilterLong.decompressBFAdaptive(data, falsePositive, m, numE);
		
		// test membership of edges
		for (long eid : edges){
			if (! bf.contains(eid))
				System.err.println("error ! " + eid);
		}
		System.out.println("decompress - DONE.");
		System.out.println("bf.bitSet: #bytes = " + bf.getBitSet().toByteArray().length);
	}
	
	////
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "";

		
//		String dataname = "pl_1000_5_01";		// diameter = 5
//		String dataname = "pl_10000_5_01";		// diameter = 6, round=2  extractFilterWithEdgeSet(656s Acer, 527s PC)	extractAllFiltersWithEdgeSet (84s)
												//				 round=3  extractAllFiltersWithEdgeSet(209s PC, 8.8GB)
												//  			 round=2 (a=1.0, b=1.0, GB), 4+98+14s (PC),	totalLink=107M (7%)
												//				 round=2 (a=1.0, b=1.0, 3.1GB), 4+80s (PC),	totalLink=107M (7%)
												//				 round=3 (a=1.0, b=1.0, 3.6GB), 5+128s (PC), totalLink=911M (%)
												//				 round=4 (a=1.0, b=1.0, 3.7GB), 7+136s (PC), totalLink=1486M (%)
//		String dataname = "pl_10000_10_01";		// diameter = 5, round=1 (a=1.0, b=1.0, 4.2GB), 6+117s (PC), totalLink=21M (%)
												//				 round=2 (a=1.0, b=1.0, 4.2GB), 12+219s (PC), totalLink=680M (%)
												//				 round=3 (a=1.0, b=1.0, 4.4GB), 18+276s (PC), totalLink=2909M (%)
												//				 round=4 (a=1.0, b=1.0, 4.6GB), 24+279s (PC), totalLink=2996M (%)			
		
//		String dataname = "pl_100000_5_01";		// diameter = 6,
		
//		String dataname = "ba_1000_5";			// diameter = 5
//		String dataname = "ba_10000_5";			// diameter = 6, 
		
//		String dataname = "er_1000_001";		// diameter = 5
//		String dataname = "er_10000_0001";		// diameter = 7, round=2  extractFilterWithEdgeSet(519s PC, BloomFilterLong 401s), extractAllFiltersWithEdgeSet (60s PC)
												// 				 round=3  extractAllFiltersWithEdgeSet (124s PC)
												//				 round=4 (a=1.0, b=1.0, 3.9GB), 7+135s (PC), totalLink=1246M (%)	
//		String dataname = "er_10000_0002";		//
												// diameter = 5	 round=4 (a=1.0, b=1.0, 5.1GB), 24+277s (PC), totalLink=2988M (%)	
		String dataname = "er_10000_00006";		// diameter = 10 round=4 (a=1.0, b=1.0, 2.4GB), 3+68s (PC), totalLink=188M (%)
												//				 round=6 (a=1.0, b=1.0, 2.8GB), 4+79s (PC), totalLink=886M (%)
		
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
		int round = 4; 		// flood
//		int round = 10; 	// gossip
		int step = 100000;	// gossip-async
		double alpha = 1.0;
		double discount = 1.0;
		double beta = 1.0;
		double falsePositive = 0.1;
		int nRun = 10;		// number of runs
		int nSample = 20;	// 20, 50, 100  number of local graphs written to file
		
		//
//		String deg_file = prefix + "_out/" + dataname + ".deg";
//		writeDegreeSeq(G, deg_file);
		

		// COMMAND-LINE <prefix> <dataname> <round> <alpha> <beta> <falsePositive> <nRun> <nSample>
//		if(args.length >= 8){
//			prefix = args[0];
//			dataname = args[1];
//			round = Integer.parseInt(args[2]);
//			alpha = Double.parseDouble(args[3]);
//			beta = Double.parseDouble(args[4]);
//			falsePositive = Double.parseDouble(args[5]);
//			nRun = Integer.parseInt(args[6]);
//			nSample = Integer.parseInt(args[7]);
//		}
//		
//		
////		String count_file = prefix + "_out/" + dataname + "-bf-" + round + "_" + String.format("%.1f",beta) + ".cnt"; // no alpha
//		String name = dataname + "-bf-" + round + "_" + String.format("%.2f",alpha) + "_" + String.format("%.2f",beta) + "_" + String.format("%.2f",falsePositive);
//		String count_file = prefix + "_out/" + name; // + ".cnt";
//		String sample_file = prefix + "_sample/" + name; // + ".out";
//		System.out.println("count_file = " + count_file);
//		
//		System.out.println("dataname = " + dataname);
//		System.out.println("round = " + round);
//		System.out.println("alpha = " + alpha);
//		System.out.println("beta = " + beta);
//		System.out.println("falsePositive = " + falsePositive);
//		System.out.println("nRun = " + nRun);
//		System.out.println("nSample = " + nSample);
//		
//		//
//		String filename = prefix + "_data/" + dataname + ".gr";
//		
//		//
//		System.out.println("filename = " + filename);
//		long start = System.currentTimeMillis();
//		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
//		
//		// TEST linkExchangeFalsePos()
//		for (int i = 0; i < nRun; i++){
//			System.out.println("run i = " + i);
//		
//			linkExchangeFalsePos(G, round, falsePositive, alpha, beta, nSample, count_file, sample_file, i);
//		}
		
		
		////////// BLOOM FILTER COMPRESSION
		// testArithmeticCoding()
////	testArithmeticCoding(falsePositive, G.V(), G.E());
//	testArithmeticCoding(falsePositive, 1000000, 10000000);
		
		// 
//		String[] dataname_list = new String[]{"pl_10000_5_01", "er_10000_0001"};
//		int[] diam_list = new int[]{6,7};
//		double[] alphaArr = new double[]{0.25, 0.5, 0.75, 1};
//		double[] betaArr = new double[]{0.5, 1};
//		double[] falsePositiveArr = new double[]{0.1};		// 0.25, 0.1, 0.01
//		
//		for (int k = 0; k < dataname_list.length; k++){
//			dataname = dataname_list[k];
//			round = diam_list[k];
//		
//			String filename = prefix + "_data/" + dataname + ".gr";
//		
//			//
//			System.out.println("filename = " + filename);
//			long start = System.currentTimeMillis();
//			EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
//			System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//			
//			System.out.println("#nodes = " + G.V());
//			System.out.println("#edges = " + G.E());
//			
//			// TEST linkExchangeFalsePos()
//			for (double alphaC : alphaArr)
//				for (double betaC : alphaArr)
//					for (int i = 0; i < nRun; i++){
//						System.out.println("run i = " + i);
//					
//						linkExchangeCompression(G, dataname, round, falsePositive, alphaC, betaC, i);
//					}
//		}
		
		// COMMAND-LINE <prefix> <dataname> <round> <alpha> <beta> <falsePositive> <nRun> 
		if(args.length >= 7){
			prefix = args[0];
			dataname = args[1];
			round = Integer.parseInt(args[2]);
			alpha = Double.parseDouble(args[3]);
			beta = Double.parseDouble(args[4]);
			falsePositive = Double.parseDouble(args[5]);
			nRun = Integer.parseInt(args[6]);
		}
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";

		System.out.println("filename = " + filename);
		long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		for (int i = 0; i < nRun; i++){
			System.out.println("run i = " + i);
		
			linkExchangeCompression(G, dataname, prefix, round, falsePositive, alpha, beta, i);
		}
		
	}

}
