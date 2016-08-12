/*
 * Aug 11, 2016
 * 	- implement Identify Vertex Labeling of DER algorithm
 */

package dp.der;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import grph.Grph;
import grph.VertexPair;
import grph.io.EdgeListReader;
import toools.io.file.RegularFile;

public class VertexLabeling {

	//// L: labeling (a permutation)
	public static double L1Dist(Grph G, int[] L){
		int n_nodes = G.getNumberOfVertices();
		double q = 0.0;
		
		for (VertexPair p : G.getEdgePairs()){
			int i = L[p.first];
			int j = L[p.second];
			
			q += Math.abs(i-n_nodes/2)+ Math.abs(j-n_nodes/2);
		}
		
		q = q/(n_nodes-2); 
		//
		return q;
	}
	
	//// non-private (PARALLEL)
	public static void parallelVertexLabeling(Grph G){
		int n_nodes = G.getNumberOfVertices();
		int t = 20;
		
		List<Integer> nodes = new ArrayList<Integer>();
		int[] L = new int[n_nodes];
		for (int u = 0; u < n_nodes; u++){
			nodes.add(u);
			L[u] = u;
		}
		
		double dist = L1Dist(G,L);
		
		for (int i = 0; i < t; i++){
			// random permutation
			Collections.shuffle(nodes);
			
			for (int u = 0; u < n_nodes; u++)
				L[u] = nodes.get(u);
			
			int count = 0;
			List<Integer> candidate = new ArrayList<Integer>();
			
			for (int j = 0; j < n_nodes/2; j++){	// n_nodes/2 candidate swaps
				// swap L[2j], L[2j+1]
				int temp = L[2*j];
				L[2*j] = L[2*j+1];
				L[2*j+1] = temp;
				
				double a_dist = L1Dist(G,L);
				if (a_dist < dist){
					count += 1;
					candidate.add(j);
				}
				
				// reset
				temp = L[2*j];
				L[2*j] = L[2*j+1];
				L[2*j+1] = temp;
			}
			// perform a series of swaps
			for (int j : candidate){
				// swap L[2j], L[2l+1]
				int temp = L[2*j];
				L[2*j] = L[2*j+1];
				L[2*j+1] = temp;
			}
			
				
			System.out.println("count = " + count + " Dist = " + L1Dist(G,L));
		}
	}
	
	////non-private (SEQUENTIAL)
	public static void sequentialVertexLabeling(Grph G){
		int n_nodes = G.getNumberOfVertices();
		int t = 20;
		
		List<Integer> nodes = new ArrayList<Integer>();
		int[] L = new int[n_nodes];
		
		for (int u = 0; u < n_nodes; u++){
			nodes.add(u);
			L[u] = u;
		}
		double dist = L1Dist(G,L);
		
		
		for (int i = 0; i < t; i++){
			// random permutation
			Collections.shuffle(nodes);
			
			for (int u = 0; u < n_nodes; u++)
				L[u] = nodes.get(u);
						
			int count = 0;
			
			for (int j = 0; j < n_nodes/2; j++){	// n_nodes/2 candidate swaps
				// swap L[2j], L[2j+1]
				int temp = L[2*j];
				L[2*j] = L[2*j+1];
				L[2*j+1] = temp;
				
				double a_dist = L1Dist(G,L);
				if (a_dist < dist){
					count += 1;
					dist = a_dist;
				}else{
					// reset
					temp = L[2*j];
					L[2*j] = L[2*j+1];
					L[2*j+1] = temp;
				}
			}
				
			System.out.println("count = " + count + " Dist = " + L1Dist(G,L));
		}
	}
	
	////
	public static int[] readLouvainLabel(int n_nodes, String file_name) throws IOException{
		int[] L = new int[n_nodes];
		int u = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(file_name));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	String[] items = str.split(",");
        	for (int i = 0; i < items.length; i++){
        		L[u++] = Integer.parseInt(items[i]);
        	}
		}
		br.close();
		
		//
		return L;
	}
	
	
	///////////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)
		String dataname = "polblogs";		// (1224,16715) 
//		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 		
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		//
//		String dataname = "wiki-Vote-wcc";	// (7066,100736)
		
		//
		String prefix = "";
		
		String filename = prefix + "_data/" + dataname + ".gr";
		
		Grph G;
		EdgeListReader reader = new EdgeListReader();
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());

		//
		int[][] A = DensityExploreReconstruct.compute_A(G);
		
		int n_nodes = G.getNumberOfVertices();
		int[] L = new int[n_nodes];
		for (int u = 0; u < n_nodes; u++)
			L[u] = u;
		
		System.out.println("L1Dist = " + L1Dist(G, L));	// = 49248.47
		
		L = readLouvainLabel(n_nodes, "_data/" + dataname + ".louvain");
		System.out.println("L1Dist (Louvain) = " + L1Dist(G, L));
		
		//
//		parallelVertexLabeling(G);
		sequentialVertexLabeling(G);
		
		
	}

}
