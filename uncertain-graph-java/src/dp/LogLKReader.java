/*
 * Nov 29, 2015
 * 	- 
 * 
 */

package dp;

import grph.Grph;
import grph.io.EdgeListReader;
import grph.io.GrphTextReader;
import toools.io.file.RegularFile;
import dp.mcmc.Dendrogram;

public class LogLKReader {

	////////////////////////////////////////////////	
	public static void main(String[] args) throws Exception{
		
		// load graph
//		String dataname = "polbooks";			// (105, 441)		
//		String dataname = "polblogs";			// (1224,16715) 	
//		String dataname = "as20graph";			// (6474,12572)		
//		String dataname = "wiki-Vote";			// (7115,100762) 	
//		String dataname = "ca-HepPh";			// (12006,118489) 	
//		String dataname = "ca-AstroPh";			// (18771,198050) 	
//		String dataname = "sm_50000_005_11";	// (50000,250000) 	
//		String dataname = "sm_100000_005_11";	// (100000,500000) 	
		// WCC
//		String dataname = "polblogs-wcc";			// (1222,16714) 	
//		String dataname = "wiki-Vote-wcc";			// (7066,100736) 	
//		String dataname = "ca-HepPh-wcc";			// (11204,117619) 
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 
		// LARGE
//		String dataname = "com_amazon_ungraph"; 	// (334863,925872) 
		String dataname = "com_dblp_ungraph";  		// (317080,1049866) 
//		String dataname = "com_youtube_ungraph"; 	// (1134890,2987624)
		
		
		int max_size = 50;
		int	n_samples = 1;
		
		//
		String filename = "_data/" + dataname + ".gr";
		String dendro_file = "../uncertain-graph/_out/" + dataname + "_louvain_dendro_" + max_size;
		
		//
		EdgeListReader reader = new EdgeListReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		
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
