/*
 * Sep 30, 2015
 * 	- convert from C (project orbis/dkTopoGen1k.cc)
 * Nov 2
 * 	- add params accept_self, accept_parallel to dkTopoGen1k_stublist(), dkTopoGen1k()
 * Nov 3
 * 	- add writeFreeStubList()
 * Nov 15
 * 	- adjustDegreeSequence(): converted from degree_seq_hist.py
 * Nov 26
 * 	- adjustDegreeSequence(): alpha = epx(-eps/2)
 */

package dp.generator;

import grph.Grph;
import grph.VertexPair;
import grph.algo.ConnectedComponentsAlgorithm;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dp.DPUtil;
import dp.IndexSorter;
import dp.combined.Const;
import dp.combined.Louvain;
import toools.io.file.RegularFile;
import toools.set.IntSet;
import naive.GreedyReconstruct;
import algs4.EdgeWeightedGraph;

public class Orbis {

	
	//// ex: as20graph_noisy.1k
	public void read1kDistribution(String infile, Map<Integer, Integer> degNumNodesMap) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(infile));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(" ");
        	int degree = Integer.parseInt(items[0]);
        	int numNodesWithDegree = Integer.parseInt(items[1]);
        	degNumNodesMap.put(degree, numNodesWithDegree);
		}
		
		br.close();
	}
	
	//// ex: as20graph_noisy.deg, com_amazon_ungraph.seq
	public void read1kDegrees(String infile, int[] degList) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(infile));
		int nodeId = 0;
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	int degree = Integer.parseInt(str);
        	degList[nodeId++] = degree;
		}
		
		br.close();
	}
	
	//// degList: node -> deg 
	public int dkTopoGen1k(Grph g, int[] degList, boolean accept_self, boolean accept_parallel, String stub_file) throws IOException{
		
		Map<Long, Integer> adjacencyMap = new HashMap<Long, Integer>();
		
		// Update our adjacency map with the existing edges so far.  This
		// in case someone wants to re-wire the graph and continue
		// connecting stubs
		for (VertexPair p : g.getEdgePairs()){
			int v1 = p.first;
			int v2 = p.second;
			
			adjacencyMap.put(v1 * Const.BIG_VAL + v2, 1);
			adjacencyMap.put(v2 * Const.BIG_VAL + v1, 1);
		}
		
		List<Stub> freeStubList = new ArrayList<Stub>();
		for (int nodeId = 0; nodeId < degList.length; nodeId++){
			int degree = degList[nodeId];
			for (int k = 0; k < degree; k++){
				Stub stub = new Stub();
				stub.nodeid = nodeId;
				stub.degree = degree;
				freeStubList.add(stub);
				
			}
		}
		System.out.println("BEFORE: freeStubList.size = " + freeStubList.size());
		
		dkShuffleList(freeStubList);
		
		System.out.println("AFTER : freeStubList.size = " + freeStubList.size());
		
		//
		return dkTopoGen1k_stublist(g, freeStubList, adjacencyMap, accept_self, accept_parallel);
		
	}
	
	////
	public static void dkShuffleList(List<Stub> freeStubList){
		int listSize = freeStubList.size();
		Stub[] theVector = new Stub[listSize];
		
		// copy data into a vector O(n)
		int cnt = 0;
		for (Stub stub : freeStubList)
			theVector[cnt++] = stub;
		
		// shuffle the vector - O(n)
		Random random = new Random();
		for (int i = 0; i < listSize; i++) {
			int randIdx = random.nextInt(listSize);
			
			Stub tmp = theVector[randIdx];
			theVector[randIdx] = theVector[i];
			theVector[i] = tmp;
		}
		
		// copy back into list - O(n)
		freeStubList.clear();
		for (int i = 0; i < listSize; i++)
			freeStubList.add(theVector[i]);
	}
	
	//// 
	public void createStubFile(int[] degList, String stub_file) throws IOException{
		List<Stub> freeStubList = new ArrayList<Stub>();
		for (int nodeId = 0; nodeId < degList.length; nodeId++){
			int degree = degList[nodeId];
			for (int k = 0; k < degree; k++){
				Stub stub = new Stub();
				stub.nodeid = nodeId;
				stub.degree = degree;
				freeStubList.add(stub);
				
			}
		}
		System.out.println("BEFORE: freeStubList.size = " + freeStubList.size());
		dkShuffleList(freeStubList);
		System.out.println("AFTER : freeStubList.size = " + freeStubList.size());
		
		writeFreeStubList(freeStubList, stub_file);
		System.out.println("writeFreeStubList - DONE");
	}
	
	////
	public void writeFreeStubList(List<Stub> freeStubList, String stub_file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(stub_file));
		for (Stub stub : freeStubList)
			bw.write(stub.nodeid + " " + stub.degree + "\n");
		
		bw.close();
	}
	
	////
	public List<Stub> readFreeStubList(String stub_file) throws IOException{
		List<Stub> freeStubList = new ArrayList<Stub>();
		
		BufferedReader br = new BufferedReader(new FileReader(stub_file));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(" ");
        	int nodeid = Integer.parseInt(items[0]);
        	int degree = Integer.parseInt(items[1]);
        	
        	Stub stub = new Stub();
			stub.nodeid = nodeid;
			stub.degree = degree;
			freeStubList.add(stub);
		}
		br.close();

		//
		return freeStubList;
	}
	
	////
	public int dkTopoGen1k_stublist(Grph g, List<Stub> freeStubList, Map<Long, Integer> adjacencyMap, boolean accept_self, boolean accept_parallel){
		int needToRewire = 0;
		
		int nextStubIter = 0;
		int count = 0;
		boolean[] mark = new boolean[freeStubList.size()];		// mark stubs used
		while (true) {
			
			Stub stub1 = freeStubList.get(nextStubIter);
			Stub stub_i = new Stub();
			
			int i;
			for (i = nextStubIter+1; i < freeStubList.size(); i++) {
				if (mark[i] == true)
					continue;
				
				stub_i = freeStubList.get(i);
				
				if (!accept_self)
					if (stub_i.nodeid == stub1.nodeid)
						continue;
				
				if (!accept_parallel)
					if (adjacencyMap.containsKey(stub_i.nodeid * Const.BIG_VAL + stub1.nodeid))
						continue;

				break;
			}
			
			if (i == freeStubList.size()) {
				nextStubIter++;		// IMPORTANT
				if (nextStubIter == freeStubList.size())
					break;
				
				needToRewire++;
				continue;
			}
			
			count += 1;
			g.addSimpleEdge(stub1.nodeid, stub_i.nodeid, false);
			adjacencyMap.put(stub1.nodeid * Const.BIG_VAL + stub_i.nodeid, 1);
			adjacencyMap.put(stub_i.nodeid * Const.BIG_VAL + stub1.nodeid, 1);
			
			
//			freeStubList.remove(i);
			mark[i] = true;
			
			nextStubIter ++;
			
			while (nextStubIter < freeStubList.size() && mark[nextStubIter] == true)
				nextStubIter ++;
			if (nextStubIter == freeStubList.size())
				break;
			
			// debug
//			if (nextStubIter % 1000 == 0 || nextStubIter > freeStubList.size() - 1000)
//				System.out.println(nextStubIter);
			
		}
		System.out.println("count = " + count);
		
		//
		return needToRewire;
	}
	
	////
	public void rewireToLargestConnectedComponent1k(Grph g){
		
	}
	
	////
	public int extractLargestConnectedComponent(Grph g){
		
		return 0;
	}
	
	////
	public static void writeDegSeq(Grph G, String seqfile) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(seqfile));
		for (int u = 0; u < G.getNumberOfVertices(); u++){
			bw.write(G.getVertexDegree(u) + "\n");
		}
		
		bw.close();
	}
	
	////
	public static void write1kDistribution(Grph G, String outfile_1k) throws IOException{
		
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile_1k));
		for (int u = 0; u < G.getNumberOfVertices(); u++){
			int deg = G.getVertexDegree(u);
			
			if (map.containsKey(deg))
				map.put(deg, map.get(deg) + 1);
			else
				map.put(deg, 1);
		}
		
		for (Map.Entry<Integer, Integer> entry : map.entrySet())
			bw.write(entry.getKey() + " " + entry.getValue() + "\n");
		
		bw.close();
	}
	
	//// converted from Python
	public static Integer[] adjustDegreeSequence(Grph G, double eps, String seq_file) throws IOException{
		int n_nodes = G.getNumberOfVertices();
		//
		Integer[] degSeq = new Integer[n_nodes];
		
		for (int u = 0; u < n_nodes; u++)
			degSeq[u] = G.getVertexDegree(u);
		
		//debug
//		for (int d : degSeq)
//			System.out.print(d + " ");
//		System.out.println();
		
		// add geometric noise
		double alpha = Math.exp(-eps/2);		// NOTE : eps/2 !
		int sum_deg = 0;
		for (int u = 0; u < n_nodes; u++){
			degSeq[u] += DPUtil.geometricMechanism(alpha);
			sum_deg += degSeq[u];
		}
		
		if (sum_deg % 2 == 1){
			degSeq[0] += 1;
			sum_deg += 1;
		}
		System.out.println("BEFORE: sum_deg = " + sum_deg);
		
		// index sort
		IndexSorter<Integer> is = new IndexSorter<Integer>(degSeq);
		is.sort();
		
		Integer[] idx = is.getIndexes();
			
		// compute degSeq
		double[] deg_ratio = new double[n_nodes];
		for (int u = 0; u < n_nodes; u++){
	        if (degSeq[u] > 0)
	            deg_ratio[u] = (double)degSeq[u]/sum_deg;    // use ABS()
	        else
	            deg_ratio[u] = 1.0/sum_deg;
		}
		
		// adjust new_degSeq
	    int ceil_s = 0;
	    for (int u = 0; u < n_nodes; u++)
	        ceil_s += (int)Math.ceil(deg_ratio[u]*sum_deg);
	    int n_ceil = n_nodes - (ceil_s - sum_deg);
	    
	    System.out.println("n_ceil = " + n_ceil);    
		
	    for (int i = 0; i < n_nodes; i++){
	    	int u =  idx[i];
	        if (i < n_ceil)
	        	degSeq[u] = (int)Math.ceil(deg_ratio[u]*sum_deg);    // ceiling
	        else     
	        	degSeq[u] = (int)Math.ceil(deg_ratio[u]*sum_deg) - 1;
	    }
	    
	    // write degSeq to seq_file
	    BufferedWriter bw = new BufferedWriter(new FileWriter(seq_file));
		for (int d: degSeq)
			bw.write(d + "\n");
		
		bw.close();
	    
	    //
	    return degSeq;
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		
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
	    int n_samples = 20;
		double eps = 2.0;		
		
		if(args.length >= 4){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			eps = Double.parseDouble(args[3]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("eps = " + eps);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		String seq_file = prefix + "_out/" + dataname + "_1k_" + String.format("%.1f",eps);
		String sample_file = prefix + "_sample/" + dataname + "_1k_" + String.format("%.1f", eps);
	    System.out.println("seq_file = " + seq_file);
		
	    Grph G;
	    EdgeListReader reader = new EdgeListReader();
		RegularFile f = new RegularFile(filename);
		long start = System.currentTimeMillis();
		G = reader.readGraph(f);
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());  
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		int n_nodes = G.getNumberOfVertices();
		Orbis orbis = new Orbis();
		
		for (int i = 0; i < n_samples; i++){
	    	System.out.println("sample i = " + i);
	    	
	    	start = System.currentTimeMillis();
			Integer[] degSeq = adjustDegreeSequence(G, eps, seq_file + "." + i + ".seq");
			System.out.println("adjustDegreeSequence - DONE, elapsed " + (System.currentTimeMillis() - start));
			//
			int sum_deg = 0;
			for (int d : degSeq){
	//			System.out.print(d + " ");
				sum_deg += d;
			}
	//		System.out.println();
			System.out.println("sum_deg = " + sum_deg);
			
			// 
			
			int[] degList = new int[n_nodes]; 
			
			orbis.read1kDegrees(seq_file + "." + i + ".seq", degList);
			System.out.println("degList.length = " + degList.length);
			
			start = System.currentTimeMillis();
			Grph g = new InMemoryGrph();
			g.addNVertices(n_nodes);
			int need2Rewire = orbis.dkTopoGen1k(g, degList, true, true, "");
			
			f = new RegularFile(sample_file + "." + i);
			EdgeListWriter writer = new EdgeListWriter();
	    	writer.writeGraph(g, f);
			
			System.out.println("dkTopoGen1k - DONE, elapsed " + (System.currentTimeMillis() - start));
		}
		
		
		
		
		/////////////////// TEST Orbis
//		int n_nodes = 105;
//		String filename = "_data/polbooks.gr";
//		String seqfile = "_dk/polbooks.seq";				// 
//		String part_file = "_out/polbooks_orbis_java.part";	
//		String stub_file = "_dk/polbooks.2.stub";
//		
////		int n_nodes = 6474;
////		String filename = "_data/as20graph.gr";
////		String seqfile = "_dk/as20graph.seq";				// 0.06s, #components = 155
////		String part_file = "_out/as20graph_orbis_java.part";	
////		String stub_file = "_dk/as20graph.1.stub";
//		
////		int n_nodes = 334863;
////		String filename = "_data/com_amazon_ungraph.gr";
////		String seqfile = "_dk/com_amazon_ungraph.seq";		// 2.5s, #components = 181, need2Rewire = 0, real modularity = -1.0579464325720469E-4
////		String part_file = "_out/com_amazon_ungraph_orbis_java.part";
////		String stub_file = "_dk/com_amazon_ungraph.cpp.1.stub";		// cpp.1.stub: real modularity = 0.0133
//		
////		int n_nodes = 317080;								// 2.5s, #components = 462, need2Rewire = 0, real modularity = -2.692618648107859E-4
////		String filename = "_data/com_dblp_ungraph.gr";
////		String seqfile = "_dk/com_dblp_ungraph.seq";
////		String part_file = "_out/com_dblp_ungraph_orbis_java.part";
////		String stub_file = "_dk/com_dblp_ungraph.cpp.1.stub";		// cpp.1.stub: real modularity = 0.1722
//		
////		int n_nodes = 1134890;
////		String filename = "_data/com_youtube_ungraph.gr";
////		String seqfile = "_dk/com_youtube_ungraph.seq";		// 11s, #components = 32343, need2Rewire = 0
////		String part_file = "_out/com_youtube_ungraph_orbis_java.part";
//		
//		//
//		Grph g = new InMemoryGrph();
//		g.addNVertices(n_nodes);
//		
//		Orbis orbis = new Orbis();
//		int[] degList = new int[n_nodes]; 
//		
//		orbis.read1kDegrees(seqfile, degList);
//		System.out.println("degList.length = " + degList.length);
//		
//		long start = System.currentTimeMillis();
//		boolean accept_self = false;
//		boolean accept_parallel = false;
//		System.out.println("accept_self = " + accept_self + " , accept_parallel = " + accept_parallel);
//		// OLD (freeStubList created on the fly, not written to .stub file)
//		int need2Rewire = orbis.dkTopoGen1k(g, degList, accept_self, accept_parallel, stub_file);
//
//		// NEW (read freeStubList from .stub file, for C++ comparison)
//////		orbis.createStubFile(degList, stub_file);	// commented when read C++ .stub file (e.g. com_amazon_ungraph.cpp.1.stub)
////		
////		System.out.println("stub_file = " + stub_file);
////		List<Stub> freeStubList = orbis.readFreeStubList(stub_file);
////		Map<Int2, Integer> adjacencyMap = new HashMap<Int2, Integer>();
////		int need2Rewire = orbis.dkTopoGen1k_stublist(g, freeStubList, adjacencyMap, accept_self, accept_parallel);
//		
//		System.out.println("dkTopoGen1k - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + g.getNumberOfVertices());
//		System.out.println("#edges = " + g.getNumberOfEdges());
//		
//		ConnectedComponentsAlgorithm algo = new ConnectedComponentsAlgorithm();
//		Collection<IntSet> components = algo.compute(g);
//		System.out.println("#components = " + components.size());
//		
//		System.out.println("need2Rewire = " + need2Rewire);
//		
//		// compare to true graph
//		EdgeListReader reader = new EdgeListReader();
//		Grph G0;
//		RegularFile f = new RegularFile(filename);
//		
//		start = System.currentTimeMillis();
//		G0 = reader.readGraph(f);
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G0.getNumberOfVertices());
//		System.out.println("#edges = " + G0.getNumberOfEdges());
//		
//		int deg_diff = 0;
//		for (int u = 0; u < G0.getNumberOfVertices(); u++)
//			deg_diff += Math.abs(g.getVertexDegree(u) - G0.getVertexDegree(u));
//		System.out.println("deg_diff = " + deg_diff);
//		
//		// modularity
//		EdgeWeightedGraph g2 = GreedyReconstruct.convertGraph(g);
//		Louvain lv = new Louvain();
//		start = System.currentTimeMillis();
//		Map<Integer, Integer> part = lv.best_partition(g2, null);
//		System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
////		Louvain.writePart(part, part_file);
////		System.out.println("writePart - DONE");
//		
//		System.out.println("real modularity = " + GreedyReconstruct.modularity(G0, part));
		
		
		
		////////////////////// C++ 
//		// TEST writeDegSeq + run dkTopoGen1k_new.exe + read .gen file and compute components/modularity
////		String dataname = "karate";			// (34, 78)
////		String dataname = "polbooks";		// (105, 441)		
////		String dataname = "polblogs";		// (1224,16715) 	
////		String dataname = "as20graph";		// (6474,12572)		
////		String dataname = "wiki-Vote";		// (7115,100762)
////		String dataname = "ca-HepPh";		// (12006,118489) 	
////		String dataname = "ca-AstroPh";		// (18771,198050) 	
//		// WCC
////		String dataname = "polblogs-wcc";			// (1222,16714) 	
////		String dataname = "wiki-Vote-wcc";			// (7066,100736) 	
////		String dataname = "ca-HepPh-wcc";			// (11204,117619) 
////		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 
//		// LARGE
////		String dataname = "com_amazon_ungraph";		// (334863,925872)				real modularity = 0.0128, 0.0136, 0.0127, 0.0126
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)				real modularity = 0.1968, 0.1833, 0.1988, 0.1988
////		String dataname = "com_youtube_ungraph";	// (1134890,2987624)
//													//						
//		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
//		String prefix = "";
//	    System.out.println("dataname = " + dataname);
//	    
//		String filename = prefix + "_data/" + dataname + ".gr";
//		String seqfile = prefix + "_dk/" + dataname + ".seq";
//		String outfile_1k = prefix + "_dk/" + dataname + ".1k";
//		String genfile = "D:/git/itce2011/orbis/out2/" + dataname + ".1.gen";
//		
//		EdgeListReader reader = new EdgeListReader();
//		Grph G;
//		RegularFile f = new RegularFile(filename);
//		
//		long start = System.currentTimeMillis();
//		G = reader.readGraph(f);
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G.getNumberOfVertices());
//		System.out.println("#edges = " + G.getNumberOfEdges());
//		
//		// write .deg and .seq files, then run dkTopoGen1k_new.exe (external)
////		writeDegSeq(G, seqfile);
////		System.out.println("writeDegSeq - DONE");
////		
////		write1kDistribution(G, outfile_1k);
////		System.out.println("write1kDistribution - DONE");
//		
//		// read .gen file and compute components/modularity
//		Grph G2;
//		f = new RegularFile(genfile);
//		
//		start = System.currentTimeMillis();
//		G2 = reader.readGraph(f);
//		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
//		
//		System.out.println("#nodes = " + G2.getNumberOfVertices());
//		System.out.println("#edges = " + G2.getNumberOfEdges());
//		
//		int deg_diff = 0;
//		for (int u = 0; u < G2.getNumberOfVertices(); u++)
//			deg_diff += Math.abs(G.getVertexDegree(u) - G2.getVertexDegree(u));
//		System.out.println("deg_diff = " + deg_diff);
//		
//		ConnectedComponentsAlgorithm algo = new ConnectedComponentsAlgorithm();
//		Collection<IntSet> components = algo.compute(G2);
//		System.out.println("#components = " + components.size());
//		
//		Louvain lv = new Louvain();
//		EdgeWeightedGraph g2 = GreedyReconstruct.convertGraph(G2);
//		Map<Integer, Integer> part = lv.best_partition(g2, null);
//		
//		System.out.println("real modularity = " + GreedyReconstruct.modularity(G, part));
		
	}

}
