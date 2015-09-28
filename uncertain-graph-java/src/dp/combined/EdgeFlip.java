/*
 * Sep 28, 2015
 * 	- implement paper "Privacy-Integrated Graph Clustering Through Differential Privacy" (EDBTw'15)
 * 
 */

package dp.combined;

import java.util.Random;

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

		double s = 2/(Math.exp(eps) + 1);
		System.out.println("s = " + s);
		
		int dist = (int)((double)n*(n-1)*s/8); // edit distance
		System.out.println("edit distance = " + dist);
		
		//
		Grph aG = new InMemoryGrph();
		aG.addNVertices(n);
		
		Random random = new Random();
	
		// 1-edges 
		for (VertexPair p : G.getEdgePairs()){
			int u = p.first;
			int v = p.second;
			
			double rand = random.nextDouble();
			if (rand < 1-s)
				aG.addSimpleEdge(u, v, false);
		}
		
		// 0-edges
		int n_zeros = (int)((n*(n-1)/2 - m)*s/2);
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
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		String dataname;
		int n_samples = 1;
		String[] dataname_list = new String[]{"com_amazon_ungraph"}; //, "com_dblp_ungraph", "com_youtube_ungraph"};
		double[][] eps_list = new double[][]{{10.0, 20.0, 30.0}, {10.0, 20.0, 30.0}, {10.0, 20.0, 30.0}};
	    
	    Grph G;
	    
	    for (int i = 0; i < dataname_list.length; i++){
	    	dataname = dataname_list[i];
	    	
	    	System.out.println("dataname = " + dataname);
	    	
	    	String filename = "_data/" + dataname + ".gr";
	    	
			EdgeListReader reader = new EdgeListReader();
			RegularFile f = new RegularFile(filename);
			long start = System.currentTimeMillis();
			G = reader.readGraph(f);
			System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
			
			System.out.println("#nodes = " + G.getNumberOfVertices());
			System.out.println("#edges = " + G.getNumberOfEdges());    	
			
	    	for (double eps : eps_list[i]){
	    		
	    		String sample_file = "_sample/" + dataname + "_edgeflip_" + String.format("%.1f", eps);
	    		
	    		System.out.println("eps = " + eps);
	    		
	    		start = System.currentTimeMillis();
	    		Grph aG = perturbGraph(G, eps);
				System.out.println("perturbGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
				System.out.println("#nodes = " + aG.getNumberOfVertices());
				System.out.println("#edges = " + aG.getNumberOfEdges());
	    		
	    		f = new RegularFile(sample_file + "." + i);
				EdgeListWriter writer = new EdgeListWriter();
		    	writer.writeGraph(aG, f);
	    	}
	    }

	}

}
