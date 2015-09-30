/*
 * Sep 30, 2015
 * 	- convert from C (project orbis/dkTopoGen1k.cc)
 */

package dp.combined;

import grph.Grph;
import grph.VertexPair;
import grph.algo.ConnectedComponentsAlgorithm;
import grph.in_memory.InMemoryGrph;
import hist.Int2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	
	//// ex: as20graph_noisy.deg
	public void read1kDegrees(String infile, int[] degList) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(infile));
		while (true){
        	String str = br.readLine();
        	if (str == null)
        		break;
        	
        	String[] items = str.split(" ");
        	int nodeId = Integer.parseInt(items[0]);
        	int degree = Integer.parseInt(items[1]);
        	degList[nodeId] = degree;
		}
		
		br.close();
	}
	
	//// Map<Integer, Integer> degNumNodesMap, 
	public int dkTopoGen1k(Grph g, int[] degList){
		
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
		return dkTopoGen1k_stublist(g, freeStubList, adjacencyMap);
		
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
		for (int i = 0; i < listSize; ++i) {
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
	public int dkTopoGen1k_stublist(Grph g, List<Stub> freeStubList, Map<Int2, Integer> adjacencyMap){
		int needToRewire = 0;
		
		int nextStubIter = 0;
		int count = 0;
		while (freeStubList.size() > 0) {
			
			int toDeleteIter = nextStubIter;
			
			Stub stub1 = freeStubList.get(nextStubIter);
			Stub stub_i = new Stub();
			
			int i;
			for (i = nextStubIter+1; i < freeStubList.size(); i++) {
				// Check that they aren't connected to the same node or
				// aren't already connected
				stub_i = freeStubList.get(i);
				if (stub_i.nodeid == stub1.nodeid)
					continue;
				
				if (adjacencyMap.containsKey(new Int2(stub_i.nodeid, stub1.nodeid)))
					continue;

				break;
			}
			
			if (i == freeStubList.size()) {
				needToRewire++;
				continue;
			}
			
			Stub stub2 = stub_i;
			// at this point stub1 and stub2 are both references to stubs
			// which we want to connect.
			count += 1;
			g.addSimpleEdge(stub1.nodeid, stub2.nodeid, false);
			adjacencyMap.put(new Int2(stub1.nodeid, stub2.nodeid), 1);
			adjacencyMap.put(new Int2(stub2.nodeid, stub1.nodeid), 1);
			
			// Safe to delete this iterator because no prevoius iterators
			// are pointing to it (we advanced nexstStubIter)
			freeStubList.remove(toDeleteIter);
			
			if (freeStubList.size() <= 1)
				break;
			
			freeStubList.remove(i);
			
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
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		int n_nodes = 6474;
//		String infile = "_dk/as20graph_noisy.deg";
		String infile = "D:/git/itce2011/uncertain-graph/_out/as20graph_noisy.deg";
		String part_file = "_out/as20graph_noisy.part";
		
		Grph g = new InMemoryGrph();
		g.addNVertices(n_nodes);
		
		Orbis orbis = new Orbis();
		int[] degList = new int[n_nodes]; 
		
		orbis.read1kDegrees(infile, degList);
		System.out.println("degList.length = " + degList.length);
		
		long start = System.currentTimeMillis();
		int need2Rewire = orbis.dkTopoGen1k(g, degList);
		System.out.println("dkTopoGen1k - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + g.getNumberOfVertices());
		System.out.println("#edges = " + g.getNumberOfEdges());
		
		ConnectedComponentsAlgorithm algo = new ConnectedComponentsAlgorithm();
		Collection<IntSet> components = algo.compute(g);
		System.out.println("#components = " + components.size());
		
		System.out.println("need2Rewire = " + need2Rewire);
		
		// modularity
		EdgeWeightedGraph g2 = GreedyReconstruct.convertGraph(g);
		Louvain lv = new Louvain();
		start = System.currentTimeMillis();
		Map<Integer, Integer> part = lv.best_partition(g2, null);
		System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		Louvain.writePart(part, part_file);
		System.out.println("writePart - DONE");
		
	}

}
