/*
 * Nov 16, 2015
 * 	-  
 */

package dp.mcmc;

import algs4.EdgeIntGraph;
import dp.mcmc.Dendrogram;
import grph.Grph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import toools.io.file.RegularFile;

public class SampleGenerator {

	
	//////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {
		
		// load graph
//		String dataname = "example";		// (13, 20)
//		String dataname = "karate";			// (34, 78)
		String dataname = "polbooks";		// (105, 441)
//		String dataname = "polblogs";		// (1224,16715) 	
//		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762) 	
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		// WCC
//		String dataname = "polblogs-wcc";			// (1222,16714) 	
//		String dataname = "wiki-Vote-wcc";			// (7066,100736) 	
//		String dataname = "ca-HepPh-wcc";			// (11204,117619) 
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 	
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)		
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)				
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 
		
		// COMMAND-LINE
		String prefix = "";
//		String tree_file = "as20graph_dendro_20_6474_1000_2.0_tree";
		String tree_file = "polbooks_dendro_20_105_1000_1.2_tree";
	    int n_samples = 1;
		double eps = 2.0;		
		
		if(args.length >= 5){
			prefix = args[0];
			dataname = args[1];
			tree_file = args[2];
			n_samples = Integer.parseInt(args[3]);
			eps = Double.parseDouble(args[4]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("tree_file = " + tree_file);
		System.out.println("eps = " + eps);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		String sample_file = prefix + "_sample/" + tree_file + "_sample_" + String.format("%.1f", eps);
	    System.out.println("sample_file = " + sample_file);
	    
	    //
	    long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
	    
	    //
	    for (int i = 0; i < n_samples; i++){
	    	System.out.println("sample i = " + i);
	    	
	    	//
	    	start = System.currentTimeMillis();
	    	Dendrogram T = new Dendrogram();
	    	T.readInternalNodes(G, prefix + "_out/" + tree_file + "." + i);
	    	System.out.println("readInternalNodes - DONE, elapsed " + (System.currentTimeMillis() - start));
	    	
	    	//
	    	start = System.currentTimeMillis();
	    	T.addLaplaceNoise(eps);
	    	System.out.println("addLaplaceNoise - DONE, elapsed " + (System.currentTimeMillis() - start));
	    	
	    	// 
	    	start = System.currentTimeMillis();
	    	EdgeIntGraph aG = T.generateSanitizedSample(G.V(), true);	// true: noisy_nEdge
	    	EdgeIntGraph.writeGraph(aG, sample_file + "." + i);
			System.out.println("generateSanitizedSample - DONE, elapsed " + (System.currentTimeMillis() - start));
		}

	}

}
