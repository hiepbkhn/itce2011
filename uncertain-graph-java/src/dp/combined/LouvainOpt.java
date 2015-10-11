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
		String dataname = "karate";			// (34, 78)	
//		String dataname = "polbooks";		// (105, 441)		
											// 		
//		String dataname = "polblogs";		// (1224,16715) 	
											// 		
//		String dataname = "as20graph";		// (6474,12572)		burn = 20, max_level = 8, (40,10) final mod = 0.317, bestCut=0.451 (6s)
											//					par.Louvain (k,max_level,mod): (2,8,0.484), (3,5,0.499), (5,3,0.504), (6,3,0.472), (10,2,0.440), (20,2,0.387)
											//					max.Louvain (k,max_level,mod): (2,8,0.323), (3,5,0.467), (5,4,0.514), (6,3,0.509), (10,2,0.527), (20,2,0.537)
//		String dataname = "wiki-Vote";		// (7115,100762) 	
											// 		
//		String dataname = "ca-HepPh";		// (12006,118489) 	 
															
//		String dataname = "ca-AstroPh";		// (18771,198050) 	burn = 30, max_level = 8, (40,10) final mod = 0.412, bestCut=0.498 (95s)
		
		// LARGE
//		String dataname = "com_amazon_ungraph"; // (334863,925872) 	par.Louvain (k,max_level,mod): (2,10,0.712), (3,6,0.711), (5,4,0.673), (6,3,0.641), (10,3,0.558), (20,2,0.427)
												//					max.Louvain (k,max_level,mod): (2,10,0.307), (3,6,0.451), (5,4,0.687), (6,3,0.706), (10,3,0.728), (20,2,0.743)
//		String dataname = "com_dblp_ungraph";  	// (317080,1049866) 
//		String dataname = "com_youtube_ungraph";// (1134890,2987624)burn = 10, max_level = 8, (100,20) final mod = 0.356, bestCut=0.494 (840s)
													 
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 1;
		int burn_factor = 20;
		int limit_size = 40;		// NOT USE
		int max_level = 3;
		int k = 2;
		
		if(args.length >= 6){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			burn_factor = Integer.parseInt(args[3]);
			max_level = Integer.parseInt(args[4]);
			k = Integer.parseInt(args[5]);
		}
		
		System.out.println("dataname = " + dataname);
		System.out.println("burn_factor = " + burn_factor + " n_samples = " + n_samples);
		System.out.println("limit_size = " + limit_size);
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
//			String part_file = prefix + "_out/" + dataname +"_nmd_" + burn_factor + "_" + limit_size + "_" + max_level + "_" + k + ".part";
			String tree_file = prefix + "_sample/" + dataname +"_nmd_" + burn_factor + "_" + max_level + "_" + k + "_tree";
			
			NodeSetLouvainOpt root_set = NodeSetLouvainOpt.recursiveLouvain(G, burn_factor, limit_size, max_level, k, 1);	// 1: partitionLouvain
			
			// type 2 - call maximizeLouvain
//			String part_file = prefix + "_out/" + dataname +"_multioptlouvain_" + burn_factor + "_" + limit_size + "_" 
//					+ max_level + "_" + k + ".part";
//			NodeSetLouvainOpt root_set = NodeSetLouvainOpt.recursiveLouvain(G, burn_factor, limit_size, max_level, k, 2);	// 2: maximizeLouvain
			
			System.out.println("recursiveLouvain - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			NodeSetLouvainOpt.writeTree(root_set, tree_file + "." + i, G.E());
			System.out.println("writeTree - DONE");
			
			//
//			NodeSetLouvainOpt.printSetIds(root_set, G.E());
//			System.out.println("final modularity = " + root_set.modularityAll(G.E()));
//			
//			List<NodeSetLouvainOpt> best_cut = NodeSetLouvainOpt.bestCut(root_set, G.E());
//			System.out.println("best_cut.size = " + best_cut.size());
//			NodeSetLouvainOpt.writeBestCut(best_cut, part_file);
		}
		
		
		// TEST readTree() ok
//		NodeSetLouvainOpt root_set = NodeSetLouvainOpt.readTree("_sample/karate_nmd_20_3_2_tree.0");
//		System.out.println("readTree - DONE");
//		
//		List<NodeSetLouvainOpt> best_cut = NodeSetLouvainOpt.bestCutOffline(root_set, G.E());
	}

}
