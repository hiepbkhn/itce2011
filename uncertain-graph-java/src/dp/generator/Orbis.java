/*
 * Sep 30, 2015
 * 	- convert from C (project orbis/dkTopoGen1k.cc)
 * Nov 2
 * 	- add params accept_self, accept_parallel to dkTopoGen1k_stublist(), dkTopoGen1k()
 */

package dp.generator;

import grph.Grph;
import grph.VertexPair;
import grph.algo.ConnectedComponentsAlgorithm;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dp.combined.Louvain;
import toools.io.file.RegularFile;
import toools.set.IntSet;
import naive.GreedyReconstruct;
import algs4.EdgeWeightedGraph;

public class Orbis {

	class Stub{
		public int nodeid;
		public int degree;
		
		public Stub(){
			
		}
	}
	
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
	public int dkTopoGen1k(Grph g, int[] degList, boolean accept_self, boolean accept_parallel){
		
		Map<Int2, Integer> adjacencyMap = new HashMap<Int2, Integer>();
		
		// Update our adjacency map with the existing edges so far.  This
		// in case someone wants to re-wire the graph and continue
		// connecting stubs
		for (VertexPair p : g.getEdgePairs()){
			int v1 = p.first;
			int v2 = p.second;
			
			adjacencyMap.put(new Int2(v1, v2), 1);
			adjacencyMap.put(new Int2(v2, v1), 1);
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
	public void dkShuffleList(List<Stub> freeStubList){
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
	public int dkTopoGen1k_stublist(Grph g, List<Stub> freeStubList, Map<Int2, Integer> adjacencyMap, boolean accept_self, boolean accept_parallel){
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
					if (adjacencyMap.containsKey(new Int2(stub_i.nodeid, stub1.nodeid)))
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
			adjacencyMap.put(new Int2(stub1.nodeid, stub_i.nodeid), 1);
			adjacencyMap.put(new Int2(stub_i.nodeid, stub1.nodeid), 1);
			
			
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
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		// TEST Orbis
		int n_nodes = 6474;
		String filename = "_data/as20graph.gr";
		String seqfile = "_dk/as20graph.seq";				// 0.06s, #components = 155
		String part_file = "_out/as20graph_orbis_java.part";	
		
//		int n_nodes = 334863;
//		String filename = "_data/com_amazon_ungraph.gr";
//		String seqfile = "_dk/com_amazon_ungraph.seq";		// 2.5s, #components = 181, need2Rewire = 0, real modularity = -1.0579464325720469E-4
//		String part_file = "_out/com_amazon_ungraph_orbis_java.part";
		
//		int n_nodes = 317080;								// 2.5s, #components = 462, need2Rewire = 0, real modularity = -2.692618648107859E-4
//		String filename = "_data/com_dblp_ungraph.gr";
//		String seqfile = "_dk/com_dblp_ungraph.seq";
//		String part_file = "_out/com_dblp_ungraph_orbis_java.part";
		
//		int n_nodes = 1134890;
//		String filename = "_data/com_youtube_ungraph.gr";
//		String seqfile = "_dk/com_youtube_ungraph.seq";		// 11s, #components = 32343, need2Rewire = 0
//		String part_file = "_out/com_youtube_ungraph_orbis_java.part";
		
		//
		Grph g = new InMemoryGrph();
		g.addNVertices(n_nodes);
		
		Orbis orbis = new Orbis();
		int[] degList = new int[n_nodes]; 
		
		orbis.read1kDegrees(seqfile, degList);
		System.out.println("degList.length = " + degList.length);
		
		long start = System.currentTimeMillis();
		boolean accept_self = false;
		boolean accept_parallel = false;
		int need2Rewire = orbis.dkTopoGen1k(g, degList, accept_self, accept_parallel);
		System.out.println("dkTopoGen1k - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + g.getNumberOfVertices());
		System.out.println("#edges = " + g.getNumberOfEdges());
		
		ConnectedComponentsAlgorithm algo = new ConnectedComponentsAlgorithm();
		Collection<IntSet> components = algo.compute(g);
		System.out.println("#components = " + components.size());
		
		System.out.println("need2Rewire = " + need2Rewire);
		
		// compare to true graph
		EdgeListReader reader = new EdgeListReader();
		Grph G0;
		RegularFile f = new RegularFile(filename);
		
		start = System.currentTimeMillis();
		G0 = reader.readGraph(f);
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G0.getNumberOfVertices());
		System.out.println("#edges = " + G0.getNumberOfEdges());
		
		int deg_diff = 0;
		for (int u = 0; u < G0.getNumberOfVertices(); u++)
			deg_diff += Math.abs(g.getVertexDegree(u) - G0.getVertexDegree(u));
		System.out.println("deg_diff = " + deg_diff);
		
		// modularity
		EdgeWeightedGraph g2 = GreedyReconstruct.convertGraph(g);
		Louvain lv = new Louvain();
		start = System.currentTimeMillis();
		Map<Integer, Integer> part = lv.best_partition(g2, null);
		System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));
		
//		Louvain.writePart(part, part_file);
//		System.out.println("writePart - DONE");
		
		System.out.println("real modularity = " + GreedyReconstruct.modularity(G0, part));
		
		
		////////////////////// C++
		// TEST writeDegSeq + run dkTopoGen1k_new.exe + read .gen file and compute components/modularity
////		String dataname = "karate";			// (105, 441)
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
//		String dataname = "ca-AstroPh-wcc";			// (17903,196972) 
//		// LARGE
////		String dataname = "com_amazon_ungraph";		// (334863,925872)				real modularity = 0.012803880783817096
////		String dataname = "com_dblp_ungraph";		// (317080,1049866)				real modularity = 0.19680873520515552
////		String dataname = "com_youtube_ungraph";	// (1134890,2987624)
//													//						
//		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
//		String prefix = "";
//	    System.out.println("dataname = " + dataname);
//	    
//		String filename = prefix + "_data/" + dataname + ".gr";
//		String seqfile = prefix + "_dk/" + dataname + ".seq";
//		String outfile_1k = prefix + "_dk/" + dataname + ".1k";
//		String genfile = prefix + "D:/git/itce2011/orbis/out2/" + dataname + ".gen";
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
//		// run dkTopoGen1k_new.exe
//		writeDegSeq(G, seqfile);
//		System.out.println("writeDegSeq - DONE");
//		
//		write1kDistribution(G, outfile_1k);
//		System.out.println("write1kDistribution - DONE");
//		
//		// read .gen file and compute components/modularity
////		Grph G2;
////		f = new RegularFile(genfile);
////		
////		start = System.currentTimeMillis();
////		G2 = reader.readGraph(f);
////		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
////		
////		System.out.println("#nodes = " + G2.getNumberOfVertices());
////		System.out.println("#edges = " + G2.getNumberOfEdges());
////		
////		int deg_diff = 0;
////		for (int u = 0; u < G2.getNumberOfVertices(); u++)
////			deg_diff += Math.abs(G.getVertexDegree(u) - G2.getVertexDegree(u));
////		System.out.println("deg_diff = " + deg_diff);
////		
////		ConnectedComponentsAlgorithm algo = new ConnectedComponentsAlgorithm();
////		Collection<IntSet> components = algo.compute(G2);
////		System.out.println("#components = " + components.size());
////		
////		Louvain lv = new Louvain();
////		EdgeWeightedGraph g2 = GreedyReconstruct.convertGraph(G2);
////		Map<Integer, Integer> part = lv.best_partition(g2, null);
////		
////		System.out.println("real modularity = " + GreedyReconstruct.modularity(G, part));
	}

}
