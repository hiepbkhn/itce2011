/*
 * Sep 18, 2015
 * 	- divisive approach using exponential mechanism with modularity Q
 * 	- use NodeSetModOpt.java
 */

package dp.combined;

import algs4.EdgeWeightedGraph;
import grph.Grph;
import grph.io.EdgeListReader;
import toools.io.file.RegularFile;

public class ModDivisiveOpt {

	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("ModDivisiveOpt");
		
		// load graph
//		String dataname = "karate";			// (34, 78)	
//		String dataname = "polbooks";		// (105, 441)		
											// 		
//		String dataname = "polblogs";		// (1224,16715) 	
											// 		
		String dataname = "as20graph";		// (6474,12572)		burn = 20, max_level = 8, (40,10) final mod = 0.317, bestCut=0.451 (6s)
											// 					burn = 50, max_level = 8, (40,10) final mod = 0.332, bestCut=0.433 (12s)
											//					burn = 20, max_level = 8, (40) final mod = 0.475, bestCut=0.522 (0.5s)	recursiveLouvain
//		String dataname = "wiki-Vote";		// (7115,100762) 	
											// 		
//		String dataname = "ca-HepPh";		// (12006,118489) 	 
															
//		String dataname = "ca-AstroPh";		// (18771,198050) 	burn = 30, max_level = 8, (40,10) final mod = 0.412, bestCut=0.498 (95s)
											//					burn = 20, max_level = 8, (40) final mod = 0.522, bestCut=0.528 (10s) recursiveLouvain
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 	burn = 1, max_level = 8, (100,20) final mod = 0.112, bestCut=0.256 ()
													//					burn = 5, max_level = 8, (100,20) final mod = 0.399, bestCut=0.524 ()
													//					burn = 20, max_level = 8, (100,20) final mod = 0.584, bestCut=0.674 (359s)
													//					burn = 50, max_level = 8, (100,20) final mod = 0.541, bestCut=0.624 (834s)
													//					burn = 20, max_level = 8, (100) final mod = 0.687, bestCut=0.693 (100s) recursiveLouvain
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624)burn = 10, max_level = 8, (100,20) final mod = 0.356, bestCut=0.494 (840s)
													// 					burn = 20, max_level = 8, (100,20) final mod = 0.357, bestCut=0.538 (1500s)
													//					burn = 30, max_level = 8, (100,20) final mod = 0.360, bestCut=0.538 (2261s)
													//					burn = 20, max_level = 8, (100) final mod = 0.544, bestCut=0.566 (308s) recursiveLouvain
													// 					burn = 30, max_level = 10, (50) final mod = 0.535, bestCut=0.566 (1000s) recursiveLouvain
													 
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 1;
		int burn_factor = 20;
		int limit_size = 40;		// at least 4*lower_size
		int lower_size = 10;		// at least 2
		int max_level = 8;
		
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
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		
		
		// TEST recursiveMod()
		/*
		EdgeListReader reader = new EdgeListReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		//
		NodeSetModOpt R = new NodeSetModOpt(G);
		System.out.println("mod = " + R.modularity(G.getNumberOfEdges()));
		*/
		
		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList(filename);
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		NodeSetModOpt2 R = new NodeSetModOpt2(G);
		System.out.println("mod = " + R.modularity(G.E()));
		
		// TEST recursiveMod(), recursiveLouvain()
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
			
			long start = System.currentTimeMillis();
//			String part_file = prefix + "_out/" + dataname +"_moddivopt_" + burn_factor + "_" + limit_size + "_" + lower_size + "_" 
//					+ max_level + ".part";
//			NodeSetModOpt2 root_set = NodeSetModOpt2.recursiveMod(G, burn_factor, limit_size, lower_size, max_level);
			
			String part_file = prefix + "_out/" + dataname +"_moddivoptlouvain_" + burn_factor + "_" + limit_size + "_" 
					+ max_level + ".part";
			NodeSetModOpt2 root_set = NodeSetModOpt2.recursiveLouvain(G, burn_factor, limit_size, max_level);
			System.out.println("recursiveMod - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			
//			NodeSetModOpt2.printSetIds(root_set, G.getNumberOfEdges());
			System.out.println("final modularity = " + root_set.modularityAll(G.E()));
			
			NodeSetModOpt2.writePart(root_set, part_file);
			System.out.println("writePart - DONE");
			
			NodeSetModOpt2.bestCut(root_set, G.E());
		}
		
	}

}
