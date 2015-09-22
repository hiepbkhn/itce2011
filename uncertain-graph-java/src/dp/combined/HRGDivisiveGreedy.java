/*
 * Sep 17, 2015
 * 	- use HRG-Divisive (see dp.comm.CommunityFit) but at each node, we decide the further partition if doing so increases the modularity
 * 	- use NodeSetDivGreedy.java
 * Sep 22
 */

package dp.combined;

import grph.Grph;
import grph.io.EdgeListReader;
import toools.io.file.RegularFile;

public class HRGDivisiveGreedy {

	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("HRGDivisiveGreedy");
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)		
											// recursiveLK		
//		String dataname = "polblogs";		// (1224,16715) 	
											// recursiveLK		
//		String dataname = "as20graph";		// (6474,12572)		
											// recursiveLK		
//		String dataname = "wiki-Vote";		// (7115,100762) 	
											// recursiveLK		
//		String dataname = "ca-HepPh";		// (12006,118489) 	 
															
		String dataname = "ca-AstroPh";		// (18771,198050) 	eps=30, max_level=7; (50,10) (28s)
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624) 
		
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <burn_factor>
		String prefix = "";
		int n_samples = 1;
		int burn_factor = 20;
		int limit_size = 50;		// at least 4*lower_size
		int lower_size = 10;		// at least 2
		int max_level = 7;
		double eps1 = 30.0;
		
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
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		String part_file = prefix + "_out/" + dataname +"_hrgdivgreedy_" + burn_factor + "_" + limit_size + "_" + lower_size + "_" 
				+ max_level + "_" + String.format("%.1f", eps1) + ".part";
		
		//
		EdgeListReader reader = new EdgeListReader();
		
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
		//
		NodeSetDivGreedy R = new NodeSetDivGreedy(G);
		System.out.println("logLK = " + R.logLK());
		
		// TEST recursiveLK()
		for (int i = 0; i < n_samples; i++){
			System.out.println("sample i = " + i);
			
			long start = System.currentTimeMillis();
			NodeSetDivGreedy root_set = NodeSetDivGreedy.recursiveLK(G, eps1, burn_factor, limit_size, lower_size, max_level);	
			System.out.println("recursiveLK - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			NodeSetDivGreedy.printSetIds(root_set);
			
			NodeSetDivGreedy.writePart(root_set, part_file);
			System.out.println("writePart - DONE");
			
		}

	}

}
