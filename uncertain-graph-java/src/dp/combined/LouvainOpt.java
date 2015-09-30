/*
 * Sep 30, 2015
 * 	- copied from ModDivisiveOpt, use NodeSetLouvainOpt (non-private, k-ary tree)
 */

package dp.combined;

import java.util.List;

import algs4.EdgeWeightedGraph;
import grph.Grph;
import grph.io.EdgeListReader;
import toools.io.file.RegularFile;

public class LouvainOpt {

	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("LouvainOpt");
		
		// load graph
//		String dataname = "karate";			// (34, 78)	
//		String dataname = "polbooks";		// (105, 441)		
											// 		
//		String dataname = "polblogs";		// (1224,16715) 	
											// 		
		String dataname = "as20graph";		// (6474,12572)		burn = 20, max_level = 8, (40,10) final mod = 0.317, bestCut=0.451 (6s)
//		String dataname = "wiki-Vote";		// (7115,100762) 	
											// 		
//		String dataname = "ca-HepPh";		// (12006,118489) 	 
															
//		String dataname = "ca-AstroPh";		// (18771,198050) 	burn = 30, max_level = 8, (40,10) final mod = 0.412, bestCut=0.498 (95s)
		
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 	burn = 1, max_level = 8, (100,20) final mod = 0.112, bestCut=0.256 ()
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624)burn = 10, max_level = 8, (100,20) final mod = 0.356, bestCut=0.494 (840s)
													 
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 1;
		int burn_factor = 20;
		int limit_size = 40;		// at least 4*lower_size
		int lower_size = 10;		// at least 2
		int max_level = 6;
		int k = 3;
		
		if(args.length >= 4){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			burn_factor = Integer.parseInt(args[3]);
		}
		if(args.length >= 5)
			limit_size = Integer.parseInt(args[4]);
		
		System.out.println("dataname = " + dataname);
		System.out.println("burn_factor = " + burn_factor + " n_samples = " + n_samples);
		System.out.println("limit_size = " + limit_size);
		System.out.println("lower_size = " + lower_size);
		System.out.println("max_level = " + max_level);
		System.out.println("k = " + k);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		
		
		// TEST recursiveLouvain()
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		NodeSetLouvainOpt R = new NodeSetLouvainOpt(G, k);
		System.out.println("mod = " + R.modularity(G.E()));
		
		// TEST recursiveMod(), recursiveLouvain()
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
			
			long start = System.currentTimeMillis();
			// type 1 - call partitionLouvain
			String part_file = prefix + "_out/" + dataname +"_partoptlouvain_" + burn_factor + "_" + limit_size + "_" 
					+ max_level + "_" + k + ".part";
			NodeSetLouvainOpt root_set = NodeSetLouvainOpt.recursiveLouvain(G, burn_factor, limit_size, max_level, k, 1);
			
			// type 2 - call maximizeLouvain
//			String part_file = prefix + "_out/" + dataname +"_multioptlouvain_" + burn_factor + "_" + limit_size + "_" 
//					+ max_level + "_" + k + ".part";
//			NodeSetLouvainOpt root_set = NodeSetLouvainOpt.recursiveLouvain(G, burn_factor, limit_size, max_level, k, 2);
			
			System.out.println("recursiveLouvain - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			
			NodeSetLouvainOpt.printSetIds(root_set, G.E());
			System.out.println("final modularity = " + root_set.modularityAll(G.E()));
			
			List<NodeSetLouvainOpt> best_cut = NodeSetLouvainOpt.bestCut(root_set, G.E());
			NodeSetLouvainOpt.writeBestCut(best_cut, part_file);
		}
		
	}

}
