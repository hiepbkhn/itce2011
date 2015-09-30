/*
 * Sep 29, 2015
 * 	- community measures: 
 * 		+ F1-score (EDBTw'15)
 * 		+ Normalized mutual information [NMI] (Lancichinetti)
 * 		+ inter/intra-cluster accuracy [A-inter, A-intra](ICML'07)
 * 		+ Adjusted Rand Index [ARI]	(KDD'07)
 * 	- modularitySet(Grph), modularitySet(EdgeWeightedGraph) for checking every modularity function
 */

package dp.combined;

import grph.Grph;
import grph.VertexPair;
import grph.io.EdgeListReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import algs4.EdgeWeightedGraph;
import toools.io.file.RegularFile;

public class CommunityMeasure {

	//// for testing (Grph)
	public static double modularitySet(Grph G, int[] node_set){
		double mod = 0.0;
		int m = G.getNumberOfEdges();
		System.out.println("m = " + m);
		
		double lc = 0;
		double dc = 0;
		
		for (int i = 0; i < node_set.length; i++)
			for (int j = i+1; j < node_set.length; j++)
				if (G.areVerticesAdjacent(node_set[i], node_set[j]))
					lc += 1;
		
		for (int i = 0; i < node_set.length; i++)
			dc += G.getVertexDegree(node_set[i]);
		
		System.out.println("lc = " + lc);
		System.out.println("dc = " + dc);
		
		mod = lc/m - (dc/(2*m))*(dc/(2*m));
		
		//
		return mod;
	}
	
	////for testing (EdgeWeightedGraph)
	public static double modularitySet(EdgeWeightedGraph G, int[] node_set){
		double mod = 0.0;
		int m = G.E();
		System.out.println("m = " + m);
		
		double lc = 0;
		double dc = 0;
		
		for (int i = 0; i < node_set.length; i++)
			for (int j = i+1; j < node_set.length; j++)
				if (G.areEdgesAdjacent(node_set[i], node_set[j]))
					lc += 1;
		
		for (int i = 0; i < node_set.length; i++)
			dc += G.degree(node_set[i]);
		
		System.out.println("lc = " + lc);
		System.out.println("dc = " + dc);
		
		mod = lc/m - (dc/(2*m))*(dc/(2*m));
		
		//
		return mod;
	}
	
	////
	public static double modularity(Grph G, int[] part){
		double mod = 0.0;
		
		int k = 0;
		for (int com : part)
			if (k < com)
				k = com;
		k += 1;
		
		double[] lc = new double[k];
		double[] dc = new double[k];
		
		//
		for (int u = 0; u < G.getNumberOfVertices(); u++)
			dc[part[u]] += G.getVertexDegree(u);
		
		for (VertexPair p : G.getEdgePairs()){
			int u = p.first;
			int v = p.second;
			
			if (part[u] == part[v])
				lc[part[u]] += 1;
		}
		
		//
		int m = G.getNumberOfEdges();
		for (int i = 0; i < k; i++)
			mod += lc[i]/m - (dc[i]/(2*m))*(dc[i]/(2*m));
		
		//
		return mod;
	}
	
	//// n : #nodes
	public static int[] readPart(String part_file, int n) throws IOException{
		int[] part = new int[n];
		
		BufferedReader br = new BufferedReader(new FileReader(part_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	if (str.length() ==0)
        		continue;
        	
        	String[] items = str.split(",");
        	
        	for (int i = 0; i < items.length; i++){
        		int u = Integer.parseInt(items[i]);
        		part[u] = count;
        	}
        	count += 1;
		}
		
		//
		return part;
	}
	
	///////////////////////////
	//// part_org: true partition (e.g. exact Louvain)
	public static double intraRatio(int[] part_org, int[] part, int num_pair){
		double aIntra = 0.0;
		int n = part_org.length;
		// init pairs
		int[][] pairs = new int[num_pair][2];
		Random random = new Random();
		int count = 0;
		int u;
		int v;
		while (count < num_pair){
			u = random.nextInt(n);
			v = random.nextInt(n);
			if (part_org[u] == part_org[v]){
				pairs[count][0] = u;
				pairs[count][1] = v;
				count += 1;
			}
				
		}
		
		// compute aIntra
		count = 0;
		for (int i = 0; i < pairs.length; i++){
			u = pairs[i][0];
			v = pairs[i][1];
			if (part[u] == part[v])
				count += 1;
		}
		System.out.println("count = " + count);
		
		aIntra = (double)count/pairs.length;
		
		//
		return aIntra;
	}
	
	
	////part_org: true partition (e.g. exact Louvain)
	public static double interRatio(int[] part_org, int[] part, int num_pair){
		double ainter = 0.0;
		int n = part_org.length;
		// init pairs
		int[][] pairs = new int[num_pair][2];
		Random random = new Random();
		int count = 0;
		int u;
		int v;
		while (count < num_pair){
			u = random.nextInt(n);
			v = random.nextInt(n);
			if (part_org[u] != part_org[v]){
				pairs[count][0] = u;
				pairs[count][1] = v;
				count += 1;
			}
				
		}
		
		// compute ainter
		count = 0;
		for (int i = 0; i < pairs.length; i++){
			u = pairs[i][0];
			v = pairs[i][1];
			if (part[u] != part[v])
				count += 1;
		}
		System.out.println("count = " + count);
		
		ainter = (double)count/pairs.length;
		
		//
		return ainter;
	}
	
	
	///////
	public static double nC2(int n){
		if (n == 0 || n == 1)
			return 1.0;
		return (n/2.0)*(n-1);
	}
	
	////
	public static double adjustedRandIndex(int[] part_org, int[] part){
		double ret = 0.0;
		int n = part_org.length;
		
		int k_org = 0;
		for (int com : part_org)
			if (k_org < com)
				k_org = com;
		k_org += 1;
		
		int k = 0;
		for (int com : part)
			if (k < com)
				k = com;
		k += 1;
		
		// n_ij, a, b
		int[][] n_ij = new int[k_org][k];
		int[] a = new int[k_org];
		int[] b = new int[k];
		
		for (int u = 0; u < n; u++){
			n_ij[part_org[u]][part[u]] += 1;
			a[part_org[u]] += 1;
			b[part[u]] += 1;
		}

		//
		double n2 = nC2(n);
		
		double index = 0.0;
		for (int i = 0; i < k_org; i++)
			for (int j = 0; j < k; j++)
				index += nC2(n_ij[i][j]);
		
		double expected_A = 0.0;
		for (int i = 0; i < k_org; i++)
			expected_A += nC2(a[i]);
		
		double expected_B = 0.0;
		for (int j = 0; j < k; j++)
			expected_B += nC2(b[j]);
		
		double expected_index = expected_A * expected_B/n2;
		double max_index = 0.5*(expected_A + expected_B);
		
		
		
		ret = (index - expected_index)/(max_index - expected_index);
		//
		return ret;
	}
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		int num_pair = 100000;
		
//		String filename = "_data/karate.gr";
//		String filename = "_data/polbooks.gr";
		
		int n_nodes = 6474;
		String filename = "_data/as20graph.gr";
		
//		int n_nodes = 18771;
//		String filename = "_data/ca-AstroPh.gr";
		
//		int n_nodes = 334863;
//		String filename = "_data/com_amazon_ungraph.gr";
		
		
		
		String path = "_out/";
		
		String louvain_file = "as20graph.louvain";	
//		String louvain_file = "ca-AstroPh.louvain";	
//		String louvain_file = "com_amazon_ungraph.louvain";						// aIntra = 1, aInter = 1, ARI = 1
		
		//
//		String compare_file = "as20graph_multioptlouvain_20_40_6_2.part";		// LouvainOpt
//		String compare_file = "as20graph_multioptlouvain_20_40_4_5.part";		// LouvainOpt
//		String compare_file = "as20graph_partoptlouvain_20_40_6_3.part";		// LouvainOpt
		String compare_file = "as20graph_partoptlouvain_20_40_5_4.part";		// LouvainOpt
//		String compare_file = "as20graph_moddivoptlouvain_30_50_10.part";		// ModDivisiveOpt
//		String compare_file = "as20graph_edgeflip_8.8-0.part";					// EdgeFlip
//		String compare_file = "as20graph_edgeflip_6.0-0.part";					// EdgeFlip
		
//		String compare_file = "ca-AstroPh_nodesetlv2_20_40_10_3_1.00_10.0_3.part";
//		String compare_file = "ca-AstroPh_nodesetlv2_20_40_10_3_2.00_10.0_3.part";
//		String compare_file = "ca-AstroPh_nodesetlv2_20_40_10_4_2.00_10.0_3.part";
//		String compare_file = "ca-AstroPh_temp.part";
		
//		String compare_file = "com_amazon_ungraph_moddivdp_20_40_10_8_2.00_20.0.part";
//		String compare_file = "com_amazon_ungraph_moddivopt_1_100_20_8.part";
//		String compare_file = "com_amazon_ungraph_nodesetlv_20_5_5.0.part";		//
//		String compare_file = "com_amazon_ungraph_nodesetlv_20_10_10.0.part";	//				
//		String compare_file = "com_amazon_ungraph_edgeflip_12.7-0.part";		// EdgeFlip		aIntra = 0.4181, aInter = 0.9935, ARI = 0.4416
//		String compare_file = "com_amazon_ungraph_filter_6.4-0.part";			// TmF
//		String compare_file = "com_amazon_ungraph_filter_12.7-0.part";			// TmF			aIntra = 0.2427, aInter = 0.9925, ARI = 0.2691
//		String compare_file = "com_amazon_ungraph_nodesetlv2_20_40_10_1_2.00_20.0_5.part";
		
		
		
		//
		EdgeListReader reader = new EdgeListReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		//
		int[] louvain_part = readPart(path + louvain_file, n_nodes);
		int[] compare_part = readPart(path + compare_file, n_nodes);
		
		System.out.println("louvain mod = " + modularity(G, louvain_part));
		System.out.println("compare mod = " + modularity(G, compare_part));
		
		//
		long start = System.currentTimeMillis();
		double aIntra = intraRatio(louvain_part, compare_part, num_pair);
		System.out.println("aIntra = " + aIntra);
		System.out.println("intraRatio - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		double aInter = interRatio(louvain_part, compare_part, num_pair);
		System.out.println("aInter = " + aInter);
		System.out.println("interRatio - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		double ARI = adjustedRandIndex(louvain_part, compare_part);
		System.out.println("ARI = " + ARI);
		
		// TEST modularitySet() ok
//		int[] node_set = new int[]{0,1,2,3,4,5,6,7,10,14,15,16,18,19,22,25,29,55,8,9,11,12,13,17,20,21,23,24,26,27,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,53,54,56,57};
//		System.out.println("modSet = " + modularitySet(G, node_set));
//		
////		node_set = new int[]{};
////		System.out.println("modSet = " + modularitySet(G, node_set));
//		
//		node_set = new int[]{51,52,58,64,65,67,68,69,85,103,104,28,30,31,59,60,61,62,63,66,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102};
//		System.out.println("modSet = " + modularitySet(G, node_set));
//		
////		node_set = new int[]{51,52,58,64,65,67,68,69,85,103,104};
////		System.out.println("modSet = " + modularitySet(G, node_set));
	}

}
