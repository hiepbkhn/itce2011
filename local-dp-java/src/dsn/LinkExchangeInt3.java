/*
 * Jun 12, 2016
 * 	- copied from LinkExchange.java
 * 	- use Int3, update insertLink(), saveLocalGraph() to take into account the counter of (duplicate) edges
 * 	- add attackLocalGraph(), inferEdges()
 */

package dsn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.Arrays;
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


public class LinkExchangeInt3 {

	////
	public static void printEdges(List<Int3> list){
		for(Int3 e:list)
			System.out.print("(" + e.val0 + "," + e.val1 + ") ");
		System.out.println();
	}
	////
	public static void graphMetric(String graph_file, int n_nodes) throws IOException{
		UnweightedGraph aG = UnweightedGraph.readEdgeListWithNodes(graph_file, "\t", n_nodes);
		System.out.println("#nodes = " + aG.V());
		System.out.println("#edges = " + aG.E());
		
		PathMetric path = new PathMetric();
		double[] distance_dist;
		
		distance_dist = UtilityMeasure.getDistanceDistr(aG, path);
		
		//
		System.out.println("diameter = " + path.s_Diam);
	}
	
	////
	public static int[] sampleNodeByDegree(int[] deg, int k){
		int n = deg.length;
		int[] ret = new int[k];
		
		int[] index = new int[n];
		for (int u = 0; u < n; u++)
			index[u] = u;
		
		//
		Util.quicksort(deg, index);
		
		for (int i = 0; i < k; i++)
			ret[i] = index[i*n/k];
		
		//
		return ret;
	}
	
	////
	public static void saveLocalGraph(List<List<Int3>> links, int[] selectedNodes, String sample_file) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(sample_file));
		
		// number of selected nodes
		bw.write(selectedNodes.length + "\n");
		for (int u : selectedNodes){
			List<Int3> edges = links.get(u);
			// node : #edges
			bw.write(u + "," + edges.size() + "\n");
			for (Int3 e : edges)
				bw.write(e.val0 + "\t" + e.val1 + "\t" + e.c + "\n");
			
		}
		bw.close();
		System.out.println("Written to sample_file.");
		
	}
	
	////
	public static void computeUtility(EdgeIntGraph aG, DegreeMetric deg, double[] deg_dist, PathMetric path, double[] distance_dist) throws IOException{
		// 1. degree distribution
		System.arraycopy(UtilityMeasure.getDegreeDistr(aG, deg), 0, deg_dist, 0, aG.V());
		
		// 2. distance distribution
//		distance_dist = UtilityMeasure.getDistanceDistr(G, path);	// hyperANF
		UnweightedGraph bG = new UnweightedGraph(aG);
		System.arraycopy(UtilityMeasure.getDistanceDistr(bG, path), 0, distance_dist, 0, 50);	// BFS
		
		
	}
	
	////
	public static void computeTrueGraph(EdgeIntGraph G, String matlab_file) throws IOException{
		int n_nodes = G.V();
		
		// compute utility
		DegreeMetric deg = new DegreeMetric();
		double[] deg_dist = new double[n_nodes]; 
		PathMetric path = new PathMetric();
		double[] distance_dist = new double[50];
		
		computeUtility(G, deg, deg_dist, path, distance_dist);
			
		
		// write to MATLAB
		MLDouble degArr = new MLDouble("degArr", deg_dist, 1);
		MLDouble distArr = new MLDouble("distArr", distance_dist, 1);
		
		MLDouble s_AD = new MLDouble("s_AD", new double[]{deg.s_AD}, 1);
		MLDouble s_MD = new MLDouble("s_MD", new double[]{deg.s_MD}, 1);
		MLDouble s_DV = new MLDouble("s_DV", new double[]{deg.s_DV}, 1);
		MLDouble s_CC = new MLDouble("s_CC", new double[]{deg.s_CC}, 1);
		MLDouble s_PL = new MLDouble("s_PL", new double[]{deg.s_PL}, 1);
		
		MLDouble s_APD = new MLDouble("s_APD", new double[]{path.s_APD}, 1);
		MLDouble s_CL = new MLDouble("s_CL", new double[]{path.s_CL}, 1);
		MLDouble s_EDiam = new MLDouble("s_EDiam", new double[]{path.s_EDiam}, 1);
		MLDouble s_Diam = new MLDouble("s_Diam", new double[]{path.s_Diam}, 1);
		
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(degArr); 
        towrite.add(distArr);
        towrite.add(s_AD);
        towrite.add(s_MD);
        towrite.add(s_DV);
        towrite.add(s_CC);
        towrite.add(s_PL);
        towrite.add(s_APD);
        towrite.add(s_CL);
        towrite.add(s_EDiam);
        towrite.add(s_Diam);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
		
	}
	
	////
	public static void computeLocalGraph(String sample_file, String matlab_file, int n_nodes) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(sample_file));
		
		String str = br.readLine();
		int k = Integer.parseInt(str);
		System.out.println("#selected nodes = " + k);
		
		// for MATLAB
		double[] a_AD = new double[k];
		double[] a_DV = new double[k];
		double[] a_MD = new double[k];
		double[] a_PL = new double[k];
		double[] a_CC = new double[k];
		
		double[] a_APD = new double[k];
		double[] a_CL = new double[k];
		double[] a_EDiam = new double[k];
		double[] a_Diam = new double[k];
		
		double[] a_degree = new double[k*n_nodes];
		double[] a_distance = new double[k*50];
		
		for(int i = 0; i < k; i++){
			str = br.readLine();
			String[] items = str.split(",");
			int u = Integer.parseInt(items[0]);
			int size = Integer.parseInt(items[1]);
			System.out.println("u = " + u + ", size = " + size);
			
			// build local graph
			EdgeIntGraph aG = new EdgeIntGraph(n_nodes); 
			for (int j = 0; j < size; j++){
				str = br.readLine();
				items = str.split("\t");
				int v = Integer.parseInt(items[0]);
				int w = Integer.parseInt(items[1]);
				
				aG.addEdge(new EdgeInt(v,w,1));
			}
			System.out.println("#nodes = " + aG.V());
			System.out.println("#edges = " + aG.E());
				
			// compute utility
			DegreeMetric deg = new DegreeMetric();
			double[] deg_dist = new double[n_nodes]; 
			PathMetric path = new PathMetric();
			double[] distance_dist = new double[50];
			
			computeUtility(aG, deg, deg_dist, path, distance_dist);
			
			a_AD[i] = deg.s_AD;
			a_DV[i] = deg.s_DV;
			a_MD[i] = deg.s_MD;
			a_PL[i] = deg.s_PL;
			a_CC[i] = deg.s_CC;
			
			a_APD[i] = path.s_APD;
			a_CL[i] = path.s_CL;
			a_EDiam[i] = path.s_EDiam;
			a_Diam[i] = path.s_Diam;
			
			for (int j = 0; j < n_nodes; j++)
				a_degree[i + j*k] = deg_dist[j];		// packed by column
			for (int j = 0; j < 50; j++)
				a_distance[i + j*k] = distance_dist[j];	// packed by column
		}
		br.close();
		
		// write to MATLAB
		MLDouble degArr = new MLDouble("degArr", a_degree, k);
		MLDouble distArr = new MLDouble("distArr", a_distance, k);
		
		MLDouble s_AD = new MLDouble("a_AD", a_AD, 1);
		MLDouble s_MD = new MLDouble("a_MD", a_MD, 1);
		MLDouble s_DV = new MLDouble("a_DV", a_DV, 1);
		MLDouble s_CC = new MLDouble("a_CC", a_CC, 1);
		MLDouble s_PL = new MLDouble("a_PL", a_PL, 1);
		
		MLDouble s_APD = new MLDouble("a_APD", a_APD, 1);
		MLDouble s_CL = new MLDouble("a_CL", a_CL, 1);
		MLDouble s_EDiam = new MLDouble("a_EDiam", a_EDiam, 1);
		MLDouble s_Diam = new MLDouble("a_Diam", a_Diam, 1);
		
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
        towrite.add(degArr); 
        towrite.add(distArr);
        towrite.add(s_AD);
        towrite.add(s_MD);
        towrite.add(s_DV);
        towrite.add(s_CC);
        towrite.add(s_PL);
        towrite.add(s_APD);
        towrite.add(s_CL);
        towrite.add(s_EDiam);
        towrite.add(s_Diam);
        
        new MatFileWriter(matlab_file, towrite );
        System.out.println("Written to MATLAB file.");
		
	}
	
	////
	public static List<Int3> createFalseLink(EdgeIntGraph G, int u, double beta){
		List<Int3> ret = new ArrayList<Int3>();
		
		int n = G.V();
		//
		Random random = new Random();
		for (int i = 0; i < beta*G.degree(u); i++){
			int w = random.nextInt(n);
			while (G.areEdgesAdjacent(u, w))
				w = random.nextInt(n);
			
			ret.add(new Int3(u, w));
			
		}
		
		
		//
		return ret;
	}
	
	////
	public static List<Int3> sampleLink(List<Int3> srcList, double alpha){
		List<Int3> ret = new ArrayList<Int3>();
		
		Random random = new Random();
		for (int i = 0; i < alpha*srcList.size(); i++){
			int k = random.nextInt(srcList.size());
			
			ret.add(srcList.get(k));
			
		}
		
		//
		return ret;
		
	}
	
	////
	public static List<Int3> sampleLinkNoDup(List<Int3> srcList, double alpha){
		if (alpha == 1)
			return srcList;
		
		List<Int3> ret = new ArrayList<Int3>();
		
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
	
	//// return totalLink
	public static long countTrueFalseDupLinks(EdgeIntGraph G, List<List<Int3>> links, int[] trueLinks, int[] falseLinks, int[] dupLinks){
		int n = links.size();
		
		long totalLink = 0;
		for (int u = 0; u < n; u++){
			Map<Int3, Integer> dup = new HashMap<Int3, Integer>();
			for(Int3 p : links.get(u)){
				if(p.val0 > p.val1){	// normalize
					int temp = p.val0;
					p.val0 = p.val1;
					p.val1 = temp;
				}
				
				if (dup.containsKey(p)){
					dupLinks[u] += 1;
				}else{
					dup.put(p, 1);
					if (G.areEdgesAdjacent(p.val0, p.val1))
						trueLinks[u] += 1;
					else
						falseLinks[u] += 1;
				}
			}
			
			totalLink += links.get(u).size();
		}
		//
		return totalLink;
	}
	
	////
	public static void linkExchange(EdgeIntGraph G, int round, double alpha, double beta, String count_file) throws IOException{
		int n = G.V();
		
		List<List<Int3>> links = new ArrayList<List<Int3>>();
		
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int3> temp = new ArrayList<Int3>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int3(u, v));
			
			List<Int3> newLinks = createFalseLink(G, u, beta);
			temp.addAll(newLinks);
			
			links.add(temp);
		}
		
		// loop
		for(int t = 1; t < round+1; t++){
			List<List<Int3>> exLinks = new ArrayList<List<Int3>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int3>());
			
			// for each pair of nodes (u,v)
			for (EdgeInt e: G.edges()){
				int u = e.either();
				int v = e.other(u);
				
				List<Int3> listU = sampleLink(links.get(u), alpha);
				List<Int3> listV = sampleLink(links.get(v), alpha);
				
				//
				exLinks.get(u).addAll(listV);
				exLinks.get(v).addAll(listU);
				
			}
			// expand lists, accept duplicate links
			for (int u = 0; u < n; u++)
				links.get(u).addAll(exLinks.get(u));
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		
		countTrueFalseDupLinks(G, links, trueLinks, falseLinks, dupLinks);
		
		System.out.println("linkExchange - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	////
	public static void linkGossip(EdgeIntGraph G, int round, double alpha, double beta, String count_file) throws IOException{
		int n = G.V();
		
		List<List<Int3>> links = new ArrayList<List<Int3>>();
		
		// compute adj lists
		List<List<Integer>> adj = new ArrayList<List<Integer>>();
		for (int u = 0; u < n; u++){
			List<Integer> nblist = new ArrayList<Integer>(G.adj(u).keySet());
			adj.add(nblist);
		}
		
		
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int3> temp = new ArrayList<Int3>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int3(u, v));
			
			List<Int3> newLinks = createFalseLink(G, u, beta);
			temp.addAll(newLinks);
			
			links.add(temp);
		}
		
		// loop
		Random random = new Random();
		for(int t = 1; t < round+1; t++){
			List<List<Int3>> exLinks = new ArrayList<List<Int3>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int3>());
			
			// for each node u
			for (int u = 0; u < n; u++){
				int v = adj.get(u).get(random.nextInt(G.degree(u)));
				
				List<Int3> listU = sampleLink(links.get(u), alpha);
				
				//
				exLinks.get(v).addAll(listU);
				
			}
			// expand lists, accept duplicate links
			for (int u = 0; u < n; u++)
				links.get(u).addAll(exLinks.get(u));
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		
		countTrueFalseDupLinks(G, links, trueLinks, falseLinks, dupLinks);
		
		System.out.println("linkGossip - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	//// used in initial stage (true if existed)
	public static boolean checkLink(List<Int3> list, Int3 e){
		// normalize e
		normalizeEdge(e);
		
		//
		int lo = 0;
		int hi = list.size()-1;
		int mid = (lo + hi)/2;
		int comp = 0;
		boolean found = false;
		while (true){
			comp = e.compareTo(list.get(mid));
			
			if (comp < 0){
				hi = mid-1;
			}else if(comp > 0){
				lo = mid+1;
			}else{
				found = true;
				break;
			}
			
			mid = (lo + hi)/2;
			
			if (lo > hi)
				break;
			
		}
		
		return found;
			
		
	}
	
	//// insert link e to a sorted list
	public static boolean insertLink(List<Int3> list, Int3 e){
		// normalize e
		normalizeEdge(e);
		
		//
		int lo = 0;
		int hi = list.size()-1;
		int mid = (lo + hi)/2;
		int comp = 0;
		boolean found = false;
		while (true){
			comp = e.compareTo(list.get(mid));
			
			if (comp < 0){
				hi = mid-1;
			}else if(comp > 0){
				lo = mid+1;
			}else{
				found = true;
				break;
			}
			
			mid = (lo + hi)/2;
			
			if (lo > hi)
				break;
			
		}
		
		if (!found){
			comp = list.get(mid).compareTo(e);
			if (comp < 0)
				list.add(mid+1, e);
			else if (comp > 0)
				list.add(mid, e);
			else
				System.err.println("ERROR in insertLink");
			return true;
		}else{
			list.get(mid).c += 1;		// increase counter
			
			return false;
		}
			
		
	}
	
	////
	public static void normalizeEdge(Int3 e){
		if (e.val0 > e.val1){
			int temp = e.val0;
			e.val0 = e.val1;
			e.val1 = temp;
		}
	}
	
	////
	public static void normalizeEdges(List<Int3> list){
		for (Int3 e:list){
			normalizeEdge(e);
		}
		
	}
	
	////
	public static void linkExchangeNoDup(EdgeIntGraph G, int round, double alpha, double beta, int nSample, String count_file, String sample_file, int iRun) throws IOException{
		int n = G.V();
		System.out.println("round = " + round);
		System.out.println("alpha = " + alpha);
		System.out.println("beta = " + beta);
		System.out.println("count_file = " + count_file);
		
		//
		List<List<Int3>> links = new ArrayList<List<Int3>>();
		
		long start = System.currentTimeMillis();
		
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int3> temp = new ArrayList<Int3>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int3(u, v));
			links.add(temp);
			
			// normalize and sort
			normalizeEdges(links.get(u));
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
					
					Int3 e = new Int3(u, w);
					boolean isNew = checkLink(links.get(u), e);
					if (isNew == false){	// if not existed
						insertLink(links.get(u), e);
						break;
					}else
						w = random.nextInt(n);
					
				}
			}
		}
		// debug - OK, all counts are 0
//		for (int u = 0; u < n; u++){
//			for (Int3 e : links.get(u))
//				System.out.print("(" + e.val0 + "," + e.val1 + ":" + e.c + ") ");
//			System.out.println();
//		}
		
		
		// loop
		for(int t = 1; t < round+1; t++){
			System.out.println("round = " + t);
			
			List<List<Int3>> exLinks = new ArrayList<List<Int3>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int3>());
			
			// for each pair of nodes (u,v)
			for (EdgeInt e: G.edges()){
				int u = e.either();
				int v = e.other(u);
				
				List<Int3> listU = sampleLinkNoDup(links.get(u), alpha);
				List<Int3> listV = sampleLinkNoDup(links.get(v), alpha);
				
				//
				exLinks.get(u).addAll(listV);
				exLinks.get(v).addAll(listU);
				
			}
			// expand lists, do not accept duplicate links
			for (int u = 0; u < n; u++){
				for (Int3 e : exLinks.get(u))
					insertLink(links.get(u), e);
			}
			
			//
//			alpha = alpha * discount;
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		long totalLink = countTrueFalseDupLinks(G, links, trueLinks, falseLinks, dupLinks);
		System.out.println("linkExchangeNoDup - DONE, elapsed " + (System.currentTimeMillis() - start));
		System.out.println("totalLink = " + totalLink);
		
		// write to count_file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file + "." + iRun + ".cnt"));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
		// sample nodes and save local graphs
		int[] deg = new int[n];
		
		for (int u = 0; u < n; u++)
			deg[u] = G.degree(u);
		int[] selectedNodes = sampleNodeByDegree(deg, nSample);
		
		saveLocalGraph(links, selectedNodes, sample_file + "." + iRun + ".out");
		
	}
	
	////
	public static void linkGossipNoDup(EdgeIntGraph G, int round, double alpha, double beta, double discount, String count_file) throws IOException{
		int n = G.V();
		
		List<List<Int3>> links = new ArrayList<List<Int3>>();
		// compute adj lists
		List<List<Integer>> adj = new ArrayList<List<Integer>>();
		for (int u = 0; u < n; u++){
			List<Integer> nblist = new ArrayList<Integer>(G.adj(u).keySet());
			adj.add(nblist);
		}
				
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int3> temp = new ArrayList<Int3>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int3(u, v));
			links.add(temp);
			
			// normalize and sort
			normalizeEdges(links.get(u));
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
					
					Int3 e = new Int3(u, w);
					boolean isNew = checkLink(links.get(u), e);
					if (isNew == false){	// if not existed
						insertLink(links.get(u), e);
						break;
					}else
						w = random.nextInt(n);
					
				}
			}
		}
		
		
		// loop
		Random random = new Random();
		for(int t = 1; t < round+1; t++){
			List<List<Int3>> exLinks = new ArrayList<List<Int3>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int3>());
			
			// for each node u
			for (int u = 0; u < n; u++){
				int v = adj.get(u).get(random.nextInt(G.degree(u)));
				
				List<Int3> listU = sampleLinkNoDup(links.get(u), alpha);
				
				//
				exLinks.get(v).addAll(listU);
				
			}
			// expand lists, do not accept duplicate links
			for (int u = 0; u < n; u++){
				for (Int3 e:exLinks.get(u))
					insertLink(links.get(u), e);
			}
			
			//
			alpha = alpha * discount;
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		
		countTrueFalseDupLinks(G, links, trueLinks, falseLinks, dupLinks);
		
		System.out.println("linkGossipNoDup - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	
	//// run by step (not round)
	public static void linkGossipAsync(EdgeIntGraph G, int step, double alpha, double beta, String count_file) throws IOException{
		int n = G.V();
		
		List<List<Int3>> links = new ArrayList<List<Int3>>();
		// compute adj lists
		List<List<Integer>> adj = new ArrayList<List<Integer>>();
		for (int u = 0; u < n; u++){
			List<Integer> nblist = new ArrayList<Integer>(G.adj(u).keySet());
			adj.add(nblist);
		}
				
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int3> temp = new ArrayList<Int3>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int3(u, v));
			links.add(temp);
			
			// normalize and sort
			normalizeEdges(links.get(u));
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
					
					Int3 e = new Int3(u, w);
					boolean isNew = checkLink(links.get(u), e);
					if (isNew == false){	// if not existed
						insertLink(links.get(u), e);
						break;
					}else
						w = random.nextInt(n);
					
				}
			}
		}
		
		
		// loop
		Random random = new Random();
		for(int t = 1; t < step+1; t++){
			// select node u
			int u = random.nextInt(n);
			int v = adj.get(u).get(random.nextInt(G.degree(u)));
			
			List<Int3> listU = sampleLinkNoDup(links.get(u), alpha);
			
			
			// expand lists, do not accept duplicate links
			for (Int3 e:listU)
				insertLink(links.get(v), e);
			
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		
		countTrueFalseDupLinks(G, links, trueLinks, falseLinks, dupLinks);
		
		System.out.println("linkGossipAsync - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	//// subgraph aG at node u
	public static AttackMetric inferEdges(EdgeIntGraph G, EdgeIntGraph aG, int u, double beta){
		AttackMetric ret = new AttackMetric();
		
		List<Int3> edges = new ArrayList<Int3>();
		
		for (EdgeInt e : aG.edges()){
			int v = e.either();
			int w = e.other(v);
			edges.add(new Int3(v,w,e.weight()));
		}
		// sort by weight (ascending)
		Collections.sort(edges, Int3.Comparators.COUNT);
		
//		System.out.println(edges.get(0).c + " " + edges.get(1).c + " " + edges.get(edges.size()-1).c);
		
		// top 1/(1+2*beta) edges (from mid to end)
//		int mid = (int) (2*beta/(1+2*beta) * edges.size());
		int mid = (int) (beta/(1+beta) * edges.size());
		
		for (int i = 0; i < mid; i++){
			Int3 e = edges.get(i);
			if (e.val0 == u || e.val1 == u)			// skip friends (true links)
				continue;
			if (G.areEdgesAdjacent(e.val0, e.val1))
				ret.FN += 1;
			else
				ret.TN += 1;
		}
		
		for (int i = mid; i < edges.size(); i++){
			Int3 e = edges.get(i);
			if (e.val0 == u || e.val1 == u)			// skip friends (true links)
				continue;
			if (G.areEdgesAdjacent(e.val0, e.val1))
				ret.TP += 1;
			else
				ret.FP += 1;
		}
		
		//
		return ret;
		
	}
	
	//// read sample file, compute edge inference probabilities, export to attack_file (MATLAB)
	public static void attackLocalGraph(EdgeIntGraph G, double beta, String sample_file, String attack_file, int round, int iRun) throws IOException{
		int n_nodes = G.V();
		
		for (int t = 1; t <= round; t++){
			BufferedReader br = new BufferedReader(new FileReader(sample_file + "-" + t + "." + iRun + ".out"));
			
			String str = br.readLine();
			int k = Integer.parseInt(str);		// number of sample graphs
			System.out.println("#selected nodes = " + k);
			
			// for MATLAB
			double[] a_attack = new double[k*4];
			
			int max_all_c = 0;
			for(int i = 0; i < k; i++){
				System.out.println("subgraph i = " + i);
				str = br.readLine();
				String[] items = str.split(",");
				int u = Integer.parseInt(items[0]);
				int size = Integer.parseInt(items[1]);
				System.out.println("u = " + u + ", size = " + size + ", deg_u = " + G.degree(u));
				
				// read local graph
				EdgeIntGraph aG = new EdgeIntGraph(n_nodes); 
				int max_c = 0;
				for (int j = 0; j < size; j++){
					str = br.readLine();
					items = str.split("\t");
					int v = Integer.parseInt(items[0]);
					int w = Integer.parseInt(items[1]);
					int	c = Integer.parseInt(items[2]);		// edge counter (see Int3)
					if (max_c < c)
						max_c = c;
					
					aG.addEdge(new EdgeInt(v,w,c));			// save c as edge weight
				}
				System.out.println("#nodes = " + aG.V());
				System.out.println("#edges = " + aG.E());
				System.out.println("max_c = " + max_c);
				if (max_all_c < max_c)
					max_all_c = max_c;
				
				// infer true links by edge weights
				AttackMetric at = inferEdges(G, aG, u, beta);
				
				a_attack[i + 0*k] = at.TP;	// packed by column
				a_attack[i + 1*k] = at.TN;
				a_attack[i + 2*k] = at.FP;
				a_attack[i + 3*k] = at.FN;
			}
			System.out.println("max_all_c = " + max_all_c);
			br.close();
			
			// write to MATLAB
			MLDouble atArr = new MLDouble("atArr", a_attack, k);
	
			ArrayList<MLArray> towrite = new ArrayList<MLArray>();
	        towrite.add(atArr); 
	        
	        new MatFileWriter(attack_file+ "-" + t + "_attack." + iRun + ".mat", towrite );
	        System.out.println("Written to MATLAB file.");
		}
	}

	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "";

		
//		String dataname = "pl_1000_5_01";		// diameter = 5
//		String dataname = "pl_10000_5_01";		// diameter = 6,  Dup: round=3 (OutOfMem, 7GB ok), 98s (Acer)
												//				NoDup: round=3 (a=0.5, b=1.0, 4.5GB), 376s (Acer)
//		String dataname = "ba_1000_5";			// diameter = 5
//		String dataname = "ba_10000_5";			// diameter = 6, NoDup: round=3 (5.1GB), 430s (Acer), 350s (PC), totalLink = 255633393
		
//		String dataname = "er_1000_001";		// diameter = 5
//		String dataname = "er_10000_0001";		// diameter = 7, NoDup: round=3 (2.5GB), 23s (PC)
		
//		String dataname = "sm_1000_005_11";		// diameter = 9
//		String dataname = "sm_10000_005_11";	// diameter = 12, NoDup: round=3 (1.2GB), 5s (PC), round=4 (1.7GB), 12s (PC)
												// 						round=5 (3.0GB), 29s (PC), round=6 (3.3GB), 74s (PC)
		//
		String dataname = "example";			// 	diameter = 5, 
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
		
//		computeTrueGraph(G, "_matlab/" + dataname + ".mat");
		
		//
		int round = 2; 		// flood
//		int round = 10; 	// gossip
		int step = 100000;	// gossip-async
		double alpha = 1.0;
		double beta = 1.0;
		double discount = 1.0;
		int nSample = 13;	// 20, 50, 100  number of local graphs written to file
		int nRun = 1;
		
		// TEST linkExchange()
//		String count_file = prefix + "_out/" + dataname + "-" + round + "_" + String.format("%.1f",alpha) + "_" + String.format("%.1f",beta) + ".cnt";
//		
//		linkExchange(G, round, alpha, beta, count_file);
		
		// TEST normalizeEdges(), insertLink()
//		List<Int3> list = new ArrayList<Int3>();
//		list.add(new Int3(2,3));
//		list.add(new Int3(3,4));
//		list.add(new Int3(4,2));
//		printEdges(list);
//		
//		normalizeEdges(list);
//		printEdges(list);
//		
//		Collections.sort(list);
//		printEdges(list);
//		
//		Int3 e1 = new Int3(2,5);
//		insertLink(list, e1);
//		printEdges(list);
		
		
		// TEST linkExchangeNoDup()
		String name = dataname + "-nodup-" + round + "_" + String.format("%.1f",alpha) + "_" + String.format("%.1f",beta) + "_" + nSample;
		String count_file = prefix + "_attack/" + name;				// _out -> _attack
		String sample_file = prefix + "_attack/" + name;			// _sample -> _attack
		String matlab_file = prefix + "_matlab/" + name + ".mat";
		String attack_file = prefix + "_attack/" + name;	// _matlab -> _attack
		System.out.println("count_file = " + count_file);
		
		//
		for (int i = 0; i < nRun; i++){
			System.out.println("run i = " + i);
			linkExchangeNoDup(G, round, alpha, beta, nSample, count_file, sample_file, i);	
			
//			computeLocalGraph(sample_file, matlab_file, 10000);
			
//			attackLocalGraph(G, beta, sample_file, attack_file, round, i);
		}
		
		
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
