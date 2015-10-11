/*
 * Sep 28, 2015
 * 	- implement paper "Privacy-Integrated Graph Clustering Through Differential Privacy" (EDBTw'15)
 * 	- perturbGraphAndLouvain() use routines from GreedyReconstruct (TmF)
 * Oct 11
 * 	- add eps=0.5 in perturbGraph()
 * 	- COMMAND-LINE
 */

package dp.combined;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import naive.GreedyReconstruct;
import algs4.EdgeWeightedGraph;
import toools.io.file.RegularFile;
import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;
import grph.io.EdgeListReader;
import grph.io.EdgeListWriter;
import hist.GraphIntSet;
import dp.DPUtil;

public class EdgeFlip {

	////
	public static Grph perturbGraph(Grph G, double eps){
		int n = G.getNumberOfVertices();
		int m = G.getNumberOfEdges();
		double eps2 = 0.5;
		int m_noisy = m + DPUtil.geometricMechanism(Math.exp(-eps2));
		double eps_t = Math.log(n/2.0*(n-1)/m_noisy - 1);
		double eps1 = eps - eps2;
		double s = 2/(Math.exp(eps) + 1);
		double s_noisy = 2/(Math.exp(eps1) + 1);
		
		System.out.println("esp_t = " + eps_t);
		System.out.println("esp = " + eps);
		System.out.println("esp2 = " + eps2);
		System.out.println("s = " + s);
		System.out.println("s_noisy = " + s_noisy);
		
		long dist = (long)((double)n*s*(n-1)/8); // edit distance
		System.out.println("edit distance = " + dist);
		
		//
		Grph aG = new InMemoryGrph();
		aG.addNVertices(n);
		
		Random random = new Random();
	
		// 1-edges 
		int n_ones = 0;
		for (VertexPair p : G.getEdgePairs()){
			int u = p.first;
			int v = p.second;
			
			double rand = random.nextDouble();
			if (rand < 1-s_noisy/2){
				aG.addSimpleEdge(u, v, false);
				n_ones += 1;
			}
		}
		
		// 0-edges
		int n_zeros = 0;
		if (eps + eps2 > eps_t)
			n_zeros = (int)((n/2.0*(n-1) - m_noisy)*s_noisy/2);		// int overflow !	// NOTE: m is not private, must use m_noisy
		else
			n_zeros = m_noisy - n;
			
		System.out.println("n_zeros = " + n_zeros);
		
		GraphIntSet I = new GraphIntSet(G);
		int count = 0;
		while (count < n_zeros){
			int u = random.nextInt(n);
			int v = random.nextInt(n);
			if (!I.hasEdge(u, v)){		// 0-cell
				aG.addSimpleEdge(u, v, false);
				count++;
			}
		}
		
		//
		return aG;
	}
	
	////
	public static void perturbGraphAndLouvain(Grph G, double eps, String sample_file, String part_file, int n_samples) throws IOException{
		
		RegularFile f;
		Map<Integer, Integer> part;
		
		for (int i = 0; i < n_samples; i++){
			Grph aG = perturbGraph(G, eps);
			
			f = new RegularFile(sample_file + "." + i);
			EdgeListWriter writer = new EdgeListWriter();
	    	writer.writeGraph(aG, f);
	    	System.out.println("writeGraph - DONE");
			
			EdgeWeightedGraph G2 = GreedyReconstruct.convertGraph(aG);
			System.out.println("convertGraph - DONE");
			
			
			Louvain lv = new Louvain();
			
			long start = System.currentTimeMillis();
			part = lv.best_partition(G2, null);
			System.out.println("best_partition - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			// compute modularity using G and part
			System.out.println("real modularity = " + GreedyReconstruct.modularity(G, part));
			
			//
			Louvain.writePart(part, part_file + "-" + i + ".part");
			System.out.println("writePart - DONE");
			
		}
		
	}
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		System.out.println("EdgeFlip");
		
		// load graph
//		String dataname = "polbooks";		// (105, 441)		
//		String dataname = "polblogs";		// (1224,16715) 	
//		String dataname = "as20graph";		// (6474,12572)		
//		String dataname = "wiki-Vote";		// (7115,100762)
//		String dataname = "ca-HepPh";		// (12006,118489) 	
//		String dataname = "ca-AstroPh";		// (18771,198050) 	
		// LARGE
//		String dataname = "com_amazon_ungraph";		// (334863,925872) 
//		String dataname = "com_dblp_ungraph";		// (317080,1049866)
		String dataname = "com_youtube_ungraph";	// (1134890,2987624)
		
		// COMMAND-LINE <prefix> <dataname> <n_samples> <eps>
		String prefix = "";
	    int n_samples = 20;
	    double eps = 1.0;
		
		if(args.length >= 4){
			prefix = args[0];
			dataname = args[1];
			n_samples = Integer.parseInt(args[2]);
			eps = Double.parseDouble(args[3]);
		}
		System.out.println("dataname = " + dataname);
		System.out.println("n_samples = " + n_samples);
		System.out.println("eps = " + eps);
		
		String filename = prefix + "_data/" + dataname + ".gr";
		String sample_file = prefix + "_sample/" + dataname + "_ef_" + String.format("%.1f", eps);
		System.out.println("sample_file = " + sample_file);
		
		
	    Grph G;
	    EdgeListReader reader = new EdgeListReader();
		RegularFile f = new RegularFile(filename);
		long start = System.currentTimeMillis();
		G = reader.readGraph(f);
		System.out.println("#nodes = " + G.getNumberOfVertices());
		System.out.println("#edges = " + G.getNumberOfEdges());  
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
	    
	    for (int i = 0; i < n_samples; i++){
	    	System.out.println("sample i = " + i);
	    	
	    	start = System.currentTimeMillis();
	    	Grph aG = perturbGraph(G, eps);
	    	System.out.println("perturbGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
	    	
			f = new RegularFile(sample_file + "." + i);
			EdgeListWriter writer = new EdgeListWriter();
	    	writer.writeGraph(aG, f);
	    }

	}

}
