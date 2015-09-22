/*
 * Sep 18, 2015
 * 	- divisive approach using exponential mechanism with modularity Q
 * 	- use NodeSetModOpt.java
 */

package dp.combined;

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
		String part_file = prefix + "_out/" + dataname +"_moddivopt_" + burn_factor + "_" + limit_size + "_" + lower_size + "_" 
						+ max_level + ".part";
		
		// TEST recursiveMod()
		EdgeListReader reader = new EdgeListReader();
		
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		//
		NodeSetModOpt R = new NodeSetModOpt(G);
		System.out.println("mod = " + R.modularity(G.getNumberOfEdges()));
		
		// TEST recursiveMod()
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
			
			long start = System.currentTimeMillis();
			NodeSetModOpt root_set = NodeSetModOpt.recursiveMod(G, burn_factor, limit_size, lower_size, max_level);	
			System.out.println("recursiveMod - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			
			NodeSetModOpt.printSetIds(root_set, G.getNumberOfEdges());
			System.out.println("final modularity = " + root_set.modularityAll(G.getNumberOfEdges()));
			
			NodeSetModOpt.writePart(root_set, part_file);
			System.out.println("writePart - DONE");
		}
		
	}

}
