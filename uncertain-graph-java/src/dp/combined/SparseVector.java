/*
 * Sparse Vector Technique applied to top-down community detection
 * May 9, 2016
 * 	- write NodeSetCut.java (thresholds set in NodeSetCut.recursiveCut())
 * 	
 */

package dp.combined;

import algs4.EdgeWeightedGraph;

public class SparseVector {

	////
	
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		System.out.println("SparseVector");
		
		// load graph
//		String dataname = "karate";			// (34, 78)
//		String dataname = "polbooks";		// (105, 441)		
//		String dataname = "polblogs";		// (1224,16715) 	
		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 			
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 

		////
		double eps = 1.0;	// 0.25, 0.5, 1.0, 2.0, 4.0
		double ratio_eps = 0.25;
		int max_level = 5;
		double ratio_level = 1.0;
		
		
		String prefix = "";
		
		System.out.println("dataname = " + dataname);
		System.out.println("eps = " + eps);
	    
		String filename = prefix + "_data/" + dataname + ".gr";
		
		//
		long start = System.currentTimeMillis();
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		int min_frac = 6;		// R.n_nodes/2 >= R.|S| >= R.n_nodes/min_frac
		
		String part_file = prefix + "_out/" + dataname +"_svt_" + min_frac + "_"  
				+ max_level + "_" + String.format("%.2f", ratio_level) + "_" + String.format("%.1f", eps) + ".part";
		
		//
		start = System.currentTimeMillis();
		NodeSetCut root_set = NodeSetCut.recursiveCut(G, eps, ratio_eps, min_frac, max_level, ratio_level);	
		System.out.println("recursiveCut - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		NodeSetCut.writePart(root_set, part_file);
		System.out.println("writePart - DONE");
		
		// compute modularity
		int[] part = CommunityMeasure.readPart(part_file, G.V());
		double true_mod = CommunityMeasure.modularity(G, part); 
		System.out.println("true_mod = " + true_mod);
		
	}

}
