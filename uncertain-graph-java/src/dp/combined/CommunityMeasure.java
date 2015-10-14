/*
 * Sep 29, 2015
 * 	- community measures: 
 * 		+ modularity
 * 		+ F1-score (WWW'14, EDBTw'15)
 * 		+ Normalized mutual information [NMI] (Lancichinetti)
 * 		+ inter/intra-cluster accuracy [A-inter, A-intra](ICML'07)
 * 		+ Adjusted Rand Index [ARI]	(KDD'07)
 * 	- modularitySet(Grph), modularitySet(EdgeWeightedGraph) for checking every modularity function
 * Oct 8
 * 	- implement NMI
 * Oct 9
 * 	- implement F1 (avgF1Score): two versions
 * Oct 14
 * 	- fastNormalizedMutualInfo(), fastAvgF1Score()
 */

package dp.combined;

import grph.Grph;
import grph.VertexPair;
import grph.io.EdgeListReader;
import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

import algs4.Edge;
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
	
	////
	public static double modularity(EdgeWeightedGraph G, int[] part){
		double mod = 0.0;
		
		int k = 0;
		for (int com : part)
			if (k < com)
				k = com;
		k += 1;
		
		double[] lc = new double[k];
		double[] dc = new double[k];
		
		//
		for (int u = 0; u < G.V(); u++)
			dc[part[u]] += G.degree(u);
		
		for (Edge p : G.edges()){
			int u = p.either();
			int v = p.other(u);
			
			if (part[u] == part[v])
				lc[part[u]] += 1;
		}
		
		//
		int m = G.E();
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
	
	//// ARI
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
	
	//// NMI
	public static double normalizedMutualInfo(int[] part_org, int[] part){
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
		
//		System.out.println("k_org = " + k_org);
//		System.out.println("k = " + k);
		// n_ij, n_i, n_j
		int[][] n_ij = new int[k_org][k];
		int[] n_i = new int[k_org];
		int[] n_j = new int[k];
		
		for (int u = 0; u < n; u++){
			n_ij[part_org[u]][part[u]] += 1;
			n_i[part_org[u]] += 1;
			n_j[part[u]] += 1;
		}

		//
		double nominator = 0.0;
		for (int i = 0; i < k_org; i++)
			for (int j = 0; j < k; j++)
				if (n_ij[i][j] > 0)
					nominator += n_ij[i][j] * Math.log((double)n_ij[i][j]/n_i[i] * (double)n/n_j[j]);
		nominator = -2*nominator;
		
		double denominator = 0.0;
		for (int i = 0; i < k_org; i++)
			denominator += n_i[i] * Math.log((double)n_i[i]/n);
		for (int j = 0; j < k; j++)
			denominator += n_j[j] * Math.log((double)n_j[j]/n);
		
		ret = nominator/denominator;
		//
		return ret;
	}
	
	// fast version
	public static double fastNormalizedMutualInfo(int[] part_org, int[] part){
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
		
//		System.out.println("k_org = " + k_org);
//		System.out.println("k = " + k);
		// n_ij, n_i, n_j
		Map<Int2, Integer> n_ij = new HashMap<Int2, Integer>();
		int[] n_i = new int[k_org];
		int[] n_j = new int[k];
		
		for (int u = 0; u < n; u++){
			Int2 key = new Int2(part_org[u], part[u]);
			if (n_ij.containsKey(key))
				n_ij.put(key, n_ij.get(key) + 1);
			else
				n_ij.put(key, 1);
				
			n_i[part_org[u]] += 1;
			n_j[part[u]] += 1;
		}

		//
		double nominator = 0.0;
		for (Map.Entry<Int2, Integer> entry : n_ij.entrySet()){
			int i = entry.getKey().val0;
			int j = entry.getKey().val1;
			int n_ij_val = entry.getValue();
			nominator += n_ij_val * Math.log((double)n_ij_val/n_i[i] * (double)n/n_j[j]);
		}
		nominator = -2*nominator;
		
		double denominator = 0.0;
		for (int i = 0; i < k_org; i++)
			denominator += n_i[i] * Math.log((double)n_i[i]/n);
		for (int j = 0; j < k; j++)
			denominator += n_j[j] * Math.log((double)n_j[j]/n);
		
		ret = nominator/denominator;
		//
		return ret;
	}
	
	
	//// average F1 score
	// A, B already sorted, used in avgF1Score(List<Integer []> C1, List<Integer []> C2)
	public static int intersect(Integer[] A, Integer[] B){
		int ret = 0;
		
		// WAY-1, if A, B are already sorted
		int iA = 0;
		int iB = 0;
		
		
		while (iA < A.length && iB < B.length){
//			System.out.println(iA + " " + iB);
			if (A[iA].compareTo(B[iB]) == 0){
				ret += 1;
				iA += 1;
				iB += 1;
				continue;
			}
			if (A[iA].compareTo(B[iB]) > 0){
				iB += 1;
				continue;
			}
			if (A[iA].compareTo(B[iB]) < 0){
				iA += 1;
				continue;
			}
			
		}
		
		// WAY-2
//		if (A.length < B.length){
//			HashSet<Integer> setB = new HashSet<Integer>(Arrays.asList(B));
//			for (int val : A)
//				if (setB.contains(val))
//					ret += 1;
//		}else{
//			HashSet<Integer> setA = new HashSet<Integer>(Arrays.asList(A));
//			for (int val : B)
//				if (setA.contains(val))
//					ret += 1;
//		}
		
		
		//
		return ret;
	}
	
	// non-overlap communitites
	public static double avgF1Score(int[] part_org, int[] part){
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
		
//		System.out.println("k_org = " + k_org);
//		System.out.println("k = " + k);
		
		int[] sizeC1 = new int[k_org];
		int[] sizeC2 = new int[k];
		
		// intersection
		int[][] inter = new int[k_org][k];
		
		for (int u = 0; u < n; u++){
			inter[part_org[u]][part[u]] += 1;
			sizeC1[part_org[u]] += 1;
			sizeC2[part[u]] += 1;
		}
		
		// Out Of Memory on 'youtube'
//		double[][] prec = new double[k_org][k];
//		double[][] recall = new double[k_org][k];
//		double[][] H = new double[k_org][k];
//		
//		for (int i = 0; i < k_org; i++){
//			for (int j = 0; j < k; j++){
//				if (inter[i][j] > 0){
//					prec[i][j] = (double)inter[i][j] / sizeC1[i];
//					recall[i][j] = (double)inter[i][j] / sizeC2[j];
//					H[i][j] = 2*prec[i][j]*recall[i][j]/(prec[i][j] + recall[i][j]);
//				}
//			}
//		}
//		
//		double[] F1 = new double[k_org];
//		double[] F2 = new double[k];
//		
//		// F1(C1[i], C2)
//		for (int i = 0; i < k_org; i++)
//			for (int j = 0; j < k; j++)
//				if (inter[i][j] > 0)
//					if (F1[i] < H[i][j])
//						F1[i] = H[i][j];
//		
//		// F1(C2[j], C1)
//		for (int j = 0; j < k; j++)
//			for (int i = 0; i < k_org; i++)
//				if (inter[i][j] > 0)
//					if (F2[j] < H[i][j])
//						F2[j] = H[i][j];

		// fixed 
		double[] F1 = new double[k_org];
		double[] F2 = new double[k];
		
		for (int i = 0; i < k_org; i++){
			for (int j = 0; j < k; j++){
				if (inter[i][j] > 0){
					double H_ij = 2*((double)inter[i][j] / sizeC1[i])*((double)inter[i][j] / sizeC2[j])/((double)inter[i][j] / sizeC1[i] + (double)inter[i][j] / sizeC2[j]);
					if (F1[i] < H_ij)
						F1[i] = H_ij;
					if (F2[j] < H_ij)
						F2[j] = H_ij;
				}
			}
		}
		
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < k_org; i++)
			sum1 += F1[i];
		for (int j = 0; j < k; j++)
			sum2 += F2[j];
		
		ret = sum1/(2*k_org) + sum2/(2*k); 
		
		
		//
		return ret;
	}
	
	// fast version
	public static double fastAvgF1Score(int[] part_org, int[] part){
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
		
//		System.out.println("k_org = " + k_org);
//		System.out.println("k = " + k);
		
		Map<Int2, Integer> inter = new HashMap<Int2, Integer>();
		int[] sizeC1 = new int[k_org];
		int[] sizeC2 = new int[k];
		
		for (int u = 0; u < n; u++){
			Int2 key = new Int2(part_org[u], part[u]);
			if (inter.containsKey(key))
				inter.put(key, inter.get(key) + 1);
			else
				inter.put(key, 1);
				
			sizeC1[part_org[u]] += 1;
			sizeC2[part[u]] += 1;
		}
		
		double[] F1 = new double[k_org];
		double[] F2 = new double[k];
		
		for (Map.Entry<Int2, Integer> entry : inter.entrySet()){
			int i = entry.getKey().val0;
			int j = entry.getKey().val1;
			int inter_ij = entry.getValue();
			double H_ij = 2*((double)inter_ij / sizeC1[i])*((double)inter_ij / sizeC2[j])/((double)inter_ij / sizeC1[i] + (double)inter_ij / sizeC2[j]);
			if (F1[i] < H_ij)
				F1[i] = H_ij;
			if (F2[j] < H_ij)
				F2[j] = H_ij;
		}
		
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < k_org; i++)
			sum1 += F1[i];
		for (int j = 0; j < k; j++)
			sum2 += F2[j];
		
		ret = sum1/(2*k_org) + sum2/(2*k); 
		
		
		//
		return ret;
	}
	
	// allow overlap communitites
	public static double avgF1Score(List<Integer []> C1, List<Integer []> C2){
		double ret = 0.0;
		
		// 1. sort sets in each list --> 
		for (int i = 0; i < C1.size(); i++){
			Integer[] A = C1.get(i);
			Arrays.sort(A);
			C1.set(i, A);
		}
		for (int i = 0; i < C2.size(); i++){
			Integer[] B = C2.get(i);
			Arrays.sort(B);
			C2.set(i, B);
		}
		
		
		// 
		int[] sizeC1 = new int[C1.size()];
		int[] sizeC2 = new int[C2.size()];
		
		for (int i = 0; i < C1.size(); i++)
			sizeC1[i] = C1.get(i).length;
		for (int j = 0; j < C2.size(); j++)
			sizeC2[j] = C2.get(j).length;

		// intersection
		int[][] inter = new int[C1.size()][C2.size()];
		double[][] prec = new double[C1.size()][C2.size()];
		double[][] recall = new double[C1.size()][C2.size()];
		double[][] H = new double[C1.size()][C2.size()];
		
		for (int i = 0; i < C1.size(); i++){
			for (int j = 0; j < C2.size(); j++){
				inter[i][j] = intersect(C1.get(i), C2.get(j));
				if (inter[i][j] > 0){
					prec[i][j] = (double)inter[i][j] / sizeC1[i];
					recall[i][j] = (double)inter[i][j] / sizeC2[j];
					H[i][j] = 2*prec[i][j]*recall[i][j]/(prec[i][j] + recall[i][j]);
				}
			}
		}
		
		double[] F1 = new double[C1.size()];
		double[] F2 = new double[C2.size()];
		
		// F1(C1[i], C2)
		for (int i = 0; i < C1.size(); i++)
			for (int j = 0; j < C2.size(); j++)
				if (inter[i][j] > 0)
					if (F1[i] < H[i][j])
						F1[i] = H[i][j];
		
		// F1(C2[j], C1)
		for (int j = 0; j < C2.size(); j++)
			for (int i = 0; i < C1.size(); i++)
				if (inter[i][j] > 0)
					if (F2[j] < H[i][j])
						F2[j] = H[i][j];
		
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < C1.size(); i++)
			sum1 += F1[i];
		for (int j = 0; j < C2.size(); j++)
			sum2 += F2[j];
		
		ret = sum1/(2*C1.size()) + sum2/(2*C2.size()); 
		//
		return ret;
	}
	
	//// relabel node ids in community file (e.g. com-amazon.top5000.cmty.txt)
	// read .nodemap computed by graph-dp/compare/graph_generator.normalize_and_save_graph()
	public static void relabelCommunity(String graph_file, String nodemap_file, String com_file, String outcom_file, int m) throws IOException{
		
		// read nodemap_file 
		BufferedReader br = new BufferedReader(new FileReader(nodemap_file));
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split(" ");
        	map.put(Integer.parseInt(items[0]), Integer.parseInt(items[1]));
		}
		br.close();
		
		// read graph_file
		br = new BufferedReader(new FileReader(graph_file));
		int count = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split("\t");
//        	if (count < 20){
//        		System.out.println(map.get(Integer.parseInt(items[0])) + " " + map.get(Integer.parseInt(items[1])));
//        		count += 1;
//        	}
		}
		br.close();
		
		// read com_file, write to outcom_file
		br = new BufferedReader(new FileReader(com_file));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outcom_file));
		while (true){
			String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split("\t");
        	for (String item : items){
        		int u = Integer.parseInt(item);
        		bw.write(map.get(u) + ",");
        	}
        	bw.write("\n");
		}
		
		br.close();
		bw.close();
	}
	
	////
	public static List<Integer[]> readGroundTruth(String file_name, int n) throws IOException{
		List<Integer []> ret = new ArrayList<Integer[]>();
		
		boolean[] mark = new boolean[n];
		BufferedReader br = new BufferedReader(new FileReader(file_name));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split(",");
        	Integer[] A = new Integer[items.length];
        	for (int i = 0; i < items.length; i++){
        		A[i] = Integer.parseInt(items[i]);
        		mark[A[i]] = true;
        	}
        	ret.add(A);
		}
		br.close();
		
		int count = 0;
		for (int i = 0; i < mark.length; i++)
			if (mark[i] == true)
				count += 1;
		System.out.println("count = " + count);
		//
		return ret;
	}
	
	////
	public static void computeAndExport(String prefix, String dataname, String sample_file, int n_samples) throws Exception{
		
		EdgeListReader reader = new EdgeListReader();
		Grph G;
		RegularFile f = new RegularFile(prefix + "_data/" + dataname + ".gr");
		G = reader.readGraph(f);
		
		int n_nodes = G.getNumberOfVertices();
		
		String louvain_file = prefix + "_data/" + dataname + ".louvain";
		int[] louvain_part = readPart(louvain_file, n_nodes);
		
		int[] comArr = new int[n_samples];
		double[] modArr = new double[n_samples];
		double[] nmiArr = new double[n_samples];
		double[] f1Arr = new double[n_samples];
		
		for (int i = 0; i < n_samples; i++){
//			System.out.println("sample " + i);
			
			String compare_file = prefix + "_louvain/" + sample_file + "." + i + ".part";
			int[] compare_part = readPart(compare_file, n_nodes);
			
			int k = 0;
			for (int com : compare_part)
				if (k < com)
					k = com;
			
			//
			comArr[i] = k+1;
			modArr[i] = modularity(G, compare_part);
			nmiArr[i] = fastNormalizedMutualInfo(louvain_part, compare_part);
			f1Arr[i] = fastAvgF1Score(louvain_part, compare_part);
			
		}
		// write to MATLAB
		String matlab_file = prefix + "_matlab/" + sample_file + ".mat";
		
		MLDouble modA = new MLDouble("modArr", modArr, 1);
		MLDouble nmiA = new MLDouble("nmiArr", nmiArr, 1);
		MLDouble f1A = new MLDouble("f1Arr", f1Arr, 1);
		MLInt32 comA = new MLInt32("comArr", comArr, 1);
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(modA); 
        towrite.add(nmiA);
        towrite.add(f1A);
        towrite.add(comA);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		int num_pair = 100000;
		
//		String filename = "_data/karate.gr";
//		String filename = "_data/polbooks.gr";
		
//		int n_nodes = 6474;
//		String filename = "_data/as20graph.gr";
//		String louvain_file = "as20graph.louvain";	
		
//		int n_nodes = 18771;
//		String filename = "_data/ca-AstroPh.gr";
//		String louvain_file = "ca-AstroPh.louvain";	
		
		int n_nodes = 334863;
		int n_edges = 925872;
		String filename = "_data/com_amazon_ungraph.gr";
		String louvain_file = "com_amazon_ungraph.louvain";						// aIntra = 1, aInter = 1, ARI = 1		
		
		
		String path = "_out/";
		
		//
//		String compare_file = "as20graph_multioptlouvain_20_40_6_2.part";			// LouvainOpt
//		String compare_file = "as20graph_multioptlouvain_20_40_4_5.part";			// LouvainOpt
//		String compare_file = "as20graph_partoptlouvain_20_40_6_3.part";			// LouvainOpt
//		String compare_file = "as20graph_partoptlouvain_20_40_5_3.part";			// LouvainOpt
//		String compare_file = "as20graph_partoptlouvain_20_40_5_4.part";			// LouvainOpt
//		String compare_file = "as20graph_moddivoptlouvain_30_50_10.part";			// ModDivisiveOpt
//		String compare_file = "as20graph_edgeflip_8.8-0.part";						// EdgeFlip
//		String compare_file = "as20graph_edgeflip_6.0-0.part";						// EdgeFlip
//		String compare_file = "as20graph_noisy.part";								// Orbis
//		String compare_file = "as20graph_nodesetlv2_20_40_10_4_2.00_10.0_3.leaf";	// NodeSetLouvain
//		String compare_file = "as20graph_nodesetlv2_20_40_10_4_2.00_20.0_3.part";	// NodeSetLouvain
		
//		String compare_file = "ca-AstroPh_nodesetlv2_20_40_10_6_1.00_20.0_3.part";
//		String compare_file = "ca-AstroPh_nodesetlv2_20_40_10_3_1.00_10.0_3.part";
//		String compare_file = "ca-AstroPh_nodesetlv2_20_40_10_3_2.00_10.0_3.part";
//		String compare_file = "ca-AstroPh_nodesetlv2_20_40_10_4_2.00_10.0_3.part";
//		String compare_file = "ca-AstroPh_temp.part";
		
//		String compare_file = "com_amazon_ungraph_partoptlouvain_20_40_10_2.part";			// LouvainOpt
//		String compare_file = "com_amazon_ungraph_moddivdp_20_40_10_8_2.00_20.0.part";
//		String compare_file = "com_amazon_ungraph_moddivopt_1_100_20_8.part";
//		String compare_file = "com_amazon_ungraph_nodesetlv_20_5_5.0.part";		//
//		String compare_file = "com_amazon_ungraph_nodesetlv_20_10_10.0.part";	//				
//		String compare_file = "com_amazon_ungraph_edgeflip_12.7-0.part";		// EdgeFlip		aIntra = 0.4181, aInter = 0.9935, ARI = 0.4416
//		String compare_file = "com_amazon_ungraph_filter_6.4-0.part";			// TmF
//		String compare_file = "com_amazon_ungraph_filter_12.7-0.part";			// TmF			aIntra = 0.2427, aInter = 0.9925, ARI = 0.2691
//		String compare_file = "com_amazon_ungraph_nodesetlv2_20_40_10_1_2.00_20.0_5.part";
//		String compare_file = "com_amazon_ungraph_hrgdivgreedy_20_40_10_8_2.00_20.0.part";	// 
//		String compare_file = "com_amazon_ungraph_nodesetlv2_20_40_10_4_2.00_20.0_5.part3";	// NodeSetLouvain
		String compare_file = "com_amazon_ungraph_nodesetlv2_20_40_10_3_2.00_20.0_10.best";	// NodeSetLouvain
//		String compare_file = "com_amazon_ungraph_nodesetlv2_20_40_10_3_2.50_30.0_10.part3";	// NodeSetLouvain
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
		String prefix= "";
		String dataname = "karate";
	    int n_samples = 1;
	    String sample_file = "";
	    
	    if(args.length >= 4){
	    	prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			sample_file = args[3];
	    }
	    
	    System.out.println("dataname = " + dataname);
		System.out.println("n_samples = " + n_samples);
		System.out.println("sample_file = " + sample_file);
		
		computeAndExport(prefix, dataname, sample_file, n_samples);
		System.out.println("computeAndExport - DONE.");
		
		
		
		//
////		EdgeListReader reader = new EdgeListReader();
////		Grph G;
////		RegularFile f = new RegularFile(filename);
////		G = reader.readGraph(f);
////		
////		System.out.println("#nodes = " + G.getNumberOfVertices());
////		System.out.println("#edges = " + G.getNumberOfEdges());
//		
//		//
//		int[] louvain_part = readPart(path + louvain_file, n_nodes);
//		int[] compare_part = readPart(path + compare_file, n_nodes);
//		
////		System.out.println("louvain_file = " + louvain_file);
////		System.out.println("louvain mod = " + modularity(G, louvain_part));
////		System.out.println("compare_file = " + compare_file);
////		System.out.println("compare mod = " + modularity(G, compare_part));
//		
//		//
//		long start = System.currentTimeMillis();
//		double aIntra = intraRatio(louvain_part, compare_part, num_pair);
//		System.out.println("aIntra = " + aIntra);
//		System.out.println("intraRatio - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		start = System.currentTimeMillis();
//		double aInter = interRatio(louvain_part, compare_part, num_pair);
//		System.out.println("aInter = " + aInter);
//		System.out.println("interRatio - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		double ARI = adjustedRandIndex(louvain_part, compare_part);
//		System.out.println("ARI = " + ARI);
//		
//		double NMI = normalizedMutualInfo(louvain_part, compare_part);
//		System.out.println("NMI = " + NMI);
//		System.out.println("NMI [fast] = " + fastNormalizedMutualInfo(louvain_part, compare_part));
//		
////		List<Integer[]> gt = readGroundTruth("_data/com_amazon_ungraph.top5000", n_nodes);
//		List<Integer[]> lv = readGroundTruth(path + louvain_file, n_nodes);
//		List<Integer[]> cp = readGroundTruth(path + compare_file, n_nodes);
//		System.out.println("lv.size = " + lv.size());
//		System.out.println("cp.size = " + cp.size());
//		System.out.println("Avg.F1 score (lv,cp) = " + avgF1Score(lv, cp));
//		System.out.println("Avg.F1 score (lv,cp) = " + avgF1Score(louvain_part, compare_part));
//		System.out.println("Avg.F1 score [fast] (lv,cp) = " + fastAvgF1Score(louvain_part, compare_part));
		
		
		// Avg.F1 score
//		List<Integer[]> gt = readGroundTruth("_data/com_amazon_ungraph.top5000", n_nodes);
//		List<Integer[]> lv = readGroundTruth(path + louvain_file, n_nodes);
//		System.out.println("lv.size = " + lv.size());
//		System.out.println("gt.size = " + gt.size());
//		System.out.println("Avg.F1 score (lv,gt) = " + avgF1Score(lv, gt));
//		System.out.println("Avg.F1 score (gt,lv) = " + avgF1Score(gt,lv));
//		
//		gt = readGroundTruth("_data/com_amazon_ungraph.all.dedup", n_nodes);
//		System.out.println("gt.size = " + gt.size());
//		System.out.println("Avg.F1 score (lv,gt) = " + avgF1Score(lv, gt));
//		System.out.println("Avg.F1 score (gt,lv) = " + avgF1Score(gt,lv));
		
		
		/////////////////////////
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
		
		
		// TEST avgF1Score()
//		List<Integer []> C1 = new ArrayList<Integer[]>();
//		Integer[] A1 = new Integer[]{1,3,2};
//		Integer[] A2 = new Integer[]{4,3,2};
//		Integer[] A3 = new Integer[]{1,2,6,5};
//		C1.add(A1); C1.add(A2); C1.add(A3);
//	
//		List<Integer []> C2 = new ArrayList<Integer[]>();
//		C2.add(A3); C2.add(A2); C2.add(A1);	C2.add(A2);		// F1 = 1.0 regardless the order of sets and set duplication !
////		Integer[] B1 = new Integer[]{1,3,2,4};
////		Integer[] B2 = new Integer[]{4,3};
////		Integer[] B3 = new Integer[]{1,2,6,5};
////		C2.add(B1); C2.add(B2); C2.add(B3);		// F1 = 0.8952
//		
//		double ret = avgF1Score(C2, C1);
//		System.out.println("ret = " + ret);
//		ret = avgF1Score(C1, C2);	// symmetric
//		System.out.println("ret = " + ret);
		
		
		// TEST relabelCommunity() + readGroundTruth()
//		relabelCommunity("_data/com-amazon.ungraph.txt", "D:/git/itce2011/graph-dp/data/com_amazon_ungraph.nodemap", 
//				"_data/com-amazon.top5000.cmty.txt", "_data/com_amazon_ungraph.top5000", 925872);
//		relabelCommunity("_data/com-dblp.ungraph.txt", "D:/git/itce2011/graph-dp/data/com_dblp_ungraph.nodemap", 
//				"_data/com-dblp.top5000.cmty.txt", "_data/com_dblp_ungraph.top5000", 1049866);
//		relabelCommunity("_data/com-youtube.ungraph.txt", "D:/git/itce2011/graph-dp/data/com_youtube_ungraph.nodemap", 
//				"_data/com-youtube.top5000.cmty.txt", "_data/com_youtube_ungraph.top5000", 2987624);
			
//		readGroundTruth("_data/com_amazon_ungraph.top5000", 334863);
//		readGroundTruth("_data/com_dblp_ungraph.top5000", 317080);
//		readGroundTruth("_data/com_youtube_ungraph.top5000", 1134890);
		
//		relabelCommunity("_data/com-amazon.ungraph.txt", "D:/git/itce2011/graph-dp/data/com_amazon_ungraph.nodemap", 
//				"_data/com-amazon.all.dedup.cmty.txt", "_data/com_amazon_ungraph.all.dedup", 925872);
//		relabelCommunity("_data/com-dblp.ungraph.txt", "D:/git/itce2011/graph-dp/data/com_dblp_ungraph.nodemap", 
//				"_data/com-dblp.all.cmty.txt", "_data/com_dblp_ungraph.all", 1049866);
//		relabelCommunity("_data/com-youtube.ungraph.txt", "D:/git/itce2011/graph-dp/data/com_youtube_ungraph.nodemap", 
//				"_data/com-youtube.all.cmty.txt", "_data/com_youtube_ungraph.all", 2987624);

//		readGroundTruth("_data/com_amazon_ungraph.all", 334863);
//		readGroundTruth("_data/com_dblp_ungraph.all", 317080);
//		readGroundTruth("_data/com_youtube_ungraph.all", 1134890);
		
		
		
	}

}
