/*
 * Jun 13, 2016
 * 	- use BitSet for each node
 * Jun 15
 * 	- add linkExchangeNoDupD2()
 * 	- factorize exchangeNoDup()
 */

package dsn;

import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AllPermission;
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

import dp.DegreeMetric;
import dp.PathMetric;
import dp.UtilityMeasure;
import algs4.EdgeInt;
import algs4.EdgeIntGraph;
import algs4.UnweightedGraph;


public class LinkExchange{

	////
	public static void printEdges(List<Int2> list){
		for(Int2 e:list)
			System.out.print("(" + e.val0 + "," + e.val1 + ") ");
		System.out.println();
	}
	
	////
	public static List<Integer> sampleLink(List<Integer> srcList, double alpha){
		List<Integer> ret = new ArrayList<Integer>();
		
		Random random = new Random();
		for (int i = 0; i < alpha*srcList.size(); i++){
			int k = random.nextInt(srcList.size());
			
			ret.add(srcList.get(k));
		}
		
		//
		return ret;
	}
	
	////
	public static List<Integer> sampleLinkNoDup(List<Integer> srcList, double alpha){
		if (alpha == 1.0)
			return srcList;
			
		List<Integer> ret = new ArrayList<Integer>();
		
		// WAY-1
//		Map<Integer, Integer> dup = new HashMap<Integer, Integer>();
//		Random random = new Random();
//		for (int i = 0; i < alpha*srcList.size(); i++){
//			int k = random.nextInt(srcList.size());
//			while(dup.containsKey(k) == true)
//				k = random.nextInt(srcList.size());
//			
//			dup.put(k, 1);
//			ret.add(srcList.get(k));
//		}
		// WAY-2 (shuffle and take alpha-fraction)
		Collections.shuffle(srcList);
		ret = srcList.subList(0, (int)(alpha*srcList.size()));
		
		//
		return ret;
		
	}
	
	//// return totalLink (NO dupLinks !)
	public static long countTrueFalseDupLinks(EdgeIntGraph G, Int2[] elist, List<BitSet> bitList, int n_edges, 
			int[] trueLinks, int[] falseLinks, int[] dupLinks){
		int n = G.V();
		
		long totalLink = 0;
		for (int u = 0; u < n; u++){
			// recover edge ids
			List<Integer> u_edges = new ArrayList<Integer>();
			for (int eid = 0; eid < n_edges; eid++)
				if (bitList.get(u).get(eid) == true)
					u_edges.add(eid);
			
			// 
			for (int eid : u_edges){
				Int2 p = elist[eid];
				if (G.areEdgesAdjacent(p.val0, p.val1))
					trueLinks[u] += 1;
				else
					falseLinks[u] += 1;
			}
			
			totalLink += u_edges.size();
		}
		//
		return totalLink;
	}
	
	////
	public static void saveLocalGraph(Int2[] elist, List<BitSet> bitList, int n_edges, int[] selectedNodes, String sample_file) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(sample_file));
		
		// number of selected nodes
		bw.write(selectedNodes.length + "\n");
		for (int u : selectedNodes){
			List<Integer> u_edges = new ArrayList<Integer>();
			for (int eid = 0; eid < n_edges; eid++)
				if (bitList.get(u).get(eid) == true)
					u_edges.add(eid);
			
			// node : #edges
			bw.write(u + "," + u_edges.size() + "\n");
			for (int eid : u_edges){
				Int2 e = elist[eid];
				bw.write(e.val0 + "\t" + e.val1 + "\n");
			}
		}
		bw.close();
		System.out.println("Written to sample_file.");
		
	}
	
	
	////
	public static void exchangeNoDup(EdgeIntGraph G, List<List<Int2>> links, int round, double alpha, double beta, 
			int nSample, String count_file, String sample_file) throws IOException{
		int n = G.V();
		
		long start = System.currentTimeMillis();
		//
		Map<Integer,Integer> map = new HashMap<Integer, Integer>();
		int n_edges = (int)Math.ceil((2+2*beta)*G.E());
		Int2[] elist = new Int2[n_edges];
		int eid = 0;
		List<BitSet> bitList = new ArrayList<BitSet>();
		
		for (int u = 0; u < n; u++){
			bitList.add(new BitSet(n_edges));
			
			for(Int2 e : links.get(u)){
				int key = e.val0*n + e.val1;
				if (!map.containsKey(key)){
					map.put(key, eid);
					elist[eid] = e;
					eid += 1;
				}
				bitList.get(u).set(map.get(key));	
			}
		}
		n_edges = eid;
		System.out.println("n_edges = " + n_edges);
		System.out.println("init - DONE");
		
		
		// loop
		for(int t = 1; t < round+1; t++){
			System.out.println("round = " + t);
			
			List<BitSet> exBitList = new ArrayList<BitSet>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exBitList.add((BitSet)bitList.get(u).clone());		// copy BitSet
			
			// for each node u
			for (int u = 0; u < n; u++){
				// recover edge ids from u's BitSet
				List<Integer> u_edges = new ArrayList<Integer>();
				for (eid = 0; eid < n_edges; eid++)
					if (bitList.get(u).get(eid) == true)
						u_edges.add(eid);
				
				// sample edges and send to v
				for (int v : G.adj(u).keySet()){
					List<Integer> listU = sampleLinkNoDup(u_edges, alpha);
					
					for (int id : listU)
						exBitList.get(v).set(id);
				}
				
			}
			// update bitList = exBitList
			for (int u = 0; u < n; u++)
				bitList.set(u, (BitSet)exBitList.get(u).clone());
			
			//
//			alpha = alpha * discount;
		}
		System.out.println("loop - DONE");
		System.out.println("exchangeNoDup - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		long totalLink = countTrueFalseDupLinks(G, elist, bitList, n_edges, trueLinks, falseLinks, dupLinks);
		System.out.println("countTrueFalseDupLinks - DONE, elapsed " + (System.currentTimeMillis() - start));
		System.out.println("totalLink = " + totalLink);
		
		// write to count_file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
		// sample nodes and save local graphs
		int[] deg = new int[n];
		
		for (int u = 0; u < n; u++)
			deg[u] = G.degree(u);
		int[] selectedNodes = LinkExchangeInt2.sampleNodeByDegree(deg, nSample);
		
		saveLocalGraph(elist, bitList, n_edges, selectedNodes, sample_file);
	}
	
	
	////
	public static void linkExchangeNoDup(EdgeIntGraph G, int round, double alpha, double beta, int nSample, String count_file, String sample_file) throws IOException{
		int n = G.V();
		System.out.println("round = " + round);
		System.out.println("alpha = " + alpha);
		System.out.println("beta = " + beta);
		System.out.println("count_file = " + count_file);
		
		//
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		
		long start = System.currentTimeMillis();
		
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
			links.add(temp);
			
			// normalize and sort
			LinkExchangeInt2.normalizeEdges(links.get(u));
			Collections.sort(links.get(u));
			
			// add false links (u,w)
			Random random = new Random();
			for (int i = 0; i < beta*G.degree(u); i++){
				int w = random.nextInt(n);
				
				while (true){
					if (w == u || G.areEdgesAdjacent(u, w) == true){
						w = random.nextInt(n);
						continue;
					}
					
					Int2 e = new Int2(u, w);
					boolean isNew = LinkExchangeInt2.insertLink(links.get(u), e);
					if (isNew == true)
						break;
					else
						w = random.nextInt(n);
					
				}
			}
		}
		
		// exchange loop
		System.out.println("linkExchangeNoDup");
		exchangeNoDup(G, links, round, alpha, beta, nSample, count_file, sample_file);
				
	}
	
	
	//// new param gamma
	public static void linkExchangeNoDupD2(EdgeIntGraph G, int round, double alpha, double beta, double gamma, int nSample, String count_file, String sample_file) throws IOException{
		int n = G.V();
		System.out.println("round = " + round);
		if (round < 1){
			System.out.println("round must be at least 1");
			return;
		}
		System.out.println("alpha = " + alpha);
		System.out.println("beta = " + beta);
		System.out.println("gamma = " + gamma);
		System.out.println("count_file = " + count_file);
		
		//
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		
		long start = System.currentTimeMillis();
		
		// 1 - initial stage t = 0
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
			links.add(temp);
			
			// normalize and sort
			LinkExchangeInt2.normalizeEdges(links.get(u));
			Collections.sort(links.get(u));
			
			// add false links (u,w)
			Random random = new Random();
			for (int i = 0; i < gamma*beta*G.degree(u); i++){
				int w = random.nextInt(n);
				
				while (true){
					if (w == u || G.areEdgesAdjacent(u, w) == true){
						w = random.nextInt(n);
						continue;
					}
					
					Int2 e = new Int2(u, w);
					boolean isNew = LinkExchangeInt2.insertLink(links.get(u), e);
					if (isNew == true)
						break;
					else
						w = random.nextInt(n);
				}
			}
		}
		
		// 
		List<List<Int2>> exLinks = new ArrayList<List<Int2>>();		// new links received at each node
		for (int u = 0; u < n; u++)
			exLinks.add(new ArrayList<Int2>());
		
		// for each pair of nodes (u,v)
		System.out.println("pre-round");
		for (EdgeInt e: G.edges()){
			int u = e.either();
			int v = e.other(u);
			
			List<Int2> listU = LinkExchangeInt2.sampleLinkNoDup(links.get(u), alpha);
			List<Int2> listV = LinkExchangeInt2.sampleLinkNoDup(links.get(v), alpha);
			
			//
			exLinks.get(u).addAll(listV);
			exLinks.get(v).addAll(listU);
			
		}
		// expand lists, do not accept duplicate links
		for (int u = 0; u < n; u++){
			for (Int2 e:exLinks.get(u))
				LinkExchangeInt2.insertLink(links.get(u), e);
		}
		
		// initialization of (1-gamma)*beta links
		for (int u = 0; u < n; u++){
			// u collects nodes not in N(u)
			List<Integer> d2nodes = new ArrayList<Integer>();
			for (Int2 e : links.get(u)){
				int v = e.val0;
				int w = e.val1;
				if (v != u && !G.areEdgesAdjacent(u, v))
					d2nodes.add(v);
				if (w != u && !G.areEdgesAdjacent(u, w))
					d2nodes.add(w);
			}
			// create (1-gamma)*beta links	
			Random random = new Random();
			for (int i = 0; i < (1-gamma)*beta*G.degree(u); i++){
				int w = random.nextInt(d2nodes.size());
				
				while (true){
					Int2 e = new Int2(u, w);
					boolean isNew = LinkExchangeInt2.insertLink(links.get(u), e);
					if (isNew == true)
						break;
					else
						w = random.nextInt(d2nodes.size());
				}
			}
		}
		
		
		// 2 - exchange loop
		System.out.println("linkExchangeNoDupD2");
		exchangeNoDup(G, links, round-1, alpha, beta, nSample, count_file, sample_file);	// round-1
		
	}
	
	////
	public static void readCountFile(String count_file) throws IOException{
		System.out.println("readCountFile, count_file = " + count_file);
		BufferedReader br = new BufferedReader(new FileReader(count_file));
		long totalLink = 0;
		long totalTrueLink = 0;
		while (true){
			String str = br.readLine();
			if (str == null)
				break;
			String[] items = str.split("\t");
			int trueLinks = Integer.parseInt(items[0]);
			int falseLinks = Integer.parseInt(items[1]);
			int dupLinks = Integer.parseInt(items[2]);
			
			totalLink += trueLinks + falseLinks + dupLinks;
			totalTrueLink += trueLinks;
		}
		System.out.println("readCountFile, totalLink = " + totalLink);
		System.out.println("readCountFile, totalTrueLink = " + totalTrueLink);
		
		br.close();
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "";

		// PL
//		String dataname = "pl_1000_5_01";		// diameter = 5
		String dataname = "pl_10000_5_01";		// diameter = 6,  	NoDup: round=3 (a=0.5, b=1.0, 2.1GB), 27+22s (PC), totalLink=245M (16.4%)
												//					NoDup: round=4 (a=0.5, b=1.0, 2.5GB), 94+58s (PC), totalLink=863M (57.6%)
												//					NoDup: round=5 (a=0.5, b=1.0, 2.9GB), 270+80s (PC), totalLink=1381M (92.2%)
												//					NoDup: round=2 (a=1.0, b=1.0, 1.8GB), 11+10s (PC),	totalLink=105M (7%)
												//					NoDup: round=3 (a=1.0, b=1.0, 2.5GB), 22+49s (PC),	totalLink=797M (53%)
												//					NoDup: round=4 (a=1.0, b=1.0, 2.9GB), 52+79s (PC),	totalLink=1470M (98%)
												//				NoDupD2: round=3 (a=0.5, b=1.0, g=0.5, 2.1GB), 20+21s (PC), totalLink=191M ()
//		String dataname = "pl_10000_10_01";
//		String dataname = "pl_10000_3_01";
		// BA
//		String dataname = "ba_1000_5";			// diameter = 5
//		String dataname = "ba_10000_5";			// diameter = 6, 	NoDup: round=3 (a=0.5, b=1.0, GB), s (Acer), totalLink=M 
		
		// ER
//		String dataname = "er_1000_001";		// diameter = 5
//		String dataname = "er_10000_0001";		// diameter = 7, 	NoDup: round=3 (a=0.5, b=1.0, GB), 
												//					NoDup: round=4 (a=1.0, b=1.0, 2.8GB), 44+73s (PC),	totalLink=1111M (73%) 
												//					NoDup: round=5 (a=1.0, b=1.0, 3.1GB), 70+91s (PC),	totalLink=1509M (99.8%)
//		String dataname = "er_10000_0002";
//		String dataname = "er_10000_00006";
		
		// SM
//		String dataname = "sm_1000_005_11";		// diameter = 9
//		String dataname = "sm_10000_005_11";	// diameter = 12, NoDup: round=3
												// 						round=5 
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
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	round=1 (11s), totalLink = 19354729
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 
		
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		
		//
		long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());

		// compute diameter
//		graphMetric(filename, G.V());
		
//		LinkExchangeInt2.computeTrueGraph(G, "_matlab/" + dataname + ".mat");
		
		//
		int round = 2; 		// flood
//		int round = 10; 	// gossip
		int step = 100000;	// gossip-async
		double alpha = 1.0;
		double beta = 1.0;
		double gamma = 0.0;
		double discount = 1.0;
		int nSample = 20;	// 20, 50, 100  number of local graphs written to file
		
		// TEST linkExchange()
//		String count_file = prefix + "_out/" + dataname + "-" + round + "_" + String.format("%.1f",alpha) + "_" + String.format("%.1f",beta) + ".cnt";
//		
//		linkExchange(G, round, alpha, beta, count_file);
		
		// TEST normalizeEdges(), insertLink()
//		List<Int2> list = new ArrayList<Int2>();
//		list.add(new Int2(2,3));
//		list.add(new Int2(3,4));
//		list.add(new Int2(4,2));
//		printEdges(list);
//		
//		normalizeEdges(list);
//		printEdges(list);
//		
//		Collections.sort(list);
//		printEdges(list);
//		
//		Int2 e1 = new Int2(2,5);
//		insertLink(list, e1);
//		printEdges(list);
		
		
		// TEST linkExchangeNoDup()
		String name = dataname + "-nodup-" + round + "_" + String.format("%.1f",alpha) + "_" + String.format("%.1f",beta) + "_" + nSample;
		String count_file = prefix + "_out/" + name + ".cnt";
		String sample_file = prefix + "_sample/" + name + ".out";
		String matlab_file = prefix + "_matlab/" + name + ".mat";
		String attack_file = prefix + "_matlab/" + name + "_attack.mat";
		System.out.println("count_file = " + count_file);
		
		//
		linkExchangeNoDup(G, round, alpha, beta, nSample, count_file, sample_file);
		
//		computeLocalGraph(sample_file, matlab_file, 10000);
		
//		attackLocalGraph(G, beta, sample_file, attack_file);
		
//		readCountFile("_out/pl_10000_5_01-nodup-3_1.0_1.0_1.0_20.cnt");
		
		
		// TEST linkExchangeNoDupD2() - distance-2
//		String name = dataname + "-nodup-d2-" + round + "_" + String.format("%.1f",alpha) + "_" + String.format("%.1f",beta) + "_" + String.format("%.1f",gamma) + "_" + nSample;
//		String count_file = prefix + "_out/" + name + ".cnt";
//		String sample_file = prefix + "_sample/" + name + ".out";
//		String matlab_file = prefix + "_matlab/" + name + ".mat";
//		String attack_file = prefix + "_matlab/" + name + "_attack.mat";
//		System.out.println("count_file = " + count_file);
//		
//		//
//		linkExchangeNoDupD2(G, round, alpha, beta, gamma, nSample, count_file, sample_file);
		
		
		//////////
		// TEST linkGossip()
//		String count_file = prefix + "_out/" + dataname + "-gossip-" + round + "_" + String.format("%.1f",alpha) + "_" + 
//				String.format("%.1f",beta) + "_" + String.format("%.1f",discount) + ".cnt";
//		
//		linkGossip(G, round, alpha, beta, count_file);
		
		// TEST linkGossipNoDup()
//		String count_file = prefix + "_out/" + dataname + "-gossip-nodup-" + round + "_" + String.format("%.1f",alpha) + "_" + 
//				String.format("%.1f",beta) + "_" + String.format("%.1f",discount) + ".cnt";
//		
//		linkGossipNoDup(G, round, alpha, beta, discount, count_file);

		//////////
		// TEST linkGossipAsync()
//		String count_file = prefix + "_out/" + dataname + "-gossip-async-" + step + "_" + String.format("%.1f",alpha) + "_" + 
//				String.format("%.1f",beta) + ".cnt";
//		
//		linkGossipAsync(G, step, alpha, beta, count_file);
		
		
	}

}
