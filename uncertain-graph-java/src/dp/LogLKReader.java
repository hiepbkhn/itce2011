/*
 * Nov 29, 2015
 * 	- 
 * 
 */

package dp;

import algs4.EdgeIntGraph;
import grph.Grph;
import grph.io.EdgeListReader;
import grph.io.GrphTextReader;
import toools.io.file.RegularFile;
import dp.mcmc.Dendrogram;

public class LogLKReader {

	////////////////////////////////////////////////	
	public static void main(String[] args) throws Exception{
		
		// load graph
//		String dataname = "polbooks";			// (105, 441)				max_size = 10
//		String dataname = "polblogs";			// (1224,16715) 		
//		String dataname = "as20graph";			// (6474,12572)				max_size = 50
//		String dataname = "wiki-Vote";			// (7115,100762) 	
//		String dataname = "ca-HepPh";			// (12006,118489) 	
//		String dataname = "ca-AstroPh";			// (18771,198050) 	
//		String dataname = "sm_50000_005_11";	// (50000,250000) 	
//		String dataname = "sm_100000_005_11";	// (100000,500000) 	
		// WCC
//		String dataname = "polblogs-wcc";			// (1222,16714) 		max_size = 20
//		String dataname = "wiki-Vote-wcc";			// (7066,100736) 		max_size = 50
//		String dataname = "ca-HepPh-wcc";			// (11204,117619) 		max_size = 50
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 		max_size = 50
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 		max_size = 50
//		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 	max_size = 50
		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624)	max_size = 100
		
		
		int max_size = 100;
		int	n_samples = 1;
		
		//
		String filename = "_data/" + dataname + ".gr";
//		String dendro_file = "../uncertain-graph/_out/" + dataname + "_louvain_dendro_" + max_size;
		String dendro_file = "_out/" + "com_youtube_ungraph_fixed_np_20_1134890_1000_tree";
		
		//
	    long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		double sumLK = 0.0;
		for (int i = 0; i < n_samples; i++){
			Dendrogram T = new Dendrogram();
			T.readInternalNodes(G, dendro_file + "." + i);
			
			double logLK = T.logLK();
			sumLK += logLK;
			System.out.println("logLK = " + logLK);
		}
		
		System.out.println("average logLK = " + sumLK/n_samples);

	}

}
