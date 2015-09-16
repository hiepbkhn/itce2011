/*
 * Sep 15, 2015
 * 	- paper "SCAN: A Structural Clustering Algorithm for Networks" (KDD'07)
 * 
 */

package dp.combined;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.io.EdgeListReader;
import toools.io.file.RegularFile;
import toools.set.IntHashSet;
import toools.set.IntSet;

public class Scan {

	////
	public static double similarity(IntSet Nu, IntSet Nv){
		int intSize = 0;
		for (IntCursor t : Nu)
			if (Nv.contains(t.value))
				intSize += 1;
		
		return intSize/Math.sqrt(Nu.size()*Nv.size());
	}
	
	////
	public static boolean isCore(Grph G, IntSet[] nb, int u, double eps, int mu){

		int N_eps = 0; 	
		for (IntCursor t : G.getNeighbours(u))
			if (similarity(nb[u], nb[t.value]) >= eps)
				N_eps += 1;
		
		if (N_eps >= mu)
			return true;
		else
			return false;
	}
	
	////
	public static int[] scan(Grph G, double eps, int mu){
		int n = G.getNumberOfVertices();
		
		int clusterId = 0;
		int[] node2cl = new int[n];		// -1: unclassified, -2: non-member
		for (int u = 0; u < n; u++)
			node2cl[u] = -1;
		
		// init node's set of neighbors
		IntSet[] nb = new IntSet[n];
		for (int u = 0; u < n; u++){
			nb[u] = G.getNeighbours(u);
			nb[u].add(u);				// always count for u
		}
		
		//
		for (int u = 0; u < n; u++){
			if (node2cl[u] != -1)
				continue;
			if (isCore(G, nb, u, eps, mu)){		// is a core
				Queue<Integer> queue = new LinkedList<Integer>();
				for (IntCursor t : nb[u])
					queue.add(t.value);
				while (queue.size() > 0){
					int y = queue.remove();
					if (isCore(G, nb, y, eps, mu))
						for (IntCursor x : nb[y])
							if (similarity(nb[y], nb[x.value]) >= eps){
								if (node2cl[x.value] == -1 || node2cl[x.value] == -2)
									node2cl[x.value] = clusterId;
								else if (node2cl[x.value] == -1)
									queue.add(x.value);
							}
							
				}
				
				
				clusterId += 1;
			}else{
				node2cl[u] = -2;		// non-member
			}
		}
		//
		return node2cl;
	}
	
	////
	public static void statistics(int[] node2cl){
		
		int numCl = 0;
		for (int u = 0; u < node2cl.length; u++){
//			System.out.println(u + " : " + node2cl[u]); 
			if (numCl < node2cl[u])
				numCl = node2cl[u];
		}
			
		List<List<Integer>> com = new ArrayList<List<Integer>>();
		List<Integer> nonMem = new ArrayList<Integer>();
		for (int i = 0; i < numCl+1; i++)
			com.add(new ArrayList<Integer>());
		
		for (int u = 0; u < node2cl.length; u++){
			if (node2cl[u] == -2){
				nonMem.add(u);
//				System.out.println("non-member : " + u);
				continue;
			}
			if (node2cl[u] == -1)
				System.err.println("unclassified : " + u);
			
			
			com.get(node2cl[u]).add(u);
			
			
		}
		// print
		System.out.println("numCl = " + (numCl+1));
//		System.out.println("clusters :");
//		for (int i = 0; i < numCl+1; i++){
//			for (int u : com.get(i))
//				System.out.print(u + " ");
//			System.out.println();
//		}
		System.out.println("non-members : " + nonMem.size());
//		for (int u : nonMem)
//			System.out.print(u + " ");
//		System.out.println();
		
	}
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		// load graph
//		String dataname = "karate";			// (105, 441)
//		String dataname = "polbooks";		// (105, 441)		
//		String dataname = "polblogs";		// (1224,16715) 	
//		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050)	5s 	
		// LARGE
		String dataname = "com_amazon_ungraph";		// (334863,925872)	
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624)
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
		String prefix = "";
	    int n_samples = 1;
	    
	    System.out.println("dataname = " + dataname);
	    
		String filename = prefix + "_data/" + dataname + ".gr";
		
		EdgeListReader reader = new EdgeListReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		long start = System.currentTimeMillis();
		G = reader.readGraph(f);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		
		// TEST scan()
		double eps = 0.05;
		int mu = 3;
		System.out.println("eps = " + eps + " mu = " + mu);
		
		start = System.currentTimeMillis();
		int[] node2cl = scan(G, eps, mu);
		System.out.println("scan - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		statistics(node2cl);
	}
}
