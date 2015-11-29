/*
 * Nov 6, 2015
 * 	- use DendrogramFixed
 */
package dp.mcmc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import algs4.UnweightedGraph;
import toools.io.file.RegularFile;
import toools.set.IntSet;
import grph.Grph;
import grph.VertexPair;
import grph.algo.AdjacencyMatrix;
import grph.edit.GrphEditor;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import grph.io.GrphTextReader;


//////////////////////////////////
public class MCMCInferenceFixedNonPriv {

	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("MCMCInferenceFixedNonPriv");
		
		// TOY GRAPH
//		Grph G = new InMemoryGrph();
//		G.addNVertices(7);
//		for (int v = 0; v < 6; v++)
//			G.addSimpleEdge(v, v+1, false);
		
		
		// load graph
//		String dataname = "example";		// (13, 20)
//		String dataname = "karate";			// (34, 78)
		String dataname = "polbooks";		// (105, 441)
//		String dataname = "polblogs";		// (1224,16715) 	1324k fitting (1069s)
//		String dataname = "as20graph";		// (6474,12572)		build_dendrogram 0.16s, 85k fitting (0.6s)
//		String dataname = "wiki-Vote";		// (7115,100762) 	
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		// WCC
//		String dataname = "polblogs-wcc";			// (1222,16714) 	
//		String dataname = "wiki-Vote-wcc";			// (7066,100736) 	
//		String dataname = "ca-HepPh-wcc";			// (11204,117619) 
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 	199k fitting (3s)
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)	355k (4s)			
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)				
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 11.4M fitting (160s)
		
		// COMMAND-LINE
		String prefix = "";
	    int n_samples = 20;
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
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		String node_file = prefix + "_out/" + dataname + "_fixed_np_" + n_samples + "_" + sample_freq + "_" + burn_factor + "_tree";
	    System.out.println("node_file = " + node_file);
		
		//
//		EdgeListReader reader = new EdgeListReader();
//		EdgeListWriter writer = new EdgeListWriter();
//		Grph G;
//		RegularFile f = new RegularFile(filename);
//		
//		G = reader.readGraph(f);
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
		
		//
		UnweightedGraph G = UnweightedGraph.readEdgeList(filename, "\t");
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
		
		//
		DendrogramFixedNonPriv T = new DendrogramFixedNonPriv();
		T.initByGraph(G);
		
		
		System.out.println("logLK = " + T.logLK + " logLK(2) = " + T.logLK());
		
		// TEST dendrogramFitting()
		long start = System.currentTimeMillis();
	    List<DendrogramFixedNonPriv> list_T = DendrogramFixedNonPriv.dendrogramFitting(T, G, burn_factor*G.V(), n_samples, sample_freq, node_file);    
	    System.out.println("logLK = " + T.logLK + " logLK(2) = " + T.logLK());
	    System.out.println("dendrogramFitting - DONE, elapsed " + (System.currentTimeMillis() - start));

	    
		
		// max toplevel
//		int max_toplevel = 0;
//		for (Node u : T.node_list)
//			if (max_toplevel < u.toplevel)
//				max_toplevel = u.toplevel;
//		System.out.println(max_toplevel);
//		
//		// TEST printDendrogram()
//		Dendrogram.printDendrogram(T.root_node);
//		
//		// TEST swap(), fastSwap()
//		Node u = T.node_list[4];
//		Node v = T.node_list[12];
//		System.out.println("fastSwap");
////		T.swap(G, u, v);
//		T.fastSwap(G, u, v);
//		System.out.println("AFTER SWAP");
//		Dendrogram.printDendrogram(T.root_node);
//		System.out.println("logLK = " + T.logLK + " logLK(2) = " + T.logLK());
//		
//		
//		// TEST Overflow in computation of Node.value
////		int ne = 12345;
////		double val = (double)ne / (500000*500000);	// overflow !
////		System.out.println(val);
////		val = (double)ne / 500000/500000;
////		System.out.println(val);
	}

}
