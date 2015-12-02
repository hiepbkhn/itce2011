package dp;

import algs4.EdgeIntGraph;
import toools.io.file.RegularFile;
import grph.io.EdgeListWriter;
import dp.mcmc.Dendrogram;

public class SampleGenerator {

	//// e.g. dendro_file = ..\_out\ca-AstroPh_louvain_dendro_50
	//		  sample_file = ..\_sample\ca-AstroPh_louvain_dendro_50_sample
	public static void generateSampleFromDendrogram(EdgeIntGraph G, String dendro_file, String sample_file, int n_samples) throws Exception{
		
		int n_nodes = G.V();
		
		for (int i = 0; i < n_samples; i++){
			Dendrogram T = new Dendrogram();
			T.readInternalNodes(G, dendro_file + "." + i);
			
			//
			long start = System.currentTimeMillis();
			EdgeIntGraph aG = T.generateSanitizedSample(n_nodes, false);	// false: use nEdge
			
			//
			EdgeIntGraph.writeGraph(aG, sample_file + "." + i);
	    	
	    	System.out.println("generateSanitizedSample and writeGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		}
	}
	
	////e.g. dendro_file = ..\_out\ca-AstroPh_louvain_dendro_50
	//		  sample_file = ..\_sample\ca-AstroPh_louvain_dendro_50_sample
	public static void generateSampleFromDendrogramWithLaplace(EdgeIntGraph G, String dendro_file, String sample_file, int n_samples, double eps_Laplace) throws Exception{
		
		int n_nodes = G.V();
		
		for (int i = 0; i < n_samples; i++){
			Dendrogram T = new Dendrogram();
			T.readInternalNodes(G, dendro_file + "." + i);
			
			T.addLaplaceNoise(eps_Laplace);
			
			//
			long start = System.currentTimeMillis();
			EdgeIntGraph aG = T.generateSanitizedSample(n_nodes, true);		// true: use noisy_nEdge !
			
			//
			
			EdgeIntGraph.writeGraph(aG, sample_file + "." + i);
	    	
	    	System.out.println("generateSanitizedSample and writeGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		}
	}
	
	//// 
	public static void main(String[] args) throws Exception{
		// load graph
//		String dataname = "polbooks";			// (105, 441)		
//		String dataname = "polblogs";			// (1224,16715) 	
//		String dataname = "as20graph";			// (6474,12572)		
//		String dataname = "wiki-Vote";			// (7115,100762) 	
//		String dataname = "ca-HepPh";			// (12006,118489) 	
		String dataname = "ca-AstroPh";			// (18771,198050) 	
		
		// COMMAND-LINE
		String prefix = "";
	    int n_samples = 10;
	    String dendro_name = "";
	    double eps_Laplace = 1.0; 
	    
	    if(args.length >= 4){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			dendro_name = args[3];
	    }
	    if(args.length >= 5)
	    	eps_Laplace = Double.parseDouble(args[4]);
		
		String filename = prefix + "_data/" + dataname + ".grph";
		String dendro_file = prefix + "_out/" + dendro_name;
		String sample_file = prefix + "_sample/" + dendro_name + "_sample";
		if(args.length >= 5)
			sample_file = prefix + "_sample/" + dendro_name + "_" + String.format("%.1f", eps_Laplace) + "_sample";
				
		System.out.println("dataname = " + dataname);
		System.out.println("dendro_name = " + dendro_name);
		System.out.println("filename = " + filename);
		System.out.println("dendro_file = " + dendro_file);
		System.out.println("sample_file = " + sample_file);
		if(args.length >= 5)
			System.out.println("eps_Laplace = " + eps_Laplace);
		
		//
	    long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		// TEST generateSampleFromDendrogram()
		if(args.length == 4)
			generateSampleFromDendrogram(G, dendro_file, sample_file, n_samples);
		else if(args.length >= 5)
			generateSampleFromDendrogramWithLaplace(G, dendro_file, sample_file, n_samples, eps_Laplace);

	}

}
