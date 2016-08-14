/*
 * Aug 12, 2016
 * 	- 
 */

package naive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import algs4.Edge;
import algs4.EdgeIntGraph;
import algs4.EdgeWeightedGraph;
import dp.DPUtil;
import dp.combined.Const;
import dp.combined.LouvainDP;
import dp.generator.Orbis;
import dp.generator.Stub;
import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import hist.Int2;
import toools.io.file.RegularFile;

public class HighPass1k {

	//// copied from LouvainDP
	public static EdgeWeightedGraph filterEqualPrivate(EdgeWeightedGraph G, int k, double eps) {
		int n = G.V();
		Map<Integer, Integer> part_init = LouvainDP.initEqualCommunity(n, n/k);
		
		EdgeWeightedGraph G_new = LouvainDP.getGraphByPartition(G, part_init);
		int n_new = G_new.V();
		System.out.println("G_new.V = " + G_new.V() + " G_new.E = " + G_new.E());
		
		// compute theta
		int m_new = G_new.E();
		double m = (double)n_new * (n_new + 1.0)/2;		// n + 1: consider all diagonal edges
		System.out.println("m = " + m);
		if (m <= m_new){
			System.err.println("m <= m_new. Exit!");
			return null;
		}
		
		double s = m_new;			// fix s
		System.out.println("s before = " + s);
		double alpha = Math.exp(-eps);
		double theta = Math.ceil( (Math.log(m-m_new) - Math.log((1+alpha)*s)) / eps); // 1+alpha: ONE-SIDED, round up theta
		s = (m - m_new)*Math.pow(alpha, theta) /(1+alpha);	// recompute s
		System.out.println("s after = " + s);
		
		System.out.println("alpha = " + alpha);
		System.out.println("theta = " + theta);
		
		// add geometric noise to G_new edge weights
		EdgeWeightedGraph G_final = new EdgeWeightedGraph(n_new);		// FIXED May 8,2016: EdgeWeightedGraph(n) -> n_new
		System.out.println("G_final.V = " + G_final.V());
		
		for (Edge e : G_new.edges()){
			double value = e.weight() + DPUtil.geometricMechanism(alpha);		// Geometric mechanism
			if (value >= theta){
				e.setWeight(value);
				G_final.addEdge(e);
			}
		}
		System.out.println("BEFORE: G_final.E = " + G_final.E());
		
		// sample s zero-cells
		Random random = new Random();
		double EPS = 0.000001;
		for (int i = 0; i < s; i++){
			int u = random.nextInt(n_new);
			int v = random.nextInt(n_new);
			if (G_new.getEdge(u, v) == null & G_final.getEdge(u, v) == null){
				// sample weight from Pr[X <= x] = 1 - alpha^(x-theta+1)	(ONE-SIDED)
				double r = random.nextDouble();	// r in (0,1)
				if (r > 1 - EPS)
					r = 1 - EPS;
				int weight = (int)(Math.log(1-r)/(-eps) + theta - 1);		// int
				if (weight > 0){
					Edge e = new Edge(u, v, weight);
					G_final.addEdge(e);
				}
			}
		}
		

		System.out.println("AFTER: G_final.E = " + G_final.E());
		System.out.println("G_final.totalWeight = " + G_final.totalWeight());
		//
		return G_final;
	}	
	
	//// not use high-pass filter
	public static EdgeWeightedGraph publishEqualPrivate(EdgeWeightedGraph G, int k, double eps, Map<Integer, Integer> part_init) {
		int n = G.V();
		
		EdgeWeightedGraph G_new = LouvainDP.getGraphByPartition(G, part_init);
		int n_new = G_new.V();
		System.out.println("G_new.V = " + G_new.V() + ", G_new.E = " + G_new.E() + ", G_new.totalWeight = " + G_new.totalWeight());
		
		// add noise to edges of G_new
		double alpha = Math.exp(-eps);
		
		for (Edge e : G_new.edges()){
			e.setWeight(e.weight() + DPUtil.geometricMechanism(alpha));		// Geometric mechanism
			// truncated
			if (e.weight() < 0)
				e.setWeight(0);
		}
		System.out.println("G_new.totalWeight = " + G_new.totalWeight());
		
		//
		return G_new;
	}	
	
	//// configuration model
	//	degSeq: noisy degree sequence, sG: noisy super-graph
	public static Grph recoverGraph(Integer[] degSeq, EdgeWeightedGraph sG, Map<Integer, Integer> part_init){
		int n_nodes = degSeq.length;
		
		System.out.println("part_init.size = " + part_init.size());
		
		EdgeWeightedGraph tempG = sG.clone();
		for (Edge e : tempG.edges())
			e.setWeight(0.0);		// reset weight
		
		// 1 - prepare and shuffle freeStubList
		Map<Long, Integer> adjacencyMap = new HashMap<Long, Integer>();
		
		List<Stub> freeStubList = new ArrayList<Stub>();
		for (int nodeId = 0; nodeId < degSeq.length; nodeId++){
			int degree = degSeq[nodeId];
			for (int k = 0; k < degree; k++){
				Stub stub = new Stub();
				stub.nodeid = nodeId;
				stub.degree = degree;
				freeStubList.add(stub);
			}
		}
		System.out.println("BEFORE: freeStubList.size = " + freeStubList.size());
		Orbis.dkShuffleList(freeStubList);
		System.out.println("AFTER : freeStubList.size = " + freeStubList.size());
		
		// 2 - rewire
		boolean accept_self = false;
		boolean accept_parallel = false;
		int needToRewire = 0;
		int nextStubIter = 0;
		int count = 0;
		boolean[] mark = new boolean[freeStubList.size()];		// mark stubs used
		Grph g = new InMemoryGrph();
		g.addNVertices(n_nodes);
		
		while (true) {
			
			Stub stub1 = freeStubList.get(nextStubIter);
			int v1 = part_init.get(stub1.nodeid);
			int v2 = -1;
			
			Stub stub_i = new Stub();
			
			int i;
			for (i = nextStubIter+1; i < freeStubList.size(); i++) {
				if (mark[i] == true)
					continue;
				
				stub_i = freeStubList.get(i);
				
				// check
				if (!accept_self)
					if (stub_i.nodeid == stub1.nodeid)
						continue;
				
				if (!accept_parallel)
					if (adjacencyMap.containsKey(new Int2(stub_i.nodeid, stub1.nodeid)))
						continue;
				
				v2 = part_init.get(stub_i.nodeid);
				if (!tempG.areEdgesAdjacent(v1, v2))
					continue;
				if (tempG.getEdge(v1, v2).weight() > 1.1 * sG.getEdge(v1, v2).weight())			// NOTICE temp constant 1.1 !
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
			
			tempG.getEdge(v1,v2).incWeight(1);		// IMPORTANT
			if (v2 != v1)
				tempG.getEdge(v2,v1).incWeight(1);
			
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
//			if (nextStubIter % 1000 == 0)
////			if (nextStubIter % 100 == 0 || nextStubIter > freeStubList.size() - 1000)
//				System.out.println(nextStubIter);
			
		}
		System.out.println("count = " + count);
		System.out.println("g.#nodes = " + g.getNumberOfVertices());
		System.out.println("g.#edges = " + g.getNumberOfEdges());
		
		//
		return g;
	}
	
	////
	public static Grph greedyReconstruct(Grph G, double eps, int k, String seq_file) throws IOException{
		int n_nodes = G.getNumberOfVertices();
		double r = 0.5;
		double eps1 = r*eps; 
		double eps2 = (1-r)*eps;
		
		// noisy 1K-series
		Integer[] degSeq = Orbis.adjustDegreeSequence(G, eps1, seq_file + ".0.seq");
			
		// super graph
		EdgeWeightedGraph G2 = GreedyReconstruct.convertGraph(G);
		
		Map<Integer, Integer> part_init = LouvainDP.initEqualCommunity(n_nodes, n_nodes/k);
		
		EdgeWeightedGraph sG = publishEqualPrivate(G2, k, eps2, part_init);
		
		long start = System.currentTimeMillis();
		Grph g = recoverGraph(degSeq, sG, part_init);
		System.out.println("recoverGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		//
		return g;
	}
	
	
	///////////////////////////////////////////////////////	
	public static void main(String[] args) throws Exception{
		// load graph
//		String dataname = "polbooks";		// (105, 441)
//		String dataname = "polblogs";		// (1224,16715) 
		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 		
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		//
//		String dataname = "wiki-Vote-wcc";	// (7066,100736)
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872)		// k = n^1/3, recoverGraph: 8.4s	k = n^1/2, recoverGraph: 2.6s
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)	
//		String dataname = "com_youtube_ungraph";	// (1134890,2987624) 	// k = n^1/3, recoverGraph: 86.2s	k = n^1/2, recoverGraph: 16.7s
		//
		
		// COMMAND-LINE
		String prefix = "";
	    int n_samples = 20;
		double eps = 2.0;		
		int k = 10;
		
		if(args.length >= 5){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			eps = Double.parseDouble(args[3]);
			k = Integer.parseInt(args[4]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("eps = " + eps);
		System.out.println("k = " + k);
		
		//
		String filename = prefix + "_data/" + dataname + ".gr";
		String seq_file = prefix + "_out/" + dataname + "_1k_" + String.format("%.1f",eps);
		String sample_file = prefix + "_sample/" + dataname + "_sg1k_" + String.format("%.1f", eps) + "_" + k;
	    System.out.println("seq_file = " + seq_file);
		
	    //
		Grph G;
		EdgeListReader reader = new EdgeListReader();
		RegularFile f = new RegularFile(filename);
		
		G = reader.readGraph(f);
		
		long start = System.currentTimeMillis();
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		//
//		int n_nodes = G.getNumberOfVertices();	
////		int k = (int)Math.sqrt(n_nodes);
//		int k = (int)Math.pow(n_nodes, 1.0/3);
////		int k = 8;
//		System.out.println("k = " + k);
		
		//
		for (int i = 0; i < n_samples; i++){
	    	System.out.println("sample i = " + i);
	    	
	    	start = System.currentTimeMillis();
	    	Grph g = greedyReconstruct(G, eps, k, seq_file);
	    	System.out.println("greedyReconstruct - DONE, elapsed " + (System.currentTimeMillis() - start));
	    	
	    	f = new RegularFile(sample_file + "." + i);
			EdgeListWriter writer = new EdgeListWriter();
	    	writer.writeGraph(g, f);
		}
		
		
	}

}
