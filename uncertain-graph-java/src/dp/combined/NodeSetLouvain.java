/*
 * Sep 28, 2015
 * 	- exponential mechanism via MCMC on K groups of nodes (first pass of Louvain method)
 */

package dp.combined;

import hist.Int2;
import hist.Int2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import algs4.Edge;
import algs4.EdgeWeightedGraph;

import com.carrotsearch.hppc.cursors.IntCursor;

import dp.DPUtil;
import dp.mcmc.Dendrogram;
import dp.mcmc.Node;


public class NodeSetLouvain {

	//
	public int[] part;	//	part[i] = 0 -> K-1 is the partition of the node i 
	public int[] size;	//	number of nodes in partition k
	public int[] lc;	// 	number of intra-edges
	public int[] dc;	// 	
	
	////
	public NodeSetLouvain(EdgeWeightedGraph G, int k){
		int n = G.V();
		
		this.part = new int[n];
		this.size = new int[k];
		this.lc = new int[k];
		this.dc = new int[k];
		//
		for (int i = 0; i < k-1; i++){
			for (int j = 0; j < n/k; j++)
				this.part[i*(n/k) + j] = i;
			this.size[i] = n/k;
		}

		int m = n - (k-1)*(n/k);
		for (int j = 0; j < m; j++)
			this.part[(k-1)*(n/k) + j] = k-1;
		this.size[k-1] = m;
		
		// lc, dc
		int u; 
		int v;
		for (Edge e : G.edges()){
			u = e.either();
			v = e.other(u);
			if (this.part[u] == this.part[v])
				this.lc[this.part[u]] += 1;
			this.dc[this.part[u]] += 1;
			this.dc[this.part[v]] += 1;
		}
		
	}
	
	////
	public double modularity(int m){
		double mod = 0.0;
		
		for (int i = 0; i < lc.length; i++)
			mod += (double)this.lc[i]/m - (this.dc[i]/(2.0*m)) *(this.dc[i]/(2.0*m));
		//
		return mod;
	}
	
	//// move node u to group 'dest'
	public void move(int u, int dest, EdgeWeightedGraph G){
		
		
		int deg_u = G.degree(u);
		this.dc[part[u]] -= deg_u;
		this.dc[dest] += deg_u;
		
		for (int v : G.adj(u).keySet()){
			if (this.part[v] == this.part[u])
				this.lc[part[u]] -= 1;
			if (this.part[v] == dest)
				this.lc[dest] += 1;
		}
		
		//
		this.part[u] = dest;
	}
	
	
	////
	public void partitionMod(EdgeWeightedGraph G, double eps_p, int n_steps, int n_samples, int sample_freq){
		
		int n_nodes = G.V();
		int n_edges = G.E();
		int k = this.lc.length;
		
		// compute dU
	    double dU = 8.0/n_edges;
	    
	    System.out.println("#steps = " + (n_steps + n_samples * sample_freq));
		//
	    Random random = new Random();
		int n_accept = 0;
		int n_accept_positive = 0;
		int u = -1;
		int dest = -1;
		int old_dest = -1;
		
		double modT = this.modularity(n_edges);
		double modT2;
		
		for (int i = 0; i < n_steps + n_samples * sample_freq; i++) {
			
			// decide (u, dest)
			u = random.nextInt(n_nodes);
			dest = random.nextInt(k);
			while (dest == this.part[u])
				dest = random.nextInt(k);
			
			old_dest = this.part[u];
			move(u, dest, G);
			
			// MCMC
			modT2 = this.modularity(n_edges);
			
			if (modT2 > modT){
				n_accept += 1;
				n_accept_positive += 1;
				modT = modT2;
				
			}else{
				double prob = Math.exp(eps_p/(2*dU)*(modT2 - modT));			// prob ~ 1.0
				// System.out.println("prob = " + prob);
				double prob_val = random.nextDouble();
				
				if (prob_val > prob){
					// reverse
					move(u, old_dest, G);
					
				}else {
					n_accept += 1;
					modT = modT2;
					//
				}
			}
			
			
		}
	}
	
	////
	public void writePart(String part_file) throws IOException{

		List<List<Integer>> com = new ArrayList<List<Integer>>();
		int k = this.lc.length;
		for (int i = 0; i < k; i++)
			com.add(new ArrayList<Integer>());
		
		for (int i = 0; i < this.part.length; i++)
			com.get(this.part[i]).add(i);
		
		//
		BufferedWriter bw = new BufferedWriter(new FileWriter(part_file));
		for (List<Integer> list:com){
			for (int u : list)
				bw.write(u + ",");
			bw.write("\n");
		}
		
		bw.close();
	}
	
}
