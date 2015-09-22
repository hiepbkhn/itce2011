/*
 * Sep 18, 2015
 * 	- divisive approach using exponential mechanism with modularity Q
 * 	- use NodeSetMod.java
 */

package dp.combined;

import java.util.HashMap;
import java.util.Map;

import algs4.EdgeWeightedGraph;
import grph.Grph;
import grph.io.EdgeListReader;
import toools.io.file.RegularFile;

public class ModDivisiveDP {

	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("ModDivisiveDP");
		
		// load graph
//		String dataname = "karate";			// (34, 78)	
//		String dataname = "polbooks";		// (105, 441)		eps = 50, max_level = 3, final modularity = 0.43
											// recursiveLK		
//		String dataname = "polblogs";		// (1224,16715) 	eps = 50, max_level = 4, final modularity = 0.37
											// recursiveLK		
		String dataname = "as20graph";		// (6474,12572)		eps = 50, max_level = 4, final modularity = 0.23 (6s pc)
											// recursiveLK		
//		String dataname = "wiki-Vote";		// (7115,100762) 	
											// recursiveLK		
//		String dataname = "ca-HepPh";		// (12006,118489) 	eps = 50, max_level = 5, final modularity = 0.382 0.47 (24s pc) 
															
//		String dataname = "ca-AstroPh";		// (18771,198050) 	eps = 50, max_level = 4, final mod = 0.503 (compare mod/modSelf) 0.43 (compare mod) (23s pc) 
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 	eps = 50, max_level = 2, final mod = 0.523 (2h15); max_level = 1, mod=0.41 (compare mod) (97s pc)
																														//	max_level = 4, mod=0.43 (compare mod) (113s pc)
																														// max_level = 8 (not compare mod) (362s)
																//		 eps = 50, max_level = 6, final mod=0.33 (not compare mod) (627s pc)
																// 		eps = 50, max_level = 6, (400,100), final mod=0.319 (249s pc)
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624) eps = 100, max_level = 4, mod=0.45 (compare mod) (485s pc)
													//					 eps = 10, max_level = 4, mod=0.20 (compare mod) (385s pc)
													 
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 1;
		int burn_factor = 50;
		int limit_size = 40;		// at least 4*lower_size
		int lower_size = 10;		// at least 2
		int max_level = 6;
		double eps1 = 30.0;	// 1, 10, 50, 100 for polbooks: interesting prob values and final results
		double ratio = 1.0; // 1.26 = 2^(1/3)
		
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
		System.out.println("eps1 = " + eps1);
		System.out.println("ratio = " + ratio);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		String part_file = prefix + "_out/" + dataname +"_moddivdp_" + burn_factor + "_" + limit_size + "_" + lower_size + "_" 
						+ max_level + "_" + String.format("%.2f", ratio) + "_" + String.format("%.1f", eps1) + ".part";
		
		// TEST recursiveMod()
//		EdgeListReader reader = new EdgeListReader();
//		
//		Grph G;
//		RegularFile f = new RegularFile(filename);
//		
//		G = reader.readGraph(f);
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
//		
//		//
//		NodeSetMod R = new NodeSetMod(G);
//		System.out.println("mod = " + R.modularity(G.getNumberOfEdges()));
//		
//		// TEST recursiveMod()
//		for (int i = 0; i < n_samples; i++){
//			System.out.println("sample i = " + i);
//			
//			long start = System.currentTimeMillis();
//			NodeSetMod root_set = NodeSetMod.recursiveMod(G, eps1, burn_factor, limit_size, lower_size, max_level, ratio);	
//			System.out.println("recursiveMod - DONE, elapsed " + (System.currentTimeMillis() - start));
//			
//			
//			NodeSetMod.printSetIds(root_set, G.getNumberOfEdges());
//			System.out.println("final modularity = " + root_set.modularityAll(G.getNumberOfEdges()));
//			
//			NodeSetMod.writePart(root_set, part_file);
//			System.out.println("writePart - DONE");
//		}
		
		// TEST readPart()
		Map<Integer, Integer> part_init = new HashMap<Integer, Integer>();
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_1_2_6_10.0.part", part_init);	// before: 0.276, after: 0.299
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_1_2_6_20.0.part", part_init);	// before: 0.402, after: 0.421
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_1_2_6_30.0.part", part_init);	// before: 0.447, after: 0.477
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_1_2_6_40.0.part", part_init);	// before: 0.490, after: 0.507
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_1_2_8_30.0.part", part_init);	// before: 0.076, after: 0.463
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_1_2_8_40.0.part", part_init);	// before: 0.216, after: 0.475
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_40_10_6_1.26_20.0.part", part_init);	// before: 0.415, after: 0.452
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_40_10_6_2.00_20.0.part", part_init);	// before: 0.369, after: 0.457
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_100_40_10_6_1.00_20.0.part", part_init);	// before: 0.420, after: 0.428
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_moddivdp_20_40_10_6_1.26_50.0.part", part_init);	// before: 0.490, after: 0.520
		
		
//		int count = NodeSetMod.readPart("_out/com_amazon_ungraph_moddivdp_20_1_2_5_30.0.part", part_init);	// before: 0.1621 , after: 0.229
//		int count = NodeSetMod.readPart("_out/com_amazon_ungraph_moddivdp_20_1_2_8_30.0.part", part_init);	// before: 0.0055 , after: 0.0965
//		int count = NodeSetMod.readPart("_out/com_amazon_ungraph_moddivdp_20_1_2_6_50.0.part", part_init);	// before: 0.333 , after: 0.362
//		int count = NodeSetMod.readPart("_out/com_amazon_ungraph_moddivdp_20_40_10_6_1.00_50.0.part", part_init);	// before: 0.316 , after: 0.364
//		int count = NodeSetMod.readPart("_out/com_amazon_ungraph_moddivdp_50_40_10_6_1.00_30.0.part", part_init);	// before: 0.162 , after: 0.165
		
		// hrgdivgreedy
//		int count = NodeSetMod.readPart("_out/ca-AstroPh_hrgdivgreedy_20_50_10_7_30.0.part", part_init);	// before: 0.035 , after: 0.216
//		int count = NodeSetMod.readPart("_out/com_amazon_ungraph_hrgdivgreedy_20_100_20_7_30.0.part", part_init);	// before: 0.0 , after: 0.007
//		int count = NodeSetMod.readPart("_out/as20graph_hrgdivgreedy_20_100_20_4_1.0.part", part_init);	// before:  , after: 0.013
//		int count = NodeSetMod.readPart("_out/as20graph_hrgdivgreedy_20_100_20_4_10.0.part", part_init);	// before:  , after: 0.0
		
		// moddivopt
//		int count = NodeSetMod.readPart("_out/as20graph_moddivopt_20_40_10_6.part", part_init);	// before: 0.385  , after: 0.454
//		int count = NodeSetMod.readPart("_out/as20graph_moddivopt_50_40_10_6.part", part_init);	// before: 0.401  , after: 0.449
		
		// louvain
		int count = NodeSetMod.readPart("_out/as20graph.louvain", part_init);	// before:  0.623  , after: 0.623
		
		System.out.println("count = " + count);
		System.out.println("part_init.size = " + part_init.size());
		
		EdgeWeightedGraph graph = EdgeWeightedGraph.readEdgeList(filename);
		
		System.out.println("init modularity = " + LouvainDP.modularity(graph, part_init, count));
		
		EdgeWeightedGraph graph_new = LouvainDP.getGraphbyPartition(graph, part_init);	// not add noise yet !
		
		Louvain lv = new Louvain();
		Map<Integer, Integer> part = lv.best_partition(graph_new, null);

	}

}
