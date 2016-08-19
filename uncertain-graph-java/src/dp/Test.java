package dp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import algs4.EdgeInt;
import algs4.EdgeIntGraph;
import algs4.EdgeWeightedGraph;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;

import dp.combined.CommunityMeasure;
import dp.combined.Const;
import dp.combined.Louvain;
import dp.combined.NodeSetLouvain;
import dp.mcmc.Dendrogram;
import dp.mcmc.Node;
import toools.io.file.RegularFile;
import toools.set.IntHashSet;
import toools.set.IntSet;
import grph.*;
import grph.algo.AdjacencyMatrix;
import grph.in_memory.*;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import grph.io.GraphBuildException;
import grph.io.GrphTextReader;
import grph.io.ParseException;

public class Test {
	////
	public static void testGrph(){
//		Grph g = new InMemoryGrph(); 
//		g.grid(4, 4);
//		RegularFile f = new RegularFile("_data/myfile.txt"); 
//		try {
//			f.setContent(g.toGrphText().getBytes());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// read text file
		GrphTextReader reader = new GrphTextReader();
		Grph g;
		try {
			RegularFile f = new RegularFile("_data/polbooks.grph");
//			RegularFile f = new RegularFile("_data/as20graph.grph");
			
			g = reader.readGraph(f);
			
			System.out.println("#nodes = " + g.getNumberOfVertices());
			System.out.println("#edges = " + g.getNumberOfEdges());
			
//			for (int v : g.getVertices().toIntArray()){
//				System.out.println(v + ":" + g.getOutVertexDegree(v));
//			}
//			
//			for (VertexPair p : g.getEdgePairs()){
//				System.out.println(p.first + " " + p.second);
//			}
//			System.out.println("DONE");
//			
//			IntSet s = g.getEdgesIncidentTo(1);		// list of edge ids, NOT neighbor vertices !
//			System.out.println(s.contains(4));
//			System.out.println(s.contains(6));
//			System.out.println(s.contains(3));
//			for (IntCursor v:s)
//				System.out.print(v.value + " ");
//			System.out.println();
//			// adjacency matrix
//			AdjacencyMatrix A = g.getAdjacencyMatrix();
//			System.out.println(A.get(6, 4));
			
			
//			g.removeEdge(0);		// remove by edge id: OK
//			g.removeEdge(0, 1);		// Exception !
			g.removeEdge(g.getEdgesConnecting(0, 1).toIntArray()[0]);	// OK but verbose !
			System.out.println("#nodes = " + g.getNumberOfVertices());
			System.out.println("#edges = " + g.getNumberOfEdges());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//// 
	public static void testJMatIO() throws IOException{
		// LAPLACE
		double eps = 1.0;
		double[] lap = new double[10000];
		for(int i = 0; i < lap.length; i++){
			lap[i] = DPUtil.laplaceMechanism(eps);
//			System.out.print(a[i] + " ");
		}
		// GEOMETRIC
		double alpha = Math.exp(-eps);
		int[] geo = new int[10000];
		for(int i = 0; i < geo.length; i++){
			geo[i] = DPUtil.geometricMechanism(alpha);
		}
		
		//
		MLDouble lapArr = new MLDouble("lapArr", lap, 1);
		MLInt32 geoArr = new MLInt32("geoArr", geo, 1);
		String fileName = "lapgeoArr.mat";
        ArrayList<MLArray> towrite = new ArrayList<MLArray>();
//        towrite.add(lapArr); 
        towrite.add(geoArr);
        
        new MatFileWriter(fileName, towrite );
        System.out.println("Written to file.");
	}
	
	////
	public static void updatePrimitives(Integer s){
		s = s * 2;
	}
	
	////
	public static void check1KSeries() throws IOException {
		File folder = new File("_console");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()){
				String name = listOfFiles[i].getName();
				if (name.contains("_1k_") && name.contains("CONSOLE")){ 
					
					
					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i].getAbsolutePath()));
					while (true){
			        	String str = br.readLine();
			        	if (str == null)
			        		break;
			        	
			        	if (str.contains("n_ceil")){
			        		int n_ceil = Integer.parseInt(str.substring(9));
			        		if (n_ceil < 0)
			        			System.out.println(listOfFiles[i].getName());
			        	}
			        	
					}
			    	br.close();
					
				}
			} 
		}

	}
	
	///////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		
		//
//		testGrph();
		
		//
//		int i = 4/2;
//		System.out.println(i);
//		int[] a = new int[]{0,1,2,3,4,5,6,7,8,9};
//		int[] b = Arrays.copyOfRange(a, 0, 5);
//		System.out.println(b.length);
//		System.out.println();
		
		//
//		Random random = new Random();
//		for (int i = 0; i < 20; i++)
//			System.out.println(random.nextDouble());
		
		// testJMatIO
//		testJMatIO();
		
		
		// TEST Dendrogram.generateSanitizedSample()
//		EdgeListReader reader = new EdgeListReader();
//		EdgeListWriter writer = new EdgeListWriter();
//		Grph G;
//		RegularFile f = new RegularFile("_data/ca-AstroPh.gr");
//		
//		G = reader.readGraph(f);
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
//		
//		Dendrogram T = new Dendrogram();
//		T.readInternalNodes(G, "_out/ca-AstroPh_hrg.0");		// as20graph: 271ms,  ca-AstroPh_hrg: 2.6s
//		
//		long start = System.currentTimeMillis();
//		Grph aG = T.generateSanitizedSample(G.getNumberOfVertices(), false);
//		System.out.println("generateSanitizedSample - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + aG.getNumberOfVertices());
//		System.out.println("#edges = " + aG.getNumberOfEdges());
		
		////
//		Integer s = 3;
//		System.out.println("Before: s = " + s);
//		updatePrimitives(s);
//		System.out.println("After: s = " + s);
		
		////
//		IntSet T = new IntHashSet();
//		int n = 10000;
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < n; i++)
//			T.add(i);
//		System.out.println("init - elapsed " + (System.currentTimeMillis() - start));
		
		////
//		Random random = new Random();
//		for (int i = 0; i < 50; i++)
//			System.out.print(random.nextInt(2) + " ");
		
		
		//// TEST Dendrogram.compute_LS_RS(), Dendrogram.readTree(), NodeSetLouvain.bestCutHRG()
//		long start = System.currentTimeMillis();
////		EdgeIntGraph G = EdgeIntGraph.readEdgeList("_data/polbooks.gr", "\t");	
////		EdgeIntGraph G = EdgeIntGraph.readEdgeList("_data/as20graph.gr", "\t");
////		EdgeIntGraph G = EdgeIntGraph.readEdgeList("_data/ca-AstroPh-wcc.gr", "\t");
////		EdgeIntGraph G = EdgeIntGraph.readEdgeList("_data/com_amazon_ungraph.gr", "\t");
////		EdgeIntGraph G = EdgeIntGraph.readEdgeList("_data/com_dblp_ungraph.gr", "\t");
//		EdgeIntGraph G = EdgeIntGraph.readEdgeList("_data/com_youtube_ungraph.gr", "\t");	
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
		
//		Dendrogram T = new Dendrogram();
//		T.readInternalNodes(G, "_out/polbooks_fixed_20_105_50_1.2_tree.0");
//		T.compute_LS_RS();
//		
//		Queue<Node> queue = new LinkedList<Node>();
//		queue.add(T.root_node);
//		while (queue.size() > 0){
//			Node R = queue.remove();
//
//			System.out.println("R.id = " + R.id + " nL=" + R.nL + " nR=" + R.nR + " nEdge=" + R.nEdge + " level=" + R.level + " toplevel=" + R.toplevel);
//			System.out.println(R.LS);
//			System.out.println(R.RS);
//			
//			if (R.left.id < 0)
//				queue.add(R.left);
//			if (R.right.id < 0)
//				queue.add(R.right);
//			
//		}
		
////		NodeSetLouvain root = Dendrogram.readTree(G, "_out/polbooks_fixed_np_20_105_1000_tree.0");					// 0.3123
////		NodeSetLouvain root = Dendrogram.readTree(G, "_out/as20graph_hrgdiv_np_20_7_2_tree.0");						// -0.0332
////		NodeSetLouvain root = Dendrogram.readTree(G, "_out/as20graph_fixed_np_20_6474_1000_tree.0");				// -0.0351
////		NodeSetLouvain root = Dendrogram.readTree(G, "_out/as20graph_fixed_20_6474_1000_2.0_tree.0");				// level=5,-0.0469, 
////		NodeSetLouvain root = Dendrogram.readTree(G, "_out/ca-AstroPh-wcc_fixed_np_20_17903_1000_tree.0");			// level=9, 0.2099
////		NodeSetLouvain root = Dendrogram.readTree(G, "_out/com_amazon_ungraph_fixed_np_20_334863_1000_tree.0");		// level=12, 0.2616 , level=14, 0.2617
////		NodeSetLouvain root = Dendrogram.readTree(G, "_out/com_dblp_ungraph_fixed_np_20_317080_1000_tree.0");		// level=14, 0.1903
//		NodeSetLouvain root = Dendrogram.readTree(G, "_out/com_youtube_ungraph_fixed_np_20_1134890_1000_tree.0");	// level=16, 0.0288 (mem 3.3GB)
//		System.out.println("readTree - DONE");
//		List<NodeSetLouvain> best_cut = NodeSetLouvain.bestCutHRGFixed(root, G.E(), 16, 0.5);
//		System.out.println("best_cut.size = " + best_cut.size());
		
		//
//		EdgeWeightedGraph G = EdgeWeightedGraph.readEdgeList("_data/as20graph.gr");
//		System.out.println("#nodes = " + G.V());
//		System.out.println("#edges = " + G.E());
//		
//		Louvain lv = new Louvain();
//		long start = System.currentTimeMillis();
//		Map<Integer, Integer> part = lv.best_partition(G, null);
//		System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));

		
		
//		Louvain.writePart(part, "ca-AstroPh-wcc.louvain");
//		System.out.println("writePart - DONE");
		
		// Jul 7, 2016 - test F1 score for subsets
//		int p = 10;
//		int q = 5;
//		int N = p*q;
//		int[] part_A = new int[N];
//		int[] part_B = new int[N];
//		for (int i = 0; i < p; i++)
//			for (int j = 0; j < q; j++){
//				part_A[i*q + j] = i*q + j;
//				part_B[i*q + j] = i;
//			}
//		double f1score = CommunityMeasure.fastAvgF1Score(part_A, part_B);		// = 2/(1+q)
//		System.out.println("f1score = " + f1score);
		
		// test F1 score for first round Louvain
////		int n_nodes = 334863;
////		String dataname = "com_amazon_ungraph";
//		int n_nodes = 1134890;
//		String dataname = "com_youtube_ungraph";
//		
//		String louvain_file = "_data/" + dataname + ".louvain";
//		int[] louvain_part = CommunityMeasure.readPart(louvain_file, n_nodes);
//		String compare_file = "_out/" + dataname + ".f1";
//		int[] compare_part = CommunityMeasure.readPart(compare_file, n_nodes);
////		String compare_file = "_out/com_amazon_ungraph.s1";
////		int[] compare_part = CommunityMeasure.readPart(compare_file, n_nodes);
//		
//		double f1score = CommunityMeasure.fastAvgF1Score(louvain_part, compare_part);
//		System.out.println("f1score = " + f1score);
		
		//
//		check1KSeries();
		
		// test Map<Long,..>
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		long key = 1*Const.BIG_VAL + 2;
		map.put(key,1);
		System.out.println(map.containsKey(key));
		key = 3*Const.BIG_VAL + 2;
		System.out.println(map.containsKey(key));
		
	}

}
