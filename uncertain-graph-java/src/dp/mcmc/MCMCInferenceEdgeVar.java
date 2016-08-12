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

import algs4.EdgeIntGraph;
import toools.io.file.RegularFile;
import toools.set.IntSet;
import grph.Grph;
import grph.VertexPair;
import grph.algo.AdjacencyMatrix;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import grph.io.GrphTextReader;



//////////////////////////////////
public class MCMCInferenceEdgeVar {

	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("MCMCInferenceEdgeVar");
		
		// TOY GRAPH
//		Grph G = new InMemoryGrph();
//		G.addNVertices(7);
//		for (int v = 0; v < 6; v++)
//			G.addSimpleEdge(v, v+1, false);
		
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)
//		String dataname = "polblogs";		// (1224,16715) 
		String dataname = "as20graph";		// (6474,12572)		build_dendrogram 0.16s, 75k fitting (16s), 750k(234s)
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		
		// COMMAND-LINE
		String prefix = "";
	    int n_samples = 50;
		int sample_freq = 1000;
		int burn_factor = 1000;
		double eps1 = 1.0;
		double eps2 = 1.0;
		
		if(args.length >= 7){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			sample_freq = Integer.parseInt(args[3]);
			burn_factor = Integer.parseInt(args[4]);
			eps1 = Double.parseDouble(args[5]);
			eps2 = Double.parseDouble(args[6]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("eps1 = " + eps1 + " eps2 = " + eps2 + " burn_factor = " + burn_factor + " sample_freq = " + sample_freq);
		//
//		String filename = "_data/" + dataname + ".grph";	// GrphTextReader		
		String filename = prefix + "_data/" + dataname + ".gr";
		String node_file = prefix + "_out/" + dataname + "_ev_dendro" + "_" + n_samples + "_" + sample_freq + "_" + burn_factor;			
		String out_file = prefix + "_sample/" + dataname + "_ev_mcmc_10_10";    		// 10 (eps1=1.0), 10 (eps2=1.0)
		
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
////		AdjacencyMatrix A = G.getAdjacencyMatrix();
		
	    //
	    long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());
			
//		AdjacencyMatrix A = G.getAdjacencyMatrix();
		
		//
		DendrogramEdgeVar T = new DendrogramEdgeVar();
		T.initByGraph(G);
		
//		T.initByInternalNodes(G, new Int4[]{new Int4(-5,-2,1,2), new Int4(-6,-3,3,4), new Int4(-7,-3,5,6), 
//				new Int4(-2,-1,0,-5), new Int4(-3,-1,-6,-7), new Int4(-1,Node.ROOT_NODE,-2,-3)});
		
//		System.out.println("inOrderPrint");
//		Dendrogram.inOrderPrint(T.root_node, true, true);
		
		System.out.println("logLK = " + T.logLK());
		System.out.println("degDiffL1 = " + T.degDiffL1());
		System.out.println("edgeVar = " + T.edgeVar());
		
//		List<Integer> u_list = Dendrogram.findChildren(T.root_node);
//		System.out.println(u_list.size());
//		for (int id:u_list)
//			System.out.print(id + " ");
		
		// TEST config_2(), config_3()
//		Node r_node = T.node_dict.get(-2);  // node_dict[-2], [-5]
//		    
////	    Node p_node = T.config_2(G, r_node);
////	    System.out.println("config_2: DONE");
//	    r_node = T.config_3(G, r_node);
//	    System.out.println("config_3: DONE");
//
//	    System.out.println("logLK = " + T.logLK());
//	    
//	    System.out.println("--root_node.id =" + T.root_node.id);
//	    Dendrogram.inOrderPrint(T.root_node, true, true);
		
		// TEST 
	    start = System.currentTimeMillis();
	    List<DendrogramEdgeVar> list_T = DendrogramEdgeVar.dendrogramFitting(T, G, eps1, burn_factor*G.V(), n_samples, sample_freq);      
	    System.out.println("dendrogram_fitting - DONE, elapsed " + (System.currentTimeMillis() - start));
	    
//	    //check T
////	    int nNode = Dendrogram.inOrderPrint(T.root_node, false, false);		// 12947 = 6474*2-1 OK
////	    System.out.println("#nodes in T : " + nNode);
	    
	    for (DendrogramEdgeVar T2 : list_T)
	        System.out.println("logLK = " + T2.logLK() + " degDiffL1 = " + T2.degDiffL1() + " edgeVar = " + T2.edgeVar());
	    
	    
	    // TEST writeInternalNodes(), readInternalNodes()
	    DendrogramEdgeVar.writeInternalEdgeVar(list_T, node_file);
	    System.out.println("writeInternalNodes - DONE");
		
//	    ArrayList<DendrogramEdgeVar> list_T2 = new ArrayList<DendrogramEdgeVar>();
//	    for (int i = 0; i < n_samples; i++)
//	    	list_T2.add(new DendrogramEdgeVar());
//	    
//	    DendrogramEdgeVar.readInternalEdgeVar(G, list_T2, node_file, n_samples);
//	    for (DendrogramEdgeVar T2 : list_T2)
//	    	System.out.println("logLK = " + T2.logLK());
//	    System.out.println("readInternalNodes - DONE");
	    
//	    // TEST addLaplaceNoise()
//	    start = System.currentTimeMillis();
//	    for (Dendrogram T2 : list_T)
//	    	T2.addLaplaceNoise(eps2);
//	    System.out.println("addLaplaceNoise - DONE, elapsed " + (System.currentTimeMillis() - start));
//	    
//	    // TEST generateSanitizedSample()
//	    start = System.currentTimeMillis();
//	    int count = 0;
//	    for (Dendrogram T2 : list_T){
//	    	Grph aG = T2.generateSanitizedSample(G.getNumberOfVertices(), true);	// list_T not list_T2 (T2 not contains .noisy_value)
//	    	f = new RegularFile(out_file + "." + count);
////	    	f.setContent(aG.toGrphText().getBytes());
//	    	writer.writeGraph(aG, f);
//	    	count ++;
//	    }
//	    System.out.println("generateSanitizedSample - DONE, elapsed " + (System.currentTimeMillis() - start));
	    
	}

}
