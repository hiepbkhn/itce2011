/*
 * Mar 18, 2016
 * 	- "Link Exchange" problem(s)
 * Mar 27
 * 	- add graphMetric(), sampleLinkNoDup(), linkExchangeNoDup()
 * Apr 6
 * 	- add linkGossip(), linkGossipNoDup(), linkGossipAsync()
 * May 25
 * 	- add approxVertexCover()
 */

package dsn;

import hist.Int2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dp.PathMetric;
import dp.UtilityMeasure;
import algs4.EdgeInt;
import algs4.EdgeIntGraph;
import algs4.UnweightedGraph;


public class LinkExchange {

	////
	public static void printEdges(List<Int2> list){
		for(Int2 e:list)
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
	public static void linkExchange(EdgeIntGraph G, int round, double alpha, double beta, String count_file) throws IOException{
		int n = G.V();
		
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
		}
		
		// loop
		for(int t = 1; t < round+1; t++){
			List<List<Int2>> exLinks = new ArrayList<List<Int2>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int2>());
			
			// for each pair of nodes (u,v)
			for (EdgeInt e: G.edges()){
				int u = e.either();
				int v = e.other(u);
				
				List<Int2> listU = sampleLink(links.get(u), alpha);
				List<Int2> listV = sampleLink(links.get(v), alpha);
				
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
		for (int u = 0; u < n; u++){
			Map<Int2, Integer> dup = new HashMap<Int2, Integer>();
			for(Int2 p : links.get(u)){
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
			
		}
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
		
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		
		// compute adj lists
		List<List<Integer>> adj = new ArrayList<List<Integer>>();
		for (int u = 0; u < n; u++){
			List<Integer> nblist = new ArrayList<Integer>(G.adj(u).keySet());
			adj.add(nblist);
		}
		
		
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
			
			List<Int2> newLinks = createFalseLink(G, u, beta);
			temp.addAll(newLinks);
			
			links.add(temp);
		}
		
		// loop
		Random random = new Random();
		for(int t = 1; t < round+1; t++){
			List<List<Int2>> exLinks = new ArrayList<List<Int2>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int2>());
			
			// for each node u
			for (int u = 0; u < n; u++){
				int v = adj.get(u).get(random.nextInt(G.degree(u)));
				
				List<Int2> listU = sampleLink(links.get(u), alpha);
				
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
		for (int u = 0; u < n; u++){
			Map<Int2, Integer> dup = new HashMap<Int2, Integer>();
			for(Int2 p : links.get(u)){
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
			
		}
		System.out.println("linkGossip - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	//// insert link e to a sorted list
	public static boolean insertLink(List<Int2> list, Int2 e){
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
		}else
			return false;
		
	}
	
	////
	public static void normalizeEdge(Int2 e){
		if (e.val0 > e.val1){
			int temp = e.val0;
			e.val0 = e.val1;
			e.val1 = temp;
		}
	}
	
	////
	public static void normalizeEdges(List<Int2> list){
		for (Int2 e:list){
			normalizeEdge(e);
		}
		
	}
	
	////
	public static void linkExchangeNoDup(EdgeIntGraph G, int round, double alpha, double beta, double discount, String count_file) throws IOException{
		int n = G.V();
		
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		
		long start = System.currentTimeMillis();
		
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
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
					
					Int2 e = new Int2(u, w);
					boolean isNew = insertLink(links.get(u), e);
					if (isNew == true)
						break;
					else
						w = random.nextInt(n);
					
				}
			}
		}
		
		
		// loop
		for(int t = 1; t < round+1; t++){
			List<List<Int2>> exLinks = new ArrayList<List<Int2>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int2>());
			
			// for each pair of nodes (u,v)
			for (EdgeInt e: G.edges()){
				int u = e.either();
				int v = e.other(u);
				
				List<Int2> listU = sampleLinkNoDup(links.get(u), alpha);
				List<Int2> listV = sampleLinkNoDup(links.get(v), alpha);
				
				//
				exLinks.get(u).addAll(listV);
				exLinks.get(v).addAll(listU);
				
			}
			// expand lists, do not accept duplicate links
			for (int u = 0; u < n; u++){
				for (Int2 e:exLinks.get(u))
					insertLink(links.get(u), e);
			}
			
			//
			alpha = alpha * discount;
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		long totalLink = 0;
		for (int u = 0; u < n; u++){
			Map<Int2, Integer> dup = new HashMap<Int2, Integer>();
			for(Int2 p : links.get(u)){
				// p is already normalized 
				if (p.val0 > p.val1)
					System.err.println("error");
				
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
		System.out.println("linkExchangeNoDup - DONE, elapsed " + (System.currentTimeMillis() - start));
		System.out.println("totalLink = " + totalLink);
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	////
	public static void linkGossipNoDup(EdgeIntGraph G, int round, double alpha, double beta, double discount, String count_file) throws IOException{
		int n = G.V();
		
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		// compute adj lists
		List<List<Integer>> adj = new ArrayList<List<Integer>>();
		for (int u = 0; u < n; u++){
			List<Integer> nblist = new ArrayList<Integer>(G.adj(u).keySet());
			adj.add(nblist);
		}
				
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
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
					
					Int2 e = new Int2(u, w);
					boolean isNew = insertLink(links.get(u), e);
					if (isNew == true)
						break;
					else
						w = random.nextInt(n);
					
				}
			}
		}
		
		
		// loop
		Random random = new Random();
		for(int t = 1; t < round+1; t++){
			List<List<Int2>> exLinks = new ArrayList<List<Int2>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int2>());
			
			// for each node u
			for (int u = 0; u < n; u++){
				int v = adj.get(u).get(random.nextInt(G.degree(u)));
				
				List<Int2> listU = sampleLinkNoDup(links.get(u), alpha);
				
				//
				exLinks.get(v).addAll(listU);
				
			}
			// expand lists, do not accept duplicate links
			for (int u = 0; u < n; u++){
				for (Int2 e:exLinks.get(u))
					insertLink(links.get(u), e);
			}
			
			//
			alpha = alpha * discount;
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		for (int u = 0; u < n; u++){
			Map<Int2, Integer> dup = new HashMap<Int2, Integer>();
			for(Int2 p : links.get(u)){
				// p is already normalized 
				if (p.val0 > p.val1)
					System.err.println("error");
				
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
			
		}
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
		
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		// compute adj lists
		List<List<Integer>> adj = new ArrayList<List<Integer>>();
		for (int u = 0; u < n; u++){
			List<Integer> nblist = new ArrayList<Integer>(G.adj(u).keySet());
			adj.add(nblist);
		}
				
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++){
			List<Int2> temp = new ArrayList<Int2>();
			for (int v:G.adj(u).keySet())
				temp.add(new Int2(u, v));
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
					
					Int2 e = new Int2(u, w);
					boolean isNew = insertLink(links.get(u), e);
					if (isNew == true)
						break;
					else
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
			
			List<Int2> listU = sampleLinkNoDup(links.get(u), alpha);
			
			
			// expand lists, do not accept duplicate links
			for (Int2 e:listU)
				insertLink(links.get(v), e);
			
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		for (int u = 0; u < n; u++){
			Map<Int2, Integer> dup = new HashMap<Int2, Integer>();
			for(Int2 p : links.get(u)){
				// p is already normalized 
				if (p.val0 > p.val1)
					System.err.println("error");
				
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
			
		}
		System.out.println("linkGossipAsync - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	////
	public static int binarySearch(List<Int2> edges, Int2 target){
		int ret = -1;
		
		int lo = 0;
		int hi = edges.size()-1;
		int mid = (lo + hi)/2;
		
		boolean found = false;
		while (true){
			Int2 e = edges.get(mid);
			
			if (e.compareTo(target) == -1){
				lo = mid + 1;
			}else if (e.compareTo(target) == 1){
				hi = mid - 1;
			}else{
				found = true;
				break;
			}
			
			mid = (lo + hi)/2;
			
			if (lo > hi)
				break;
		}
		
		if (!found)
			ret = -1;
		else{
			ret = mid;
		}
		
		//
		return ret;
	}
	
	////
	public static int binarySearchVertex(List<Int2> edges, int u){
		int ret = -1;
		
		int lo = 0;
		int hi = edges.size()-1;
		int mid = (lo + hi)/2;
		
		boolean found = false;
		while (true){
			Int2 e = edges.get(mid);
			
			if (e.val0 < u){
				lo = mid + 1;
			}else if (e.val0 > u){
				hi = mid - 1;
			}else{
				found = true;
				break;
			}
			
			mid = (lo + hi)/2;
			
			if (lo > hi)
				break;
		}
		
		if (!found)
			ret = -1;
		else{
			ret = mid;
			while (ret > 0 && edges.get(ret).val0 == u)
				ret = ret - 1;
		}
		
		//
		return ret;
	}
	
	////
	public static void approxVertexCoverNaive(EdgeIntGraph G){
		int n = G.V();
		
		List<Int2> edges = new ArrayList<Int2>();
		for (EdgeInt e : G.edges()){
			int u = e.either();
			int v = e.other(u);
			edges.add(new Int2(u,v));
		}
		
		//
		List<Integer> cover = new ArrayList<Integer>();
		Random random = new Random();
		while (edges.size() > 0){
			// pick a random edge
			int id = random.nextInt(edges.size());
			Int2 e = edges.get(id);
			
			int u = e.val0;
			int v = e.val1;
			cover.add(u);
			cover.add(v);
			
//			if (cover.size() % 1000 == 0)
//				System.out.println(cover.size());
			
			// remove all edges containing u,v
			id = 0;
			while (id < edges.size()){
				e = edges.get(id);
				int u1 = e.val0;
				int v1 = e.val1;
				if (u1 == u || u1 == v || v1 == u || v1 == v){
					edges.remove(id);
				}else
					id += 1;
			}
			
		}
		
		//
		System.out.println("cover.size = " + cover.size());
		if (cover.size() < 100){
			for (int u : cover)
				System.out.print(u + " ");
			System.out.println();
		}
		
	}
	
	
	//// faster
	public static void approxVertexCover(EdgeIntGraph G){
//		int n = G.V();
//		
//		List<Int2> edges = new ArrayList<Int2>();
//		for (EdgeInt e : G.edges()){
//			int u = e.either();
//			int v = e.other(u);
//			edges.add(new Int2(u,v));
//		}
//		
//		// sort edges
//		normalizeEdges(edges);
//		Collections.sort(edges);
//		
//		//
//		List<Integer> cover = new ArrayList<Integer>();
//		Random random = new Random();
//		while (edges.size() > 0){
//			// pick a random edge
//			int id = random.nextInt(edges.size());
//			Int2 e = edges.get(id);
//			
//			int u = e.val0;		// u < v
//			int v = e.val1;
//			cover.add(u);
//			cover.add(v);
//			
//				if (cover.size() % 1000 == 0)
//					System.out.println(cover.size());
//			
//			// remove all edges containing u,v
//			boolean stop = false;
//			for (int v1 : G.adj(u).keySet()){
//				e = new Int2(u, v1);
//				normalizeEdge(e);
//				
//				id = binarySearch(edges, e);
//				if (id != -1)
//					edges.remove(id);
//				
//				if (edges.size() == 0){
//					stop = true;
//					break;
//				}
//			}
//			if (stop)
//				break;
//			
//			for (int u1 : G.adj(v).keySet()){
//				e = new Int2(u1, v);
//				normalizeEdge(e);
//				
//				id = binarySearch(edges, e);
//				if (id != -1)
//					edges.remove(id);
//				
//				if (edges.size() == 0){
//					stop = true;
//					break;
//				}
//			}
//			if (stop)
//				break;
//			
//			
//		}
		
		//////////////// FASTER ?? no faster on amazon
		int n = G.V();
		
		List<Int2> edges = new ArrayList<Int2>();
		for (EdgeInt e : G.edges()){
			int u = e.either();
			int v = e.other(u);
			edges.add(new Int2(u,v));
		}
		
		List<HashSet<Integer>> adj = new ArrayList<HashSet<Integer>>();
		for (int u = 0; u < n; u++){
			adj.add(new HashSet<Integer>());
			for (int v : G.adj(u).keySet())
				adj.get(u).add(v);
		}
		
		
		// sort edges
		normalizeEdges(edges);
		Collections.sort(edges);
		
		System.out.println("here");
		
		//
		List<Integer> cover = new ArrayList<Integer>();
		Random random = new Random();
		while (edges.size() > 0){
			// pick a random edge
			int id = random.nextInt(edges.size());
			Int2 e = edges.get(id);
			
			int u = e.val0;		// u < v
			int v = e.val1;
			cover.add(u);
			cover.add(v);
			
			// debug
			if (cover.size() % 1000 == 0)
				System.out.println(cover.size());
			
			// remove all edges containing u,v
			boolean stop = false;
			List<Integer> temp = new ArrayList<Integer>();
			
			// node u
			id = binarySearchVertex(edges, u);
			if (id != -1 && edges.size() > 0){
				while(id < edges.size() && edges.get(id).val0 == u){
					int v1 = edges.get(id).val1;
					edges.remove(id);
					adj.get(u).remove(v1);
					adj.get(v1).remove(u);
				}
			}
			
			for (int v1 : adj.get(u)){
				e = new Int2(u, v1);
				normalizeEdge(e);
				
				id = binarySearch(edges, e);
				if (id != -1){
					edges.remove(id);
					temp.add(v1);
					adj.get(v1).remove(u);
					
					if (edges.size() == 0){
						stop = true;
						break;
					}
				}
				
			}
			adj.get(u).removeAll(temp);
			if (stop || edges.size() == 0)
				break;
			
			// node v
			id = binarySearchVertex(edges, v);
			if (id != -1 && edges.size() > 0){
				while(id < edges.size() && edges.get(id).val0 == v){
					int u1 = edges.get(id).val1;
					edges.remove(id);
					adj.get(v).remove(u1);
					adj.get(u1).remove(v);
				}
			}
			
			temp = new ArrayList<Integer>();
			for (int u1 : G.adj(v).keySet()){
				e = new Int2(u1, v);
				normalizeEdge(e);
				
				id = binarySearch(edges, e);
				if (id != -1){
					edges.remove(id);
					temp.add(u1);
					adj.get(u1).remove(v);
					
					if (edges.size() == 0){
						stop = true;
						break;
					}
				}
			}
			adj.get(v).removeAll(temp);
			if (stop)
				break;
			
			
		}
		
		//
		System.out.println("cover.size = " + cover.size());
		if (cover.size() < 100){
			for (int u : cover)
				System.out.print(u + " ");
			System.out.println();
		}
	}
	
	//// select top-degree nodes (deterministic)
	public static void greedyVertexCoverNaive(EdgeIntGraph G){
		int n = G.V();
		List<Integer> cover = new ArrayList<Integer>();
		int[] deg = new int[n];
		boolean[] marked = new boolean[n];
		
		for (int u = 0; u < n; u++){
			deg[u] = G.degree(u);
			marked[u] = false;
		}
		
		
		int count = 0;
		while (true){
			// find top-degree node
			int top = -1;
			for (int u = 0; u < n; u++)
				if (marked[u] == false){
					top = u;
					break;
				}
			for (int u = 0; u < n; u++)
				if (marked[u] == false && deg[top] < deg[u])
					top = u;
			//
			cover.add(top);
			marked[top] = true;
			
			// debug
			if (cover.size() % 1000 == 0)
				System.out.println(cover.size());
			
			// update degrees
			for (int v : G.adj(top).keySet())
				if (marked[v] == false){
					count += 1;
				}
			
			if (count == G.E())
				break;
		}
		
		/////////////// SLOWER due to SET OPERATIONS
//		int n = G.V();
//		List<Integer> cover = new ArrayList<Integer>();
//		int[] deg = new int[n];
//		boolean[] marked = new boolean[n];
//		HashSet<Integer> nodes = new HashSet<Integer>();
//		
//		for (int u = 0; u < n; u++){
//			deg[u] = G.degree(u);
//			marked[u] = false;
//			nodes.add(u);
//		}
//		
//		int count = 0;
//		while (true){
//			// find top-degree node
//			int top = -1;
//			for (int u : nodes)
//				if (top == -1)
//					top = u;
//				else if (deg[top] < deg[u])
//					top = u;
//			//
//			cover.add(top);
//			marked[top] = true;
//			nodes.remove(top);
//			
//			// debug
//			if (cover.size() % 1000 == 0)
//				System.out.println(cover.size());
//			
//			// update degrees
//			for (int v : G.adj(top).keySet())
//				if (marked[v] == false){
//					count += 1;
//				}
//			
//			if (count == G.E())
//				break;
//		}
		
		
		//
		System.out.println("cover.size = " + cover.size());
		if (cover.size() < 100){
			for (int u : cover)
				System.out.print(u + " ");
			System.out.println();
		}
		
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "";

		
//		String dataname = "pl_1000_5_01";		// diameter = 5
//		String dataname = "pl_10000_5_01";		// diameter = 6,  Dup: round=3 (OutOfMem, 7GB ok), 98s (Acer)
												//				NoDup: round=3 (4.5GB), 376s (Acer)
												// cover.size = 8278,	cover.size = 7171 (greedy)
//		String dataname = "ba_1000_5";			// diameter = 5
//		String dataname = "ba_10000_5";			// diameter = 6, NoDup: round=3 (5.1GB), 430s (Acer), 350s (PC), totalLink = 255633393
												// cover.size = 8384,	cover.size = 7167 (greedy)	
		
//		String dataname = "er_1000_001";		// diameter = 5
//		String dataname = "er_10000_0001";		// diameter = 7, NoDup: round=3 (2.5GB), 23s (PC)
												// cover.size = 9076,	cover.size = 9952 (greedy)	
//		String dataname = "sm_1000_005_11";		// diameter = 9
//		String dataname = "sm_10000_005_11";	// diameter = 12, NoDup: round=3 (1.2GB), 5s (PC), round=4 (1.7GB), 12s (PC)
												// 						round=5 (3.0GB), 29s (PC), round=6 (3.3GB), 74s (PC)
												// cover.size = 9448,	cover.size = 9987 (greedy)	
		//
//		String dataname = "example";			// 		diameter = 5, cover.size = 10
//		String dataname = "karate";				// (34, 78)	diameter = 5, cover.size = 20
//		String dataname = "polbooks";			// (105, 441)		
//		String dataname = "polblogs";			// (1224,16715) 	
//		String dataname = "as20graph";			// (6474,12572)		
//		String dataname = "wiki-Vote";			// (7115,100762)
//		String dataname = "ca-HepPh";			// (12006,118489) 	
//		String dataname = "ca-AstroPh";			// (18771,198050) 			
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	, cover.size = 240510 (43s, Acer), cover.size = 309152 (greedy)
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	, cover.size = 225870 (63s, Acer)
		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 
		
		
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
		
		
		//
		int round = 3; 		// flood
//		int round = 10; 	// gossip
		int step = 100000;	// gossip-async
		double alpha = 0.5;
		double discount = 1.0;
		double beta = 1.0;
		
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
//		String count_file = prefix + "_out/" + dataname + "-nodup-" + round + "_" + String.format("%.1f",alpha) + "_" + 
//				String.format("%.1f",beta) + "_" + String.format("%.1f",discount) + ".cnt";
//		
//		linkExchangeNoDup(G, round, alpha, beta, discount, count_file);
		
		
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
		
		
		
		/////////
		// TEST approxVertexCover()
//		approxVertexCoverNaive(G);
		
//		start = System.currentTimeMillis();
//		approxVertexCover(G);
//		System.out.println("approxVertexCover - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		greedyVertexCoverNaive(G);
		System.out.println("greedyVertexCoverNaive - DONE, elapsed " + (System.currentTimeMillis() - start));
		
	}

}
