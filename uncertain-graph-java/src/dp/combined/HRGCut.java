/*
 * Sep 17, 2015
 * 	- compute modularity of HRG models by top-down/bottom-up cuts (see MCMCFit.java)
 */

package dp.combined;

import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import hrg.HRG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dp.mcmc.Dendrogram;
import toools.io.file.RegularFile;

public class HRGCut {
	
	//// copied from TmFPart.filterByNodesets()
	public static double modularity(Grph G, Map<Integer, Integer> part){
		int n = G.getNumberOfVertices();
		int m = G.getNumberOfEdges();
		
		Grph aG = new InMemoryGrph();
		aG.addNVertices((int)n);
		
		int[] nodeToSet = new int[n];	// point to containing nodelist
		int c = 0;	// number of clusters
		for (int u = 0; u < n; u++)
			if (c < part.get(u))
				c = part.get(u);
		c = c + 1;
		
		// debug
		System.out.println("#clusters = " + c);
		List<List<Integer>> children_list = new ArrayList<List<Integer>>();
		for (int i = 0; i < c; i++)
			children_list.add(new ArrayList<Integer>());
		for (int u = 0; u < n; u++)
			children_list.get(part.get(u)).add(u);
		
//		for (List<Integer> children : children_list){
//			for (int u : children)
//				System.out.print(u + ",");
//			System.out.println();
//		}
		
		// compute lc, dc
		int[] lc = new int[c];
		int[] dc = new int[c];
		
		for (VertexPair p : G.getEdgePairs()){
			int u = p.first;
			int v = p.second;
			int u_com = part.get(u);
			int v_com = part.get(v);
			if (u_com == v_com)
				lc[u_com] += 1;
			dc[u_com] += 1;
			dc[v_com] += 1;
		}
		// check
//		int sum_lc = 0;
//		int sum_dc = 0;
//		for (int i = 0; i < c; i++){
//			sum_lc += lc[i];
//			sum_dc += dc[i];
//		}
//		System.out.println("sum_lc = " + sum_lc);
//		System.out.println("sum_dc = " + sum_dc);
		
		// compute modularity
		double mod = 0.0;
		for (int i = 0; i < c; i++)
			mod += (double)lc[i]/m - ((double)dc[i]*dc[i]/(4*m*m));
		
		//
		return mod;
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("HRGCut");
		
		// load graph
//		String dataname = "karate";			// (34, 78)		
//		String dataname = "polbooks";		// (105, 441)
//		String dataname = "polblogs";		// (1224,16715) 	1548k fitting (2093s)
		String dataname = "as20graph";		// (6474,12572)		build_dendrogram 0.16s, 75k fitting (16s), 750k(234s), 10 samples (68s)
//		String dataname = "wiki-Vote";		// (7115,100762) 	81k fitting (37s)
//		String dataname = "ca-HepPh";		// (12006,118489) 	22k fitting (8.8s)
//		String dataname = "ca-AstroPh";		// (18771,198050) 	28.7k fitting (19.6s)
		
		// COMMAND-LINE
		String prefix = "";
	    int n_samples = 1;
		int sample_freq = 1000;
		int burn_factor = 1000;
		
		if(args.length >= 5){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			sample_freq = Integer.parseInt(args[3]);
			burn_factor = Integer.parseInt(args[4]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("burn_factor = " + burn_factor + " sample_freq = " + sample_freq + " n_samples = " + n_samples);
		
		String filename = prefix + "_data/" + dataname + ".gr";	// EdgeListReader
		String node_file = prefix + "_out/" + dataname + "_hrg" + "_" + n_samples + "_" + sample_freq + "_" + burn_factor;
	    String out_file = prefix + "_sample/" + dataname + "_fit";    		

	    //
		EdgeListReader reader = new EdgeListReader();
		EdgeListWriter writer = new EdgeListWriter();
		
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
			
		//
		HRG T = new HRG();
		T.initByGraph(G);
		
		System.out.println("logLK = " + T.logLK());
		
		// TEST partitionTopDown()
//	    long start = System.currentTimeMillis();
//	    List<HRG> list_T = HRG.dendrogramFitting(T, G, burn_factor*G.getNumberOfVertices(), n_samples, sample_freq);      
//	    System.out.println("dendrogramFitting - DONE, elapsed " + (System.currentTimeMillis() - start));

		
		List<HRG> list_T = new ArrayList<HRG>();
		for (int i = 0; i < n_samples; i++)
			list_T.add(new HRG());
//		HRG.readInternalNodes(G, list_T, "_out/as20graph_hrg", n_samples);					// bad
//		HRG.readInternalNodes(G, list_T, "_out/as20graph_hrg_50_1000_6474", n_samples);		// bad
		HRG.readInternalNodes(G, list_T, "_out/as20graph_louvain_dendro_50", n_samples);	// good
		
	    for (HRG T2 : list_T){
	        System.out.println("logLK = " + T2.logLK());
	        
	        List<Map<Integer, Integer>> part_list = Dendrogram.partitionTopDown(T2);
	        System.out.println("partitionTopDown - DONE");
	        
	        int count = 0;
	        for (Map<Integer, Integer> part : part_list){
	        	System.out.println("partition " + (count++));
	        	System.out.println("modularity = " + modularity(G, part));
	        }
	    }
	    
		
	}

}
