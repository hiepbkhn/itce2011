package test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import algs4.EdgeInt;
import algs4.EdgeIntGraph;
import algs4.UnweightedGraph;

public class ClusteringCoefficient {

	////
	public static double globalClusteringCoeff(EdgeIntGraph G){
		double ret = 0.0;
		
		int n_nodes = G.V();
		double triples = 0.0;
		double triangles = 0.0;
		
		for (int u = 0; u < n_nodes; u++){
			Set<Integer> nb = G.adj(u).keySet();
			
			triples += nb.size() * (nb.size()-1)/2;
			
			for (int v : nb)
				for (int t : nb)
					if (v > u && t > v && G.areEdgesAdjacent(v, t))
						triangles += 1;
		}
			
		ret = 3*triangles/triples;
				
		//
		return ret;
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
//		String dataname = "example";		// (13, 20)
		
//		String dataname = "com_amazon_ungraph";		// (334863,925872)		// 0.20522444916452579 (1s)  max_deg = 549
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)		// 0.3063766130023435	(2s)	max_deg = 343
		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 	// 0.006218559818028638 (72s Acer, 57s pc) max_deg = 28754
		
		String prefix = "";
		
		String filename = prefix + "_data/" + dataname + ".gr";
		
		long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		System.out.println("global CC = " + globalClusteringCoeff(G));
		System.out.println("globalClusteringCoeff - DONE, elapsed " + (System.currentTimeMillis() - start));
		
	}

}
