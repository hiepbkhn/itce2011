/**
 * 20 Mar, 2015
 * conversion from Python, run 30 times faster !
 * 
 */
package hrg;

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

import dp.mcmc.DendrogramEdgeVar;
import toools.io.file.RegularFile;
import toools.set.IntSet;
import grph.Grph;
import grph.VertexPair;
import grph.algo.AdjacencyMatrix;
import grph.in_memory.InMemoryGrph;
import grph.io.GrphTextReader;


//////////////////////////////////
public class Compare {

	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("Compare");
		
		// TOY GRAPH
//		Grph G = new InMemoryGrph();
//		G.addNVertices(7);
//		for (int v = 0; v < 6; v++)
//			G.addSimpleEdge(v, v+1, false);
		
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)
//		String dataname = "polblogs";		// (1224,16715) 	1324k fitting (1069s)
//		String dataname = "as20graph";		// (6474,12572)		build_dendrogram 0.16s, 75k fitting (16s), 750k(234s), 10 samples (68s)
//		String dataname = "wiki-Vote";		// (7115,100762) 	81k fitting (37s)
		String dataname = "ca-HepPh";		// (12006,118489) 	22k fitting (8.8s)
//		String dataname = "ca-AstroPh";		// (18771,198050) 	28.7k fitting (19.6s)
		
		
		String filename = "_data/" + dataname + ".grph";
		String node_file = "_out/" + dataname + "_ev_hrg";
	    String out_file = "_sample/" + dataname + "_ev_fit";  
	    String hrg_file = "_out/" + dataname + "_louvain_dendro";

	    //
		GrphTextReader reader = new GrphTextReader();
		Grph G;
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
			
//		AdjacencyMatrix A = G.getAdjacencyMatrix();
		
		//
		HRGEdgeVar T = new HRGEdgeVar();
		T.initByGraph(G);
		
//		T.initByInternalNodes(G, new Int4[]{new Int4(-5,-2,1,2), new Int4(-6,-3,3,4), new Int4(-7,-3,5,6), 
//				new Int4(-2,-1,0,-5), new Int4(-3,-1,-6,-7), new Int4(-1,Node.ROOT_NODE,-2,-3)});
		
//		System.out.println("TEST lowestCommonAncestor");
//		System.out.println("lowestCommonAncestor = " + HRGEdgeVar.lowestCommonAncestor(T.node_dict.get(0), T.node_dict.get(2)));
		
//		System.out.println("inOrderPrint");
//		HRGEdgeVar.inOrderPrint(T.root_node, true, true);
		
		System.out.println("logLK = " + T.logLK());
		System.out.println("degDiffL1 = " + T.degDiffL1());
		System.out.println("edgeVar = " + T.edgeVar());
		
	    
	    // read 
		DendrogramEdgeVar T2 = new DendrogramEdgeVar();
		T2.readInternalNodes(G, hrg_file + ".0");
	    System.out.println("logLK = " + T2.logLK() + " degDiffL1 = " + T2.degDiffL1() + " edgeVar = " + T2.edgeVar());
	}

}
