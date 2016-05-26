/*
 * May 25, 2016
 * 	- add approxVertexCover(), greedyVertexCover()
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


public class VertexCover {

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
					deg[v] = deg[v] - 1;
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
//					deg[v] = deg[v] - 1;
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
	
	////select top-degree nodes (using Heap class)
	public static void greedyVertexCover(EdgeIntGraph G){
		int n = G.V();
		List<Integer> cover = new ArrayList<Integer>();
		Int2[] deg = new Int2[n];
		boolean[] marked = new boolean[n];
		
		for (int u = 0; u < n; u++){
			deg[u] = new Int2(u, G.degree(u));
			marked[u] = false;
		}
		
		Heap heap = new Heap(deg);
//		heap.print();
		
		heap.buildheap();
//		heap.print();
		
		int count = 0;
		while (true){
			// find top-degree node
			heap.exchange(0, n-1);
//			heap.print();
			
			int top = heap.a[n-1].val0;
			heap.a[n-1].val1 = -1;
//			System.out.print("top = " + top);
			n = n - 1;
			
			//
			cover.add(top);
			marked[top] = true;
			
			// debug
//			if (cover.size() % 1000 == 0)
//				System.out.println(cover.size());
			
			// update degrees
			for (int v : G.adj(top).keySet())
				if (marked[v] == false){
					heap.update(v);
					count += 1;
				}
			heap.maxheap(0);
			
//			System.out.println(" , count = " + count + " n = " + n);
//			System.out.println("after update v");
//			heap.print();
			
			if (count == G.E())
				break;
		}

		//
		System.out.println("cover.size = " + cover.size());
		if (cover.size() < 100){
			for (int u : cover)
				System.out.print(u + " ");
			System.out.println();
		}
		
		//
		checkVertexCover(G, cover);
	}
	
	////
	public static void checkVertexCover(EdgeIntGraph G, List<Integer> cover){
		Collections.sort(cover);
		
		Integer[] temp = new Integer[cover.size()];
		cover.toArray(temp);
		//
		int n = G.V();
		
		boolean valid = true;
		for (int u = 0; u < n; u++){
			if (Arrays.binarySearch(temp, u) >= 0)
				continue;
			for (int v : G.adj(u).keySet())
				if (Arrays.binarySearch(temp, v) < 0)
					valid = false;
		}
		System.out.println("valid = " + valid);
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "";

		
//		String dataname = "pl_1000_5_01";		// diameter = 5
//		String dataname = "pl_10000_5_01";		// diameter = 6,  cover.size = 8278,	cover.size = 6046 (greedyN)
		
//		String dataname = "ba_1000_5";			// diameter = 5
//		String dataname = "ba_10000_5";			// diameter = 6, cover.size = 8384,	cover.size = 6043 (greedy)	
		
//		String dataname = "er_1000_001";		// diameter = 5
//		String dataname = "er_10000_0001";		// diameter = 7, cover.size = 9076,	cover.size = 7227 (greedyN), cover.size = 7219 (greedy)
		
//		String dataname = "sm_1000_005_11";		// diameter = 9
		String dataname = "sm_10000_005_11";	// diameter = 12, cover.size = 9448,	cover.size = 8207 (greedyN), cover.size = 8246 (greedy)	
		//
//		String dataname = "example";			// 		diameter = 5, cover.size = 10, cover.size = 8 (greedy)
//		String dataname = "karate";				// (34, 78)	diameter = 5, cover.size = 20, cover.size = 14 (greedy)
//		String dataname = "polbooks";			// (105, 441)			cover.size = 62
//		String dataname = "polblogs";			// (1224,16715) 		cover.size = 563
//		String dataname = "as20graph";			// (6474,12572)			cover.size = 1055 (greedy)
//		String dataname = "wiki-Vote";			// (7115,100762)		cover.size = 2372
//		String dataname = "ca-HepPh";			// (12006,118489) 		cover.size = 7037
//		String dataname = "ca-AstroPh";			// (18771,198050) 		cover.size = 12057	
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	, cover.size = 240510 (43s, Acer), cover.size = 162093 (greedyN)
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	, cover.size = 225870 (63s, Acer), cover.size = 165269 (greedy)
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624), 									cover.size = 279128 (greedy, 1s) 
		
		
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
		
		
		// TEST approxVertexCover()
//		approxVertexCoverNaive(G);
		
//		start = System.currentTimeMillis();
//		approxVertexCover(G);
//		System.out.println("approxVertexCover - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
//		greedyVertexCoverNaive(G);
		greedyVertexCover(G);
		System.out.println("greedyVertexCoverNaive - DONE, elapsed " + (System.currentTimeMillis() - start));
		
	}

}
