/*
 * Mar 18, 2016
 * 	- "Link Exchange" problem(s)
 * 
 */

package dsn;

import hist.Int2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import algs4.EdgeInt;
import algs4.EdgeIntGraph;


public class LinkExchange {

	
	////
	public static List<Int2> createFalseLink(EdgeIntGraph G, int u, double beta){
		List<Int2> ret = new ArrayList<Int2>();
		
		int n = G.V();
		//
		for (int v:G.adj(u).keySet())
			ret.add(new Int2(u, v));
		
		//
		Random random = new Random();
		for (int i = 0; i < beta*G.degree(u); i++){
			int w = random.nextInt(n);
			while (G.areEdgesAdjacent(u, w))
				w = random.nextInt(n);
			
			ret.add(new Int2(u, w));
			
		}
		
		
		//
		return ret;
	}
	
	////
	public static List<Int2> sampleLink(List<Int2> srcList, double alpha){
		List<Int2> ret = new ArrayList<Int2>();
		
		Random random = new Random();
		for (int i = 0; i < alpha*srcList.size(); i++){
			int k = random.nextInt(srcList.size());
			
			ret.add(srcList.get(k));
			
		}
		
		//
		return ret;
		
	}
	
	////
	public static void linkExchange(EdgeIntGraph G, int round, double alpha, double beta, String count_file) throws IOException{
		int n = G.V();
		
		List<List<Int2>> links = new ArrayList<List<Int2>>();
		
		long start = System.currentTimeMillis();
		// initial stage
		for (int u = 0; u < n; u++)
			links.add(createFalseLink(G, u, beta));
		
		
		// loop
		for(int t = 1; t < round+1; t++){
			List<List<Int2>> exLinks = new ArrayList<List<Int2>>();		// new links received at each node
			for (int u = 0; u < n; u++)
				exLinks.add(new ArrayList<Int2>());
			
			// for each pair of nodes (u,v)
			for (EdgeInt e: G.edges()){
				int u = e.either();
				int v = e.other(u);
				
				List<Int2> listU = sampleLink(links.get(u), alpha);
				List<Int2> listV = sampleLink(links.get(v), alpha);
				
				//
				exLinks.get(u).addAll(listV);
				exLinks.get(v).addAll(listU);
				
			}
			// expand lists
			for (int u = 0; u < n; u++)
				links.get(u).addAll(exLinks.get(u));
		}
		
		// count true/false/duplicate links
		int[] trueLinks = new int[n];
		int[] falseLinks = new int[n];
		int[] dupLinks = new int[n];
		for (int u = 0; u < n; u++){
			Map<Int2, Integer> dup = new HashMap<Int2, Integer>();
			for(Int2 p : links.get(u)){
				if(p.val0 > p.val1){	// normalize
					int temp = p.val0;
					p.val0 = p.val1;
					p.val1 = temp;
				}
				
				if (dup.containsKey(p)){
					dupLinks[u] += 1;
				}else{
					dup.put(p, 1);
					if (G.areEdgesAdjacent(p.val0, p.val1))
						trueLinks[u] += 1;
					else
						falseLinks[u] += 1;
				}
			}
			
		}
		System.out.println("linkExchange - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		// write to file
		BufferedWriter bw = new BufferedWriter(new FileWriter(count_file));
		for (int u = 0; u < n; u++){
			bw.write(trueLinks[u] + "\t" + falseLinks[u] + "\t" + dupLinks[u] + "\n");
		}
		bw.close();
		System.out.println("Written to count_file.");
		
	}
	
	
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		String prefix = "";
		String dataname = "pl_1000_5_01";
		
		
		String filename = prefix + "_data/" + dataname + ".gr";
		
		//
		long start = System.currentTimeMillis();
		EdgeIntGraph G = EdgeIntGraph.readEdgeList(filename, "\t");	// "\t" or " "
		System.out.println("readGraph - DONE, elapsed " + (System.currentTimeMillis() - start));
		
		System.out.println("#nodes = " + G.V());
		System.out.println("#edges = " + G.E());

		
		//
		int round = 0;
		double alpha = 0.5;
		double beta = 1.0;
		String count_file = prefix + "_out/" + dataname + "-" + round + "_" + String.format("%.1f",alpha) + "_" + String.format("%.1f",beta) + ".cnt";
		
		linkExchange(G, round, alpha, beta, count_file);
		
		
	}

}
